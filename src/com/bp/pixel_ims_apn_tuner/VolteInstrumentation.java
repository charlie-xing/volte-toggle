package com.bp.pixel_ims_apn_tuner;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.Process;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Privileged worker launched via:
 *   am instrument -w --no-restart -e action on -e sub 3 -e slot 0 -e apn true com.bp.pixel_ims_apn_tuner/.VolteInstrumentation
 *
 * Runs inside this app's own UID (!= shell 2000) to bypass CVE-2025-48617 isShell() checks,
 * while temporarily adopting shell permissions (MODIFY_PHONE_STATE, WRITE_APN_SETTINGS) via UiAutomation.
 *
 * Performs:
 *  1. Self-grants READ_PHONE_STATE (from vvb2060/Ims v3.1).
 *  2. Provisions persistent Internet + IMS APNs from CARRIER_DB (if matched & enabled).
 *  3. Writes Persistent VoLTE (KEY_VOIMS_OPT_IN_STATUS = 1 + setAdvancedCallingSettingEnabled).
 *  4. Overrides CarrierConfig (tries persistent=true first, falls back to persistent=false).
 *  5. Resets IMS stack on target slot and waits for IMS registration.
 */
public class VolteInstrumentation extends Instrumentation {

    static final String TAG = "PixelImsTuner";
    public static final String ACTION_RESULT = "com.bp.pixel_ims_apn_tuner.RESULT";

