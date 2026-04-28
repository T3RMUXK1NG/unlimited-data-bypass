package com.t3rmuxk1ng.unlimiteddatabypass.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.t3rmuxk1ng.unlimiteddatabypass.core.BypassEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.vpn.BypassVpnService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MAIN ACTIVITY
 * Terminal-style UI with live logging
 */
public class MainActivity extends AppCompatActivity {

    private static final int VPN_REQUEST = 1001;
    private static final int PERM_REQUEST = 1002;

    private TextView tvStatus, tvSpeed, tvLog;
    private Button btnActivate;
    private ScrollView scrollView;
    
    private boolean isActive = false;
    private BypassEngine engine;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private StringBuilder logBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermissions();
        initEngine();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvLog = findViewById(R.id.tvLog);
        btnActivate = findViewById(R.id.btnActivate);
        scrollView = findViewById(R.id.scrollView);

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

        log("🔥 JIO BYPASS v2.0");
        log("═══════════════════════════════════");
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

    private void initEngine() {
        engine = new BypassEngine(this);
        engine.setCallback(new BypassEngine.BypassCallback() {
            @Override
            public void onStatus(String status) {
                handler.post(() -> tvStatus.setText(status));
            }

            @Override
            public void onSpeed(double down, double up, int ping) {
                handler.post(() -> {
                    String speed = String.format("↓ %.1f Mbps  ↑ %.1f Mbps  %d ms", down, up, ping);
                    tvSpeed.setText(speed);
                    log("⚡ " + speed);
                });
            }

            @Override
            public void onData(long bytes) {
                double mb = bytes / (1024.0 * 1024.0);
                // Could update data counter
            }

            @Override
            public void onMethod(String method) {
                log("✅ Method: " + method);
            }

            @Override
            public void onLog(String l) {
                log(l);
            }

            @Override
            public void onError(String e) {
                log("❌ " + e);
            }
        });
    }

    private void startBypass() {
        log("═══════════════════════════════════");
        log("🔥 ACTIVATING...");
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

        // Start VPN service
        Intent service = new Intent(this, BypassVpnService.class);
        service.setAction(BypassVpnService.ACTION_START);
        startService(service);

        // Start bypass engine
        engine.start();

        log("✅ VPN Service started");
    }

    private void stopBypass() {
        log("🛑 Stopping...");

        isActive = false;
        updateUI();

        // Stop VPN service
        Intent service = new Intent(this, BypassVpnService.class);
        service.setAction(BypassVpnService.ACTION_STOP);
        startService(service);

        // Stop engine
        engine.stop();

        log("❌ STOPPED");
    }

    private void updateUI() {
        if (isActive) {
            tvStatus.setText("✅ ACTIVE");
            tvStatus.setTextColor(Color.parseColor("#00FF00"));
            btnActivate.setText("🛑 STOP");
        } else {
            tvStatus.setText("❌ INACTIVE");
            tvStatus.setTextColor(Color.parseColor("#FF0000"));
            btnActivate.setText("🔥 ACTIVATE");
        }
    }

    private void log(String msg) {
        String time = timeFormat.format(new Date());
        String line = "[" + time + "] " + msg + "\n";
        
        logBuilder.append(line);
        
        // Limit log size
        if (logBuilder.length() > 20000) {
            logBuilder = new StringBuilder(logBuilder.substring(logBuilder.length() - 15000));
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
        if (engine != null) {
            engine.stop();
        }
        super.onDestroy();
    }
}
