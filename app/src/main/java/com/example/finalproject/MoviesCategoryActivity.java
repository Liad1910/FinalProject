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

public class MoviesCategoryActivity extends AppCompatActivity {

    private TextView tvTitleMovies;
    private String selectedGenre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movies_category);

        tvTitleMovies = findViewById(R.id.tvTitleMovies);

        // קבלת הז'אנר מה־Intent (Action/Comedy/Drama/...)
        selectedGenre = getIntent().getStringExtra("genre");
        if (selectedGenre == null) selectedGenre = "All";

        tvTitleMovies.setText("Movies – " + selectedGenre);

        setupMovieButtonsAndFilter();
    }

    private void setupMovieButtonsAndFilter() {

        // 1) מיפוי סרטים לקטגוריות
        Map<Integer, List<String>> movieGenres = new HashMap<>();

        movieGenres.put(R.id.btnTitanic,      Arrays.asList("Romance", "Drama"));
        movieGenres.put(R.id.btnForrestGump,  Arrays.asList("Drama", "Comedy"));
        movieGenres.put(R.id.btnMatrix,       Arrays.asList("Action", "Sci-Fi"));
        movieGenres.put(R.id.btnBoozgalos,    Arrays.asList("Comedy"));
        movieGenres.put(R.id.btnOppenheimer,  Arrays.asList("Drama"));
        movieGenres.put(R.id.btnFightClub,    Arrays.asList("Action", "Drama"));
        movieGenres.put(R.id.btnToyStory,     Arrays.asList("Comedy"));
        movieGenres.put(R.id.btnMeg,          Arrays.asList("Horror", "Action"));
        movieGenres.put(R.id.btnMeanGirls,    Arrays.asList("Comedy"));
        movieGenres.put(R.id.btnMissy,        Arrays.asList("Comedy"));
        movieGenres.put(R.id.btnchiks,        Arrays.asList("Comedy"));
        movieGenres.put(R.id.btnUp,           Arrays.asList("Comedy"));
        movieGenres.put(R.id.btnToystory,     Arrays.asList("Comedy"));
        movieGenres.put(R.id.btnToalltheboys, Arrays.asList("Romance"));
        movieGenres.put(R.id.btnPianist,      Arrays.asList("Drama"));
        movieGenres.put(R.id.btnGodfather,    Arrays.asList("Drama"));
        movieGenres.put(R.id.btnSchindlers,   Arrays.asList("Drama"));
        movieGenres.put(R.id.btnT,           Arrays.asList("Horror")); // ⭐ IT

        // 2) קליקים – כולם פותחים MovieContentActivity עם נתונים מתאימים

        findViewById(R.id.btnTitanic).setOnClickListener(v -> openTitanic());
        findViewById(R.id.btnForrestGump).setOnClickListener(v -> openForrestGump());
        findViewById(R.id.btnMatrix).setOnClickListener(v -> openMatrix());
        findViewById(R.id.btnBoozgalos).setOnClickListener(v -> openBoozgalosMovie());
        findViewById(R.id.btnOppenheimer).setOnClickListener(v -> openOppenheimer());
        findViewById(R.id.btnFightClub).setOnClickListener(v -> openFightClub());
        findViewById(R.id.btnToyStory).setOnClickListener(v -> openToyStory());
        findViewById(R.id.btnMeg).setOnClickListener(v -> openMeg());
        findViewById(R.id.btnMeanGirls).setOnClickListener(v -> openMeanGirls());
        findViewById(R.id.btnMissy).setOnClickListener(v -> openMissy());
        findViewById(R.id.btnchiks).setOnClickListener(v -> openWhiteChicks());
        findViewById(R.id.btnUp).setOnClickListener(v -> openUpMovie());
        findViewById(R.id.btnToystory).setOnClickListener(v -> openToyStory()); // אותו סרט
        findViewById(R.id.btnToalltheboys).setOnClickListener(v -> openToAllTheBoys());
        findViewById(R.id.btnPianist).setOnClickListener(v -> openPianist());
        findViewById(R.id.btnGodfather).setOnClickListener(v -> openGodfather());
        findViewById(R.id.btnSchindlers).setOnClickListener(v -> openSchindlersList());
        findViewById(R.id.btnT).setOnClickListener(v -> openIT()); // ⭐ IT

        // 3) סינון פוסטרים לפי קטגוריה
        if (!selectedGenre.equals("All")) {
            for (Map.Entry<Integer, List<String>> entry : movieGenres.entrySet()) {
                View posterBtn = findViewById(entry.getKey());
                List<String> genresList = entry.getValue();

                if (posterBtn != null) {
                    boolean shouldShow = genresList.contains(selectedGenre);
                    posterBtn.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    // ---------- פונקציות פתיחה לכל סרט ----------

    private void openTitanic() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "titanic_1997");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Titanic (1997)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Titanic+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.titanic_poster);
        startActivity(i);
    }

    private void openForrestGump() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "forrest_gump_1994");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Forrest Gump (1994)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Forrest+Gump+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.forrest_gump_poster);
        startActivity(i);
    }

    private void openMatrix() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "matrix_1999");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Matrix (1999)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Matrix+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.matrix_poster);
        startActivity(i);
    }

    private void openBoozgalosMovie() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "boozgalos_2024");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "בוזגלוס – הסרט (2024)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/watch?v=W5BYnCTs-Hc");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.boozgalos_poster);
        startActivity(i);
    }

    private void openOppenheimer() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "oppenheimer_2023");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Oppenheimer (2023)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Oppenheimer+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.oppenheimer_poster);
        startActivity(i);
    }

    private void openFightClub() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "fight_club_1999");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Fight Club (1999)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Fight+Club+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.fight_club_poster);
        startActivity(i);
    }

    private void openToyStory() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "toy_story_1995");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Toy Story (1995)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Toy+Story+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.toy_story_poster);
        startActivity(i);
    }

    private void openMeg() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "the_meg_2018");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Meg (2018)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Meg+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.the_meg_poster);
        startActivity(i);
    }

    private void openMeanGirls() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "mean_girls_2004");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Mean Girls (2004)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Mean+Girls+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.mean_girls_poster);
        startActivity(i);
    }

    private void openMissy() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "the_wrong_missy_2020");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Wrong Missy (2020)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Wrong+Missy+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.wrong_missy_poster);
        startActivity(i);
    }

    private void openWhiteChicks() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "white_chicks_2004");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "White Chicks (2004)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=White+Chicks+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.white_chicks_poster);
        startActivity(i);
    }

    private void openUpMovie() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "up_2009");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Up (2009)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Up+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.up_poster);
        startActivity(i);
    }

    private void openToAllTheBoys() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "to_all_the_boys_2018");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE,
                "To All the Boys I've Loved Before (2018)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=To+All+the+Boys+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.to_all_the_boys_poster);
        startActivity(i);
    }

    private void openPianist() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "the_pianist_2002");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Pianist (2002)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Pianist+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.the_pianist_poster);
        startActivity(i);
    }

    private void openGodfather() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "the_godfather_1972");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Godfather (1972)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Godfather+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.the_godfather_poster);
        startActivity(i);
    }

    private void openSchindlersList() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "schindlers_list_1993");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Schindler's List (1993)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Schindler%27s+List+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.schindlers_list_poster);
        startActivity(i);
    }

    private void openIT() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "it_2017");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "IT (2017)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/watch?v=FnCdOQsX5kc");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.it_poster);
        startActivity(i);
    }
}
