package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateTitleActivity extends BaseActivity {

    // 🔑 TMDB
    private static final String TMDB_API_KEY = "ce829465ca9e4f15441987a1f3624293";
    private static final String TMDB_IMG_BASE = "https://image.tmdb.org/t/p/w500";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";

    private EditText etTitle, etYear;
    private RadioButton rbMovie, rbSeries;
    private Spinner spGenre;
    private TextView tvSelectedGenre;
    private Button btnCreate;

    private FirebaseFirestore db;
    private FirebaseUser user;
    private TmdbApi tmdbApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ עם Drawer + toolbar ורוד
        setPageContent(R.layout.activity_create_title);

        // Views
        etTitle = findViewById(R.id.etTitleName);
        etYear  = findViewById(R.id.etTitleYear);
        rbMovie = findViewById(R.id.rbMovie);
        rbSeries= findViewById(R.id.rbSeries);
        spGenre = findViewById(R.id.spGenre);
        tvSelectedGenre = findViewById(R.id.tvSelectedGenre);
        btnCreate = findViewById(R.id.btnCreateTitle);

        // Spinner adapter (טקסט לבן)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.genres_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGenre.setAdapter(adapter);

        // ✅ תמיד רואים איזה ז׳אנר נבחר
        spGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String genre = parent.getItemAtPosition(position).toString();
                tvSelectedGenre.setText("Genre: " + genre);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvSelectedGenre.setText("Genre: לא נבחר");
            }
        });

        // Firebase
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Retrofit TMDB
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        tmdbApi = retrofit.create(TmdbApi.class);

        btnCreate.setOnClickListener(v -> createOrOpen());
    }

    private void createOrOpen() {
        String title = etTitle.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "תכתבי שם של סרט/סדרה", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = rbSeries.isChecked() ? "series" : "movie";

        Integer year = null;
        if (!yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (Exception e) {
                Toast.makeText(this, "שנה לא תקינה", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String genre = (spGenre.getSelectedItem() != null) ? spGenre.getSelectedItem().toString() : "Unknown";
        String titleId = buildTitleId(type, title, year);

        Integer finalYear = year;

        db.collection("titles").document(titleId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        openTitle(titleId);
                    } else {
                        createWithTmdb(titleId, type, title, finalYear, genre);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void createWithTmdb(String titleId, String type, String title, Integer year, String genre) {

        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("title", title);
        if (year != null) data.put("year", year);
        data.put("genres", Arrays.asList(genre));
        data.put("posterResName",
                type.equals("series") ? "poster_default_series" : "poster_default_movie");
        data.put("createdAt", System.currentTimeMillis());
        if (user != null) data.put("createdBy", user.getUid());

        // אם אין מפתח אמיתי
        if (TMDB_API_KEY == null || TMDB_API_KEY.trim().isEmpty() || TMDB_API_KEY.contains("PASTE")) {
            saveAndOpen(titleId, data, "בלי TMDB");
            return;
        }

        Call<TmdbSearchResponse> call =
                type.equals("series")
                        ? tmdbApi.searchTv(TMDB_API_KEY, title, year)
                        : tmdbApi.searchMovie(TMDB_API_KEY, title, year);

        call.enqueue(new Callback<TmdbSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<TmdbSearchResponse> call,
                                   @NonNull Response<TmdbSearchResponse> response) {

                Log.d("TMDB", "code=" + response.code());

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().results != null
                        && !response.body().results.isEmpty()) {

                    // ✅ אם המבנה שלך הוא TmdbSearchResponse.TmdbResult תשני בהתאם
                    TmdbResult first = response.body().results.get(0);

                    if (first.posterPath != null && !first.posterPath.trim().isEmpty()) {
                        data.put("posterUrl", TMDB_IMG_BASE + first.posterPath);
                        data.put("tmdbId", first.id);
                        data.put("posterSource", "tmdb");
                        Log.d("TMDB", "poster found ✔");
                    } else {
                        Log.d("TMDB", "no poster_path");
                    }
                } else {
                    Log.d("TMDB", "no results");
                }

                saveAndOpen(titleId, data, "עם TMDB");
            }

            @Override
            public void onFailure(@NonNull Call<TmdbSearchResponse> call, @NonNull Throwable t) {
                Log.e("TMDB", "failure", t);
                saveAndOpen(titleId, data, "TMDB נכשל");
            }
        });
    }

    private void saveAndOpen(String titleId, Map<String, Object> data, String msg) {
        db.collection("titles").document(titleId).set(data)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "נוצר ✅ (" + msg + ")", Toast.LENGTH_SHORT).show();
                    openTitle(titleId);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void openTitle(String titleId) {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_TITLE_ID, titleId);
        startActivity(i);
        finish();
    }

    private String buildTitleId(String type, String title, Integer year) {
        String clean = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "_");

        return type + "_" + clean + "_" + (year == null ? "0" : year);
    }
}
