package com.example.finalproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class WatchlistReminderScheduler {

    private static final int REQ_DAILY = 8801;

    public static void scheduleDaily(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, WatchlistReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                REQ_DAILY,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // כל יום ב-20:30 (אפשר לשנות)
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);

        // אם כבר עבר היום – מחר
        if (c.getTimeInMillis() <= System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }

        // setExactAndAllowWhileIdle כדי שיעבוד טוב גם ב-doze
        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
        }
    }

    // אופציונלי: כשנכנסים לאפליקציה -> התראה “מיידית”
    public static void scheduleInSeconds(Context context, int seconds) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, WatchlistReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                8802,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long t = System.currentTimeMillis() + seconds * 1000L;
        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi);
        }
    }
}
