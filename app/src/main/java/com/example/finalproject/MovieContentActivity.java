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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MovieContentActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_ID = "EXTRA_MOVIE_ID";
    public static final String EXTRA_MOVIE_TITLE = "EXTRA_MOVIE_TITLE";
    public static final String EXTRA_TRAILER_URL = "EXTRA_TRAILER_URL";
    public static final String EXTRA_POSTER_RES_ID = "EXTRA_POSTER_RES_ID";

    private String movieId;
    private String movieTitle;
    private String trailerUrl;
    private int posterResId;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;
    private FirebaseUser currentUser;

    // UI ביקורת
    private RatingBar ratingBarReview;
    private EditText etReviewText;
    private Button btnSendReview;

    // UI רשימת ביקורות
    private LinearLayout layoutReviewsList;
    private TextView tvNoReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_content);

        // ----- Toolbar -----
        Toolbar toolbar = findViewById(R.id.toolbarMovie);
        setSupportActionBar(toolbar);
        // לא חייבים כותרת מה־Toolbar אם לא רוצים:
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        // ----- קבלת נתונים -----
        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        trailerUrl = getIntent().getStringExtra(EXTRA_TRAILER_URL);
        posterResId = getIntent().getIntExtra(EXTRA_POSTER_RES_ID, 0);

        // ----- Firebase -----
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userDocRef = db.collection("users").document(currentUser.getUid());
        }

        // ----- Views -----
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

        // ----- הצגת מידע -----
        tvTitle.setText(movieTitle != null ? movieTitle : "Movie");

        if (posterResId != 0) {
            imgPoster.setImageResource(posterResId);
        }

        // ----- כפתור טריילר -----
        btnTrailer.setOnClickListener(v -> {
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                startActivity(intent);
            } else {
                Toast.makeText(this, "אין טריילר לסרט", Toast.LENGTH_SHORT).show();
            }
        });

        // ----- כפתור שיתוף -----
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            String subject = movieTitle != null ? movieTitle : "סרט שווה";
            String text = subject + (trailerUrl != null ? "\nטריילר: " + trailerUrl : "");

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);

            startActivity(Intent.createChooser(shareIntent, "שתף דרך"));
        });

        // ----- כפתור מועדפים -----
        btnFavorites.setOnClickListener(v -> {
            if (userDocRef == null) {
                Toast.makeText(this, "צריך להתחבר כדי לשמור מועדפים", Toast.LENGTH_SHORT).show();
                return;
            }
            saveToFavorites();
        });

        // ----- טופס ביקורת -----
        setupReviewForm();

        // ----- טעינת ביקורות -----
        loadReviews();
    }

    // =====================================================
    //   תפריט אמיתי (3 נקודות) ב־Toolbar
    // =====================================================
    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, loginPage.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_drawer_menu, menu);  // nav_menu.xml שלך
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        // ---------- Movies ----------
        if (id == R.id.nav_movies_action) {
            intent = new Intent(this, MoviesCategoryActivity.class);
            intent.putExtra("genre", "Action");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_movies_comedy) {
            intent = new Intent(this, MoviesCategoryActivity.class);
            intent.putExtra("genre", "Comedy");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_movies_drama) {
            intent = new Intent(this, MoviesCategoryActivity.class);
            intent.putExtra("genre", "Drama");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_movies_horror) {
            intent = new Intent(this, MoviesCategoryActivity.class);
            intent.putExtra("genre", "Horror");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_movies_romance) {
            intent = new Intent(this, MoviesCategoryActivity.class);
            intent.putExtra("genre", "Romance");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_movies_scifi) {
            intent = new Intent(this, MoviesCategoryActivity.class);
            intent.putExtra("genre", "Sci-Fi");
            startActivity(intent);
            return true;
        }

        // ---------- Series ----------
        else if (id == R.id.nav_series_action) {
            intent = new Intent(this, SeriesCategoryActivity.class);
            intent.putExtra("genre", "Action");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_series_comedy) {
            intent = new Intent(this, SeriesCategoryActivity.class);
            intent.putExtra("genre", "Comedy");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_series_drama) {
            intent = new Intent(this, SeriesCategoryActivity.class);
            intent.putExtra("genre", "Drama");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_series_horror) {
            intent = new Intent(this, SeriesCategoryActivity.class);
            intent.putExtra("genre", "Horror");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_series_romance) {
            intent = new Intent(this, SeriesCategoryActivity.class);
            intent.putExtra("genre", "Romance");
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_series_scifi) {
            intent = new Intent(this, SeriesCategoryActivity.class);
            intent.putExtra("genre", "Sci-Fi");
            startActivity(intent);
            return true;
        }

        if (id == R.id.nav_login) {
            startActivity(new Intent(this, loginPage.class));
            return true;
        } else if (id == R.id.nav_register) {
            startActivity(new Intent(this, registerPage.class));
            return true;
        } else if (id == R.id.nav_user_page) {
            startActivity(new Intent(this, activity_user_page.class));
            return true;
        }else if (id == R.id.nav_logout) {
                performLogout();
                return true;
            }


            return super.onOptionsItemSelected(item);

    }

    // =====================================================
    //   טופס ביקורת
    // =====================================================

    private void setupReviewForm() {
        if (currentUser == null) {
            ratingBarReview.setIsIndicator(true);
            etReviewText.setEnabled(false);

            btnSendReview.setOnClickListener(v ->
                    Toast.makeText(this, "צריך להתחבר כדי לכתוב ביקורת", Toast.LENGTH_SHORT).show()
            );
        } else {
            btnSendReview.setOnClickListener(v -> sendReview());
        }
    }

    private void sendReview() {
        if (currentUser == null) {
            Toast.makeText(this, "צריך להתחבר כדי לכתוב ביקורת", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingBarReview.getRating();
        String text = etReviewText.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "תני דירוג לפני שליחה", Toast.LENGTH_SHORT).show();
            return;
        }

        if (text.isEmpty()) {
            Toast.makeText(this, "תכתבי לפחות כמה מילים על הסרט :)", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "שגיאה בשמירת הביקורת", Toast.LENGTH_SHORT).show()
                );
    }

    // =====================================================
    //   טעינת ביקורות
    // =====================================================

    private void loadReviews() {
        db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    layoutReviewsList.removeAllViews();

                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvNoReviews.setVisibility(View.GONE);

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        addReviewView(doc);
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בטעינת ביקורות", Toast.LENGTH_SHORT).show());
    }

    // =====================================================
    //   בניית תיבת ביקורת
    // =====================================================

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

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dpToPx(10));
        box.setLayoutParams(params);

        TextView tvUser = new TextView(this);
        tvUser.setText(userEmail);
        tvUser.setTextColor(Color.parseColor("#FF67F3"));
        tvUser.setTextSize(14);
        tvUser.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvRating = new TextView(this);
        if (rating != null)
            tvRating.setText("⭐ " + rating.intValue() + "/5");
        else
            tvRating.setText("");
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

    // =====================================================
    //   הוספה למועדפים
    // =====================================================

    private void saveToFavorites() {
        Map<String, Object> movieData = new HashMap<>();
        movieData.put("movieId", movieId);
        movieData.put("title", movieTitle);
        movieData.put("trailerUrl", trailerUrl);
        movieData.put("posterResId", posterResId);
        movieData.put("addedAt", System.currentTimeMillis());

        userDocRef.collection("favorites")
                .document(movieId)
                .set(movieData)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "נוסף למועדפים ✔", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
