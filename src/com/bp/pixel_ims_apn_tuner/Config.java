package com.bp.pixel_ims_apn_tuner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;

/** CarrierConfig bundle builder, per-SIM preferences, and stock profile baseline snapshot. */
public final class Config {

    public static final String PREFS = "volte";

    public static final String CONFIG_VERSION_KEY = "pixel_ims_tuner_version";
    public static final int CONFIG_VERSION = 1;

    // Global option & boot-idempotency keys
    public static final String K_BOOT            = "opt_boot";          // Re-apply CarrierConfig after reboot
    public static final String K_LAST_BOOT_ID    = "last_boot_id";      // Kernel boot_id already re-applied
    public static final String K_LAST_BOOT_NOTIF = "last_boot_notif";   // Kernel boot_id already alerted for no-ADB

    // Per-SIM state & option keys
    public static String keyOn(int subId)         { return "volte_on_" + subId; }
    public static String keyEdited(int subId)     { return "sim_edited_" + subId; }
    public static String keyApn(int subId)        { return "opt_apn_" + subId; }
    public static String keyVolte(int subId)      { return "opt_volte_" + subId; }
    public static String keyVowifi(int subId)     { return "opt_vowifi_" + subId; }
    public static String keyVonr(int subId)       { return "opt_vonr_" + subId; }
    public static String keySa(int subId)         { return "opt_sa_" + subId; }
    // Advanced per-SIM options (default true)
    public static String keyVt(int subId)         { return "opt_vt_" + subId; }
    public static String keyWfcRoaming(int subId) { return "opt_wfc_roaming_" + subId; }
    public static String keyUt(int subId)         { return "opt_ut_" + subId; }
    public static String keyCrossSim(int subId)   { return "opt_cross_sim_" + subId; }
    public static String keyAllowApn(int subId)   { return "opt_allow_apn_" + subId; }
    public static String keyLtePlus(int subId)    { return "opt_lte_plus_" + subId; }
    public static String keyS26Ua(int subId)      { return "opt_s26_ua_" + subId; }

