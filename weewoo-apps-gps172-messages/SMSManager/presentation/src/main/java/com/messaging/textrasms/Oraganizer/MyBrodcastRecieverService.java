package com.messaging.textrasms.Oraganizer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.messaging.textrasms.manager.R;
import com.messaging.textrasms.manager.receiver.SmsReceiver;

public class MyBrodcastRecieverService extends NotificationListenerService {
    private static final String TAG = "MyBrodcastReciever";
    public static Notification notification;
    private static SmsReceiver br_ScreenOffReceiver;
    String title = "App is running in background";
    NotificationManager manager;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;

    }

    @Override
    public void onCreate() {

        br_ScreenOffReceiver = new SmsReceiver();

    }

    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.messaging.textrasms.manager";
        String channelName = "SMS Messages..";
        NotificationChannel chan = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        }
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(chan);
        }

        title = "App is running in background";
        Intent intent = new Intent(this, MyBrodcastRecieverService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.messages_app_icon)
                .setContentTitle(title)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setAutoCancel(true)
                .setOngoing(false)
                .setDeleteIntent(pendingIntent)
                .build();

        notification.flags = Notification.FLAG_INSISTENT | Notification.FLAG_AUTO_CANCEL;
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        manager.notify(2, notification);
        //notification.flags == Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
        registerScreenOffReceiver();
        return START_STICKY;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG, "**********  onNotificationPosted");
        Log.i(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "********** onNOtificationRemoved");
        Log.i(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());


    }

    @Override
    public void onDestroy() {
        // unregisterReceiver(br_ScreenOffReceiver);
        //br_ScreenOffReceiver = null;
    }

    private void registerScreenOffReceiver() {

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(br_ScreenOffReceiver, filter);
        stopForeground(true);
        stopSelf();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
//        stopForeground(true);
//        stopSelf();


    }
}