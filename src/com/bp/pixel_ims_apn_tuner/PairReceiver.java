package com.bp.pixel_ims_apn_tuner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * Receives the pairing code. The system "Pair device with pairing code" dialog must remain
 * open, so the code is entered via direct reply in the notification shade (pulling down the
 * shade does not send that dialog to the background).
 */
public class PairReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.bp.pixel_ims_apn_tuner.PAIR";
    public static final String KEY_CODE = "code";
    private static final String CHANNEL = "pair";
    private static final int NOTIF_ID = 42;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent == null || !ACTION.equals(intent.getAction())) return;
        String code = null;
        Bundle remote = android.app.RemoteInput.getResultsFromIntent(intent);
        if (remote != null) {
            CharSequence cs = remote.getCharSequence(KEY_CODE);
            if (cs != null) code = cs.toString().trim();
        }
        if (code == null || code.isEmpty()) return;

        final String c = code.trim();
        final Context app = context.getApplicationContext();
        final PendingResult pr = goAsync();
        new Thread(new Runnable() {
            @Override public void run() {
                String msg;
                try {
                    Adb.pair(app, c);
                    msg = L.s(app, Adb.check(app) ? R.string.notif_paired : R.string.notif_no_conn);
                } catch (Throwable t) {
                    String m = t.getMessage();
                    msg = L.s(app, R.string.pair_failed,
                            (m == null || m.isEmpty()) ? t.getClass().getSimpleName() : m);
                }
                Log.i(VolteInstrumentation.TAG, "pair -> " + msg);
                Adb.closeIfNotKeepAlive();
                post(app, msg, false);
                toast(app, msg);
                pr.finish();
            }
        }).start();
    }

    /** Also shows the pairing result in a Toast so it is immediately visible while the shade is still open. */
    private static void toast(final Context ctx, final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override public void run() {
                try { Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show(); }
                catch (Throwable ignored) {}
            }
        });
    }

    /** Clears the pairing notification once the channel is ready. */
    public static void dismiss(Context ctx) {
        try { ctx.getSystemService(NotificationManager.class).cancel(NOTIF_ID); }
        catch (Throwable ignored) {}
    }

    /** Posts a notification with a direct-reply input so the user can enter the code while the system pairing dialog stays open. */
    public static void prompt(Context ctx) {
        post(ctx, L.s(ctx, R.string.notif_prompt), true);
    }

    private static void post(Context ctx, String text, boolean withInput) {
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        NotificationChannel ch = new NotificationChannel(
                CHANNEL, L.s(ctx, R.string.notif_channel), NotificationManager.IMPORTANCE_HIGH);
        nm.createNotificationChannel(ch);

        Notification.Builder b = new Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setContentTitle(L.s(ctx, R.string.notif_title))
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setOngoing(withInput)
                .setAutoCancel(!withInput);

        if (withInput) {
            android.app.RemoteInput ri = new android.app.RemoteInput.Builder(KEY_CODE)
                    .setLabel(L.s(ctx, R.string.code_hint)).build();
            Intent i = new Intent(ctx, PairReceiver.class).setAction(ACTION);
            PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            Notification.Action act = new Notification.Action.Builder(
                    android.R.drawable.ic_menu_send, L.s(ctx, R.string.notif_action), pi)
                    .addRemoteInput(ri).build();
            b.addAction(act);

            // Request full-screen intent with a no-op broadcast so SystemUI treats the
            // heads-up notification as sticky (60s+ instead of auto-hiding after 5s)
            // without closing the system Wireless Debugging pairing dialog.
            Intent noop = new Intent(ctx, PairReceiver.class).setAction(ACTION);
            PendingIntent fsi = PendingIntent.getBroadcast(ctx, 1, noop,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            b.setFullScreenIntent(fsi, true);
        }
        nm.notify(NOTIF_ID, b.build());
    }
}
