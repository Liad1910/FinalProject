package com.example.finalproject;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity {

    private BottomNavigationView bottomNav;
    private TextView tvHelloMain;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            scheduleRemindersIfNeeded(auth.getCurrentUser());
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageContent(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        auth = FirebaseAuth.getInstance();

        tvHelloMain = findViewById(R.id.tvHelloMain);
        bottomNav = findViewById(R.id.bottomNav);

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            updateHelloText(user);
        };

        ensureAnonymousIfNeeded();
        handleNotificationPermission(auth.getCurrentUser());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bnav_home) {
                // כבר בבית, לא עושים כלום
                return true;
            }
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
        updateHelloText(auth.getCurrentUser());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth != null && authStateListener != null) {
            auth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (auth != null && authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    // =====================================================
    // Notification permission handling
    // =====================================================

    private void handleNotificationPermission(FirebaseUser user) {
        if (user == null || user.isAnonymous()) return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            scheduleRemindersIfNeeded(user);
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            scheduleRemindersIfNeeded(user);
        } else {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void scheduleRemindersIfNeeded(FirebaseUser user) {
        if (user == null || user.isAnonymous()) return;

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean alreadyScheduled = prefs.getBoolean("notifications_scheduled", false);

        if (!alreadyScheduled) {
            WatchlistReminderScheduler.scheduleOnceOnAppOpen(this);
            WatchlistReminderScheduler.scheduleDaily(this);
            prefs.edit().putBoolean("notifications_scheduled", true).apply();
        }
    }

    // =====================================================
    // Anonymous login
    // =====================================================

    private void ensureAnonymousIfNeeded() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            auth.signInAnonymously().addOnCompleteListener(this, task -> {
                if (!task.isSuccessful()) {
                    String msg = (task.getException() != null)
                            ? task.getException().getMessage()
                            : "unknown error";
                    Toast.makeText(this,
                            "כניסה אנונימית נכשלה: " + msg,
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // =====================================================
    // Hello text
    // =====================================================

    private void updateHelloText(FirebaseUser user) {
        if (tvHelloMain == null) return;

        if (user == null) {
            tvHelloMain.setText("שלום");
            return;
        }

        if (user.isAnonymous()) {
            tvHelloMain.setText("שלום אנונימי");
            return;
        }

        String name = user.getDisplayName();
        if (name != null && !name.trim().isEmpty()) {
            tvHelloMain.setText("שלום " + name);
            return;
        }

        String email = user.getEmail();
        if (email != null && email.contains("@")) {
            String username = email.substring(0, email.indexOf("@"));
            tvHelloMain.setText("שלום " + username);
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
            String[] options = {
                    "התחברות",
                    "הרשמה",
                    "הקולנוע הקרוב",
                    "צ'אט",
                    "צור סרט / סדרה"
            };

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
            String[] options = {
                    "פרופיל",
                    "הקולנוע הקרוב",
                    "צ'אט",
                    "צור סרט / סדרה",
                    "התנתקות"
            };

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