package com.t3rmuxk1ng.unlimiteddatabypass.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.t3rmuxk1ng.unlimiteddatabypass.R;
import com.t3rmuxk1ng.unlimiteddatabypass.advanced.JioGodModeEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.advanced.JioPayloadGenerator;
import com.t3rmuxk1ng.unlimiteddatabypass.services.BypassService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MAIN ACTIVITY - GOD MODE EDITION
 * Jio MP India - Unlimited Data Bypass
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
    private TextView tvBypassed, tvUptime, tvMethod;
    private Button btnActivate;
    private LinearLayout logContainer;
    private ScrollView logScrollView;

    // Feature Switches
    private SwitchCompat switchApn, switchDns, switchHeader, switchProxy;
    private SwitchCompat switchTunnel, switchVpn, switch5g, switchUnlimited;

    // State
    private boolean isBypassActive = false;
    private JioGodModeEngine godModeEngine;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "GOD MODE Starting...");

            initViews();
            checkPermissions();
            setupClickListeners();
            createNotificationChannel();
            initGodModeEngine();
            detectISP();

            Log.d(TAG, "GOD MODE Ready!");

        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvISP = findViewById(R.id.tvISP);
        tvNetworkType = findViewById(R.id.tvNetworkType);

        tvDownloadSpeed = findViewById(R.id.tvDownloadSpeed);
        tvUploadSpeed = findViewById(R.id.tvUploadSpeed);
        tvPing = findViewById(R.id.tvPing);

        btnActivate = findViewById(R.id.btnActivate);

        switchApn = findViewById(R.id.switchApn);
        switchDns = findViewById(R.id.switchDns);
        switchHeader = findViewById(R.id.switchHeader);
        switchProxy = findViewById(R.id.switchProxy);
        switchTunnel = findViewById(R.id.switchTunnel);
        switchVpn = findViewById(R.id.switchVpn);
        switch5g = findViewById(R.id.switch5g);
        switchUnlimited = findViewById(R.id.switchUnlimited);

        // Set Jio as default
        tvISP.setText("ISP: Jio (MP, India)");
    }

    private void initGodModeEngine() {
        godModeEngine = new JioGodModeEngine(this);
        godModeEngine.setCallback(new JioGodModeEngine.GodModeCallback() {
            @Override
            public void onStatusUpdate(String status) {
                uiHandler.post(() -> {
                    if (tvStatus != null) tvStatus.setText(status);
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
                });
            }

            @Override
            public void onDataBypassed(long bytes) {
                uiHandler.post(() -> {
                    double mb = bytes / (1024.0 * 1024.0);
                    double gb = mb / 1024.0;
                    // Could display bypassed data
                });
            }

            @Override
            public void onError(String error) {
                uiHandler.post(() -> {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onConnected(String method) {
                uiHandler.post(() -> {
                    isBypassActive = true;
                    updateStatusUI(true);
                    Toast.makeText(MainActivity.this, 
                        "✅ Connected via " + method, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onLog(String log) {
                Log.d(TAG, log);
            }
        });
    }

    private void checkPermissions() {
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
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
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
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "GOD MODE Bypass",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Unlimited Data Bypass Active");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void detectISP() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                
                String operatorName = tm.getNetworkOperatorName();
                int networkType = tm.getDataNetworkType();
                
                if (operatorName.toLowerCase().contains("jio")) {
                    tvISP.setText("ISP: Jio (MP, India)");
                }
                
                tvNetworkType.setText("Network: " + getNetworkTypeName(networkType));
            }
        } catch (Exception e) {
            Log.e(TAG, "ISP detect error: " + e.getMessage());
        }
    }

    private String getNetworkTypeName(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G LTE";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return "4G/5G";
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
                    Toast.makeText(this, "Selected: " + payloads[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void activateGodMode() {
        Log.d(TAG, "Activating GOD MODE...");

        // Request VPN permission
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
            return;
        }

        startGodMode();
    }

    private void startGodMode() {
        isBypassActive = true;
        updateStatusUI(true);
        tvStatus.setText("🔥 ACTIVATING...");

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
        } catch (Exception e) {
            Log.e(TAG, "Service error: " + e.getMessage());
        }

        // Activate GOD MODE
        godModeEngine.activateGodMode();

        Toast.makeText(this, "🔥 GOD MODE ACTIVATING...", Toast.LENGTH_SHORT).show();
    }

    private void deactivateGodMode() {
        Log.d(TAG, "Deactivating GOD MODE...");

        isBypassActive = false;
        updateStatusUI(false);

        // Stop service
        try {
            Intent serviceIntent = new Intent(this, BypassService.class);
            stopService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Service stop error: " + e.getMessage());
        }

        // Stop GOD MODE
        godModeEngine.deactivate();

        Toast.makeText(this, "GOD MODE Stopped", Toast.LENGTH_SHORT).show();
    }

    private void updateStatusUI(boolean active) {
        runOnUiThread(() -> {
            try {
                if (tvStatus != null) {
                    if (active) {
                        tvStatus.setText("✅ GOD MODE ACTIVE");
                        tvStatus.setTextColor(getColor(R.color.status_active));
                    } else {
                        tvStatus.setText("❌ GOD MODE INACTIVE");
                        tvStatus.setTextColor(getColor(R.color.status_inactive));
                    }
                }

                if (btnActivate != null) {
                    btnActivate.setText(active ? "🛑 DEACTIVATE" : "🔥 ACTIVATE GOD MODE");
                }
            } catch (Exception e) {
                Log.e(TAG, "UI update error: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startGodMode();
            } else {
                Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        if (godModeEngine != null) {
            godModeEngine.deactivate();
        }
        super.onDestroy();
    }
}
