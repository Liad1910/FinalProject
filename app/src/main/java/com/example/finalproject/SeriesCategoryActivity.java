package com.example.finalproject;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class SeriesCategoryActivity extends AppCompatActivity {

    private String selectedGenre;

    private RecyclerView rvUserTitles;
    private TitlesAdapter adapter;
    private ArrayList<TitleCard> userTitles = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_category);

        // ===== Genre =====
        selectedGenre = getIntent().getStringExtra("genre");
        if (selectedGenre == null) selectedGenre = "All";

        TextView tvTitleSeries = findViewById(R.id.tvTitleSeries);
        tvTitleSeries.setText("Series – " + selectedGenre);

        // ===== RecyclerView =====
        rvUserTitles = findViewById(R.id.rvUserTitles);
        rvUserTitles.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new TitlesAdapter(this, userTitles);
        rvUserTitles.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadUserSeries();
    }

    // =====================================================
    // טעינת סדרות שנוצרו ע״י משתמשים
    // =====================================================
    private void loadUserSeries() {

        Query q = db.collection("titles")
                .whereEqualTo("type", "series");

        if (!"All".equals(selectedGenre)) {
            q = q.whereArrayContains("genres", selectedGenre);
        }

        q.addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;

            userTitles.clear();

            for (DocumentSnapshot d : snap.getDocuments()) {

                TitleCard t = new TitleCard();
                t.id = d.getId();                       // ⭐⭐ קריטי
                t.title = d.getString("title");
                t.type = d.getString("type");
                t.posterResName = d.getString("posterResName");

                userTitles.add(t);
            }

            adapter.notifyDataSetChanged();
        });
    }
}
