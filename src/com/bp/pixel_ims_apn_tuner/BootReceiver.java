package com.bp.pixel_ims_apn_tuner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Schedules {@link BootReapplyJobService} on `BOOT_COMPLETED` when "Re-apply after boot" is enabled
 * so CarrierConfig overrides are automatically replayed as soon as a Wi-Fi connection is available
 * (including automatic recovery of stuck Wireless Debugging via `adb_wifi_enabled`).
 */
public class BootReceiver extends BroadcastReceiver {

    static final String CHANNEL = "boot_status";
    static final int NOTIF_ID = 43;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        if (!Config.prefs(context).getBoolean(Config.K_BOOT, false)) return;
        if (!Config.hasAnyEnabledSimPref(context)) return;
        if (Config.isBootAlreadyApplied(context)) return;

        Log.i(VolteInstrumentation.TAG, "BOOT_COMPLETED received: scheduling Wi-Fi boot reapply job");
        BootReapplyJobService.schedule(context);
    }

    static void notifyBoot(Context ctx, String title, String text) {
        notifyStatus(ctx, title, text, false);
    }

    static void notifyStatus(Context ctx, String title, String text, boolean ongoing) {
        try {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm == null) return;
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL, L.s(ctx, R.string.boot_notif_channel), NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(ch);

            Intent open = new Intent(ctx, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(ctx, 0, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Notification.Builder b = new Notification.Builder(ctx, CHANNEL)
                    .setSmallIcon(ongoing
                            ? android.R.drawable.stat_notify_sync
                            : android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setStyle(new Notification.BigTextStyle().bigText(text))
                    .setContentIntent(pi)
                    .setOngoing(ongoing)
                    .setAutoCancel(!ongoing);
            if (ongoing) {
                b.setProgress(0, 0, true);
            }
            nm.notify(NOTIF_ID, b.build());
        } catch (Throwable t) {
            Log.w(VolteInstrumentation.TAG, "notifyStatus failed: " + t.getMessage());
        }
    }
}
