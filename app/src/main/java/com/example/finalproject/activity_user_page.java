package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
}
