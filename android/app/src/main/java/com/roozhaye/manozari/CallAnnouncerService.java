package com.roozhaye.manozari;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class CallAnnouncerService extends Service {
    public static final String CHANNEL_ID = "rmz_call_announcer_channel";
    public static final int NOTIF_ID = 7002;
    public static final String EXTRA_TEXT = "spoken_text";
    public static final String EXTRA_TITLE = "notif_title";
    public static final String EXTRA_IS_MALE = "is_male";
    public static final String EXTRA_REPEAT = "repeat_count";
    public static final String EXTRA_IS_CALL = "is_call";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean cancelled = false;

    public static void announce(Context context, String title, String text, boolean isMale, int repeatCount, boolean isCall) {
        Intent intent = new Intent(context, CallAnnouncerService.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_TEXT, text);
        intent.putExtra(EXTRA_IS_MALE, isMale);
        intent.putExtra(EXTRA_REPEAT, Math.max(1, Math.min(2, repeatCount)));
        intent.putExtra(EXTRA_IS_CALL, isCall);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopActive(Context context) {
        context.stopService(new Intent(context, CallAnnouncerService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "اعلام صوتی تماس و پیام‌ها",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cancelled = false;
        String title = intent != null ? intent.getStringExtra(EXTRA_TITLE) : "اعلام صوتی";
        String text = intent != null ? intent.getStringExtra(EXTRA_TEXT) : "";
        boolean isMale = intent != null && intent.getBooleanExtra(EXTRA_IS_MALE, false);
        int repeat = intent != null ? intent.getIntExtra(EXTRA_REPEAT, 1) : 1;
        boolean isCall = intent != null && intent.getBooleanExtra(EXTRA_IS_CALL, false);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title != null ? title : "روزهای من و زری")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIF_ID, notification);
        }

        if (text == null || text.trim().isEmpty()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        final String spoken = repeat == 2 ? (text + " ... " + text) : text;
        handler.postDelayed(() -> {
            if (cancelled) return;
            BilingualSpeechHelper.speak(this, spoken, isMale, 1.0f, isCall, success -> stopSelf());
        }, isCall ? 450 : 150);

        // Safety stop after 35s
        handler.postDelayed(this::stopSelf, 35000);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        cancelled = true;
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
