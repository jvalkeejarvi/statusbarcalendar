/**
 *
 */
package com.juusosoft.statusbarcalendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * @author Juuso Valkeejärvi
 * @version 18.6.2014
 */
public class SettingsActivity extends Activity {

	private SharedPreferences prefs;

	SharedPreferences.OnSharedPreferenceChangeListener spChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			updateNotification();
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
		prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(spChanged);
	}

	/**
	 * Constructor
	 */
	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}
	}

	/**
	 * Updates the notification that shows event. Called every time that any setting is changed.
	 */
	public void updateNotification() {
        if (prefs.getBoolean("service", true)) {
            Intent intent = new Intent(this, NotificationService.class);
            startService(intent);
        }
    }
}
