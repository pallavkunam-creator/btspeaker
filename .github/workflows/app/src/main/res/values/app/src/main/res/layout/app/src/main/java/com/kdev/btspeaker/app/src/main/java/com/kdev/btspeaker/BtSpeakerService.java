package com.kdev.btspeaker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class BtSpeakerService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "bt_speaker_channel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            notification = buildNotificationOreo(pendingIntent);
        } else {
            notification = new Notification.Builder(this)
                .setContentTitle("BT Speaker Active")
                .setContentText("Phone is acting as Bluetooth speaker")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setContentIntent(pendingIntent)
                .build();
        }

        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    private Notification buildNotificationOreo(PendingIntent pendingIntent) {
        try {
            Class<?> builderClass = Class.forName("android.app.Notification$Builder");
            Object builder = builderClass.getConstructor(android.content.Context.class, String.class)
                .newInstance(this, CHANNEL_ID);
            builderClass.getMethod("setContentTitle", CharSequence.class).invoke(builder, "BT Speaker Active");
            builderClass.getMethod("setContentText", CharSequence.class).invoke(builder, "Phone is acting as Bluetooth speaker");
            builderClass.getMethod("setSmallIcon", int.class).invoke(builder, android.R.drawable.stat_sys_data_bluetooth);
            builderClass.getMethod("setContentIntent", PendingIntent.class).invoke(builder, pendingIntent);
            return (Notification) builderClass.getMethod("build").invoke(builder);
        } catch (Exception e) {
            return new Notification.Builder(this)
                .setContentTitle("BT Speaker Active")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .build();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Class<?> channelClass = Class.forName("android.app.NotificationChannel");
                Object channel = channelClass.getConstructor(String.class, CharSequence.class, int.class)
                    .newInstance(CHANNEL_ID, "BT Speaker", 3);
                Object manager = getSystemService(android.app.NotificationManager.class);
                manager.getClass().getMethod("createNotificationChannel", channelClass).invoke(manager, channel);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
