package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class BoozgalosMovieActivity extends AppCompatActivity {

    private static final String TRAILER_URL = "https://www.youtube.com/watch?v=W5BYnCTs-Hc"; // דוגמה – אפשר לעדכן

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boozgalos_movie); // לוודא שהקובץ נקרא boozgalos_movie.xml

        Button btnTrailer   = findViewById(R.id.btnTrailer);
        Button btnShare     = findViewById(R.id.btnShare);
        Button btnFavorites = findViewById(R.id.btnFavorites);

        // טריילר
        btnTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TRAILER_URL));
                startActivity(intent);
            }
        });

        // שיתוף
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "בוזגלוס – הסרט (2024)");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "טריילר רשמי: " + TRAILER_URL);
                startActivity(Intent.createChooser(shareIntent, "שתף דרך"));
            }
        });

        // מועדפים – לפי המימוש שלך
        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: שמירה של "בוזגלוס – הסרט" למועדפים של המשתמש
            }
        });
    }
}
