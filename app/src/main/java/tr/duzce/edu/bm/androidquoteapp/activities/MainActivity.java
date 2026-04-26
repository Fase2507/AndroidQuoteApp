package tr.duzce.edu.bm.androidquoteapp.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import tr.duzce.edu.bm.androidquoteapp.services.GeminiService;

public class MainActivity extends AppCompatActivity {
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
    private ExecutorService executorService;
    private boolean isFavorited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        executorService = Executors.newSingleThreadExecutor();

        fetchNewQuote();

        btnRefresh.setOnClickListener(v -> fetchNewQuote());
        btnTranslate.setOnClickListener(v -> translateQuote());

        btnGoToFavorites.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
        });
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        ivFavorite.setOnClickListener(v -> handleFavoriteClick());
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
                FavoriteQuotes newFavorite = new FavoriteQuotes(currentText, currentAuth, currentCat, System.currentTimeMillis());
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
        if (btnGoToFavorites != null) btnGoToFavorites.setEnabled(!isLoading);
    }
}
