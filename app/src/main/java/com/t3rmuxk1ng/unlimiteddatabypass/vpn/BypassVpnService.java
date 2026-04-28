package com.t3rmuxk1ng.unlimiteddatabypass.vpn;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.t3rmuxk1ng.unlimiteddatabypass.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BYPASS VPN SERVICE
 * Main VPN service that intercepts all traffic
 */
public class BypassVpnService extends VpnService {

    private static final String TAG = "BypassVpnService";
    private static final String CHANNEL_ID = "bypass_vpn";
    private static final int NOTIFICATION_ID = 1001;
    
    private static final String VPN_ADDRESS = "10.8.0.2";
    private static final int MTU = 1500;
    
    private static final String[] DNS_SERVERS = {
        "1.1.1.1", "1.0.0.1",
        "8.8.8.8", "8.8.4.4"
    };
    
    private ParcelFileDescriptor vpnInterface;
    private FileInputStream vpnInput;
    private FileOutputStream vpnOutput;
    private ExecutorService executor;
    private volatile boolean isRunning = false;
    
    public static final String ACTION_START = "com.t3rmuxk1ng.unlimiteddatabypass.START";
    public static final String ACTION_STOP = "com.t3rmuxk1ng.unlimiteddatabypass.STOP";
    
    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newCachedThreadPool();
        createNotificationChannel();
        Log.d(TAG, "VPN Service created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            
            if (ACTION_STOP.equals(action)) {
                stopVpn();
                stopSelf();
                return START_NOT_STICKY;
            }
            
            if (ACTION_START.equals(action)) {
                startVpn();
            }
        }
        
        return START_STICKY;
    }
    
    private void startVpn() {
        if (isRunning) return;
        
        Log.d(TAG, "Starting VPN...");
        
        try {
            // Create VPN interface
            Builder builder = new Builder();
            builder.setSession("JioBypass");
            builder.setMtu(MTU);
            builder.addAddress(VPN_ADDRESS, 24);
            builder.addRoute("0.0.0.0", 0);
            
            // Add DNS servers
            for (String dns : DNS_SERVERS) {
                builder.addDnsServer(dns);
            }
            
            // Establish
            vpnInterface = builder.establish();
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN");
                stopSelf();
                return;
            }
            
            vpnInput = new FileInputStream(vpnInterface.getFileDescriptor());
            vpnOutput = new FileOutputStream(vpnInterface.getFileDescriptor());
            
            // Start foreground
            startForeground(NOTIFICATION_ID, createNotification());
            
            isRunning = true;
            
            // Start packet processing
            startPacketProcessing();
            
            Log.d(TAG, "VPN started successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "VPN start error: " + e.getMessage());
            stopSelf();
        }
    }
    
    private void startPacketProcessing() {
        // Input thread - packets FROM device
        executor.execute(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(32767);
            
            while (isRunning && vpnInput != null) {
                try {
                    int length = vpnInput.read(buffer.array());
                    
                    if (length > 0) {
                        // Process outgoing packet
                        byte[] processed = processPacket(buffer.array(), length);
                        
                        if (processed != null && vpnOutput != null) {
                            // Write back to VPN (will go to network)
                            // In real implementation, this would go through tunnel
                        }
                    }
                    
                    buffer.clear();
                    
                } catch (Exception e) {
                    if (isRunning) {
                        Log.e(TAG, "Packet read error: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    private byte[] processPacket(byte[] packet, int length) {
        // Basic packet processing
        // In production, this would inject headers, route through tunnel, etc.
        
        if (length < 20) return packet;
        
        // Parse IP header
        int version = (packet[0] >> 4) & 0x0F;
        
        if (version == 4) {
            // IPv4 packet
            int protocol = packet[9] & 0xFF;
            
            // TCP
            if (protocol == 6) {
                // Could inject headers here for HTTP traffic
            }
        }
        
        return packet;
    }
    
    private void stopVpn() {
        isRunning = false;
        
        Log.d(TAG, "Stopping VPN...");
        
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
            Log.e(TAG, "VPN stop error: " + e.getMessage());
        }
        
        stopForeground(true);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Bypass VPN",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Bypass VPN Active");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        Intent intent = new Intent(this, com.t3rmuxk1ng.unlimiteddatabypass.ui.MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🔥 Jio Bypass Active")
            .setContentText("Unlimited Data Mode")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }
    
    @Override
    public void onDestroy() {
        stopVpn();
        if (executor != null) {
            executor.shutdownNow();
        }
        super.onDestroy();
    }
    
    @Override
    public void onRevoke() {
        stopVpn();
        super.onRevoke();
    }
}
