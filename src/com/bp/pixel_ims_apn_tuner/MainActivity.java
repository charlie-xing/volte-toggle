package com.bp.pixel_ims_apn_tuner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private boolean queryingIms = false;
    private volatile boolean visible = false;
    private volatile boolean destroyed = false;
    private Boolean adbOk = null;   // null = not probed yet
    private final Map<Integer, ImsQueryTool.SlotStatus> slotLive = new HashMap<>();
    private final Set<Integer> pollingSubs = new HashSet<>();
    private final Map<Integer, Integer> pollGen = new HashMap<>();
    private final Set<Integer> expandedAdvanced = new HashSet<>();

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

    @Override protected void onStart() {
        super.onStart();
        visible = true;
        Adb.setKeepAlive(true);
    }

    @Override protected void onResume() {
        super.onResume();
        render();
        // When returning to the app after entering the pairing code in the notification shade,
        // the channel may have just become ready; re-probe so the UI updates automatically.
        if (!probing && !Boolean.TRUE.equals(adbOk)) {
            probeAdb();
        } else if (Boolean.TRUE.equals(adbOk) && !busy && !queryingIms && pollingSubs.isEmpty()) {
            queryImsLive();
        }
    }

    @Override protected void onStop() {
        super.onStop();
        visible = false;
        if (!isChangingConfigurations()) {
            pollingSubs.clear();
            Adb.closeWhenIdle();
        }
    }

    @Override protected void onDestroy() {
        destroyed = true;
        if (!isChangingConfigurations()) {
            Adb.closeWhenIdle();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] p, int[] r) {
        super.onRequestPermissionsResult(req, p, r);
        if (req == REQ_NOTIF && r.length > 0 && r[0] == PackageManager.PERMISSION_GRANTED) {
            PairReceiver.prompt(this);
            openWirelessDebugging();
            log(getString(R.string.pair_notif_posted));
        }
        render();
        if (req == REQ_PHONE && Boolean.TRUE.equals(adbOk)) queryImsLive();
    }

    // ---------------- ADB channel ----------------

    private void probeAdb() {
        if (probing) return;
        probing = true;
        log(getString(R.string.adb_probe));
        new Thread(new Runnable() {
            @Override public void run() {
                final boolean ok = Adb.check(MainActivity.this);
                if (ok) {
                    Broker.grantSelfPermissions(MainActivity.this);
                }
                ui.post(new Runnable() {
                    @Override public void run() {
                        probing = false;
                        adbOk = ok;
                        if (ok) PairReceiver.dismiss(MainActivity.this);
                        log(ok ? getString(R.string.adb_ready_log)
                                : getString(R.string.adb_unavail_log));
                        render();
                        if (ok) queryImsLive();
                    }
                });
            }
        }).start();
    }

    private void queryImsLive() {
        if (queryingIms || busy || !Boolean.TRUE.equals(adbOk)) return;
        final List<SubscriptionInfo> subs = getActiveSims();
        if (subs == null || subs.isEmpty()) return;
        queryingIms = true;
        render();
        new Thread(new Runnable() {
            @Override public void run() {
                final Map<Integer, ImsQueryTool.SlotStatus> result =
                        Broker.querySlotStatus(MainActivity.this, subs);
                ui.post(new Runnable() {
                    @Override public void run() {
                        queryingIms = false;
                        if (!result.isEmpty()) {
                            slotLive.clear();
                            slotLive.putAll(result);
                        }
                        render();
                    }
                });
            }
        }).start();
    }

    private List<SubscriptionInfo> getActiveSims() {
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) return null;
        try {
            SubscriptionManager sm = getSystemService(SubscriptionManager.class);
            return sm != null ? sm.getActiveSubscriptionInfoList() : null;
        } catch (Throwable ignored) {
            return null;
        }
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
                @Override public void onClick(View v) {
                    adbOk = null;
                    slotLive.clear();
                    render();
                    probeAdb();
                }
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
                            @Override public void run() {
                                adbOk = fo;
                                log(fm);
                                render();
                                if (fo) queryImsLive();
                            }
                        });
                    }
                }).start();
            }
        });
        c.addView(pair);
        return c;
    }

    // ---------------- Rendering ----------------

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
        if (list.size() >= 2) {
            final List<SubscriptionInfo> allSims = list;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            row.setPadding(0, dp(10), 0, 0);
            Button applyAllBtn = new Button(this);
            applyAllBtn.setText(getString(R.string.btn_apply_all));
            applyAllBtn.setAllCaps(false);
            applyAllBtn.setEnabled(Boolean.TRUE.equals(adbOk) && !busy);
            applyAllBtn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { applyAllSims(allSims); }
            });
            row.addView(applyAllBtn);
            simBox.addView(row);
        }
    }

    private View simCard(final SubscriptionInfo si) {
        final int subId = si.getSubscriptionId();
        final int slot = ImsQueryTool.resolveSlot(this, subId, si.getSimSlotIndex());
        final SharedPreferences p = Config.prefs(this);
        LinearLayout c = card();

        CharSequence name = si.getDisplayName();
        if (name == null || name.length() == 0) name = si.getCarrierName();
        String plmn = CarrierDb.resolvePlmn(this, si);
        c.addView(text("SIM " + (slot >= 0 ? (slot + 1) : "?") + " · " + name, 16, true, null));
        c.addView(text("subId " + subId + " · slot " + slot
                + (plmn.isEmpty() ? "" : " · PLMN " + plmn), 11, false, muted()));

        CarrierDb.Entry dbEntry = CarrierDb.lookup(this, plmn);
        Config.Baseline base = Config.readBaseline(this, subId);

        if (dbEntry != null) {
            c.addView(text(getString(R.string.sim_carrier_db_match,
                    dbEntry.mnoName, dbEntry.ratMode, dbEntry.inetName, dbEntry.remainingApnNames()),
                    12, false, muted()));
        } else if (!plmn.isEmpty()) {
            c.addView(text(getString(R.string.sim_carrier_db_none), 12, false, muted()));
        }

        c.addView(text(base.hasOverride
                ? getString(R.string.status_custom)
                : getString(R.string.status_system), 13, false, null));

        if (Boolean.TRUE.equals(adbOk)) {
            ImsQueryTool.SlotStatus st = slotLive.get(subId);
            if (st != null) {
                c.addView(text(
                        getString(st.imsRegistered ? R.string.ims_live_reg : R.string.ims_live_unreg),
                        13, true, st.imsRegistered ? accentOk() : accentWarn()));
                if (st.hasConfig) {
                    String active = st.formatActiveFeatures(getString(R.string.eff_cfg_none));
                    c.addView(text(getString(R.string.eff_cfg_fmt, active), 12, false, muted()));
                }
            } else if (queryingIms || pollingSubs.contains(subId)) {
                c.addView(text(getString(R.string.ims_live_checking), 12, false, muted()));
            }
        }

        // 1. In CARRIER_DB: prefill APN/VoLTE/VoWiFi from CARRIER_DB, VoNR/5G SA from CarrierConfig; all editable.
        // 2. Not in CARRIER_DB, present in Google profile: lock services already enabled in stock profile, APN disabled.
        // 3. Neither in CARRIER_DB nor in Google profile: all services editable, APN disabled.
        final boolean inDb = dbEntry != null;
        final boolean inGoogle = !inDb && base.hasAnyStockIms();

        final boolean defApn    = inDb;
        final boolean defVolte  = inDb ? !"WIFI_ONLY".equals(dbEntry.ratMode) : base.volte;
        final boolean defVowifi = inDb
                ? ("LTE_WIFI".equals(dbEntry.ratMode) || "WIFI_ONLY".equals(dbEntry.ratMode))
                : base.vowifi;
        final boolean defVonr   = base.vonr;
        final boolean defSa     = base.sa;

        final boolean lockApn    = !inDb;
        final boolean lockVolte  = inGoogle && base.volte;
        final boolean lockVowifi = inGoogle && base.vowifi;
        final boolean lockVonr   = inGoogle && base.vonr;
        final boolean lockSa     = inGoogle && base.sa;

        final boolean valVolte  = lockVolte  ? true  : p.getBoolean(Config.keyVolte(subId), defVolte);
        final boolean valVowifi = lockVowifi ? true  : p.getBoolean(Config.keyVowifi(subId), defVowifi);
        final boolean valVonr   = lockVonr   ? true  : p.getBoolean(Config.keyVonr(subId), defVonr);
        final boolean valSa     = lockSa     ? true  : p.getBoolean(Config.keySa(subId), defSa);
        final boolean valApn    = lockApn    ? false : p.getBoolean(Config.keyApn(subId), defApn);

        final boolean defS26Ua      = !inGoogle;
        final boolean valS26Ua      = p.getBoolean(Config.keyS26Ua(subId), defS26Ua);
        final boolean valVt         = p.getBoolean(Config.keyVt(subId), true);
        final boolean valWfcRoaming = p.getBoolean(Config.keyWfcRoaming(subId), true);
        final boolean valUt         = p.getBoolean(Config.keyUt(subId), true);
        final boolean valCrossSim   = p.getBoolean(Config.keyCrossSim(subId), true);
        final boolean valLtePlus    = p.getBoolean(Config.keyLtePlus(subId), true);
        final boolean valAllowApn   = p.getBoolean(Config.keyAllowApn(subId), true);

        final CheckBox cbVolte  = simCheck(getString(R.string.opt_volte),  valVolte,  lockVolte,  false, Config.keyVolte(subId));
        final CheckBox cbVowifi = simCheck(getString(R.string.opt_vowifi), valVowifi, lockVowifi, false, Config.keyVowifi(subId));
        final CheckBox cbVonr   = simCheck(getString(R.string.opt_vonr),   valVonr,   lockVonr,   false, Config.keyVonr(subId));
        final CheckBox cbSa     = simCheck(getString(R.string.opt_sa),     valSa,     lockSa,     false, Config.keySa(subId));
        final CheckBox cbApn    = simCheck(getString(R.string.opt_apn, CarrierDb.count(this)),
                valApn, false, lockApn, Config.keyApn(subId));

        final CheckBox cbS26Ua      = simCheck(getString(R.string.opt_s26_ua),      valS26Ua,      false, false, Config.keyS26Ua(subId));
        final CheckBox cbVt         = simCheck(getString(R.string.opt_vt),          valVt,         false, false, Config.keyVt(subId));
        final CheckBox cbWfcRoaming = simCheck(getString(R.string.opt_wfc_roaming), valWfcRoaming, false, false, Config.keyWfcRoaming(subId));
        final CheckBox cbUt         = simCheck(getString(R.string.opt_ut),          valUt,         false, false, Config.keyUt(subId));
        final CheckBox cbCrossSim   = simCheck(getString(R.string.opt_cross_sim),   valCrossSim,   false, false, Config.keyCrossSim(subId));
        final CheckBox cbLtePlus    = simCheck(getString(R.string.opt_lte_plus),    valLtePlus,    false, false, Config.keyLtePlus(subId));
        final CheckBox cbAllowApn   = simCheck(getString(R.string.opt_allow_apn),   valAllowApn,   false, false, Config.keyAllowApn(subId));

        c.addView(cbVolte);
        c.addView(cbVowifi);
        c.addView(cbVonr);
        c.addView(cbSa);
        c.addView(cbApn);

        // Collapsible per-SIM Advanced options
        final boolean advOpen = expandedAdvanced.contains(subId);
        final String advCollapsed = getString(R.string.adv_collapsed);
        final String advExpanded  = getString(R.string.adv_expanded);
        final TextView advHeader = text(advOpen ? advExpanded : advCollapsed, 12, true, muted());
        advHeader.setPadding(0, dp(6), 0, dp(4));
        c.addView(advHeader);

        final LinearLayout advBody = new LinearLayout(this);
        advBody.setOrientation(LinearLayout.VERTICAL);
        advBody.setPadding(dp(8), 0, 0, dp(4));
        advBody.setVisibility(advOpen ? View.VISIBLE : View.GONE);
        advBody.addView(cbS26Ua);
        advBody.addView(cbVt);
        advBody.addView(cbWfcRoaming);
        advBody.addView(cbUt);
        advBody.addView(cbCrossSim);
        advBody.addView(cbLtePlus);
        advBody.addView(cbAllowApn);
        c.addView(advBody);

        advHeader.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean show = advBody.getVisibility() != View.VISIBLE;
                advBody.setVisibility(show ? View.VISIBLE : View.GONE);
                advHeader.setText(show ? advExpanded : advCollapsed);
                if (show) expandedAdvanced.add(subId); else expandedAdvanced.remove(subId);
            }
        });

        final boolean hasOverrideOrApplied = base.hasOverride || p.getBoolean(Config.keyOn(subId), false);
        final Button applyBtn = new Button(this);
        final Button resetBtn = new Button(this);

        CompoundButton.OnCheckedChangeListener onOptionChanged = new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton v, boolean checked) {
                if (!v.isPressed()) return;
                String key = (String) v.getTag();
                p.edit().putBoolean(key, checked).putBoolean(Config.keyEdited(subId), true).apply();
                boolean canReset = !busy && (hasOverrideOrApplied
                        ? Boolean.TRUE.equals(adbOk)
                        : p.getBoolean(Config.keyEdited(subId), false));
                resetBtn.setEnabled(canReset);
            }
        };
        cbVolte.setOnCheckedChangeListener(onOptionChanged);
        cbVowifi.setOnCheckedChangeListener(onOptionChanged);
        cbVonr.setOnCheckedChangeListener(onOptionChanged);
        cbSa.setOnCheckedChangeListener(onOptionChanged);
        cbApn.setOnCheckedChangeListener(onOptionChanged);
        cbS26Ua.setOnCheckedChangeListener(onOptionChanged);
        cbVt.setOnCheckedChangeListener(onOptionChanged);
        cbWfcRoaming.setOnCheckedChangeListener(onOptionChanged);
        cbUt.setOnCheckedChangeListener(onOptionChanged);
        cbCrossSim.setOnCheckedChangeListener(onOptionChanged);
        cbLtePlus.setOnCheckedChangeListener(onOptionChanged);
        cbAllowApn.setOnCheckedChangeListener(onOptionChanged);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_HORIZONTAL);
        actions.setPadding(0, dp(8), 0, 0);

        applyBtn.setText(getString(R.string.btn_apply));
        applyBtn.setEnabled(Boolean.TRUE.equals(adbOk) && !busy);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                p.edit()
                        .putBoolean(Config.keyVolte(subId),      cbVolte.isChecked())
                        .putBoolean(Config.keyVowifi(subId),     cbVowifi.isChecked())
                        .putBoolean(Config.keyVonr(subId),       cbVonr.isChecked())
                        .putBoolean(Config.keySa(subId),         cbSa.isChecked())
                        .putBoolean(Config.keyApn(subId),        cbApn.isChecked())
                        .putBoolean(Config.keyS26Ua(subId),      cbS26Ua.isChecked())
                        .putBoolean(Config.keyVt(subId),         cbVt.isChecked())
                        .putBoolean(Config.keyWfcRoaming(subId), cbWfcRoaming.isChecked())
                        .putBoolean(Config.keyUt(subId),         cbUt.isChecked())
                        .putBoolean(Config.keyCrossSim(subId),   cbCrossSim.isChecked())
                        .putBoolean(Config.keyLtePlus(subId),    cbLtePlus.isChecked())
                        .putBoolean(Config.keyAllowApn(subId),   cbAllowApn.isChecked())
                        .apply();
                applySim(subId, slot);
            }
        });

        resetBtn.setText(getString(R.string.btn_reset));
        boolean canReset = !busy && (hasOverrideOrApplied
                ? Boolean.TRUE.equals(adbOk)
                : p.getBoolean(Config.keyEdited(subId), false));
        resetBtn.setEnabled(canReset);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!hasOverrideOrApplied) {
                    Config.clearSimPrefs(MainActivity.this, subId);
                    log(getString(R.string.reset_local_ok, subId));
                    render();
                } else {
                    resetSim(subId, slot);
                }
            }
        });

        LinearLayout.LayoutParams lpApply = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpApply.rightMargin = dp(8);
        actions.addView(applyBtn, lpApply);
        actions.addView(resetBtn);
        c.addView(actions);

        LinearLayout settingsRow = new LinearLayout(this);
        settingsRow.setOrientation(LinearLayout.HORIZONTAL);
        settingsRow.setGravity(Gravity.CENTER_HORIZONTAL);
        settingsRow.setPadding(0, dp(4), 0, 0);
        Button simSettingsBtn = new Button(this);
        simSettingsBtn.setText(getString(R.string.btn_sim_settings));
        simSettingsBtn.setAllCaps(false);
        simSettingsBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        simSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openSimSettings(subId); }
        });
        settingsRow.addView(simSettingsBtn);
        c.addView(settingsRow);
        return c;
    }

    private CheckBox simCheck(String label, boolean checked, boolean lockedBySystem,
                              boolean disabledPlain, String key) {
        CheckBox cb = new CheckBox(this);
        cb.setText(lockedBySystem ? getString(R.string.opt_system_profile, label) : label);
        cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        cb.setChecked(checked);
        cb.setEnabled(!lockedBySystem && !disabledPlain && !busy);
        cb.setTag(key);
        return cb;
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
        c.addView(check(getString(R.string.opt_boot), Config.K_BOOT, false, p));
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
                if (Config.K_BOOT.equals(key) && !on) {
                    BootReapplyJobService.cancel(MainActivity.this);
                }
            }
        });
        return cb;
    }

    // ---------------- Actions ----------------

    private void applySim(final int subId, final int slot) {
        if (busy) return;
        busy = true;
        pollingSubs.remove(subId);
        int gen = nextPollGen(subId);
        render();
        log(getString(R.string.applying, subId));
        final SharedPreferences p = Config.prefs(this);
        final boolean expectIms = p.getBoolean(Config.keyVolte(subId), true)
                || p.getBoolean(Config.keyVowifi(subId), false)
                || p.getBoolean(Config.keyVonr(subId), false);
        new Thread(new Runnable() {
            @Override public void run() {
                final Broker.Result r = Broker.apply(MainActivity.this, subId, slot, true, null, false);
                if (r.ok) {
                    Config.prefs(MainActivity.this).edit()
                            .putBoolean(Config.keyOn(subId), true).apply();
                }
                ui.post(new Runnable() {
                    @Override public void run() {
                        busy = false;
                        log(getString(r.ok ? R.string.result_ok : R.string.result_fail, r.text));
                        if (r.ok && !r.detached) {
                            slotLive.remove(subId);
                            pollingSubs.add(subId);
                            render();
                            pollImsAfterApply(subId, expectIms, gen);
                        } else {
                            render();
                        }
                    }
                });
            }
        }).start();
    }

    private int nextPollGen(int subId) {
        Integer prev = pollGen.get(subId);
        int next = (prev == null ? 0 : prev) + 1;
        pollGen.put(subId, next);
        return next;
    }

    /**
     * Non-blocking background IMS & effective CarrierConfig status poll after fast Apply so the UI
     * stays responsive even for data-only / roaming SIMs whose IMS cannot register.
     */
    private void pollImsAfterApply(final int subId, final boolean expectIms, final int gen) {
        final long[] delaysMs = expectIms ? new long[] { 5_000L, 7_000L } : new long[] { 2_000L };
        new Thread(new Runnable() {
            @Override public void run() {
                for (int i = 0; i < delaysMs.length; i++) {
                    try { Thread.sleep(delaysMs[i]); } catch (InterruptedException e) { return; }
                    if (destroyed || !visible) return;
                    // Avoid overlapping with another foreground am instrument call
                    while (busy && !destroyed && visible) {
                        try { Thread.sleep(500L); } catch (InterruptedException e) { return; }
                    }
                    if (destroyed || !visible) return;
                    final List<SubscriptionInfo> subs = getActiveSims();
                    if (subs == null || subs.isEmpty()) break;
                    final Map<Integer, ImsQueryTool.SlotStatus> result =
                            Broker.querySlotStatus(MainActivity.this, subs);
                    final ImsQueryTool.SlotStatus st = result.get(subId);
                    final boolean isLast = (i == delaysMs.length - 1);
                    final boolean matched = (st != null && st.imsRegistered == expectIms);
                    if (matched || isLast) {
                        ui.post(new Runnable() {
                            @Override public void run() {
                                Integer curGen = pollGen.get(subId);
                                if (curGen == null || curGen != gen) return;
                                pollingSubs.remove(subId);
                                if (!result.isEmpty()) slotLive.putAll(result);
                                render();
                            }
                        });
                        return;
                    }
                }
                ui.post(new Runnable() {
                    @Override public void run() {
                        Integer curGen = pollGen.get(subId);
                        if (curGen != null && curGen == gen) {
                            pollingSubs.remove(subId);
                            render();
                        }
                    }
                });
            }
        }).start();
    }

    private void resetSim(final int subId, final int slot) {
        if (busy) return;
        busy = true;
        pollingSubs.remove(subId);
        nextPollGen(subId);
        render();
        log(getString(R.string.resetting, subId));
        new Thread(new Runnable() {
            @Override public void run() {
                final Broker.Result r = Broker.apply(MainActivity.this, subId, slot, false);
                if (r.ok) {
                    Config.clearSimPrefs(MainActivity.this, subId);
                }
                ui.post(new Runnable() {
                    @Override public void run() {
                        busy = false;
                        slotLive.remove(subId);
                        if (r.ims != null) {
                            slotLive.put(subId, ImsQueryTool.SlotStatus.fromImsOnly(r.ims));
                        }
                        log(getString(r.ok ? R.string.result_ok : R.string.result_fail, r.text));
                        render();
                        if (!r.detached) {
                            queryImsLive();
                        }
                    }
                });
            }
        }).start();
    }

    private void ensureSimResolvedPrefsSaved(SubscriptionInfo si) {
        final int subId = si.getSubscriptionId();
        final SharedPreferences p = Config.prefs(this);
        String plmn = CarrierDb.resolvePlmn(this, si);
        CarrierDb.Entry dbEntry = CarrierDb.lookup(this, plmn);
        Config.Baseline base = Config.readBaseline(this, subId);

        boolean inDb = dbEntry != null;
        boolean inGoogle = !inDb && base.hasAnyStockIms();

        boolean defApn    = inDb;
        boolean defVolte  = inDb ? !"WIFI_ONLY".equals(dbEntry.ratMode) : base.volte;
        boolean defVowifi = inDb
                ? ("LTE_WIFI".equals(dbEntry.ratMode) || "WIFI_ONLY".equals(dbEntry.ratMode))
                : base.vowifi;
        boolean defVonr   = base.vonr;
        boolean defSa     = base.sa;

        boolean lockApn    = !inDb;
        boolean lockVolte  = inGoogle && base.volte;
        boolean lockVowifi = inGoogle && base.vowifi;
        boolean lockVonr   = inGoogle && base.vonr;
        boolean lockSa     = inGoogle && base.sa;

        p.edit()
                .putBoolean(Config.keyVolte(subId),      lockVolte  ? true  : p.getBoolean(Config.keyVolte(subId), defVolte))
                .putBoolean(Config.keyVowifi(subId),     lockVowifi ? true  : p.getBoolean(Config.keyVowifi(subId), defVowifi))
                .putBoolean(Config.keyVonr(subId),       lockVonr   ? true  : p.getBoolean(Config.keyVonr(subId), defVonr))
                .putBoolean(Config.keySa(subId),         lockSa     ? true  : p.getBoolean(Config.keySa(subId), defSa))
                .putBoolean(Config.keyApn(subId),        lockApn    ? false : p.getBoolean(Config.keyApn(subId), defApn))
                .putBoolean(Config.keyS26Ua(subId),      p.getBoolean(Config.keyS26Ua(subId), !inGoogle))
                .putBoolean(Config.keyVt(subId),         p.getBoolean(Config.keyVt(subId), true))
                .putBoolean(Config.keyWfcRoaming(subId), p.getBoolean(Config.keyWfcRoaming(subId), true))
                .putBoolean(Config.keyUt(subId),         p.getBoolean(Config.keyUt(subId), true))
                .putBoolean(Config.keyCrossSim(subId),   p.getBoolean(Config.keyCrossSim(subId), true))
                .putBoolean(Config.keyLtePlus(subId),    p.getBoolean(Config.keyLtePlus(subId), true))
                .putBoolean(Config.keyAllowApn(subId),   p.getBoolean(Config.keyAllowApn(subId), true))
                .apply();
    }

    private void applyAllSims(final List<SubscriptionInfo> sims) {
        if (busy || sims == null || sims.isEmpty()) return;
        busy = true;
        final Map<Integer, Integer> gens = new HashMap<>();
        final Map<Integer, Boolean> expects = new HashMap<>();
        final SharedPreferences p = Config.prefs(this);
        for (SubscriptionInfo si : sims) {
            ensureSimResolvedPrefsSaved(si);
            int subId = si.getSubscriptionId();
            pollingSubs.remove(subId);
            gens.put(subId, nextPollGen(subId));
            boolean expectIms = p.getBoolean(Config.keyVolte(subId), true)
                    || p.getBoolean(Config.keyVowifi(subId), false)
                    || p.getBoolean(Config.keyVonr(subId), false);
            expects.put(subId, expectIms);
        }
        render();
        log(getString(R.string.applying_all));
        new Thread(new Runnable() {
            @Override public void run() {
                if (ApplyMode.isDetached()) {
                    for (SubscriptionInfo si : sims) {
                        Config.prefs(MainActivity.this).edit()
                                .putBoolean(Config.keyOn(si.getSubscriptionId()), true).apply();
                    }
                    final Broker.Result r = Broker.applyDetachedBatch(MainActivity.this, false);
                    ui.post(new Runnable() {
                        @Override public void run() {
                            busy = false;
                            log(getString(r.ok ? R.string.result_ok : R.string.result_fail, r.text));
                            render();
                        }
                    });
                    return;
                }

                final StringBuilder summary = new StringBuilder();
                boolean allOk = true;
                final List<Integer> okSubs = new java.util.ArrayList<>();
                for (SubscriptionInfo si : sims) {
                    int subId = si.getSubscriptionId();
                    int slot = ImsQueryTool.resolveSlot(MainActivity.this, subId, si.getSimSlotIndex());
                    Broker.Result r = Broker.apply(MainActivity.this, subId, slot, true, null, false);
                    if (r.ok) {
                        Config.prefs(MainActivity.this).edit()
                                .putBoolean(Config.keyOn(subId), true).apply();
                        okSubs.add(subId);
                    } else {
                        allOk = false;
                    }
                    if (summary.length() > 0) summary.append(" | ");
                    summary.append("SIM ").append(slot >= 0 ? (slot + 1) : subId).append(": ").append(r.text);
                }
                final boolean finalOk = allOk;
                final String finalText = summary.toString();
                ui.post(new Runnable() {
                    @Override public void run() {
                        busy = false;
                        log(getString(finalOk ? R.string.result_ok : R.string.result_fail, finalText));
                        for (Integer subId : okSubs) {
                            slotLive.remove(subId);
                            pollingSubs.add(subId);
                        }
                        render();
                        for (Integer subId : okSubs) {
                            Integer g = gens.get(subId);
                            Boolean exp = expects.get(subId);
                            if (g != null && exp != null) {
                                pollImsAfterApply(subId, exp, g);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void openSimSettings(int subId) {
        try {
            Intent i = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                i.putExtra(Settings.EXTRA_SUB_ID, subId);
            }
            startActivity(i);
            return;
        } catch (Throwable ignored) {}
        try {
            Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Throwable ignored) {}
    }

    private void openWirelessDebugging() {
        try {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(":settings:fragment_args_key", "toggle_adb_wireless");
            startActivity(i);
        } catch (Throwable t) {
            log(getString(R.string.no_dev_settings));
        }
    }

    private void log(String s) { if (logView != null) logView.setText(s); }

    // ---------------- Language ----------------

    @Override protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(L.wrap(base));
    }

    /** Title + language selector button in the top-right corner. */
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

    // ---------------- Helpers ----------------

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

    private Integer accentOk() {
        return night() ? Color.parseColor("#81C995") : Color.parseColor("#188038");
    }

    private Integer accentWarn() {
        return night() ? Color.parseColor("#FDD663") : Color.parseColor("#B06000");
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
