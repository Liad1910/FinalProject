package com.example.finalproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SeriesCategoryActivity extends AppCompatActivity {

    private TextView tvTitleSeries, tvResultsCount;
    private TextInputEditText etSearch;
    private MaterialButton btnGenre, btnSort;

    private RecyclerView rvAllSeries;
    private MoviesGridAdapter adapter;

    private final ArrayList<MovieItem> allSeries = new ArrayList<>();
    private final ArrayList<MovieItem> filteredSeries = new ArrayList<>();

    private FirebaseFirestore db;

    private String selectedGenre = "All";
    private String searchQuery = "";

    private enum SortMode { AZ, ZA }
    private SortMode sortMode = SortMode.AZ;

    // Bottom nav
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_category);

        // ❌ להסתיר תפריט עליון
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvTitleSeries = findViewById(R.id.tvTitleSeries);
        tvResultsCount = findViewById(R.id.tvResultsCountSeries);
        etSearch = findViewById(R.id.etSearchSeries);
        btnGenre = findViewById(R.id.btnGenreSeries);
        btnSort = findViewById(R.id.btnSortSeries);

        rvAllSeries = findViewById(R.id.rvAllSeries);
        rvAllSeries.setLayoutManager(new GridLayoutManager(this, 3));
        rvAllSeries.setNestedScrollingEnabled(false);

        adapter = new MoviesGridAdapter(this, filteredSeries);
        rvAllSeries.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // ===== BottomNav =====
        bottomNav = findViewById(R.id.bottomNav);
        setupBottomNav();
        bottomNav.setSelectedItemId(R.id.bnav_series);

        buildLegacySeries();
        loadUserSeries();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = (s != null) ? s.toString().trim() : "";
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnGenre.setOnClickListener(v -> showGenreDialog());
        btnSort.setOnClickListener(v -> {
            sortMode = (sortMode == SortMode.AZ) ? SortMode.ZA : SortMode.AZ;
            btnSort.setText(sortMode == SortMode.AZ ? "מיון A→Z" : "מיון Z→A");
            applyFilters();
        });

        btnSort.setText("מיון A→Z");
        tvTitleSeries.setText("Series");
    }

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
                // כבר פה
                return true;
            }

            if (id == R.id.bnav_more) {
                showMoreDialog();
                bottomNav.getMenu().findItem(R.id.bnav_more).setChecked(false);
                return true;
            }

            return false;
        });
    }

    // =====================================================
    // "עוד"
    // =====================================================
    private void showMoreDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = (user != null && !user.isAnonymous());

        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
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
                        startActivity(new Intent(this, activity_user_page.class));
                        break;
                    case 1:
                        startActivity(new Intent(this, NearbyCinemaFreeActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(this,AiActivity.class));
                        break;
                    case 3:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                        break;
                }
            });
        }

        builder.setNegativeButton("סגור", null);
        builder.show();
    }

    private void buildLegacySeries() {
        addSeries("got", "Game of Thrones", Arrays.asList("Fantasy","Drama"), R.drawable.got_poster);
        addSeries("breaking_bad", "Breaking Bad", Arrays.asList("Crime","Drama"), R.drawable.breaking_bad_poster);
        addSeries("friends", "Friends", Arrays.asList("Comedy"), R.drawable.friends_poster);
        addSeries("stranger_things", "Stranger Things", Arrays.asList("Sci-Fi","Horror"), R.drawable.stranger_things_poster);
        addSeries("chernobyl", "Chernobyl", Arrays.asList("Drama"), R.drawable.chernobyl_poster);
        applyFilters();
    }

    private void addSeries(String id, String title, List<String> genres, int posterRes) {
        MovieItem m = new MovieItem();
        m.id = id;
        m.title = title;
        m.genres = genres;
        m.posterResId = posterRes;
        m.isUserTitle = false;
        allSeries.add(m);
    }

    private void loadUserSeries() {
        db.collection("titles")
                .whereEqualTo("type", "series")
                .orderBy("title", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;

                    allSeries.removeIf(s -> s.isUserTitle);

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        MovieItem m = new MovieItem();
                        m.id = d.getId();
                        m.title = d.getString("title");
                        m.posterUrl = d.getString("posterUrl");
                        m.genres = (List<String>) d.get("genres");
                        m.isUserTitle = true;
                        allSeries.add(m);
                    }
                    applyFilters();
                });
    }

    private void applyFilters() {
        filteredSeries.clear();

        for (MovieItem s : allSeries) {
            if (!"All".equals(selectedGenre) && (s.genres == null || !s.genres.contains(selectedGenre)))
                continue;

            if (searchQuery != null && !searchQuery.isEmpty()) {
                String t = (s.title != null) ? s.title.toLowerCase() : "";
                if (!t.contains(searchQuery.toLowerCase()))
                    continue;
            }

            filteredSeries.add(s);
        }

        Comparator<MovieItem> cmp = (a, b) -> a.title.compareToIgnoreCase(b.title);
        Collections.sort(filteredSeries, cmp);
        if (sortMode == SortMode.ZA) Collections.reverse(filteredSeries);

        adapter.notifyDataSetChanged();
        tvResultsCount.setText("נמצאו " + filteredSeries.size() + " סדרות");
    }

    private void showGenreDialog() {
        final String[] genres = {"All","Drama","Comedy","Crime","Sci-Fi","Fantasy","Horror"};

        int preselect = 0;
        for (int i = 0; i < genres.length; i++) {
            if (genres[i].equals(selectedGenre)) { preselect = i; break; }
        }

        new AlertDialog.Builder(this)
                .setTitle("בחרי ז'אנר")
                .setSingleChoiceItems(genres, preselect, (d, i) -> selectedGenre = genres[i])
                .setPositiveButton("אישור", (d, w) -> {
                    btnGenre.setText("ז'אנר: " + ("All".equals(selectedGenre) ? "הכל" : selectedGenre));
                    applyFilters();
                })
                .setNegativeButton("ביטול", null)
                .show();
    }
}
