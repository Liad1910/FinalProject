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

        // Base layout עם Drawer + Toolbar
        super.setContentView(R.layout.activity_base);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hamburger toggle
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

        updateMenuByAuthState();
    }

    /** טוען Layout של דף ספציפי לתוך ה-FrameLayout */
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
        MenuItem login = menu.findItem(R.id.nav_login);
        MenuItem register = menu.findItem(R.id.nav_register);
        if (login != null) login.setVisible(!isLoggedIn);
        if (register != null) register.setVisible(!isLoggedIn);

        // פרופיל / יצירה / התנתקות
        MenuItem profile = menu.findItem(R.id.nav_user_page);
        MenuItem create = menu.findItem(R.id.nav_create_title);
        MenuItem logout = menu.findItem(R.id.nav_logout);

        if (profile != null) profile.setVisible(isLoggedIn);
        if (create != null) create.setVisible(isLoggedIn);
        if (logout != null) logout.setVisible(isLoggedIn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMenuByAuthState();
    }

    // =====================================================
    // טיפול בלחיצות בתפריט
    // =====================================================
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // ✅ Movies / Series פשוטים
        if (id == R.id.nav_movies) {
            startActivity(new Intent(this, MoviesCategoryActivity.class));
        } else if (id == R.id.nav_series) {
            startActivity(new Intent(this, SeriesCategoryActivity.class));
        }

        // ✅ User
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
