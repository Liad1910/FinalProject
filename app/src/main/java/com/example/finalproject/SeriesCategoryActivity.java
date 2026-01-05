package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeriesCategoryActivity extends AppCompatActivity {

    private TextView tvTitleSeries;
    private String selectedGenre;

    private RecyclerView rvUserTitles;
    private TitlesAdapter adapter;
    private final ArrayList<TitleCard> userTitles = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_category);

        tvTitleSeries = findViewById(R.id.tvTitleSeries);

        selectedGenre = getIntent().getStringExtra("genre");
        if (selectedGenre == null) selectedGenre = "All";

        tvTitleSeries.setText("Series – " + selectedGenre);

        setupLegacySeriesButtonsAndFilter();

        rvUserTitles = findViewById(R.id.rvUserTitles);
        if (rvUserTitles != null) {
            rvUserTitles.setLayoutManager(new GridLayoutManager(this, 3));
            rvUserTitles.setNestedScrollingEnabled(false);

            adapter = new TitlesAdapter(this, userTitles);
            rvUserTitles.setAdapter(adapter);

            db = FirebaseFirestore.getInstance();
            loadUserSeries();
        }
    }

    // =====================================================
    // פתיחת סדרה ישנה (כמו סרט)
    // =====================================================
    private void openLegacySeries(String id, String title, String trailerUrl, int posterResId) {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, id);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, title);
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL, trailerUrl);
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, posterResId);
        startActivity(i);
    }

    private void safeClick(int id, Runnable r) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(x -> r.run());
    }

    private void setupLegacySeriesButtonsAndFilter() {

        Map<Integer, List<String>> seriesGenres = new HashMap<>();

        seriesGenres.put(R.id.btnGot,             Arrays.asList("Fantasy", "Drama"));
        seriesGenres.put(R.id.btnBreakingBad,     Arrays.asList("Crime", "Drama"));
        seriesGenres.put(R.id.btnStrangerThings,  Arrays.asList("Sci-Fi", "Horror"));
        seriesGenres.put(R.id.btnFriends,         Arrays.asList("Comedy"));
        seriesGenres.put(R.id.btnSopranos,        Arrays.asList("Crime", "Drama"));
        seriesGenres.put(R.id.btnTheCrown,        Arrays.asList("Drama"));
        seriesGenres.put(R.id.btnHTGAWM,          Arrays.asList("Crime", "Drama"));
        seriesGenres.put(R.id.btnGreys,           Arrays.asList("Drama"));
        seriesGenres.put(R.id.btnTwoAndHalfMen,   Arrays.asList("Comedy"));
        seriesGenres.put(R.id.btnTheOffice,       Arrays.asList("Comedy"));
        seriesGenres.put(R.id.btnWalkingDead,     Arrays.asList("Horror"));
        seriesGenres.put(R.id.btnHouseMD,         Arrays.asList("Drama"));
        seriesGenres.put(R.id.btnFargo,           Arrays.asList("Crime", "Drama"));
        seriesGenres.put(R.id.btnOzark,           Arrays.asList("Crime", "Drama"));
        seriesGenres.put(R.id.btnVikings,         Arrays.asList("Drama", "Adventure"));
        seriesGenres.put(R.id.btnHouseOfCards,    Arrays.asList("Drama"));
        seriesGenres.put(R.id.btnChernobyl,       Arrays.asList("Drama"));
        seriesGenres.put(R.id.btnBlackMirror,     Arrays.asList("Sci-Fi", "Drama"));
        seriesGenres.put(R.id.btnQueensGambit,    Arrays.asList("Drama"));
        seriesGenres.put(R.id.btnBigMouth,        Arrays.asList("Comedy", "Animation"));

        // ✅ קליקים
        safeClick(R.id.btnGot, () ->
                openLegacySeries("series_game_of_thrones", "Game of Thrones (2011)",
                        "https://www.youtube.com/results?search_query=Game+of+Thrones+trailer",
                        R.drawable.got_poster));

        safeClick(R.id.btnBreakingBad, () ->
                openLegacySeries("series_breaking_bad", "Breaking Bad (2008)",
                        "https://www.youtube.com/results?search_query=Breaking+Bad+trailer",
                        R.drawable.breaking_bad_poster));

        safeClick(R.id.btnStrangerThings, () ->
                openLegacySeries("series_stranger_things", "Stranger Things (2016)",
                        "https://www.youtube.com/results?search_query=Stranger+Things+trailer",
                        R.drawable.stranger_things_poster));

        safeClick(R.id.btnFriends, () ->
                openLegacySeries("series_friends", "Friends (1994)",
                        "https://www.youtube.com/results?search_query=Friends+trailer",
                        R.drawable.friends_poster));

        safeClick(R.id.btnSopranos, () ->
                openLegacySeries("series_sopranos", "The Sopranos (1999)",
                        "https://www.youtube.com/results?search_query=The+Sopranos+trailer",
                        R.drawable.sopranos_poster));

        safeClick(R.id.btnTheCrown, () ->
                openLegacySeries("series_the_crown", "The Crown (2016)",
                        "https://www.youtube.com/results?search_query=The+Crown+trailer",
                        R.drawable.the_crown_poster));

        safeClick(R.id.btnHTGAWM, () ->
                openLegacySeries("series_htgawm", "How to Get Away with Murder (2014)",
                        "https://www.youtube.com/results?search_query=How+to+Get+Away+with+Murder+trailer",
                        R.drawable.htgawm_poster));

        safeClick(R.id.btnGreys, () ->
                openLegacySeries("series_greys_anatomy", "Grey's Anatomy (2005)",
                        "https://www.youtube.com/results?search_query=Grey%27s+Anatomy+trailer",
                        R.drawable.greys_poster));

        safeClick(R.id.btnTwoAndHalfMen, () ->
                openLegacySeries("series_two_and_a_half_men", "Two and a Half Men (2003)",
                        "https://www.youtube.com/results?search_query=Two+and+a+Half+Men+trailer",
                        R.drawable.two_and_half_men_poster));

        safeClick(R.id.btnTheOffice, () ->
                openLegacySeries("series_the_office_us", "The Office (US) (2005)",
                        "https://www.youtube.com/results?search_query=The+Office+US+trailer",
                        R.drawable.the_office_poster));

        safeClick(R.id.btnWalkingDead, () ->
                openLegacySeries("series_the_walking_dead", "The Walking Dead (2010)",
                        "https://www.youtube.com/results?search_query=The+Walking+Dead+trailer",
                        R.drawable.walking_dead_poster));

        safeClick(R.id.btnHouseMD, () ->
                openLegacySeries("series_house_md", "House M.D. (2004)",
                        "https://www.youtube.com/results?search_query=House+MD+trailer",
                        R.drawable.house_md_poster));

        safeClick(R.id.btnFargo, () ->
                openLegacySeries("series_fargo", "Fargo (2014)",
                        "https://www.youtube.com/results?search_query=Fargo+series+trailer",
                        R.drawable.fargo_poster));

        safeClick(R.id.btnOzark, () ->
                openLegacySeries("series_ozark", "Ozark (2017)",
                        "https://www.youtube.com/results?search_query=Ozark+trailer",
                        R.drawable.ozark_poster));

        safeClick(R.id.btnVikings, () ->
                openLegacySeries("series_vikings", "Vikings (2013)",
                        "https://www.youtube.com/results?search_query=Vikings+trailer",
                        R.drawable.vikings_poster));

        safeClick(R.id.btnHouseOfCards, () ->
                openLegacySeries("series_house_of_cards", "House of Cards (2013)",
                        "https://www.youtube.com/results?search_query=House+of+Cards+trailer",
                        R.drawable.house_of_cards_poster));

        safeClick(R.id.btnChernobyl, () ->
                openLegacySeries("series_chernobyl", "Chernobyl (2019)",
                        "https://www.youtube.com/results?search_query=Chernobyl+miniseries+trailer",
                        R.drawable.chernobyl_poster));

        safeClick(R.id.btnBlackMirror, () ->
                openLegacySeries("series_black_mirror", "Black Mirror (2011)",
                        "https://www.youtube.com/results?search_query=Black+Mirror+trailer",
                        R.drawable.black_mirror_poster));

        safeClick(R.id.btnQueensGambit, () ->
                openLegacySeries("series_queens_gambit", "The Queen's Gambit (2020)",
                        "https://www.youtube.com/results?search_query=The+Queen%27s+Gambit+trailer",
                        R.drawable.queens_gambit_poster));

        safeClick(R.id.btnBigMouth, () ->
                openLegacySeries("series_big_mouth", "Big Mouth (2017)",
                        "https://www.youtube.com/results?search_query=Big+Mouth+trailer",
                        R.drawable.big_mouth_poster));

        // ✅ סינון לפי ז׳אנר
        if (!"All".equals(selectedGenre)) {
            for (Map.Entry<Integer, List<String>> entry : seriesGenres.entrySet()) {
                View btn = findViewById(entry.getKey());
                if (btn != null) {
                    boolean show = entry.getValue().contains(selectedGenre);
                    btn.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    private void loadUserSeries() {

        Query q = db.collection("titles")
                .whereEqualTo("type", "series");

        if (!"All".equals(selectedGenre)) {
            q = q.whereArrayContains("genres", selectedGenre);
        }

        q = q.orderBy("title", Query.Direction.ASCENDING);

        q.addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;

            userTitles.clear();

            for (DocumentSnapshot d : snap.getDocuments()) {
                TitleCard t = new TitleCard();
                t.id = d.getId();
                t.title = d.getString("title");
                t.type = d.getString("type");
                t.posterResName = d.getString("posterResName");

// ✅ חדש
                t.posterUrl = d.getString("posterUrl");

                userTitles.add(t);
            }

            adapter.notifyDataSetChanged();
        });
    }
}
