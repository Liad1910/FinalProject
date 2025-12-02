package com.example.finalproject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.FirebaseApp;

import java.util.HashMap;
import java.util.Map;

public class registerPage extends AppCompatActivity {

    EditText eTEmail, eTPass, eTUsername, eTBirthYear, eTMovie, eTSeries;
    Spinner spGenre;
    TextView tVMsg;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_page);

        FirebaseApp.initializeApp(this);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        eTEmail = findViewById(R.id.eTEmail);
        eTPass = findViewById(R.id.eTPass);
        eTUsername = findViewById(R.id.eTUsername);
        eTBirthYear = findViewById(R.id.eTBirthYear);
        spGenre = findViewById(R.id.spGenre);
        tVMsg = findViewById(R.id.tVMsg);

        // Spinner - 6 ז'אנרים
        String[] genres = {
                "Choose favorite genre",
                "Action",
                "Comedy",
                "Drama",
                "Horror",
                "Romance",
                "Sci-Fi"
        };

        ArrayAdapter<String> genreAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genres);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGenre.setAdapter(genreAdapter);
    }

    public void createUser(View view) {

        String email = eTEmail.getText().toString().trim();
        String pass = eTPass.getText().toString().trim();
        String username = eTUsername.getText().toString().trim();
        String birthYear = eTBirthYear.getText().toString().trim();

        String genre = spGenre.getSelectedItem().toString();

        // בדיקות שדות
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) ||
                TextUtils.isEmpty(username) || TextUtils.isEmpty(birthYear) )
             {
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
                .addOnSuccessListener((AuthResult result) -> {

                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        pd.dismiss();
                        tVMsg.setText("Unknown error (user is null)");
                        return;
                    }

                    String uid = user.getUid();
                    tVMsg.setText("Saving profile...");

                    Map<String, Object> map = new HashMap<>();
                    map.put("email", email);
                    map.put("username", username);
                    map.put("birthYear", birthYear);
                    map.put("favoriteGenre", genre);

                    db.collection("users").document(uid)
                            .set(map)
                            .addOnSuccessListener(aVoid -> {
                                pd.dismiss();
                                tVMsg.setText("User created!");

                                // מעבר למיין אחרי שהפרופיל נשמר
                                Intent intent = new Intent(registerPage.this, MainActivity.class);
                                // לנקות את הסטאק כדי שלא יהיה אפשר BACK להרשמה
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_NEW_TASK);
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
                    if (e instanceof FirebaseAuthWeakPasswordException)
                        tVMsg.setText("Weak password");
                    else if (e instanceof FirebaseAuthUserCollisionException)
                        tVMsg.setText("User already exists");
                    else if (e instanceof FirebaseAuthInvalidCredentialsException)
                        tVMsg.setText("Invalid email");
                    else
                        tVMsg.setText("Error: " + e.getMessage());
                });
    }
}
