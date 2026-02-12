package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity {

    private BottomNavigationView bottomNav;
    private TextView tvHelloMain;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

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

        // מאזין לשינויים בהתחברות
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            updateHelloText(user);
        };

        ensureAnonymousIfNeeded();

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
    // מבטיח משתמש אנונימי אם אין משתמש
    // =====================================================
    private void ensureAnonymousIfNeeded() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            auth.signInAnonymously().addOnCompleteListener(this, task -> {
                if (!task.isSuccessful()) {
                    String msg = (task.getException() != null)
                            ? task.getException().getMessage()
                            : "unknown error";
                    Toast.makeText(this, "כניסה אנונימית נכשלה: " + msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // =====================================================
    // עדכון טקסט שלום - בלי אימייל
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

        // קודם כל מנסים displayName
        String name = user.getDisplayName();
        if (name != null && !name.trim().isEmpty()) {
            tvHelloMain.setText("שלום " + name);
            return;
        }

        // אם אין displayName – ניקח את השם מהמייל בלי להציג את המייל
        String email = user.getEmail();
        if (email != null && email.contains("@")) {
            String username = email.substring(0, email.indexOf("@"));
            tvHelloMain.setText("שלום " + username);
            return;
        }

        tvHelloMain.setText("שלום משתמש");
    }

    // =====================================================
    // Dialog עוד
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
                    "צ'אט"
            };

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

            String[] options = {
                    "פרופיל",
                    "הקולנוע הקרוב",
                    "צ'אט",
                    "התנתקות"
            };

            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        startActivity(new Intent(this, activity_user_page.class));
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

    // =====================================================
    // Logout
    // =====================================================
    private void logoutFromBottomMenu() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

        updateMenuByAuthState();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
