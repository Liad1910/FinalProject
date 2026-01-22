package com.example.finalproject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_category);

        tvTitleSeries = findViewById(R.id.tvTitleSeries);
        tvResultsCount = findViewById(R.id.tvResultsCountSeries);
        etSearch = findViewById(R.id.etSearchSeries);
        btnGenre = findViewById(R.id.btnGenreSeries);
        btnSort = findViewById(R.id.btnSortSeries);

        rvAllSeries = findViewById(R.id.rvAllSeries);
        rvAllSeries.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new MoviesGridAdapter(this, filteredSeries);
        rvAllSeries.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        buildLegacySeries();
        loadUserSeries();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
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

            if (!searchQuery.isEmpty() && !s.title.toLowerCase().contains(searchQuery.toLowerCase()))
                continue;

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

        new AlertDialog.Builder(this)
                .setTitle("בחרי ז'אנר")
                .setSingleChoiceItems(genres, 0, (d, i) -> selectedGenre = genres[i])
                .setPositiveButton("אישור", (d, w) -> {
                    btnGenre.setText("ז'אנר: " + selectedGenre);
                    applyFilters();
                })
                .show();
    }
}
