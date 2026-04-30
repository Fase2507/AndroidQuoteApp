package tr.duzce.edu.bm.androidquoteapp;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesActivity extends AppCompatActivity {

    private MaterialButton btnGoBack;
    private RadioGroup orderByRadioGroup;
    private RecyclerView rvFavorites;
    private LinearProgressIndicator progressBar;
    private MaterialTextView tvEmptyState;
    private AppDatabase database;
    private ExecutorService executorService;
    private FavoritesAdapter adapter;
    private List<FavoriteQuotes> favoriteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize views
        btnGoBack = findViewById(R.id.btnGoBack);
        orderByRadioGroup = findViewById(R.id.sortOptions);
        rvFavorites = findViewById(R.id.rvFavorites);
        progressBar = findViewById(R.id.favoritesProgressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        btnGoBack.setOnClickListener(v -> finish());

        // Handle RadioGroup changes
        orderByRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.SortByLatest) {
                orderByLatest();
            } else if (checkedId == R.id.SortByAuthor) {
                orderByAuthor();
            } else if (checkedId == R.id.SortByCategory) {
                orderByCategory();
            }
        });

        loadFavorites();
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            favoriteList = database.quoteDao().getAllFavorites();
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (favoriteList != null) {
                    // Default sort by latest
                    orderByLatest();
                }
                updateUI();
            });
        });
    }

    private void orderByAuthor() {
        if (favoriteList != null && !favoriteList.isEmpty()) {
            Collections.sort(favoriteList, (q1, q2) -> {
                String a1 = q1.getAuthor() != null ? q1.getAuthor() : "";
                String a2 = q2.getAuthor() != null ? q2.getAuthor() : "";
                return a1.compareToIgnoreCase(a2);
            });
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }

    private void orderByCategory() {
        if (favoriteList != null && !favoriteList.isEmpty()) {
            Collections.sort(favoriteList, (q1, q2) -> {
                String c1 = q1.getCategory() != null ? q1.getCategory() : "";
                String c2 = q2.getCategory() != null ? q2.getCategory() : "";
                return c1.compareToIgnoreCase(c2);
            });
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }

    private void orderByLatest() {
        if (favoriteList != null && !favoriteList.isEmpty()) {
            Collections.sort(favoriteList, (q1, q2) -> Long.compare(q2.getTimestamp(), q1.getTimestamp()));
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }

    private void updateUI() {
        if (favoriteList == null || favoriteList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
            
            if (adapter == null) {
                adapter = new FavoritesAdapter(favoriteList, this::unfavoriteQuote);
                rvFavorites.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void unfavoriteQuote(FavoriteQuotes quote, int position) {
        executorService.execute(() -> {
            database.quoteDao().deleteFavorite(quote);
            runOnUiThread(() -> {
                favoriteList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, favoriteList.size());
                
                if (favoriteList.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvFavorites.setVisibility(View.GONE);
                }
                
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}