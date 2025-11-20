package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TheGodfatherActivity extends AppCompatActivity {

    private static final String TRAILER_URL = "https://www.youtube.com/watch?v=sY1S34973zA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_the_godfather); // לוודא שה-XML נקרא ככה

        Button btnTrailer   = findViewById(R.id.btnTrailer);
        Button btnShare     = findViewById(R.id.btnShare);
        Button btnFavorites = findViewById(R.id.btnFavorites);

        // כפתור טריילר
        btnTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TRAILER_URL));
                startActivity(intent);
            }
        });

        // כפתור שיתוף
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "הסנדק (1972)");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "טריילר רשמי: " + TRAILER_URL);
                startActivity(Intent.createChooser(shareIntent, "שתף דרך"));
            }
        });

        // כפתור מועדפים – פה את מוסיפה את המימוש שלך
        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: לשמור את "הסנדק" למועדפים של המשתמש (Firestore / Local וכו')
            }
        });
    }
}
