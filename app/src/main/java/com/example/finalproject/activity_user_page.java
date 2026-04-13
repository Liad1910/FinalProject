package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class activity_user_page extends BaseActivity {

    private TextView tvEmail, tvFavorites, tvWatchlist;
    private BottomNavigationView bottomNav;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageContent(R.layout.activity_user_page);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvEmail = findViewById(R.id.tvEmail);
        tvFavorites = findViewById(R.id.tvFavorites);
        tvWatchlist = findViewById(R.id.tvWatchlist);
        bottomNav = findViewById(R.id.bottomNav);

        // ===== BottomNav =====
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bnav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            if (id == R.id.bnav_movies) {
                startActivity(new Intent(this, MoviesCategoryActivity.class));
                finish();
                return true;
            }
            if (id == R.id.bnav_series) {
                startActivity(new Intent(this, SeriesCategoryActivity.class));
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

        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        tvEmail.setText(currentUser.getEmail());

        userDocRef = db.collection("users").document(currentUser.getUid());

        loadFavorites();
        loadWatchlist();
    }

    private void loadFavorites() {
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
                            sb.append("• ").append(title).append("\n");
                        }
                    }
                    tvFavorites.setText(sb.toString());
                });
    }

    private void loadWatchlist() {
        userDocRef.collection("watchlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        tvWatchlist.setText("(אין ברשימת צפייה עדיין)");
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String title = doc.getString("title");
                        if (!TextUtils.isEmpty(title)) {
                            sb.append("• ").append(title).append("\n");
                        }
                    }
                    tvWatchlist.setText(sb.toString());
                });

        tvWatchlist.setOnClickListener(v -> showRemoveDialog());
    }

    private void showRemoveDialog() {
        userDocRef.collection("watchlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) return;

                    String[] titles = new String[querySnapshot.size()];
                    String[] ids = new String[querySnapshot.size()];

                    for (int i = 0; i < querySnapshot.size(); i++) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(i);
                        titles[i] = doc.getString("title");
                        ids[i] = doc.getId();
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("הסרת סרט מרשימת צפייה")
                            .setItems(titles, (dialog, which) -> {
                                userDocRef.collection("watchlist")
                                        .document(ids[which])
                                        .delete()
                                        .addOnSuccessListener(a -> {
                                            Toast.makeText(this, "הוסר ✅", Toast.LENGTH_SHORT).show();
                                            loadWatchlist();
                                        });
                            })
                            .setNegativeButton("ביטול", null)
                            .show();
                });
    }

    private void showMoreDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = (user != null && !user.isAnonymous());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("עוד");

        if (!isLoggedIn) {
            String[] options = {"התחברות", "הרשמה", "הקולנוע הקרוב", "צ'אט"};

            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: startActivity(new Intent(this, loginPage.class)); break;
                    case 1: startActivity(new Intent(this, registerPage.class)); break;
                    case 2: startActivity(new Intent(this, NearbyCinemaFreeActivity.class)); break;
                    case 3: startActivity(new Intent(this, AiActivity.class)); break;
                }
            });

        } else {
            String[] options = {"פרופיל", "הקולנוע הקרוב", "צ'אט", "התנתקות"};

            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: startActivity(new Intent(this, activity_user_page.class)); break;
                    case 1: startActivity(new Intent(this, NearbyCinemaFreeActivity.class)); break;
                    case 2: startActivity(new Intent(this, AiActivity.class)); break;
                    case 3:
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                        break;
                }
            });
        }

        builder.setNegativeButton("סגור", null);
        builder.show();
    }
}