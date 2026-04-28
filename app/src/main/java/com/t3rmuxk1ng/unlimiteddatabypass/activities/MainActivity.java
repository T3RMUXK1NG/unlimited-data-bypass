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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.t3rmuxk1ng.unlimiteddatabypass.R;
import com.t3rmuxk1ng.unlimiteddatabypass.advanced.JioGodModeEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.advanced.LiveLogManager;
import com.t3rmuxk1ng.unlimiteddatabypass.services.BypassService;

import java.util.ArrayList;
import java.util.List;

/**
 * MAIN ACTIVITY - GOD MODE v2.0
 * Live Terminal Logging
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int VPN_REQUEST_CODE = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1002;
    private static final String CHANNEL_ID = "bypass_channel";

    // UI
    private TextView tvStatus, tvISP, tvNetworkType;
    private TextView tvDownloadSpeed, tvUploadSpeed, tvPing;
    private TextView tvLog;
    private ScrollView logScrollView;
    private Button btnActivate;

    // State
    private boolean isBypassActive = false;
    private JioGodModeEngine godModeEngine;
    private LiveLogManager logManager;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initLogManager();
        checkPermissions();
        setupClickListeners();
        createNotificationChannel();
        initGodModeEngine();
        detectISP();

        logManager.info("📱 App initialized successfully");
        logManager.info("🎯 Target: Jio MP, India");
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvISP = findViewById(R.id.tvISP);
        tvNetworkType = findViewById(R.id.tvNetworkType);
        tvDownloadSpeed = findViewById(R.id.tvDownloadSpeed);
        tvUploadSpeed = findViewById(R.id.tvUploadSpeed);
        tvPing = findViewById(R.id.tvPing);
        tvLog = findViewById(R.id.tvLog);
        logScrollView = findViewById(R.id.logScrollView);
        btnActivate = findViewById(R.id.btnActivate);
    }

    private void initLogManager() {
        logManager = LiveLogManager.getInstance();
        logManager.init(this, tvLog, logScrollView);
    }

    private void initGodModeEngine() {
        godModeEngine = new JioGodModeEngine(this);
        godModeEngine.setCallback(new JioGodModeEngine.GodModeCallback() {
            @Override
            public void onStatusUpdate(String status) {
                uiHandler.post(() -> {
                    if (tvStatus != null) tvStatus.setText(status);
                    logManager.info("📡 " + status);
                });
            }

            @Override
            public void onSpeedUpdate(double download, double upload, int ping) {
                uiHandler.post(() -> {
                    if (tvDownloadSpeed != null)
                        tvDownloadSpeed.setText(String.format("%.1f", download));
                    if (tvUploadSpeed != null)
                        tvUploadSpeed.setText(String.format("%.1f", upload));
                    if (tvPing != null)
                        tvPing.setText(String.valueOf(ping));
                    
                    logManager.logSpeedUpdate(download, upload, ping);
                });
            }

            @Override
            public void onDataBypassed(long bytes) {
                double mb = bytes / (1024.0 * 1024.0);
                logManager.data(String.format("📊 Data bypassed: %.2f MB", mb));
            }

            @Override
            public void onError(String error) {
                uiHandler.post(() -> {
                    logManager.error("❌ " + error);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onConnected(String method) {
                uiHandler.post(() -> {
                    isBypassActive = true;
                    updateStatusUI(true);
                    logManager.success("🎉 CONNECTED via " + method);
                    logManager.success("🔥 GOD MODE ACTIVE!");
                    Toast.makeText(MainActivity.this, 
                        "✅ Connected via " + method, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onLog(String log) {
                logManager.debug(log);
            }
        });
    }

    private void checkPermissions() {
        logManager.info("🔐 Checking permissions...");
        
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            logManager.warning("⚠️ Requesting " + permissionsNeeded.size() + " permissions");
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            logManager.success("✓ All permissions granted");
        }
    }

    private void setupClickListeners() {
        btnActivate.setOnClickListener(v -> {
            if (isBypassActive) {
                deactivateGodMode();
            } else {
                activateGodMode();
            }
        });

        findViewById(R.id.btnSelectISP).setOnClickListener(v -> showPayloadSelector());
        
        findViewById(R.id.btnClearLog).setOnClickListener(v -> {
            logManager.clear();
            logManager.info("🗑️ Log cleared");
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "GOD MODE", NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Unlimited Data Bypass");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void detectISP() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                
                String operatorName = tm.getNetworkOperatorName();
                int networkType = tm.getDataNetworkType();
                
                logManager.info("📱 Network: " + operatorName);
                logManager.info("📶 Type: " + getNetworkTypeName(networkType));
                
                if (operatorName.toLowerCase().contains("jio")) {
                    tvISP.setText("Jio");
                    logManager.success("✓ Jio detected!");
                }
                
                tvNetworkType.setText(getNetworkTypeName(networkType));
            }
        } catch (Exception e) {
            logManager.error("ISP detect failed: " + e.getMessage());
        }
    }

    private String getNetworkTypeName(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_LTE: return "4G";
            case TelephonyManager.NETWORK_TYPE_NR: return "5G";
            default: return "4G/5G";
        }
    }

    private void showPayloadSelector() {
        String[] payloads = {
            "🔥 Host Header Injection",
            "⚡ SNI Spoofing",
            "🔀 X-Online-Host",
            "🚇 WebSocket Tunnel",
            "🌐 Direct IP Bypass",
            "🎯 Auto (Recommended)"
        };

        new AlertDialog.Builder(this)
                .setTitle("⚡ Select Bypass Method")
                .setItems(payloads, (dialog, which) -> {
                    logManager.info("📋 Selected: " + payloads[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void activateGodMode() {
        logManager.info("═══════════════════════════════════");
        logManager.info("🔥 ACTIVATING GOD MODE...");
        logManager.info("═══════════════════════════════════");

        // Request VPN permission
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null) {
            logManager.info("🔐 Requesting VPN permission...");
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
            return;
        }

        logManager.success("✓ VPN permission granted");
        startGodMode();
    }

    private void startGodMode() {
        isBypassActive = true;
        updateStatusUI(true);
        tvStatus.setText("🔥 CONNECTING...");

        logManager.info("🚀 Starting bypass service...");
        logManager.info("📡 Initializing network tunnel...");

        // Start foreground service
        try {
            Intent serviceIntent = new Intent(this, BypassService.class);
            serviceIntent.putExtra("isp_name", "Jio");
            serviceIntent.putExtra("god_mode", true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            logManager.success("✓ Service started");
        } catch (Exception e) {
            logManager.error("Service error: " + e.getMessage());
        }

        // Activate GOD MODE
        godModeEngine.activateGodMode();
    }

    private void deactivateGodMode() {
        logManager.info("🛑 Stopping GOD MODE...");

        isBypassActive = false;
        updateStatusUI(false);

        try {
            Intent serviceIntent = new Intent(this, BypassService.class);
            stopService(serviceIntent);
            logManager.success("✓ Service stopped");
        } catch (Exception e) {
            logManager.error("Service stop error: " + e.getMessage());
        }

        godModeEngine.deactivate();
        logManager.info("❌ GOD MODE DEACTIVATED");
    }

    private void updateStatusUI(boolean active) {
        runOnUiThread(() -> {
            if (tvStatus != null) {
                if (active) {
                    tvStatus.setText("✅ ACTIVE");
                    tvStatus.setTextColor(getColor(R.color.status_active));
                } else {
                    tvStatus.setText("❌ INACTIVE");
                    tvStatus.setTextColor(getColor(R.color.status_inactive));
                }
            }

            if (btnActivate != null) {
                btnActivate.setText(active ? "🛑 DEACTIVATE" : "🔥 ACTIVATE");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                logManager.success("✓ VPN permission granted");
                startGodMode();
            } else {
                logManager.error("❌ VPN permission denied");
                Toast.makeText(this, "VPN permission required!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (godModeEngine != null) godModeEngine.deactivate();
        super.onDestroy();
    }
}
