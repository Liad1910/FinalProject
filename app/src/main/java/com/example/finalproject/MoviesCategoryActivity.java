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

        // 1) מיפוי סרטים לקטגוריות (עבור הסינון)
        Map<Integer, List<String>> movieGenres = new HashMap<>();

        // --- סרטים שהיו כבר ---
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
        movieGenres.put(R.id.btnT,            Arrays.asList("Horror")); // IT

        // --- סרטים חדשים שהוספת ---

        movieGenres.put(R.id.btnAvatar,            Arrays.asList("Action", "Sci-Fi"));
        movieGenres.put(R.id.btnDarkKnight,        Arrays.asList("Action"));
        movieGenres.put(R.id.btnInception,         Arrays.asList("Action", "Sci-Fi"));
        movieGenres.put(R.id.btnEndgame,           Arrays.asList("Action"));
        movieGenres.put(R.id.btnLaLaLand,          Arrays.asList("Romance"));
        movieGenres.put(R.id.btnLotrReturnKing,    Arrays.asList("Action", "Drama"));
        movieGenres.put(R.id.btnJoker,             Arrays.asList("Drama"));
        movieGenres.put(R.id.btnHarryPotter1,      Arrays.asList("Action", "Fantasy"));
        movieGenres.put(R.id.btnPlanetApes,        Arrays.asList("Sci-Fi"));
        movieGenres.put(R.id.btnT2,                Arrays.asList("Action", "Sci-Fi"));
        movieGenres.put(R.id.btnPirates1,          Arrays.asList("Action", "Comedy"));
        movieGenres.put(R.id.btnToyStory3,         Arrays.asList("Comedy"));
        movieGenres.put(R.id.btnBarbie,            Arrays.asList("Comedy"));
        movieGenres.put(R.id.btnTopGunMaverick,    Arrays.asList("Action"));

        // 2) קליקים – כל כפתור פותח את MovieContentActivity

        // ישנים
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
        findViewById(R.id.btnToystory).setOnClickListener(v -> openToyStory());
        findViewById(R.id.btnToalltheboys).setOnClickListener(v -> openToAllTheBoys());
        findViewById(R.id.btnPianist).setOnClickListener(v -> openPianist());
        findViewById(R.id.btnGodfather).setOnClickListener(v -> openGodfather());
        findViewById(R.id.btnSchindlers).setOnClickListener(v -> openSchindlersList());
        findViewById(R.id.btnT).setOnClickListener(v -> openIT());

        // חדשים
        findViewById(R.id.btnAvatar).setOnClickListener(v -> openAvatar());
        findViewById(R.id.btnDarkKnight).setOnClickListener(v -> openDarkKnight());
        findViewById(R.id.btnInception).setOnClickListener(v -> openInception());
        findViewById(R.id.btnEndgame).setOnClickListener(v -> openEndgame());
        findViewById(R.id.btnLaLaLand).setOnClickListener(v -> openLaLaLand());
        findViewById(R.id.btnLotrReturnKing).setOnClickListener(v -> openLotrReturnKing());
        findViewById(R.id.btnJoker).setOnClickListener(v -> openJoker());
        findViewById(R.id.btnHarryPotter1).setOnClickListener(v -> openHarryPotter1());
        findViewById(R.id.btnPlanetApes).setOnClickListener(v -> openPlanetApes());
        findViewById(R.id.btnT2).setOnClickListener(v -> openT2());
        findViewById(R.id.btnPirates1).setOnClickListener(v -> openPirates1());
        findViewById(R.id.btnToyStory3).setOnClickListener(v -> openToyStory3());
        findViewById(R.id.btnBarbie).setOnClickListener(v -> openBarbie());
        findViewById(R.id.btnTopGunMaverick).setOnClickListener(v -> openTopGunMaverick());

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

    // ---------- פונקציות פתיחה לסרטים קיימים ----------

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

    // ---------- פונקציות פתיחה לסרטים החדשים ----------

    private void openAvatar() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "avatar_2009");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Avatar (2009)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Avatar+2009+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.avatar_poster);
        startActivity(i);
    }

    private void openDarkKnight() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "dark_knight_2008");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "The Dark Knight (2008)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=The+Dark+Knight+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.dark_knight_poster);
        startActivity(i);
    }

    private void openInception() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "inception_2010");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Inception (2010)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Inception+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.inception_poster);
        startActivity(i);
    }

    private void openEndgame() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "avengers_endgame_2019");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Avengers: Endgame (2019)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Avengers+Endgame+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.endgame_poster);
        startActivity(i);
    }

    private void openLaLaLand() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "la_la_land_2016");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "La La Land (2016)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=La+La+Land+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.la_la_land_poster);
        startActivity(i);
    }

    private void openLotrReturnKing() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "lotr_return_king_2003");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE,
                "The Lord of the Rings: The Return of the King (2003)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Return+of+the+King+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.lotr_return_king_poster);
        startActivity(i);
    }

    private void openJoker() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "joker_2019");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Joker (2019)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Joker+2019+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.joker_poster);
        startActivity(i);
    }

    private void openHarryPotter1() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "harry_potter_sorcerer_2001");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE,
                "Harry Potter and the Sorcerer's Stone (2001)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Harry+Potter+and+the+Sorcerer%27s+Stone+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.harry_potter1_poster);
        startActivity(i);
    }

    private void openPlanetApes() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "planet_of_the_apes_1968");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Planet of the Apes (1968)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Planet+of+the+Apes+1968+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.planet_apes_poster);
        startActivity(i);
    }

    private void openT2() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "terminator_2_1991");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Terminator 2: Judgment Day (1991)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Terminator+2+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.t2_poster);
        startActivity(i);
    }

    private void openPirates1() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "pirates_curse_black_pearl_2003");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE,
                "Pirates of the Caribbean: The Curse of the Black Pearl (2003)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Pirates+of+the+Caribbean+Curse+of+the+Black+Pearl+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.pirates1_poster);
        startActivity(i);
    }

    private void openToyStory3() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "toy_story_3_2010");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Toy Story 3 (2010)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Toy+Story+3+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.toy_story3_poster);
        startActivity(i);
    }

    private void openBarbie() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "barbie_2023");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Barbie (2023)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Barbie+2023+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.barbie_poster);
        startActivity(i);
    }

    private void openTopGunMaverick() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, "top_gun_maverick_2022");
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, "Top Gun: Maverick (2022)");
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL,
                "https://www.youtube.com/results?search_query=Top+Gun+Maverick+trailer");
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, R.drawable.top_gun_maverick_poster);
        startActivity(i);
    }
}
