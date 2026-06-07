package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class registerPage extends AppCompatActivity {

    private EditText eTEmail, eTPass, eTUsername, eTBirthYear;
    private Spinner spGenre;
    private TextView tVMsg;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        eTEmail = findViewById(R.id.eTEmail);
        eTPass = findViewById(R.id.eTPass);
        eTUsername = findViewById(R.id.eTUsername);
        eTBirthYear = findViewById(R.id.eTBirthYear);
        spGenre = findViewById(R.id.spGenre);
        tVMsg = findViewById(R.id.tVMsg);
        bottomNav = findViewById(R.id.bottomNav);

        String[] genres = {
                "Choose favorite genre",
                "Action", "Comedy", "Drama", "Horror", "Romance", "Sci-Fi"
        };

        ArrayAdapter<String> genreAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genres);

        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGenre.setAdapter(genreAdapter);

        setupBottomNav();
    }

    public void createUser(View view) {
        String email = eTEmail.getText().toString().trim();
        String pass = eTPass.getText().toString().trim();
        String username = eTUsername.getText().toString().trim();
        String birthYear = eTBirthYear.getText().toString().trim();
        String genre = spGenre.getSelectedItem().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) ||
                TextUtils.isEmpty(username) || TextUtils.isEmpty(birthYear)) {
            tVMsg.setText("Please fill all fields");
            return;
        }

        if (genre.equals("Choose favorite genre")) {
            tVMsg.setText("Please choose favorite genre");
            return;
        }

        if (pass.length() < 6) {
            tVMsg.setText("Password must be at least 6 characters");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Creating user...");
        pd.setCancelable(false);
        pd.show();

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();

                    if (user == null) {
                        pd.dismiss();
                        tVMsg.setText("Unknown error");
                        return;
                    }

                    String uid = user.getUid();

                    UserProfileChangeRequest profileUpdates =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                    user.updateProfile(profileUpdates);

                    Map<String, Object> map = new HashMap<>();
                    map.put("uid", uid);
                    map.put("email", email);
                    map.put("username", username);
                    map.put("birthYear", birthYear);
                    map.put("favoriteGenre", genre);
                    map.put("createdAt", System.currentTimeMillis());

                    db.collection("users").document(uid).set(map)
                            .addOnSuccessListener(aVoid -> {
                                pd.dismiss();
                                tVMsg.setText("User created!");

                                Intent intent = new Intent(this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                pd.dismiss();
                                tVMsg.setText("Firestore Error: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();

                    if (e instanceof FirebaseAuthWeakPasswordException) {
                        tVMsg.setText("Weak password");
                    } else if (e instanceof FirebaseAuthUserCollisionException) {
                        tVMsg.setText("User already exists");
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        tVMsg.setText("Invalid email");
                    } else {
                        tVMsg.setText("Error: " + e.getMessage());
                    }
                });
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
                    startActivity(new Intent(this, loginPage.class));
                    break;
                case 1:
                    return;
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

