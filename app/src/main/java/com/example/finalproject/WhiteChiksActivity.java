package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

public class WhiteChiksActivity extends BaseActivity {   // ✅ CHANGED: ירשנו BaseActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageContent(R.layout.white_chiks);   // ✅ CHANGED: במקום setContentView

        Button btnTrailer = findViewById(R.id.btnTrailer);

        btnTrailer.setOnClickListener(v -> {
            String trailerUrl = "https://www.youtube.com/watch?v=aeVkbNka9HM";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl)));
        });
    }
}
