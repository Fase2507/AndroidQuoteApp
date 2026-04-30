package tr.duzce.edu.bm.androidquoteapp;

import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.google.android.material.button.MaterialButton;

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


        // Set the current selection based on the app's current locale
        String currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        if (currentLang.startsWith("tr")) {
            rgLanguage.check(R.id.rbTr);
        } else {
            rgLanguage.check(R.id.rbEn);
        }

        // Set the current selection based on the app's current theme
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
            
            // Apply the new locale
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
