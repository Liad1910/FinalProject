package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.view.Menu;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // טוען את ה-Layout של המסך הראשי לתוך BaseActivity
        setPageContent(R.layout.activity_main);

        // ❌ לא להציג תפריט עליון (3 נקודות) במסך הראשי
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // 🏠 בית – נשארים בדף הראשי
            if (id == R.id.bnav_home) {
                return true;
            }

            // 🎬 סרטים
            if (id == R.id.bnav_movies) {
                startActivity(new Intent(this, MoviesCategoryActivity.class));
                return true;
            }

            // 📺 סדרות
            if (id == R.id.bnav_series) {
                startActivity(new Intent(this, SeriesCategoryActivity.class));
                return true;
            }

            // ➕ עוד
            if (id == R.id.bnav_more) {
                showMoreDialog();

                // שלא יישאר מסומן
                bottomNav.getMenu().findItem(R.id.bnav_more).setChecked(false);
                return true;
            }

            return false;
        });

        // ברירת מחדל – בית
        bottomNav.setSelectedItemId(R.id.bnav_home);
    }

    // =====================================================
    // מונע יצירת תפריט עליון (overflow) במסך הראשי
    // =====================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    // =====================================================
    // Dialog "עוד" – דינמי לפי מצב התחברות
    // =====================================================
    private void showMoreDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = (user != null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("עוד");

        if (!isLoggedIn) {
            String[] options = {"התחברות", "הרשמה"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    startActivity(new Intent(this, loginPage.class));
                } else {
                    startActivity(new Intent(this, registerPage.class));
                }
            });
        } else {
            String[] options = {"פרופיל", "יצירת כותרת", "התנתקות"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    startActivity(new Intent(this, activity_user_page.class));
                } else if (which == 1) {
                    startActivity(new Intent(this, CreateTitleActivity.class));
                } else {
                    logoutFromBottomMenu();
                }
            });
        }

        builder.setNegativeButton("סגור", null);
        builder.show();
    }

    // =====================================================
    // Logout – מסונכרן עם BaseActivity
    // =====================================================
    private void logoutFromBottomMenu() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

        // עדכון Drawer
        updateMenuByAuthState();

        // חזרה למסך הראשי
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
