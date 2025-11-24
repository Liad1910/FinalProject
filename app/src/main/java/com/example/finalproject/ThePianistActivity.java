package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

public class ThePianistActivity extends BaseActivity {   // ✅ CHANGED: יורש BaseActivity

    private static final String TRAILER_URL =
            "https://www.youtube.com/watch?v=BFwGqLa_oAo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageContent(R.layout.the_pianist);   // ✅ CHANGED: במקום setContentView

        Button btnTrailer   = findViewById(R.id.btnTrailer);
        Button btnShare     = findViewById(R.id.btnShare);
        Button btnFavorites = findViewById(R.id.btnFavorites);

        // טריילר
        btnTrailer.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TRAILER_URL)))
        );

        // שיתוף
        btnShare.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "הפסנתרן (2002)");
            i.putExtra(Intent.EXTRA_TEXT, "טריילר: " + TRAILER_URL);
            startActivity(Intent.createChooser(i, "שתף דרך"));
        });

        // מועדפים
        btnFavorites.setOnClickListener(v -> {
            // TODO: save to favorites
        });
    }
}
