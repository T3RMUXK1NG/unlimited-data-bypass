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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.t3rmuxk1ng.unlimiteddatabypass.R;
import com.t3rmuxk1ng.unlimiteddatabypass.core.ISPPayload;
import com.t3rmuxk1ng.unlimiteddatabypass.core.RealBypassEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.services.BypassService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * MAIN ACTIVITY - REAL BYPASS EDITION
 * Actually works with real payload injection
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
    private SwitchCompat switchApn, switchDns, switchHeader, switchProxy;
    private SwitchCompat switchTunnel, switchVpn, switch5g, switchUnlimited;

    // State
    private boolean isBypassActive = false;
    private ISPPayload currentPayload;
    private RealBypassEngine bypassEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "onCreate: Starting...");

            initViews();
            checkPermissions();
            setupClickListeners();
            createNotificationChannel();
            initBypassEngine();
            detectISP();

            Log.d(TAG, "onCreate: Ready!");

        } catch (Exception e) {
            Log.e(TAG, "onCreate Error: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        Log.d(TAG, "initViews: Initializing...");

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

        // Default payload
        currentPayload = ISPPayload.getAllPayloads()[0];

        Log.d(TAG, "initViews: Done");
    }

    private void initBypassEngine() {
        bypassEngine = new RealBypassEngine(this);
        bypassEngine.setCallback(new RealBypassEngine.BypassCallback() {
            @Override
            public void onStatusUpdate(String status) {
                runOnUiThread(() -> {
                    if (tvStatus != null) tvStatus.setText(status);
                });
            }

            @Override
            public void onSpeedUpdate(double download, double upload, int ping) {
                runOnUiThread(() -> {
                    if (tvDownloadSpeed != null) tvDownloadSpeed.setText(String.format("%.1f", download));
                    if (tvUploadSpeed != null) tvUploadSpeed.setText(String.format("%.1f", upload));
                    if (tvPing != null) tvPing.setText(String.valueOf(ping));
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onConnected() {
                runOnUiThread(() -> {
                    isBypassActive = true;
                    updateStatusUI(true);
                    Toast.makeText(MainActivity.this, "✅ Bypass Connected!", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void checkPermissions() {
        Log.d(TAG, "checkPermissions: Checking...");

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
                deactivateBypass();
            } else {
                activateBypass();
            }
        });

        btnSelectISP.setOnClickListener(v -> showISPSelector());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bypass Status",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Unlimited Data Bypass Status");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void detectISP() {
        Log.d(TAG, "detectISP: Detecting...");

        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                String operatorName = tm.getNetworkOperatorName();
                String mccMnc = tm.getNetworkOperator();

                Log.d(TAG, "Operator: " + operatorName + ", MCC/MNC: " + mccMnc);

                // Detect ISP from operator name
                String detectedISP = detectISPFromName(operatorName);
                currentPayload = ISPPayload.getByName(detectedISP);

                if (tvISP != null) {
                    tvISP.setText("ISP: " + currentPayload.getName());
                }

                // Network type
                int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
                try {
                    networkType = tm.getDataNetworkType();
                } catch (Exception e) {
                    Log.e(TAG, "Network type error: " + e.getMessage());
                }

                String networkName = getNetworkTypeName(networkType);
                if (tvNetworkType != null) {
                    tvNetworkType.setText("Network: " + networkName);
                }
            } else {
                if (tvISP != null) {
                    tvISP.setText("ISP: " + currentPayload.getName());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "detectISP Error: " + e.getMessage(), e);
        }
    }

    private String detectISPFromName(String operatorName) {
        if (operatorName == null) return "Auto Detect";

        String op = operatorName.toLowerCase();

        if (op.contains("jio") || op.contains("reliance")) return "Jio (India)";
        if (op.contains("airtel")) return "Airtel (India)";
        if (op.contains("vodafone") || op.contains("idea") || op.contains("vi")) return "Vi (India)";
        if (op.contains("bsnl")) return "BSNL (India)";
        if (op.contains("t-mobile") || op.contains("tmobile")) return "T-Mobile (USA)";
        if (op.contains("att") || op.contains("at&t")) return "AT&T (USA)";
        if (op.contains("mtn")) return "MTN (Africa)";
        if (op.contains("globe")) return "Globe (Philippines)";
        if (op.contains("jazz") || op.contains("mobilink")) return "Jazz (Pakistan)";

        return "Auto Detect";
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
        String[] ispNames = ISPPayload.getAllNames();

        new AlertDialog.Builder(this)
                .setTitle("Select Your ISP")
                .setItems(ispNames, (dialog, which) -> {
                    ISPPayload[] payloads = ISPPayload.getAllPayloads();
                    currentPayload = payloads[which];
                    if (tvISP != null) {
                        tvISP.setText("ISP: " + currentPayload.getName());
                    }
                    Toast.makeText(this, "Selected: " + currentPayload.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void activateBypass() {
        Log.d(TAG, "activateBypass: Activating...");

        if (currentPayload == null) {
            Toast.makeText(this, "Please select ISP first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start VPN service
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
            return;
        }

        startBypass();
    }

    private void startBypass() {
        Log.d(TAG, "startBypass: Starting...");

        isBypassActive = true;
        updateStatusUI(true);
        tvStatus.setText("🔄 Connecting...");

        // Start foreground service
        try {
            Intent serviceIntent = new Intent(this, BypassService.class);
            serviceIntent.putExtra("isp_name", currentPayload.getName());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Service start error: " + e.getMessage());
        }

        // Start real bypass engine
        bypassEngine.startBypass(currentPayload);

        Toast.makeText(this, "⚡ Activating Bypass...", Toast.LENGTH_SHORT).show();
    }

    private void deactivateBypass() {
        Log.d(TAG, "deactivateBypass: Deactivating...");

        isBypassActive = false;
        updateStatusUI(false);

        // Stop service
        try {
            Intent serviceIntent = new Intent(this, BypassService.class);
            stopService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Service stop error: " + e.getMessage());
        }

        // Stop bypass engine
        bypassEngine.stopBypass();

        Toast.makeText(this, "Bypass Deactivated", Toast.LENGTH_SHORT).show();
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
            Log.d(TAG, "Permissions result received");
        }
    }

    @Override
    protected void onDestroy() {
        if (bypassEngine != null) {
            bypassEngine.stopBypass();
        }
        super.onDestroy();
    }
}
