package com.xcl.volte;

import android.content.Context;

/** 界面语言：默认英文，可在应用里切到中文。t(ctx, 英文, 中文)。 */
public final class L {

    public static final String K_LANG = "opt_lang";   // "en" / "zh"

    public static boolean zh(Context c) {
        return "zh".equals(Config.prefs(c).getString(K_LANG, "en"));
    }

    public static void setZh(Context c, boolean zh) {
        Config.prefs(c).edit().putString(K_LANG, zh ? "zh" : "en").apply();
    }

    public static String t(Context c, String en, String zh) {
        return zh(c) ? zh : en;
    }

    private L() {}
}