    @Override
    public void onCreate(final Bundle args) {
        super.onCreate(args);
        final String action = arg(args, "action", "on");
        final int subId = Integer.parseInt(arg(args, "sub", "-1"));
        final int slot = Integer.parseInt(arg(args, "slot", "-1"));
        final boolean volte = "true".equals(arg(args, "volte", "true"));
        final boolean vonr = "true".equals(arg(args, "vonr", "false"));
        final boolean sa = "true".equals(arg(args, "sa", "false"));
        final boolean vowifi = "true".equals(arg(args, "vowifi", "false"));
        final boolean apn = "true".equals(arg(args, "apn", "false"));
        final boolean wfcRoaming = "true".equals(arg(args, "wfc_roaming", "true"));
        final boolean vt = "true".equals(arg(args, "vt", "true"));
        final boolean ut = "true".equals(arg(args, "ut", "true"));
        final boolean crossSim = "true".equals(arg(args, "cross_sim", "true"));
        final boolean allowApn = "true".equals(arg(args, "allow_apn", "true"));
        final boolean ltePlus = "true".equals(arg(args, "lte_plus", "true"));
        final String s26UaArg = arg(args, "s26_ua", "");
        final boolean baseVolte = "true".equals(arg(args, "base_volte", "false"));
        final boolean baseVowifi = "true".equals(arg(args, "base_vowifi", "false"));
        final boolean waitIms = "true".equals(arg(args, "wait", "true"));
        final boolean notify = "true".equals(arg(args, "notify", "false"));
        final String plmnArg = arg(args, "plmn", "");

        new Thread(new Runnable() {
            @Override public void run() {
                Bundle out = new Bundle();
                String msg;
                boolean ok = false;
                try {
                    if ("boot_all".equals(action) || "apply_all".equals(action)) {
                        msg = workBatch("boot_all".equals(action), notify);
                        ok = true;
                        out.putString("result", "ok:" + msg);
                    } else {
                        WorkResult wr = work(action, subId, slot, volte, vonr, sa, vowifi, apn,
                                wfcRoaming, vt, ut, crossSim, allowApn, ltePlus, s26UaArg,
                                baseVolte, baseVowifi, waitIms, plmnArg);
                        msg = wr.msg;
                        ok = true;
                        out.putString("result", "ok:" + msg);
                        if (wr.ims != null) out.putString("ims", String.valueOf(wr.ims));
                        if (notify) {
                            Context ctx = getContext();
                            boolean isOff = "off".equals(action);
                            int titleRes = isOff ? R.string.detached_done_reset : R.string.detached_done_apply;
                            BootReceiver.notifyStatus(ctx, L.s(ctx, titleRes), msg, false);
                        }
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "failed", t);
                    msg = t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage());
                    out.putString("result", "err:" + msg);
                    if (notify) {
                        Context ctx = getContext();
                        BootReceiver.notifyStatus(ctx,
                                L.s(ctx, R.string.boot_notif_title_fail), msg, false);
                    }
                }
                if (!ok) Log.w(TAG, "Instrumentation finished with error: " + msg);
                broadcast(msg);
                finish(Activity.RESULT_OK, out);
            }
        }).start();
    }

    private String workBatch(boolean bootOnly, boolean notify) throws Exception {
        try {
            HiddenApiBypass.addHiddenApiExemptions("L");
        } catch (Throwable t) {
            Log.w(TAG, "HiddenApiBypass failed, continuing", t);
        }

        Context ctx = getContext();
        UiAutomation ua = adoptShellIdentityWithRetry(
                Manifest.permission.MODIFY_PHONE_STATE,
                Manifest.permission.WRITE_APN_SETTINGS,
                "android.permission.READ_PRIVILEGED_PHONE_STATE",
                "android.permission.GRANT_RUNTIME_PERMISSIONS");
        List<SubscriptionInfo> targets = new ArrayList<>();
        try {
            grantPhonePermission(ctx);
            SubscriptionManager sm = ctx.getSystemService(SubscriptionManager.class);
            List<SubscriptionInfo> all = sm != null ? sm.getActiveSubscriptionInfoList() : null;
            if (all != null) {
                SharedPreferences p = Config.prefs(ctx);
                for (SubscriptionInfo si : all) {
                    if (!bootOnly || p.getBoolean(Config.keyOn(si.getSubscriptionId()), false)) {
                        targets.add(si);
                    }
                }
            }
        } finally {
            try { ua.dropShellPermissionIdentity(); } catch (Throwable ignored) {}
        }

        if (targets.isEmpty()) {
            return "no target SIMs";
        }

        SharedPreferences p = Config.prefs(ctx);
        StringBuilder summary = new StringBuilder();
        boolean allOk = true;
        for (SubscriptionInfo si : targets) {
            int sub = si.getSubscriptionId();
            int rawSlot = si.getSimSlotIndex();
            int resolvedSlot = ImsQueryTool.resolveSlot(ctx, sub, rawSlot);
            CharSequence name = si.getDisplayName();
            if (name == null || name.length() == 0) name = si.getCarrierName();
            if (name == null || name.length() == 0) name = "sub " + sub;
            String label = resolvedSlot >= 0 ? ("SIM " + (resolvedSlot + 1) + " (" + name + ")") : String.valueOf(name);

            boolean apn = !bootOnly && p.getBoolean(Config.keyApn(sub), false);
            boolean volte = p.getBoolean(Config.keyVolte(sub), true);
            boolean vonr = p.getBoolean(Config.keyVonr(sub), false);
            boolean sa = p.getBoolean(Config.keySa(sub), false);
            boolean vowifi = p.getBoolean(Config.keyVowifi(sub), false);
            boolean wfcRoaming = p.getBoolean(Config.keyWfcRoaming(sub), true);
            boolean vt = p.getBoolean(Config.keyVt(sub), true);
            boolean ut = p.getBoolean(Config.keyUt(sub), true);
            boolean crossSim = p.getBoolean(Config.keyCrossSim(sub), true);
            boolean allowApn = p.getBoolean(Config.keyAllowApn(sub), true);
            boolean ltePlus = p.getBoolean(Config.keyLtePlus(sub), true);
            String s26UaArg = p.contains(Config.keyS26Ua(sub))
                    ? String.valueOf(p.getBoolean(Config.keyS26Ua(sub), true)) : "";
            boolean baseVolte = p.getBoolean(Config.keyBaseVolte(sub), false);
            boolean baseVowifi = p.getBoolean(Config.keyBaseVowifi(sub), false);

            try {
                WorkResult wr = work("on", sub, resolvedSlot, volte, vonr, sa, vowifi, apn,
                        wfcRoaming, vt, ut, crossSim, allowApn, ltePlus, s26UaArg,
                        baseVolte, baseVowifi, true, "");
                if (summary.length() > 0) summary.append('\n');
                summary.append(label).append(": ").append(wr.msg);
            } catch (Throwable t) {
                allOk = false;
                if (summary.length() > 0) summary.append('\n');
                summary.append(label).append(": ").append(t.getClass().getSimpleName());
            }
        }

        if (notify) {
            int titleRes = bootOnly
                    ? (allOk ? R.string.boot_notif_title_ok : R.string.boot_notif_title_fail)
                    : (allOk ? R.string.detached_done_apply : R.string.boot_notif_title_fail);
            BootReceiver.notifyStatus(ctx, L.s(ctx, titleRes), summary.toString(), false);
        }
        return summary.toString();
    }

    private static final class WorkResult {
        final String msg;
        final Boolean ims;
        WorkResult(String msg, Boolean ims) { this.msg = msg; this.ims = ims; }
    }

    private WorkResult work(String action, int subId, int slot,
                            boolean volte, boolean vonr, boolean sa, boolean vowifi, boolean apn,
                            boolean wfcRoaming, boolean vt, boolean ut, boolean crossSim,
                            boolean allowApn, boolean ltePlus, String s26UaArg,
                            boolean baseVolte, boolean baseVowifi, boolean waitIms, String plmnArg) throws Exception {
        try {
            HiddenApiBypass.addHiddenApiExemptions("L");
        } catch (Throwable t) {
            Log.w(TAG, "HiddenApiBypass failed, continuing", t);
        }

        UiAutomation ua = adoptShellIdentityWithRetry(
                Manifest.permission.MODIFY_PHONE_STATE,
                Manifest.permission.WRITE_APN_SETTINGS,
                "android.permission.READ_PRIVILEGED_PHONE_STATE",
                "android.permission.GRANT_RUNTIME_PERMISSIONS");
        try {
            Context ctx = getContext();
            boolean want = !"off".equals(action);
            int resolvedSlot = ImsQueryTool.resolveSlot(ctx, subId, slot);

            // 1. Auto-grant READ_PHONE_STATE so UI can read SIMs & CarrierConfig without extra prompts
            grantPhonePermission(ctx);

            // 2. Provision all operator APNs from CARRIER_DB (when applying), or restore defaults on reset
            String apnSummary = "";
            String plmn = plmnArg.isEmpty() ? resolvePlmn(ctx, subId) : plmnArg;
            CarrierDb.Entry entry = CarrierDb.lookup(ctx, plmn);
            if (want && apn && entry != null) {
                ApnProvisioner.Result r = ApnProvisioner.provision(ctx, subId, entry);
                if (r.provisioned) {
                    apnSummary = " · APN " + entry.mnoName + " (" + r.totalCount + " APNs, " + entry.ratMode + ")";
                }
            } else if (!want && entry != null) {
                ApnProvisioner.restoreDefaultApns(ctx, subId);
            }

            // 3. Persistent VoLTE / IMS opt-in & VoWiFi settings in siminfo / ProvisioningManager
            // KEY_VOIMS_OPT_IN_STATUS forces VoLTE on at the platform level, so tie it strictly to `volte`.
            // setAdvancedCallingSettingEnabled(true) is required by Shannon IMS as the master IMS gate
            // even for VoWiFi-only (otherwise ImsConnectivityMgr.turnOnIms ignores with "VoLTE toggle is OFF").
            boolean wantIms = volte || vowifi || vonr;
            boolean optInOk = false;
            if (want) {
                if (volte) {
                    optInOk = setPersistentVolte(subId, true, true);
                } else if (wantIms) {
                    setPersistentVolte(subId, false, true);
                } else if (!baseVolte) {
                    setPersistentVolte(subId, false, false);
                }
                if (vowifi) {
                    setPersistentVowifi(subId, true, wfcRoaming);
                } else if (!baseVowifi) {
                    setPersistentVowifi(subId, false, false);
                }
            } else {
                if (!baseVolte) {
                    setPersistentVolte(subId, false, false);
                }
                if (!baseVowifi) {
                    setPersistentVowifi(subId, false, false);
                }
            }

            // 4. CarrierConfig override (try persistent=true first, fall back to persistent=false)
            boolean useS26Ua = s26UaArg.isEmpty() ? (entry != null) : "true".equals(s26UaArg);
            String imsUserAgent = (want && useS26Ua)
                    ? (entry != null ? entry.imsUserAgent : CarrierDb.SAMSUNG_S26_IMS_UA)
                    : null;
            CarrierConfigManager ccm = ctx.getSystemService(CarrierConfigManager.class);
            PersistableBundle values = want
                    ? Config.build(volte, vonr, sa, vowifi, wfcRoaming, vt, ut, crossSim,
                            allowApn, ltePlus, imsUserAgent)
                    : null;
            boolean persistent = applyCarrierConfig(ccm, subId, values);

            // Verify applied config version when enabling, and persist SIM applied state in prefs
            if (want) {
                if (subId > 0) {
                    Config.prefs(ctx).edit().putBoolean(Config.keyOn(subId), true).apply();
                }
                try {
                    PersistableBundle verify = ccm.getConfigForSubId(subId, Config.CONFIG_VERSION_KEY);
                    if (verify != null && verify.getInt(Config.CONFIG_VERSION_KEY, 0) == Config.CONFIG_VERSION) {
                        Log.i(TAG, "overrideConfig verified for subId=" + subId + ", persistent=" + persistent);
                    }
                } catch (Throwable ignored) {}
            } else if (subId > 0) {
                Config.clearSimPrefs(ctx, subId);
            }

            // 5. Reset IMS stack and narrow permissions to read-only before optional status wait
            if (resolvedSlot >= 0) {
                resetIms(ctx, subId, resolvedSlot);
            }
            try {
                ua.adoptShellPermissionIdentity("android.permission.READ_PRIVILEGED_PHONE_STATE");
            } catch (Throwable ignored) {}

            Boolean ims = null;
            if (want) {
                if (waitIms) {
                    boolean expectIms = volte || vowifi || vonr;
                    ims = awaitIms(subId, resolvedSlot, expectIms, 15_000L);
                }
            } else if (!baseVolte && !baseVowifi) {
                // On Reset for non-IMS stock profiles, wait briefly for deregistration
                ims = awaitIms(subId, resolvedSlot, false, 5_000L);
            } else {
                // On Reset for stock-IMS profiles (e.g. roaming / no-service eSIM), do not block 25s
                try { Thread.sleep(1000L); } catch (InterruptedException ignored) {}
                ims = ImsQueryTool.checkImsRegistered(subId, resolvedSlot);
            }

            String base = (ims != null)
                    ? L.s(ctx, R.string.result_fmt,
                            L.s(ctx, want ? R.string.st_on : R.string.st_off),
                            L.s(ctx, ims ? R.string.ims_reg : R.string.ims_unreg))
                    : L.s(ctx, want ? R.string.st_on : R.string.st_off);
            StringBuilder details = new StringBuilder(base);
            if (want) {
                details.append(persistent ? " [persistent]" : (optInOk ? " [VoLTE opt-in]" : ""));
                details.append(apnSummary);
            }
            return new WorkResult(details.toString(), ims);
        } finally {
            try { ua.dropShellPermissionIdentity(); } catch (Throwable ignored) {}
        }
    }

    private void grantPhonePermission(Context ctx) {
        try {
            Object pm = ctx.getSystemService("permission");
            if (pm != null) {
                Method m = pm.getClass().getMethod(
                        "grantRuntimePermission", String.class, String.class, android.os.UserHandle.class);
                m.invoke(pm, ctx.getPackageName(), Manifest.permission.READ_PHONE_STATE, Process.myUserHandle());
            }
        } catch (Throwable t) {
            Log.w(TAG, "grantPhonePermission skipped: " + t.getMessage());
        }
    }

    private String resolvePlmn(Context ctx, int subId) {
        try {
            SubscriptionManager sm = ctx.getSystemService(SubscriptionManager.class);
            if (sm != null) {
                SubscriptionInfo si = sm.getActiveSubscriptionInfo(subId);
                if (si != null) {
                    return CarrierDb.resolvePlmn(ctx, si);
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "resolvePlmn failed for subId=" + subId, t);
        }
        return CarrierDb.resolvePlmn(ctx, subId, "", "");
    }

    /**
     * Writes KEY_VOIMS_OPT_IN_STATUS (68) via ProvisioningManager and updates
     * ImsMmTelManager.setAdvancedCallingSettingEnabled() in telephony.db (siminfo).
     * Decoupled so VoWiFi-only mode can keep Advanced Calling enabled for Shannon IMS
     * without forcing VoLTE opt-in (VOICE_OVER_LTE=1) on cellular.
     */
    private boolean setPersistentVolte(int subId, boolean optIn, boolean advCalling) {
        try {
            Class<?> pmClass = Class.forName("android.telephony.ims.ProvisioningManager");
            Object pm = pmClass.getMethod("createForSubscriptionId", int.class).invoke(null, subId);
            int keyOptIn = pmClass.getField("KEY_VOIMS_OPT_IN_STATUS").getInt(null);
            int targetVal = optIn ? 1 : 0;

            Method getVal = pmClass.getMethod("getProvisioningIntValue", int.class);
            Method setVal = pmClass.getMethod("setProvisioningIntValue", int.class, int.class);
            int curVal = (Integer) getVal.invoke(pm, keyOptIn);
            if (curVal != targetVal) {
                int rc = (Integer) setVal.invoke(pm, keyOptIn, targetVal);
                Log.i(TAG, "setProvisioningIntValue(KEY_VOIMS_OPT_IN_STATUS=" + targetVal + ") rc=" + rc);
            }

            Class<?> mmTelClass = Class.forName("android.telephony.ims.ImsMmTelManager");
            Object mmTel = mmTelClass.getMethod("createForSubscriptionId", int.class).invoke(null, subId);
            mmTelClass.getMethod("setAdvancedCallingSettingEnabled", boolean.class).invoke(mmTel, advCalling);
            return optIn;
        } catch (Throwable t) {
            Log.w(TAG, "setPersistentVolte non-fatal failure: " + t.getMessage());
            return false;
        }
    }

    /**
     * Enables VoWiFi and VoWiFi Roaming in siminfo via ImsMmTelManager and ProvisioningManager
     * (keys 27 = KEY_VOICE_OVER_WIFI_ENABLED_OVERRIDE, 26 = KEY_VOICE_OVER_WIFI_ROAMING_ENABLED_OVERRIDE).
     */
    private void setPersistentVowifi(int subId, boolean enable, boolean enableRoaming) {
        boolean roam = enable && enableRoaming;
        try {
            Class<?> mmTelClass = Class.forName("android.telephony.ims.ImsMmTelManager");
            Object mmTel = mmTelClass.getMethod("createForSubscriptionId", int.class).invoke(null, subId);
            mmTelClass.getMethod("setVoWiFiSettingEnabled", boolean.class).invoke(mmTel, enable);
            mmTelClass.getMethod("setVoWiFiRoamingSettingEnabled", boolean.class).invoke(mmTel, roam);
        } catch (Throwable t) {
            Log.w(TAG, "setVoWiFiSettingEnabled non-fatal failure: " + t.getMessage());
        }
        try {
            Class<?> pmClass = Class.forName("android.telephony.ims.ProvisioningManager");
            Object pm = pmClass.getMethod("createForSubscriptionId", int.class).invoke(null, subId);
            Method setVal = pmClass.getMethod("setProvisioningIntValue", int.class, int.class);
            setVal.invoke(pm, 27, enable ? 1 : 0); // KEY_VOICE_OVER_WIFI_ENABLED_OVERRIDE
            setVal.invoke(pm, 26, roam ? 1 : 0);   // KEY_VOICE_OVER_WIFI_ROAMING_ENABLED_OVERRIDE
        } catch (Throwable t) {
            Log.w(TAG, "setPersistentVowifi provisioning non-fatal failure: " + t.getMessage());
        }
    }

    /**
     * Clears any previous override first when applying non-null values (because AOSP
     * CarrierConfigLoader.overrideConfig merges non-null bundles via putAll instead of replacing),
     * then attempts persistent=true first (Android 12-15 / pre-QPR2 16) with fallback to
     * persistent=false on Android 16 QPR2+ / 17.
     */
    private boolean applyCarrierConfig(CarrierConfigManager ccm, int subId, PersistableBundle values) throws Exception {
        if (values != null) {
            try {
                invokeOverrideConfig(ccm, subId, null);
            } catch (Throwable ignored) {}
        }
        return invokeOverrideConfig(ccm, subId, values);
    }

    private boolean invokeOverrideConfig(CarrierConfigManager ccm, int subId, PersistableBundle values) throws Exception {
        try {
            Method override3 = CarrierConfigManager.class.getMethod(
                    "overrideConfig", int.class, PersistableBundle.class, boolean.class);
            try {
                override3.invoke(ccm, subId, values, true);
                return true;
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof SecurityException) {
                    Log.i(TAG, "persistent=true rejected by system; falling back to persistent=false");
                    override3.invoke(ccm, subId, values, false);
                    return false;
                }
                throw e;
            }
        } catch (NoSuchMethodException e) {
            Method override2 = CarrierConfigManager.class.getMethod(
                    "overrideConfig", int.class, PersistableBundle.class);
            override2.invoke(ccm, subId, values);
            return false;
        }
    }

    /** Trigger immediate IMS re-registration via TelephonyManager, with disableIms/enableIms fallback. */
    private void resetIms(Context ctx, int subId, int slot) {
        try {
            TelephonyManager tm = ctx.getSystemService(TelephonyManager.class);
            if (tm != null) {
                TelephonyManager subTm = subId > 0 ? tm.createForSubscriptionId(subId) : tm;
                if (subTm == null) subTm = tm;
                Method m = null;
                for (Method cand : subTm.getClass().getDeclaredMethods()) {
                    if ("resetIms".equals(cand.getName())) {
                        m = cand;
                        if (cand.getParameterTypes().length == 1 && cand.getParameterTypes()[0] == int.class) {
                            break;
                        }
                    }
                }
                if (m == null) {
                    m = TelephonyManager.class.getMethod("resetIms", int.class);
                }
                m.setAccessible(true);
                if (m.getParameterTypes().length == 1) {
                    m.invoke(subTm, slot);
                } else {
                    m.invoke(subTm);
                }
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "TelephonyManager.resetIms failed, trying disableIms/enableIms fallback", t);
        }
        try {
            Class<?> sm = Class.forName("android.os.ServiceManager");
            IBinder b = (IBinder) sm.getMethod("getService", String.class).invoke(null, "phone");
            if (b == null) return;
            Object phone = Class.forName("com.android.internal.telephony.ITelephony$Stub")
                    .getMethod("asInterface", IBinder.class).invoke(null, b);
            Class<?> itel = Class.forName("com.android.internal.telephony.ITelephony");
            itel.getMethod("disableIms", int.class).invoke(phone, slot);
            itel.getMethod("enableIms", int.class).invoke(phone, slot);
        } catch (Throwable t) {
            Log.w(TAG, "disableIms/enableIms fallback failed (non-fatal)", t);
        }
    }

    /**
     * Wait up to timeoutMs for IMS registration state to match target.
     * When expecting registration after resetIms(), avoids returning early on a stale
     * pre-reset `true` by waiting until either a drop-and-reregister is seen or >= 5s elapse.
     */
    private boolean awaitIms(int subId, int slot, boolean want, long timeoutMs) {
        long start = System.currentTimeMillis();
        long deadline = start + timeoutMs;
        boolean sawUnregistered = false;
        boolean cur = false;
        while (System.currentTimeMillis() < deadline) {
            cur = ImsQueryTool.checkImsRegistered(subId, slot);
            if (!cur) sawUnregistered = true;
            long elapsed = System.currentTimeMillis() - start;
            if (want) {
                if (cur && (sawUnregistered || elapsed >= 5_000L)) break;
            } else {
                if (!cur) break;
            }
            try { Thread.sleep(1000L); } catch (InterruptedException e) { break; }
        }
        return cur;
    }

    private UiAutomation adoptShellIdentityWithRetry(String... permissions) {
        // Keep user's Accessibility Services (password managers, gestures, etc.) running and skip
        // creating an accessibility event bridge on Android 13+ — we only need shell permissions.
        int flags = UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES;
        if (Build.VERSION.SDK_INT >= 33) {
            flags |= UiAutomation.FLAG_DONT_USE_ACCESSIBILITY;
        }
        IllegalStateException last = null;
        for (int i = 0; i < 10; i++) {
            try {
                UiAutomation ua = getUiAutomation(flags);
                if (ua == null && flags != UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES) {
                    ua = getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
                }
                if (ua != null) {
                    ua.adoptShellPermissionIdentity(permissions);
                    return ua;
                }
            } catch (IllegalStateException e) {
                last = e;
            } catch (Throwable ignored) {}
            try { Thread.sleep(500L); } catch (InterruptedException e) { break; }
        }
        if (last != null) throw last;
        throw new IllegalStateException("UiAutomation unavailable (must be started via am instrument)");
    }

    private void broadcast(String msg) {
        try {
            Intent i = new Intent(ACTION_RESULT).setPackage("com.bp.pixel_ims_apn_tuner").putExtra("msg", msg);
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
