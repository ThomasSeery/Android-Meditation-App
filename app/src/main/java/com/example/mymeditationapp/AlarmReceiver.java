package com.example.mymeditationapp;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.os.Build;
import android.util.Log;

import java.util.Date;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String reminder = intent.getStringExtra("reminder");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Meditation Channel";
            String description = "Channel for meditation notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("meditation", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        switch(reminder){
            case "daily":
                showDailyNotification(context);
                break;
            case "meditation":
                showMeditationNotification(context);
                break;
        }
        Log.d("AlarmReceiver", "Alarm received");
    }

    public void showMeditationNotification(Context context){
        Intent intent = new Intent(context, ViewMeditationSessionsActivity.class);
        intent.putExtra("date", new Date());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //Create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "meditation")
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Meditation Session")
                .setContentText("Time to do your meditation session before it's too late!")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent); //Sets the destination intent when notification is clicked

        //Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(0, builder.build());
            Log.d("AlarmReceiver", "Notification shown");
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.VIBRATE}, 123);
            Log.d("AlarmReceiver", "Vibrate permission not granted");
        }
    }

    public void showDailyNotification(Context context){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "meditation")
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Meditation Reminder")
                .setContentText("It's time to take a break and do your meditation practice!")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
            Log.d("AlarmReceiver", "Notification shown");
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.VIBRATE}, 123);
            Log.d("AlarmReceiver", "Vibrate permission not granted");
        }
    }
}
