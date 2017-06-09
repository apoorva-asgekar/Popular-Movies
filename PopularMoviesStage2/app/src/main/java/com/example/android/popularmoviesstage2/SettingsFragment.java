package com.example.android.popularmoviesstage2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

/**
 * Created by apoorva on 4/7/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_general);

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();

        for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
            Preference pref = prefScreen.getPreference(i);
            if (!(pref instanceof CheckBoxPreference)) {
                String prefValue = sharedPreferences.getString(pref.getKey(), "");
                setPreferenceSummary(pref, prefValue);
            }
        }
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String prefStringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            int prefIndex = listPref.findIndexOfValue(prefStringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPref.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(prefStringValue);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);

        if (preference != null) {
            if (!(preference instanceof CheckBoxPreference)) {
                String prefValue = sharedPreferences.getString(key, "");
                setPreferenceSummary(preference, prefValue);
            }
        }
    }
}
