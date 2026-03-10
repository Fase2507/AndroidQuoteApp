package tr.duzce.edu.bm.androidquoteapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

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

    private final GeminiService geminiService = new GeminiService();
    private Quote currentQuote = null;
    private boolean isTranslated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Old fashioned findViewById
        textViewQuote = findViewById(R.id.textViewQuote);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        textViewCategory = findViewById(R.id.textViewCategory);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnTranslate = findViewById(R.id.btnTranslate);
        progressBar = findViewById(R.id.progressBar);

        fetchNewQuote();

        btnRefresh.setOnClickListener(v -> fetchNewQuote());
        btnTranslate.setOnClickListener(v -> translateQuote());
    }

    private void fetchNewQuote() {
        showLoading(true);
        isTranslated = false;
        btnTranslate.setText("Translate");

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
                        }

                        @Override
                        public void onError(Exception e) {
                            textViewCategory.setText("General");
                            showLoading(false);
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
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Translation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (btnRefresh != null) btnRefresh.setEnabled(!isLoading);
        if (btnTranslate != null) btnTranslate.setEnabled(!isLoading);
    }
}
