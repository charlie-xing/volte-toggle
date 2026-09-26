package com.xcl.volte;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PersistableBundle;

/** 要写入的 carrier config 键，以及本应用自己的设置存储。 */
public final class Config {

    public static final String PREFS = "volte";

    // 用户可选项
    public static final String K_VONR = "opt_vonr";       // 5G 通话 (VoNR)
    public static final String K_SA = "opt_sa";           // 允许 5G SA 独立组网
    public static final String K_VOWIFI = "opt_vowifi";   // Wi-Fi 通话（国内联通基本无用）
    public static final String K_BOOT = "opt_boot";       // 开机自动重新应用

    // 每个 sub 的期望状态：volte_on_<subId>
    public static String keyOn(int subId) { return "volte_on_" + subId; }

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** 构造要覆盖的 carrier config。persistent=false，所以重启后失效，需要重放。 */
    public static PersistableBundle build(boolean vonr, boolean sa, boolean vowifi) {
        PersistableBundle b = new PersistableBundle();

        // —— VoLTE 主闸门 ——
        b.putBoolean("carrier_volte_available_bool", true);
        b.putBoolean("enhanced_4g_lte_on_by_default_bool", true);
        b.putBoolean("hide_enhanced_4g_lte_bool", false);
        b.putBoolean("editable_enhanced_4g_lte_bool", true);
        b.putBoolean("carrier_volte_provisioned_bool", true);
        b.putBoolean("carrier_volte_provisioning_required_bool", false);
        // 呼叫转移/呼叫等待走 UT
        b.putBoolean("carrier_supports_ss_over_ut_bool", true);
        // 在「设置 → 网络」里显示 IMS 注册状态，方便自查
        b.putBoolean("show_ims_registration_status_bool", true);

        if (vonr) {
            b.putBoolean("vonr_enabled_bool", true);
            b.putBoolean("vonr_setting_visibility_bool", true);
        }
        if (sa) {
            // 1 = NSA, 2 = SA；出厂只给了 [1]
            b.putIntArray("carrier_nr_availabilities_int_array", new int[] { 1, 2 });
        }
        if (vowifi) {
            b.putBoolean("carrier_wfc_ims_available_bool", true);
            b.putBoolean("carrier_default_wfc_ims_enabled_bool", true);
            b.putBoolean("carrier_wfc_ims_provisioned_bool", true);
            b.putBoolean("editable_wfc_mode_bool", true);
        }
        return b;
    }

    /** 判断 VoLTE 是否已生效时读的那个键。 */
    public static final String PROBE_KEY = "carrier_volte_available_bool";

    private Config() {}
}
