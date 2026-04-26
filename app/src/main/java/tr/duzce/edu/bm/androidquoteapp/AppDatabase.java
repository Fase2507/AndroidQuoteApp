package tr.duzce.edu.bm.androidquoteapp;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import tr.duzce.edu.bm.androidquoteapp.dao.QuoteDao;
import tr.duzce.edu.bm.androidquoteapp.dao.UserDao;
import tr.duzce.edu.bm.androidquoteapp.models.FavoriteQuotes;
import tr.duzce.edu.bm.androidquoteapp.models.User;

@Database(entities = {FavoriteQuotes.class, User.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract QuoteDao quoteDao();
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "quotes_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
