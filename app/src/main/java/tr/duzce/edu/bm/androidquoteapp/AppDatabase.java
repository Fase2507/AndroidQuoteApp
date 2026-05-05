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

@Database(entities = {FavoriteQuotes.class, User.class}, version = 14, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract QuoteDao quoteDao();
    public abstract UserDao userDao();

    static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Guest kullanıcısını veritabanı seviyesinde garanti et
            database.execSQL("INSERT OR IGNORE INTO users (email, password, isValidated, tokenExpiryTimestamp) " +
                    "VALUES ('Guest', 'GUEST_ACCOUNT', 1, 0)");
        }
    };

    static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Doğrulanmamış ve süresi dolmuş token'a sahip kullanıcıları temizle
            database.execSQL("DELETE FROM users WHERE isValidated = 0 AND tokenExpiryTimestamp > 0 AND tokenExpiryTimestamp < ?",
                    new Object[]{System.currentTimeMillis()});
        }
    };

    static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Yapısal bir şema değişikliği yok, sadece sürüm yükseltme
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "quotes_database")
                    .addMigrations(MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}