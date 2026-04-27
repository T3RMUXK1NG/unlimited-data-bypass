package com.t3rmuxk1ng.unlimiteddatabypass.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.VpnService;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.t3rmuxk1ng.unlimiteddatabypass.R;
import com.t3rmuxk1ng.unlimiteddatabypass.activities.MainActivity;
import com.t3rmuxk1ng.unlimiteddatabypass.config.ISPDatabase;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.APNBypassEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.DNSManipulationEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.HeaderInjectionEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.ProxyBypassEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.SpeedOptimizerEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.engines.TunnelEngine;
import com.t3rmuxk1ng.unlimiteddatabypass.models.ISPConfig;
import com.t3rmuxk1ng.unlimiteddatabypass.utils.NetworkHelper;
import com.t3rmuxk1ng.unlimiteddatabypass.utils.PacketProcessor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * BYPASS SERVICE - CORE VPN SERVICE
 * Handles all bypass operations at system level
 * Runs as Foreground Service with VPN interface
 */
public class BypassService extends VpnService {

    private static final String TAG = "BypassService";
    private static final String CHANNEL_ID = "bypass_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    // VPN Configuration
    private static final String VPN_ADDRESS = "10.0.0.2";
    private static final String VPN_ROUTE = "0.0.0.0";
    private static final int VPN_PREFIX = 32;
    private static final int MTU = 1500;
    
    // DNS Servers (Multiple for redundancy)
    private static final String[] DNS_SERVERS = {
        "8.8.8.8",           // Google Primary
        "8.8.4.4",           // Google Secondary
        "1.1.1.1",           // Cloudflare Primary
        "1.0.0.1",           // Cloudflare Secondary
        "208.67.222.222",    // OpenDNS Primary
        "208.67.220.220",    // OpenDNS Secondary
        "9.9.9.9",           // Quad9 Primary
        "149.112.112.112"    // Quad9 Secondary
    };
    
    // Engines
    private APNBypassEngine apnEngine;
    private DNSManipulationEngine dnsEngine;
    private HeaderInjectionEngine headerEngine;
    private ProxyBypassEngine proxyEngine;
    private TunnelEngine tunnelEngine;
    private SpeedOptimizerEngine speedOptimizer;
    
    // VPN Components
    private ParcelFileDescriptor vpnInterface;
    private FileInputStream vpnInput;
    private FileOutputStream vpnOutput;
    private PacketProcessor packetProcessor;
    
    // State
    private boolean isRunning = false;
    private ISPConfig ispConfig;
    private Thread vpnThread;
    
    // Configuration flags
    private boolean apnBypassEnabled = true;
    private boolean dnsBypassEnabled = true;
    private boolean headerBypassEnabled = true;
    private boolean proxyBypassEnabled = true;
    private boolean tunnelBypassEnabled = true;
    private boolean vpnBypassEnabled = true;
    private boolean fiveGBoostEnabled = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BypassService Created");
        
        initEngines();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BypassService Started");
        
        // Get configuration from intent
        if (intent != null) {
            ispConfig = (ISPConfig) intent.getSerializableExtra("isp_config");
            apnBypassEnabled = intent.getBooleanExtra("apn_bypass", true);
            dnsBypassEnabled = intent.getBooleanExtra("dns_bypass", true);
            headerBypassEnabled = intent.getBooleanExtra("header_bypass", true);
            proxyBypassEnabled = intent.getBooleanExtra("proxy_bypass", true);
            tunnelBypassEnabled = intent.getBooleanExtra("tunnel_bypass", true);
            vpnBypassEnabled = intent.getBooleanExtra("vpn_bypass", true);
            fiveGBoostEnabled = intent.getBooleanExtra("5g_boost", true);
        }
        
        if (!isRunning) {
            startForeground(NOTIFICATION_ID, createNotification());
            startVPN();
            startEngines();
            isRunning = true;
        }
        
