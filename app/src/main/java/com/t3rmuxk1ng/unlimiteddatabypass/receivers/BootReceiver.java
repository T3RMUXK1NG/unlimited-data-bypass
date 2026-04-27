package com.t3rmuxk1ng.unlimiteddatabypass.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Build;
import android.util.Log;

import com.t3rmuxk1ng.unlimiteddatabypass.config.ISPDatabase;
import com.t3rmuxk1ng.unlimiteddatabypass.models.ISPConfig;
import com.t3rmuxk1ng.unlimiteddatabypass.services.BypassService;

/**
 * BOOT RECEIVER - AUTO START ON DEVICE BOOT
 * Automatically starts bypass service when device boots
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    private static final String PREFS_NAME = "bypass_prefs";
    private static final String KEY_AUTO_START = "auto_start";
    private static final String KEY_LAST_ISP = "last_isp_index";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot Received: " + intent.getAction());
        
        if (intent == null || intent.getAction() == null) {
            return;
        }
        
        String action = intent.getAction();
        
        // Handle boot events
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
            action.equals(Intent.ACTION_REBOOT) ||
            action.equals("android.intent.action.QUICKBOOT_POWERON") ||
            action.equals("com.htc.intent.action.QUICKBOOT_POWERON")) {
            
            handleBoot(context);
        }
        
        // Handle shutdown
        if (action.equals(Intent.ACTION_SHUTDOWN)) {
            handleShutdown(context);
        }
    }

    private void handleBoot(Context context) {
        Log.d(TAG, "Handling Boot - Checking Auto-Start Setting");
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean autoStart = prefs.getBoolean(KEY_AUTO_START, true);
        
        if (autoStart) {
            Log.d(TAG, "Auto-Start Enabled - Starting Bypass Service");
            
            // Delay to ensure system is ready
            new Thread(() -> {
                try {
                    Thread.sleep(10000); // Wait 10 seconds for system to fully boot
                    
                    // Get last used ISP
                    int lastIspIndex = prefs.getInt(KEY_LAST_ISP, 0);
                    ISPConfig ispConfig = ISPDatabase.getISPByIndex(lastIspIndex);
                    
                    // Start bypass service
                    startBypassService(context, ispConfig);
                    
                } catch (InterruptedException e) {
                    Log.e(TAG, "Boot delay interrupted: " + e.getMessage());
                }
            }).start();
        } else {
            Log.d(TAG, "Auto-Start Disabled - Skipping");
        }
    }

    private void handleShutdown(Context context) {
        Log.d(TAG, "Device Shutting Down - Saving State");
        
        // Stop bypass service gracefully
        Intent serviceIntent = new Intent(context, BypassService.class);
        context.stopService(serviceIntent);
    }

    private void startBypassService(Context context, ISPConfig ispConfig) {
        Log.d(TAG, "Starting Bypass Service");
        
        // Check VPN permission
        Intent vpnPrepare = VpnService.prepare(context);
        if (vpnPrepare == null) {
            // VPN permission already granted
            Intent serviceIntent = new Intent(context, BypassService.class);
            serviceIntent.putExtra("isp_config", ispConfig);
            serviceIntent.putExtra("apn_bypass", true);
            serviceIntent.putExtra("dns_bypass", true);
            serviceIntent.putExtra("header_bypass", true);
            serviceIntent.putExtra("proxy_bypass", true);
            serviceIntent.putExtra("tunnel_bypass", true);
            serviceIntent.putExtra("vpn_bypass", true);
            serviceIntent.putExtra("5g_boost", true);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            
            Log.d(TAG, "Bypass Service Started Successfully");
        } else {
            Log.w(TAG, "VPN Permission Not Granted - Cannot Auto-Start");
        }
    }
}
