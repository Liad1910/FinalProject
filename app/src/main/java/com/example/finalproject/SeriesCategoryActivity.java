package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeriesCategoryActivity extends AppCompatActivity {

    private TextView tvTitleSeries;
    private String selectedGenre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_series_category);

        tvTitleSeries = findViewById(R.id.tvTitleSeries);

        // קבלת הז'אנר מה-Intent (Action / Comedy / Drama / All ...)
        selectedGenre = getIntent().getStringExtra("genre");
        if (selectedGenre == null) {
            selectedGenre = "All";
        }

        tvTitleSeries.setText("Series – " + selectedGenre);

        setupSeriesButtonsAndFilter();
    }

    private void setupSeriesButtonsAndFilter() {

        // 1) מיפוי סדרות לז'אנרים (עבור סינון)
        Map<Integer, List<String>> seriesGenres = new HashMap<>();

        seriesGenres.put(R.id.btnGot,             Arrays.asList("Fantasy", "Drama"));
        seriesGenres.put(R.id.btnBreakingBad,     Arrays.asList("Crime", "Drama"));
        seriesGenres.put(R.id.btnStrangerThings,  Arrays.asList("Sci-Fi", "Horror"));
        seriesGenres.put(R.id.btnFriends,         Arrays.asList("Comedy"));
        seriesGenres.put(R.id.btnSopranos,        Arrays.asList("Crime", "Drama"));
        seriesGenres.put(R.id.btnTheCrown,        Arrays.asList("Drama"));
        seriesGenres.put(R.id.btnHTGAWM,          Arrays.asList("Crime", "Drama")); // How to Get Away with Murder
        seriesGenres.put(R.id.btnGreys,           Arrays.asList("Drama"));          // Grey's Anatomy
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

        // 2) קליקים לכל כפתור סדרה (פותחים את MovieContentActivity)

        setSeriesClick(R.id.btnGot,            this::openGameOfThrones);
        setSeriesClick(R.id.btnBreakingBad,    this::openBreakingBad);
        setSeriesClick(R.id.btnStrangerThings, this::openStrangerThings);
        setSeriesClick(R.id.btnFriends,        this::openFriends);
        setSeriesClick(R.id.btnSopranos,       this::openSopranos);
        setSeriesClick(R.id.btnTheCrown,       this::openTheCrown);
        setSeriesClick(R.id.btnHTGAWM,         this::openHTGAWM);
        setSeriesClick(R.id.btnGreys,          this::openGreys);
        setSeriesClick(R.id.btnTwoAndHalfMen,  this::openTwoAndHalfMen);
        setSeriesClick(R.id.btnTheOffice,      this::openTheOffice);
        setSeriesClick(R.id.btnWalkingDead,    this::openWalkingDead);
        setSeriesClick(R.id.btnHouseMD,        this::openHouseMD);
        setSeriesClick(R.id.btnFargo,          this::openFargo);
        setSeriesClick(R.id.btnOzark,          this::openOzark);
        setSeriesClick(R.id.btnVikings,        this::openVikings);
        setSeriesClick(R.id.btnHouseOfCards,   this::openHouseOfCards);
        setSeriesClick(R.id.btnChernobyl,      this::openChernobyl);
        setSeriesClick(R.id.btnBlackMirror,    this::openBlackMirror);
        setSeriesClick(R.id.btnQueensGambit,   this::openQueensGambit);
        setSeriesClick(R.id.btnBigMouth,       this::openBigMouth);

        // 3) סינון לפי ז'אנר שנבחר (אם לא All)
        if (!"All".equals(selectedGenre)) {
            for (Map.Entry<Integer, List<String>> entry : seriesGenres.entrySet()) {
                View btn = findViewById(entry.getKey());
                if (btn != null) {
                    boolean shouldShow = entry.getValue().contains(selectedGenre);
                    btn.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    private void setSeriesClick(int viewId, Runnable action) {
        View v = findViewById(viewId);
        if (v != null) {
            v.setOnClickListener(view -> action.run());
        }
    }

    // ---------- פונקציות פתיחה לכל סדרה ----------
    // משתמשים בפוסטרים מתוך res/drawable כפי שסיכמנו

    private void openGameOfThrones() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_game_of_thrones");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Game of Thrones (2011)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Game+of+Thrones+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.got_poster);
        startActivity(i);
    }

    private void openBreakingBad() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_breaking_bad");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Breaking Bad (2008)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Breaking+Bad+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.breaking_bad_poster);
        startActivity(i);
    }

    private void openStrangerThings() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_stranger_things");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Stranger Things (2016)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Stranger+Things+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.stranger_things_poster);
        startActivity(i);
    }

    private void openFriends() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_friends");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Friends (1994)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Friends+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.friends_poster);
        startActivity(i);
    }

    private void openSopranos() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_sopranos");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Sopranos (1999)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Sopranos+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.sopranos_poster);
        startActivity(i);
    }

    private void openTheCrown() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_the_crown");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Crown (2016)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Crown+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.the_crown_poster);
        startActivity(i);
    }

    private void openHTGAWM() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_htgawm");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "How to Get Away with Murder (2014)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=How+to+Get+Away+with+Murder+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.htgawm_poster);
        startActivity(i);
    }

    private void openGreys() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_greys_anatomy");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Grey's Anatomy (2005)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Grey%27s+Anatomy+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.greys_poster);
        startActivity(i);
    }

    private void openTwoAndHalfMen() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_two_and_a_half_men");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Two and a Half Men (2003)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Two+and+a+Half+Men+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.two_and_half_men_poster);
        startActivity(i);
    }

    private void openTheOffice() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_the_office_us");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Office (US) (2005)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Office+US+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.the_office_poster);
        startActivity(i);
    }

    private void openWalkingDead() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_the_walking_dead");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Walking Dead (2010)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Walking+Dead+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.walking_dead_poster);
        startActivity(i);
    }

    private void openHouseMD() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_house_md");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "House M.D. (2004)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=House+MD+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.house_md_poster);
        startActivity(i);
    }

    private void openFargo() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_fargo");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Fargo (2014)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Fargo+series+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.fargo_poster);
        startActivity(i);
    }

    private void openOzark() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_ozark");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Ozark (2017)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Ozark+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.ozark_poster);
        startActivity(i);
    }

    private void openVikings() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_vikings");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Vikings (2013)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Vikings+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.vikings_poster);
        startActivity(i);
    }

    private void openHouseOfCards() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_house_of_cards");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "House of Cards (2013)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=House+of+Cards+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.house_of_cards_poster);
        startActivity(i);
    }

    private void openChernobyl() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_chernobyl");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Chernobyl (2019)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Chernobyl+miniseries+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.chernobyl_poster);
        startActivity(i);
    }

    private void openBlackMirror() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_black_mirror");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Black Mirror (2011)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Black+Mirror+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.black_mirror_poster);
        startActivity(i);
    }

    private void openQueensGambit() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_queens_gambit");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Queen's Gambit (2020)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Queen%27s+Gambit+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.queens_gambit_poster);
        startActivity(i);
    }

    private void openBigMouth() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "series_big_mouth");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Big Mouth (2017)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Big+Mouth+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.big_mouth_poster);
        startActivity(i);
    }
}
