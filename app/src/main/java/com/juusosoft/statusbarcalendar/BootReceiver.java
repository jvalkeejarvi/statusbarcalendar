/**
 * 
 */
package com.juusosoft.statusbarcalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Juuso Valkeejärvi
 * @version 26.6.2014
 */
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		if (prefs.getBoolean("startonboot", true)) {
			AlarmManager alarm = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent service = new Intent(context, NotificationService.class);
			PendingIntent pi = PendingIntent.getService(context, 0, service, 0);
			alarm.cancel(pi);
			alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 50000, pi);
		}
	}

}
