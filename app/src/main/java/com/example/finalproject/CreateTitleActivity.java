package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class CreateTitleActivity extends AppCompatActivity {

    private static final String TMDB_API_KEY = "PUT_YOUR_TMDB_KEY_HERE";
    private static final String TMDB_IMG_BASE = "https://image.tmdb.org/t/p/w500";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";

    private EditText etTitle, etYear;
    private RadioButton rbMovie, rbSeries;
    private Spinner spGenre;
    private Button btnCreate;

    private FirebaseFirestore db;
    private FirebaseUser user;

    private TmdbApi tmdbApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_title);

        etTitle = findViewById(R.id.etTitleName);
        etYear  = findViewById(R.id.etTitleYear);
        rbMovie = findViewById(R.id.rbMovie);
        rbSeries= findViewById(R.id.rbSeries);
        spGenre = findViewById(R.id.spGenre);
        btnCreate = findViewById(R.id.btnCreateTitle);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.genres_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGenre.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        tmdbApi = retrofit.create(TmdbApi.class);

        btnCreate.setOnClickListener(v -> createOrOpen());
    }

    private void createOrOpen() {
        final String title = etTitle.getText().toString().trim();
        final String yearStr = etYear.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "תכתבי שם של סרט/סדרה", Toast.LENGTH_SHORT).show();
            return;
        }

        final String type = rbSeries.isChecked() ? "series" : "movie";

        final Integer yearFinal;
        if (!TextUtils.isEmpty(yearStr)) {
            try {
                yearFinal = Integer.parseInt(yearStr);
            } catch (Exception e) {
                Toast.makeText(this, "שנה לא תקינה", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            yearFinal = null;
        }

        final String genre = spGenre.getSelectedItem().toString();
        final String titleId = buildTitleId(type, title, yearFinal);

        db.collection("titles").document(titleId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        openTitle(titleId);
                    } else {
                        createTitleDocWithTmdb(titleId, type, title, yearFinal, genre);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void createTitleDocWithTmdb(String titleId, String type, String title, Integer year, String genre) {

        // דיפולטים (למקרה שאין פוסטר)
        String posterResName = type.equals("series")
                ? "poster_default_series"
                : "poster_default_movie";

        // בסיס הדאטה
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("title", title);
        if (year != null) data.put("year", year);
        data.put("genres", Arrays.asList(genre));
        data.put("posterResName", posterResName);
        data.put("createdAt", System.currentTimeMillis());
        if (user != null) data.put("createdBy", user.getUid());

        // אם אין מפתח TMDB – נשמור בלי פוסטרUrl
        if (TMDB_API_KEY == null || TMDB_API_KEY.contains("PUT_YOUR")) {
            db.collection("titles").document(titleId).set(data)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "העמוד נוצר ✅ (בלי TMDB)", Toast.LENGTH_SHORT).show();
                        openTitle(titleId);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "שגיאה ביצירה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
            return;
        }

        // חיפוש ב-TMDB
        Call<TmdbSearchResponse> call;
        if ("series".equals(type)) {
            call = tmdbApi.searchTv(TMDB_API_KEY, title, year);
        } else {
            call = tmdbApi.searchMovie(TMDB_API_KEY, title, year);
        }

        call.enqueue(new Callback<TmdbSearchResponse>() {
            @Override
            public void onResponse(Call<TmdbSearchResponse> call, Response<TmdbSearchResponse> response) {

                if (response.isSuccessful() && response.body() != null
                        && response.body().results != null && !response.body().results.isEmpty()) {

                    TmdbResult first = response.body().results.get(0);

                    if (first.posterPath != null && !first.posterPath.trim().isEmpty()) {
                        String posterUrl = TMDB_IMG_BASE + first.posterPath;

                        data.put("posterUrl", posterUrl);
                        data.put("tmdbId", first.id);
                        data.put("posterSource", "tmdb");
                    }
                }

                db.collection("titles").document(titleId).set(data)
                        .addOnSuccessListener(a -> {
                            Toast.makeText(CreateTitleActivity.this, "העמוד נוצר ✅", Toast.LENGTH_SHORT).show();
                            openTitle(titleId);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(CreateTitleActivity.this, "שגיאה ביצירה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
            }

            @Override
            public void onFailure(Call<TmdbSearchResponse> call, Throwable t) {
                // גם אם TMDB נכשל – נשמור בלי posterUrl
                db.collection("titles").document(titleId).set(data)
                        .addOnSuccessListener(a -> {
                            Toast.makeText(CreateTitleActivity.this, "נוצר ✅ (TMDB נכשל)", Toast.LENGTH_SHORT).show();
                            openTitle(titleId);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(CreateTitleActivity.this, "שגיאה ביצירה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
            }
        });
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

        String y = (year == null) ? "0" : String.valueOf(year);
        return type + "_" + clean + "_" + y;
    }
}
