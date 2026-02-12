package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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

    // Bottom nav
    private BottomNavigationView bottomNav;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // טוען את הדף בתוך BaseActivity
        setPageContent(R.layout.activity_user_page);

        // ❌ להסתיר תפריט עליון (ActionBar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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

        // ✅ BottomNavigation
        bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            setupBottomNav();
        } else {
            Log.e(TAG, "bottomNav not found in activity_user_page.xml (add BottomNavigationView)");
        }

        loadUserData();
    }

    // =====================================================
    // מונע יצירת תפריט עליון (overflow)
    // =====================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    // =====================================================
    // Bottom Nav setup
    // =====================================================
    private void setupBottomNav() {

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bnav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }

            if (id == R.id.bnav_movies) {
                startActivity(new Intent(this, MoviesCategoryActivity.class));
                return true;
            }

            if (id == R.id.bnav_series) {
                startActivity(new Intent(this, SeriesCategoryActivity.class));
                return true;
            }

            if (id == R.id.bnav_more) {
                showMoreDialog();
                bottomNav.getMenu().findItem(R.id.bnav_more).setChecked(false);
                return true;
            }

            return false;
        });

        // שלא ייבחר טאב אוטומטית
        bottomNav.getMenu().setGroupCheckable(0, false, true);
    }

    // =====================================================
    // Dialog "עוד" – דינמי לפי מצב התחברות + אנונימי
    // =====================================================
    private void showMoreDialog() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = (user != null && !user.isAnonymous());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("עוד");

        if (!isLoggedIn) {

            String[] options = {"התחברות", "הרשמה", "הקולנוע הקרוב", "צ'אט"};

            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        startActivity(new Intent(this, loginPage.class));
                        break;
                    case 1:
                        startActivity(new Intent(this, registerPage.class));
                        break;
                    case 2:
                        startActivity(new Intent(this, NearbyCinemaFreeActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(this, AiActivity.class));
                        break;
                }
            });

        } else {

            String[] options = {"פרופיל", "הקולנוע הקרוב", "צ'אט", "התנתקות"};

            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        // את כבר בפרופיל, אז אפשר פשוט לא לעשות כלום
                        // או לבצע רענון: recreate();
                        break;
                    case 1:
                        startActivity(new Intent(this, NearbyCinemaFreeActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(this, AiActivity.class));
                        break;
                    case 3:
                        logoutFromBottomMenu();
                        break;
                }
            });
        }

        builder.setNegativeButton("סגור", null);
        builder.show();
    }

    private void logoutFromBottomMenu() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

        updateMenuByAuthState();

        startActivity(new Intent(this, MainActivity.class));
        finish();
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

        // אם את לא רוצה להראות אימייל בכלל - אפשר למחוק את השורה הזאת:
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

                    tvFavorites.setText(sb.length() == 0 ? "(אין מועדפים עדיין)" : sb.toString());
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
