package tr.duzce.edu.bm.androidquoteapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textview.MaterialTextView;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private final List<FavoriteQuotes> favoriteList;
    private OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onUnfavoriteClick(FavoriteQuotes quote, int position);
    }

    public FavoritesAdapter(List<FavoriteQuotes> favoriteList, OnFavoriteClickListener listener) {
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_quote, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteQuotes favorite = favoriteList.get(position);
        holder.tvQuoteText.setText(favorite.getQuoteText());
        holder.tvAuthor.setText(favorite.getAuthor());
        holder.chipCategory.setText(favorite.getCategory());

        // ARKA PLAN KODU BAŞLANGICI
        // Veritabanından gelen kategoriyi alıp küçük harfe çeviriyoruz
        String category = favorite.getCategory() != null ? favorite.getCategory().trim().toLowerCase() : "";
        int backgroundResId;

        // Kategoriye göre doğru resmi seçiyoruz
        switch (category) {
            case "love":
                backgroundResId = R.drawable.bg_love;
                break;
            case "life":
                backgroundResId = R.drawable.bg_life;
                break;
            case "motivation":
                backgroundResId = R.drawable.bg_motivation;
                break;
            case "wisdom":
                backgroundResId = R.drawable.bg_wisdom;
                break;
            case "humor":
                backgroundResId = R.drawable.bg_humor;
                break;
            default:
                backgroundResId = R.drawable.bg_wisdom; // Eşleşme olmazsa varsayılan
                break;
        }

        // Seçilen resmi bu kartın arka planına uyguluyoruz
        if (holder.itemBackgroundImageView != null) {
            holder.itemBackgroundImageView.setImageResource(backgroundResId);
        }
        //end

        holder.ivFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnfavoriteClick(favorite, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView tvQuoteText, tvAuthor;
        Chip chipCategory;
        ImageView ivFavorite;
        ImageView itemBackgroundImageView; // 1. YENİ EKLENDİ: Resmi tanımladık

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuoteText = itemView.findViewById(R.id.tvQuoteText);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);

            // 2. YENİ EKLENDİ: XML'deki resmi bulduk
            itemBackgroundImageView = itemView.findViewById(R.id.itemBackgroundImageView);
        }
    }
}