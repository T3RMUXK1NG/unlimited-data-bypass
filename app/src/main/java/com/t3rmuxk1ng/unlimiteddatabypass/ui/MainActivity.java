package com.t3rmuxk1ng.unlimiteddatabypass.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.t3rmuxk1ng.unlimiteddatabypass.R;
import com.t3rmuxk1ng.unlimiteddatabypass.advanced.LiveLogManager;
import com.t3rmuxk1ng.unlimiteddatabypass.vpn.IntegratedVpnService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MAIN ACTIVITY
 * Terminal-style UI with live logging
 * Now uses IntegratedVpnService for REAL bypass
 */
public class MainActivity extends AppCompatActivity implements LiveLogManager.LogListener {

    private static final int VPN_REQUEST = 1001;
    private static final int PERM_REQUEST = 1002;

    private TextView tvStatus, tvSpeed, tvLog, tvStats;
    private Button btnActivate;
    private ScrollView scrollView;

    private boolean isActive = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private StringBuilder logBuilder = new StringBuilder();

    private LiveLogManager logManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermissions();
        initLogManager();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvLog = findViewById(R.id.tvLog);
        btnActivate = findViewById(R.id.btnActivate);
        scrollView = findViewById(R.id.scrollView);

        // Stats TextView - may not exist in layout
        tvStats = findViewById(R.id.tvStats);

        btnActivate.setOnClickListener(v -> {
            if (isActive) {
                stopBypass();
            } else {
                startBypass();
            }
        });

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            logBuilder = new StringBuilder();
            tvLog.setText("");
        });

        log("═══════════════════════════════════");
        log("🔥 JIO UNLIMITED BYPASS v3.0");
        log("🔥 REAL VPN INTEGRATION");
        log("═══════════════════════════════════");
        log("");
        log("📡 ISP: Jio India (MP Circle)");
        log("🎯 Target: Bypass 2GB/day limit");
        log("🔧 Method: VPN Tunnel + SNI Spoof");
        log("");
    }

    private void checkPermissions() {
        List<String> needed = new ArrayList<>();

        String[] perms = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE
        };

        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                needed.add(p);
            }
        }

        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), PERM_REQUEST);
        }
    }

    private void initLogManager() {
        logManager = LiveLogManager.getInstance();
        logManager.setListener(this);

        // Check if VPN is already running
        if (IntegratedVpnService.isRunning()) {
            isActive = true;
            updateUI();
            log("✅ VPN already running");
        }
    }

    @Override
    public void onLogMessage(String message) {
        log(message);
    }

    private void startBypass() {
        log("═══════════════════════════════════");
        log("🔥 ACTIVATING REAL VPN BYPASS...");
        log("═══════════════════════════════════");

        // Request VPN permission
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST);
            return;
        }

        activate();
    }

    private void activate() {
        isActive = true;
        updateUI();

        // Start INTEGRATED VPN service (real bypass)
        Intent service = new Intent(this, IntegratedVpnService.class);
        service.setAction(IntegratedVpnService.ACTION_START);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(service);
        } else {
            startService(service);
        }

        log("✅ Integrated VPN Service started");
        log("⏳ Initializing tunnel...");

        // Start stats updater
        startStatsUpdater();
    }

    private void stopBypass() {
        log("🛑 Stopping VPN...");

        isActive = false;
        updateUI();

        // Stop VPN service
        Intent service = new Intent(this, IntegratedVpnService.class);
        service.setAction(IntegratedVpnService.ACTION_STOP);
        startService(service);

        log("❌ VPN STOPPED");
    }

    private void updateUI() {
        if (isActive) {
            tvStatus.setText("🔥 ACTIVE");
            tvStatus.setTextColor(Color.parseColor("#00FF00"));
            btnActivate.setText("🛑 STOP BYPASS");
            btnActivate.setBackgroundColor(Color.parseColor("#FF4444"));
        } else {
            tvStatus.setText("❌ INACTIVE");
            tvStatus.setTextColor(Color.parseColor("#FF0000"));
            btnActivate.setText("🔥 ACTIVATE BYPASS");
            btnActivate.setBackgroundColor(Color.parseColor("#4CAF50"));
        }
    }

    private void startStatsUpdater() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isActive) {
                    IntegratedVpnService vpn = IntegratedVpnService.getInstance();
                    if (vpn != null && tvStats != null) {
                        tvStats.setText(vpn.getStats());
                    }

                    // Update every 3 seconds
                    handler.postDelayed(this, 3000);
                }
            }
        }, 1000);
    }

    private void log(String msg) {
        String time = timeFormat.format(new Date());
        String line = "[" + time + "] " + msg + "\n";

        logBuilder.append(line);

        // Limit log size
        if (logBuilder.length() > 30000) {
            logBuilder = new StringBuilder(logBuilder.substring(logBuilder.length() - 20000));
        }

        handler.post(() -> {
            tvLog.setText(logBuilder.toString());
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VPN_REQUEST) {
            if (resultCode == RESULT_OK) {
                log("✅ VPN Permission granted");
                activate();
            } else {
                log("❌ VPN Permission denied");
                Toast.makeText(this, "VPN permission required!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (logManager != null) {
            logManager.removeListener();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check VPN status
        if (IntegratedVpnService.isRunning() && !isActive) {
            isActive = true;
            updateUI();
            startStatsUpdater();
        }
    }
}
