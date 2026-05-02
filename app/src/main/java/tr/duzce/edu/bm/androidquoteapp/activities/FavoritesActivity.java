package tr.duzce.edu.bm.androidquoteapp.activities;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tr.duzce.edu.bm.androidquoteapp.AppDatabase;
import tr.duzce.edu.bm.androidquoteapp.R;
import tr.duzce.edu.bm.androidquoteapp.adapters.FavoritesAdapter;
import tr.duzce.edu.bm.androidquoteapp.models.FavoriteQuotes;

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
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Kullanıcı bilgisini al
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserEmail = pref.getString("current_user_email", "Guest");

        btnGoBack = findViewById(R.id.btnGoBack);
        orderByRadioGroup = findViewById(R.id.sortOptions);
        rvFavorites = findViewById(R.id.rvFavorites);
        progressBar = findViewById(R.id.favoritesProgressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        btnGoBack.setOnClickListener(v -> finish());

        orderByRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.SortByLatest) {
                orderByLatest();
            } else if (checkedId == R.id.SortByAuthor) {
                orderByAuthor();
            } else if (checkedId == R.id.SortByCategory) {
                orderByCategory();
            }
        });

        setupSwipeActions();
        loadFavorites();
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            // SADECE aktif kullanıcıya ait favorileri getir
            favoriteList = database.quoteDao().getAllFavoritesByUser(currentUserEmail);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (favoriteList != null) {
                    orderByLatest();
                }
                updateUI();
            });
        });
    }

    // ... (unfavoriteQuote metodunda da kullanıcı bazlı kontrol için güncelleme)
    private void unfavoriteQuote(FavoriteQuotes quote, int position) {
        executorService.execute(() -> {
            database.quoteDao().deleteFavorite(quote);
            runOnUiThread(() -> {
                if (favoriteList != null && position >= 0 && position < favoriteList.size()) {
                    favoriteList.remove(position);
                    if (adapter != null) {
                        adapter.notifyItemRemoved(position);
                    }
                    if (favoriteList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    }
                }
            });
        });
    }
    
    // UI güncelleme ve swipe işlemleri aynı kalabilir ancak veriler artık kullanıcıya özel.
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

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (favoriteList == null || position < 0) return;
                FavoriteQuotes quote = favoriteList.get(position);
                if (direction == ItemTouchHelper.LEFT) {
                    unfavoriteQuote(quote, position);
                } else {
                    adapter.toggleHighlight(position);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // ... (Swipe çizim kodları aynı kalabilir)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvFavorites);
    }

    private void orderByAuthor() {
        if (favoriteList != null) {
            Collections.sort(favoriteList, (q1, q2) -> q1.getAuthor().compareToIgnoreCase(q2.getAuthor()));
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }

    private void orderByCategory() {
        if (favoriteList != null) {
            Collections.sort(favoriteList, (q1, q2) -> q1.getCategory().compareToIgnoreCase(q2.getCategory()));
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }

    private void orderByLatest() {
        if (favoriteList != null) {
            Collections.sort(favoriteList, (q1, q2) -> Long.compare(q2.getTimestamp(), q1.getTimestamp()));
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
