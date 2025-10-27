package com.example.finalproject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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

    EditText eTEmail, eTPass, eTUsername, eTBirthYear, eTMovie, eTSeries, eTGenre;
    TextView tVMsg;
    FirebaseFirestore db;
    private static final String TAG = "RegisterPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_page);

        FirebaseApp.initializeApp(this); // חשוב!

        db = FirebaseFirestore.getInstance();

        eTEmail = findViewById(R.id.eTEmail);
        eTPass = findViewById(R.id.eTPass);
        eTUsername = findViewById(R.id.eTUsername);
        eTBirthYear = findViewById(R.id.eTBirthYear);
        eTMovie = findViewById(R.id.eTMovie);
        eTSeries = findViewById(R.id.eTSeries);
        eTGenre = findViewById(R.id.eTGenre);
        tVMsg = findViewById(R.id.tVMsg);
    }

    public void createUser(View view) {

        String email = eTEmail.getText().toString().trim();
        String pass = eTPass.getText().toString().trim();
        String username = eTUsername.getText().toString().trim();
        String birthYear = eTBirthYear.getText().toString().trim();
        String movie = eTMovie.getText().toString().trim();
        String series = eTSeries.getText().toString().trim();
        String genre = eTGenre.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) ||
                TextUtils.isEmpty(username) || TextUtils.isEmpty(birthYear) ||
                TextUtils.isEmpty(movie) || TextUtils.isEmpty(series) ||
                TextUtils.isEmpty(genre)) {
            tVMsg.setText("Please fill all fields");
            return;
        }

        if (pass.length() < 6) {
            tVMsg.setText("Password must be at least 6 characters");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Creating user...");
        pd.show();

        com.google.firebase.auth.FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    pd.dismiss();

                    FirebaseUser user = result.getUser();
                    if (user == null) return;

                    String uid = user.getUid();
                    tVMsg.setText("User created!\nSaving profile...");

                    Map<String, Object> map = new HashMap<>();
                    map.put("email", email);
                    map.put("username", username);
                    map.put("birthYear", birthYear);
                    map.put("favoriteMovie", movie);
                    map.put("favoriteSeries", series);
                    map.put("favoriteGenre", genre);

                    db.collection("users").document(uid)
                            .set(map)
                            .addOnSuccessListener(aVoid ->
                                    tVMsg.setText("✅ Registered!\nUid: " + uid))
                            .addOnFailureListener(e ->
                                    tVMsg.setText("Firestore Error: " + e.getMessage()));
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
