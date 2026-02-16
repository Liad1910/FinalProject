package com.example.finalproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class WatchlistReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "watchlist_channel";
    public static final int NOTIF_ID = 2011;

    @Override
    public void onReceive(Context context, Intent intent) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("watchlist")
                .orderBy("addedAt") // הראשון ברשימה
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) return;

                    String title = qs.getDocuments().get(0).getString("title");
                    if (title == null || title.trim().isEmpty()) title = "סרט מהרשימה שלך";

                    ensureChannel(context);

                    Intent openApp = new Intent(context, MainActivity.class);
                    openApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    NotificationCompat.Builder nb = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_background) // ⬅️ תוודאי שיש לך אייקון כזה
                            .setContentTitle("רשימת צפייה 🎬")
                            .setContentText("הגיע הזמן לראות: " + title)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(NOTIF_ID, nb.build());
                });
    }

    private void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Watchlist Reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        nm.createNotificationChannel(channel);
    }
}
