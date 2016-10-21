package com.juusosoft.statusbarcalendar;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author Juuso Valkeejärvi
 * @version 13.6.2014
 */
public class NotificationService extends IntentService {
    private static final int notificationID = 100;
    private final int[][] rowsBig = {{R.id.row0bullet, R.id.row0title},
            {R.id.row1bullet, R.id.row1title},
            {R.id.row2bullet, R.id.row2title},
            {R.id.row3bullet, R.id.row3title},
            {R.id.row4bullet, R.id.row4title},
            {R.id.row5bullet, R.id.row5title},
            {R.id.row6bullet, R.id.row6title},
            {R.id.row7bullet, R.id.row7title},
            {R.id.row8bullet, R.id.row8title},
            {R.id.row9bullet, R.id.row9title}};
    private final int[][] rowsSmall = {{R.id.row0bullet, R.id.row0title},
            {R.id.row1bullet, R.id.row1title},
            {R.id.row2bullet, R.id.row2title}};
    private static final int BULLET = 0;
    private static final int TITLE = 1;
    private Set<String> cals;
    private int lookAhead;


    /**
     * Constructor for notificationservice
     */
    public NotificationService() {
        super("AgendaNotifications");
    }

    public void onCreate() {
        Set<String> visible = new HashSet<>();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPref.getStringSet("list", null) == null) {
            try {
                visible = new GetVisibleCalendars().execute(
                        getContentResolver()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            Editor edit = sharedPref.edit();
            edit.putStringSet("list", visible).apply();
        }

        lookAhead = sharedPref.getInt("lookahead", getResources().getInteger(R.integer.default_lookahead));
        cals = sharedPref.getStringSet("list", new HashSet<String>());
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(
                Context.ALARM_SERVICE);
        Intent service = new Intent(getApplicationContext(),
                NotificationService.class);
        PendingIntent pi = PendingIntent.getService(getApplicationContext(),
                MainActivity.alarmRequestCode, service, 0);
        alarm.cancel(pi);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews notificationBig = new RemoteViews(getPackageName(),
                R.layout.notification);
        RemoteViews notificationSmall = new RemoteViews(getPackageName(),
                R.layout.notificationsmall);
        RemoteViews notificationEmpty = new RemoteViews(getPackageName(),
                R.layout.notificationempty);


        ArrayList<Day> events = this.ReadEvents();
        //Build notification
        Builder builder = new Notification.Builder(getApplicationContext());
        builder.setOngoing(true).setSmallIcon(android.R.color.transparent)
                .setPriority(Notification.PRIORITY_LOW);
        if (sharedPref.getBoolean("forcetop",
                getResources().getBoolean(R.bool.default_forcetop)))
            builder.setPriority(Notification.PRIORITY_HIGH);
        Intent notificationClickIntent = new Intent(Intent.ACTION_VIEW);
        notificationClickIntent.setData(Uri
                .parse("content://com.android.calendar/time"));
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, notificationClickIntent, 0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        int interval = Integer.parseInt(sharedPref.getString("updateinterval", getResources().getString(R.string.default_updateinterval)));
        long when = interval * 60 * 1000;
        Locale current = getResources().getConfiguration().locale;
        SimpleDateFormat date = new SimpleDateFormat("EEE d.M", current);
        SimpleDateFormat time = new SimpleDateFormat(sharedPref.getString("clockstyle", getResources().getString(R.string.default_clockstyle)),
                current);
        if (events.size() > 0) {
            int currentRow = 0;
            int currentRowSmall = 0;
            Calendar lastEventcal = events.get(0).getEvents().get(0)
                    .getEndCal();
            for (int j = 0; j < events.size()
                    && currentRow < (rowsBig.length - 1); j++) {
                //Set day text
                Day day = events.get(j);
                ArrayList<Event> eventList = day.getEvents();
                notificationBig.setViewVisibility(rowsBig[currentRow][BULLET],
                        View.GONE);
                notificationBig.setViewVisibility(rowsBig[currentRow][TITLE],
                        View.VISIBLE);
                notificationBig.setTextViewText(rowsBig[currentRow][TITLE],
                        date.format(day.getCal().getTimeInMillis()));
                currentRow++;
                for (int k = 0; k < eventList.size()
                        && currentRow < rowsBig.length; k++) {
                    Event event = eventList.get(k);
                    //Get bullet color and symbol
                    int bulletColor = 0xffffffff;
                    if (sharedPref.getBoolean("coloredbullets", getResources()
                            .getBoolean(R.bool.default_coloredbullets)))
                        bulletColor = 0xff000000 + event.getColor();
                    String bulletSymbol = sharedPref.getString(
                            "bulletsymbol",
                            getResources().getString(
                                    R.string.default_bulletsymbol));
                    //Set notification row text
                    StringBuilder eventText = new StringBuilder();
                    if (!event.isAllDay()) {
                        eventText.append(time.format(event.getStartCal()
                                .getTimeInMillis()));
                        if (sharedPref.getBoolean("showendtime", getResources()
                                .getBoolean(R.bool.default_showendtime))) {
                            eventText.append("-");
                            eventText.append(time.format(event.getEndCal()
                                    .getTimeInMillis()));
                        }
                        eventText.append("  ");
                    }
                    eventText.append(event.getTitle());
                    //Set big notification row
                    setRow(notificationBig, rowsBig[currentRow][BULLET],
                            rowsBig[currentRow][TITLE], true, bulletSymbol,
                            bulletColor, eventText.toString());
                    //Set small notification row
                    if (currentRowSmall < rowsSmall.length) {
                        setRow(notificationSmall,
                                rowsSmall[currentRowSmall][BULLET],
                                rowsSmall[currentRowSmall][TITLE],
                                true,
                                bulletSymbol,
                                bulletColor,
                                date.format(event.getStartCal()
                                        .getTimeInMillis())
                                        + "  "
                                        + eventText.toString());
                        currentRowSmall++;
                    }
                    currentRow++;
                }
            }
            //Hide rest of the rows
            for (; currentRow < rowsBig.length; currentRow++)
                setRow(notificationBig, rowsBig[currentRow][BULLET],
                        rowsBig[currentRow][TITLE], false, "", 0, "");
            for (; currentRowSmall < rowsSmall.length; currentRowSmall++)
                setRow(notificationSmall, rowsSmall[currentRowSmall][BULLET],
                        rowsSmall[currentRowSmall][TITLE], false, "", 0, "");

            when = lastEventcal.getTimeInMillis()
                    - Calendar.getInstance().getTimeInMillis() + 2000;

            notification.contentView = notificationSmall;
            notification.bigContentView = notificationBig;
            mNotificationManager.cancel(notificationID);
            mNotificationManager.notify(notificationID, notification);
        } else {
            if (sharedPref.getBoolean("emptynotification", getResources()
                    .getBoolean(R.bool.default_emptynotification))) {
                notification.contentView = notificationEmpty;
                notification.bigContentView = notificationEmpty;
                mNotificationManager.cancel(notificationID);
                mNotificationManager.notify(notificationID, notification);
            } else
                mNotificationManager.cancel(notificationID);
        }
        alarm.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()
                        + Math.min(when, (interval * 60 * 1000)), pi);
    }


