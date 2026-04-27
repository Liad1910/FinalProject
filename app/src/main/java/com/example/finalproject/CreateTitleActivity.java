package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
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
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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

    private static final String TMDB_API_KEY = "ce829465ca9e4f15441987a1f3624293";
    private static final String TMDB_IMG_BASE = "https://image.tmdb.org/t/p/w500";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";

    private EditText etTitle, etYear;
    private RadioButton rbMovie, rbSeries;
    private Spinner spGenre;
    private TextView tvSelectedGenre;
    private Button btnCreate;
    private BottomNavigationView bottomNav;

    private FirebaseFirestore db;
    private FirebaseUser user;
    private TmdbApi tmdbApi;

    private interface VerifyCb {
        void onResult(boolean exists);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageContent(R.layout.activity_create_title);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        etTitle = findViewById(R.id.etTitleName);
        etYear  = findViewById(R.id.etTitleYear);
        rbMovie = findViewById(R.id.rbMovie);
        rbSeries = findViewById(R.id.rbSeries);
        spGenre = findViewById(R.id.spGenre);
        tvSelectedGenre = findViewById(R.id.tvSelectedGenre);
        btnCreate = findViewById(R.id.btnCreateTitle);
        bottomNav = findViewById(R.id.bottomNav);

        // =====================================================
        // תיקון RadioButton — רק אחד דלוק בכל פעם
        // =====================================================
        rbMovie.setOnClickListener(v -> {
            rbMovie.setChecked(true);
            rbSeries.setChecked(false);
        });

        rbSeries.setOnClickListener(v -> {
            rbSeries.setChecked(true);
            rbMovie.setChecked(false);
        });

        setupBottomNav();
        bottomNav.getMenu().setGroupCheckable(0, false, true);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.genres_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGenre.setAdapter(adapter);

        spGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tvSelectedGenre.setText("Genre: " + parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvSelectedGenre.setText("Genre: לא נבחר");
            }
        });

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        tmdbApi = retrofit.create(TmdbApi.class);

        btnCreate.setOnClickListener(v -> createOrOpen());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { return false; }

    // =====================================================
    // BottomNav
    // =====================================================
    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bnav_home) {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
                return true;
            }
            if (id == R.id.bnav_movies) {
                Intent i = new Intent(this, MoviesCategoryActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
                return true;
            }
            if (id == R.id.bnav_series) {
                Intent i = new Intent(this, SeriesCategoryActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
                return true;
            }
            if (id == R.id.bnav_more) {
                showMoreDialog();
                bottomNav.getMenu().findItem(R.id.bnav_more).setChecked(false);
                return true;
            }
            return false;
        });
    }

    private void showMoreDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = (user != null && !user.isAnonymous());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("עוד");

        if (!isLoggedIn) {
            String[] options = {"התחברות", "הרשמה", "הקולנוע הקרוב", "צ'אט", "צור סרט / סדרה"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: startActivity(new Intent(this, loginPage.class)); break;
                    case 1: startActivity(new Intent(this, registerPage.class)); break;
                    case 2: startActivity(new Intent(this, NearbyCinemaFreeActivity.class)); break;
                    case 3: startActivity(new Intent(this, AiActivity.class)); break;
                    case 4: return; // כבר פה
                }
            });
        } else {
            String[] options = {"פרופיל", "הקולנוע הקרוב", "צ'אט", "צור סרט / סדרה", "התנתקות"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: startActivity(new Intent(this, activity_user_page.class)); break;
                    case 1: startActivity(new Intent(this, NearbyCinemaFreeActivity.class)); break;
                    case 2: startActivity(new Intent(this, AiActivity.class)); break;
                    case 3: return; // כבר פה
                    case 4:
                        FirebaseAuth.getInstance().signOut();
                        Intent i = new Intent(this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                        finish();
                        break;
                }
            });
        }
        builder.setNegativeButton("סגור", null);
        builder.show();
    }

    // =====================================================
    // Create / Open logic
    // =====================================================
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

        Toast.makeText(this, "בודקת אם קיים ב־TMDB... 🔍", Toast.LENGTH_SHORT).show();
        verifyOnTmdb(type, title, year, exists -> {
            if (!exists) {
                Toast.makeText(this,
                        "לא מצאתי את \"" + title + "\" ב־TMDB 😕 בדקי שהשם נכון (באנגלית)",
                        Toast.LENGTH_LONG).show();
                return;
            }

            db.collection("titles").document(titleId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            openTitle(titleId);
                        } else {
                            createWithTmdb(titleId, type, title, finalYear, genre);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    // =====================================================
    // בדיקת קיום ב־TMDB
    // =====================================================
    private void verifyOnTmdb(String type, String title, Integer year, VerifyCb callback) {
        Call<TmdbSearchResponse> call = type.equals("series")
                ? tmdbApi.searchTv(TMDB_API_KEY, title, year)
                : tmdbApi.searchMovie(TMDB_API_KEY, title, year);

        call.enqueue(new Callback<TmdbSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<TmdbSearchResponse> call,
                                   @NonNull Response<TmdbSearchResponse> response) {
                boolean found = response.isSuccessful()
                        && response.body() != null
                        && response.body().results != null
                        && !response.body().results.isEmpty();
                runOnUiThread(() -> callback.onResult(found));
            }

            @Override
            public void onFailure(@NonNull Call<TmdbSearchResponse> call, @NonNull Throwable t) {
                Log.e("TMDB", "verify failure", t);
                runOnUiThread(() -> callback.onResult(true));
            }
        });
    }

    // =====================================================
    // TMDB create
    // =====================================================
    private void createWithTmdb(String titleId, String type, String title, Integer year, String genre) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("title", title);
        if (year != null) data.put("year", year);
        data.put("genres", Arrays.asList(genre));
        data.put("posterResName", type.equals("series") ? "poster_default_series" : "poster_default_movie");
        data.put("createdAt", System.currentTimeMillis());
        if (user != null) data.put("createdBy", user.getUid());

        if (TMDB_API_KEY == null || TMDB_API_KEY.trim().isEmpty() || TMDB_API_KEY.contains("PASTE")) {
            saveAndOpen(titleId, data, "בלי TMDB");
            return;
        }

        Call<TmdbSearchResponse> call = type.equals("series")
                ? tmdbApi.searchTv(TMDB_API_KEY, title, year)
                : tmdbApi.searchMovie(TMDB_API_KEY, title, year);

        call.enqueue(new Callback<TmdbSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<TmdbSearchResponse> call,
                                   @NonNull Response<TmdbSearchResponse> response) {
                Log.d("TMDB", "code=" + response.code());
                if (response.isSuccessful() && response.body() != null
                        && response.body().results != null && !response.body().results.isEmpty()) {
                    TmdbResult first = response.body().results.get(0);
                    if (first.posterPath != null && !first.posterPath.trim().isEmpty()) {
                        data.put("posterUrl", TMDB_IMG_BASE + first.posterPath);
                        data.put("tmdbId", first.id);
                        data.put("posterSource", "tmdb");
                    }
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
                        Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openTitle(String titleId) {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_TITLE_ID, titleId);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private String buildTitleId(String type, String title, Integer year) {
        String clean = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", "").trim().replaceAll("\\s+", "_");
        return type + "_" + clean + "_" + (year == null ? "0" : year);
    }
}