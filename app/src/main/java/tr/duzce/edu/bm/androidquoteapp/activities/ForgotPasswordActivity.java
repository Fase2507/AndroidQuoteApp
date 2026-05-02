package tr.duzce.edu.bm.androidquoteapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.mail.MessagingException;
import tr.duzce.edu.bm.androidquoteapp.AppDatabase;
import tr.duzce.edu.bm.androidquoteapp.R;
import tr.duzce.edu.bm.androidquoteapp.models.User;
import tr.duzce.edu.bm.androidquoteapp.services.EmailService;
import tr.duzce.edu.bm.androidquoteapp.utils.PasswordUtils;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputLayout emailLayout, codeLayout, newPasswordLayout;
    private TextInputEditText emailEditText, codeEditText, newPasswordEditText;
    private LinearLayout resetStepLayout;
    private Button actionBtn, backBtn;
    
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isCodeSent = false;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db = AppDatabase.getInstance(this);
        initComponents();
        
        String prefilledEmail = getIntent().getStringExtra("email");
        if (prefilledEmail != null) emailEditText.setText(prefilledEmail);

        actionBtn.setOnClickListener(v -> {
            if (!isCodeSent) {
                sendVerificationCode();
            } else {
                resetPassword();
            }
        });

        backBtn.setOnClickListener(v -> finish());
    }

    private void initComponents() {
        emailLayout = findViewById(R.id.emailLayout);
        codeLayout = findViewById(R.id.codeLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        emailEditText = findViewById(R.id.email);
        codeEditText = findViewById(R.id.verificationCode);
        newPasswordEditText = findViewById(R.id.newPassword);
        resetStepLayout = findViewById(R.id.resetStepLayout);
        actionBtn = findViewById(R.id.actionBtn);
        backBtn = findViewById(R.id.backBtn);
    }

    private void sendVerificationCode() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return;
        }

        executorService.execute(() -> {
            currentUser = db.userDao().getUserByEmail(email);
            if (currentUser == null) {
                runOnUiThread(() -> emailLayout.setError("User not found"));
            } else {
                String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                currentUser.setVerificationToken(code);
                currentUser.setTokenExpiryTimestamp(System.currentTimeMillis() + (10 * 60 * 1000)); // 10 mins
                db.userDao().updateUser(currentUser);

                try {
                    EmailService.sendVerificationEmail(email, code);
                    runOnUiThread(() -> {
                        isCodeSent = true;
                        emailLayout.setEnabled(false);
                        resetStepLayout.setVisibility(View.VISIBLE);
                        actionBtn.setText("Reset Password");
                        Toast.makeText(this, "Verification code sent to email", Toast.LENGTH_LONG).show();
                    });
                } catch (MessagingException e) {
                    runOnUiThread(() -> Toast.makeText(this, "Error sending email", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void resetPassword() {
        String code = codeEditText.getText().toString().trim().toUpperCase();
        String newPass = newPasswordEditText.getText().toString().trim();

        if (code.isEmpty() || newPass.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 4) {
            newPasswordLayout.setError("Password too short");
            return;
        }

        if (currentUser != null && code.equals(currentUser.getVerificationToken())) {
            if (System.currentTimeMillis() > currentUser.getTokenExpiryTimestamp()) {
                Toast.makeText(this, "Code expired. Try again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            executorService.execute(() -> {
                String hashedPass = PasswordUtils.hashPassword(newPass);
                db.userDao().updatePassword(currentUser.getEmail(), hashedPass);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                    finish();
                });
            });
        } else {
            codeLayout.setError("Invalid code");
        }
    }
}
