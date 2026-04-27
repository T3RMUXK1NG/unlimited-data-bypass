package com.t3rmuxk1ng.unlimiteddatabypass.engines;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

import com.t3rmuxk1ng.unlimiteddatabypass.models.ISPConfig;

/**
 * APN BYPASS ENGINE
 * Modifies APN settings to bypass data limits
 */
public class APNBypassEngine {

    private static final String TAG = "APNBypassEngine";
    private static final Uri APN_URI = Uri.parse("content://telephony/carriers");
    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    private Context context;
    private boolean isActive = false;
    private ISPConfig currentConfig;
    private int originalApnId = -1;

    public APNBypassEngine(Context context) {
        this.context = context;
    }

    /**
     * Start APN Bypass
     */
    public void start(ISPConfig config) {
        if (config == null) {
            Log.e(TAG, "Cannot start - no ISP config");
            return;
        }

        this.currentConfig = config;
        Log.d(TAG, "Starting APN Bypass for: " + config.getName());

        try {
            // Save original APN
            originalApnId = getCurrentApnId();
            Log.d(TAG, "Original APN ID: " + originalApnId);

            // Create bypass APN
            int bypassApnId = createBypassApn(config);
            if (bypassApnId != -1) {
                // Set as preferred APN
                setPreferredApn(bypassApnId);
                isActive = true;
                Log.d(TAG, "APN Bypass Active - ID: " + bypassApnId);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to start APN bypass: " + e.getMessage());
        }
    }

    /**
     * Stop APN Bypass
     */
    public void stop() {
        if (!isActive) return;

        Log.d(TAG, "Stopping APN Bypass");

        try {
            // Restore original APN
            if (originalApnId != -1) {
                setPreferredApn(originalApnId);
                Log.d(TAG, "Restored original APN");
            }

            // Delete bypass APNs
            deleteBypassApns();

        } catch (Exception e) {
            Log.e(TAG, "Failed to stop APN bypass: " + e.getMessage());
        }

        isActive = false;
    }

    /**
     * Create bypass APN
     */
    private int createBypassApn(ISPConfig config) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        // APN Configuration
        values.put(Telephony.Carriers.NAME, "UNLIMITED_BYPASS");
        values.put(Telephony.Carriers.APN, config.getApnName() != null ? config.getApnName() : "internet");
        values.put(Telephony.Carriers.PROXY, config.getApnProxy() != null ? config.getApnProxy() : "");
        values.put(Telephony.Carriers.PORT, config.getApnPort() != null ? config.getApnPort() : "80");
        values.put(Telephony.Carriers.USER, "");
        values.put(Telephony.Carriers.PASSWORD, "");
        values.put(Telephony.Carriers.SERVER, "");
        values.put(Telephony.Carriers.MMSC, "");
        values.put(Telephony.Carriers.TYPE, "default,supl,hipri");
        values.put(Telephony.Carriers.PROTOCOL, "IPV4V6");
        values.put(Telephony.Carriers.ROAMING_PROTOCOL, "IPV4V6");
        values.put(Telephony.Carriers.CARRIER_ENABLED, 1);
        values.put(Telephony.Carriers.BEARER_BITMASK, 0);
        values.put(Telephony.Carriers.MVNO_TYPE, "");
        values.put(Telephony.Carriers.MVNO_MATCH_DATA, "");

        // Insert APN
        Uri uri = resolver.insert(APN_URI, values);
        if (uri != null) {
            return Integer.parseInt(uri.getLastPathSegment());
        }

        return -1;
    }

    /**
     * Get current APN ID
     */
    private int getCurrentApnId() {
        Cursor cursor = context.getContentResolver().query(
                PREFERRED_APN_URI,
                new String[]{"_id"},
                null, null, null
        );

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                }
            } finally {
                cursor.close();
            }
        }

        return -1;
    }

    /**
     * Set preferred APN
     */
    private void setPreferredApn(int apnId) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", apnId);
        resolver.update(PREFERRED_APN_URI, values, null, null);
    }

    /**
     * Delete bypass APNs
     */
    private void deleteBypassApns() {
        context.getContentResolver().delete(
                APN_URI,
                Telephony.Carriers.NAME + " = ?",
                new String[]{"UNLIMITED_BYPASS"}
        );
    }

    public boolean isActive() {
        return isActive;
    }
}
