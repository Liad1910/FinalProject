package com.example.finalproject;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WatchlistReminderWorker extends Worker {

    public static final String CHANNEL_ID = "watchlist_channel";
    private static final int BASE_ID = 6000; // 6000..6004

    public WatchlistReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context context = getApplicationContext();

        // ✅ Android 13+ permission
        if (!canPostNotifications(context)) return Result.success();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) return Result.success();

        ensureChannel(context);

        CountDownLatch latch = new CountDownLatch(1);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("watchlist")
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        latch.countDown();
                        return;
                    }

                    NotificationManagerCompat nm = NotificationManagerCompat.from(context);

                    // מנקה קודמות (כדי שלא ייערם)
                    for (int i = 0; i < 5; i++) nm.cancel(BASE_ID + i);

                    int count = qs.getDocuments().size();

                    for (int i = 0; i < count; i++) {
                        String title = qs.getDocuments().get(i).getString("title");
                        if (title == null || title.trim().isEmpty()) title = "סרט #" + (i + 1);

                        NotificationCompat.Builder nb = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("רשימת צפייה 🎬")
                                .setContentText("הגיע הזמן לראות: " + title)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_HIGH);

                        try {
                            nm.notify(BASE_ID + i, nb.build());
                        } catch (SecurityException ignored) {
                            // אם המשתמש חסם הרשאות אחרי שאישר פעם
                            break;
                        }
                    }

                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        try {
            // מחכים קצת שה-Firestore יסיים (Worker זה Thread ברקע)
            latch.await(8, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) { }

        return Result.success();
    }

    private boolean canPostNotifications(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;

        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Watchlist Reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        nm.createNotificationChannel(channel);
    }
}
