package com.example.finalproject;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class MovieContentActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_ID = "EXTRA_MOVIE_ID";
    public static final String EXTRA_MOVIE_TITLE = "EXTRA_MOVIE_TITLE";
    public static final String EXTRA_TRAILER_URL = "EXTRA_TRAILER_URL";
    public static final String EXTRA_POSTER_RES_ID = "EXTRA_POSTER_RES_ID";

    public static final String EXTRA_TITLE_ID = "EXTRA_TITLE_ID";

    private String movieId;
    private String movieTitle;
    private String trailerUrl;
    private int posterResId;

    private String titleId;

    private FirebaseFirestore db;
    private DocumentReference userDocRef;
    private FirebaseUser currentUser;

    private RatingBar ratingBarReview;
    private EditText etReviewText;
    private Button btnSendReview;

    private LinearLayout layoutReviewsList;
    private TextView tvNoReviews;

    private String posterUrl; // URL מה-TMDB (אם יש)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_content);

        Toolbar toolbar = findViewById(R.id.toolbarMovie);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("");

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userDocRef = db.collection("users").document(currentUser.getUid());
        }

        TextView tvTitle = findViewById(R.id.tvMovieTitle);
        ImageView imgPoster = findViewById(R.id.imgPoster);
        Button btnTrailer = findViewById(R.id.btnTrailer);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnFavorites = findViewById(R.id.btnFavorites);

        ratingBarReview = findViewById(R.id.ratingBarReview);
        etReviewText = findViewById(R.id.etReviewText);
        btnSendReview = findViewById(R.id.btnSendReview);

        layoutReviewsList = findViewById(R.id.layoutReviewsList);
        tvNoReviews = findViewById(R.id.tvNoReviews);

        setupButtons(btnTrailer, btnShare, btnFavorites);
        setupReviewForm();

        // ✅ מצב חדש (Firestore)
        titleId = getIntent().getStringExtra(EXTRA_TITLE_ID);
        if (titleId != null && !titleId.trim().isEmpty()) {
            loadTitleFromFirestore(titleId, tvTitle, imgPoster);
            return;
        }

        // מצב ישן
        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        trailerUrl = getIntent().getStringExtra(EXTRA_TRAILER_URL);
        posterResId = getIntent().getIntExtra(EXTRA_POSTER_RES_ID, 0);

        tvTitle.setText(movieTitle != null ? movieTitle : "Movie");

        if (posterResId != 0) imgPoster.setImageResource(posterResId);
        else imgPoster.setImageResource(R.drawable.poster_default_movie);

        loadReviews();
    }

    private void setupButtons(Button btnTrailer, Button btnShare, Button btnFavorites) {

        btnTrailer.setOnClickListener(v -> {
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl)));
            } else {
                Toast.makeText(this, "אין טריילר לעמוד הזה", Toast.LENGTH_SHORT).show();
            }
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            String subject = (movieTitle != null && !movieTitle.isEmpty()) ? movieTitle : "סרט/סדרה שווה";
            String text = subject + ((trailerUrl != null && !trailerUrl.isEmpty()) ? "\nטריילר: " + trailerUrl : "");

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);

            startActivity(Intent.createChooser(shareIntent, "שתף דרך"));
        });

        btnFavorites.setOnClickListener(v -> {
            if (userDocRef == null) {
                Toast.makeText(this, "צריך להתחבר כדי לשמור מועדפים", Toast.LENGTH_SHORT).show();
                return;
            }
            if (movieId == null || movieId.trim().isEmpty()) {
                Toast.makeText(this, "עוד רגע... הנתונים נטענים", Toast.LENGTH_SHORT).show();
                return;
            }
            saveToFavorites();
        });
    }

    private void setupReviewForm() {
        ratingBarReview.setIsIndicator(false);

        if (currentUser == null) {
            ratingBarReview.setIsIndicator(true);
            etReviewText.setEnabled(false);
            btnSendReview.setOnClickListener(v ->
                    Toast.makeText(this, "צריך להתחבר כדי לכתוב ביקורת", Toast.LENGTH_SHORT).show());
        } else {
            etReviewText.setEnabled(true);
            btnSendReview.setOnClickListener(v -> sendReview());
        }
    }

    private void sendReview() {
        if (currentUser == null) {
            Toast.makeText(this, "צריך להתחבר כדי לכתוב ביקורת", Toast.LENGTH_SHORT).show();
            return;
        }
        if (movieId == null || movieId.trim().isEmpty()) {
            Toast.makeText(this, "עוד רגע... הנתונים נטענים", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingBarReview.getRating();
        String text = etReviewText.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "תני דירוג לפני שליחה", Toast.LENGTH_SHORT).show();
            return;
        }
        if (text.isEmpty()) {
            Toast.makeText(this, "תכתבי לפחות כמה מילים :)", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> review = new HashMap<>();
        review.put("movieId", movieId);
        review.put("movieTitle", movieTitle);
        review.put("rating", rating);
        review.put("text", text);
        review.put("timestamp", System.currentTimeMillis());
        review.put("userEmail", currentUser.getEmail());
        review.put("userId", currentUser.getUid());

        db.collection("reviews")
                .add(review)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "הביקורת נשמרה! 💜", Toast.LENGTH_SHORT).show();
                    etReviewText.setText("");
                    ratingBarReview.setRating(0);
                    loadReviews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בשמירת הביקורת", Toast.LENGTH_SHORT).show());
    }

    private void loadReviews() {
        if (movieId == null || movieId.trim().isEmpty()) {
            tvNoReviews.setVisibility(View.VISIBLE);
            return;
        }

        db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    layoutReviewsList.removeAllViews();

                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvNoReviews.setVisibility(View.GONE);
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) addReviewView(doc);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בטעינת ביקורות", Toast.LENGTH_SHORT).show());
    }

    private void addReviewView(DocumentSnapshot doc) {
        String userEmail = doc.getString("userEmail");
        String text = doc.getString("text");
        Double rating = doc.getDouble("rating");

        if (userEmail == null) userEmail = "משתמש";
        if (text == null) text = "";

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.parseColor("#151528"));
        int p = dpToPx(10);
        box.setPadding(p, p, p, p);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(10));
        box.setLayoutParams(params);

        TextView tvUser = new TextView(this);
        tvUser.setText(userEmail);
        tvUser.setTextColor(Color.parseColor("#FF67F3"));
        tvUser.setTextSize(14);
        tvUser.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvRating = new TextView(this);
        if (rating != null) tvRating.setText("⭐ " + rating.intValue() + "/5");
        tvRating.setTextColor(Color.parseColor("#FFC94F"));

        TextView tvText = new TextView(this);
        tvText.setText(text);
        tvText.setTextColor(Color.WHITE);
        tvText.setTextSize(14);

        box.addView(tvUser);
        box.addView(tvRating);
        box.addView(tvText);

        layoutReviewsList.addView(box);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void saveToFavorites() {
        if (userDocRef == null) return;

        Map<String, Object> movieData = new HashMap<>();
        movieData.put("movieId", movieId);
        movieData.put("title", movieTitle);
        movieData.put("trailerUrl", trailerUrl);
        movieData.put("posterResId", posterResId);
        if (posterUrl != null && !posterUrl.trim().isEmpty()) movieData.put("posterUrl", posterUrl);
        movieData.put("addedAt", System.currentTimeMillis());

        userDocRef.collection("favorites")
                .document(movieId)
                .set(movieData)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "נוסף למועדפים ✔", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadTitleFromFirestore(String id, TextView tvTitle, ImageView imgPoster) {

        // ✅ כדי שלא יופיע לבן בזמן טעינה
        imgPoster.setImageResource(R.drawable.poster_default_movie);

        db.collection("titles").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "העמוד לא נמצא", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    movieId = id;

                    String title = doc.getString("title");
                    Long yearL = doc.getLong("year");
                    String type = doc.getString("type"); // movie / series
                    String posterResName = doc.getString("posterResName");
                    posterUrl = doc.getString("posterUrl");
                    trailerUrl = doc.getString("trailerUrl");

                    if (title == null) title = "Title";

                    String fullTitle = title;
                    if (yearL != null && yearL != 0) fullTitle = title + " (" + yearL + ")";
                    movieTitle = fullTitle;
                    tvTitle.setText(fullTitle);

                    int fallbackRes = ("series".equals(type))
                            ? R.drawable.poster_default_series
                            : R.drawable.poster_default_movie;

                    if (posterUrl != null && !posterUrl.trim().isEmpty()) {
                        Glide.with(this)
                                .load(posterUrl)
                                .placeholder(fallbackRes)
                                .error(fallbackRes)
                                .into(imgPoster);
                        posterResId = fallbackRes;
                    } else {
                        int resId = 0;
                        if (posterResName != null && !posterResName.trim().isEmpty()) {
                            resId = getResources().getIdentifier(posterResName, "drawable", getPackageName());
                        }
                        if (resId == 0) resId = fallbackRes;

                        posterResId = resId;
                        imgPoster.setImageResource(resId);
                    }

                    loadReviews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בטעינת העמוד: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // תפריט וכו' נשאר כמו אצלך – אם תרצי אשלח גם, אבל זה לא קשור לתמונה
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_drawer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // ... אצלך (כמו שהיה) ...
        return super.onOptionsItemSelected(item);
    }
}