        return START_STICKY;
    }

    private void initEngines() {
        apnEngine = new APNBypassEngine(this);
        dnsEngine = new DNSManipulationEngine(this);
        headerEngine = new HeaderInjectionEngine(this);
        proxyEngine = new ProxyBypassEngine(this);
        tunnelEngine = new TunnelEngine(this);
        speedOptimizer = new SpeedOptimizerEngine(this);
        packetProcessor = new PacketProcessor(this);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bypass Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Unlimited Data Bypass Active");
            channel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("⚡ Unlimited Data Bypass Active")
                .setContentText("Bypassing data limits - 5G Speed Mode")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void startVPN() {
        Log.d(TAG, "Starting VPN Interface");
        
        try {
            Builder builder = new Builder();
            builder.setSession("UnlimitedDataBypass");
            builder.setMtu(MTU);
            builder.addAddress(VPN_ADDRESS, 24);
            builder.addRoute(VPN_ROUTE, 0);
            
            // Add all DNS servers
            for (String dns : DNS_SERVERS) {
                builder.addDnsServer(dns);
            }
            
            // Add search domains
            builder.addSearchDomain("jio.com");
            builder.addSearchDomain("airtel.in");
            builder.addSearchDomain("vodafoneidea.com");
            
            // Allow all apps through VPN
            builder.addAllowedApplication("com.android.chrome");
            builder.addAllowedApplication("com.google.android.youtube");
            builder.addAllowedApplication("com.instagram.android");
            builder.addAllowedApplication("com.whatsapp");
            builder.addAllowedApplication("com.facebook.katana");
            builder.addAllowedApplication("com.twitter.android");
            builder.addAllowedApplication("com.netflix.mediaclient");
            builder.addAllowedApplication("com.spotify.music");
            
            // Establish VPN interface
            vpnInterface = builder.establish();
            
            if (vpnInterface != null) {
                vpnInput = new FileInputStream(vpnInterface.getFileDescriptor());
                vpnOutput = new FileOutputStream(vpnInterface.getFileDescriptor());
                
                // Start packet processing thread
                vpnThread = new Thread(this::processPackets);
                vpnThread.start();
                
                Log.d(TAG, "VPN Interface Established Successfully");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to establish VPN: " + e.getMessage());
        }
    }

    private void processPackets() {
        Log.d(TAG, "Starting Packet Processing");
        
        ByteBuffer buffer = ByteBuffer.allocate(32767);
        
        while (isRunning && vpnInterface != null) {
            try {
                // Read packet from VPN interface
                int length = vpnInput.read(buffer.array());
                
                if (length > 0) {
                    // Process packet through bypass engines
                    byte[] processedPacket = processPacket(buffer.array(), length);
                    
                    // Write processed packet back
                    if (processedPacket != null) {
                        vpnOutput.write(processedPacket, 0, processedPacket.length);
                    }
                }
                
                buffer.clear();
                
            } catch (Exception e) {
                Log.e(TAG, "Packet processing error: " + e.getMessage());
            }
        }
    }

    private byte[] processPacket(byte[] packet, int length) {
        // Apply header injection if enabled
        if (headerBypassEnabled && headerEngine != null) {
            packet = headerEngine.processPacket(packet, length, ispConfig);
        }
        
        // Apply proxy bypass if enabled
        if (proxyBypassEnabled && proxyEngine != null) {
            packet = proxyEngine.processPacket(packet, length, ispConfig);
        }
        
        // Apply DNS manipulation if enabled
        if (dnsBypassEnabled && dnsEngine != null) {
            packet = dnsEngine.processPacket(packet, length, ispConfig);
        }
        
        // Apply 5G optimization
        if (fiveGBoostEnabled && speedOptimizer != null) {
            packet = speedOptimizer.optimizePacket(packet, length);
        }
        
        return packet;
    }

    private void startEngines() {
        Log.d(TAG, "Starting Bypass Engines");
        
        // Start APN Bypass
        if (apnBypassEnabled && apnEngine != null) {
            apnEngine.start(ispConfig);
            Log.d(TAG, "APN Bypass Engine Started");
        }
        
        // Start DNS Manipulation
        if (dnsBypassEnabled && dnsEngine != null) {
            dnsEngine.start(ispConfig);
            Log.d(TAG, "DNS Manipulation Engine Started");
        }
        
        // Start Header Injection
        if (headerBypassEnabled && headerEngine != null) {
            headerEngine.start(ispConfig);
            Log.d(TAG, "Header Injection Engine Started");
        }
        
        // Start Proxy Bypass
        if (proxyBypassEnabled && proxyEngine != null) {
            proxyEngine.start(ispConfig);
            Log.d(TAG, "Proxy Bypass Engine Started");
        }
        
        // Start Tunnel Engine
        if (tunnelBypassEnabled && tunnelEngine != null) {
            tunnelEngine.start(ispConfig);
            Log.d(TAG, "Tunnel Engine Started");
        }
        
        // Enable 5G Speed Boost
        if (fiveGBoostEnabled && speedOptimizer != null) {
            speedOptimizer.enable5GBoost();
            Log.d(TAG, "5G Speed Boost Enabled");
        }
    }

    private void stopEngines() {
        Log.d(TAG, "Stopping Bypass Engines");
        
        if (apnEngine != null) apnEngine.stop();
        if (dnsEngine != null) dnsEngine.stop();
        if (headerEngine != null) headerEngine.stop();
        if (proxyEngine != null) proxyEngine.stop();
        if (tunnelEngine != null) tunnelEngine.stop();
        if (speedOptimizer != null) speedOptimizer.disable5GBoost();
    }

    private void stopVPN() {
        Log.d(TAG, "Stopping VPN Interface");
        
        isRunning = false;
        
        if (vpnThread != null) {
            vpnThread.interrupt();
            vpnThread = null;
        }
        
        try {
            if (vpnInput != null) {
                vpnInput.close();
                vpnInput = null;
            }
            if (vpnOutput != null) {
                vpnOutput.close();
                vpnOutput = null;
            }
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing VPN: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "BypassService Destroyed");
        
        stopEngines();
        stopVPN();
        
        super.onDestroy();
    }

    @Override
    public void onRevoke() {
        Log.d(TAG, "VPN Permission Revoked");
        stopEngines();
        stopVPN();
        stopForeground(true);
        super.onRevoke();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
