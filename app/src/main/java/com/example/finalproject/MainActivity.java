package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // ----- שדות למסך -----
    private TextView tvHelloMain;
    private Button btnGoLogin, btnGo, btnLogout, btnUserPage;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private FirebaseAuth.AuthStateListener authListener;
    private ListenerRegistration userDocListener;

    // ----- שדות לקרוסלת תמונות -----
    private ImageView ivMainImage;
    private ImageButton btnNext, btnPrev, btnMenu;

    private int currentIndex = 0;

    // Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    // תמונות הפוסטרים של הסרטים (חייב להתאים ל-drawable)
    private final int[] images = {
            R.drawable.up_poster,               // Up
            R.drawable.white_chicks_poster,     // White Chicks
            R.drawable.to_all_the_boys_poster,  // To All the Boys I've Loved Before
            R.drawable.the_pianist_poster,      // The Pianist
            R.drawable.it_poster                // IT
    };

    // מזהי סרטים (למועדפים)
    private final String[] carouselMovieIds = {
            "up_2009",
            "white_chicks_2004",
            "to_all_the_boys_2018",
            "the_pianist_2002",
            "it_2017"
    };

    // שמות הסרטים
    private final String[] carouselMovieTitles = {
            "Up (2009)",
            "White Chicks (2004)",
            "To All the Boys I've Loved Before (2018)",
            "The Pianist (2002)",
            "IT (2017)"
    };

    // קישורי טריילר (אפשר להחליף לטריילר אחר אם תרצי)
    private final String[] carouselTrailerUrls = {
            "https://www.youtube.com/results?search_query=Up+trailer",
            "https://www.youtube.com/results?search_query=White+Chicks+trailer",
            "https://www.youtube.com/results?search_query=To+All+the+Boys+I%27ve+Loved+Before+trailer",
            "https://www.youtube.com/results?search_query=The+Pianist+trailer",
            "https://www.youtube.com/watch?v=FnCdOQsX5kc" // IT official trailer
    };

    // ----- onCreate -----
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Drawer + NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navView      = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        // מציאת רכיבים
        tvHelloMain = findViewById(R.id.tvHelloMain);
        btnGoLogin  = findViewById(R.id.btnGoLogin);
        btnGo       = findViewById(R.id.btnGo);
        btnLogout   = findViewById(R.id.btnLogout);
        btnUserPage = findViewById(R.id.btnUserPage);

        ivMainImage = findViewById(R.id.ivMainImage);
        btnNext     = findViewById(R.id.btnNext);
        btnPrev     = findViewById(R.id.btnPrev);
        btnMenu     = findViewById(R.id.btnMenu);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ----- ניווטים בסיסיים -----
        btnGoLogin.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, loginPage.class)));

        btnGo.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, registerPage.class)));

        btnLogout.setOnClickListener(this::logout);

        btnUserPage.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, activity_user_page.class)));

        // כפתור 3 קווים – פתיחת התפריט הצדדי
        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        // ----- טיפול בתמונות קרוסלה -----
        ivMainImage.setImageResource(images[currentIndex]);

        // לחיצה על התמונה המרכזית – פותחת MovieContentActivity עם הסרט המתאים
        ivMainImage.setOnClickListener(v -> openCurrentCarouselMovie());

        btnNext.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % images.length;
            ivMainImage.setImageResource(images[currentIndex]);
        });

        btnPrev.setOnClickListener(v -> {
            currentIndex = (currentIndex - 1 + images.length) % images.length;
            ivMainImage.setImageResource(images[currentIndex]);
        });

        // ----- מאזין התחברות לפיירבייס -----
        authListener = fa -> {
            FirebaseUser user = fa.getCurrentUser();
            if (user == null) {
                showAnonymousUI();
                detachUserDocListener();
            } else {
                showLoggedInUI();
                attachUserDocListener(user.getUid(), user);
            }
        };
    }

    // פותח את הסרט לפי ה-index הנוכחי בקרוסלה
    private void openCurrentCarouselMovie() {
        Intent i = new Intent(MainActivity.this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID,    carouselMovieIds[currentIndex]);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, carouselMovieTitles[currentIndex]);
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL, carouselTrailerUrls[currentIndex]);
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, images[currentIndex]);
        startActivity(i);
    }

    // ----- טיפול בלחיצות בתפריט הצד -----
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Movies
        if (id == R.id.nav_movies_action) {
            openMovies("Action");
        } else if (id == R.id.nav_movies_comedy) {
            openMovies("Comedy");
        } else if (id == R.id.nav_movies_drama) {
            openMovies("Drama");
        } else if (id == R.id.nav_movies_horror) {
            openMovies("Horror");
        } else if (id == R.id.nav_movies_romance) {
            openMovies("Romance");
        } else if (id == R.id.nav_movies_scifi) {
            openMovies("Sci-Fi");
        }

        // Series
        else if (id == R.id.nav_series_action) {
            openSeries("Action");
        } else if (id == R.id.nav_series_comedy) {
            openSeries("Comedy");
        } else if (id == R.id.nav_series_drama) {
            openSeries("Drama");
        } else if (id == R.id.nav_series_horror) {
            openSeries("Horror");
        } else if (id == R.id.nav_series_romance) {
            openSeries("Romance");
        } else if (id == R.id.nav_series_scifi) {
            openSeries("Sci-Fi");
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openMovies(String genre) {
        Intent intent = new Intent(this, MoviesCategoryActivity.class);
        intent.putExtra("genre", genre);
        startActivity(intent);
    }

    private void openSeries(String genre) {
        Intent intent = new Intent(this, SeriesCategoryActivity.class);
        intent.putExtra("genre", genre);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // ----- מחזור חיים -----
    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
        detachUserDocListener();
    }

    // ----- עבודה עם Firestore בשביל השם -----
    private void attachUserDocListener(String uid, FirebaseUser fallbackUser) {
        detachUserDocListener();
        DocumentReference ref = db.collection("users").document(uid);
        userDocListener = ref.addSnapshotListener((snap, e) -> {
            String nameFromFs = (snap != null && snap.exists()) ? snap.getString("username") : null;
            String display = firstNonEmpty(
                    nameFromFs,
                    safe(fallbackUser.getDisplayName()),
                    safe(fallbackUser.getEmail())
            );
            tvHelloMain.setText(display != null ? "שלום " + display : "שלום אנונימי");
        });
    }

    private void detachUserDocListener() {
        if (userDocListener != null) {
            userDocListener.remove();
            userDocListener = null;
        }
    }

    // ----- מצבי UI -----
    private void showAnonymousUI() {
        tvHelloMain.setText("שלום אנונימי");
        btnLogout.setVisibility(View.GONE);     // מסתירים התנתקות
        btnUserPage.setVisibility(View.GONE);   // מסתירים דף משתמש
        btnGoLogin.setVisibility(View.VISIBLE); // מראים לוגין
        btnGo.setVisibility(View.VISIBLE);      // מראים הרשמה
    }

    private void showLoggedInUI() {
        btnLogout.setVisibility(View.VISIBLE);   // מראים התנתקות
        btnUserPage.setVisibility(View.VISIBLE); // מראים דף משתמש
        btnGoLogin.setVisibility(View.GONE);     // מסתירים לוגין
        btnGo.setVisibility(View.GONE);          // מסתירים הרשמה
    }

    // ----- פונקציות עזר -----
    private static String safe(String s) {
        return (s != null && !s.trim().isEmpty()) ? s.trim() : null;
    }

    private static String firstNonEmpty(String... arr) {
        for (String s : arr) {
            if (safe(s) != null) return s.trim();
        }
        return null;
    }

    public void logout(View view) {
        getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .edit().putBoolean("stayConnect", false).apply();
        auth.signOut();
        showAnonymousUI();
        detachUserDocListener();
    }
}
