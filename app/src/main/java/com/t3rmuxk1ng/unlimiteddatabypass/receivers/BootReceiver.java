package com.t3rmuxk1ng.unlimiteddatabypass.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.t3rmuxk1ng.unlimiteddatabypass.services.BypassService;

/**
 * BOOT RECEIVER - AUTO START ON DEVICE BOOT
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "Boot Received: " + action);

        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
            action.equals("android.intent.action.QUICKBOOT_POWERON")) {
            
            handleBoot(context);
        }
    }

    private void handleBoot(Context context) {
        Log.d(TAG, "Handling Boot - Checking Auto-Start Setting");
        
        try {
            SharedPreferences prefs = context.getSharedPreferences("bypass_prefs", Context.MODE_PRIVATE);
            boolean autoStart = prefs.getBoolean("auto_start", false);

            if (autoStart) {
                Log.d(TAG, "Auto-Start Enabled - Starting Bypass Service");
                
                // Delay to ensure system is ready
                new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                        
                        Intent serviceIntent = new Intent(context, BypassService.class);
                        serviceIntent.putExtra("isp_name", "Default ISP");
                        
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent);
                        } else {
                            context.startService(serviceIntent);
                        }
                        
                        Log.d(TAG, "Bypass Service Started");
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Boot start error: " + e.getMessage());
                    }
                }).start();
            }
        } catch (Exception e) {
            Log.e(TAG, "handleBoot error: " + e.getMessage());
        }
    }
}
