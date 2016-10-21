package com.juusosoft.statusbarcalendar;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

/**
  * 
  * @author Juuso Valkeejärvi
  * @version 25.6.2014
  * Listens to calendar events and causes the service to update notification when
  * there are changes in calendars.
  */
public class CalendarReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences shared = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (shared.getBoolean("service", true) && ContextCompat.checkSelfPermission(context,
				Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
			Intent service = new Intent(context, NotificationService.class);
			context.startService(service);
		}
	}
}