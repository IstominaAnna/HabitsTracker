package com.example.habitstracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class Notification_reciever extends BroadcastReceiver {
    String nameHabit;
    String motivation;
    int id;


    @Override
    public void onReceive(Context context, Intent intent) {
        //intent.getExtras();
        nameHabit = intent.getStringExtra("name");
        motivation = intent.getStringExtra("motivation");
        id = intent.getIntExtra("id", 0);

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent reapiting_intent = new Intent(context, MainActivity.class);
        reapiting_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, reapiting_intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.icons8100)
                .setContentTitle(nameHabit)
                .setContentText(motivation)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);

        notificationManager.notify(id, builder.build());




    }
}
