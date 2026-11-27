package com.bp.pixel_ims_apn_tuner;

import android.content.Context;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight CLI tool executed via `CLASSPATH=<apk> app_process /system/bin ...ImsQueryTool`
 * over local loopback ADB to query real-time IMS registration state and unfiltered effective
 * CarrierConfig flags via ICarrierConfigLoader (as shell) without launching `am instrument`.
 */
public final class ImsQueryTool {

    private static final String SHELL_PKG = "com.android.shell";

    private static final String[] EFFECTIVE_KEYS = new String[] {
            "carrier_volte_available_bool",
            "carrier_wfc_ims_available_bool",
            "vonr_enabled_bool",
            "carrier_nr_availabilities_int_array",
            "carrier_vt_available_bool",
            "carrier_wfc_ims_roaming_available_bool",
            "carrier_supports_ss_over_ut_bool",
            "carrier_cross_sim_ims_available_bool",
            "hide_lte_plus_data_icon_bool",
            "ims.ims_user_agent_string"
    };

    /** Live IMS registration and effective CarrierConfig flags read from ICarrierConfigLoader. */
    public static final class SlotStatus {
        public final boolean imsRegistered;
        public final boolean hasConfig;
        public final boolean volte;
        public final boolean vowifi;
        public final boolean vonr;
        public final boolean sa;
        public final boolean vt;
        public final boolean wfcRoaming;
        public final boolean ut;
        public final boolean crossSim;
        public final boolean ltePlus;
        public final boolean s26Ua;

        public SlotStatus(boolean imsRegistered, boolean hasConfig,
                          boolean volte, boolean vowifi, boolean vonr, boolean sa,
                          boolean vt, boolean wfcRoaming, boolean ut,
                          boolean crossSim, boolean ltePlus, boolean s26Ua) {
            this.imsRegistered = imsRegistered;
            this.hasConfig = hasConfig;
            this.volte = volte;
            this.vowifi = vowifi;
            this.vonr = vonr;
            this.sa = sa;
            this.vt = vt;
            this.wfcRoaming = wfcRoaming;
            this.ut = ut;
            this.crossSim = crossSim;
            this.ltePlus = ltePlus;
            this.s26Ua = s26Ua;
        }

        public static SlotStatus fromImsOnly(boolean imsRegistered) {
            return new SlotStatus(imsRegistered, false,
                    false, false, false, false, false, false, false, false, false, false);
        }

        public static SlotStatus parse(boolean imsRegistered, String cfgSpec) {
            if (cfgSpec == null || cfgSpec.isEmpty()) {
                return fromImsOnly(imsRegistered);
            }
            boolean volte = false, vowifi = false, vonr = false, sa = false;
            boolean vt = false, wfcRoaming = false, ut = false, crossSim = false, ltePlus = false, s26Ua = false;
            for (String item : cfgSpec.split(",")) {
                int eq = item.indexOf('=');
                if (eq <= 0) continue;
                String k = item.substring(0, eq).trim();
                boolean v = "1".equals(item.substring(eq + 1).trim());
                switch (k) {
                    case "volte":     volte = v; break;
                    case "vowifi":    vowifi = v; break;
                    case "vonr":      vonr = v; break;
                    case "sa":        sa = v; break;
                    case "vt":        vt = v; break;
                    case "wfc_roam":  wfcRoaming = v; break;
                    case "ut":        ut = v; break;
                    case "cross_sim": crossSim = v; break;
                    case "lte_plus":  ltePlus = v; break;
                    case "s26_ua":    s26Ua = v; break;
                }
            }
            return new SlotStatus(imsRegistered, true,
                    volte, vowifi, vonr, sa, vt, wfcRoaming, ut, crossSim, ltePlus, s26Ua);
        }

