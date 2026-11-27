package com.bp.pixel_ims_apn_tuner;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Provisions persistent APN profiles (Internet, IMS, MMS, XCAP, DUN, etc.) from CARRIER_DB
 * in content://telephony/carriers using the delegated shell identity (WRITE_APN_SETTINGS)
 * inside VolteInstrumentation, and restores factory APNs on reset.
 */
public final class ApnProvisioner {

    private static final Uri CARRIERS_URI = Uri.parse("content://telephony/carriers");

    public static final class Result {
        public final boolean provisioned;
        public final long inetId;
        public final long imsId;
        public final int totalCount;
        public final String summary;

        Result(boolean provisioned, long inetId, long imsId, int totalCount, String summary) {
            this.provisioned = provisioned;
            this.inetId = inetId;
            this.imsId = imsId;
            this.totalCount = totalCount;
            this.summary = summary;
        }
    }

    public static Result provision(Context ctx, int subId, CarrierDb.Entry e) {
        if (e == null) {
            return new Result(false, -1, -1, 0, "no CARRIER_DB entry");
        }
        ContentResolver cr = ctx.getContentResolver();
        SchemaFlags flags = probeSchema(cr);

        long inetId = -1L;
        long imsId = -1L;
        int count = 0;
        boolean primaryDefaultDone = false;

        for (CarrierDb.ApnSpec spec : e.apns) {
            if (spec.isIms()) {
                imsId = upsertImsApn(cr, subId, e, spec, flags);
                if (imsId > 0) count++;
            } else if (spec.isDefault() && !primaryDefaultDone) {
                primaryDefaultDone = true;
                inetId = upsertInternetApn(cr, subId, e, spec, flags);
                if (inetId > 0) {
                    count++;
                    if (subId > 0) setPreferredApn(cr, subId, inetId);
                }
            } else {
                long extraId = upsertExtraApn(cr, subId, e, spec, flags);
                if (extraId > 0) count++;
            }
        }

        String summary = e.mnoName + " (" + e.ratMode + ", " + count + " APNs)";
        Log.i(VolteInstrumentation.TAG, "APN provisioned: " + summary);
        return new Result(count > 0, inetId, imsId, count, summary);
    }

    /**
     * Restores factory APN settings for subId via TelephonyProvider's restore URI.
     */
    public static void restoreDefaultApns(Context ctx, int subId) {
        ContentResolver cr = ctx.getContentResolver();
        try {
            Uri uri = subId > 0
                    ? Uri.parse("content://telephony/carriers/restore/subId/" + subId)
                    : Uri.parse("content://telephony/carriers/restore");
            cr.delete(uri, null, null);
            Log.i(VolteInstrumentation.TAG, "Restored default APNs for subId=" + subId);
        } catch (Throwable t) {
            Log.w(VolteInstrumentation.TAG, "restoreDefaultApns failed (non-fatal)", t);
        }
    }

    private static final class SchemaFlags {
        boolean hasLingeringBitmask;
        boolean hasInfrastructureBitmask;
    }

    private static SchemaFlags probeSchema(ContentResolver cr) {
        SchemaFlags flags = new SchemaFlags();
        try (Cursor c = cr.query(CARRIERS_URI, null, "_id < 100", null, null)) {
            if (c != null) {
                for (String col : c.getColumnNames()) {
                    if ("lingering_network_type_bitmask".equals(col)) flags.hasLingeringBitmask = true;
                    if ("infrastructure_bitmask".equals(col)) flags.hasInfrastructureBitmask = true;
                }
            }
        } catch (Throwable t) {
            Log.w(VolteInstrumentation.TAG, "Schema probe failed", t);
        }
        return flags;
    }

    private static List<Long> queryIds(ContentResolver cr, String where, String[] args) {
        List<Long> ids = new ArrayList<>();
        try (Cursor c = cr.query(CARRIERS_URI, new String[] { "_id" }, where, args, "_id ASC")) {
            if (c != null) {
                while (c.moveToNext()) {
                    ids.add(c.getLong(0));
                }
            }
        }
        return ids;
    }

