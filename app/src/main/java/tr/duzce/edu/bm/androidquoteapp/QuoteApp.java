package tr.duzce.edu.bm.androidquoteapp;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import tr.duzce.edu.bm.androidquoteapp.utils.AlarmUtils;

public class QuoteApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Theme from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        int theme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(theme);

        // Schedule/Update Alarms based on saved preference
        int mode = prefs.getInt("notification_mode", AlarmUtils.NOTIF_OFF);
        AlarmUtils.updateAlarms(this, mode);
    }
}
