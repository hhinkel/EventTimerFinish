package com.example.android.eventtimerfinish;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class FinishPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            return false;
        }
    }
}
