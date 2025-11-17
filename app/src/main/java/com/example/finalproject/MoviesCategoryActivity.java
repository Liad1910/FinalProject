package com.example.finalproject;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MoviesCategoryActivity extends AppCompatActivity {

    private TextView tvTitleMovies, tvContentMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movies_category);

        tvTitleMovies   = findViewById(R.id.tvTitleMovies);
        tvContentMovies = findViewById(R.id.tvContentMovies);

        // קבלת הז'אנר מה־Intent
        String genre = getIntent().getStringExtra("genre");
        if (genre == null) genre = "All";

        tvTitleMovies.setText("Movies – " + genre);

        // פה בעתיד אפשר להציג רשימת סרטים אמתית מה-DB
        tvContentMovies.setText("Here you will see movies in the \"" + genre + "\" category.");
    }
}