    private void setRow(RemoteViews remote, int bulletID, int textID,
                        boolean visible, String bullet, int color, String text) {
        int visibleInt;
        if (visible)
            visibleInt = View.VISIBLE;
        else
            visibleInt = View.GONE;
        remote.setViewVisibility(bulletID, visibleInt);
        remote.setViewVisibility(textID, visibleInt);
        remote.setTextColor(bulletID, color);
        remote.setTextViewText(bulletID, bullet);
        remote.setTextViewText(textID, text);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void cleanAlarmAndNotification (Context context) {
        Intent service = new Intent(context, NotificationService.class);
        context.stopService(service);
        cleanAlarm(context);
        cleanNotification(context);
    }

    private static void cleanAlarm (Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent service = new Intent(context, NotificationService.class);
        PendingIntent pi = PendingIntent.getService(context, MainActivity.alarmRequestCode,
                service, 0);
        alarm.cancel(pi);
    }

    private static void cleanNotification (Context context) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }


    public ArrayList<Day> ReadEvents() {
        final String[] EVENT_PROJECTION = new String[]{
                Events.TITLE,
                Instances.BEGIN,
                Instances.END,
                Events.EVENT_LOCATION,
                Events.CALENDAR_ID,
                Events.ALL_DAY,
                Events.DISPLAY_COLOR};

        // The indices for the projection array above.
        final int PROJECTION_EVENT_TITLE = 0;
        final int PROJECTION_EVENT_BEGIN = 1;
        final int PROJECTION_EVENT_END = 2;
        final int PROJECTION_EVENT_LOCATION = 3;
        final int PROJECTION_EVENT_CALID = 4;
        final int PROJECTION_EVENT_ALLDAY = 5;
        final int PROJECTION_EVENT_COLOR = 6;

        Cursor cur;

        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        long start = cal.getTimeInMillis();
        long range = lookAhead * 60 * 60 * 1000;
        long end = start + range;

        String selection = "((" + Instances.BEGIN + " < ? AND "
                + Instances.END + " > ?) OR (" + Instances.BEGIN + " BETWEEN ? AND ? ))";
        String selection2 = "((" + Instances.BEGIN + " < ? AND "
                + Instances.END + " > ?) OR (" + Instances.BEGIN + " > ? ))";
        String[] selectionArgs = {Double.toString(start),
                Double.toString(start), Double.toString(start),
                Double.toString(end)};
        String[] selectionArgs2 = {Double.toString(start),
                Double.toString(start), Double.toString(start)};

        ContentResolver resolver = getContentResolver();
        Uri.Builder eventsUriBuilder = Instances.CONTENT_URI.buildUpon();
        eventsUriBuilder.clearQuery();

        ArrayList<Day> days = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED) {
            if (lookAhead != 0) {
                ContentUris.appendId(eventsUriBuilder, start);
                ContentUris.appendId(eventsUriBuilder, end);
                Uri eventUri = eventsUriBuilder.build();
                cur = resolver.query(eventUri, EVENT_PROJECTION, selection,
                        selectionArgs, Instances.BEGIN + " ASC");
            } else {
                range = 960 * 60 * 60 * 1000L;
                end = start + range;
                ContentUris.appendId(eventsUriBuilder, start);
                ContentUris.appendId(eventsUriBuilder, end);
                Uri eventUri = eventsUriBuilder.build();
                cur = resolver.query(eventUri, EVENT_PROJECTION, selection2,
                        selectionArgs2, Instances.BEGIN + " ASC");
            }
        } else {
            return days;
        }

