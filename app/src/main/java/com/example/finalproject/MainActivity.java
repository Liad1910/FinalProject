package com.example.finalproject;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends BaseActivity {

    private BottomNavigationView bottomNav;
    private TextView tvHelloMain;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db;

    private MaterialCardView cardGuest, cardStats, cardFavorites, cardWatchlist;
    private MaterialButton btnLoginGuest, btnRegisterGuest;

    private TextView tvFavCount, tvWatchlistCount, tvReviewsCount;
    private TextView tvFavoritesList, tvWatchlistList;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) scheduleRemindersIfNeeded(auth.getCurrentUser());
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageContent(R.layout.activity_main);

        // הסתרת ActionBar ו-Toolbar של BaseActivity
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvHelloMain = findViewById(R.id.tvHelloMain);
        bottomNav = findViewById(R.id.bottomNav);

        cardGuest = findViewById(R.id.cardGuest);
        cardStats = findViewById(R.id.cardStats);
        cardFavorites = findViewById(R.id.cardFavorites);
        cardWatchlist = findViewById(R.id.cardWatchlist);

        btnLoginGuest = findViewById(R.id.btnLoginGuest);
        btnRegisterGuest = findViewById(R.id.btnRegisterGuest);

        tvFavCount = findViewById(R.id.tvFavCount);
        tvWatchlistCount = findViewById(R.id.tvWatchlistCount);
        tvReviewsCount = findViewById(R.id.tvReviewsCount);
        tvFavoritesList = findViewById(R.id.tvFavoritesList);
        tvWatchlistList = findViewById(R.id.tvWatchlistList);

        btnLoginGuest.setOnClickListener(v ->
                startActivity(new Intent(this, loginPage.class)));

        btnRegisterGuest.setOnClickListener(v ->
                startActivity(new Intent(this, registerPage.class)));

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            updateHelloText(user);
            updateUI(user);
        };

        ensureAnonymousIfNeeded();
        handleNotificationPermission(auth.getCurrentUser());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bnav_home) return true;
            if (id == R.id.bnav_movies) {
                Intent i = new Intent(this, MoviesCategoryActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
                return true;
            }
            if (id == R.id.bnav_series) {
                Intent i = new Intent(this, SeriesCategoryActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
                return true;
            }
            if (id == R.id.bnav_more) {
                showMoreDialog();
                bottomNav.getMenu().findItem(R.id.bnav_more).setChecked(false);
                return true;
            }
            return false;
        });

        bottomNav.getMenu().setGroupCheckable(0, false, true);
    }

    // =====================================================
    // onResume — מתרענן בכל חזרה למסך
    // =====================================================
    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = auth.getCurrentUser();
        updateHelloText(user);
        updateUI(user);
    }

    // =====================================================
    // UI לפי מצב התחברות
    // =====================================================
    private void updateUI(FirebaseUser user) {
        boolean isLoggedIn = (user != null && !user.isAnonymous());

        if (!isLoggedIn) {
            cardGuest.setVisibility(View.VISIBLE);
            cardStats.setVisibility(View.GONE);
            cardFavorites.setVisibility(View.GONE);
            cardWatchlist.setVisibility(View.GONE);
        } else {
            cardGuest.setVisibility(View.GONE);
            cardStats.setVisibility(View.VISIBLE);
            cardFavorites.setVisibility(View.VISIBLE);
            cardWatchlist.setVisibility(View.VISIBLE);
            loadUserData(user);
        }
    }

    private void loadUserData(FirebaseUser user) {
        String uid = user.getUid();

        // מועדפים
        db.collection("users").document(uid).collection("favorites")
                .get()
                .addOnSuccessListener(snap -> {
                    tvFavCount.setText(String.valueOf(snap.size()));
                    StringBuilder sb = new StringBuilder();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String title = doc.getString("title");
                        if (!TextUtils.isEmpty(title))
                            sb.append("• ").append(title).append("\n");
                    }
                    tvFavoritesList.setText(sb.length() > 0 ? sb.toString().trim() : "(אין מועדפים עדיין)");
                });

        // רשימת צפייה
        db.collection("users").document(uid).collection("watchlist")
                .get()
                .addOnSuccessListener(snap -> {
                    tvWatchlistCount.setText(String.valueOf(snap.size()));
                    StringBuilder sb = new StringBuilder();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String title = doc.getString("title");
                        if (!TextUtils.isEmpty(title))
                            sb.append("• ").append(title).append("\n");
                    }
                    tvWatchlistList.setText(sb.length() > 0 ? sb.toString().trim() : "(אין ברשימת צפייה עדיין)");
                });

        // ביקורות
        db.collection("reviews")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snap -> tvReviewsCount.setText(String.valueOf(snap.size())))
                .addOnFailureListener(e -> tvReviewsCount.setText("0"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth != null && authStateListener != null)
            auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (auth != null && authStateListener != null)
            auth.removeAuthStateListener(authStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { return false; }

    // =====================================================
    // Notification
    // =====================================================
    private void handleNotificationPermission(FirebaseUser user) {
        if (user == null || user.isAnonymous()) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            scheduleRemindersIfNeeded(user);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            scheduleRemindersIfNeeded(user);
        } else {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void scheduleRemindersIfNeeded(FirebaseUser user) {
        if (user == null || user.isAnonymous()) return;
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("notifications_scheduled", false)) {
            WatchlistReminderScheduler.scheduleOnceOnAppOpen(this);
            WatchlistReminderScheduler.scheduleDaily(this);
            prefs.edit().putBoolean("notifications_scheduled", true).apply();
        }
    }

    // =====================================================
    // Anonymous login
    // =====================================================
    private void ensureAnonymousIfNeeded() {
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously().addOnCompleteListener(this, task -> {
                // אין הודעה למשתמש
            });
        }
    }

    // =====================================================
    // Hello text
    // =====================================================
    private void updateHelloText(FirebaseUser user) {
        if (tvHelloMain == null) return;
        if (user == null) { tvHelloMain.setText("שלום"); return; }
        if (user.isAnonymous()) { tvHelloMain.setText("שלום אנונימי"); return; }

        String name = user.getDisplayName();
        if (name != null && !name.trim().isEmpty()) { tvHelloMain.setText("שלום " + name); return; }

        String email = user.getEmail();
        if (email != null && email.contains("@")) {
            tvHelloMain.setText("שלום " + email.substring(0, email.indexOf("@")));
            return;
        }
        tvHelloMain.setText("שלום משתמש");
    }

    // =====================================================
    // More dialog
    // =====================================================
    private void showMoreDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = (user != null && !user.isAnonymous());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("עוד");

        if (!isLoggedIn) {
            String[] options = {"התחברות", "הרשמה", "הקולנוע הקרוב", "צ'אט", "צור סרט / סדרה"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: startActivity(new Intent(this, loginPage.class)); break;
                    case 1: startActivity(new Intent(this, registerPage.class)); break;
                    case 2: startActivity(new Intent(this, NearbyCinemaFreeActivity.class)); break;
                    case 3: startActivity(new Intent(this, AiActivity.class)); break;
                    case 4: startActivity(new Intent(this, CreateTitleActivity.class)); break;
                }
            });
        } else {
            String[] options = {"פרופיל", "הקולנוע הקרוב", "צ'אט", "צור סרט / סדרה", "התנתקות"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: startActivity(new Intent(this, activity_user_page.class)); break;
                    case 1: startActivity(new Intent(this, NearbyCinemaFreeActivity.class)); break;
                    case 2: startActivity(new Intent(this, AiActivity.class)); break;
                    case 3: startActivity(new Intent(this, CreateTitleActivity.class)); break;
                    case 4: logoutFromBottomMenu(); break;
                }
            });
        }
        builder.setNegativeButton("סגור", null);
        builder.show();
    }

    private void logoutFromBottomMenu() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
}