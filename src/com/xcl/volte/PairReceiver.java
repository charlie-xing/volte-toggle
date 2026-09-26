package com.xcl.volte;

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
 * 接收配对码。系统的「使用配对码配对设备」对话框必须一直开着，所以配对码要从
 * 通知栏的直接回复里输入（下拉通知栏不会让那个对话框退到后台）。
 */
public class PairReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.xcl.volte.PAIR";
    public static final String KEY_CODE = "code";
    private static final String CHANNEL = "pair";
    private static final int NOTIF_ID = 42;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String code = null;
        Bundle remote = android.app.RemoteInput.getResultsFromIntent(intent);
        if (remote != null) {
            CharSequence cs = remote.getCharSequence(KEY_CODE);
            if (cs != null) code = cs.toString().trim();
        }
        if (code == null) code = intent.getStringExtra(KEY_CODE);
        if (intent.hasExtra("prompt")) { prompt(context); return; }
        if (code == null || code.trim().isEmpty()) return;

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
                post(app, msg, false);
                toast(app, msg);
                pr.finish();
            }
        }).start();
    }

    /** 配对结果用 Toast 再提示一次，通知栏还开着时也能立刻看到。 */
    private static void toast(final Context ctx, final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override public void run() {
                try { Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show(); }
                catch (Throwable ignored) {}
            }
        });
    }

    /** 通道已就绪时清掉配对通知。 */
    public static void dismiss(Context ctx) {
        try { ctx.getSystemService(NotificationManager.class).cancel(NOTIF_ID); }
        catch (Throwable ignored) {}
    }

    /** 弹出带「直接回复」输入框的通知，让用户在系统配对对话框开着时填码。 */
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
        }
        nm.notify(NOTIF_ID, b.build());
    }
}
