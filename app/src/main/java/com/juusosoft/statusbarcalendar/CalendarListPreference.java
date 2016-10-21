package com.juusosoft.statusbarcalendar;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.preference.MultiSelectListPreference;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.widget.Toast;


/**
 * @author Juuso Valkeejärvi
 * @version 25.6.2014
 *          Class that reads all device calendars and shows a list of them with checkboxes
 */
public class CalendarListPreference extends MultiSelectListPreference {

    /**
     * @param context application context
     * @param attrs   attributes
     */
    public CalendarListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> entriesValues = new ArrayList<>();

        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};

        try {
            Cursor cursor = cr.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String ID = cursor.getString(0);
                    String displayName = cursor.getString(1);
                    entries.add(displayName);
                    entriesValues.add(ID);
                }
                cursor.close();
            }

        } catch (SecurityException ex) {
            CharSequence text = "Calendar permission not granted";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        setEntries(entries.toArray(new CharSequence[]{}));
        setEntryValues(entriesValues.toArray(new CharSequence[]{}));
    }
}