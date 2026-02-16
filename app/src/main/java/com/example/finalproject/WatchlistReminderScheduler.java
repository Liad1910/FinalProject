package com.example.finalproject;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WatchlistReminderScheduler {

    private static final String WORK_ONCE = "watchlist_once";
    private static final String WORK_DAILY = "watchlist_daily";

    // תזכורת עוד X שניות (לבדיקה)
    public static void scheduleInSeconds(Context context, int seconds) {
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(WatchlistReminderWorker.class)
                .setInitialDelay(seconds, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_ONCE, ExistingWorkPolicy.REPLACE, req);
    }

    // תזכורת יומית (WorkManager המינימום זה 15 דקות, יומי זה סבבה)
    public static void scheduleDaily(Context context) {
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                WatchlistReminderWorker.class,
                1, TimeUnit.DAYS
        ).build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_DAILY, ExistingPeriodicWorkPolicy.UPDATE, req);
    }
}
