package com.xcl.volte;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.flyfishxu.kadb.Kadb;
import com.flyfishxu.kadb.cert.KadbCert;
import com.flyfishxu.kadb.shell.AdbShellResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import kotlin.Pair;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;

/**
 * 本应用自带的 ADB 客户端：连接手机自己的「无线调试」回环端口，以 shell 身份执行命令。
 * 不需要 Shizuku，也不需要电脑（首次配对除外）。
 */
public final class Adb {

    private static final String HOST = "127.0.0.1";
    private static final String K_PORT = "adb_port";      // 上次可用的连接端口
    private static boolean keysReady = false;

    // ---------- 密钥 ----------

    /** 加载或生成本应用的 ADB 密钥对（配对时会被 adbd 记住，之后重启也认）。 */
    public static synchronized void initKeys(Context ctx) throws Exception {
        if (keysReady) return;
        File cf = new File(ctx.getFilesDir(), "adb_cert.pem");
        File kf = new File(ctx.getFilesDir(), "adb_key.pem");
        if (cf.exists() && kf.exists()) {
            KadbCert.INSTANCE.set(read(cf), read(kf));
        } else {
            long tenYears = System.currentTimeMillis() + 10L * 365 * 24 * 3600 * 1000;
            Pair<byte[], byte[]> p = KadbCert.INSTANCE.get(
                    2048, "VolteToggle", "VolteToggle", "VolteToggle",
                    "VolteToggle", "VolteToggle", "VolteToggle",
                    tenYears, new BigInteger(64, new SecureRandom()));
            write(cf, p.getFirst());
            write(kf, p.getSecond());
        }
        keysReady = true;
    }

    // ---------- 配对 ----------

    /** 用「无线调试 → 使用配对码配对设备」里显示的 6 位码完成一次性配对。 */
    public static void pair(Context ctx, final String code) throws Exception {
        initKeys(ctx);
        final int port = Discovery.find(ctx, Discovery.PAIRING, 8000);
        if (port <= 0) {
            throw new IllegalStateException(
                    L.t(ctx, "Pairing port not found: open Developer options → Wireless debugging → "
                            + "Pair device with pairing code first, then pair again",
                            "找不到配对端口：请先在「开发者选项 → 无线调试 → 使用配对码配对设备」打开那个对话框，再回来点配对"));
        }
        BuildersKt.<Unit>runBlocking(
                EmptyCoroutineContext.INSTANCE,
                new Function2<CoroutineScope, Continuation<? super Unit>, Object>() {
                    @Override
                    public Object invoke(CoroutineScope scope, Continuation<? super Unit> cont) {
                        return Kadb.Companion.pair(HOST, port, code, "VoLTE Toggle", cont);
                    }
                });
    }

    // ---------- 执行 ----------

    public static class Out {
        public final int code;
        public final String text;
        Out(int code, String text) { this.code = code; this.text = text; }
    }

    /** 以 shell 身份执行一条命令。 */
    public static Out exec(Context ctx, String cmd) throws Exception {
        initKeys(ctx);
        SharedPreferences p = Config.prefs(ctx);

        // 先试上次用过的端口，不行再走 mDNS 重新发现
        int cached = p.getInt(K_PORT, -1);
        Exception last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            int port = (attempt == 0) ? cached : Discovery.find(ctx, Discovery.CONNECT, 8000);
            if (port <= 0) continue;
            try {
                Kadb kadb = Kadb.Companion.create(HOST, port, 10000, 120000);
                try {
                    AdbShellResponse r = kadb.shell(cmd);
                    p.edit().putInt(K_PORT, port).apply();
                    return new Out(r.getExitCode(), r.getAllOutput());
                } finally {
                    try { kadb.close(); } catch (Throwable ignored) {}
                }
            } catch (Exception e) {
                Log.d(VolteInstrumentation.TAG, "端口 " + port + " 连接失败", e);
                last = e;
            }
        }
        if (last != null) throw last;
        throw new IllegalStateException(
                L.t(ctx, "Cannot reach local ADB: make sure Developer options → Wireless debugging is on "
                        + "(Wi-Fi required) and pairing is done",
                        "连不上本机 ADB：请确认「开发者选项 → 无线调试」已打开（需连着 Wi-Fi），并已完成配对"));
    }

    /** 快速自检。 */
    public static boolean check(Context ctx) {
        try {
            return exec(ctx, "echo ok").text.trim().equals("ok");
        } catch (Throwable t) {
            return false;
        }
    }

    // ---------- 小工具 ----------

    private static byte[] read(File f) throws Exception {
        try (FileInputStream in = new FileInputStream(f)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
            return bos.toByteArray();
        }
    }

    private static void write(File f, byte[] b) throws Exception {
        try (FileOutputStream out = new FileOutputStream(f)) { out.write(b); }
    }

    private Adb() {}
}
