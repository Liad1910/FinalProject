package com.example.finalproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class activity_user_page extends BaseActivity {

    private static final String TAG = "UserPageActivity";

    private TextView tvEmail, tvMsgUser, tvFavorites;
    private EditText etUsername, etBirthYear, etMovie, etSeries, etGenre;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_page);

        // Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views
        tvEmail = findViewById(R.id.tvEmail);
        tvMsgUser = findViewById(R.id.tvMsgUser);
        tvFavorites = findViewById(R.id.tvFavorites);   // ⭐ חדש

        etUsername = findViewById(R.id.etUsername);
        etBirthYear = findViewById(R.id.etBirthYear);
        etMovie = findViewById(R.id.etMovie);
        etSeries = findViewById(R.id.etSeries);
        etGenre = findViewById(R.id.etGenre);

        findViewById(R.id.btnSaveUser).setOnClickListener(v -> saveUserData());

        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No logged-in user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();
        tvEmail.setText(currentUser.getEmail());

        // אותה קולקציה כמו בהרשמה: "users" אות קטנה
        userDocRef = db.collection("users").document(uid);

        tvMsgUser.setText("Loading user data...");

        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
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

                    tvMsgUser.setText(""); // הכל טוב

                    // ⭐ אחרי שהמשתמש נטען – נטען גם מועדפים
                    loadFavorites();

                } else {
                    tvMsgUser.setTextColor(0xFFFF0000);
                    tvMsgUser.setText("No data found yet. You can fill and save.");
                    Log.d(TAG, "No such document for user " + uid);

                    // גם אם אין מסמך עדיין, אפשר לנסות לטעון מועדפים
                    loadFavorites();
                }
            } else {
                Exception e = task.getException();
                Log.e(TAG, "Error getting document", e);
                tvMsgUser.setTextColor(0xFFFF0000);
                tvMsgUser.setText("Error loading user data: " +
                        (e != null ? e.getMessage() : ""));
            }
        });
    }

    // ⭐ פונקציה חדשה – טעינת מועדפים
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

                    if (sb.length() == 0) {
                        tvFavorites.setText("(אין מועדפים עדיין)");
                    } else {
                        tvFavorites.setText(sb.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading favorites", e);
                    tvFavorites.setText("שגיאה בטעינת מועדפים");
                });
    }

    private void saveUserData() {
        if (userDocRef == null) {
            Toast.makeText(this, "User document not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        // כאן שיהיה מה שכבר כתבת (שמירת username, birthYear, וכו')
        // לא נגעתי בזה כדי לא לשבור דברים :)
    }
}
