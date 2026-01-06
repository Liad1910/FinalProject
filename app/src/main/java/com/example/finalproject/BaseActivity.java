package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ה-Layout הבסיסי עם Drawer + Toolbar
        super.setContentView(R.layout.activity_base);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer toggle (המבורגר)
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // עדכון תפריט לפי מצב התחברות
        updateMenuByAuthState();
    }

    /**
     * טוען Layout של דף ספציפי לתוך ה-FrameLayout
     */
    protected void setPageContent(@LayoutRes int layoutResId) {
        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        contentFrame.removeAllViews();
        LayoutInflater.from(this).inflate(layoutResId, contentFrame, true);
    }

    // =====================================================
    // הצגה / הסתרה של פריטי תפריט לפי התחברות
    // =====================================================
    protected void updateMenuByAuthState() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Menu menu = navigationView.getMenu();

        boolean isLoggedIn = (user != null);

        // התחברות / הרשמה
        menu.findItem(R.id.nav_login).setVisible(!isLoggedIn);
        menu.findItem(R.id.nav_register).setVisible(!isLoggedIn);

        // פרופיל / יצירה / התנתקות
        menu.findItem(R.id.nav_user_page).setVisible(isLoggedIn);
        menu.findItem(R.id.nav_create_title).setVisible(isLoggedIn);
        menu.findItem(R.id.nav_logout).setVisible(isLoggedIn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // חשוב: מתעדכן כשחוזרים ממסך התחברות / התנתקות
        updateMenuByAuthState();
    }

    // =====================================================
    // טיפול בלחיצות בתפריט
    // =====================================================
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

        // User
        else if (id == R.id.nav_login) {
            startActivity(new Intent(this, loginPage.class));
        } else if (id == R.id.nav_register) {
            startActivity(new Intent(this, registerPage.class));
        } else if (id == R.id.nav_user_page) {
            startActivity(new Intent(this, activity_user_page.class));
        } else if (id == R.id.nav_create_title) {
            startActivity(new Intent(this, CreateTitleActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // =====================================================
    // פעולות עזר
    // =====================================================
    protected void openMovies(String genre) {
        Intent intent = new Intent(this, MoviesCategoryActivity.class);
        intent.putExtra("genre", genre);
        startActivity(intent);
    }

    protected void openSeries(String genre) {
        Intent intent = new Intent(this, SeriesCategoryActivity.class);
        intent.putExtra("genre", genre);
        startActivity(intent);
    }

    protected void logout() {
        FirebaseAuth.getInstance().signOut();
        updateMenuByAuthState();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
