package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ToyStoryActivity extends BaseActivity {   // ✅ CHANGED: נורש BaseActivity

    private static final String TRAILER_URL =
            "https://www.youtube.com/watch?v=v-PjgYDrg70";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageContent(R.layout.activity_toy_story);  // ✅ CHANGED: במקום setContentView

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
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "צעצוע של סיפור (1995)");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "טריילר רשמי: " + TRAILER_URL);
            startActivity(Intent.createChooser(shareIntent, "שתף דרך"));
        });

        // מועדפים (תממשי לפי Firestore/SharedPref)
        btnFavorites.setOnClickListener(v -> {
            // TODO: שמירה למועדפים
        });
    }
}
