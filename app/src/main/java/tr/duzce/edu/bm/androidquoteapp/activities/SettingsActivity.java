package tr.duzce.edu.bm.androidquoteapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import tr.duzce.edu.bm.androidquoteapp.R;

public class SettingsActivity extends AppCompatActivity {
    private RadioGroup rgLanguage;
    private MaterialButton btnGoBack;
    private RadioGroup rgTheme;
    private MaterialCardView cardProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnGoBack = findViewById(R.id.btnGoBack);
        rgLanguage = findViewById(R.id.rgLanguage);
        rgTheme = findViewById(R.id.rgTheme);
        cardProfile = findViewById(R.id.cardProfile);

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

        cardProfile.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
        });

        rgLanguage.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            String languageTag = "en";
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
