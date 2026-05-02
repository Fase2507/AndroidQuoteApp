package tr.duzce.edu.bm.androidquoteapp;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import tr.duzce.edu.bm.androidquoteapp.dao.QuoteDao;
import tr.duzce.edu.bm.androidquoteapp.dao.UserDao;
import tr.duzce.edu.bm.androidquoteapp.models.FavoriteQuotes;
import tr.duzce.edu.bm.androidquoteapp.models.User;

@Database(entities = {FavoriteQuotes.class, User.class}, version = 10, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract QuoteDao quoteDao();
    public abstract UserDao userDao();

    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 1. Create unique index on users(email) to support Foreign Key
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`) ");

            // 2. Re-create favorite_quotes table with Foreign Key constraint
            // SQLite doesn't support adding FK to existing table, so we use the table swap pattern
            database.execSQL("CREATE TABLE IF NOT EXISTS `favorite_quotes_new` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`userEmail` TEXT, " +
                    "`quoteText` TEXT, " +
                    "`author` TEXT, " +
                    "`category` TEXT, " +
                    "`timestamp` INTEGER NOT NULL, " +
                    "`isHighlighted` INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(`userEmail`) REFERENCES `users`(`email`) ON UPDATE NO ACTION ON DELETE CASCADE )");

            // Copy data from old table to new table
            database.execSQL("INSERT INTO `favorite_quotes_new` (id, userEmail, quoteText, author, category, timestamp, isHighlighted) " +
                    "SELECT id, userEmail, quoteText, author, category, timestamp, isHighlighted FROM `favorite_quotes` ");

            // Drop old table
            database.execSQL("DROP TABLE `favorite_quotes` ");

            // Rename new table to original name
            database.execSQL("ALTER TABLE `favorite_quotes_new` RENAME TO `favorite_quotes` ");

            // Re-create index on userEmail
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_favorite_quotes_userEmail` ON `favorite_quotes` (`userEmail`) ");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "quotes_database")
                    .addMigrations(MIGRATION_9_10)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
