package com.example.finalproject;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class WhiteChiksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.white_chiks);

        Button btnTrailer = findViewById(R.id.btnTrailer);
        btnTrailer.setOnClickListener(v -> {
            String trailerUrl = "https://www.youtube.com/watch?v=aeVkbNka9HM"; // White Chicks - Trailer
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl)));
        });
    }
}
