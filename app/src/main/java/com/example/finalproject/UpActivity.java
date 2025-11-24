package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// ✅ CHANGED: במקום AppCompatActivity
public class UpActivity extends BaseActivity {   // ✅ CHANGED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        // ✅ CHANGED: במקום setContentView
        setPageContent(R.layout.up);   // ✅ CHANGED

        // הגדרת התאמה לגודל המסך
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.moviePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // חיבור הכפתור לפי ה-ID מה-layout
        Button btnTrailer = findViewById(R.id.btnTrailer);

        // מאזין לכפתור הטריילר
        btnTrailer.setOnClickListener(v -> {
            String trailerUrl = "https://www.youtube.com/watch?v=ORFWdXl_zJ4"; // Up (2009)
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
            startActivity(intent);
        });
    }
}
