package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView tvHelloMain;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvHelloMain = findViewById(R.id.tvHelloMain);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // בדיקת "השאר אותי מחובר" שנשמרה בלוגין
        boolean stay = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .getBoolean("stayConnect", false);

        FirebaseUser current = FBRef.refAuth.getCurrentUser();

        if (!stay) {
            // אם לא רוצים להישאר מחוברים – ננתק (גם אם Firebase שומר סשן)
            if (current != null) {
                FBRef.refAuth.signOut();
            }
            tvHelloMain.setText("שלום אנונימי");
            return; // לא שולפים מה-DB
        }

        // stay=true → נשלוף שם משתמש מה-DB
        showGreeting();
    }

    private void showGreeting() {
        FirebaseUser user = FBRef.refAuth.getCurrentUser();

        if (user == null) {
            tvHelloMain.setText("שלום אנונימי");
            return;
        }

        String uid = user.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String username = doc.getString("username");
                        if (username != null && !username.isEmpty()) {
                            tvHelloMain.setText("שלום " + username);
                        } else {
                            tvHelloMain.setText("שלום אנונימי");
                        }
                    } else {
                        tvHelloMain.setText("שלום אנונימי");
                    }
                })
                .addOnFailureListener(e -> tvHelloMain.setText("שלום אנונימי"));
    }

    public void goToRegister(View view){
        startActivity(new Intent(this, registerPage.class));
    }

    public void goToLogin(View view){
        // שימי לב: LoginPage עם אות גדולה
        startActivity(new Intent(this, loginPage.class));
    }

    public void logout(View view) {
        // התנתקות וחזרה ללוגין
        FBRef.refAuth.signOut();
        startActivity(new Intent(MainActivity.this, MainActivity.class));
        finish();
    }
}
