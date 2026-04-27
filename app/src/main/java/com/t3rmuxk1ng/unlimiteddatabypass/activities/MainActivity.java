package com.t3rmuxk1ng.unlimiteddatabypass.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.t3rmuxk1ng.unlimiteddatabypass.R;
import com.t3rmuxk1ng.unlimiteddatabypass.config.ISPDatabase;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.APNBypassEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.DNSManipulationEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.HeaderInjectionEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.ProxyBypassEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.SpeedOptimizerEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.TunnelEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.models.ISPConfig;
import com.t3rmuxk1ng.unlimiteddatabypass.receivers.BootReceiver;
import com.t3rmuxk1ng.unlimiteddatabypass.services.BypassService;
import com.t3rmuxk1ng.unlimiteddatabypass.utils.PermissionHelper;
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

    // UI Components
    private TextView tvTitle, tvSubtitle, tvStatus, tvISP, tvNetworkType;
    private TextView tvDownloadSpeed, tvUploadSpeed, tvPing;
    private Button btnActivate, btnSelectISP, btnSettings;
    private CardView statusCard, speedCard, featuresCard;
    
    // Feature Switches
    private Switch switchApn, switchDns, switchHeader, switchProxy;
    private Switch switchTunnel, switchVpn, switch5g, switchUnlimited;
    
    // Engines
    private APNBypassEngine apnBypassEngine;
    private DNSManipulationEngine dnsEngine;
    private HeaderInjectionEngine headerEngine;
    private ProxyBypassEngine proxyEngine;
    private TunnelEngine tunnelEngine;
    private SpeedOptimizerEngine speedOptimizer;
    
    // State
    private boolean isBypassActive = false;
    private ISPConfig currentISP;
    private SpeedMonitor speedMonitor;
    
    // Constants
    private static final int VPN_REQUEST_CODE = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1002;
    private static final String CHANNEL_ID = "bypass_channel";
    
    // Permissions Array
    private final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_SETTINGS,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    };
    
    // Additional permissions for Android 13+
    private final String[] ANDROID_13_PERMISSIONS = {
        Manifest.permission.READ_PHONE_NUMBERS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initEngines();
        checkPermissions();
        setupClickListeners();
        createNotificationChannel();
        detectISP();
        startSpeedMonitor();
        
        // Auto-start bypass if configured
        if (getSharedPreferences("bypass_prefs", MODE_PRIVATE)
                .getBoolean("auto_start", false)) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                activateBypass();
            }, 1000);
        }
    }

    private void initViews() {
        // Header
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        
        // Status Card
        statusCard = findViewById(R.id.statusCard);
        tvStatus = findViewById(R.id.tvStatus);
        tvISP = findViewById(R.id.tvISP);
        tvNetworkType = findViewById(R.id.tvNetworkType);
        
        // Speed Card
        speedCard = findViewById(R.id.speedCard);
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
        btnSettings = findViewById(R.id.btnSettings);
        
        // Apply animations
        animateCard(statusCard, 0);
        animateCard(speedCard, 100);
        animateCard(featuresCard, 200);
    }
    
    private void animateCard(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(delay)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
    }

    private void initEngines() {
        apnBypassEngine = new APNBypassEngine(this);
        dnsEngine = new DNSManipulationEngine(this);
        headerEngine = new HeaderInjectionEngine(this);
        proxyEngine = new ProxyBypassEngine(this);
        tunnelEngine = new TunnelEngine(this);
        speedOptimizer = new SpeedOptimizerEngine(this);
        speedMonitor = new SpeedMonitor(this);
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        
        // Add Android 13+ permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : ANDROID_13_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) 
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
        
        // Request system alert window permission
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
        }
        
        // Request write settings permission
        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivity(intent);
        }
        
        // Request battery optimization exemption
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            android.os.PowerManager pm = (android.os.PowerManager) 
                    getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(android.net.Uri.parse("package:" + packageName));
                startActivity(intent);
            }
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
        
        btnSettings.setOnClickListener(v -> showSettings());
        
        // Feature switches
        switchApn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isBypassActive && isChecked != apnBypassEngine.isActive()) {
                if (isChecked) apnBypassEngine.start(currentISP);
                else apnBypassEngine.stop();
            }
        });
        
        switchDns.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isBypassActive) {
                if (isChecked) dnsEngine.start(currentISP);
                else dnsEngine.stop();
            }
        });
        
        switch5g.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isBypassActive) {
                if (isChecked) speedOptimizer.enable5GBoost();
                else speedOptimizer.disable5GBoost();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(getString(R.string.notification_channel_desc));
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void detectISP() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) 
                == PackageManager.PERMISSION_GRANTED) {
            String operatorName = tm.getNetworkOperatorName();
            String mccMnc = tm.getNetworkOperator();
            
            currentISP = ISPDatabase.detectISP(operatorName, mccMnc);
            tvISP.setText("ISP: " + currentISP.getName());
            
            // Detect network type
            int networkType = tm.getDataNetworkType();
            String networkName = getNetworkTypeName(networkType);
            tvNetworkType.setText("Network: " + networkName);
        } else {
            currentISP = ISPDatabase.getDefaultISP();
            tvISP.setText("ISP: Manual Selection Required");
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
                return "5G 🚀";
            default:
                return "Unknown";
        }
    }

    private void showISPSelector() {
        String[] ispNames = ISPDatabase.getAllISPNames();
        
        new AlertDialog.Builder(this)
            .setTitle("Select Your ISP")
            .setItems(ispNames, (dialog, which) -> {
                currentISP = ISPDatabase.getISPByIndex(which);
                tvISP.setText("ISP: " + currentISP.getName());
                Toast.makeText(this, "Selected: " + currentISP.getName(), 
                        Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showSettings() {
        // Create settings dialog
        View settingsView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        
        new AlertDialog.Builder(this)
            .setTitle("⚙️ Settings")
            .setView(settingsView)
            .setPositiveButton("Save", (dialog, which) -> {
                // Save settings
                saveSettings();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void saveSettings() {
        getSharedPreferences("bypass_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("auto_start", true)
            .putBoolean("background_service", true)
            .putBoolean("notification", true)
            .apply();
        
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }

    private void activateBypass() {
        if (currentISP == null) {
            Toast.makeText(this, "Please select ISP first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Request VPN permission
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
            return;
        }
        
        // Start bypass
        startBypass();
    }
    
    private void startBypass() {
        isBypassActive = true;
        updateStatusUI(true);
        
        // Start Bypass Service
        Intent serviceIntent = new Intent(this, BypassService.class);
        serviceIntent.putExtra("isp_config", currentISP);
        serviceIntent.putExtra("apn_bypass", switchApn.isChecked());
        serviceIntent.putExtra("dns_bypass", switchDns.isChecked());
        serviceIntent.putExtra("header_bypass", switchHeader.isChecked());
        serviceIntent.putExtra("proxy_bypass", switchProxy.isChecked());
        serviceIntent.putExtra("tunnel_bypass", switchTunnel.isChecked());
        serviceIntent.putExtra("vpn_bypass", switchVpn.isChecked());
        serviceIntent.putExtra("5g_boost", switch5g.isChecked());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        // Start individual engines
        if (switchApn.isChecked()) apnBypassEngine.start(currentISP);
        if (switchDns.isChecked()) dnsEngine.start(currentISP);
        if (switchHeader.isChecked()) headerEngine.start(currentISP);
        if (switchProxy.isChecked()) proxyEngine.start(currentISP);
        if (switchTunnel.isChecked()) tunnelEngine.start(currentISP);
        if (switch5g.isChecked()) speedOptimizer.enable5GBoost();
        
        Toast.makeText(this, getString(R.string.msg_activated), Toast.LENGTH_LONG).show();
    }

    private void deactivateBypass() {
        isBypassActive = false;
        updateStatusUI(false);
        
        // Stop Bypass Service
        Intent serviceIntent = new Intent(this, BypassService.class);
        stopService(serviceIntent);
        
        // Stop all engines
        apnBypassEngine.stop();
        dnsEngine.stop();
        headerEngine.stop();
        proxyEngine.stop();
        tunnelEngine.stop();
        speedOptimizer.disable5GBoost();
        
        Toast.makeText(this, getString(R.string.msg_deactivated), Toast.LENGTH_SHORT).show();
    }

    private void updateStatusUI(boolean active) {
        runOnUiThread(() -> {
            if (active) {
                tvStatus.setText(getString(R.string.status_active));
                tvStatus.setTextColor(getColor(R.color.status_active));
                btnActivate.setText(getString(R.string.btn_deactivate));
                btnActivate.setBackground(getDrawable(R.drawable.button_secondary));
            } else {
                tvStatus.setText(getString(R.string.status_inactive));
                tvStatus.setTextColor(getColor(R.color.status_inactive));
                btnActivate.setText(getString(R.string.btn_activate));
                btnActivate.setBackground(getDrawable(R.drawable.button_primary));
            }
        });
    }

    private void startSpeedMonitor() {
        speedMonitor.setSpeedListener(new SpeedMonitor.SpeedListener() {
            @Override
            public void onSpeedUpdate(double downloadSpeed, double uploadSpeed, int ping) {
                runOnUiThread(() -> {
                    tvDownloadSpeed.setText(String.format("%.1f", downloadSpeed));
                    tvUploadSpeed.setText(String.format("%.1f", uploadSpeed));
                    tvPing.setText(String.valueOf(ping));
                });
            }
        });
        speedMonitor.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startBypass();
        } else if (requestCode == VPN_REQUEST_CODE) {
            Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "Some permissions denied. App may not work properly.", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speedMonitor != null) {
            speedMonitor.stop();
        }
    }
}
