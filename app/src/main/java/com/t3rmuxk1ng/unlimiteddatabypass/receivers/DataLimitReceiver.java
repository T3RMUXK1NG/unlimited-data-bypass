package com.t3rmuxk1ng.unlimiteddatabypass.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.t3rmuxk1ng.unlimiteddatabypass.services.BypassService;

/**
 * DATA LIMIT RECEIVER
 * Handles data limit reset events
 */
public class DataLimitReceiver extends BroadcastReceiver {

    private static final String TAG = "DataLimitReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "Data limit event: " + action);

        // Handle data limit reset
        if (action.equals("com.t3rmuxk1ng.unlimiteddatabypass.DATA_LIMIT_RESET")) {
            handleDataLimitReset(context);
        }
    }

    private void handleDataLimitReset(Context context) {
        Log.d(TAG, "Handling data limit reset");

        // Reset data counters
        context.getSharedPreferences("bypass_stats", Context.MODE_PRIVATE)
                .edit()
                .putLong("data_bypassed", 0)
                .putLong("data_saved", 0)
                .apply();

        Log.d(TAG, "Data counters reset");
    }
}
