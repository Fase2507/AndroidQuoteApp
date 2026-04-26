package tr.duzce.edu.bm.androidquoteapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tr.duzce.edu.bm.androidquoteapp.AppDatabase;
import tr.duzce.edu.bm.androidquoteapp.R;
import tr.duzce.edu.bm.androidquoteapp.models.FavoriteQuotes;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvEmail, tvFavoritesCount;
    private Button btnBack, btnLogout;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        db = AppDatabase.getInstance(this);
        initComponents();
        loadUserInfo();
        setupClickListeners();
    }

    private void initComponents() {
        tvEmail = findViewById(R.id.tvEmail);
        tvFavoritesCount = findViewById(R.id.tvFavoritesCount);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadUserInfo() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userEmail = pref.getString("current_user_email", "Guest");
        tvEmail.setText(userEmail);

        if (userEmail.equals("Guest")) {
            tvFavoritesCount.setText(getString(R.string.total_favorites, 0));
        } else {
            executorService.execute(() -> {
                List<FavoriteQuotes> favorites = db.quoteDao().getAllFavorites();
                runOnUiThread(() -> {
                    tvFavoritesCount.setText(getString(R.string.total_favorites, favorites.size()));
                });
            });
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        pref.edit().clear().apply();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
