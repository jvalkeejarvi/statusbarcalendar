package com.juusosoft.statusbarcalendar;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.Snackbar;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

/**
 * @author Juuso Valkeejärvi
 * @version 25.6.2014
 *          Main activity of the app
 */
public class MainActivity extends AppCompatActivity {

    public static final int alarmRequestCode = 12;
    private static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            requestCalendarPermission();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    public void onResume() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("service", true)) {
            startStopNotificationService(this.getApplicationContext(), true);
        }
        super.onResume();
    }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(MainActivity.this,
                    SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void requestCalendarPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CALENDAR},
                MY_PERMISSIONS_REQUEST_READ_CALENDAR);
    }

    private void showPermissionMissingSnackbar() {
        CoordinatorLayout coord =  (CoordinatorLayout) findViewById(R.id.container);
        Snackbar.make(coord, "Calendar permission not granted", Snackbar.LENGTH_INDEFINITE)
                .show();
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // NotificationService.cleanAlarmAndNotification(this.getApplicationContext());
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_CALENDAR)) {

                        // Show dialog explaining why this app needs calendar permission
                        showDialogOK("SMS and Location Services Permission required for this app",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                requestCalendarPermission();
                                                break;
                                            case DialogInterface.BUTTON_NEGATIVE:
                                                // proceed with logic by disabling the related features or quit the app.
                                                break;
                                        }
                                    }
                                });
                    } else {
                        // Show toast if user has clicked 'Show never again' on permission request
                        showPermissionMissingSnackbar();
                        /*CharSequence text = "Grant calendar permission in app settings";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(this.getApplicationContext(), text, duration);
                        toast.show();*/
                        // finish();
                    }
                }
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        /**
         *
         */
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Switch toggle = (Switch) rootView.findViewById(R.id.service_toggle);
            if (toggle.isChecked() != sharedPref.getBoolean("service", true))
                toggle.setChecked(sharedPref.getBoolean("service", true));
            toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    Editor edit = sharedPref.edit();
                    edit.putBoolean("service", isChecked).apply();
                    startStopNotificationService(getActivity().getApplicationContext(), isChecked);
                }
            });
            return rootView;
        }
    }

    /**
     * Start service and save service running status to preferences
     *
     * @param context   Activity passed to function
     * @param startstop true to start service and show notification, false to stop it
     */
    public static void startStopNotificationService(Context context, boolean startstop) {
        Intent service = new Intent(context, NotificationService.class);
        // Start service if READ_CALENDAR permission is granted and startstop is tue
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED && startstop) {
            context.startService(service);
            // Otherwise stop service and cancel set alarm
        } else {
            NotificationService.cleanAlarmAndNotification(context);
        }
    }
}
