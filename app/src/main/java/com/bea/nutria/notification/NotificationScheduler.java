package com.bea.nutria.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class NotificationScheduler {

    public static final long INTERVAL_MS = 15L * 60L * 1000L;

    public static void scheduleNext(Context ctx, long delayMs) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, NotificationReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAt = System.currentTimeMillis() + delayMs;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    setExactCompat(am, triggerAt, pi);
                } else {
                    setInexactCompat(am, triggerAt, pi);
                }
            } else {
                setExactCompat(am, triggerAt, pi);
            }
        } catch (SecurityException ignored) {
            setInexactCompat(am, triggerAt, pi);
        }
    }

    public static void startLoop(Context ctx) {
        scheduleNext(ctx, INTERVAL_MS);
    }

    public static void rescheduleAfterBoot(Context ctx) {
        scheduleNext(ctx, INTERVAL_MS);
    }

    private static void setExactCompat(AlarmManager am, long triggerAt, PendingIntent pi) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    private static void setInexactCompat(AlarmManager am, long triggerAt, PendingIntent pi) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }
}
