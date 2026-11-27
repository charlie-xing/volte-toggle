package com.bp.pixel_ims_apn_tuner;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * UI language: defaults to English, switchable from a list inside the app.
 * Strings live in res/values-xx/strings.xml; this class only applies the selected locale to a Context.
 */
public final class L {

    public static final String K_LANG = "opt_lang";

    public static final String[] CODES = {
            "be", "uk", "id", "ms", "de", "en", "es", "fil", "fr", "pt", "ja", "zh-Hans", "zh-Hant", "ko", "ru" };
    public static final String[] NAMES = {
            "Беларуская", "Українська", "Bahasa Indonesia", "Bahasa Melayu", "Deutsch",
            "English", "Español", "Filipino", "Français", "Português",
            "日本語", "简体中文", "繁體中文", "한국어", "Русский" };
    private static final String[] TAGS = {
            "be", "uk", "id-ID", "ms-MY", "de", "en", "es", "tl-PH", "fr", "pt-BR", "ja", "zh-Hans-CN", "zh-Hant-TW", "ko", "ru" };

    public static int index(Context c) {
        String v = Config.prefs(c).getString(K_LANG, "en");
        if ("zh".equals(v)) v = "zh-Hans";   // Legacy value saved by v1.0
        for (int i = 0; i < CODES.length; i++) if (CODES[i].equals(v)) return i;
        return 5; // Default to English
    }

    public static void set(Context c, int i) {
        Config.prefs(c).edit().putString(K_LANG, CODES[i]).apply();
    }

    /** Returns a Context with the in-app language applied. */
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
