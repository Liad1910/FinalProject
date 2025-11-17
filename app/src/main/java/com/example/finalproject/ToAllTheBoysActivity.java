package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ToAllTheBoysActivity extends AppCompatActivity {

    private static final String TRAILER_URL = "https://www.youtube.com/watch?v=555oiY9RWM4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.to_all_the_boys);

        Button btnTrailer   = findViewById(R.id.btnTrailer);
        Button btnShare     = findViewById(R.id.btnShare);
        Button btnFavorites = findViewById(R.id.btnFavorites);

        btnTrailer.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TRAILER_URL))));

        btnShare.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "לכל הנערים שאהבתי (2018)");
            i.putExtra(Intent.EXTRA_TEXT, "טריילר: " + TRAILER_URL);
            startActivity(Intent.createChooser(i, "שתף דרך"));
        });

        // שמירה למועדפים – תשאירי לפי המימוש שלך (למשל Firestore)
        btnFavorites.setOnClickListener(v -> {
            // TODO: save to favorites
        });
    }
}
