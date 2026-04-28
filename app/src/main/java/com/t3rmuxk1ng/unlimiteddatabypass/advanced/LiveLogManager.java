package com.t3rmuxk1ng.unlimiteddatabypass.advanced;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * LIVE LOG MANAGER
 * Real-time terminal-style logging
 */
public class LiveLogManager {

    private static final String TAG = "LiveLog";
    private static LiveLogManager instance;

    // Listener interface for external log handling
    public interface LogListener {
        void onLogMessage(String message);
    }

    private TextView logTextView;
    private ScrollView scrollView;
    private Context context;
    private ExecutorService executor;
    private Handler mainHandler;
    private SimpleDateFormat timeFormat;

    private List<String> logBuffer = new ArrayList<>();
    private static final int MAX_LINES = 500;
    private LogListener listener;
    
    // Log colors
    public static final String COLOR_INFO = "#00E5FF";     // Cyan
    public static final String COLOR_SUCCESS = "#00FF00";  // Green
    public static final String COLOR_ERROR = "#FF0000";    // Red
    public static final String COLOR_WARNING = "#FFD700";  // Yellow
    public static final String COLOR_DEBUG = "#B0BEC5";    // Gray
    public static final String COLOR_DATA = "#FF00FF";     // Magenta
    public static final String COLOR_SPEED = "#00FFFF";    // Aqua

    private LiveLogManager() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    }

    public static synchronized LiveLogManager getInstance() {
        if (instance == null) {
            instance = new LiveLogManager();
        }
        return instance;
    }

    public void init(Context context, TextView textView, ScrollView scrollView) {
        this.context = context.getApplicationContext();
        this.logTextView = textView;
        this.scrollView = scrollView;
        clear();
        log("🔥 JIO GOD MODE v2.0", "INFO");
        log("═══════════════════════════════════", "INFO");
        log("📡 Initializing bypass engine...", "INFO");
    }

    public void setListener(LogListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    public void log(String message, String type) {
        executor.execute(() -> {
            String timestamp = timeFormat.format(new Date());
            String color = getColorForType(type);
            String prefix = getPrefixForType(type);

            String logLine = String.format("[%s] %s %s", timestamp, prefix, message);

            logBuffer.add(logLine);

            // Limit buffer
            if (logBuffer.size() > MAX_LINES) {
                logBuffer.remove(0);
            }

            // Update UI
            mainHandler.post(() -> {
                if (logTextView != null) {
                    StringBuilder sb = new StringBuilder();
                    for (String line : logBuffer) {
                        sb.append(line).append("\n");
                    }
                    logTextView.setText(sb.toString());

                    // Auto scroll
                    if (scrollView != null) {
                        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    }
                }

                // Notify listener
                if (listener != null) {
                    listener.onLogMessage(logLine);
                }
            });

            // Also log to Android
            Log.d(TAG, prefix + " " + message);
        });
    }

    private String getColorForType(String type) {
        switch (type.toUpperCase()) {
            case "SUCCESS": return COLOR_SUCCESS;
            case "ERROR": return COLOR_ERROR;
            case "WARNING": return COLOR_WARNING;
            case "DATA": return COLOR_DATA;
            case "SPEED": return COLOR_SPEED;
            case "DEBUG": return COLOR_DEBUG;
            default: return COLOR_INFO;
        }
    }

    private String getPrefixForType(String type) {
        switch (type.toUpperCase()) {
            case "SUCCESS": return "✅";
            case "ERROR": return "❌";
            case "WARNING": return "⚠️";
            case "DATA": return "📊";
            case "SPEED": return "⚡";
            case "DEBUG": return "🔍";
            default: return "ℹ️";
        }
    }

    // Convenience methods
    public void info(String message) { log(message, "INFO"); }
    public void success(String message) { log(message, "SUCCESS"); }
    public void error(String message) { log(message, "ERROR"); }
    public void warning(String message) { log(message, "WARNING"); }
    public void data(String message) { log(message, "DATA"); }
    public void speed(String message) { log(message, "SPEED"); }
    public void debug(String message) { log(message, "DEBUG"); }

    public void clear() {
        logBuffer.clear();
        if (logTextView != null) {
            mainHandler.post(() -> logTextView.setText(""));
        }
    }

    public List<String> getLogBuffer() {
        return new ArrayList<>(logBuffer);
    }

    public String getLogAsString() {
        StringBuilder sb = new StringBuilder();
        for (String line : logBuffer) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    // Network activity logger
    public void logNetworkActivity(String host, String method, int code, long bytes) {
        String message = String.format("🌐 %s %s → %d (%.1f KB)", 
            method, host, code, bytes / 1024.0);
        data(message);
    }

    // Speed update logger
    public void logSpeedUpdate(double download, double upload, int ping) {
        String message = String.format("↓ %.1f Mbps | ↑ %.1f Mbps | Ping: %dms", 
            download, upload, ping);
        speed(message);
    }

    // Connection logger
    public void logConnection(String server, int port, String method) {
        success(String.format("🔗 Connected to %s:%d via %s", server, port, method));
    }

    // Bypass status logger
    public void logBypassStatus(String status) {
        info("🔥 BYPASS: " + status);
    }

    // Error with details
    public void logError(String operation, Exception e) {
        error(String.format("❌ %s failed: %s", operation, e.getMessage()));
    }

    // Phase logger
    public void logPhase(int phase, String description) {
        info("═══ PHASE " + phase + ": " + description + " ═══");
    }

    // Host test logger
    public void logHostTest(String host, boolean success, int latency) {
        if (success) {
            success(String.format("✓ %s OK (%dms)", host, latency));
        } else {
            warning(String.format("✗ %s FAILED", host));
        }
    }

    // Tunnel logger
    public void logTunnel(String tunnelId, String action) {
        data(String.format("🚇 Tunnel %s: %s", tunnelId, action));
    }

    // Payload logger
    public void logPayload(String type, String payload) {
        debug(String.format("📝 %s: %s", type, payload.substring(0, Math.min(50, payload.length())) + "..."));
    }
}
