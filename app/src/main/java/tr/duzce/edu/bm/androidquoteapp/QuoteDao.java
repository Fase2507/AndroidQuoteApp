package tr.duzce.edu.bm.androidquoteapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QuoteDao {

    // Inserts a new quote. If it already exists, it replaces it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(FavoriteQuotes quote);

    // Deletes by passing the entire object (Room matches the Primary Key automatically)
    @Delete
    void deleteFavorite(FavoriteQuotes quote);


    // Deletes a specific quote from favorites by text
    @Query("DELETE FROM favorite_quotes WHERE quoteText = :quoteText")
    void deleteByQuoteText(String quoteText);

    // Checks if a quote is already favorited by searching its exact text
    // Returns true (1) if it exists, false (0) if it doesn't
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_quotes WHERE quoteText = :quoteText)")
    boolean isFavoritedByQuote(String quoteText);


    // Gets all favorited quotes (useful for your Favorites Screen)
    @Query("SELECT * FROM favorite_quotes")
    List<Quote> getAllFavorites();
}