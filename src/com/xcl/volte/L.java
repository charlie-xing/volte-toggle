package com.xcl.volte;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * 界面语言：默认英文，可在应用里从列表切换。
 * 文案放在 res/values-xx/strings.xml，这里只负责把选中的语言套到 Context 上。
 */
public final class L {

    public static final String K_LANG = "opt_lang";

    public static final String[] CODES = {
            "en", "zh-Hans", "zh-Hant", "ja", "ko", "ru", "es", "fr", "de", "pt" };
    public static final String[] NAMES = {
            "English", "简体中文", "繁體中文", "日本語", "한국어",
            "Русский", "Español", "Français", "Deutsch", "Português" };
    private static final String[] TAGS = {
            "en", "zh-Hans-CN", "zh-Hant-TW", "ja", "ko", "ru", "es", "fr", "de", "pt-BR" };

    public static int index(Context c) {
        String v = Config.prefs(c).getString(K_LANG, "en");
        if ("zh".equals(v)) v = "zh-Hans";   // v1.0 存的旧值
        for (int i = 0; i < CODES.length; i++) if (CODES[i].equals(v)) return i;
        return 0;
    }

    public static void set(Context c, int i) {
        Config.prefs(c).edit().putString(K_LANG, CODES[i]).apply();
    }

    /** 返回套用了应用内语言的 Context。 */
    public static Context wrap(Context c) {
        Configuration conf = new Configuration(c.getResources().getConfiguration());
        conf.setLocale(Locale.forLanguageTag(TAGS[index(c)]));
        return c.createConfigurationContext(conf);
    }

    public static String s(Context c, int id, Object... args) {
        Context w = wrap(c);
        return args.length == 0 ? w.getString(id) : w.getString(id, args);
    }

    private L() {}
}
