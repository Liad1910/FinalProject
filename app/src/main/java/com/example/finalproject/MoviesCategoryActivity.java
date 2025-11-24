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

        // 🔥 1) מיפוי סרטים לקטגוריות באנגלית (לפי התפריט שלך)
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


        // 🔥 2) חיבור קליקים
        findViewById(R.id.btnTitanic).setOnClickListener(v ->
                startActivity(new Intent(this, titanic.class)));

        findViewById(R.id.btnForrestGump).setOnClickListener(v ->
                startActivity(new Intent(this, ForrestGumpActivity.class)));

        findViewById(R.id.btnMatrix).setOnClickListener(v ->
                startActivity(new Intent(this, MatrixActivity.class)));

        findViewById(R.id.btnBoozgalos).setOnClickListener(v ->
                startActivity(new Intent(this, BoozgalosMovieActivity.class)));

        findViewById(R.id.btnOppenheimer).setOnClickListener(v ->
                startActivity(new Intent(this, OppenheimerActivity.class)));

        findViewById(R.id.btnFightClub).setOnClickListener(v ->
                startActivity(new Intent(this, FightClubActivity.class)));

        findViewById(R.id.btnToyStory).setOnClickListener(v ->
                startActivity(new Intent(this, ToyStoryActivity.class)));

        findViewById(R.id.btnMeg).setOnClickListener(v ->
                startActivity(new Intent(this, TheMegActivity.class)));

        findViewById(R.id.btnMeanGirls).setOnClickListener(v ->
                startActivity(new Intent(this, MeanGirlsActivity.class)));

        findViewById(R.id.btnMissy).setOnClickListener(v ->
                startActivity(new Intent(this, WrongMissyActivity.class)));

        findViewById(R.id.btnchiks).setOnClickListener(v ->
                startActivity(new Intent(this, WhiteChiksActivity.class)));

        findViewById(R.id.btnUp).setOnClickListener(v ->
                startActivity(new Intent(this, UpActivity.class)));

        findViewById(R.id.btnToystory).setOnClickListener(v ->
                startActivity(new Intent(this, ToyStoryActivity.class)));

        findViewById(R.id.btnToalltheboys).setOnClickListener(v ->
                startActivity(new Intent(this, ToAllTheBoysActivity.class)));

        findViewById(R.id.btnPianist).setOnClickListener(v ->
                startActivity(new Intent(this, ThePianistActivity.class)));

        findViewById(R.id.btnGodfather).setOnClickListener(v ->
                startActivity(new Intent(this, TheGodfatherActivity.class)));

        findViewById(R.id.btnSchindlers).setOnClickListener(v ->
                startActivity(new Intent(this, SchindlersListActivity.class)));


        // 🔥 3) סינון פוסטרים לפי קטגוריה
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
}
