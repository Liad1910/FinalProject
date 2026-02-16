package com.example.finalproject;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    public static final String EXTRA_OPEN_TAB_ID = "OPEN_TAB_ID";

    private String movieId;
    private String movieTitle;
    private String trailerUrl;
    private int posterResId;
    private String posterUrl;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DocumentReference userDocRef;

    private RatingBar ratingBarReview;
    private EditText etReviewText;
    private Button btnSendReview;
    private LinearLayout layoutReviewsList;
    private TextView tvNoReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_content);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            userDocRef = db.collection("users").document(currentUser.getUid());
        }

        TextView tvTitle = findViewById(R.id.tvMovieTitle);
        ImageView imgPoster = findViewById(R.id.imgPoster);
        Button btnTrailer = findViewById(R.id.btnTrailer);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnFavorites = findViewById(R.id.btnFavorites);
        Button btnWatchlist = findViewById(R.id.btnWatchlist);

        ratingBarReview = findViewById(R.id.ratingBarReview);
        etReviewText = findViewById(R.id.etReviewText);
        btnSendReview = findViewById(R.id.btnSendReview);

        layoutReviewsList = findViewById(R.id.layoutReviewsList);
        tvNoReviews = findViewById(R.id.tvNoReviews);

        setupBottomNav();
        setupButtons(btnTrailer, btnShare, btnFavorites);
        setupReviewForm();

        btnWatchlist.setOnClickListener(v -> {
            if (userDocRef == null || movieId == null) {
                Toast.makeText(this, "צריך להתחבר", Toast.LENGTH_SHORT).show();
                return;
            }
            saveToWatchlist();
        });

        String titleId = getIntent().getStringExtra(EXTRA_TITLE_ID);
        if (titleId != null && !titleId.trim().isEmpty()) {
            loadTitleFromFirestore(titleId, tvTitle, imgPoster);
            return;
        }

        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        trailerUrl = getIntent().getStringExtra(EXTRA_TRAILER_URL);
        posterResId = getIntent().getIntExtra(EXTRA_POSTER_RES_ID, 0);

        tvTitle.setText(movieTitle != null ? movieTitle : "Movie");

        if (posterResId != 0) {
            imgPoster.setImageResource(posterResId);
        } else {
            imgPoster.setImageResource(R.drawable.poster_default_movie);
        }

        loadReviews();
    }

    // ================= Bottom Nav =================
    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        bottomNav.setOnItemSelectedListener(item -> {
            Intent i = new Intent(MovieContentActivity.this, MainActivity.class);
            i.putExtra(EXTRA_OPEN_TAB_ID, item.getItemId());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
            return true;
        });
    }

    // ================= Buttons =================
    private void setupButtons(Button btnTrailer, Button btnShare, Button btnFavorites) {

        btnTrailer.setOnClickListener(v -> {
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl)));
                return;
            }
            String q = (movieTitle != null ? movieTitle : "trailer") + " official trailer";
            Uri uri = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(q));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        });

        btnShare.setOnClickListener(v -> {
            String safeTitle = (movieTitle != null && !movieTitle.trim().isEmpty()) ? movieTitle : "Movie";
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, safeTitle);
            i.putExtra(Intent.EXTRA_TEXT, safeTitle + (trailerUrl != null ? "\n" + trailerUrl : ""));
            startActivity(Intent.createChooser(i, "שתף דרך"));
        });

        btnFavorites.setOnClickListener(v -> {
            if (userDocRef == null || movieId == null) {
                Toast.makeText(this, "צריך להתחבר", Toast.LENGTH_SHORT).show();
                return;
            }
            saveToFavorites();
        });
    }

    // ================= Watchlist =================
    private void saveToWatchlist() {
        Map<String, Object> data = new HashMap<>();
        data.put("movieId", movieId);
        data.put("title", movieTitle);
        data.put("posterUrl", posterUrl);
        data.put("addedAt", System.currentTimeMillis());

        userDocRef.collection("watchlist").document(movieId).set(data)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "נוסף לרשימת צפייה 🔔", Toast.LENGTH_SHORT).show();
                    WatchlistReminderScheduler.scheduleDaily(this);
                });
    }

    // ================= Reviews =================
    private void setupReviewForm() {
        if (currentUser == null) {
            ratingBarReview.setIsIndicator(true);
            etReviewText.setEnabled(false);
            btnSendReview.setOnClickListener(v ->
                    Toast.makeText(this, "צריך להתחבר כדי לכתוב ביקורת", Toast.LENGTH_SHORT).show());
        } else {
            btnSendReview.setOnClickListener(v -> sendReview());
        }
    }

    private void sendReview() {
        float rating = ratingBarReview.getRating();
        String text = etReviewText.getText().toString().trim();

        if (rating == 0 || text.isEmpty()) {
            Toast.makeText(this, "מלאי דירוג וטקסט", Toast.LENGTH_SHORT).show();
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

        db.collection("reviews").add(review).addOnSuccessListener(a -> {
            etReviewText.setText("");
            ratingBarReview.setRating(0);
            loadReviews();
        });
    }

    private void loadReviews() {
        if (movieId == null) return;

        db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(q -> {
                    layoutReviewsList.removeAllViews();
                    if (q.isEmpty()) {
                        tvNoReviews.setVisibility(android.view.View.VISIBLE);
                        return;
                    }
                    tvNoReviews.setVisibility(android.view.View.GONE);
                    for (DocumentSnapshot d : q) addReviewView(d);
                });
    }

    private void addReviewView(DocumentSnapshot doc) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(Color.parseColor("#151528"));
        int p = dp(10);
        box.setPadding(p, p, p, p);

        TextView user = new TextView(this);
        user.setText(doc.getString("userEmail"));
        user.setTextColor(Color.parseColor("#FF67F3"));
        user.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView rate = new TextView(this);
        Double r = doc.getDouble("rating");
        rate.setText("⭐ " + (r != null ? r.intValue() : 0) + "/5");
        rate.setTextColor(Color.parseColor("#FFC94F"));

        TextView text = new TextView(this);
        text.setText(doc.getString("text"));
        text.setTextColor(Color.WHITE);

        box.addView(user);
        box.addView(rate);
        box.addView(text);

        layoutReviewsList.addView(box);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private void loadTitleFromFirestore(String id, TextView tvTitle, ImageView imgPoster) {
        db.collection("titles").document(id).get().addOnSuccessListener(doc -> {
            movieId = id;
            movieTitle = doc.getString("title");
            trailerUrl = doc.getString("trailerUrl");
            posterUrl = doc.getString("posterUrl");

            tvTitle.setText(movieTitle != null ? movieTitle : "Movie");

            if (posterUrl != null && !posterUrl.isEmpty()) {
                Glide.with(this).load(posterUrl)
                        .placeholder(R.drawable.poster_default_movie)
                        .into(imgPoster);
                posterResId = R.drawable.poster_default_movie;
            } else {
                imgPoster.setImageResource(R.drawable.poster_default_movie);
            }

            loadReviews();
        });
    }

    private void saveToFavorites() {
        Map<String, Object> data = new HashMap<>();
        data.put("movieId", movieId);
        data.put("title", movieTitle);
        data.put("trailerUrl", trailerUrl);
        data.put("posterUrl", posterUrl);
        data.put("posterResId", posterResId);
        data.put("addedAt", System.currentTimeMillis());

        userDocRef.collection("favorites").document(movieId).set(data)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "נוסף למועדפים 💖", Toast.LENGTH_SHORT).show());
    }
}
