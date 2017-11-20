/**
 *
 */
package com.juusosoft.statusbarcalendar;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

/**
 * @author Juuso Valkeejärvi
 * @version 26.6.2014
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if ((prefs.getBoolean("startonboot", true)) &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                        == PackageManager.PERMISSION_GRANTED) {
            // Set an alarm so that service start is delayed slightly
            AlarmManager alarm = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            Intent service = new Intent(context, NotificationService.class);
            PendingIntent pi = PendingIntent.getService(context, 0, service, 0);
            alarm.cancel(pi);
            alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 15000, pi);
        }

    }
}