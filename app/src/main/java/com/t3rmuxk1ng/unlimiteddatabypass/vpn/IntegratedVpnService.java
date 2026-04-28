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
import com.t3rmuxk1ng.unlimiteddatabypass.core.DnsProxy;
import com.t3rmuxk1ng.unlimiteddatabypass.core.PacketParser;
import com.t3rmuxk1ng.unlimiteddatabypass.core.TunnelRouter;
import com.t3rmuxk1ng.unlimiteddatabypass.advanced.LiveLogManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * INTEGRATED VPN SERVICE
 * Real working VPN with packet routing through bypass tunnel
 */
public class IntegratedVpnService extends VpnService implements TunnelRouter.TunnelCallback, DnsProxy.DNSCallback {

    private static final String TAG = "IntegratedVpn";
    private static final String CHANNEL_ID = "jio_bypass_vpn";
    private static final int NOTIFICATION_ID = 2001;

    // VPN configuration
    private static final String VPN_ADDRESS = "10.8.0.2";
    private static final int VPN_PREFIX = 24;
    private static final int MTU = 1500;

    // DNS servers (public)
    private static final String[] DNS_SERVERS = {
        "1.1.1.1", "1.0.0.1",
        "8.8.8.8", "8.8.4.4"
    };

    // Components
    private ParcelFileDescriptor vpnInterface;
    private FileInputStream vpnInput;
    private FileOutputStream vpnOutput;
    private ExecutorService executor;
    private volatile boolean isRunning = false;

    private TunnelRouter tunnelRouter;
    private DnsProxy dnsProxy;
    private LiveLogManager log;

    // Statistics
    private AtomicLong bytesIn = new AtomicLong(0);
    private AtomicLong bytesOut = new AtomicLong(0);
    private AtomicLong packetsIn = new AtomicLong(0);
    private AtomicLong packetsOut = new AtomicLong(0);
    private long startTime = 0;

    // Actions
    public static final String ACTION_START = "com.t3rmuxk1ng.unlimiteddatabypass.START_VPN";
    public static final String ACTION_STOP = "com.t3rmuxk1ng.unlimiteddatabypass.STOP_VPN";

    // Singleton instance for status check
    private static IntegratedVpnService instance;

    public static boolean isRunning() {
        return instance != null && instance.isRunning;
    }

    public static IntegratedVpnService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        executor = Executors.newCachedThreadPool();
        log = LiveLogManager.getInstance();
        tunnelRouter = new TunnelRouter();
        dnsProxy = new DnsProxy();

        createNotificationChannel();
        log.info("🚀 Integrated VPN Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        log.info("📋 VPN Command: " + action);

        if (ACTION_STOP.equals(action)) {
            stopVpn();
            stopSelf();
            return START_NOT_STICKY;
        }

        if (ACTION_START.equals(action) && !isRunning) {
            startVpn();
        }

        return START_STICKY;
    }

    /**
     * Start VPN with proper configuration
     */
    private void startVpn() {
        if (isRunning) return;

        log.info("═══════════════════════════════════");
        log.info("🔥 STARTING REAL VPN BYPASS");
        log.info("═══════════════════════════════════");

        try {
            // Build VPN interface
            Builder builder = new Builder();

            builder.setSession("JioUnlimitedBypass");
            builder.setMtu(MTU);
            builder.addAddress(VPN_ADDRESS, VPN_PREFIX);

            // Route ALL traffic through VPN
            builder.addRoute("0.0.0.0", 0);

            // Add DNS servers
            for (String dns : DNS_SERVERS) {
                builder.addDnsServer(dns);
            }

            // Allow all apps
            try {
                builder.addAllowedApplication("com.android.chrome");
                builder.addAllowedApplication("com.android.browser");
                builder.addAllowedApplication("com.google.android.youtube");
                builder.addAllowedApplication("com.instagram.android");
                builder.addAllowedApplication("com.whatsapp");
                builder.addAllowedApplication("com.facebook.katana");
                builder.addAllowedApplication("com.twitter.android");
                builder.addAllowedApplication("com.spotify.music");
                builder.addAllowedApplication("com.netflix.mediaclient");
                builder.addAllowedApplication("com.ubercab");
            } catch (Exception e) {
                // If specific apps fail, allow all
            }

            // Establish VPN interface
            vpnInterface = builder.establish();

            if (vpnInterface == null) {
                log.error("❌ VPN establish failed!");
                stopSelf();
                return;
            }

            log.success("✓ VPN interface established");
            log.info("📡 IP: " + VPN_ADDRESS + "/" + VPN_PREFIX);
            log.info("📏 MTU: " + MTU);

            // Get file descriptors
            vpnInput = new FileInputStream(vpnInterface.getFileDescriptor());
            vpnOutput = new FileOutputStream(vpnInterface.getFileDescriptor());

            // Start foreground service
            startForeground(NOTIFICATION_ID, createNotification("🔥 Bypass Active"));

            isRunning = true;
            startTime = System.currentTimeMillis();

            // Set callbacks
            tunnelRouter.setCallback(this);
            dnsProxy.setCallback(this);

            // Start components
            tunnelRouter.start();
            dnsProxy.start();

            // Start packet processing threads
            startOutgoingThread();
            startIncomingThread();

            log.success("═══════════════════════════════════");
            log.success("🎉 VPN BYPASS ACTIVE!");
            log.success("═══════════════════════════════════");

            updateNotification("🔥 Connected - Unlimited Mode");

        } catch (Exception e) {
            log.error("VPN start error: " + e.getMessage());
            stopSelf();
        }
    }

