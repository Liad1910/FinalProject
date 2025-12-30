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

public class CreateTitleActivity extends AppCompatActivity {

    private EditText etTitle, etYear;
    private RadioButton rbMovie, rbSeries;
    private Spinner spGenre;
    private Button btnCreate;

    private FirebaseFirestore db;
    private FirebaseUser user;

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

        // Adapter לז׳אנרים מה-strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.genres_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGenre.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

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

        final String genre = spGenre.getSelectedItem().toString();  // ✅ הז׳אנר שנבחר
        final String titleId = buildTitleId(type, title, yearFinal);

        db.collection("titles").document(titleId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        openTitle(titleId);
                    } else {
                        createTitleDoc(titleId, type, title, yearFinal, genre);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void createTitleDoc(String titleId, String type, String title, Integer year, String genre) {

        String posterResName = type.equals("series")
                ? "poster_default_series"
                : "poster_default_movie";

        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("title", title);
        if (year != null) data.put("year", year);

        // ✅ שומר ז׳אנרים כמו רשימה (כדי שבעתיד תוכלי כמה ז׳אנרים)
        data.put("genres", Arrays.asList(genre));

        data.put("posterResName", posterResName);
        data.put("createdAt", System.currentTimeMillis());
        if (user != null) data.put("createdBy", user.getUid());

        db.collection("titles").document(titleId).set(data)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "העמוד נוצר ✅", Toast.LENGTH_SHORT).show();
                    openTitle(titleId);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה ביצירה: " + e.getMessage(), Toast.LENGTH_LONG).show()
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

        String y = (year == null) ? "0" : String.valueOf(year);
        return type + "_" + clean + "_" + y;
    }
}
