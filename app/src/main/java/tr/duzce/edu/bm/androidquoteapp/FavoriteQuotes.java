package tr.duzce.edu.bm.androidquoteapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_quotes")
public class FavoriteQuotes {

    // The quote text itself is now the unique identifier (Primary Key)
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "quoteText")
    private String quoteText;
    private String author;
    private String category;

    // Constructor
    public FavoriteQuotes(@NonNull String quoteText, String author, String category) {
        this.quoteText = quoteText;
        this.author = author;
        this.category = category;
    }

    // Getters and Setters
    @NonNull
    public String getQuoteText() { return quoteText; }
    public void setQuoteText(@NonNull String quoteText) { this.quoteText = quoteText; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}