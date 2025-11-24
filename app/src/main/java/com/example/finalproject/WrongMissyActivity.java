package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WrongMissyActivity extends BaseActivity {  // ✅ CHANGED: יורשת BaseActivity

    private static final String TRAILER_URL =
            "https://www.youtube.com/watch?v=8Qn_M4W8aIQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageContent(R.layout.activity_wrong_missy);  // ✅ CHANGED: ה-XML הנכון

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
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "מיסי מסתבכת (2020)");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "טריילר: " + TRAILER_URL);
            startActivity(Intent.createChooser(shareIntent, "שתף דרך"));
        });

        // מועדפים – לפי המימוש שלך
        btnFavorites.setOnClickListener(v -> {
            // TODO: לשמור את "מיסי מסתבכת" למועדפים של המשתמש
        });
    }
}
