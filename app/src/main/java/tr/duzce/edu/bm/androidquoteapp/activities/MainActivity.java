package tr.duzce.edu.bm.androidquoteapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tr.duzce.edu.bm.androidquoteapp.AppDatabase;
import tr.duzce.edu.bm.androidquoteapp.R;
import tr.duzce.edu.bm.androidquoteapp.api.RetrofitClient;
import tr.duzce.edu.bm.androidquoteapp.models.FavoriteQuotes;
import tr.duzce.edu.bm.androidquoteapp.models.Quote;
import tr.duzce.edu.bm.androidquoteapp.models.User;
import tr.duzce.edu.bm.androidquoteapp.services.GeminiService;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout mainLayout;
    private MaterialTextView textViewQuote;
    private MaterialTextView textViewAuthor;
    private MaterialTextView textViewCategory;
    private MaterialButton btnRefresh;
    private MaterialButton btnTranslate;
    private MaterialButton btnGoToFavorites;
    private MaterialButton btnSettings;
    private CircularProgressIndicator progressBar;
    private FloatingActionButton ivFavorite;

    private final GeminiService geminiService = new GeminiService();
    private Quote currentQuote = null;
    private boolean isTranslated = false;

    private AppDatabase database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isFavorited = false;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.main);
        textViewQuote = findViewById(R.id.textViewQuote);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        textViewCategory = findViewById(R.id.textViewCategory);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnTranslate = findViewById(R.id.btnTranslate);
        btnSettings = findViewById(R.id.btnSettings);
        btnGoToFavorites = findViewById(R.id.btnGoToFavorites);
        progressBar = findViewById(R.id.progressBar);
        ivFavorite = findViewById(R.id.ivFavorite);

        database = AppDatabase.getInstance(this);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserEmail = pref.getString("current_user_email", "Guest");

        if ("Guest".equals(currentUserEmail)) {
            ensureGuestExists();
        }

        // İlk açılışta varsayılan arka planı ayarla
        updateBackground(null);

        handleIntent(getIntent());

        btnRefresh.setOnClickListener(v -> fetchNewQuote());
        btnTranslate.setOnClickListener(v -> translateQuote());
        btnGoToFavorites.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FavoritesActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
        ivFavorite.setOnClickListener(v -> handleFavoriteClick());
    }

    private void ensureGuestExists() {
        executorService.execute(() -> {
            if (database.userDao().getUserByEmail("Guest") == null) {
                database.userDao().registerUser(new User("Guest", "GUEST_ACCOUNT", true));
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("quote_text")) {
            String text = intent.getStringExtra("quote_text");
            String author = intent.getStringExtra("quote_author");
            currentQuote = new Quote();
            currentQuote.setText(text);
            currentQuote.setAuthor(author);
            displayQuote(currentQuote);
        } else {
            fetchNewQuote();
        }
    }

    private void fetchNewQuote() {
        showLoading(true);
        isTranslated = false;
        isFavorited = false;
        updateHeartIcon();

        RetrofitClient.getQuoteApi().getRandomQuote().enqueue(new Callback<List<Quote>>() {
            @Override
            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentQuote = response.body().get(0);
                    displayQuote(currentQuote);
                } else {
                    Toast.makeText(MainActivity.this, "Error fetching quote", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                }
            }

            @Override
            public void onFailure(Call<List<Quote>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Connection error", Toast.LENGTH_LONG).show();
                showLoading(false);
            }
        });
    }

    private void displayQuote(Quote quote) {
        showLoading(true);
        textViewQuote.setText(quote.getText());
        textViewAuthor.setText("- " + (quote.getAuthor() != null ? quote.getAuthor() : "Unknown"));
        
        geminiService.categorizeQuote(quote.getText(), new GeminiService.Callback() {
            @Override
            public void onSuccess(String result) {
                textViewCategory.setText(result);
                updateBackground(result);
                showLoading(false);
                checkFavoriteStatus(quote.getText());
            }
            @Override
            public void onError(Exception e) {
                textViewCategory.setText("General");
                updateBackground("General");
                showLoading(false);
                checkFavoriteStatus(quote.getText());
            }
        });
    }

    private void updateBackground(String category) {
        // Kategori null olsa bile metodun çalışmaya devam etmesini sağla
        String normalized = (category != null) ? category.toLowerCase().trim() : "";
        int resId = R.drawable.bg_wisdom; // Varsayılan (Wisdom)

        if (normalized.contains("life")) {
            resId = R.drawable.bg_life;
        } else if (normalized.contains("love")) {
            resId = R.drawable.bg_love;
        } else if (normalized.contains("motivation")) {
            resId = R.drawable.bg_motivation;
        } else if (normalized.contains("humor")) {
            resId = R.drawable.bg_humor;
        }

        // Arka planı uygula
        mainLayout.setBackgroundResource(resId);
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
                Toast.makeText(MainActivity.this, "Translation error", Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }

    private void checkFavoriteStatus(String textToCheck) {
        executorService.execute(() -> {
            isFavorited = database.quoteDao().isFavoritedByUser(textToCheck, currentUserEmail);
            runOnUiThread(this::updateHeartIcon);
        });
    }

    private void handleFavoriteClick() {
        if (currentQuote == null || textViewQuote.getText().toString().isEmpty()) return;
        String currentText = textViewQuote.getText().toString();
        String currentAuth = textViewAuthor.getText().toString();
        String currentCat = textViewCategory.getText().toString();

        executorService.execute(() -> {
            if (isFavorited) {
                database.quoteDao().deleteByQuoteTextAndUser(currentText, currentUserEmail);
                isFavorited = false;
            } else {
                database.quoteDao().insertFavorite(new FavoriteQuotes(currentUserEmail, currentText, currentAuth, currentCat, System.currentTimeMillis()));
                isFavorited = true;
            }
            runOnUiThread(() -> {
                updateHeartIcon();
                Toast.makeText(this, isFavorited ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateHeartIcon() {
        ivFavorite.setImageResource(isFavorited ? R.drawable.favorite_button_filled_24 : R.drawable.favorite_button_border_24);
        int color = isFavorited ? android.R.color.holo_red_light : android.R.color.black;
        ImageViewCompat.setImageTintList(ivFavorite, ColorStateList.valueOf(ContextCompat.getColor(this, color)));
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
