package tr.duzce.edu.bm.androidquoteapp.activities;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

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

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            
            private final Paint paint = new Paint();

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (favoriteList == null || position < 0 || position >= favoriteList.size()) return;

                FavoriteQuotes quote = favoriteList.get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    unfavoriteQuote(quote, position);
                } else if (direction == ItemTouchHelper.RIGHT) {
                    if (adapter != null) {
                        adapter.toggleHighlight(position);
                    }
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) { // Swiping Right (Highlight)
                        paint.setColor(Color.parseColor("#FBC02D")); // Gold color
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, paint);
                        
                        Drawable icon = ContextCompat.getDrawable(FavoritesActivity.this, R.drawable.highlighted_star);
                        if (icon != null) {
                            int iconMargin = (int) (height - width) / 2;
                            int iconTop = itemView.getTop() + (int) (height - width) / 2;
                            int iconBottom = iconTop + (int) width;
                            int iconLeft = itemView.getLeft() + iconMargin;
                            int iconRight = iconLeft + (int) width;
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }
                    } else if (dX < 0) { // Swiping Left (Remove)
                        paint.setColor(Color.parseColor("#E57373")); // Red color
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, paint);
                        
                        Drawable icon = ContextCompat.getDrawable(FavoritesActivity.this, R.drawable.remove_icon);
                        if (icon != null) {
                            int iconMargin = (int) (height - width) / 2;
                            int iconTop = itemView.getTop() + (int) (height - width) / 2;
                            int iconBottom = iconTop + (int) width;
                            int iconLeft = itemView.getRight() - iconMargin - (int) width;
                            int iconRight = itemView.getRight() - iconMargin;
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvFavorites);
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            favoriteList = database.quoteDao().getAllFavorites();
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (favoriteList != null) {
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
                if (favoriteList != null && position >= 0 && position < favoriteList.size()) {
                    favoriteList.remove(position);
                    if (adapter != null) {
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, favoriteList.size());
                    }
                    
                    if (favoriteList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    }
                    
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
