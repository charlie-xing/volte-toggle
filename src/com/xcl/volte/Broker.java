package com.xcl.volte;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

/**
 * 通过内置 ADB 客户端以 shell 身份执行 `am instrument`，把特权活交给
 * {@link VolteInstrumentation}（它跑在本应用自己的 uid 里）。
 */
public final class Broker {

    static final String TARGET = "com.xcl.volte/com.xcl.volte.VolteInstrumentation";

    public static class Result {
        public final boolean ok;
        public final String text;
        Result(boolean ok, String text) { this.ok = ok; this.text = text; }
    }

    public static Result apply(Context ctx, int subId, int slot, boolean on) {
        SharedPreferences p = Config.prefs(ctx);
        StringBuilder cmd = new StringBuilder("am instrument -w");
        // Android 14+ 才有 --no-restart：附着到已运行的进程，界面不会被杀
        if (Build.VERSION.SDK_INT >= 34) cmd.append(" --no-restart");
        cmd.append(" -e action ").append(on ? "on" : "off")
           .append(" -e sub ").append(subId)
           .append(" -e slot ").append(slot)
           .append(" -e vonr ").append(p.getBoolean(Config.K_VONR, true))
           .append(" -e sa ").append(p.getBoolean(Config.K_SA, false))
           .append(" -e vowifi ").append(p.getBoolean(Config.K_VOWIFI, false))
           .append(" ").append(TARGET);

        try {
            Adb.Out out = Adb.exec(ctx, cmd.toString());
            Log.d(VolteInstrumentation.TAG, "am instrument:\n" + out.text);
            String line = pick(out.text, "INSTRUMENTATION_RESULT: result=");
            if (line != null) {
                if (line.startsWith("ok:")) return new Result(true, line.substring(3));
                return new Result(false, line.startsWith("err:") ? line.substring(4) : line);
            }
            if (out.text.contains("INSTRUMENTATION_FAILED")) return new Result(false, out.text.trim());
            return new Result(false, out.text.trim().isEmpty() ? L.t(ctx, "No result returned", "没有返回结果") : out.text.trim());
        } catch (Throwable t) {
            String m = t.getMessage();
            return new Result(false, (m == null || m.isEmpty()) ? t.getClass().getSimpleName() : m);
        }
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
