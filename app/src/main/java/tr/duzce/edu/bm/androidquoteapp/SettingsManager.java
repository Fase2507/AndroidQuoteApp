package tr.duzce.edu.bm.androidquoteapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREF_NAME = "QuoteAppSettings";
    private static final String KEY_HOUR = "notification_hour";
    private static final String KEY_MINUTE = "notification_minute";
    private static final String KEY_ENABLED = "notifications_enabled";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setNotificationTime(int hour, int minute) {
        prefs.edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply();
    }

    public int getNotificationHour() {
        return prefs.getInt(KEY_HOUR, 9);
    }

    public int getNotificationMinute() {
        return prefs.getInt(KEY_MINUTE, 0);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_ENABLED, true);
    }
}