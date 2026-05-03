package tr.duzce.edu.bm.androidquoteapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import tr.duzce.edu.bm.androidquoteapp.R;
import tr.duzce.edu.bm.androidquoteapp.notifications.NotificationReceiver;
import tr.duzce.edu.bm.androidquoteapp.utils.AlarmUtils;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private RadioGroup rgLanguage;
    private MaterialButton btnGoBack;
    private RadioGroup rgTheme;
    private RadioGroup rgNotifications;
    private MaterialButton btnTestNotification;
    private SharedPreferences prefs;
    private MaterialCardView cardProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        btnGoBack = findViewById(R.id.btnGoBack);
        rgLanguage = findViewById(R.id.rgLanguage);
        rgTheme = findViewById(R.id.rgTheme);
        rgNotifications = findViewById(R.id.rgNotifications);
        btnTestNotification = findViewById(R.id.btnTestNotification);
        cardProfile = findViewById(R.id.cardProfile);

        // Initialize Language Selection
        initLanguageUI();

        // Initialize Theme Selection from SharedPreferences
        int savedTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (savedTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            rgTheme.check(R.id.lightThemeBtn);
        } else if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            rgTheme.check(R.id.darkThemeBtn);
        } else {
            rgTheme.check(R.id.systemThemeBtn);
        }

        // Initialize Notification Selection from SharedPreferences
        int savedNotif = prefs.getInt("notification_mode", AlarmUtils.NOTIF_OFF);
        if (savedNotif == AlarmUtils.NOTIF_ONCE) {
            rgNotifications.check(R.id.rbNotifOnce);
        } else if (savedNotif == AlarmUtils.NOTIF_TWICE) {
            rgNotifications.check(R.id.rbNotifTwice);
        } else {
            rgNotifications.check(R.id.rbNotifOff);
        }

        btnGoBack.setOnClickListener(v -> finish());
        cardProfile.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
        });

        btnTestNotification.setOnClickListener(v -> {
            if (checkAndRequestNotificationPermission()) {
                sendTestNotification();
            }
        });


        rgLanguage.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            String languageTag = (checkedId == R.id.rbTr) ? "tr" : "en";
            
            // Check current active language to prevent infinite recreation loop
            String currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags();
            if (currentLang.isEmpty()) currentLang = Locale.getDefault().getLanguage();
            
            if (!currentLang.startsWith(languageTag)) {
                LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageTag);
                AppCompatDelegate.setApplicationLocales(appLocale);
                // Mandatory: recreate activity to apply translation immediately
                recreate();
            }
        });

        rgTheme.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            int mode;
            if (checkedId == R.id.lightThemeBtn) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.darkThemeBtn) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }

            prefs.edit().putInt("theme_mode", mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        rgNotifications.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            int mode;
            if (checkedId == R.id.rbNotifOnce) {
                mode = AlarmUtils.NOTIF_ONCE;
            } else if (checkedId == R.id.rbNotifTwice) {
                mode = AlarmUtils.NOTIF_TWICE;
            } else {
                mode = AlarmUtils.NOTIF_OFF;
            }

            prefs.edit().putInt("notification_mode", mode).apply();
            AlarmUtils.updateAlarms(this, mode);
        });
    }

    private void initLanguageUI() {
        String currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        if (currentLang.isEmpty()) {
            currentLang = Locale.getDefault().getLanguage();
        }

        if (currentLang.startsWith("tr")) {
            rgLanguage.check(R.id.rbTr);
        } else {
            rgLanguage.check(R.id.rbEn);
        }
    }

    private boolean checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    private void sendTestNotification() {
        Intent intent = new Intent(this, NotificationReceiver.class);
        sendBroadcast(intent);
        Toast.makeText(this, R.string.testing_notification, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendTestNotification();
            } else {
                Toast.makeText(this, R.string.notif_permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }
}
