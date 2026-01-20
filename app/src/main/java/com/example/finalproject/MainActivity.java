package com.example.finalproject;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    BroadcastReceiver broadcastReceiver;
    // ----- שדות למסך -----
    private TextView tvHelloMain;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private FirebaseAuth.AuthStateListener authListener;
    private ListenerRegistration userDocListener;

    // ----- קרוסלת תמונות -----
    private ImageView ivMainImage;
    private ImageButton btnNext, btnPrev, btnMenu;

    private int currentIndex = 0;

    // Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    // תמונות הפוסטרים של הסרטים
    private final int[] images = {
            R.drawable.up_poster,
            R.drawable.white_chicks_poster,
            R.drawable.to_all_the_boys_poster,
            R.drawable.the_pianist_poster,
            R.drawable.it_poster
    };

    private final String[] carouselMovieIds = {
            "up_2009",
            "white_chicks_2004",
            "to_all_the_boys_2018",
            "the_pianist_2002",
            "it_2017"
    };

    private final String[] carouselMovieTitles = {
            "Up (2009)",
            "White Chicks (2004)",
            "To All the Boys I've Loved Before (2018)",
            "The Pianist (2002)",
            "IT (2017)"
    };

    private final String[] carouselTrailerUrls = {
            "https://www.youtube.com/results?search_query=Up+trailer",
            "https://www.youtube.com/results?search_query=White+Chicks+trailer",
            "https://www.youtube.com/results?search_query=To+All+the+Boys+I've+Loved+Before+trailer",
            "https://www.youtube.com/results?search_query=The+Pianist+trailer",
            "https://www.youtube.com/watch?v=FnCdOQsX5kc"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        broadcastReceiver= new Network();
        registerNetworkBrodcastReciver();


        // ✅ Splash Screen – חייב להיות לפני super
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        // רכיבים
        tvHelloMain = findViewById(R.id.tvHelloMain);
        ivMainImage = findViewById(R.id.ivMainImage);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );


        // קרוסלה
        ivMainImage.setImageResource(images[currentIndex]);

        ivMainImage.setOnClickListener(v -> openCurrentCarouselMovie());

        btnNext.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % images.length;
            ivMainImage.setImageResource(images[currentIndex]);
        });

        btnPrev.setOnClickListener(v -> {
            currentIndex = (currentIndex - 1 + images.length) % images.length;
            ivMainImage.setImageResource(images[currentIndex]);
        });

        // מאזין התחברות
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

    // ----- פתיחת סרט מהקרוסלה -----
    private void openCurrentCarouselMovie() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, carouselMovieIds[currentIndex]);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, carouselMovieTitles[currentIndex]);
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL, carouselTrailerUrls[currentIndex]);
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, images[currentIndex]);
        startActivity(i);
    }



    // ----- תפריט צד -----
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_movies_action) openMovies("Action");
        else if (id == R.id.nav_movies_comedy) openMovies("Comedy");
        else if (id == R.id.nav_movies_drama) openMovies("Drama");
        else if (id == R.id.nav_movies_horror) openMovies("Horror");
        else if (id == R.id.nav_movies_romance) openMovies("Romance");
        else if (id == R.id.nav_movies_scifi) openMovies("Sci-Fi");

        else if (id == R.id.nav_series_action) openSeries("Action");
        else if (id == R.id.nav_series_comedy) openSeries("Comedy");
        else if (id == R.id.nav_series_drama) openSeries("Drama");
        else if (id == R.id.nav_series_horror) openSeries("Horror");
        else if (id == R.id.nav_series_romance) openSeries("Romance");
        else if (id == R.id.nav_series_scifi) openSeries("Sci-Fi");

        else if (id == R.id.nav_login) startActivity(new Intent(this, loginPage.class));
        else if (id == R.id.nav_register) startActivity(new Intent(this, registerPage.class));
        else if (id == R.id.nav_user_page) startActivity(new Intent(this, activity_user_page.class));
        else if (id == R.id.nav_create_title) {
            startActivity(new Intent(this, CreateTitleActivity.class));
        }
       else if (id == R.id.nav_nearby_cinema) {
            Toast.makeText(this, "פותחת עמוד קולנוע 🎬", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, NearbyCinemaFreeActivity.class));
        }


        else if (id == R.id.nav_ai) {
            startActivity(new Intent(this, AiActivity.class));
        }

        else if (id == R.id.nav_logout) logout(null);

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
        if (authListener != null) auth.removeAuthStateListener(authListener);
        detachUserDocListener();
    }

    // ----- Firestore -----
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
        navView.getMenu().findItem(R.id.nav_login).setVisible(true);
        navView.getMenu().findItem(R.id.nav_register).setVisible(true);
        navView.getMenu().findItem(R.id.nav_user_page).setVisible(false);
        navView.getMenu().findItem(R.id.nav_logout).setVisible(false);
    }

    private void showLoggedInUI() {
        navView.getMenu().findItem(R.id.nav_login).setVisible(false);
        navView.getMenu().findItem(R.id.nav_register).setVisible(false);
        navView.getMenu().findItem(R.id.nav_user_page).setVisible(true);
        navView.getMenu().findItem(R.id.nav_logout).setVisible(true);
    }

    // ----- עזר -----
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

    protected void  registerNetworkBrodcastReciver (){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            registerReceiver(broadcastReceiver,new IntentFilter((ConnectivityManager.CONNECTIVITY_ACTION)));
        }
    }
}

