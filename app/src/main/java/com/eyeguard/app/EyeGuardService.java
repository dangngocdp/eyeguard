package com.eyeguard.app;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
public class EyeGuardService extends Service {
    private static final String CHANNEL_ID = "EyeGuardChannel";
    private static final String CHANNEL_ALERT_ID = "EyeGuardAlert";
    private static final int NOTIF_ID = 1;
    private static final int NOTIF_ALERT_ID = 2;
    private static final int WARNING_SECONDS = 20 * 60;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int secondsOn = 0;
    private boolean warningShown = false;
    private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) { stopCounting(); }
            else if (Intent.ACTION_SCREEN_ON.equals(action)) { startCounting(); }
        }
    };
    private Runnable countRunnable = new Runnable() {
        @Override
        public void run() {
            if (!warningShown) {
                secondsOn++;
                updateNotification();
                if (secondsOn >= WARNING_SECONDS) { showWarning(); }
                else { handler.postDelayed(this, 1000); }
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);
        startCounting();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "RESET".equals(intent.getAction())) {
            cancelAlertNotification();
            startCounting();
        }
        startForeground(NOTIF_ID, buildForegroundNotification("Dang bao ve mat... 0:00"));
        return START_STICKY;
    }
    private void startCounting() {
        secondsOn = 0; warningShown = false;
        handler.removeCallbacks(countRunnable);
        handler.postDelayed(countRunnable, 1000);
        updateNotification();
    }
    private void stopCounting() {
        secondsOn = 0; warningShown = false;
        handler.removeCallbacks(countRunnable);
        cancelAlertNotification();
        updateNotificationText("Man hinh tat - Da reset");
    }
    private void showWarning() {
        warningShown = true;
        handler.removeCallbacks(countRunnable);
        updateNotificationText("Hay nghi mat!");
        sendHeadsUpNotification();
    }
    private void sendHeadsUpNotification() {
        PendingIntent pi = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        Notification alert = new Notification.Builder(this, CHANNEL_ALERT_ID)
            .setContentTitle("Relax Eyes!")
            .setContentText("Ban da nhin man hinh 20 phut. Hay nghi mat ngay!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setFullScreenIntent(pi, true)
            .build();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIF_ALERT_ID, alert);
    }
    private void cancelAlertNotification() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIF_ALERT_ID);
    }
    private void updateNotification() {
        updateNotificationText(String.format("Man hinh bat: %d:%02d / 20:00", secondsOn/60, secondsOn%60));
    }
    private void updateNotificationText(String text) {
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIF_ID, buildForegroundNotification(text));
    }
    private Notification buildForegroundNotification(String text) {
        PendingIntent pi = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Eye Guard").setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pi).setOngoing(true).build();
    }
    private void createNotificationChannels() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        NotificationChannel c1 = new NotificationChannel(CHANNEL_ID, "Eye Guard", NotificationManager.IMPORTANCE_LOW);
        c1.setSound(null, null);
        nm.createNotificationChannel(c1);
        NotificationChannel c2 = new NotificationChannel(CHANNEL_ALERT_ID, "Eye Guard Canh Bao", NotificationManager.IMPORTANCE_HIGH);
        c2.setShowBadge(true);
        nm.createNotificationChannel(c2);
    }
    @Override public void onDestroy() { super.onDestroy(); handler.removeCallbacks(countRunnable); unregisterReceiver(screenReceiver); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