    // Per-SIM cached stock Google CarrierConfig snapshot (captured when our override is absent)
    public static String keyBaseSaved(int subId)   { return "base_saved_" + subId; }
    public static String keyBaseVersion(int subId) { return "base_ver_" + subId; }
    public static String keyBaseVolte(int subId)   { return "base_volte_" + subId; }
    public static String keyBaseVowifi(int subId)  { return "base_vowifi_" + subId; }
    public static String keyBaseVonr(int subId)    { return "base_vonr_" + subId; }
    public static String keyBaseSa(int subId)      { return "base_sa_" + subId; }

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Returns true if at least one SIM subscription has saved overrides enabled (`volte_on_<subId>`). */
    public static boolean hasAnyEnabledSimPref(Context c) {
        try {
            for (java.util.Map.Entry<String, ?> e : prefs(c).getAll().entrySet()) {
                String k = e.getKey();
                if (k != null && k.startsWith("volte_on_") && Boolean.TRUE.equals(e.getValue())) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    /** Returns a unique identifier for the current OS boot session (`/proc/sys/kernel/random/boot_id`). */
    public static String currentBootId(Context c) {
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.FileReader("/proc/sys/kernel/random/boot_id"))) {
            String line = br.readLine();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
        } catch (Throwable ignored) {}
        try {
            int bc = android.provider.Settings.Global.getInt(
                    c.getContentResolver(), android.provider.Settings.Global.BOOT_COUNT, -1);
            if (bc >= 0) return "bc:" + bc;
        } catch (Throwable ignored) {}
        long approxBootSec = (System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime()) / 10_000L;
        return "ts:" + approxBootSec;
    }

    public static boolean isBootAlreadyApplied(Context c) {
        String id = currentBootId(c);
        return id.equals(prefs(c).getString(K_LAST_BOOT_ID, ""));
    }

    public static void markBootApplied(Context c) {
        prefs(c).edit().putString(K_LAST_BOOT_ID, currentBootId(c)).apply();
    }

    public static boolean isBootAttentionNotified(Context c) {
        String id = currentBootId(c);
        return id.equals(prefs(c).getString(K_LAST_BOOT_NOTIF, ""));
    }

    public static void markBootAttentionNotified(Context c) {
        prefs(c).edit().putString(K_LAST_BOOT_NOTIF, currentBootId(c)).apply();
    }

    /** Clears saved per-SIM edits, applied state, and baseline snapshot so it is freshly re-read. */
    public static void clearSimPrefs(Context c, int subId) {
        prefs(c).edit()
                .remove(keyOn(subId))
                .remove(keyEdited(subId))
                .remove(keyApn(subId))
                .remove(keyVolte(subId))
                .remove(keyVowifi(subId))
                .remove(keyVonr(subId))
                .remove(keySa(subId))
                .remove(keyVt(subId))
                .remove(keyWfcRoaming(subId))
                .remove(keyUt(subId))
                .remove(keyCrossSim(subId))
                .remove(keyAllowApn(subId))
                .remove(keyLtePlus(subId))
                .remove(keyS26Ua(subId))
                .remove(keyBaseSaved(subId))
                .remove(keyBaseVersion(subId))
                .remove(keyBaseVolte(subId))
                .remove(keyBaseVowifi(subId))
                .remove(keyBaseVonr(subId))
                .remove(keyBaseSa(subId))
                .apply();
    }

    /** Stock Google CarrierConfig snapshot for a SIM. */
    public static final class Baseline {
        public final boolean volte;
        public final boolean vowifi;
        public final boolean vonr;
        public final boolean sa;
        public final String version;
        public final boolean hasOverride;

        public Baseline(boolean volte, boolean vowifi, boolean vonr, boolean sa,
                        String version, boolean hasOverride) {
            this.volte = volte;
            this.vowifi = vowifi;
            this.vonr = vonr;
            this.sa = sa;
            this.version = version == null ? "" : version;
            this.hasOverride = hasOverride;
        }

        public boolean hasAnyStockIms() {
            return volte || vowifi || vonr || sa;
        }
    }

    /**
     * Reads CarrierConfig for subId. When our override marker (CONFIG_VERSION_KEY) is absent,
     * snapshots the stock Google CarrierConfig flags and version into SharedPreferences.
     * When our override is active, returns the cached stock snapshot so overrides are not
     * mistaken for stock Google profile capabilities.
     */
    public static Baseline readBaseline(Context c, int subId) {
        SharedPreferences p = prefs(c);
        try {
            CarrierConfigManager ccm = c.getSystemService(CarrierConfigManager.class);
            PersistableBundle b = ccm.getConfigForSubId(subId,
                    CONFIG_VERSION_KEY,
                    "carrier_config_version_string",
                    "carrier_volte_available_bool",
                    "carrier_wfc_ims_available_bool",
                    "vonr_enabled_bool",
                    "carrier_nr_availabilities_int_array");
            if (b != null) {
                boolean hasOverride = b.getInt(CONFIG_VERSION_KEY, 0) > 0;
                String ver = b.getString("carrier_config_version_string", "");
                if (!hasOverride) {
                    boolean volte = b.getBoolean("carrier_volte_available_bool", false);
                    boolean vowifi = b.getBoolean("carrier_wfc_ims_available_bool", false);
                    boolean vonr = b.getBoolean("vonr_enabled_bool", false);
                    boolean sa = hasNrSa(b.getIntArray("carrier_nr_availabilities_int_array"));
                    p.edit()
                            .putBoolean(keyBaseSaved(subId), true)
                            .putString(keyBaseVersion(subId), ver == null ? "" : ver)
                            .putBoolean(keyBaseVolte(subId), volte)
                            .putBoolean(keyBaseVowifi(subId), vowifi)
                            .putBoolean(keyBaseVonr(subId), vonr)
                            .putBoolean(keyBaseSa(subId), sa)
                            .apply();
                    return new Baseline(volte, vowifi, vonr, sa, ver, false);
                }
                if (p.getBoolean(keyBaseSaved(subId), false)) {
                    return new Baseline(
                            p.getBoolean(keyBaseVolte(subId), false),
                            p.getBoolean(keyBaseVowifi(subId), false),
                            p.getBoolean(keyBaseVonr(subId), false),
                            p.getBoolean(keyBaseSa(subId), false),
                            p.getString(keyBaseVersion(subId), ver),
                            true);
                }
                return new Baseline(false, false, false, false, ver, true);
            }
        } catch (Throwable ignored) {}

        if (p.getBoolean(keyBaseSaved(subId), false)) {
            return new Baseline(
                    p.getBoolean(keyBaseVolte(subId), false),
                    p.getBoolean(keyBaseVowifi(subId), false),
                    p.getBoolean(keyBaseVonr(subId), false),
                    p.getBoolean(keyBaseSa(subId), false),
                    p.getString(keyBaseVersion(subId), ""),
                    p.getBoolean(keyOn(subId), false));
        }
        return new Baseline(false, false, false, false, "", p.getBoolean(keyOn(subId), false));
    }

    private static boolean hasNrSa(int[] arr) {
        if (arr == null) return false;
        for (int v : arr) {
            if (v == 2) return true;
        }
        return false;
    }

    /**
     * Build merged CarrierConfig overrides (volte-toggle + vvb2060/Ims v3.1 + ImsForPixel keys).
     */
    public static PersistableBundle build(boolean volte, boolean vonr, boolean sa, boolean vowifi,
                                          boolean wfcRoaming, boolean vt, boolean ut, boolean crossSim,
                                          boolean allowApn, boolean ltePlus, String imsUserAgent) {
        PersistableBundle b = new PersistableBundle();
        b.putInt(CONFIG_VERSION_KEY, CONFIG_VERSION);

        if (imsUserAgent != null && !imsUserAgent.isEmpty()) {
            b.putString("ims.ims_user_agent_string", imsUserAgent);
        }

        if (volte || vowifi) {
            // Bypass Android 14-17 MMTEL & Entitlement provisioning locks
            b.putPersistableBundle("ims.mmtel_requires_provisioning_bundle", new PersistableBundle());
            b.putBoolean("imsserviceentitlement.ims_provisioning_bool", false);
            b.putBoolean("carrier_volte_provisioning_required_bool", false);
            b.putBoolean("carrier_supports_ims_sms_bool", true);
            b.putBoolean("show_ims_registration_status_bool", true);
            b.putBoolean("carrier_cross_sim_ims_available_bool", crossSim);
            b.putBoolean("enable_cross_sim_calling_on_opportunistic_data_bool", crossSim);
        }

        if (volte) {
            // VoLTE, roaming VoLTE, UT/SS
            b.putBoolean("carrier_volte_available_bool", true);
            b.putBoolean("carrier_volte_roaming_available_bool", true);
            b.putBoolean("imsvoice.carrier_volte_roaming_available_bool", true);
            b.putBoolean("carrier_default_volte_roaming_enabled_bool", true);
            b.putBoolean("enhanced_4g_lte_on_by_default_bool", true);
            b.putBoolean("hide_enhanced_4g_lte_bool", false);
            b.putBoolean("editable_enhanced_4g_lte_bool", true);
            b.putBoolean("carrier_volte_provisioned_bool", true);
            b.putBoolean("carrier_supports_ss_over_ut_bool", ut);
        }

        if (volte || vowifi || vonr || vt) {
            b.putBoolean("carrier_vt_available_bool", vt);
        }

        if (ltePlus) {
            // Carrier Aggregation / LTE+ status bar display & bandwidth threshold bypass
            b.putBoolean("hide_lte_plus_data_icon_bool", false);
            b.putInt("lte_plus_threshold_bandwidth_khz_int", 0);
        }

        if (allowApn) {
            b.putBoolean("allow_adding_apns_bool", true);
        }

        if (vonr) {
            b.putBoolean("vonr_enabled_bool", true);
            b.putBoolean("vonr_setting_visibility_bool", true);
        }
        if (sa || vonr) {
            // 0 = SA_DISABLE_POLICY_NONE (prevent IMS stack from disabling 5G SA)
            b.putInt("ims.sa_disable_policy_int", 0);
        }
        if (sa) {
            // 1 = NSA, 2 = SA
            b.putIntArray("carrier_nr_availabilities_int_array", new int[] { 1, 2 });
            // Aligned with standard Android 4G LTE RSRP scale (Pixel 6 - 11)
            b.putIntArray("5g_nr_ssrsrp_thresholds_int_array", new int[] {
                    -128, /* SIGNAL_STRENGTH_POOR */
                    -118, /* SIGNAL_STRENGTH_MODERATE */
                    -108, /* SIGNAL_STRENGTH_GOOD */
                    -98   /* SIGNAL_STRENGTH_GREAT */
            });
        }
        if (vowifi) {
            b.putBoolean("carrier_wfc_ims_available_bool", true);
            b.putBoolean("carrier_wfc_ims_roaming_available_bool", wfcRoaming);
            b.putBoolean("carrier_default_wfc_ims_enabled_bool", true);
            b.putBoolean("carrier_default_wfc_ims_roaming_enabled_bool", wfcRoaming);
            b.putBoolean("carrier_wfc_ims_provisioned_bool", true);
            b.putBoolean("carrier_wfc_supports_wifi_only_bool", true);
            b.putBoolean("editable_wfc_mode_bool", true);
            b.putBoolean("editable_wfc_roaming_mode_bool", true);
            b.putBoolean("show_wifi_calling_icon_in_status_bar_bool", true);
            b.putInt("wfc_spn_format_idx_int", 6);
        }
        return b;
    }

    private Config() {}
}
