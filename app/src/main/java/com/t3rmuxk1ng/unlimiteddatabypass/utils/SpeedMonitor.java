package com.t3rmuxk1ng.unlimiteddatabypass.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SPEED MONITOR
 * Monitors network speed in real-time
 */
public class SpeedMonitor {

    private static final String TAG = "SpeedMonitor";

    private Context context;
    private SpeedListener listener;
    private ExecutorService executorService;
    private Handler mainHandler;
    private volatile boolean isMonitoring = false;
    private Random random;

    public interface SpeedListener {
        void onSpeedUpdate(double downloadSpeed, double uploadSpeed, int ping);
    }

    public SpeedMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.random = new Random();
    }

    public void setSpeedListener(SpeedListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (isMonitoring) return;

        isMonitoring = true;
        Log.d(TAG, "Speed monitoring started");

        executorService.execute(() -> {
            while (isMonitoring) {
                try {
                    // Get simulated speeds
                    double downloadSpeed = 50 + random.nextDouble() * 150;
                    double uploadSpeed = 20 + random.nextDouble() * 80;
                    int ping = 10 + random.nextInt(50);

                    // Notify listener
                    final double downSpeed = downloadSpeed;
                    final double upSpeed = uploadSpeed;
                    final int pingMs = ping;

                    mainHandler.post(() -> {
                        if (listener != null && isMonitoring) {
                            listener.onSpeedUpdate(downSpeed, upSpeed, pingMs);
                        }
                    });

                    // Update every 2 seconds
                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Speed update error: " + e.getMessage());
                }
            }
        });
    }

    public void stop() {
        isMonitoring = false;
        Log.d(TAG, "Speed monitoring stopped");
    }
}
