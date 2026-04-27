package com.t3rmuxk1ng.unlimiteddatabypass.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.t3rmuxk1ng.unlimiteddatabypass.config.ISPDatabase;
import com.t3rmuxk1ng.unlimiteddatabypass.models.ISPConfig;
import com.t3rmuxk1ng.unlimiteddatabypass.services.BypassService;

/**
 * NETWORK CHANGE RECEIVER
 * Listens for network changes and reconfigures bypass
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "Network change detected: " + action);

        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ||
            action.equals("android.net.wifi.WIFI_STATE_CHANGED") ||
            action.equals("android.net.wifi.STATE_CHANGE")) {
            
            handleNetworkChange(context);
        }
    }

    private void handleNetworkChange(Context context) {
        ConnectivityManager cm = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return;

        boolean isConnected = false;
        String networkType = "Unknown";
        int networkSpeed = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                if (caps != null) {
                    isConnected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                    networkSpeed = caps.getLinkDownstreamBandwidthKbps();

                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        networkType = "WiFi";
                    } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        networkType = "Mobile";
                    }
                }
            }
        } else {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null) {
                isConnected = info.isConnected();
                networkType = info.getTypeName();
            }
        }

        Log.d(TAG, "Network: " + (isConnected ? "Connected" : "Disconnected") + 
                ", Type: " + networkType + ", Speed: " + networkSpeed + " Kbps");

        if (isConnected) {
            // Re-detect ISP
            ISPConfig ispConfig = ISPDatabase.getDefaultISP();
            
            // Restart bypass service if needed
            restartBypassIfNeeded(context, ispConfig);
        }
    }

    private void restartBypassIfNeeded(Context context, ISPConfig config) {
        // Check if bypass was active before network change
        boolean wasActive = context.getSharedPreferences("bypass_prefs", Context.MODE_PRIVATE)
                .getBoolean("bypass_active", false);

        if (wasActive) {
            Log.d(TAG, "Restarting bypass service after network change");
            
            Intent serviceIntent = new Intent(context, BypassService.class);
            serviceIntent.putExtra("isp_config", config);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
