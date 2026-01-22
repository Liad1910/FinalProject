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

public class MoviesCategoryActivity extends AppCompatActivity {

    private TextView tvTitleMovies, tvResultsCount;
    private TextInputEditText etSearch;
    private MaterialButton btnClearSearch, btnGenre, btnSort;

    private RecyclerView rvAllMovies;
    private MoviesGridAdapter adapter;

    private final ArrayList<MovieItem> allMovies = new ArrayList<>();
    private final ArrayList<MovieItem> filteredMovies = new ArrayList<>();

    private FirebaseFirestore db;

    private String selectedGenre = "All";
    private String searchQuery = "";

    private enum SortMode { TITLE_AZ, TITLE_ZA }
    private SortMode sortMode = SortMode.TITLE_AZ;

    // AI
    private boolean aiMode = false;
    private String aiGenre = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_category);

        tvTitleMovies = findViewById(R.id.tvTitleMovies);
        tvResultsCount = findViewById(R.id.tvResultsCount);

        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnGenre = findViewById(R.id.btnGenre);
        btnSort = findViewById(R.id.btnSort);

        rvAllMovies = findViewById(R.id.rvAllMovies);
        rvAllMovies.setLayoutManager(new GridLayoutManager(this, 3));
        rvAllMovies.setNestedScrollingEnabled(false);

        adapter = new MoviesGridAdapter(this, filteredMovies);
        rvAllMovies.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // ===== AI Extras =====
        aiMode = getIntent().getBooleanExtra("AI_MODE", false);
        aiGenre = getIntent().getStringExtra("AI_GENRE");

        // ===== Genre selection (from intent) =====
        String g = getIntent().getStringExtra("genre");
        if (g != null && !g.trim().isEmpty()) selectedGenre = g;

        if (aiMode && aiGenre != null && !aiGenre.trim().isEmpty()) {
            selectedGenre = mapAiGenreToLegacyGenre(aiGenre);
        }

        tvTitleMovies.setText(aiMode ? "AI Picks – " + selectedGenre : "Movies – " + selectedGenre);
        btnGenre.setText("ז'אנר: " + ("All".equals(selectedGenre) ? "הכל" : selectedGenre));

        // Load legacy + Firestore
        buildLegacyMovies();
        loadUserMoviesFromFirestore();

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = (s != null) ? s.toString().trim() : "";
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            searchQuery = "";
            applyFilters();
        });

        // Genre dialog
        btnGenre.setOnClickListener(v -> showGenreDialog());

        // Sort toggle
        btnSort.setOnClickListener(v -> {
            sortMode = (sortMode == SortMode.TITLE_AZ) ? SortMode.TITLE_ZA : SortMode.TITLE_AZ;
            btnSort.setText(sortMode == SortMode.TITLE_AZ ? "מיון: A→Z" : "מיון: Z→A");
            applyFilters();
        });

        btnSort.setText("מיון: A→Z");
    }

    // =====================================================
    // Legacy movies as data (instead of 200 ImageButtons)
    // =====================================================
    private void buildLegacyMovies() {
        allMovies.clear();

        addLegacy("titanic_1997", "Titanic (1997)",
                Arrays.asList("Romance", "Drama"),
                "https://www.youtube.com/results?search_query=Titanic+trailer",
                R.drawable.titanic_poster);

        addLegacy("forrest_gump_1994", "Forrest Gump (1994)",
                Arrays.asList("Drama", "Comedy"),
                "https://www.youtube.com/results?search_query=Forrest+Gump+trailer",
                R.drawable.forrest_gump_poster);

        addLegacy("matrix_1999", "The Matrix (1999)",
                Arrays.asList("Action", "Sci-Fi"),
                "https://www.youtube.com/results?search_query=The+Matrix+trailer",
                R.drawable.matrix_poster);

        addLegacy("oppenheimer_2023", "Oppenheimer (2023)",
                Arrays.asList("Drama"),
                "https://www.youtube.com/results?search_query=Oppenheimer+trailer",
                R.drawable.oppenheimer_poster);

        addLegacy("white_chicks_2004", "White Chicks (2004)",
                Arrays.asList("Comedy"),
                "https://www.youtube.com/results?search_query=White+Chicks+trailer",
                R.drawable.white_chicks_poster);

        addLegacy("up_2009", "Up (2009)",
                Arrays.asList("Comedy"),
                "https://www.youtube.com/results?search_query=Up+trailer",
                R.drawable.up_poster);

        addLegacy("to_all_the_boys_2018", "To All the Boys I've Loved Before (2018)",
                Arrays.asList("Romance"),
                "https://www.youtube.com/results?search_query=To+All+the+Boys+trailer",
                R.drawable.to_all_the_boys_poster);

        addLegacy("the_pianist_2002", "The Pianist (2002)",
                Arrays.asList("Drama"),
                "https://www.youtube.com/results?search_query=The+Pianist+trailer",
                R.drawable.the_pianist_poster);

        addLegacy("it_2017", "IT (2017)",
                Arrays.asList("Horror"),
                "https://www.youtube.com/watch?v=FnCdOQsX5kc",
                R.drawable.it_poster);

        // הוסיפי כאן עוד מהאוסף שלך בצורה דומה (זה הרבה יותר נקי מ-ImageButtons)
        applyFilters();
    }

    private void addLegacy(String id, String title, List<String> genres, String trailerUrl, int posterResId) {
        MovieItem m = new MovieItem();
        m.id = id;
        m.title = title;
        m.genres = genres;
        m.trailerUrl = trailerUrl;
        m.posterResId = posterResId;
        m.isUserTitle = false;
        allMovies.add(m);
    }

    // =====================================================
    // Firestore user movies
    // =====================================================
    private void loadUserMoviesFromFirestore() {

        Query q = db.collection("titles")
                .whereEqualTo("type", "movie")
                .orderBy("title", Query.Direction.ASCENDING);

        q.addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;

            // remove previous user items
            allMovies.removeIf(m -> m.isUserTitle);

            for (DocumentSnapshot d : snap.getDocuments()) {
                MovieItem m = new MovieItem();
                m.id = d.getId();
                m.title = d.getString("title");
                m.posterUrl = d.getString("posterUrl");
                m.trailerUrl = d.getString("trailerUrl");
                m.isUserTitle = true;

                // genres array
                List<String> g = (List<String>) d.get("genres");
                m.genres = (g != null) ? g : Arrays.asList("All");

                allMovies.add(m);
            }

            applyFilters();
        });
    }

    // =====================================================
    // Filtering + Sorting
    // =====================================================
    private void applyFilters() {
        filteredMovies.clear();

        for (MovieItem m : allMovies) {
            // genre filter
            if (!"All".equals(selectedGenre)) {
                if (m.genres == null || !m.genres.contains(selectedGenre)) continue;
            }

            // search filter
            if (searchQuery != null && !searchQuery.isEmpty()) {
                String t = (m.title != null) ? m.title.toLowerCase() : "";
                if (!t.contains(searchQuery.toLowerCase())) continue;
            }

            filteredMovies.add(m);
        }

        // sort
        Comparator<MovieItem> cmp = (a, b) -> {
            String ta = (a.title != null) ? a.title : "";
            String tb = (b.title != null) ? b.title : "";
            return ta.compareToIgnoreCase(tb);
        };

        Collections.sort(filteredMovies, cmp);
        if (sortMode == SortMode.TITLE_ZA) Collections.reverse(filteredMovies);

        adapter.notifyDataSetChanged();

        tvResultsCount.setText("נמצאו " + filteredMovies.size() + " סרטים");
    }

    // =====================================================
    // Genre dialog
    // =====================================================
    private void showGenreDialog() {
        final String[] genres = new String[]{
                "All", "Action", "Comedy", "Drama", "Romance", "Horror", "Sci-Fi"
        };

        int preselect = 0;
        for (int i = 0; i < genres.length; i++) {
            if (genres[i].equals(selectedGenre)) { preselect = i; break; }
        }

        new AlertDialog.Builder(this)
                .setTitle("בחרי ז'אנר")
                .setSingleChoiceItems(genres, preselect, (d, which) -> {
                    selectedGenre = genres[which];
                })
                .setPositiveButton("אישור", (d, w) -> {
                    btnGenre.setText("ז'אנר: " + ("All".equals(selectedGenre) ? "הכל" : selectedGenre));
                    tvTitleMovies.setText(aiMode ? "AI Picks – " + selectedGenre : "Movies – " + selectedGenre);
                    applyFilters();
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    // =====================================================
    // AI genre mapping
    // =====================================================
    private String mapAiGenreToLegacyGenre(String ai) {
        String g = ai.trim().toLowerCase();
        switch (g) {
            case "action": return "Action";
            case "comedy": return "Comedy";
            case "drama": return "Drama";
            case "romance": return "Romance";
            case "horror": return "Horror";
            case "sci_fi":
            case "sci-fi":
            case "scifi":
                return "Sci-Fi";
            default: return "All";
        }
    }
}
