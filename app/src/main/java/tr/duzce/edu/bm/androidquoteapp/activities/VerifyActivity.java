package tr.duzce.edu.bm.androidquoteapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tr.duzce.edu.bm.androidquoteapp.AppDatabase;
import tr.duzce.edu.bm.androidquoteapp.R;
import tr.duzce.edu.bm.androidquoteapp.models.User;

public class VerifyActivity extends AppCompatActivity {
    private TextInputEditText tokenEditText;
    private Button verifyBtn;
    private AppDatabase db;
    private String email;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify);

        db = AppDatabase.getInstance(this);
        email = getIntent().getStringExtra("email");

        tokenEditText = findViewById(R.id.token);
        verifyBtn = findViewById(R.id.verifyBtn);

        verifyBtn.setOnClickListener(v -> verify());
    }

    private void verify() {
        String enteredToken = tokenEditText.getText().toString().trim().toUpperCase();

        if (enteredToken.isEmpty()) {
            Toast.makeText(this, "Please enter the code", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            User user = db.userDao().getUserByEmail(email);
            if (user != null && enteredToken.equals(user.getVerificationToken())) {
                
                // Security: Check if token has expired
                if (System.currentTimeMillis() > user.getTokenExpiryTimestamp()) {
                    runOnUiThread(() -> Toast.makeText(this, "Verification code has expired. Please register again.", Toast.LENGTH_LONG).show());
                    return;
                }

                user.setValidated(true);
                user.setVerificationToken(null); // Clear token after use
                db.userDao().updateUser(user);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Account Verified! You can now login.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(VerifyActivity.this, LoginActivity.class));
                    finish();
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Invalid Verification Code", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
