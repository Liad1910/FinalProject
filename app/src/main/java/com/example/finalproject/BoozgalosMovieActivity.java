package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

public class BoozgalosMovieActivity extends BaseActivity {   // ✅ CHANGED

    private static final String TRAILER_URL =
            "https://www.youtube.com/watch?v=W5BYnCTs-Hc"; // אפשר לעדכן

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageContent(R.layout.activity_boozgalos_movie);   // ✅ CHANGED

        Button btnTrailer   = findViewById(R.id.btnTrailer);
        Button btnShare     = findViewById(R.id.btnShare);
        Button btnFavorites = findViewById(R.id.btnFavorites);

        // טריילר
        btnTrailer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TRAILER_URL));
            startActivity(intent);
        });

        // שיתוף
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "בוזגלוס – הסרט (2024)");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "טריילר רשמי: " + TRAILER_URL);
            startActivity(Intent.createChooser(shareIntent, "שתף דרך"));
        });

        // מועדפים
        btnFavorites.setOnClickListener(v -> {
            // TODO: save movie t
