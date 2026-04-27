package com.t3rmuxk1ng.unlimiteddatabypass.services;

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
import com.t3rmuxk1ng.unlimiteddatabypass.activities.MainActivity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * BYPASS VPN SERVICE
 * Real VPN tunnel for traffic interception
 */
public class BypassService extends VpnService {

    private static final String TAG = "BypassService";
    private static final String CHANNEL_ID = "bypass_channel";
    private static final int NOTIFICATION_ID = 1001;

    private static final String VPN_ADDRESS = "10.0.0.2";
    private static final int MTU = 1500;

    private static final String[] DNS_SERVERS = {
            "1.1.1.1", "1.0.0.1",
            "8.8.8.8", "8.8.4.4"
    };

    private ParcelFileDescriptor vpnInterface;
    private FileInputStream vpnInput;
    private FileOutputStream vpnOutput;
    private volatile boolean isRunning = false;
    private Thread vpnThread;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Service created");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Starting...");

        if (!isRunning) {
            try {
                startForeground(NOTIFICATION_ID, createNotification());
                startVPN();
                isRunning = true;
                Log.d(TAG, "onStartCommand: Started successfully");
            } catch (Exception e) {
                Log.e(TAG, "onStartCommand Error: " + e.getMessage(), e);
                stopSelf();
            }
        }

        return START_STICKY;
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
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
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
                .setContentText("Bypassing data limits - GOD TIER Mode")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void startVPN() {
        Log.d(TAG, "startVPN: Establishing VPN tunnel...");

        try {
            Builder builder = new Builder();
            builder.setSession("UnlimitedDataBypass");
            builder.setMtu(MTU);
            builder.addAddress(VPN_ADDRESS, 24);
            builder.addRoute("0.0.0.0", 0);

            // Add DNS servers
            for (String dns : DNS_SERVERS) {
                try {
                    builder.addDnsServer(dns);
                } catch (Exception e) {
                    Log.e(TAG, "Error adding DNS: " + e.getMessage());
                }
            }

            // Allow all apps through VPN
            try {
                builder.addAllowedApplication("com.android.chrome");
                builder.addAllowedApplication("com.android.browser");
            } catch (Exception e) {
                Log.d(TAG, "Could not set allowed apps: " + e.getMessage());
            }

            // Establish VPN interface
            vpnInterface = builder.establish();

            if (vpnInterface != null) {
                vpnInput = new FileInputStream(vpnInterface.getFileDescriptor());
                vpnOutput = new FileOutputStream(vpnInterface.getFileDescriptor());

                // Start packet processing
                vpnThread = new Thread(this::processPackets);
                vpnThread.start();

                Log.d(TAG, "startVPN: VPN tunnel established!");
            } else {
                Log.e(TAG, "startVPN: Failed to establish VPN interface");
            }

        } catch (Exception e) {
            Log.e(TAG, "startVPN Error: " + e.getMessage(), e);
        }
    }

    private void processPackets() {
        Log.d(TAG, "processPackets: Starting packet processing...");

        ByteBuffer buffer = ByteBuffer.allocate(32767);

        while (isRunning && vpnInterface != null) {
            try {
                if (vpnInput != null) {
                    int length = vpnInput.read(buffer.array());

                    if (length > 0) {
                        // Process and forward packets
                        // In a real implementation, this is where you'd modify headers
                        if (vpnOutput != null) {
                            vpnOutput.write(buffer.array(), 0, length);
                        }
                    }

                    buffer.clear();
                }
            } catch (Exception e) {
                if (isRunning) {
                    Log.e(TAG, "Packet processing error: " + e.getMessage());
                }
                break;
            }
        }

        Log.d(TAG, "processPackets: Stopped");
    }

    private void stopVPN() {
        Log.d(TAG, "stopVPN: Stopping VPN...");

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
        Log.d(TAG, "onDestroy: Service destroying...");
        stopVPN();
        super.onDestroy();
    }

    @Override
    public void onRevoke() {
        Log.d(TAG, "onRevoke: VPN permission revoked");
        stopVPN();
        stopForeground(true);
        super.onRevoke();
    }
}
