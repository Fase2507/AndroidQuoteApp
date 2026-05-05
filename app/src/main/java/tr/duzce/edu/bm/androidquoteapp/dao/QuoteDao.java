package tr.duzce.edu.bm.androidquoteapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import tr.duzce.edu.bm.androidquoteapp.models.FavoriteQuotes;

@Dao
public interface QuoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(FavoriteQuotes quote);

    @Delete
    void deleteFavorite(FavoriteQuotes quote);

    // Kullanıcı bazlı metodlar (Yeni standart)
    @Query("DELETE FROM favorite_quotes WHERE quoteText = :quoteText AND userEmail = :userEmail")
    void deleteByQuoteTextAndUser(String quoteText, String userEmail);

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_quotes WHERE quoteText = :quoteText AND userEmail = :userEmail)")
    boolean isFavoritedByUser(String quoteText, String userEmail);

    @Query("SELECT * FROM favorite_quotes WHERE userEmail = :userEmail")
    List<FavoriteQuotes> getAllFavoritesByUser(String userEmail);

}
