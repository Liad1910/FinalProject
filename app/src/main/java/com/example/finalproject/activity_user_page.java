package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class activity_user_page extends AppCompatActivity {

    private static final String TAG = "UserPageActivity";

    private TextView tvEmail, tvMsgUser;
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

                } else {
                    tvMsgUser.setTextColor(0xFFFF0000);
                    tvMsgUser.setText("No data found yet. You can fill and save.");
                    Log.d(TAG, "No such document for user " + uid);
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

    private void saveUserData() {
        if (userDocRef == null) {
            Toast.makeText(this, "User document not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        tvMsgUser.setTextColor(0xFFFF0000);
        tvMsgUser.setText("");   // מנקים הודעות קודמות

        String username = etUsername.getText().toString().trim();
        String birthYear = etBirthYear.getText().toString().trim();
        String movie = etMovie.getText().toString().trim();
        String series = etSeries.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        tvMsgUser.setText("Saving...");

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("birthYear", birthYear);
        data.put("favoriteMovie", movie);
        data.put("favoriteSeries", series);
        data.put("favoriteGenre", genre);

        // set + merge = גם אם אין מסמך, הוא ייווצר. ואם יש – יעדכן רק את השדות האלה.
        userDocRef.set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // הודעת הצלחה
                    tvMsgUser.setTextColor(0xFF00FF00); // ירוק
                    tvMsgUser.setText("User data saved!");

                    // מעבר לדף הראשי (MainActivity) אחרי שמירה
                    Intent intent = new Intent(activity_user_page.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data", e);
                    tvMsgUser.setTextColor(0xFFFF0000); // אדום
                    tvMsgUser.setText("Error saving user data: " + e.getMessage());
                });
    }
}
