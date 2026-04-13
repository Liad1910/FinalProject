package com.example.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // איפוס הדגל בכל פתיחה חדשה של האפליקציה
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("notifications_scheduled", false)
                .apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}