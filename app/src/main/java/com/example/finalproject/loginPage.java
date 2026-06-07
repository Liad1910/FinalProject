package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class loginPage extends AppCompatActivity {

    private EditText eTEmail, eTPass;
    private TextView tVMsg;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        eTEmail = findViewById(R.id.eTEmail);
        eTPass = findViewById(R.id.eTPass);
        tVMsg = findViewById(R.id.tVMsg);
        bottomNav = findViewById(R.id.bottomNav);

        setupBottomNav();
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
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();

                    if (user == null) {
                        pd.dismiss();
                        tVMsg.setText("Login failed");
                        return;
                    }

                    String uid = user.getUid();

                    // בודק שיש מסמך משתמש ב-Firestore
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("email", email);
                                    data.put("uid", uid);

                                    db.collection("users").document(uid).set(data)
                                            .addOnSuccessListener(a -> openMain(pd))
                                            .addOnFailureListener(e -> {
                                                pd.dismiss();
                                                tVMsg.setText("Firestore Error: " + e.getMessage());
                                            });
                                } else {
                                    openMain(pd);
                                }
                            })
                            .addOnFailureListener(e -> {
                                pd.dismiss();
                                tVMsg.setText("Firestore Error: " + e.getMessage());
                            });
                })
                .addOnFailureListener(exp -> {
                    pd.dismiss();

                    if (exp instanceof FirebaseAuthInvalidUserException) {
                        tVMsg.setText("Invalid email address.");
                    } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                        tVMsg.setText("Wrong password.");
                    } else {
                        tVMsg.setText("Error: " + exp.getMessage());
                    }
                });
    }

    private void openMain(ProgressDialog pd) {
        if (pd.isShowing()) {
            pd.dismiss();
        }

        tVMsg.setText("User logged in successfully");

        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void setupBottomNav() {
        bottomNav.getMenu().setGroupCheckable(0, false, true);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bnav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;

            } else if (id == R.id.bnav_movies) {
                startActivity(new Intent(this, MoviesCategoryActivity.class));
                finish();
                return true;

            } else if (id == R.id.bnav_series) {
                startActivity(new Intent(this, SeriesCategoryActivity.class));
                finish();
                return true;

            } else if (id == R.id.bnav_more) {
                showMoreDialog();
                bottomNav.getMenu().findItem(R.id.bnav_more).setChecked(false);
                return true;
            }

            return false;
        });
    }

    private void showMoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("עוד");

        String[] options = {"התחברות", "הרשמה", "הקולנוע הקרוב", "צ'אט", "צור סרט / סדרה"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    return;
                case 1:
                    startActivity(new Intent(this, registerPage.class));
                    break;
                case 2:
                    startActivity(new Intent(this, NearbyCinemaFreeActivity.class));
                    break;
                case 3:
                    startActivity(new Intent(this, AiActivity.class));
                    break;
                case 4:
                    startActivity(new Intent(this, CreateTitleActivity.class));
                    break;
            }
        });

        builder.setNegativeButton("סגור", null);
        builder.show();
    }
}