        /** Formats compact summary of features currently active in effective CarrierConfig. */
        public String formatActiveFeatures(String noneLabel) {
            List<String> tags = new ArrayList<>();
            if (volte) tags.add("VoLTE");
            if (vowifi) tags.add("VoWiFi");
            if (vonr) tags.add("VoNR");
            if (sa) tags.add("5G SA");
            if (vt) tags.add("ViLTE");
            if (wfcRoaming) tags.add("WFC Roam");
            if (ut) tags.add("UT");
            if (crossSim) tags.add("Cross-SIM");
            if (ltePlus) tags.add("LTE+");
            if (s26Ua) tags.add("S26 UA");
            if (tags.isEmpty()) return noneLabel;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tags.size(); i++) {
                if (i > 0) sb.append(" · ");
                sb.append(tags.get(i));
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        try {
            HiddenApiBypass.addHiddenApiExemptions("L");
        } catch (Throwable ignored) {}

        try {
            if (args != null && args.length > 0) {
                for (String a : args) {
                    if (a == null || a.isEmpty()) continue;
                    String[] parts = a.split(":");
                    int subId = Integer.parseInt(parts[0]);
                    int slot = parts.length > 1 ? Integer.parseInt(parts[1]) : -1;
                    emitSlotStatus(subId, slot);
                }
                return;
            }

            // Standalone mode (no args): query active subId for slots 0 and 1 via ISub
            Class<?> sm = Class.forName("android.os.ServiceManager");
            Method getService = sm.getMethod("getService", String.class);
            IBinder isubBinder = (IBinder) getService.invoke(null, "isub");
            if (isubBinder == null) return;
            Object isub = Class.forName("com.android.internal.telephony.ISub$Stub")
                    .getMethod("asInterface", IBinder.class).invoke(null, isubBinder);
            Class<?> isubClass = Class.forName("com.android.internal.telephony.ISub");

            Method getSubId = null;
            for (Method m : isubClass.getDeclaredMethods()) {
                Class<?>[] pt = m.getParameterTypes();
                if ("getSubId".equals(m.getName()) && pt.length == 1 && pt[0] == int.class) {
                    m.setAccessible(true);
                    getSubId = m;
                    break;
                }
            }
            if (getSubId == null) return;

            for (int slot = 0; slot <= 1; slot++) {
                int subId = -1;
                try {
                    Object res = getSubId.invoke(isub, slot);
                    if (res instanceof int[]) {
                        int[] arr = (int[]) res;
                        if (arr.length > 0) subId = arr[0];
                    } else if (res instanceof Integer) {
                        subId = (Integer) res;
                    }
                } catch (Throwable ignored) {}
                if (subId > 0 && subId != Integer.MAX_VALUE) {
                    emitSlotStatus(subId, slot);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    private static void emitSlotStatus(int subId, int slot) {
        boolean reg = checkImsRegistered(subId, slot);
        System.out.println("IMS:" + subId + ":" + reg);
        PersistableBundle cfg = readEffectiveCarrierConfig(subId);
        if (cfg != null) {
            System.out.println("CFG:" + subId + ":" + encodeEffectiveConfig(cfg));
        }
    }

    /**
     * Reads unfiltered effective CarrierConfig for subId via ICarrierConfigLoader as shell
     * (which holds READ_PRIVILEGED_PHONE_STATE).
     */
    public static PersistableBundle readEffectiveCarrierConfig(int subId) {
        if (subId <= 0 || subId == Integer.MAX_VALUE) return null;
        try {
            Class<?> sm = Class.forName("android.os.ServiceManager");
            IBinder b = (IBinder) sm.getMethod("getService", String.class).invoke(null, "carrier_config");
            if (b == null) return null;
            Object loader = Class.forName("com.android.internal.telephony.ICarrierConfigLoader$Stub")
                    .getMethod("asInterface", IBinder.class).invoke(null, b);
            Class<?> cls = Class.forName("com.android.internal.telephony.ICarrierConfigLoader");

            // 1. Android 14+ subset query (fastest: marshals only requested keys)
            try {
                Method m = cls.getMethod("getConfigSubsetForSubIdWithFeature",
                        int.class, String.class, String.class, String[].class);
                Object res = m.invoke(loader, subId, SHELL_PKG, null, EFFECTIVE_KEYS);
                if (res instanceof PersistableBundle) return (PersistableBundle) res;
            } catch (NoSuchMethodException ignored) {
            } catch (Throwable ignored) {}

            // 2. Android 12+ full config with feature attribution
            try {
                Method m = cls.getMethod("getConfigForSubIdWithFeature",
                        int.class, String.class, String.class);
                Object res = m.invoke(loader, subId, SHELL_PKG, null);
                if (res instanceof PersistableBundle) return (PersistableBundle) res;
            } catch (NoSuchMethodException ignored) {
            } catch (Throwable ignored) {}

            // 3. Legacy 2-arg fallback
            try {
                Method m = cls.getMethod("getConfigForSubId", int.class, String.class);
                Object res = m.invoke(loader, subId, SHELL_PKG);
                if (res instanceof PersistableBundle) return (PersistableBundle) res;
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        return null;
    }

    private static String encodeEffectiveConfig(PersistableBundle b) {
        boolean volte = b.getBoolean("carrier_volte_available_bool", false);
        boolean vowifi = b.getBoolean("carrier_wfc_ims_available_bool", false);
        boolean vonr = b.getBoolean("vonr_enabled_bool", false);
        boolean sa = hasNrSa(b.getIntArray("carrier_nr_availabilities_int_array"));
        boolean vt = b.getBoolean("carrier_vt_available_bool", false);
        boolean wfcRoam = b.getBoolean("carrier_wfc_ims_roaming_available_bool", false);
        boolean ut = b.getBoolean("carrier_supports_ss_over_ut_bool", false);
        boolean crossSim = b.getBoolean("carrier_cross_sim_ims_available_bool", false);
        boolean ltePlus = !b.getBoolean("hide_lte_plus_data_icon_bool", true);
        String ua = b.getString("ims.ims_user_agent_string", "");
        boolean s26Ua = ua != null && (ua.contains("Samsung") || ua.contains("SM-S942B"));
        return "volte=" + (volte ? 1 : 0)
                + ",vowifi=" + (vowifi ? 1 : 0)
                + ",vonr=" + (vonr ? 1 : 0)
                + ",sa=" + (sa ? 1 : 0)
                + ",vt=" + (vt ? 1 : 0)
                + ",wfc_roam=" + (wfcRoam ? 1 : 0)
                + ",ut=" + (ut ? 1 : 0)
                + ",cross_sim=" + (crossSim ? 1 : 0)
                + ",lte_plus=" + (ltePlus ? 1 : 0)
                + ",s26_ua=" + (s26Ua ? 1 : 0);
    }

    private static boolean hasNrSa(int[] arr) {
        if (arr == null) return false;
        for (int v : arr) {
            if (v == 2) return true;
        }
        return false;
    }

    /**
     * Checks IMS registration using subId-exact ITelephony variants:
     *   1. isImsRegisteredForSubscriber(subId)
     *   2. isImsRegistered(subId)
     * Never passes `slot` into `isImsRegistered(int)` because that parameter is a `subId` and
     * passing slot 0/1 would query another SIM's subscription on Dual-SIM devices.
     */
    public static boolean checkImsRegistered(int subId, int slot) {
        if (subId <= 0 || subId == Integer.MAX_VALUE) return false;
        try {
            Class<?> sm = Class.forName("android.os.ServiceManager");
            IBinder b = (IBinder) sm.getMethod("getService", String.class).invoke(null, "phone");
            if (b == null) return false;
            Object phone = Class.forName("com.android.internal.telephony.ITelephony$Stub")
                    .getMethod("asInterface", IBinder.class).invoke(null, b);
            Class<?> itel = Class.forName("com.android.internal.telephony.ITelephony");

            Boolean r = invokeSingleIntBool(itel, phone, "isImsRegisteredForSubscriber", subId);
            if (r != null) return r;

            r = invokeSingleIntBool(itel, phone, "isImsRegistered", subId);
            if (r != null) return r;

            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    private static Boolean invokeSingleIntBool(Class<?> iface, Object target, String name, int arg) {
        try {
            for (Method m : iface.getDeclaredMethods()) {
                Class<?>[] pt = m.getParameterTypes();
                if (name.equals(m.getName()) && pt.length == 1 && pt[0] == int.class) {
                    m.setAccessible(true);
                    Object res = m.invoke(target, arg);
                    if (res instanceof Boolean) return (Boolean) res;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /**
     * Resolves a valid logical SIM slot index (0 or 1) for a subscription, handling eSIM profiles
     * on Tensor/Pixel devices that sometimes report simSlotIndex < 0 in activeSubscriptionInfoList.
     */
    public static int resolveSlot(Context ctx, int subId, int rawSlot) {
        if (rawSlot >= 0) return rawSlot;
        try {
            SubscriptionManager sm = ctx.getSystemService(SubscriptionManager.class);
            if (sm == null) return rawSlot;
            int occupiedSlot = -1;
            for (int s = 0; s <= 1; s++) {
                try {
                    SubscriptionInfo info = sm.getActiveSubscriptionInfoForSimSlotIndex(s);
                    if (info != null && info.getSubscriptionId() != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                        if (info.getSubscriptionId() == subId) return s;
                        occupiedSlot = s;
                    }
                } catch (Throwable ignored) {}
            }
            List<SubscriptionInfo> list = sm.getActiveSubscriptionInfoList();
            if (list != null) {
                for (SubscriptionInfo info : list) {
                    if (info.getSubscriptionId() == subId && info.getSimSlotIndex() >= 0) {
                        return info.getSimSlotIndex();
                    }
                    if (info.getSubscriptionId() != subId && info.getSimSlotIndex() >= 0) {
                        occupiedSlot = info.getSimSlotIndex();
                    }
                }
            }
            if (occupiedSlot == 0) return 1;
            if (occupiedSlot == 1) return 0;
            return 1;
        } catch (Throwable ignored) {
            return rawSlot;
        }
    }

    private ImsQueryTool() {}
}
