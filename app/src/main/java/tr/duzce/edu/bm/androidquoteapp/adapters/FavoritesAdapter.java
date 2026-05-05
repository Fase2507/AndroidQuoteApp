package tr.duzce.edu.bm.androidquoteapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import tr.duzce.edu.bm.androidquoteapp.R;
import tr.duzce.edu.bm.androidquoteapp.models.FavoriteQuotes;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private List<FavoriteQuotes> favoriteQuotes;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onUnfavoriteClick(FavoriteQuotes quote, int position);
    }

    public FavoritesAdapter(List<FavoriteQuotes> favoriteQuotes, OnItemClickListener listener) {
        this.favoriteQuotes = favoriteQuotes;
        this.listener = listener;
    }

    // Yeni listeyi set etmek için bu metodu ekledik
    public void updateList(List<FavoriteQuotes> newList) {
        this.favoriteQuotes = newList;
        notifyDataSetChanged();
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

        if (quote.isHighlighted()) {
            holder.cardContainer.setBackgroundResource(R.drawable.bg_highlighted_quote);
            int textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.highlight_text);
            holder.tvQuote.setTextColor(textColor);
            holder.tvAuthor.setTextColor(textColor);
        } else {
            holder.cardContainer.setBackgroundResource(R.drawable.bg_normal_quote);

        }

        holder.btnUnfavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnfavoriteClick(quote, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteQuotes != null ? favoriteQuotes.size() : 0;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuote, tvAuthor, tvCategory;
        ImageView btnUnfavorite;
        View cardContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuote = itemView.findViewById(R.id.tvQuoteText);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvCategory = itemView.findViewById(R.id.chipCategory);
            btnUnfavorite = itemView.findViewById(R.id.ivFavorite);
            cardContainer = itemView.findViewById(R.id.cardContainer);
        }
    }
}
