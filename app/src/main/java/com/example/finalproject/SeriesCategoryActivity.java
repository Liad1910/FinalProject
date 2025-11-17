package com.example.finalproject;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SeriesCategoryActivity extends AppCompatActivity {

    private TextView tvTitleSeries, tvContentSeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_series_category);

        tvTitleSeries   = findViewById(R.id.tvTitleSeries);
        tvContentSeries = findViewById(R.id.tvContentSeries);

        // קבלת הז'אנר מה-Intent
        String genre = getIntent().getStringExtra("genre");
        if (genre == null) genre = "All";

        tvTitleSeries.setText("Series – " + genre);
        tvContentSeries.setText("Here you will see series in the \"" + genre + "\" category.");
    }
}
