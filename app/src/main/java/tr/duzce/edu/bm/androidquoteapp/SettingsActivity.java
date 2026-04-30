package tr.duzce.edu.bm.androidquoteapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private RadioGroup rgLanguage;
    private MaterialButton btnGoBack;
    private RadioGroup rgTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnGoBack = findViewById(R.id.btnGoBack);
        rgLanguage = findViewById(R.id.rgLanguage);
        rgTheme = findViewById(R.id.rgTheme);


        SettingsManager settingsManager = new SettingsManager(this);
        MaterialSwitch switchNotifications = findViewById(R.id.switchNotifications);
        MaterialButton btnTimePicker = findViewById(R.id.btnTimePicker);


        switchNotifications.setChecked(settingsManager.isNotificationsEnabled());
        btnTimePicker.setText(String.format(Locale.getDefault(), "Bildirim Saati: %02d:%02d",
                settingsManager.getNotificationHour(), settingsManager.getNotificationMinute()));
        btnTimePicker.setEnabled(settingsManager.isNotificationsEnabled());


        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setNotificationsEnabled(isChecked);
            btnTimePicker.setEnabled(isChecked);

            if (isChecked) {
                DailyQuoteScheduler.reschedule(this);
            } else {
                DailyQuoteScheduler.cancel(this);
            }
        });


        btnTimePicker.setOnClickListener(v -> {
            int currentHour = settingsManager.getNotificationHour();
            int currentMinute = settingsManager.getNotificationMinute();

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {

                        settingsManager.setNotificationTime(hourOfDay, minute);
                        btnTimePicker.setText(String.format(Locale.getDefault(), "Bildirim Saati: %02d:%02d", hourOfDay, minute));


                        DailyQuoteScheduler.reschedule(this);
                    }, currentHour, currentMinute, true);
            timePickerDialog.show();
        });



        String currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        if (currentLang.startsWith("tr")) {
            rgLanguage.check(R.id.rbTr);
        } else {
            rgLanguage.check(R.id.rbEn);
        }


        int currentTheme = AppCompatDelegate.getDefaultNightMode();
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            rgTheme.check(R.id.lightThemeBtn);
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            rgTheme.check(R.id.darkThemeBtn);
        } else {
            rgTheme.check(R.id.systemThemeBtn);
        }

        btnGoBack.setOnClickListener(v -> finish());

        rgLanguage.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            String languageTag = "en"; // Default
            if (checkedId == R.id.rbTr) {
                languageTag = "tr";
            } else if (checkedId == R.id.rbEn) {
                languageTag = "en";
            }


            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageTag);
            AppCompatDelegate.setApplicationLocales(appLocale);
        });

        rgTheme.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.lightThemeBtn) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.darkThemeBtn) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else if (checkedId == R.id.systemThemeBtn) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        });
    }
}