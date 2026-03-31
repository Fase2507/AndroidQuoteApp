package tr.duzce.edu.bm.androidquoteapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView textViewQuote;
    private TextView textViewAuthor;
    private TextView textViewCategory;
    private Button btnRefresh;
    private Button btnTranslate;
    private ProgressBar progressBar;
    private ImageView ivFavorite; //ui element for favorite button

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
        setContentView(R.layout.activity_main);

        // old fashioned findViewById
        textViewQuote = findViewById(R.id.textViewQuote);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        textViewCategory = findViewById(R.id.textViewCategory);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnTranslate = findViewById(R.id.btnTranslate);
        progressBar = findViewById(R.id.progressBar);
        ivFavorite = findViewById(R.id.ivFavorite);
        //initialize db
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        fetchNewQuote();

        btnRefresh.setOnClickListener(v -> fetchNewQuote());
        btnTranslate.setOnClickListener(v -> translateQuote());

        ivFavorite.setOnClickListener(v -> handleFavoriteClick());
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
        // Prevent action if no quote is currently loaded
        if (currentQuote == null || textViewQuote.getText().toString().isEmpty()) return;

        // Get the exact text currently visible on the screen
        String currentText = textViewQuote.getText().toString();
        String currentAuth = textViewAuthor.getText().toString();
        String currentCat = textViewCategory.getText().toString();

        executorService.execute(() -> {
            boolean isAdded;

            if (isFavorited) {
                // Remove from database using the text
                database.quoteDao().deleteByQuoteText(currentText);
                isFavorited = false;
                isAdded = false;
            } else {
                // Add to database using the new FavoriteQuotes model
                FavoriteQuotes newFavorite = new FavoriteQuotes(currentText, currentAuth, currentCat);
                database.quoteDao().insertFavorite(newFavorite);
                isFavorited = true;
                isAdded = true;
            }

            // Update UI on the main thread
            runOnUiThread(() -> {
                updateHeartIcon();

                // Show the Toast notification
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
            // Set the filled heart icon
            ivFavorite.setImageResource(R.drawable.favorite_button_filled_24);

            // Get the red color from Android's default colors (or use your own R.color.red)
            int colorRed = ContextCompat.getColor(this, android.R.color.holo_red_light);

            // Apply the red tint dynamically
            ImageViewCompat.setImageTintList(ivFavorite, ColorStateList.valueOf(colorRed));
        } else {
            // Set the bordered heart icon
            ivFavorite.setImageResource(R.drawable.favorite_button_border_24);

            // Get the black color
            int colorBlack = ContextCompat.getColor(this, android.R.color.black);

            // Apply the black tint dynamically
            ImageViewCompat.setImageTintList(ivFavorite, ColorStateList.valueOf(colorBlack));
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (btnRefresh != null) btnRefresh.setEnabled(!isLoading);
        if (btnTranslate != null) btnTranslate.setEnabled(!isLoading);
    }
}
