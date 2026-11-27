package com.bp.pixel_ims_apn_tuner;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.flyfishxu.kadb.Kadb;
import com.flyfishxu.kadb.cert.KadbCert;
import com.flyfishxu.kadb.shell.AdbShellResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import kotlin.Pair;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;

/**
 * Built-in ADB client: connects to the device's own Wireless Debugging loopback port
 * and runs commands as shell. Requires neither Shizuku nor a PC (except for initial pairing).
 */
public final class Adb {

    private static final String HOST = "127.0.0.1";
    private static final String K_PORT = "adb_port";      // Last working connection port
    private static final Object IO_LOCK = new Object();
    private static boolean keysReady = false;
    private static volatile boolean pairingInProgress = false;
    private static volatile byte[] pinnedServerPubKeyHash = null;
    private static volatile boolean keepAlive = false;
    private static Kadb activeKadb = null;
    private static int activePort = -1;
    private static long lastRecoverAttemptMs = 0L;
    private static final long RECOVER_COOLDOWN_MS = 25_000L;

    // ---------- Wi-Fi & stuck Wireless Debugging recovery ----------

    /** Returns true if the device currently has an active Wi-Fi transport. */
    public static boolean isWifiConnected(Context ctx) {
        try {
            ConnectivityManager cm = ctx.getSystemService(ConnectivityManager.class);
            if (cm == null) return false;
            Network active = cm.getActiveNetwork();
            if (active != null) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(active);
                if (nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true;
            }
            Network[] all = cm.getAllNetworks();
            if (all != null) {
                for (Network n : all) {
                    NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                    if (nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean canWriteSecureSettings(Context ctx) {
        return ctx.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Recovers Wireless Debugging after a reboot or Wi-Fi reconnect when `adbd` fails to publish
     * its TLS port (or when Android resets `adb_wifi_enabled` across reboot). Only runs after the
     * app has been paired at least once (`K_PORT > 0`); `AdbDebuggingManager` itself enforces
     * trusted Wi-Fi BSSID/SSID checks before opening a port.
     */
    public static boolean recoverWirelessDebugging(Context ctx) {
        if (!isWifiConnected(ctx) || !canWriteSecureSettings(ctx)) return false;
        if (Config.prefs(ctx).getInt(K_PORT, -1) <= 0) return false;
        long now = SystemClock.elapsedRealtime();
        if (lastRecoverAttemptMs != 0L && (now - lastRecoverAttemptMs) < RECOVER_COOLDOWN_MS) {
            return false;
        }
        try {
            lastRecoverAttemptMs = now;
            Settings.Global.putInt(ctx.getContentResolver(), "adb_wifi_enabled", 0);
            Thread.sleep(1000L);
            Settings.Global.putInt(ctx.getContentResolver(), "adb_wifi_enabled", 1);
            // Allow AdbDebuggingManager + MdnsAdvertiser to finish probing the new TLS port
            Thread.sleep(1500L);
            pinnedServerPubKeyHash = null;
            Log.i(VolteInstrumentation.TAG, "Recovered Wireless Debugging via adb_wifi_enabled toggle");
            return true;
        } catch (Throwable t) {
            Log.w(VolteInstrumentation.TAG, "recoverWirelessDebugging failed", t);
            return false;
        }
    }

    // ---------- Keys ----------

    /** Loads or generates this app's ADB key pair (remembered by adbd across reboots once paired). */
    public static synchronized void initKeys(Context ctx) throws Exception {
        if (keysReady) return;
        File cf = new File(ctx.getFilesDir(), "adb_cert.pem");
        File kf = new File(ctx.getFilesDir(), "adb_key.pem");
        if (cf.exists() && kf.exists()) {
            KadbCert.INSTANCE.set(read(cf), read(kf));
        } else {
            long tenYears = System.currentTimeMillis() + 10L * 365 * 24 * 3600 * 1000;
            Pair<byte[], byte[]> p = KadbCert.INSTANCE.get(
                    2048, "PixelImsApnTuner", "PixelImsApnTuner", "PixelImsApnTuner",
                    "PixelImsApnTuner", "PixelImsApnTuner", "US",
                    tenYears, new BigInteger(64, new SecureRandom()));
            write(cf, p.getFirst());
            write(kf, p.getSecond());
        }
        keysReady = true;
    }

    /**
     * Verifies or pins the local adbd TLS server public key fingerprint (TOFU per adbd session).
     * During SPAKE2 pairing, adbd uses a temporary pairing TLS context authenticated via
     * RFC 5705 exported keying material, so pinning applies to the main ADB TLS transport.
     */
    public static synchronized void verifyOrPinServerCert(X509Certificate cert)
            throws CertificateException {
        if (pairingInProgress) return;
        try {
            byte[] encoded = cert.getPublicKey().getEncoded();
            if (encoded == null || encoded.length == 0) {
                throw new CertificateException("Empty server public key encoding");
            }
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(encoded);
            byte[] currentPin = pinnedServerPubKeyHash;
            if (currentPin == null) {
                pinnedServerPubKeyHash = digest;
            } else if (!MessageDigest.isEqual(currentPin, digest)) {
                throw new CertificateException("ADB TLS server public key fingerprint mismatch");
            }
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            throw new CertificateException("Failed to verify server public key fingerprint", e);
        }
    }

    // ---------- Pairing ----------

    /** Completes one-time pairing using the 6-digit code shown in Wireless Debugging -> Pair device with pairing code. */
    public static void pair(Context ctx, final String code) throws Exception {
        initKeys(ctx);
        close();
        final int port = Discovery.find(ctx, Discovery.PAIRING, 8000);
        if (port <= 0) {
            throw new IllegalStateException(
                    L.s(ctx, R.string.err_no_pair_port));
        }
        pairingInProgress = true;
        pinnedServerPubKeyHash = null;
        try {
            BuildersKt.<Unit>runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    new Function2<CoroutineScope, Continuation<? super Unit>, Object>() {
                        @Override
                        public Object invoke(CoroutineScope scope, Continuation<? super Unit> cont) {
                            // AdbDebuggingManager splits "<base64_key> <name>" on whitespace ("\\s+")
                            // and takes token[1], so use hyphens instead of spaces.
                            return Kadb.Companion.pair(HOST, port, code, "Pixel-IMS-APN-Tuner", cont);
                        }
                    });
        } finally {
            pairingInProgress = false;
        }
    }

    // ---------- Execution ----------

    public static class Out {
        public final int code;
        public final String text;
        Out(int code, String text) { this.code = code; this.text = text; }
    }

    /** Marks whether the UI wants the reusable ADB session kept open across commands. */
    public static void setKeepAlive(boolean active) {
        keepAlive = active;
    }

    /** Closes the cached ADB connection immediately (must be called off the main thread). */
    public static void close() {
        synchronized (IO_LOCK) {
            closeLocked();
        }
    }

    /** Closes the cached ADB connection only when no UI session has requested keep-alive. */
    public static void closeIfNotKeepAlive() {
        synchronized (IO_LOCK) {
            if (!keepAlive) closeLocked();
        }
    }

    /** Marks keep-alive off and closes the cached ADB connection off the main thread once idle. */
    public static void closeWhenIdle() {
        keepAlive = false;
        new Thread(new Runnable() {
            @Override public void run() {
                synchronized (IO_LOCK) {
                    if (!keepAlive) closeLocked();
                }
            }
        }, "adb-close").start();
    }

    private static void closeLocked() {
        Kadb k = activeKadb;
        activeKadb = null;
        activePort = -1;
        if (k != null) {
            try { k.close(); } catch (Throwable ignored) {}
        }
    }

    /**
     * Executes a command as shell, reusing an existing local ADB TLS connection when available
     * so Android does not re-trigger the "Wireless debugging connected" notification on every call.
     */
    public static Out exec(Context ctx, String cmd) throws Exception {
        initKeys(ctx);
        SharedPreferences p = Config.prefs(ctx);

        synchronized (IO_LOCK) {
            // 1. Reuse existing persistent TLS session if open
            if (activeKadb != null) {
                try {
                    AdbShellResponse r = activeKadb.shell(cmd);
                    return new Out(r.getExitCode(), r.getAllOutput());
                } catch (Exception e) {
                    Log.d(VolteInstrumentation.TAG, "Reused ADB connection (port " + activePort + ") failed, reconnecting", e);
                    closeLocked();
                }
            }

            // 2. Try the previously used port first; fall back to mDNS rediscovery, and if no port
            // is advertised while Wireless Debugging is enabled on Wi-Fi, recover stuck adbd once.
            int cached = p.getInt(K_PORT, -1);
            Exception last = null;
            boolean recovered = false;
            for (int attempt = 0; attempt < 3; attempt++) {
                int port;
                if (attempt == 0) {
                    port = cached;
                } else if (attempt == 1) {
                    port = Discovery.find(ctx, Discovery.CONNECT, 8000);
                } else {
                    if (!recovered) break;
                    port = Discovery.find(ctx, Discovery.CONNECT, 8000);
                }
                if (port <= 0) {
                    if (attempt == 1 && recoverWirelessDebugging(ctx)) {
                        recovered = true;
                    }
                    continue;
                }
                if (attempt >= 1 && port != cached) {
                    // When adbd restarts on a new Wireless Debugging port, allow re-pinning its new TLS cert
                    pinnedServerPubKeyHash = null;
                }
                Kadb kadb = null;
                try {
                    kadb = Kadb.Companion.create(HOST, port, 10000, 120000);
                    AdbShellResponse r = kadb.shell(cmd);
                    p.edit().putInt(K_PORT, port).apply();
                    activeKadb = kadb;
                    activePort = port;
                    return new Out(r.getExitCode(), r.getAllOutput());
                } catch (Exception e) {
                    if (kadb != null) {
                        try { kadb.close(); } catch (Throwable ignored) {}
                    }
                    Log.d(VolteInstrumentation.TAG, "Port " + port + " connection failed", e);
                    last = e;
                }
            }
            if (last != null) throw last;
            throw new IllegalStateException(
                    L.s(ctx, R.string.err_no_adb));
        }
    }

    /** Quick self-check. */
    public static boolean check(Context ctx) {
        try {
            return exec(ctx, "echo ok").text.trim().equals("ok");
        } catch (Throwable t) {
            return false;
        }
    }

    // ---------- Helpers ----------

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
        f.setReadable(false, false);
        f.setReadable(true, true);
        f.setWritable(false, false);
        f.setWritable(true, true);
    }

    private Adb() {}
}
