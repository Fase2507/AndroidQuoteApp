package tr.duzce.edu.bm.androidquoteapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import tr.duzce.edu.bm.androidquoteapp.R;
import tr.duzce.edu.bm.androidquoteapp.models.FavoriteQuotes;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private final List<FavoriteQuotes> favoriteQuotes;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onUnfavoriteClick(FavoriteQuotes quote, int position);
    }

    public FavoritesAdapter(List<FavoriteQuotes> favoriteQuotes, OnItemClickListener listener) {
        this.favoriteQuotes = favoriteQuotes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_quote, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteQuotes quote = favoriteQuotes.get(position);
        holder.tvQuote.setText(quote.getQuoteText());
        holder.tvAuthor.setText(quote.getAuthor());
        holder.tvCategory.setText(quote.getCategory());

        holder.btnUnfavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnfavoriteClick(quote, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteQuotes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuote, tvAuthor, tvCategory;
        ImageView btnUnfavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuote = itemView.findViewById(R.id.tvQuoteText);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvCategory = itemView.findViewById(R.id.chipCategory);
            btnUnfavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}
