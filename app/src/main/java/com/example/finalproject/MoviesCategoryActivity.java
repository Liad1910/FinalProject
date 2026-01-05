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

public class MoviesCategoryActivity extends AppCompatActivity {

    private TextView tvTitleMovies;
    private String selectedGenre;

    // New from users
    private RecyclerView rvUserTitles;
    private TitlesAdapter adapter;
    private final ArrayList<TitleCard> userTitles = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_category);

        tvTitleMovies = findViewById(R.id.tvTitleMovies);

        selectedGenre = getIntent().getStringExtra("genre");
        if (selectedGenre == null) selectedGenre = "All";

        tvTitleMovies.setText("Movies – " + selectedGenre);

        // 1) ישנים: כפתורים מה-XML
        setupLegacyMoviesButtonsAndFilter();

        // 2) חדשים: RecyclerView
        rvUserTitles = findViewById(R.id.rvUserTitles);
        if (rvUserTitles != null) {
            rvUserTitles.setLayoutManager(new GridLayoutManager(this, 3));
            rvUserTitles.setNestedScrollingEnabled(false);

            adapter = new TitlesAdapter(this, userTitles);
            rvUserTitles.setAdapter(adapter);

            db = FirebaseFirestore.getInstance();
            loadUserMovies();
        }
    }

    // =====================================================
    // פונקציה גנרית לפתיחת סרט ישן
    // =====================================================
    private void openLegacyMovie(String id, String title, String trailerUrl, int posterResId) {
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

    // =====================================================
    // 1) כפתורים ישנים: סינון + קליקים
    // =====================================================
    private void setupLegacyMoviesButtonsAndFilter() {

        Map<Integer, List<String>> movieGenres = new HashMap<>();

        // --- ישנים ---
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
        movieGenres.put(R.id.btnT,            Arrays.asList("Horror"));

        // --- חדשים (כפתורים ב-XML) ---
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

        // =========================
        // ✅ קליקים לכל הסרטים
        // =========================

        // ישנים
        safeClick(R.id.btnTitanic, () ->
                openLegacyMovie("titanic_1997", "Titanic (1997)",
                        "https://www.youtube.com/results?search_query=Titanic+trailer",
                        R.drawable.titanic_poster));

        safeClick(R.id.btnForrestGump, () ->
                openLegacyMovie("forrest_gump_1994", "Forrest Gump (1994)",
                        "https://www.youtube.com/results?search_query=Forrest+Gump+trailer",
                        R.drawable.forrest_gump_poster));

        safeClick(R.id.btnMatrix, () ->
                openLegacyMovie("matrix_1999", "The Matrix (1999)",
                        "https://www.youtube.com/results?search_query=The+Matrix+trailer",
                        R.drawable.matrix_poster));

        safeClick(R.id.btnBoozgalos, () ->
                openLegacyMovie("boozgalos_2024", "בוזגלוס – הסרט (2024)",
                        "https://www.youtube.com/watch?v=W5BYnCTs-Hc",
                        R.drawable.boozgalos_poster));

        safeClick(R.id.btnOppenheimer, () ->
                openLegacyMovie("oppenheimer_2023", "Oppenheimer (2023)",
                        "https://www.youtube.com/results?search_query=Oppenheimer+trailer",
                        R.drawable.oppenheimer_poster));

        safeClick(R.id.btnFightClub, () ->
                openLegacyMovie("fight_club_1999", "Fight Club (1999)",
                        "https://www.youtube.com/results?search_query=Fight+Club+trailer",
                        R.drawable.fight_club_poster));

        safeClick(R.id.btnToyStory, () ->
                openLegacyMovie("toy_story_1995", "Toy Story (1995)",
                        "https://www.youtube.com/results?search_query=Toy+Story+trailer",
                        R.drawable.toy_story_poster));

        safeClick(R.id.btnMeg, () ->
                openLegacyMovie("the_meg_2018", "The Meg (2018)",
                        "https://www.youtube.com/results?search_query=The+Meg+trailer",
                        R.drawable.the_meg_poster));

        safeClick(R.id.btnMeanGirls, () ->
                openLegacyMovie("mean_girls_2004", "Mean Girls (2004)",
                        "https://www.youtube.com/results?search_query=Mean+Girls+trailer",
                        R.drawable.mean_girls_poster));

        safeClick(R.id.btnMissy, () ->
                openLegacyMovie("the_wrong_missy_2020", "The Wrong Missy (2020)",
                        "https://www.youtube.com/results?search_query=The+Wrong+Missy+trailer",
                        R.drawable.wrong_missy_poster));

        safeClick(R.id.btnchiks, () ->
                openLegacyMovie("white_chicks_2004", "White Chicks (2004)",
                        "https://www.youtube.com/results?search_query=White+Chicks+trailer",
                        R.drawable.white_chicks_poster));

        safeClick(R.id.btnUp, () ->
                openLegacyMovie("up_2009", "Up (2009)",
                        "https://www.youtube.com/results?search_query=Up+trailer",
                        R.drawable.up_poster));

        safeClick(R.id.btnToystory, () ->
                openLegacyMovie("toy_story_1995", "Toy Story (1995)",
                        "https://www.youtube.com/results?search_query=Toy+Story+trailer",
                        R.drawable.toy_story_poster));

        safeClick(R.id.btnToalltheboys, () ->
                openLegacyMovie("to_all_the_boys_2018", "To All the Boys I've Loved Before (2018)",
                        "https://www.youtube.com/results?search_query=To+All+the+Boys+trailer",
                        R.drawable.to_all_the_boys_poster));

        safeClick(R.id.btnPianist, () ->
                openLegacyMovie("the_pianist_2002", "The Pianist (2002)",
                        "https://www.youtube.com/results?search_query=The+Pianist+trailer",
                        R.drawable.the_pianist_poster));

        safeClick(R.id.btnGodfather, () ->
                openLegacyMovie("the_godfather_1972", "The Godfather (1972)",
                        "https://www.youtube.com/results?search_query=The+Godfather+trailer",
                        R.drawable.the_godfather_poster));

        safeClick(R.id.btnSchindlers, () ->
                openLegacyMovie("schindlers_list_1993", "Schindler's List (1993)",
                        "https://www.youtube.com/results?search_query=Schindler%27s+List+trailer",
                        R.drawable.schindlers_list_poster));

        safeClick(R.id.btnT, () ->
                openLegacyMovie("it_2017", "IT (2017)",
                        "https://www.youtube.com/watch?v=FnCdOQsX5kc",
                        R.drawable.it_poster));

        // חדשים
        safeClick(R.id.btnAvatar, () ->
                openLegacyMovie("avatar_2009", "Avatar (2009)",
                        "https://www.youtube.com/results?search_query=Avatar+2009+trailer",
                        R.drawable.avatar_poster));

        safeClick(R.id.btnDarkKnight, () ->
                openLegacyMovie("dark_knight_2008", "The Dark Knight (2008)",
                        "https://www.youtube.com/results?search_query=The+Dark+Knight+trailer",
                        R.drawable.dark_knight_poster));

        safeClick(R.id.btnInception, () ->
                openLegacyMovie("inception_2010", "Inception (2010)",
                        "https://www.youtube.com/results?search_query=Inception+trailer",
                        R.drawable.inception_poster));

        safeClick(R.id.btnEndgame, () ->
                openLegacyMovie("avengers_endgame_2019", "Avengers: Endgame (2019)",
                        "https://www.youtube.com/results?search_query=Avengers+Endgame+trailer",
                        R.drawable.endgame_poster));

        safeClick(R.id.btnLaLaLand, () ->
                openLegacyMovie("la_la_land_2016", "La La Land (2016)",
                        "https://www.youtube.com/results?search_query=La+La+Land+trailer",
                        R.drawable.la_la_land_poster));

        safeClick(R.id.btnLotrReturnKing, () ->
                openLegacyMovie("lotr_return_king_2003", "The Lord of the Rings: The Return of the King (2003)",
                        "https://www.youtube.com/results?search_query=Return+of+the+King+trailer",
                        R.drawable.lotr_return_king_poster));

        safeClick(R.id.btnJoker, () ->
                openLegacyMovie("joker_2019", "Joker (2019)",
                        "https://www.youtube.com/results?search_query=Joker+2019+trailer",
                        R.drawable.joker_poster));

        safeClick(R.id.btnHarryPotter1, () ->
                openLegacyMovie("harry_potter_sorcerer_2001", "Harry Potter and the Sorcerer's Stone (2001)",
                        "https://www.youtube.com/results?search_query=Harry+Potter+and+the+Sorcerer%27s+Stone+trailer",
                        R.drawable.harry_potter1_poster));

        safeClick(R.id.btnPlanetApes, () ->
                openLegacyMovie("planet_of_the_apes_1968", "Planet of the Apes (1968)",
                        "https://www.youtube.com/results?search_query=Planet+of+the+Apes+1968+trailer",
                        R.drawable.planet_apes_poster));

        safeClick(R.id.btnT2, () ->
                openLegacyMovie("terminator_2_1991", "Terminator 2: Judgment Day (1991)",
                        "https://www.youtube.com/results?search_query=Terminator+2+trailer",
                        R.drawable.t2_poster));

        safeClick(R.id.btnPirates1, () ->
                openLegacyMovie("pirates_curse_black_pearl_2003", "Pirates of the Caribbean: The Curse of the Black Pearl (2003)",
                        "https://www.youtube.com/results?search_query=Pirates+of+the+Caribbean+Curse+of+the+Black+Pearl+trailer",
                        R.drawable.pirates1_poster));

        safeClick(R.id.btnToyStory3, () ->
                openLegacyMovie("toy_story_3_2010", "Toy Story 3 (2010)",
                        "https://www.youtube.com/results?search_query=Toy+Story+3+trailer",
                        R.drawable.toy_story3_poster));

        safeClick(R.id.btnBarbie, () ->
                openLegacyMovie("barbie_2023", "Barbie (2023)",
                        "https://www.youtube.com/results?search_query=Barbie+2023+trailer",
                        R.drawable.barbie_poster));

        safeClick(R.id.btnTopGunMaverick, () ->
                openLegacyMovie("top_gun_maverick_2022", "Top Gun: Maverick (2022)",
                        "https://www.youtube.com/results?search_query=Top+Gun+Maverick+trailer",
                        R.drawable.top_gun_maverick_poster));

        // =========================
        // ✅ סינון לפי selectedGenre
        // =========================
        if (!"All".equals(selectedGenre)) {
            for (Map.Entry<Integer, List<String>> entry : movieGenres.entrySet()) {
                View btn = findViewById(entry.getKey());
                if (btn != null) {
                    boolean show = entry.getValue().contains(selectedGenre);
                    btn.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    // =====================================================
    // טעינת סרטים חדשים מה-Firestore (שנוצרו ע"י משתמש)
    // =====================================================
    private void loadUserMovies() {

        Query q = db.collection("titles")
                .whereEqualTo("type", "movie");

        if (!"All".equals(selectedGenre)) {
            q = q.whereArrayContains("genres", selectedGenre);
        }

        // ✅ מיון לפי שם
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
