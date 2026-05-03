package tr.duzce.edu.bm.androidquoteapp.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout emailLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerBtn, loginBtn;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);
        initComponents();
        setupClickListeners();
    }

    private void initComponents() {
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        registerBtn = findViewById(R.id.registerBtn);
        loginBtn = findViewById(R.id.loginBtn);

        int redColor = Color.RED;
        emailLayout.setErrorTextColor(ColorStateList.valueOf(redColor));
        passwordLayout.setErrorTextColor(ColorStateList.valueOf(redColor));
        confirmPasswordLayout.setErrorTextColor(ColorStateList.valueOf(redColor));
    }

    private void setupClickListeners() {
        registerBtn.setOnClickListener(v -> register());
        loginBtn.setOnClickListener(v -> finish());
    }

    private void register() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        resetErrors();


        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.passwords_dont_match));
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            emailLayout.setError(getString(R.string.invalid_email));
            return;
        }

        if (password.length() < 4) {
            passwordLayout.setError(getString(R.string.invalid_password));
            return;
        }

        executorService.execute(() -> {
            User existingUser = db.userDao().getUserByEmail(email);
            
            // Eğer kullanıcı zaten varsa ve DOĞRULANMIŞSA hata ver
            if (existingUser != null && existingUser.isValidated()) {
                runOnUiThread(() -> emailLayout.setError(getString(R.string.user_already_exists)));
                return;
            }

            // Yeni kod ve güvenli şifre hash'i hazırla
            String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            String hashedPassword = PasswordUtils.hashPassword(password);
            long expiryTime = System.currentTimeMillis() + (30 * 60 * 1000); // 30 Dakika

            if (existingUser == null) {
                // Tamamen yeni kayıt
                User newUser = new User(email, hashedPassword, false); 
                newUser.setVerificationToken(verificationCode);
                newUser.setTokenExpiryTimestamp(expiryTime);
                db.userDao().registerUser(newUser);
            } else {
                // Kaydı var ama doğrulanmamış; verileri güncelle (Re-Register / Recovery)
                existingUser.setPassword(hashedPassword);
                existingUser.setVerificationToken(verificationCode);
                existingUser.setTokenExpiryTimestamp(expiryTime);
                db.userDao().updateUser(existingUser);
            }
            
            try {
                EmailService.sendVerificationEmail(email, verificationCode);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Verification code sent to your email!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, VerifyActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                });
            } catch (MessagingException e) {
                runOnUiThread(() -> Toast.makeText(this, "Email service error", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void resetErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }
}
