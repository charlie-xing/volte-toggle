package com.xcl.volte;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {

    private static final int REQ_PHONE = 1001;
    private static final int REQ_NOTIF = 1002;

    private LinearLayout root;
    private LinearLayout adbBox;
    private LinearLayout simBox;
    private TextView logView;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private boolean busy = false;
    private boolean probing = false;
    private Boolean adbOk = null;   // null = 还没测

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle(t("VoLTE Toggle", "VoLTE 开关"));

        ScrollView sv = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));
        sv.addView(root);
        setContentView(sv);

        root.addView(headerRow());
        root.addView(text(t("Overrides the carrier config to enable VoLTE / VoNR. Android does not let "
                + "third-party apps write persistent config, so it must be re-applied after every reboot.",
                "覆盖运营商配置开启 VoLTE / VoNR。系统不允许第三方应用写持久化配置，"
                + "所以每次重启后需要重新应用一次。"), 13, false, muted()));

        root.addView(stepsCard());

        adbBox = new LinearLayout(this);
        adbBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(adbBox);

        simBox = new LinearLayout(this);
        simBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(simBox);

        root.addView(optionsCard());

        LinearLayout logCard = card();
        logCard.addView(text(t("Log", "日志"), 12, true, muted()));
        logView = text("—", 12, false, muted());
        logView.setTypeface(Typeface.MONOSPACE);
        logCard.addView(logView);
        root.addView(logCard);

        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.READ_PHONE_STATE }, REQ_PHONE);
        }
        probeAdb();
    }

    @Override protected void onResume() {
        super.onResume();
        render();
        // 从通知栏输完配对码切回应用时，通道可能刚刚就绪，重新检测一次，界面才会自动更新。
        if (!probing && !Boolean.TRUE.equals(adbOk)) probeAdb();
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] p, int[] r) {
        super.onRequestPermissionsResult(req, p, r);
        render();
    }

    // ---------------- ADB 通道 ----------------

    private void probeAdb() {
        if (probing) return;
        probing = true;
        log(t("Checking local ADB channel …", "正在检测本机 ADB 通道 …"));
        new Thread(new Runnable() {
            @Override public void run() {
                final boolean ok = Adb.check(MainActivity.this);
                ui.post(new Runnable() {
                    @Override public void run() {
                        probing = false;
                        adbOk = ok;
                        if (ok) PairReceiver.dismiss(MainActivity.this);
                        log(ok ? t("ADB channel ready", "ADB 通道就绪")
                                : t("ADB channel unavailable, pair first", "ADB 通道不可用，需要先配对"));
                        render();
                    }
                });
            }
        }).start();
    }

    private View adbCard() {
        LinearLayout c = card();
        c.addView(text(t("ADB channel (local loopback)", "ADB 通道（本机回环）"), 12, true, muted()));

        if (adbOk == null) {
            c.addView(text(t("Checking …", "检测中 …"), 14, false, null));
            return c;
        }
        if (adbOk) {
            c.addView(text(t("Ready — you can toggle VoLTE now", "已就绪 — 可以直接开关 VoLTE"), 15, true, null));
            Button b = new Button(this);
            b.setText(t("Re-check", "重新检测"));
            b.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { adbOk = null; render(); probeAdb(); }
            });
            c.addView(b);
            return c;
        }

        c.addView(text(t("Not connected. One-time pairing:", "未连接。一次性配对步骤："), 15, true, null));
        c.addView(text(t("1. Turn on Wi-Fi (wireless debugging needs Wi-Fi)\n"
                + "2. Tap the button below; it opens Wireless debugging and posts a notification\n"
                + "3. In Wireless debugging tap \"Pair device with pairing code\" and keep the dialog open\n"
                + "4. Pull down the notification shade, type the 6-digit code into the notification and send",
                "1. 打开 Wi-Fi（无线调试必须连着 Wi-Fi）\n"
                + "2. 点下面的按钮，会跳到「无线调试」并弹出一条通知\n"
                + "3. 在无线调试里点「输入配对码配对设备」，保持对话框开着\n"
                + "4. 下拉通知栏，把对话框里的 6 位配对码填进通知的输入框，发送"),
                13, false, muted()));

        Button go = new Button(this);
        go.setText(t("Start pairing", "开始配对"));
        go.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS },
                            REQ_NOTIF);
                    return;
                }
                PairReceiver.prompt(MainActivity.this);
                openWirelessDebugging();
                log(t("Pairing notification posted — enter the code from the system dialog",
                        "已弹出配对通知，请在系统对话框里拿配对码填进去"));
            }
        });
        c.addView(go);

        c.addView(text(t("(The box below only helps if you can keep the pairing dialog in the foreground;\n"
                + "normally use the notification above.)",
                "（下面这个输入框只在你能让配对对话框保持在前台时才有用；\n通常请用上面的通知方式。）"), 11, false, muted()));

        final EditText code = new EditText(this);
        code.setHint(t("6-digit pairing code", "6 位配对码"));
        code.setInputType(InputType.TYPE_CLASS_NUMBER);
        c.addView(code);

        Button pair = new Button(this);
        pair.setText(t("Pair", "配对"));
        pair.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final String s = code.getText().toString().trim();
                if (s.isEmpty()) { log(t("Enter the pairing code first", "请先填配对码")); return; }
                log(t("Pairing …", "配对中 …"));
                new Thread(new Runnable() {
                    @Override public void run() {
                        String msg;
                        boolean ok = false;
                        try {
                            Adb.pair(MainActivity.this, s);
                            ok = Adb.check(MainActivity.this);
                            msg = ok ? t("Paired successfully", "配对成功")
                                    : t("Paired but still cannot connect, try Re-check",
                                        "配对完成但仍连不上，试试重新检测");
                        } catch (Throwable t) {
                            String m = t.getMessage();
                            msg = t("Pairing failed: ", "配对失败：") + ((m == null || m.isEmpty())
                                    ? t.getClass().getSimpleName() : m);
                        }
                        final String fm = msg;
                        final boolean fo = ok;
                        ui.post(new Runnable() {
                            @Override public void run() { adbOk = fo; log(fm); render(); }
                        });
                    }
                }).start();
            }
        });
        c.addView(pair);
        return c;
    }

    // ---------------- 渲染 ----------------

    private void render() {
        adbBox.removeAllViews();
        adbBox.addView(adbCard());

        simBox.removeAllViews();
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            LinearLayout c = card();
            c.addView(text(t("Phone permission is required to read SIM state",
                    "需要「电话」权限才能读取 SIM 状态"), 14, false, null));
            simBox.addView(c);
            return;
        }
        SubscriptionManager sm = getSystemService(SubscriptionManager.class);
        List<SubscriptionInfo> list = null;
        try { list = sm.getActiveSubscriptionInfoList(); } catch (Throwable ignored) {}
        if (list == null || list.isEmpty()) {
            LinearLayout c = card();
            c.addView(text(t("No active SIM detected", "没有检测到已激活的 SIM"), 14, false, null));
            simBox.addView(c);
            return;
        }
        for (SubscriptionInfo si : list) simBox.addView(simCard(si));
    }

    private View simCard(final SubscriptionInfo si) {
        final int subId = si.getSubscriptionId();
        final int slot = si.getSimSlotIndex();
        LinearLayout c = card();

        CharSequence name = si.getDisplayName();
        if (name == null || name.length() == 0) name = si.getCarrierName();
        c.addView(text("SIM " + (slot + 1) + " · " + name, 16, true, null));
        c.addView(text("subId " + subId + " · slot " + slot, 11, false, muted()));

        boolean on = readVolte(subId);
        c.addView(text(on ? t("Current: on", "当前：已开启") : t("Current: off", "当前：未开启"), 13, false, null));

        final Switch sw = new Switch(this);
        sw.setText("VoLTE");
        sw.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        sw.setPadding(0, dp(8), 0, 0);
        sw.setChecked(on);
        sw.setEnabled(Boolean.TRUE.equals(adbOk) && !busy);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton v, boolean want) {
                if (!v.isPressed()) return;   // 只响应用户操作，忽略 setChecked
                toggle(subId, slot, want);
            }
        });
        c.addView(sw);
        return c;
    }

    private View stepsCard() {
        final String collapsed = t("How to use  ▸  tap to expand", "使用步骤  ▸  点开看怎么操作");
        final String expanded  = t("How to use  ▾  collapse", "使用步骤  ▾  收起");
        LinearLayout c = card();
        final TextView header = text(collapsed, 13, true, muted());
        header.setPadding(0, dp(2), 0, dp(2));
        c.addView(header);

        final LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setVisibility(View.GONE);
        body.setPadding(0, dp(6), 0, 0);

        body.addView(text(t("① First-time setup (once only)", "① 首次准备（只需做一次）"), 14, true, null));
        body.addView(text(t(
                "1. Turn on Wi-Fi and connect to a network.\n"
              + "    ⚠️ Wireless debugging only works while Wi-Fi is connected — required.\n"
              + "2. Enable Developer options:\n"
              + "    Settings → About phone → tap \"Build number\" 7 times\n"
              + "    until you see \"You are now a developer\".\n"
              + "3. Enable Wireless debugging:\n"
              + "    Settings → System → Developer options → Wireless debugging,\n"
              + "    turn it on and keep it on.\n"
              + "4. Back here, tap \"Start pairing\" in the ADB channel card below,\n"
              + "    then type the 6-digit code from the system into the notification.\n"
              + "    Once it shows \"Ready\" you are paired — no need to pair again.",
                "1. 先打开 Wi-Fi 并连上网络。\n"
              + "    ⚠️ 无线调试只有在 Wi-Fi 连着时才工作，这步必须做。\n"
              + "2. 打开「开发者选项」：\n"
              + "    设置 → 关于手机 → 连续点 7 下「版本号」，\n"
              + "    看到「已处于开发者模式」即可。\n"
              + "3. 打开「无线调试」：\n"
              + "    设置 → 系统 → 开发者选项 → 找到「无线调试」打开，\n"
              + "    并让它保持打开。\n"
              + "4. 回到本页，在下方「ADB 通道」卡片点「开始配对」，\n"
              + "    按提示把系统给的 6 位配对码填进通知里发送。\n"
              + "    看到「已就绪」就配对好了，以后不用再配。"),
                13, false, null));

        body.addView(text(t("② Daily use", "② 日常使用"), 14, true, null));
        body.addView(text(t(
                "1. Make sure the ADB channel below shows \"Ready\"\n"
              + "    (if unavailable, Wi-Fi or wireless debugging is usually off).\n"
              + "2. Flip the \"VoLTE\" switch of the SIM to turn it on / off.\n"
              + "3. For VoNR / 5G SA / VoWiFi as well: tick them in Options,\n"
              + "    then flip that SIM's switch again to write them.\n"
              + "4. The override is lost after a reboot — flip the switch again here,\n"
              + "    or tick \"Re-apply after boot\" to replay it automatically.",
                "1. 确认下方「ADB 通道」显示「已就绪」\n"
              + "    （若显示不可用，多半是 Wi-Fi 或无线调试没开）。\n"
              + "2. 拨动对应 SIM 卡的「VoLTE」开关，即可开启 / 关闭。\n"
              + "3. 想同时开 VoNR / 5G SA / VoWiFi：先在「选项」里勾选，\n"
              + "    再重新拨一次该 SIM 的开关才会写入。\n"
              + "4. 手机重启后覆盖会失效——回到本页重新拨一次开关，\n"
              + "    或勾选「开机后自动重新应用」让它自动重放。"),
                13, false, null));
        c.addView(body);

        header.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean show = body.getVisibility() != View.VISIBLE;
                body.setVisibility(show ? View.VISIBLE : View.GONE);
                header.setText(show ? expanded : collapsed);
            }
        });
        return c;
    }

    private LinearLayout optionsCard() {
        final SharedPreferences p = Config.prefs(this);
        LinearLayout c = card();
        c.addView(text(t("Options", "选项"), 12, true, muted()));
        c.addView(check(t("Also enable VoNR (5G calling)", "同时开启 VoNR（5G 通话）"), Config.K_VONR, true, p));
        c.addView(check(t("Also enable 5G SA (standalone)", "同时开启 5G SA（独立组网）"), Config.K_SA, false, p));
        c.addView(check(t("Also enable VoWiFi (usually no effect on China Unicom)",
                "同时开启 VoWiFi（国内联通通常无效）"), Config.K_VOWIFI, false, p));
        c.addView(check(t("Re-apply after boot", "开机后自动重新应用"), Config.K_BOOT, false, p));
        c.addView(text(t("After changing options, flip the SIM switch again to write them.",
                "改完选项要重新拨一次 SIM 的开关才会写入。"), 11, false, muted()));
        return c;
    }

    private CheckBox check(String label, final String key, boolean def, final SharedPreferences p) {
        CheckBox cb = new CheckBox(this);
        cb.setText(label);
        cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        cb.setChecked(p.getBoolean(key, def));
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton v, boolean on) {
                p.edit().putBoolean(key, on).apply();
            }
        });
        return cb;
    }

    // ---------------- 动作 ----------------

    private void toggle(final int subId, final int slot, final boolean on) {
        if (busy) return;
        busy = true;
        render();
        log((on ? t("Enabling", "开启") : t("Disabling", "关闭")) + " sub " + subId + " …");
        new Thread(new Runnable() {
            @Override public void run() {
                final Broker.Result r = Broker.apply(MainActivity.this, subId, slot, on);
                if (r.ok) {
                    Config.prefs(MainActivity.this).edit()
                            .putBoolean(Config.keyOn(subId), on).apply();
                }
                ui.post(new Runnable() {
                    @Override public void run() {
                        busy = false;
                        log((r.ok ? t("OK: ", "成功：") : t("Failed: ", "失败：")) + r.text);
                        render();
                    }
                });
            }
        }).start();
    }

    /** 读当前生效的 carrier config（公开 API，只要 READ_PHONE_STATE）。 */
    private boolean readVolte(int subId) {
        try {
            CarrierConfigManager ccm = getSystemService(CarrierConfigManager.class);
            PersistableBundle b = ccm.getConfigForSubId(subId, Config.PROBE_KEY);
            return b != null && b.getBoolean(Config.PROBE_KEY, false);
        } catch (Throwable t) {
            return Config.prefs(this).getBoolean(Config.keyOn(subId), false);
        }
    }

    private void openWirelessDebugging() {
        try {
            android.content.Intent i = new android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(":settings:fragment_args_key", "toggle_adb_wireless");
            startActivity(i);
        } catch (Throwable t) {
            log(t("Cannot open Developer options; go to Settings → System → Developer options → Wireless debugging manually",
                    "打不开开发者选项，请手动进入 设置 → 系统 → 开发者选项 → 无线调试"));
        }
    }

    private void log(String s) { if (logView != null) logView.setText(s); }

    // ---------------- 语言 ----------------

    private String t(String en, String zh) { return L.t(this, en, zh); }

    /** 标题 + 右上角的中/英切换按钮。 */
    private View headerRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(t("VoLTE Toggle", "VoLTE 开关"), 24, true, null);
        row.addView(title, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button lang = new Button(this);
        lang.setText(L.zh(this) ? "English" : "中文");
        lang.setAllCaps(false);
        lang.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                L.setZh(MainActivity.this, !L.zh(MainActivity.this));
                recreate();
            }
        });
        row.addView(lang);
        return row;
    }

    // ---------------- 小工具 ----------------

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    private boolean night() {
        int m = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return m == Configuration.UI_MODE_NIGHT_YES;
    }

    private Integer muted() {
        return night() ? Color.parseColor("#9AA0A6") : Color.parseColor("#5F6368");
    }

    private TextView text(String s, int sp, boolean bold, Integer color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        if (color != null) t.setTextColor(color);
        t.setPadding(0, dp(3), 0, dp(3));
        t.setGravity(Gravity.START);
        return t;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        bg.setColor(night() ? Color.parseColor("#1E2022") : Color.parseColor("#F1F3F4"));
        c.setBackground(bg);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(14);
        c.setLayoutParams(lp);
        return c;
    }
}
