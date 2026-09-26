package com.xcl.volte;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
        setTitle(getString(R.string.app_name));

        ScrollView sv = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));
        sv.addView(root);
        setContentView(sv);

        root.addView(headerRow());
        root.addView(text(getString(R.string.intro), 13, false, muted()));

        root.addView(stepsCard());

        adbBox = new LinearLayout(this);
        adbBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(adbBox);

        simBox = new LinearLayout(this);
        simBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(simBox);

        root.addView(optionsCard());

        LinearLayout logCard = card();
        logCard.addView(text(getString(R.string.log_title), 12, true, muted()));
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
        log(getString(R.string.adb_probe));
        new Thread(new Runnable() {
            @Override public void run() {
                final boolean ok = Adb.check(MainActivity.this);
                ui.post(new Runnable() {
                    @Override public void run() {
                        probing = false;
                        adbOk = ok;
                        if (ok) PairReceiver.dismiss(MainActivity.this);
                        log(ok ? getString(R.string.adb_ready_log)
                                : getString(R.string.adb_unavail_log));
                        render();
                    }
                });
            }
        }).start();
    }

    private View adbCard() {
        LinearLayout c = card();
        c.addView(text(getString(R.string.adb_card_title), 12, true, muted()));

        if (adbOk == null) {
            c.addView(text(getString(R.string.checking), 14, false, null));
            return c;
        }
        if (adbOk) {
            c.addView(text(getString(R.string.adb_ready), 15, true, null));
            Button b = new Button(this);
            b.setText(getString(R.string.recheck));
            b.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { adbOk = null; render(); probeAdb(); }
            });
            c.addView(b);
            return c;
        }

        c.addView(text(getString(R.string.not_connected), 15, true, null));
        c.addView(text(getString(R.string.pair_steps),
                13, false, muted()));

        Button go = new Button(this);
        go.setText(getString(R.string.start_pairing));
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
                log(getString(R.string.pair_notif_posted));
            }
        });
        c.addView(go);

        c.addView(text(getString(R.string.manual_hint), 11, false, muted()));

        final EditText code = new EditText(this);
        code.setHint(getString(R.string.code_hint));
        code.setInputType(InputType.TYPE_CLASS_NUMBER);
        c.addView(code);

        Button pair = new Button(this);
        pair.setText(getString(R.string.pair));
        pair.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final String s = code.getText().toString().trim();
                if (s.isEmpty()) { log(getString(R.string.enter_code_first)); return; }
                log(getString(R.string.pairing));
                new Thread(new Runnable() {
                    @Override public void run() {
                        String msg;
                        boolean ok = false;
                        try {
                            Adb.pair(MainActivity.this, s);
                            ok = Adb.check(MainActivity.this);
                            msg = ok ? getString(R.string.paired_ok)
                                    : getString(R.string.paired_recheck);
                        } catch (Throwable t) {
                            String m = t.getMessage();
                            msg = getString(R.string.pair_failed, (m == null || m.isEmpty())
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
            c.addView(text(getString(R.string.need_phone_perm), 14, false, null));
            simBox.addView(c);
            return;
        }
        SubscriptionManager sm = getSystemService(SubscriptionManager.class);
        List<SubscriptionInfo> list = null;
        try { list = sm.getActiveSubscriptionInfoList(); } catch (Throwable ignored) {}
        if (list == null || list.isEmpty()) {
            LinearLayout c = card();
            c.addView(text(getString(R.string.no_sim), 14, false, null));
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
        c.addView(text(on ? getString(R.string.current_on) : getString(R.string.current_off), 13, false, null));

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
        final String collapsed = getString(R.string.steps_collapsed);
        final String expanded  = getString(R.string.steps_expanded);
        LinearLayout c = card();
        final TextView header = text(collapsed, 13, true, muted());
        header.setPadding(0, dp(2), 0, dp(2));
        c.addView(header);

        final LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setVisibility(View.GONE);
        body.setPadding(0, dp(6), 0, 0);

        body.addView(text(getString(R.string.setup_title), 14, true, null));
        body.addView(text(getString(R.string.setup_body),
                13, false, null));

        body.addView(text(getString(R.string.daily_title), 14, true, null));
        body.addView(text(getString(R.string.daily_body),
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
        c.addView(text(getString(R.string.options), 12, true, muted()));
        c.addView(check(getString(R.string.opt_vonr), Config.K_VONR, true, p));
        c.addView(check(getString(R.string.opt_sa), Config.K_SA, false, p));
        c.addView(check(getString(R.string.opt_vowifi), Config.K_VOWIFI, false, p));
        c.addView(check(getString(R.string.opt_boot), Config.K_BOOT, false, p));
        c.addView(text(getString(R.string.options_note), 11, false, muted()));
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
        log(getString(on ? R.string.toggling_on : R.string.toggling_off, subId));
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
                        log(getString(r.ok ? R.string.result_ok : R.string.result_fail, r.text));
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
            log(getString(R.string.no_dev_settings));
        }
    }

    private void log(String s) { if (logView != null) logView.setText(s); }

    // ---------------- 语言 ----------------

    @Override protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(L.wrap(base));
    }

    /** 标题 + 右上角的语言选择按钮。 */
    private View headerRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(getString(R.string.app_name), 24, true, null);
        row.addView(title, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button lang = new Button(this);
        lang.setText(L.NAMES[L.index(this)] + "  ▾");
        lang.setAllCaps(false);
        lang.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { pickLanguage(); }
        });
        row.addView(lang);
        return row;
    }

    private void pickLanguage() {
        final int cur = L.index(this);
        new AlertDialog.Builder(this)
                .setTitle(R.string.language)
                .setSingleChoiceItems(L.NAMES, cur, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        d.dismiss();
                        if (which == cur) return;
                        L.set(MainActivity.this, which);
                        recreate();
                    }
                })
                .show();
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
