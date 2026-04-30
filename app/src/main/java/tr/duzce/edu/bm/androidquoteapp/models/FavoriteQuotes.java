package tr.duzce.edu.bm.androidquoteapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_quotes")
public class FavoriteQuotes {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String quoteText;
    private String author;
    private String category;
    private long timestamp;
    // FavoriteQuotes.java içine eklenecekler:
    private boolean isHighlighted = false; // Varsayılan olarak false

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }
    public FavoriteQuotes(String quoteText, String author, String category, long timestamp) {
        this.quoteText = quoteText;
        this.author = author;
        this.category = category;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
