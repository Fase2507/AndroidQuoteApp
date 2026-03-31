package tr.duzce.edu.bm.androidquoteapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tr.duzce.edu.bm.androidquoteapp.atilla.AppDatabase;
import tr.duzce.edu.bm.androidquoteapp.atilla.FavoriteQuotes;
import tr.duzce.edu.bm.androidquoteapp.fatih.GeminiService;
import tr.duzce.edu.bm.androidquoteapp.fatih.Quote;
import tr.duzce.edu.bm.androidquoteapp.fatih.RetrofitClient;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView textViewQuote;
    private TextView textViewAuthor;
    private TextView textViewCategory;
    private Button btnRefresh;
    private Button btnTranslate;
    private CircularProgressIndicator progressBar;
    private ImageView ivFavorite;

    private final GeminiService geminiService = new GeminiService();
    private Quote currentQuote = null;
    private boolean isTranslated = false;

    // Database components
    private AppDatabase database;
    private ExecutorService executorService;
    private boolean isFavorited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the side bar layout which includes activity_main
        setContentView(R.layout.activity_side_bar);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Sidebar Setup
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Passing 'toolbar' to the toggle constructor makes the hamburger icon appear and function automatically
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize Quote UI elements (from the included activity_main)
        textViewQuote = findViewById(R.id.textViewQuote);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        textViewCategory = findViewById(R.id.textViewCategory);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnTranslate = findViewById(R.id.btnTranslate);
        progressBar = findViewById(R.id.progressBar);
        ivFavorite = findViewById(R.id.ivFavorite);

        // Initialize DB
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        fetchNewQuote();

        btnRefresh.setOnClickListener(v -> fetchNewQuote());
        btnTranslate.setOnClickListener(v -> translateQuote());
        ivFavorite.setOnClickListener(v -> handleFavoriteClick());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_favorites) {
            Toast.makeText(this, "Favorites clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void fetchNewQuote() {
        showLoading(true);
        isTranslated = false;
        btnTranslate.setText("Translate");

        isFavorited = false;
        updateHeartIcon();

        RetrofitClient.getQuoteApi().getRandomQuote().enqueue(new Callback<List<Quote>>() {
            @Override
            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentQuote = response.body().get(0);
                    textViewQuote.setText(currentQuote.getText());
                    textViewAuthor.setText("- " + (currentQuote.getAuthor() != null ? currentQuote.getAuthor() : "Unknown"));
                    
                    geminiService.categorizeQuote(currentQuote.getText(), new GeminiService.Callback() {
                        @Override
                        public void onSuccess(String result) {
                            textViewCategory.setText(result);
                            showLoading(false);
                            checkFavoriteStatus(currentQuote.getText());
                        }

                        @Override
                        public void onError(Exception e) {
                            textViewCategory.setText("General");
                            showLoading(false);
                            checkFavoriteStatus(currentQuote.getText());
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Error fetching quote", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                }
            }

            @Override
            public void onFailure(Call<List<Quote>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }

    private void translateQuote() {
        if (currentQuote == null) return;
        showLoading(true);

        String targetLang = isTranslated ? "English" : "Turkish";
        geminiService.translateQuote(currentQuote.getText(), targetLang, new GeminiService.Callback() {
            @Override
            public void onSuccess(String result) {
                textViewQuote.setText(result);
                isTranslated = !isTranslated;
                btnTranslate.setText(isTranslated ? "Show Original" : "Translate");
                showLoading(false);
                checkFavoriteStatus(result);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Translation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                showLoading(false);
                checkFavoriteStatus(currentQuote.getText());
            }
        });
    }

    private void checkFavoriteStatus(String textToCheck) {
        executorService.execute(() -> {
            isFavorited = database.quoteDao().isFavoritedByQuote(textToCheck);
            runOnUiThread(this::updateHeartIcon);
        });
    }

    private void handleFavoriteClick() {
        if (currentQuote == null || textViewQuote.getText().toString().isEmpty()) return;

        String currentText = textViewQuote.getText().toString();
        String currentAuth = textViewAuthor.getText().toString();
        String currentCat = textViewCategory.getText().toString();

        executorService.execute(() -> {
            boolean isAdded;
            if (isFavorited) {
                database.quoteDao().deleteByQuoteText(currentText);
                isFavorited = false;
                isAdded = false;
            } else {
                FavoriteQuotes newFavorite = new FavoriteQuotes(currentText, currentAuth, currentCat);
                database.quoteDao().insertFavorite(newFavorite);
                isFavorited = true;
                isAdded = true;
            }

            runOnUiThread(() -> {
                updateHeartIcon();
                if (isAdded) {
                    Toast.makeText(MainActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateHeartIcon() {
        if (isFavorited) {
            ivFavorite.setImageResource(R.drawable.favorite_button_filled_24);
            int colorRed = ContextCompat.getColor(this, android.R.color.holo_red_light);
            ImageViewCompat.setImageTintList(ivFavorite, ColorStateList.valueOf(colorRed));
        } else {
            ivFavorite.setImageResource(R.drawable.favorite_button_border_24);
            int colorBlack = ContextCompat.getColor(this, android.R.color.black);
            ImageViewCompat.setImageTintList(ivFavorite, ColorStateList.valueOf(colorBlack));
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (btnRefresh != null) btnRefresh.setEnabled(!isLoading);
        if (btnTranslate != null) btnTranslate.setEnabled(!isLoading);
    }
}