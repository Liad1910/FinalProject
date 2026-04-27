package com.example.finalproject;

import static com.example.finalproject.FBRef.refAuth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class loginPage extends AppCompatActivity {

    private EditText eTEmail, eTPass;
    private TextView tVMsg;
    private CheckBox cbStayConnect;
    private FirebaseAuth auth;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        FirebaseApp.initializeApp(this);
        auth = refAuth;

        eTEmail = findViewById(R.id.eTEmail);
        eTPass = findViewById(R.id.eTPass);
        tVMsg = findViewById(R.id.tVMsg);

        bottomNav = findViewById(R.id.bottomNav);

        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav.getMenu().setGroupCheckable(0, false, true);
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
                    case 0: return; // כבר פה
                    case 1: startActivity(new Intent(this, registerPage.class)); break;
                    case 2: startActivity(new Intent(this, NearbyCinemaFreeActivity.class)); break;
                    case 3: startActivity(new Intent(this, AiActivity.class)); break;
                    case 4: startActivity(new Intent(this, CreateTitleActivity.class)); break;
                }
            });
        } else {
            String[] options = {"פרופיל", "הקולנוע הקרוב", "צ'אט", "צור סרט / סדרה", "התנתקות"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: startActivity(new Intent(this, activity_user_page.class)); break;
                    case 1: startActivity(new Intent(this, NearbyCinemaFreeActivity.class)); break;
                    case 2: startActivity(new Intent(this, AiActivity.class)); break;
                    case 3: startActivity(new Intent(this, CreateTitleActivity.class)); break;
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

    public void loginUser(android.view.View view) {
        String email = eTEmail.getText().toString().trim();
        String pass = eTPass.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            tVMsg.setText("Please fill all fields");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in user...");
        pd.setCancelable(false);
        pd.show();

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(this, (AuthResult result) -> {
                    if (pd.isShowing()) pd.dismiss();

                    getSharedPreferences("MyPrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("stayConnect", cbStayConnect.isChecked())
                            .apply();

                    tVMsg.setText("User logged in successfully");

                    Intent i = new Intent(this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(this, exp -> {
                    if (pd.isShowing()) pd.dismiss();
                    if (exp instanceof FirebaseAuthInvalidUserException)
                        tVMsg.setText("Invalid email address.");
                    else if (exp instanceof FirebaseAuthInvalidCredentialsException)
                        tVMsg.setText("Wrong password.");
                    else
                        tVMsg.setText("An error occurred. Please try again.");
                });
    }
}