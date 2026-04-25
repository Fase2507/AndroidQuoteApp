package tr.duzce.edu.bm.androidquoteapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(FavoriteQuotes quote);

    @Delete
    void deleteFavorite(FavoriteQuotes quote);

    @Query("DELETE FROM favorite_quotes WHERE quoteText = :quoteText")
    void deleteByQuoteText(String quoteText);

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_quotes WHERE quoteText = :quoteText)")
    boolean isFavoritedByQuote(String quoteText);

    @Query("SELECT * FROM favorite_quotes")
    List<FavoriteQuotes> getAllFavorites();
}