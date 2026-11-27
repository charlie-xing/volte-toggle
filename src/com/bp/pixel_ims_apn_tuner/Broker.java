package com.bp.pixel_ims_apn_tuner;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs `am instrument` as shell via the built-in ADB client, delegating privileged work to
 * {@link VolteInstrumentation} (which runs under this app's own UID), and runs lightweight
 * `app_process` queries via {@link ImsQueryTool} for live IMS registration & effective CarrierConfig.
 */
public final class Broker {

    static final String TARGET = "com.bp.pixel_ims_apn_tuner/com.bp.pixel_ims_apn_tuner.VolteInstrumentation";

    public static class Result {
        public final boolean ok;
        public final String text;
        public final Boolean ims;
        public final boolean detached;
        Result(boolean ok, String text, Boolean ims) {
            this(ok, text, ims, false);
        }
        Result(boolean ok, String text, Boolean ims, boolean detached) {
            this.ok = ok;
            this.text = text;
            this.ims = ims;
            this.detached = detached;
        }
    }

    public static Result apply(Context ctx, int subId, int slot, boolean on) {
        return apply(ctx, subId, slot, on, null, true);
    }

    public static Result apply(Context ctx, int subId, int slot, boolean on, Boolean apnOverride) {
        return apply(ctx, subId, slot, on, apnOverride, true);
    }

    public static Result apply(Context ctx, int subId, int slot, boolean on,
                               Boolean apnOverride, boolean waitIms) {
        SharedPreferences p = Config.prefs(ctx);
        int resolvedSlot = ImsQueryTool.resolveSlot(ctx, subId, slot);
        boolean apn = apnOverride != null ? apnOverride : p.getBoolean(Config.keyApn(subId), false);
        boolean volte = p.getBoolean(Config.keyVolte(subId), true);
        boolean vonr = p.getBoolean(Config.keyVonr(subId), false);
        boolean sa = p.getBoolean(Config.keySa(subId), false);
        boolean vowifi = p.getBoolean(Config.keyVowifi(subId), false);
        boolean wfcRoaming = p.getBoolean(Config.keyWfcRoaming(subId), true);
        boolean vt = p.getBoolean(Config.keyVt(subId), true);
        boolean ut = p.getBoolean(Config.keyUt(subId), true);
        boolean crossSim = p.getBoolean(Config.keyCrossSim(subId), true);
        boolean allowApn = p.getBoolean(Config.keyAllowApn(subId), true);
        boolean ltePlus = p.getBoolean(Config.keyLtePlus(subId), true);
        String s26Ua = p.contains(Config.keyS26Ua(subId))
                ? String.valueOf(p.getBoolean(Config.keyS26Ua(subId), true)) : "";
        boolean baseVolte = p.getBoolean(Config.keyBaseVolte(subId), false);
        boolean baseVowifi = p.getBoolean(Config.keyBaseVowifi(subId), false);

        boolean detached = ApplyMode.isDetached();
        StringBuilder args = new StringBuilder("am instrument -w --no-hidden-api-checks");
        if (!detached && Build.VERSION.SDK_INT >= 34) {
            args.append(" --no-restart");
        }
        args.append(" -e action ").append(on ? "on" : "off")
            .append(" -e sub ").append(subId)
            .append(" -e slot ").append(resolvedSlot)
            .append(" -e apn ").append(apn)
            .append(" -e volte ").append(volte)
            .append(" -e vonr ").append(vonr)
            .append(" -e sa ").append(sa)
            .append(" -e vowifi ").append(vowifi)
            .append(" -e wfc_roaming ").append(wfcRoaming)
            .append(" -e vt ").append(vt)
            .append(" -e ut ").append(ut)
            .append(" -e cross_sim ").append(crossSim)
            .append(" -e allow_apn ").append(allowApn)
            .append(" -e lte_plus ").append(ltePlus);
        if (!s26Ua.isEmpty()) {
            args.append(" -e s26_ua ").append(s26Ua);
        }
        args.append(" -e base_volte ").append(baseVolte)
            .append(" -e base_vowifi ").append(baseVowifi)
            .append(" -e wait ").append(detached || waitIms)
            .append(" -e notify ").append(detached)
            .append(" ").append(TARGET);

        try {
            if (detached) {
                BootReceiver.notifyStatus(ctx,
                        L.s(ctx, R.string.detached_applying_title),
                        L.s(ctx, R.string.detached_applying_body),
                        true);
                String cmd = "nohup " + args + " >/dev/null 2>&1 &";
                Adb.Out out = Adb.exec(ctx, cmd);
                if (out.code != 0) {
                    return new Result(false, out.text.trim(), null, true);
                }
                return new Result(true, L.s(ctx, R.string.detached_applying_title), null, true);
            }

            Adb.Out out = Adb.exec(ctx, args.toString());
            if (Build.VERSION.SDK_INT >= 34 && out.text.contains("setActiveInstrumentation")) {
                out = Adb.exec(ctx, args.toString().replace(" --no-restart", ""));
            }
            Log.d(VolteInstrumentation.TAG, "am instrument:\n" + out.text);
            String line = pick(out.text, "INSTRUMENTATION_RESULT: result=");
            String imsLine = pick(out.text, "INSTRUMENTATION_RESULT: ims=");
            Boolean ims = imsLine != null ? Boolean.valueOf("true".equals(imsLine)) : null;
            if (line != null) {
                if (line.startsWith("ok:")) return new Result(true, line.substring(3), ims);
                return new Result(false, line.startsWith("err:") ? line.substring(4) : line, ims);
            }
            if (out.text.contains("INSTRUMENTATION_FAILED")) return new Result(false, out.text.trim(), null);
            return new Result(false, out.text.trim().isEmpty() ? L.s(ctx, R.string.err_no_result) : out.text.trim(), null);
        } catch (Throwable t) {
            String m = t.getMessage();
            return new Result(false, (m == null || m.isEmpty()) ? t.getClass().getSimpleName() : m, null);
        }
    }

    /**
     * Dispatches multi-SIM apply in a single detached VolteInstrumentation session on Android 12–13
     * so applying multiple SIMs cannot kill the caller mid-loop.
     */
    public static Result applyDetachedBatch(Context ctx, boolean bootOnly) {
        try {
            if (!bootOnly) {
                BootReceiver.notifyStatus(ctx,
                        L.s(ctx, R.string.detached_applying_title),
                        L.s(ctx, R.string.detached_applying_body),
                        true);
            }
            String action = bootOnly ? "boot_all" : "apply_all";
            String cmd = "nohup am instrument -w --no-hidden-api-checks"
                    + " -e action " + action
                    + " -e notify true "
                    + TARGET + " >/dev/null 2>&1 &";
            Adb.Out out = Adb.exec(ctx, cmd);
            if (out.code != 0) {
                return new Result(false, out.text.trim(), null, true);
            }
            return new Result(true, L.s(ctx, R.string.detached_applying_title), null, true);
        } catch (Throwable t) {
            String m = t.getMessage();
            return new Result(false, (m == null || m.isEmpty()) ? t.getClass().getSimpleName() : m, null, true);
        }
    }

    /**
     * Queries live IMS registration status and unfiltered effective CarrierConfig flags for active
     * SIMs via lightweight `app_process` without launching `am instrument`.
     */
    public static Map<Integer, ImsQueryTool.SlotStatus> querySlotStatus(Context ctx, List<SubscriptionInfo> subs) {
        Map<Integer, ImsQueryTool.SlotStatus> res = new HashMap<>();
        try {
            String apk = ctx.getApplicationInfo().sourceDir;
            if (apk == null || apk.isEmpty()) return res;
            StringBuilder cmd = new StringBuilder();
            cmd.append("CLASSPATH='").append(apk.replace("'", "'\\''"))
               .append("' app_process /system/bin ").append(ImsQueryTool.class.getName());
            if (subs != null) {
                for (SubscriptionInfo si : subs) {
                    int subId = si.getSubscriptionId();
                    int slot = ImsQueryTool.resolveSlot(ctx, subId, si.getSimSlotIndex());
                    cmd.append(' ').append(subId).append(':').append(slot);
                }
            }
            Adb.Out out = Adb.exec(ctx, cmd.toString());
            Map<Integer, Boolean> imsMap = new HashMap<>();
            Map<Integer, String> cfgMap = new HashMap<>();
            for (String line : out.text.split("\n")) {
                String s = line.trim();
                if (s.startsWith("IMS:")) {
                    String[] parts = s.split(":", 3);
                    if (parts.length == 3) {
                        try {
                            imsMap.put(Integer.parseInt(parts[1]), "true".equals(parts[2]));
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (s.startsWith("CFG:")) {
                    String[] parts = s.split(":", 3);
                    if (parts.length == 3) {
                        try {
                            cfgMap.put(Integer.parseInt(parts[1]), parts[2]);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            for (Map.Entry<Integer, Boolean> e : imsMap.entrySet()) {
                int subId = e.getKey();
                res.put(subId, ImsQueryTool.SlotStatus.parse(e.getValue(), cfgMap.get(subId)));
            }
        } catch (Throwable t) {
            Log.w(VolteInstrumentation.TAG, "querySlotStatus failed: " + t.getMessage());
        }
        return res;
    }

    /**
     * Self-grants READ_PHONE_STATE and WRITE_SECURE_SETTINGS (for Wireless Debugging auto-recovery
     * after reboot) when missing.
     */
    public static void grantSelfPermissions(Context ctx) {
        String pkg = ctx.getPackageName();
        String[] perms = new String[] {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_SECURE_SETTINGS
        };
        for (String perm : perms) {
            try {
                if (ctx.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                    Adb.exec(ctx, "pm grant " + pkg + " " + perm);
                }
            } catch (Throwable ignored) {}
        }
    }

    public static void grantPhonePermission(Context ctx) {
        grantSelfPermissions(ctx);
    }

    private static String pick(String out, String prefix) {
        for (String l : out.split("\n")) {
            String s = l.trim();
            if (s.startsWith(prefix)) return s.substring(prefix.length()).trim();
        }
        return null;
    }

    private Broker() {}
}