    private static void putOptionalFields(ContentValues cv, CarrierDb.ApnSpec spec) {
        cv.put("user", spec.user);
        cv.put("password", spec.password);
        cv.put("proxy", spec.proxy);
        cv.put("port", spec.port);
        cv.put("mmsc", spec.mmsc);
        cv.put("mmsproxy", spec.mmsproxy);
        cv.put("mmsport", spec.mmsport);
        if (spec.mtu > 0) {
            cv.put("mtu", spec.mtu);
        }
    }

    private static long upsertInternetApn(ContentResolver cr, int subId, CarrierDb.Entry e,
                                          CarrierDb.ApnSpec spec, SchemaFlags flags) {
        List<Long> ids = queryIds(
                cr,
                "numeric=? AND LOWER(type) LIKE '%default%' AND LOWER(apn)!='ims'",
                new String[] { e.numeric });

        long keepId = ids.isEmpty() ? -1L : ids.get(0);
        for (int i = 1; i < ids.size(); i++) {
            cr.delete(CARRIERS_URI, "_id=?", new String[] { String.valueOf(ids.get(i)) });
        }

        ContentValues cv = new ContentValues();
        cv.put("name", spec.name);
        cv.put("numeric", e.numeric);
        cv.put("mcc", e.mcc);
        cv.put("mnc", e.mnc);
        cv.put("apn", spec.apn);
        putOptionalFields(cv, spec);
        cv.put("authtype", spec.authtype);
        cv.put("type", spec.type);
        cv.put("protocol", spec.protocol);
        cv.put("roaming_protocol", spec.roamingProtocol);
        cv.put("carrier_enabled", 1);
        cv.put("current", 1);
        cv.put("bearer", 0);
        cv.put("bearer_bitmask", CarrierDb.INET_BEARER_BITMASK);
        cv.put("network_type_bitmask", CarrierDb.INET_NETTYPE_BITMASK);
        cv.put("sub_id", subId);
        cv.put("edited", 1);
        cv.put("user_visible", 1);
        cv.put("user_editable", 1);
        cv.put("owned_by", 1);
        if (flags.hasLingeringBitmask) {
            cv.put("lingering_network_type_bitmask", CarrierDb.INET_NETTYPE_BITMASK);
        }
        if (flags.hasInfrastructureBitmask) {
            cv.put("infrastructure_bitmask", 3);
        }

        if (keepId > 0) {
            cr.update(CARRIERS_URI, cv, "_id=?", new String[] { String.valueOf(keepId) });
            return keepId;
        }
        Uri inserted = cr.insert(CARRIERS_URI, cv);
        if (inserted != null) {
            try {
                return Long.parseLong(inserted.getLastPathSegment());
            } catch (Throwable ignored) {}
        }
        List<Long> check = queryIds(
                cr,
                "numeric=? AND LOWER(apn)=LOWER(?) AND LOWER(type) LIKE '%default%'",
                new String[] { e.numeric, spec.apn });
        return check.isEmpty() ? -1L : check.get(0);
    }

    private static long upsertImsApn(ContentResolver cr, int subId, CarrierDb.Entry e,
                                     CarrierDb.ApnSpec spec, SchemaFlags flags) {
        List<Long> ids = queryIds(
                cr,
                "numeric=? AND (LOWER(apn)=LOWER(?) OR LOWER(type) LIKE '%ims%')",
                new String[] { e.numeric, spec.apn });

        long keepId = ids.isEmpty() ? -1L : ids.get(0);
        for (int i = 1; i < ids.size(); i++) {
            cr.delete(CARRIERS_URI, "_id=?", new String[] { String.valueOf(ids.get(i)) });
        }

        int imsNetmask = e.imsNetmask();
        int imsBearermask = e.imsBearermask();

        ContentValues cv = new ContentValues();
        cv.put("name", spec.name);
        cv.put("numeric", e.numeric);
        cv.put("mcc", e.mcc);
        cv.put("mnc", e.mnc);
        cv.put("apn", spec.apn);
        putOptionalFields(cv, spec);
        cv.put("type", "ims");
        cv.put("protocol", spec.protocol);
        cv.put("roaming_protocol", spec.roamingProtocol);
        cv.put("authtype", spec.authtype);
        cv.put("carrier_enabled", 1);
        cv.put("bearer", 0);
        cv.put("bearer_bitmask", imsBearermask);
        cv.put("network_type_bitmask", imsNetmask);
        cv.put("sub_id", subId);
        cv.put("edited", 1);
        cv.put("user_visible", 1);
        cv.put("user_editable", 1);
        cv.put("owned_by", 1);
        if (flags.hasLingeringBitmask) {
            cv.put("lingering_network_type_bitmask", imsNetmask);
        }
        if (flags.hasInfrastructureBitmask) {
            cv.put("infrastructure_bitmask", 3);
        }

        if (keepId > 0) {
            cr.update(CARRIERS_URI, cv, "_id=?", new String[] { String.valueOf(keepId) });
            return keepId;
        }
        Uri inserted = cr.insert(CARRIERS_URI, cv);
        if (inserted != null) {
            try {
                return Long.parseLong(inserted.getLastPathSegment());
            } catch (Throwable ignored) {}
        }
        List<Long> check = queryIds(
                cr,
                "numeric=? AND (LOWER(apn)=LOWER(?) OR LOWER(type) LIKE '%ims%')",
                new String[] { e.numeric, spec.apn });
        return check.isEmpty() ? -1L : check.get(0);
    }