        while (cur != null && cur.moveToNext()) {
            String eventTitle;
            String eventLocation;
            long startTime;
            long endTime;
            String calID;
            int allDay;
            int color;
            boolean allDayBoolean = false;
            startTime = cur.getLong(PROJECTION_EVENT_BEGIN);
            endTime = cur.getLong(PROJECTION_EVENT_END);

            eventTitle = cur.getString(PROJECTION_EVENT_TITLE);
            eventLocation = cur.getString(PROJECTION_EVENT_LOCATION);
            calID = cur.getString(PROJECTION_EVENT_CALID);
            allDay = cur.getInt(PROJECTION_EVENT_ALLDAY);
            color = cur.getInt(PROJECTION_EVENT_COLOR);
            if (cals.contains(calID)) {
                Calendar.getInstance().get(Calendar.DATE);
                cal.setTimeInMillis(startTime);
                cal2.setTimeInMillis(endTime);
                if (days.size() == 0) {
                    days.add(new Day(cal.get(Calendar.YEAR), cal
                            .get(Calendar.MONTH), cal.get(Calendar.DATE)));
                }

                Day dayToCompare = days.get(days.size() - 1);

                if ((int) cal.get(Calendar.YEAR) != dayToCompare.getYear()
                        || (int) cal.get(Calendar.MONTH) != dayToCompare.getMonth()
                        || (int) cal.get(Calendar.DATE) != dayToCompare.getDay()) {
                    days.add(new Day(cal.get(Calendar.YEAR), cal
                            .get(Calendar.MONTH), cal.get(Calendar.DATE)));
                }
                if (eventLocation == null)
                    eventLocation = "";
                if (allDay == 1)
                    allDayBoolean = true;
                days.get(days.size() - 1).addEvent(
                        new Event(cal, cal2, eventTitle + "  "
                                + eventLocation, color, allDayBoolean));
                int eventCount = 0;
                for (int i = 0; i < days.size(); i++)
                    eventCount += days.get(i).getEventCount();
                if (eventCount > rowsBig.length)
                    break;
            }
        }
        if (cur != null) cur.close();
        return days;
    }


    private class GetVisibleCalendars extends
            AsyncTask<ContentResolver, Void, Set<String>> {

        ContentResolver cr;
        Cursor cursor;
        String[] projection = new String[]{CalendarContract.Calendars._ID};
        String selection = "(" + CalendarContract.Calendars.VISIBLE + " = ?)";
        String[] selectionArgs = new String[]{"1"};

        @Override
        protected Set<String> doInBackground(ContentResolver... params) {
            Set<String> visible = new HashSet<>();
            cr = params[0];

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                cursor = cr.query(CalendarContract.Calendars.CONTENT_URI,
                        projection, selection, selectionArgs, null);
            } else {
                return visible;
            }

            while (cursor != null && cursor.moveToNext()) {
                String name = cursor.getString(0);
                visible.add(name);
            }
            if (cursor != null) cursor.close();
            return visible;
        }
    }
}
