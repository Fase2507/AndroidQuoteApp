package tr.duzce.edu.bm.androidquoteapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tr.duzce.edu.bm.androidquoteapp.AppDatabase;
import tr.duzce.edu.bm.androidquoteapp.R;
import tr.duzce.edu.bm.androidquoteapp.models.User;
import tr.duzce.edu.bm.androidquoteapp.utils.PasswordUtils;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginBtn, registerBtn, guestBtn, forgotPasswordBtn;
    private CheckBox rememberMeCheckBox;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);
        initComponents();
        checkRememberMe();
        setupClickListeners();
    }

    private void initComponents() {
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        guestBtn = findViewById(R.id.guestBtn);
        forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);
        rememberMeCheckBox = findViewById(R.id.checkBox);

        int redColor = Color.RED;
        emailLayout.setErrorTextColor(ColorStateList.valueOf(redColor));
        passwordLayout.setErrorTextColor(ColorStateList.valueOf(redColor));
        emailLayout.setErrorIconDrawable(android.R.drawable.stat_notify_error);
        passwordLayout.setErrorIconDrawable(android.R.drawable.stat_notify_error);
    }

    private void setupClickListeners() {
        loginBtn.setOnClickListener(v -> login());
        registerBtn.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
        
        guestBtn.setOnClickListener(v -> {
            executorService.execute(() -> {
                User guestUser = db.userDao().getUserByEmail("Guest");
                if (guestUser == null) {
                    db.userDao().registerUser(new User("Guest", "GUEST_ACCOUNT", true));
                }
                
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.guest_warning, Toast.LENGTH_LONG).show();
                    saveCurrentUser("Guest");
                    navigateToMain();
                });
            });
        });

        forgotPasswordBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                emailLayout.setError("Enter your email first");
            } else {
                handleForgotPassword(email);
            }
        });
    }

    private void handleForgotPassword(String email) {
        executorService.execute(() -> {
            User user = db.userDao().getUserByEmail(email);
            if (user == null) {
                runOnUiThread(() -> emailLayout.setError("User not found"));
            } else {
                // Navigate to ForgotPasswordActivity (to be created)
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }

    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        resetErrors();

        boolean hasError = false;
        if (email.isEmpty()) {
            emailLayout.setError(getString(R.string.fill_all_fields));
            hasError = true;
        }

        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.fill_all_fields));
            hasError = true;
        }

        if (hasError) return;

        executorService.execute(() -> {
            User user = db.userDao().getUserByEmail(email);
            
            boolean isValid = user != null && PasswordUtils.verifyPassword(password, user.getPassword());

            runOnUiThread(() -> {
                if (isValid) {
                    if (!user.isValidated()) {
                        Toast.makeText(this, R.string.verify_email_first, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (rememberMeCheckBox.isChecked()) {
                        saveLoginStatus(email, password);
                    }
                    saveCurrentUser(email);
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    passwordLayout.setError(getString(R.string.invalid_credentials));
                    if (user != null && !user.getPassword().contains(":")) {
                        passwordLayout.setError("Old account detected. Use Forgot Password to reset.");
                    }
                }
            });
        });
    }

    private void resetErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }

    private void saveLoginStatus(String email, String password) {
        SharedPreferences pref = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void saveCurrentUser(String email) {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        pref.edit().putString("current_user_email", email).apply();
    }

    private void checkRememberMe() {
        SharedPreferences pref = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        if (pref.getBoolean("remember", false)) {
            emailEditText.setText(pref.getString("email", ""));
            passwordEditText.setText(pref.getString("password", ""));
            rememberMeCheckBox.setChecked(true);
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
