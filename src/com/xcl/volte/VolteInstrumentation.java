package com.xcl.volte;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Method;

/**
 * 特权执行体。由 shell（Shizuku 或 adb）用
 *   am instrument -w --no-restart -e action on -e sub 5 com.xcl.volte/.VolteInstrumentation
 * 拉起，跑在本应用自己的进程/uid 里。
 *
 * 为什么能过：
 *  - CarrierConfigLoader.secureOverrideConfig 第一层查的是 Binder.getCallingUid() 是否为 shell(2000)。
 *    instrumentation 跑在 app uid，所以不触发；Shizuku 直接调则必被拦。
 *  - MODIFY_PHONE_STATE 由 UiAutomation.adoptShellPermissionIdentity() 借 shell 的授权表满足。
 *  - persistent 必须为 false（第三层要求 isSystemApp），所以用 2 参重载，重启后失效。
 */
public class VolteInstrumentation extends Instrumentation {

    static final String TAG = "VolteToggle";
    public static final String ACTION_RESULT = "com.xcl.volte.RESULT";

    @Override
    public void onCreate(final Bundle args) {
        super.onCreate(args);
        final String action = arg(args, "action", "on");
        final int subId = Integer.parseInt(arg(args, "sub", "-1"));
        final int slot = Integer.parseInt(arg(args, "slot", "-1"));
        final boolean vonr = "true".equals(arg(args, "vonr", "true"));
        final boolean sa = "true".equals(arg(args, "sa", "false"));
        final boolean vowifi = "true".equals(arg(args, "vowifi", "false"));

        new Thread(new Runnable() {
            @Override public void run() {
                Bundle out = new Bundle();
                String msg;
                try {
                    msg = work(action, subId, slot, vonr, sa, vowifi);
                    out.putString("result", "ok:" + msg);
                } catch (Throwable t) {
                    Log.e(TAG, "failed", t);
                    msg = t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage());
                    out.putString("result", "err:" + msg);
                }
                broadcast(msg);
                finish(Activity.RESULT_OK, out);
            }
        }).start();
    }

    private String work(String action, int subId, int slot,
                        boolean vonr, boolean sa, boolean vowifi) throws Exception {
        try {
            HiddenApiBypass.addHiddenApiExemptions("L");
        } catch (Throwable t) {
            Log.w(TAG, "HiddenApiBypass failed, continuing", t);
        }

        UiAutomation ua = awaitUiAutomation();
        if (ua == null) throw new IllegalStateException("UiAutomation unavailable (must be started via am instrument)");
        ua.adoptShellPermissionIdentity();
        try {
            Context ctx = getContext();
            CarrierConfigManager ccm = ctx.getSystemService(CarrierConfigManager.class);

            // 明确取 2 参重载 -> persistent=false，避开 "only can be invoked by system app"
            Method override = CarrierConfigManager.class.getMethod(
                    "overrideConfig", int.class, PersistableBundle.class);

            PersistableBundle values = "off".equals(action)
                    ? null                                    // null = 清空覆盖，回到运营商默认
                    : Config.build(vonr, sa, vowifi);
            override.invoke(ccm, subId, values);

            resetIms(ctx, slot);

            boolean want = !"off".equals(action);
            boolean ims = awaitIms(subId, want);
            if (L.zh(ctx)) return (want ? "已开启" : "已关闭") + "，IMS " + (ims ? "已注册" : "未注册");
            return (want ? "enabled" : "disabled") + ", IMS " + (ims ? "registered" : "not registered");
        } finally {
            try { ua.dropShellPermissionIdentity(); } catch (Throwable ignored) {}
        }
    }

    /** 让 IMS 栈重新注册，立即生效而不用重启手机。 */
    private void resetIms(Context ctx, int slot) {
        try {
            TelephonyManager tm = ctx.getSystemService(TelephonyManager.class);
            Method m = TelephonyManager.class.getMethod("resetIms", int.class);
            m.invoke(tm, slot);
        } catch (Throwable t) {
            Log.w(TAG, "resetIms failed (非致命)", t);
        }
    }

    /** 等 IMS 注册状态稳定，最多 25 秒。 */
    private boolean awaitIms(int subId, boolean want) {
        long deadline = System.currentTimeMillis() + 25_000L;
        boolean cur = isImsRegistered(subId);
        while (System.currentTimeMillis() < deadline) {
            cur = isImsRegistered(subId);
            if (cur == want) break;
            try { Thread.sleep(1000L); } catch (InterruptedException e) { break; }
        }
        return cur;
    }

    private boolean isImsRegistered(int subId) {
        try {
            Class<?> sm = Class.forName("android.os.ServiceManager");
            IBinder b = (IBinder) sm.getMethod("getService", String.class).invoke(null, "phone");
            if (b == null) return false;
            Class<?> stub = Class.forName("com.android.internal.telephony.ITelephony$Stub");
            Object phone = stub.getMethod("asInterface", IBinder.class).invoke(null, b);
            Class<?> itel = Class.forName("com.android.internal.telephony.ITelephony");
            Object r = itel.getMethod("isImsRegistered", int.class).invoke(phone, subId);
            return Boolean.TRUE.equals(r);
        } catch (Throwable t) {
            return false;
        }
    }

    private UiAutomation awaitUiAutomation() {
        for (int i = 0; i < 6; i++) {
            try {
                UiAutomation ua = getUiAutomation();
                if (ua != null) return ua;
            } catch (Throwable ignored) {}
            try { Thread.sleep(500L); } catch (InterruptedException e) { break; }
        }
        return null;
    }

    private void broadcast(String msg) {
        try {
            Intent i = new Intent(ACTION_RESULT).setPackage("com.xcl.volte").putExtra("msg", msg);
            getContext().sendBroadcast(i);
        } catch (Throwable ignored) {}
    }

    private static String arg(Bundle b, String k, String def) {
        if (b == null) return def;
        String v = b.getString(k);
        return (v == null || v.isEmpty()) ? def : v;
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        try { super.finish(resultCode, results); } catch (Throwable ignored) {}
    }
}
