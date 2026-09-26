package com.xcl.volte;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import java.util.List;

/**
 * 开机后重放覆盖。carrier config 的 persistent 只能是 false（系统限制），
 * 重启后会回到运营商默认值，必须重新应用一次。
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        if (!Config.prefs(context).getBoolean(Config.K_BOOT, false)) return;

        final PendingResult pr = goAsync();
        final Context app = context.getApplicationContext();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    // 等网络/adbd 起来，最多约 50 秒
                    long deadline = System.currentTimeMillis() + 50_000L;
                    while (System.currentTimeMillis() < deadline && !Adb.check(app)) {
                        Thread.sleep(5000L);
                    }
                    SubscriptionManager sm = app.getSystemService(SubscriptionManager.class);
                    List<SubscriptionInfo> list = sm.getActiveSubscriptionInfoList();
                    if (list == null) return;
                    SharedPreferences pp = Config.prefs(app);
                    for (SubscriptionInfo si : list) {
                        int sub = si.getSubscriptionId();
                        if (!pp.getBoolean(Config.keyOn(sub), false)) continue;
                        Broker.Result r = Broker.apply(app, sub, si.getSimSlotIndex(), true);
                        Log.i(VolteInstrumentation.TAG,
                                "开机重放 sub=" + sub + " -> " + r.ok + " " + r.text);
                    }
                } catch (Throwable t) {
                    Log.e(VolteInstrumentation.TAG, "开机重放失败", t);
                } finally {
                    pr.finish();
                }
            }
        }).start();
    }
}
