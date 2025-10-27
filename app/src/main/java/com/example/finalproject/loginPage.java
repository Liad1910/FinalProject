package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class loginPage extends AppCompatActivity {

    private EditText eTEmail, eTPass;
    private TextView tVMsg;
    private CheckBox cbStayConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);

        FirebaseApp.initializeApp(this);

        eTEmail = findViewById(R.id.eTEmail);
        eTPass  = findViewById(R.id.eTPass);
        tVMsg   = findViewById(R.id.tVMsg);
        cbStayConnect = findViewById(R.id.cbStayConnect);
    }

    // ⬅️ קפיצה אוטומטית ל-Main אם המשתמש מחובר וסימן "השאר אותי מחובר"
    @Override
    protected void onStart() {
        super.onStart();
        boolean isChecked = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .getBoolean("stayConnect", false);

        FirebaseUser current = FBRef.refAuth.getCurrentUser();
        if (current != null && isChecked) {
            startActivity(new Intent(loginPage.this, MainActivity.class));
            finish();
        }
    }

    // onClick מה-XML
    public void loginUser(android.view.View view) {
        String email = eTEmail.getText().toString().trim();
        String pass  = eTPass.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            tVMsg.setText("Please fill all fields");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in user...");
        pd.setCancelable(false);
        pd.show();

        FBRef.refAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(this, (AuthResult result) -> {
                    if (pd.isShowing()) pd.dismiss();

                    // נשמור את הבחירה של המשתמש
                    getSharedPreferences("MyPrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("stayConnect", cbStayConnect.isChecked())
                            .apply();

                    tVMsg.setText("User logged in successfully");
                    startActivity(new Intent(loginPage.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(this, exp -> {
                    if (pd.isShowing()) pd.dismiss();

                    if (exp instanceof FirebaseAuthInvalidUserException) {
                        tVMsg.setText("Invalid email address.");
                    } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                        tVMsg.setText("General authentication failure.");
                    } else {
                        tVMsg.setText("An error occurred. Please try again later.");
                    }
                });
    }
}