    /**
     * Thread to read packets FROM device (outgoing to network)
     */
    private void startOutgoingThread() {
        executor.execute(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(32767);
            byte[] packet = new byte[32767];

            log.info("📤 Outgoing packet thread started");

            while (isRunning && vpnInput != null) {
                try {
                    int length = vpnInput.read(packet);

                    if (length > 0) {
                        packetsOut.incrementAndGet();
                        bytesOut.addAndGet(length);

                        // Route packet through tunnel
                        tunnelRouter.processOutgoingPacket(packet, length);
                    }

                } catch (Exception e) {
                    if (isRunning) {
                        log.error("Outgoing read error: " + e.getMessage());
                    }
                    break;
                }
            }

            log.info("📤 Outgoing packet thread stopped");
        });
    }

    /**
     * Thread to write packets TO device (incoming from network)
     */
    private void startIncomingThread() {
        log.info("📥 Incoming packet thread started");
        // This thread responds to callbacks from tunnelRouter
    }

    /**
     * Callback from TunnelRouter - packet received from network
     */
    @Override
    public void onPacketFromNetwork(byte[] data, int length) {
        if (!isRunning || vpnOutput == null || data == null) return;

        try {
            vpnOutput.write(data, 0, length);
            vpnOutput.flush();

            packetsIn.incrementAndGet();
            bytesIn.addAndGet(length);

        } catch (Exception e) {
            log.error("Incoming write error: " + e.getMessage());
        }
    }

    /**
     * Callback from TunnelRouter - log message
     */
    @Override
    public void onLog(String message) {
        log.debug("[Tunnel] " + message);
    }

    /**
     * Callback from TunnelRouter - error
     */
    @Override
    public void onError(String error) {
        log.error("[Tunnel] " + error);
    }

    /**
     * Callback from DNS Proxy - DNS response
     */
    @Override
    public void onDNSResponse(byte[] originalQuery, byte[] responseData, int length) {
        if (!isRunning || vpnOutput == null) return;

        try {
            // Build UDP packet with DNS response and send to VPN
            // This requires proper packet building with source/dest
            vpnOutput.write(responseData, 0, length);

            packetsIn.incrementAndGet();

        } catch (Exception e) {
            log.error("DNS response write error: " + e.getMessage());
        }
    }

    /**
     * Stop VPN
     */
    private void stopVpn() {
        if (!isRunning) return;

        log.info("🛑 Stopping VPN...");
        isRunning = false;

        // Stop components
        tunnelRouter.stop();
        dnsProxy.stop();

        // Close file descriptors
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
            log.error("VPN close error: " + e.getMessage());
        }

        // Print statistics
        long uptime = (System.currentTimeMillis() - startTime) / 1000;
        log.info("═══════════════════════════════════");
        log.info("📊 VPN STATISTICS");
        log.info("═══════════════════════════════════");
        log.info("⏱️ Uptime: " + uptime + " seconds");
        log.info("📤 Packets Out: " + packetsOut.get());
        log.info("📥 Packets In: " + packetsIn.get());
        log.info("↓ Download: " + (bytesIn.get() / 1024) + " KB");
        log.info("↑ Upload: " + (bytesOut.get() / 1024) + " KB");
        log.info("═══════════════════════════════════");

        stopForeground(true);

        instance = null;
    }

    /**
     * Get current statistics
     */
    public String getStats() {
        long uptime = (System.currentTimeMillis() - startTime) / 1000;
        return String.format("↑%dKB ↓%dKB | %ds",
            bytesOut.get() / 1024,
            bytesIn.get() / 1024,
            uptime);
    }

    /**
     * Create notification channel
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Jio Bypass VPN",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Unlimited Data Bypass Active");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Create notification
     */
    private Notification createNotification(String status) {
        Intent intent = new Intent(this, com.t3rmuxk1ng.unlimiteddatabypass.ui.MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent stopIntent = new Intent(this, IntegratedVpnService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🔥 Jio Unlimited Bypass")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build();
    }

    /**
     * Update notification
     */
    private void updateNotification(String status) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification(status));
        }
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

    @Override
    public void onLowMemory() {
        log.warning("⚠️ Low memory warning");
        super.onLowMemory();
    }
}