    private static long upsertExtraApn(ContentResolver cr, int subId, CarrierDb.Entry e,
                                       CarrierDb.ApnSpec spec, SchemaFlags flags) {
        List<Long> ids = queryIds(
                cr,
                "numeric=? AND (LOWER(type)=LOWER(?) OR (LOWER(apn)=LOWER(?) AND LOWER(type)=LOWER(?)))",
                new String[] { e.numeric, spec.type, spec.apn, spec.type });

        long keepId = ids.isEmpty() ? -1L : ids.get(0);
        for (int i = 1; i < ids.size(); i++) {
            cr.delete(CARRIERS_URI, "_id=?", new String[] { String.valueOf(ids.get(i)) });
        }

        ContentValues cv = new ContentValues();
        cv.put("name", spec.name);
        cv.put("numeric", e.numeric);
        cv.put("mcc", e.mcc);
        cv.put("mnc", e.mnc);
        cv.put("apn", spec.apn);
        putOptionalFields(cv, spec);
        cv.put("authtype", spec.authtype);
        cv.put("type", spec.type);
        cv.put("protocol", spec.protocol);
        cv.put("roaming_protocol", spec.roamingProtocol);
        cv.put("carrier_enabled", 1);
        cv.put("current", 1);
        cv.put("bearer", 0);
        cv.put("bearer_bitmask", CarrierDb.INET_BEARER_BITMASK);
        cv.put("network_type_bitmask", CarrierDb.INET_NETTYPE_BITMASK);
        cv.put("sub_id", subId);
        cv.put("edited", 1);
        cv.put("user_visible", 1);
        cv.put("user_editable", 1);
        cv.put("owned_by", 1);
        if (flags.hasLingeringBitmask) {
            cv.put("lingering_network_type_bitmask", CarrierDb.INET_NETTYPE_BITMASK);
        }
        if (flags.hasInfrastructureBitmask) {
            cv.put("infrastructure_bitmask", 3);
        }

        if (keepId > 0) {
            cr.update(CARRIERS_URI, cv, "_id=?", new String[] { String.valueOf(keepId) });
            return keepId;
        }
        Uri inserted = cr.insert(CARRIERS_URI, cv);
        if (inserted != null) {
            try {
                return Long.parseLong(inserted.getLastPathSegment());
            } catch (Throwable ignored) {}
        }
        List<Long> check = queryIds(
                cr,
                "numeric=? AND LOWER(apn)=LOWER(?) AND LOWER(type)=LOWER(?)",
                new String[] { e.numeric, spec.apn, spec.type });
        return check.isEmpty() ? -1L : check.get(0);
    }

    private static void setPreferredApn(ContentResolver cr, int subId, long inetId) {
        try {
            Uri uri = Uri.parse("content://telephony/carriers/preferapn/subId/" + subId);
            ContentValues cv = new ContentValues();
            cv.put("apn_id", inetId);
            cr.update(uri, cv, null, null);
        } catch (Throwable t) {
            Log.w(VolteInstrumentation.TAG, "setPreferredApn failed (non-fatal)", t);
        }
    }

    private ApnProvisioner() {}
}
