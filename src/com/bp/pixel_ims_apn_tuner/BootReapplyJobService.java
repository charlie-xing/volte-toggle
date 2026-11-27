package com.bp.pixel_ims_apn_tuner;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Background JobService constrained to {@link NetworkCapabilities#TRANSPORT_WIFI}.
 *
 * <ul>
 *   <li>If the phone reboots outdoors without Wi-Fi, JobScheduler keeps this job pending with
 *       zero battery drain and triggers {@link #onStartJob} automatically once Wi-Fi connects.</li>
 *   <li>If Wireless Debugging (`adbd`) is stuck after reboot despite `adb_wifi_enabled == 1`,
 *       {@link Adb#check} automatically invokes {@link Adb#recoverWirelessDebugging} to restart
 *       the TLS server on trusted Wi-Fi networks.</li>
 *   <li>On Android 12–13 ({@link ApplyMode#isDetached()}), marks boot as applied before firing
 *       detached `nohup am instrument` so process restart does not cause a reschedule loop.</li>
 * </ul>
 */
public class BootReapplyJobService extends JobService {

    public static final int JOB_ID = 4301;

    private volatile boolean cancelled = false;
    private Thread worker;

    public static void schedule(Context context) {
        try {
            Context app = context.getApplicationContext();
            JobScheduler js = app.getSystemService(JobScheduler.class);
            if (js == null) return;
            if (js.getPendingJob(JOB_ID) != null) return;

            ComponentName cn = new ComponentName(app, BootReapplyJobService.class);
            JobInfo.Builder b = new JobInfo.Builder(JOB_ID, cn)
                    .setBackoffCriteria(20_000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

            try {
                NetworkRequest wifiReq = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                        .build();
                b.setRequiredNetwork(wifiReq);
                js.schedule(b.build());
            } catch (Throwable fallbackErr) {
                Log.w(VolteInstrumentation.TAG, "Custom Wi-Fi NetworkRequest rejected, falling back to UNMETERED", fallbackErr);
                JobInfo fallback = new JobInfo.Builder(JOB_ID, cn)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                        .setBackoffCriteria(20_000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                        .build();
                js.schedule(fallback);
            }
        } catch (Throwable t) {
            Log.e(VolteInstrumentation.TAG, "Failed to schedule BootReapplyJobService", t);
        }
    }

    public static void cancel(Context context) {
        try {
            JobScheduler js = context.getApplicationContext().getSystemService(JobScheduler.class);
            if (js != null) js.cancel(JOB_ID);
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        final Context app = getApplicationContext();
        if (!Config.prefs(app).getBoolean(Config.K_BOOT, false)
                || !Config.hasAnyEnabledSimPref(app)
                || Config.isBootAlreadyApplied(app)) {
            return false;
        }

        cancelled = false;
        worker = new Thread(new Runnable() {
            @Override public void run() {
                boolean needsReschedule = false;
                try {
                    if (!Adb.isWifiConnected(app)) {
                        Log.d(VolteInstrumentation.TAG, "BootReapplyJobService: Wi-Fi not active yet, rescheduling");
                        needsReschedule = true;
                        return;
                    }

                    // Wait up to 20s for active SIM subscriptions to finish initializing after boot
                    List<SubscriptionInfo> targets = waitForTargetSims(app, 20_000L);
                    if (cancelled) {
                        needsReschedule = true;
                        return;
                    }
                    if (targets.isEmpty()) {
                        // SIM may still be PIN-locked right after boot; retry with backoff
                        Log.d(VolteInstrumentation.TAG, "BootReapplyJobService: no active target SIMs yet, rescheduling");
                        needsReschedule = true;
                        return;
                    }

                    // Probe local ADB (Adb.exec automatically recovers stuck Wireless Debugging via WRITE_SECURE_SETTINGS)
                    boolean adbReady = false;
                    for (int i = 0; i < 3 && !cancelled; i++) {
                        if (!Adb.isWifiConnected(app)) {
                            needsReschedule = true;
                            return;
                        }
                        if (Adb.check(app)) {
                            adbReady = true;
                            break;
                        }
                        if (i < 2) {
                            Thread.sleep(4000L);
                        }
                    }

                    if (!adbReady) {
                        if (Adb.isWifiConnected(app) && !Config.isBootAttentionNotified(app)) {
                            Config.markBootAttentionNotified(app);
                            BootReceiver.notifyBoot(app,
                                    L.s(app, R.string.boot_notif_title_fail),
                                    L.s(app, R.string.boot_notif_no_adb));
                        }
                        // Keep rescheduling so connecting to a trusted Wi-Fi or enabling Wireless Debugging auto-applies
                        needsReschedule = true;
                        return;
                    }

                    Broker.grantSelfPermissions(app);

                    if (ApplyMode.isDetached()) {
                        // Mark before detached am instrument force-stops our process on Android 12-13
                        Config.markBootApplied(app);
                        Broker.Result r = Broker.applyDetachedBatch(app, true);
                        if (!r.ok) {
                            BootReceiver.notifyBoot(app, L.s(app, R.string.boot_notif_title_fail), r.text);
                        }
                        needsReschedule = false;
                        return;
                    }

                    StringBuilder body = new StringBuilder();
                    boolean allOk = true;
                    for (SubscriptionInfo si : targets) {
                        if (cancelled) {
                            needsReschedule = true;
                            return;
                        }
                        int sub = si.getSubscriptionId();
                        int slot = ImsQueryTool.resolveSlot(app, sub, si.getSimSlotIndex());
                        CharSequence name = si.getDisplayName();
                        if (name == null || name.length() == 0) name = si.getCarrierName();
                        if (name == null || name.length() == 0) name = "sub " + sub;

                        // APNs are already persistent in telephony.db; only replay CarrierConfig on boot
                        Broker.Result r = Broker.apply(app, sub, slot, true, false);
                        Log.i(VolteInstrumentation.TAG,
                                "Boot replay sub=" + sub + " -> " + r.ok + " " + r.text);
                        if (!r.ok) allOk = false;
                        if (body.length() > 0) body.append('\n');
                        String label = slot >= 0 ? ("SIM " + (slot + 1) + " (" + name + ")") : String.valueOf(name);
                        body.append(label).append(": ").append(r.text);
                    }

                    if (allOk) {
                        Config.markBootApplied(app);
                    } else {
                        needsReschedule = true;
                    }
                    String title = L.s(app, allOk ? R.string.boot_notif_title_ok : R.string.boot_notif_title_fail);
                    BootReceiver.notifyBoot(app, title, body.toString());
                } catch (Throwable t) {
                    Log.e(VolteInstrumentation.TAG, "BootReapplyJobService failed", t);
                    needsReschedule = true;
                } finally {
                    Adb.closeIfNotKeepAlive();
                    jobFinished(params, needsReschedule);
                }
            }
        }, "boot-reapply-job");
        worker.start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        cancelled = true;
        Thread w = worker;
        if (w != null) w.interrupt();
        // Reschedule if Wi-Fi dropped before boot re-apply finished
        return !Config.isBootAlreadyApplied(getApplicationContext());
    }

    private List<SubscriptionInfo> waitForTargetSims(Context app, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        SharedPreferences pp = Config.prefs(app);
        SubscriptionManager sm = app.getSystemService(SubscriptionManager.class);
        while (System.currentTimeMillis() < deadline && !cancelled) {
            List<SubscriptionInfo> targets = new ArrayList<>();
            try {
                List<SubscriptionInfo> list = sm != null ? sm.getActiveSubscriptionInfoList() : null;
                if (list != null) {
                    for (SubscriptionInfo si : list) {
                        if (pp.getBoolean(Config.keyOn(si.getSubscriptionId()), false)) {
                            targets.add(si);
                        }
                    }
                }
            } catch (Throwable ignored) {}
            if (!targets.isEmpty()) return targets;
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                break;
            }
        }
        return new ArrayList<>();
    }
}
