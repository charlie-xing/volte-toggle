package com.xcl.volte;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 用 mDNS 找出本机 adbd 的「无线调试」端口。
 * adbd 会广播两个服务：_adb-tls-connect._tcp（连接口）和 _adb-tls-pairing._tcp（配对口，
 * 只在「使用配对码配对设备」对话框打开时存在）。
 */
public final class Discovery {

    public static final String CONNECT = "_adb-tls-connect._tcp";
    public static final String PAIRING = "_adb-tls-pairing._tcp";

    /** 返回本机上该服务的端口，找不到返回 -1。 */
    public static int find(Context ctx, final String type, long timeoutMs) {
        final NsdManager nsd = (NsdManager) ctx.getSystemService(Context.NSD_SERVICE);
        if (nsd == null) return -1;

        final List<InetAddress> local = localAddresses();
        final List<Integer> hits = Collections.synchronizedList(new ArrayList<Integer>());
        final CountDownLatch done = new CountDownLatch(1);

        // NsdManager 一次只能有一个 resolveService 在进行，并发调用会报「listener already
        // in use」而静默失败。这里把 resolve 排队串行化：上一条 resolve 回调后再解析下一条。
        final ArrayDeque<NsdServiceInfo> pending = new ArrayDeque<>();
        final boolean[] resolving = { false };
        final Runnable[] pump = new Runnable[1];

        final NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
            @Override public void onResolveFailed(NsdServiceInfo s, int e) {
                synchronized (pending) { resolving[0] = false; }
                pump[0].run();
            }
            @Override public void onServiceResolved(NsdServiceInfo s) {
                InetAddress h = s.getHost();
                int port = s.getPort();
                // 只要本机自己广播的那一条
                if (port > 0 && h != null && (h.isLoopbackAddress() || local.contains(h))) {
                    hits.add(port);
                    done.countDown();
                }
                synchronized (pending) { resolving[0] = false; }
                pump[0].run();
            }
        };

        pump[0] = new Runnable() {
            @Override public void run() {
                NsdServiceInfo next;
                synchronized (pending) {
                    if (resolving[0]) return;
                    next = pending.poll();
                    if (next == null) return;
                    resolving[0] = true;
                }
                try { nsd.resolveService(next, resolveListener); }
                catch (Throwable t) {
                    synchronized (pending) { resolving[0] = false; }
                    pump[0].run();
                }
            }
        };

        final NsdManager.DiscoveryListener listener = new NsdManager.DiscoveryListener() {
            @Override public void onStartDiscoveryFailed(String t, int e) { done.countDown(); }
            @Override public void onStopDiscoveryFailed(String t, int e) {}
            @Override public void onDiscoveryStarted(String t) {}
            @Override public void onDiscoveryStopped(String t) {}
            @Override public void onServiceLost(NsdServiceInfo s) {}

            @Override public void onServiceFound(NsdServiceInfo s) {
                synchronized (pending) { pending.add(s); }
                pump[0].run();
            }
        };

        try {
            nsd.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, listener);
            done.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            Log.w(VolteInstrumentation.TAG, "mDNS 发现失败", t);
        } finally {
            try { nsd.stopServiceDiscovery(listener); } catch (Throwable ignored) {}
        }
        return hits.isEmpty() ? -1 : hits.get(0);
    }

    private static List<InetAddress> localAddresses() {
        List<InetAddress> out = new ArrayList<>();
        try {
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                 e.hasMoreElements(); ) {
                NetworkInterface ni = e.nextElement();
                for (Enumeration<InetAddress> a = ni.getInetAddresses(); a.hasMoreElements(); ) {
                    out.add(a.nextElement());
                }
            }
        } catch (Throwable ignored) {}
        return out;
    }

    private Discovery() {}
}
