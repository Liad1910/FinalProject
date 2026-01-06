package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        // Toolbar בלי כותרת, כמו MAIN
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // אותו אייקון hamburger כמו בעמוד הראשי
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void setPageContent(@LayoutRes int layoutResId) {
        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        LayoutInflater.from(this).inflate(layoutResId, contentFrame, true);
    }

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
        else if (id == R.id.nav_create_title) startActivity(new Intent(this, CreateTitleActivity.class));
        else if (id == R.id.nav_logout) logout();

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

    private void logout() {
        // אם יש לך מימוש אחר – תשאירי אותו
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }
}
