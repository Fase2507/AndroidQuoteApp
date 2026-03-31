package tr.duzce.edu.bm.androidquoteapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {FavoriteQuotes.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    // Connects the DAO to the database
    public abstract QuoteDao quoteDao();

    // Singleton pattern to prevent multiple instances of database opening at the same time
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "quotes_database")
                    .fallbackToDestructiveMigration() // Handles schema changes
                    .build();
        }
        return instance;
    }
}