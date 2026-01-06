package com.example.finalproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class activity_user_page extends BaseActivity {

    private static final String TAG = "UserPageActivity";

    // Views
    private TextView tvEmail, tvMsgUser, tvFavorites;
    private EditText etUsername, etBirthYear, etMovie, etSeries, etGenre;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ טוען את הדף בתוך BaseActivity (כולל תפריט)
        setPageContent(R.layout.activity_user_page);

        // Firebase init
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views
        tvEmail = findViewById(R.id.tvEmail);
        tvMsgUser = findViewById(R.id.tvMsgUser);
        tvFavorites = findViewById(R.id.tvFavorites);

        etUsername = findViewById(R.id.etUsername);
        etBirthYear = findViewById(R.id.etBirthYear);
        etMovie = findViewById(R.id.etMovie);
        etSeries = findViewById(R.id.etSeries);
        etGenre = findViewById(R.id.etGenre);

        findViewById(R.id.btnSaveUser).setOnClickListener(v -> saveUserData());

        loadUserData();
    }

    // =====================================================
    // טעינת נתוני משתמש
    // =====================================================
    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No logged-in user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();
        tvEmail.setText(currentUser.getEmail());

        userDocRef = db.collection("users").document(uid);

        tvMsgUser.setText("Loading user data...");

        userDocRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting document", task.getException());
                tvMsgUser.setTextColor(0xFFFF6B6B);
                tvMsgUser.setText("Error loading user data");
                loadFavorites();
                return;
            }

            DocumentSnapshot document = task.getResult();
            if (document != null && document.exists()) {

                String username = document.getString("username");
                String birthYear = document.getString("birthYear");
                String movie = document.getString("favoriteMovie");
                String series = document.getString("favoriteSeries");
                String genre = document.getString("favoriteGenre");

                if (username != null) etUsername.setText(username);
                if (birthYear != null) etBirthYear.setText(birthYear);
                if (movie != null) etMovie.setText(movie);
                if (series != null) etSeries.setText(series);
                if (genre != null) etGenre.setText(genre);

                tvMsgUser.setText("");
            } else {
                tvMsgUser.setTextColor(0xFFFF6B6B);
                tvMsgUser.setText("No data found yet. You can fill and save.");
                Log.d(TAG, "No user document for uid=" + uid);
            }

            // ⭐ טוען מועדפים בכל מקרה
            loadFavorites();
        });
    }

    // =====================================================
    // טעינת מועדפים
    // =====================================================
    private void loadFavorites() {
        if (userDocRef == null) {
            tvFavorites.setText("(אין משתמש)");
            return;
        }

        userDocRef.collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        tvFavorites.setText("(אין מועדפים עדיין)");
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String title = doc.getString("title");
                        if (!TextUtils.isEmpty(title)) {
                            if (sb.length() > 0) sb.append("\n");
                            sb.append("• ").append(title);
                        }
                    }

                    tvFavorites.setText(
                            sb.length() == 0 ? "(אין מועדפים עדיין)" : sb.toString()
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading favorites", e);
                    tvFavorites.setText("שגיאה בטעינת מועדפים");
                });
    }

    // =====================================================
    // שמירת נתוני משתמש
    // =====================================================
    private void saveUserData() {
        if (userDocRef == null) {
            Toast.makeText(this, "User document not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = etUsername.getText().toString().trim();
        String birthYear = etBirthYear.getText().toString().trim();
        String movie = etMovie.getText().toString().trim();
        String series = etSeries.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();

        userDocRef.update(
                "username", username,
                "birthYear", birthYear,
                "favoriteMovie", movie,
                "favoriteSeries", series,
                "favoriteGenre", genre
        ).addOnSuccessListener(a ->
                Toast.makeText(this, "הפרטים נשמרו 💜", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show()
        );
    }
}
