package com.example.finalproject;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    BroadcastReceiver broadcastReceiver;
    private boolean receiverRegistered = false;

    private TextView tvHelloMain, tvSubtitle, tvCarouselTitle;
    private ImageView ivMainImage;
    private ImageButton btnNext, btnPrev;
    private MaterialButton btnWatchNow;
    private BottomNavigationView bottomNav;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private ListenerRegistration userDocListener;

    private int currentIndex = 0;

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
            "https://www.youtube.com/results?search_query=The+Pianist+trailer",
            "https://www.youtube.com/watch?v=FnCdOQsX5kc"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // אם זה עושה בעיות אפשר לכבות זמנית:
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        // Network receiver (לא לרשום פעמיים!)
        try {
            broadcastReceiver = new Network();
            registerNetworkBrodcastReciver();
        } catch (Exception e) {
            Log.e(TAG, "Receiver register failed", e);
        }

        // findViewById
        tvHelloMain = findViewById(R.id.tvHelloMain);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvCarouselTitle = findViewById(R.id.tvCarouselTitle);

        ivMainImage = findViewById(R.id.ivMainImage);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnWatchNow = findViewById(R.id.btnWatchNow);

        bottomNav = findViewById(R.id.bottomNav);

        // בדיקת null כדי למנוע קריסה ולהבין מה חסר
        if (tvHelloMain == null || tvCarouselTitle == null || ivMainImage == null || bottomNav == null) {
            Toast.makeText(this, "שגיאת UI: רכיב חסר ב-activity_main.xml", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Missing view: " +
                    "tvHelloMain=" + (tvHelloMain != null) +
                    ", tvCarouselTitle=" + (tvCarouselTitle != null) +
                    ", ivMainImage=" + (ivMainImage != null) +
                    ", bottomNav=" + (bottomNav != null));
            return; // לא להמשיך כדי לא לקרוס
        }

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Carousel initial
        updateCarouselUI();

        // Clicks
        ivMainImage.setOnClickListener(v -> openCurrentCarouselMovie());
        if (btnWatchNow != null) btnWatchNow.setOnClickListener(v -> openCurrentCarouselMovie());

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                currentIndex = (currentIndex + 1) % images.length;
                updateCarouselUI();
            });
        }

        if (btnPrev != null) {
            btnPrev.setOnClickListener(v -> {
                currentIndex = (currentIndex - 1 + images.length) % images.length;
                updateCarouselUI();
            });
        }

        // Bottom nav
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            try {
                if (id == R.id.bnav_movies) {
                    startActivity(new Intent(this, MoviesCategoryActivity.class));
                    return true;
                } else if (id == R.id.bnav_series) {
                    startActivity(new Intent(this, SeriesCategoryActivity.class));
                    return true;
                } else if (id == R.id.bnav_register) {
                    startActivity(new Intent(this, registerPage.class));
                    return true;
                } else if (id == R.id.bnav_login) {
                    startActivity(new Intent(this, loginPage.class));
                    return true;
                } else if (id == R.id.bnav_profile) {
                    startActivity(new Intent(this, activity_user_page.class));
                    return true;
                }
            } catch (Exception e) {
                Toast.makeText(this, "שגיאת ניווט: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Navigation error", e);
            }
            return false;
        });

        // Auth listener
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

    private void updateCarouselUI() {
        ivMainImage.setImageResource(images[currentIndex]);
        tvCarouselTitle.setText(carouselMovieTitles[currentIndex]);
    }

    private void openCurrentCarouselMovie() {
        Intent i = new Intent(this, MovieContentActivity.class);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_ID, carouselMovieIds[currentIndex]);
        i.putExtra(MovieContentActivity.EXTRA_MOVIE_TITLE, carouselMovieTitles[currentIndex]);
        i.putExtra(MovieContentActivity.EXTRA_TRAILER_URL, carouselTrailerUrls[currentIndex]);
        i.putExtra(MovieContentActivity.EXTRA_POSTER_RES_ID, images[currentIndex]);
        startActivity(i);
    }

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

            if (tvHelloMain != null) {
                tvHelloMain.setText(display != null ? "שלום " + display : "שלום!");
            }
        });
    }

    private void detachUserDocListener() {
        if (userDocListener != null) {
            userDocListener.remove();
            userDocListener = null;
        }
    }

    private void showAnonymousUI() {
        if (tvHelloMain != null) tvHelloMain.setText("שלום אנונימי");
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.bnav_login).setVisible(true);
            bottomNav.getMenu().findItem(R.id.bnav_register).setVisible(true);
            bottomNav.getMenu().findItem(R.id.bnav_profile).setVisible(false);
        }
    }

    private void showLoggedInUI() {
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.bnav_login).setVisible(false);
            bottomNav.getMenu().findItem(R.id.bnav_register).setVisible(false);
            bottomNav.getMenu().findItem(R.id.bnav_profile).setVisible(true);
        }
    }

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

    protected void registerNetworkBrodcastReciver() {
        if (broadcastReceiver == null || receiverRegistered) return;

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        try {
            registerReceiver(broadcastReceiver, filter);
            receiverRegistered = true;
        } catch (Exception e) {
            Log.e(TAG, "registerReceiver failed", e);
        }
    }

    protected void unregisteredNetwork() {
        if (!receiverRegistered) return;
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception ignored) {
        } finally {
            receiverRegistered = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth != null && authListener != null) auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (auth != null && authListener != null) auth.removeAuthStateListener(authListener);
        detachUserDocListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisteredNetwork();
    }
}
