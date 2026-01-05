package com.example.finalproject;

import static com.example.finalproject.FBRef.refAuth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;  // ✅ CHANGED (נשאר, כי יורשים מ-AppCompatActivity)

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

// ✅ CHANGED: במקום BaseActivity
public class loginPage extends AppCompatActivity {  // ✅ CHANGED

    private EditText eTEmail, eTPass;
    private TextView tVMsg;
    private CheckBox cbStayConnect;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ CHANGED: נשאר setContentView רגיל (כי זה דף בלי תפריט)
        setContentView(R.layout.activity_login_page);  // ✅ CHANGED (אם היה setPageContent - לא צריך)

        FirebaseApp.initializeApp(this);
        auth = refAuth;

        eTEmail = findViewById(R.id.eTEmail);
        eTPass = findViewById(R.id.eTPass);
        tVMsg = findViewById(R.id.tVMsg);
        cbStayConnect = findViewById(R.id.cbStayConnect);
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
                    startActivity(new Intent(this, MainActivity.class));
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
