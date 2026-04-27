package com.t3rmuxk1ng.unlimiteddatabypass.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.t3rmuxk1ng.unlimiteddatabypass.R;
import com.t3rmuxk1ng.unlimiteddatabypass.config.ISPDatabase;
import com.t3rmuxk1ng.unlimiteddatabypass.models.ISPConfig;
import com.t3rmuxk1ng.unlimiteddatabypass.services.BypassService;
import com.t3rmuxk1ng.unlimiteddatabypass.utils.SpeedMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * MAIN ACTIVITY - UNLIMITED DATA BYPASS
 * GOD TIER EDITION v1.0
 * Created by T3rmuxk1ng
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int VPN_REQUEST_CODE = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1002;
    private static final String CHANNEL_ID = "bypass_channel";

    // UI Components
    private TextView tvStatus, tvISP, tvNetworkType;
    private TextView tvDownloadSpeed, tvUploadSpeed, tvPing;
    private Button btnActivate, btnSelectISP;
    
    // Feature Switches
    private Switch switchApn, switchDns, switchHeader, switchProxy;
    private Switch switchTunnel, switchVpn, switch5g, switchUnlimited;
    
    // State
    private boolean isBypassActive = false;
    private ISPConfig currentISP;
    private SpeedMonitor speedMonitor;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "onCreate: Activity starting...");
            
            random = new Random();
            initViews();
            checkPermissions();
            setupClickListeners();
            createNotificationChannel();
            detectISP();
            startSpeedMonitor();
            
            Log.d(TAG, "onCreate: Activity started successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "onCreate Error: " + e.getMessage(), e);
            Toast.makeText(this, "Error starting app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        Log.d(TAG, "initViews: Initializing views...");
        
        // Status Card
        tvStatus = findViewById(R.id.tvStatus);
        tvISP = findViewById(R.id.tvISP);
        tvNetworkType = findViewById(R.id.tvNetworkType);
        
        // Speed Card
        tvDownloadSpeed = findViewById(R.id.tvDownloadSpeed);
        tvUploadSpeed = findViewById(R.id.tvUploadSpeed);
        tvPing = findViewById(R.id.tvPing);
        
        // Feature Switches
        switchApn = findViewById(R.id.switchApn);
        switchDns = findViewById(R.id.switchDns);
        switchHeader = findViewById(R.id.switchHeader);
        switchProxy = findViewById(R.id.switchProxy);
        switchTunnel = findViewById(R.id.switchTunnel);
        switchVpn = findViewById(R.id.switchVpn);
        switch5g = findViewById(R.id.switch5g);
        switchUnlimited = findViewById(R.id.switchUnlimited);
        
        // Buttons
        btnActivate = findViewById(R.id.btnActivate);
        btnSelectISP = findViewById(R.id.btnSelectISP);
        
        // Set default ISP
        currentISP = ISPDatabase.getDefaultISP();
        
        Log.d(TAG, "initViews: Views initialized");
    }

    private void checkPermissions() {
        Log.d(TAG, "checkPermissions: Checking permissions...");
        
        List<String> permissionsNeeded = new ArrayList<>();
        
        String[] basicPermissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        };
        
        for (String permission : basicPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        
        // Add notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            Log.d(TAG, "checkPermissions: Requesting " + permissionsNeeded.size() + " permissions");
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "checkPermissions: All permissions granted");
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up click listeners...");
        
        btnActivate.setOnClickListener(v -> {
            try {
                if (isBypassActive) {
                    deactivateBypass();
                } else {
                    activateBypass();
                }
            } catch (Exception e) {
                Log.e(TAG, "Button click error: " + e.getMessage(), e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        btnSelectISP.setOnClickListener(v -> showISPSelector());
        
        Log.d(TAG, "setupClickListeners: Click listeners set up");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Bypass Status",
                        NotificationManager.IMPORTANCE_LOW
                );
                channel.setDescription("Unlimited Data Bypass Status");
                
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                    Log.d(TAG, "Notification channel created");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel: " + e.getMessage());
            }
        }
    }

    private void detectISP() {
        Log.d(TAG, "detectISP: Detecting ISP...");
        
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) 
                    == PackageManager.PERMISSION_GRANTED) {
                String operatorName = tm.getNetworkOperatorName();
                String mccMnc = tm.getNetworkOperator();
                
                currentISP = ISPDatabase.detectISP(operatorName, mccMnc);
                if (tvISP != null) {
                    tvISP.setText("ISP: " + (currentISP != null ? currentISP.getName() : "Unknown"));
                }
                
                // Detect network type
                int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
                try {
                    networkType = tm.getDataNetworkType();
                } catch (Exception e) {
                    Log.e(TAG, "Error getting network type: " + e.getMessage());
                }
                
                String networkName = getNetworkTypeName(networkType);
                if (tvNetworkType != null) {
                    tvNetworkType.setText("Network: " + networkName);
                }
            } else {
                currentISP = ISPDatabase.getDefaultISP();
                if (tvISP != null) {
                    tvISP.setText("ISP: " + (currentISP != null ? currentISP.getName() : "Select ISP"));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "detectISP Error: " + e.getMessage(), e);
            currentISP = ISPDatabase.getDefaultISP();
        }
    }
    
    private String getNetworkTypeName(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G/4G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G LTE";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return "Unknown";
        }
    }

    private void showISPSelector() {
        try {
            String[] ispNames = ISPDatabase.getAllISPNames();
            
            new AlertDialog.Builder(this)
                .setTitle("Select Your ISP")
                .setItems(ispNames, (dialog, which) -> {
                    currentISP = ISPDatabase.getISPByIndex(which);
                    if (tvISP != null && currentISP != null) {
                        tvISP.setText("ISP: " + currentISP.getName());
                    }
                    Toast.makeText(this, "Selected: " + (currentISP != null ? currentISP.getName() : "Unknown"), 
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        } catch (Exception e) {
            Log.e(TAG, "showISPSelector Error: " + e.getMessage(), e);
            Toast.makeText(this, "Error showing ISP selector", Toast.LENGTH_SHORT).show();
        }
    }

    private void activateBypass() {
        Log.d(TAG, "activateBypass: Activating bypass...");
        
        if (currentISP == null) {
            Toast.makeText(this, "Please select ISP first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Request VPN permission
            Intent vpnIntent = VpnService.prepare(this);
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
                return;
            }
            
            // Start bypass
            startBypass();
            
        } catch (Exception e) {
            Log.e(TAG, "activateBypass Error: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startBypass() {
        Log.d(TAG, "startBypass: Starting bypass service...");
        
        isBypassActive = true;
        updateStatusUI(true);
        
        // Start Bypass Service
        try {
            Intent serviceIntent = new Intent(this, BypassService.class);
            if (currentISP != null) {
                serviceIntent.putExtra("isp_name", currentISP.getName());
            }
            serviceIntent.putExtra("apn_bypass", switchApn != null && switchApn.isChecked());
            serviceIntent.putExtra("dns_bypass", switchDns != null && switchDns.isChecked());
            serviceIntent.putExtra("header_bypass", switchHeader != null && switchHeader.isChecked());
            serviceIntent.putExtra("proxy_bypass", switchProxy != null && switchProxy.isChecked());
            serviceIntent.putExtra("tunnel_bypass", switchTunnel != null && switchTunnel.isChecked());
            serviceIntent.putExtra("vpn_bypass", switchVpn != null && switchVpn.isChecked());
            serviceIntent.putExtra("5g_boost", switch5g != null && switch5g.isChecked());
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            
            Log.d(TAG, "startBypass: Service started");
            Toast.makeText(this, "Bypass Activated!", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Log.e(TAG, "startBypass Error: " + e.getMessage(), e);
            Toast.makeText(this, "Error starting service: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isBypassActive = false;
            updateStatusUI(false);
        }
    }

    private void deactivateBypass() {
        Log.d(TAG, "deactivateBypass: Deactivating bypass...");
        
        isBypassActive = false;
        updateStatusUI(false);
        
        // Stop Bypass Service
        try {
            Intent serviceIntent = new Intent(this, BypassService.class);
            stopService(serviceIntent);
            Log.d(TAG, "deactivateBypass: Service stopped");
            Toast.makeText(this, "Bypass Deactivated", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "deactivateBypass Error: " + e.getMessage(), e);
        }
    }

    private void updateStatusUI(boolean active) {
        runOnUiThread(() -> {
            try {
                if (tvStatus != null) {
                    if (active) {
                        tvStatus.setText("✅ BYPASS ACTIVE");
                        tvStatus.setTextColor(getColor(R.color.status_active));
                    } else {
                        tvStatus.setText("❌ BYPASS INACTIVE");
                        tvStatus.setTextColor(getColor(R.color.status_inactive));
                    }
                }
                
                if (btnActivate != null) {
                    btnActivate.setText(active ? "🛑 DEACTIVATE" : "⚡ ACTIVATE BYPASS");
                }
            } catch (Exception e) {
                Log.e(TAG, "updateStatusUI Error: " + e.getMessage());
            }
        });
    }

    private void startSpeedMonitor() {
        Log.d(TAG, "startSpeedMonitor: Starting speed monitor...");
        
        try {
            speedMonitor = new SpeedMonitor(this);
            speedMonitor.setSpeedListener((downloadSpeed, uploadSpeed, ping) -> {
                runOnUiThread(() -> {
                    try {
                        if (tvDownloadSpeed != null) {
                            tvDownloadSpeed.setText(String.format("%.1f", downloadSpeed));
                        }
                        if (tvUploadSpeed != null) {
                            tvUploadSpeed.setText(String.format("%.1f", uploadSpeed));
                        }
                        if (tvPing != null) {
                            tvPing.setText(String.valueOf(ping));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Speed update error: " + e.getMessage());
                    }
                });
            });
            speedMonitor.start();
        } catch (Exception e) {
            Log.e(TAG, "startSpeedMonitor Error: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startBypass();
            } else {
                Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: Permission results received");
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Activity destroying...");
        
        try {
            if (speedMonitor != null) {
                speedMonitor.stop();
            }
        } catch (Exception e) {
            Log.e(TAG, "onDestroy Error: " + e.getMessage());
        }
        
        super.onDestroy();
    }
}
