package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

public class titanic extends BaseActivity {  // ✅ CHANGED: במקום AppCompatActivity

    private static final String TRAILER_URL =
            "https://www.youtube.com/watch?v=CHekzSiZjrY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageContent(R.layout.activity_titanic);  // ✅ CHANGED: במקום setContentView

        Button btnTrailer   = findViewById(R.id.btnTrailer);
        Button btnShare     = findViewById(R.id.btnShare);
        Button btnFavorites = findViewById(R.id.btnFavorites);

        // כפתור טריילר
        btnTrailer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TRAILER_URL));
            startActivity(intent);
        });

        // כפתור שיתוף
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "טיטאניק (1997)");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "טריילר רשמי: " + TRAILER_URL);
            startActivity(Intent.createChooser(shareIntent, "שתף דרך"));
        });

        // כפתור מועדפים
        btnFavorites.setOnClickListener(v -> {
            // TODO: לשמור למועדפים
        });
    }
}
