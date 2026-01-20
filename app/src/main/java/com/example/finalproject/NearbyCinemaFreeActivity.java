package com.example.finalproject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NearbyCinemaFreeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_cinema_free);

        Toast.makeText(this, "נכנסתי לעמוד קולנוע 🎬", Toast.LENGTH_SHORT).show();

        Button btnNearbyCinemas = findViewById(R.id.btnNearbyCinemas);
        Button btnNowPlaying    = findViewById(R.id.btnNowPlaying);
        Button btnNavigate      = findViewById(R.id.btnNavigate);

        if (btnNearbyCinemas == null || btnNowPlaying == null || btnNavigate == null) {
            Toast.makeText(this, "בעיה ב-IDs של הכפתורים (findViewById החזיר null)", Toast.LENGTH_LONG).show();
            return;
        }

        btnNearbyCinemas.setOnClickListener(v -> {
            Toast.makeText(this, "לחצת: קולנועים קרובים", Toast.LENGTH_SHORT).show();
            openGoogleMapsSearch("בתי קולנוע קרובים אליי");
        });

        btnNowPlaying.setOnClickListener(v -> {
            Toast.makeText(this, "לחצת: סרטים שמוקרנים עכשיו", Toast.LENGTH_SHORT).show();
            openYouTubeSearch("סרטים שמוקרנים עכשיו");
        });

        btnNavigate.setOnClickListener(v -> {
            Toast.makeText(this, "לחצת: ניווט לקולנוע", Toast.LENGTH_SHORT).show();
            openWazeSearch("קולנוע");
        });
    }

    private void openGoogleMapsSearch(String query) {
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");

        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Uri web = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query));
            startActivity(new Intent(Intent.ACTION_VIEW, web));
        }
    }

    private void openYouTubeSearch(String query) {
        Uri yt = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query));
        startActivity(new Intent(Intent.ACTION_VIEW, yt));
    }

    private void openWazeSearch(String query) {
        Uri waze = Uri.parse("https://waze.com/ul?q=" + Uri.encode(query) + "&navigate=yes");
        Intent i = new Intent(Intent.ACTION_VIEW, waze);

        try {
            startActivity(i);
        } catch (Exception e) {
            openGoogleMapsSearch(query);
        }
    }
}
