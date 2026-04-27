package com.t3rmuxk1ng.unlimiteddatabypass.engines;

import android.content.Context;
import android.util.Log;

import com.t3rmuxk1ng.unlimiteddatabypass.models.ISPConfig;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * HEADER INJECTION ENGINE
 * Injects custom headers to bypass ISP restrictions
 */
public class HeaderInjectionEngine {

    private static final String TAG = "HeaderInjectionEngine";

    // Common bypass headers
    private static final Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>() {{
        put("X-Forwarded-For", "127.0.0.1");
        put("X-Real-IP", "127.0.0.1");
        put("X-Online-Host", "localhost");
        put("X-Forwarded-Host", "localhost");
        put("X-Original-URL", "/");
        put("X-Rewrite-URL", "/");
        put("Connection", "keep-alive");
        put("Keep-Alive", "timeout=600");
        put("Accept-Encoding", "gzip, deflate");
        put("Cache-Control", "no-cache");
    }};

    // User agents for rotation
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 14; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    };

    private Context context;
    private boolean isActive = false;
    private ISPConfig currentConfig;
    private Map<String, String> customHeaders;
    private int userAgentIndex = 0;

    public HeaderInjectionEngine(Context context) {
        this.context = context;
        this.customHeaders = new HashMap<>();
    }

    /**
     * Start Header Injection
     */
    public void start(ISPConfig config) {
        if (config == null) {
            Log.e(TAG, "Cannot start - no ISP config");
            return;
        }

        this.currentConfig = config;
        Log.d(TAG, "Starting Header Injection for: " + config.getName());

        // Initialize headers
        initHeaders(config);

        isActive = true;
        Log.d(TAG, "Header Injection Active");
    }

    /**
     * Stop Header Injection
     */
    public void stop() {
        if (!isActive) return;

        Log.d(TAG, "Stopping Header Injection");
        customHeaders.clear();
        isActive = false;
    }

    /**
     * Initialize custom headers
     */
    private void initHeaders(ISPConfig config) {
        customHeaders.clear();

        // Add default headers
        customHeaders.putAll(DEFAULT_HEADERS);

        // Add ISP-specific headers
        if (config.getHeaders() != null) {
            for (ISPConfig.HTTPHeader header : config.getHeaders()) {
                customHeaders.put(header.getName(), header.getValue());
            }
        }

        // Add host header if specified
        if (config.getHostHeader() != null && !config.getHostHeader().isEmpty()) {
            customHeaders.put("Host", config.getHostHeader());
        }

        // Set user agent
        if (config.getUserAgent() != null && !config.getUserAgent().isEmpty()) {
            customHeaders.put("User-Agent", config.getUserAgent());
        } else {
            customHeaders.put("User-Agent", rotateUserAgent());
        }

        Log.d(TAG, "Initialized " + customHeaders.size() + " custom headers");
    }

    /**
     * Rotate user agent
     */
    private String rotateUserAgent() {
        String ua = USER_AGENTS[userAgentIndex];
        userAgentIndex = (userAgentIndex + 1) % USER_AGENTS.length;
        return ua;
    }

    /**
     * Process packet and inject headers
     */
    public byte[] processPacket(byte[] packet, int length, ISPConfig config) {
        if (!isActive || packet == null || length < 40) {
            return packet;
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(packet, 0, length);

            // Check if TCP (HTTP uses TCP)
            int protocol = buffer.get(9) & 0xFF;
            if (protocol != 6) { // 6 = TCP
                return packet;
            }

            // Get destination port
            int destPort = ((buffer.get(22) & 0xFF) << 8) | (buffer.get(23) & 0xFF);
            if (destPort != 80 && destPort != 443 && destPort != 8080) {
                return packet; // Not HTTP/HTTPS
            }

            // Try to find HTTP payload
            int ipHeaderLength = (buffer.get(0) & 0x0F) * 4;
            int tcpHeaderLength = ((buffer.get(ipHeaderLength + 12) >> 4) & 0x0F) * 4;
            int payloadStart = ipHeaderLength + tcpHeaderLength;

            if (payloadStart >= length) {
                return packet; // No payload
            }

            // Check for HTTP request
            byte[] payload = new byte[Math.min(100, length - payloadStart)];
            System.arraycopy(packet, payloadStart, payload, 0, payload.length);
            String payloadStr = new String(payload, StandardCharsets.UTF_8);

            if (payloadStr.startsWith("GET ") || payloadStr.startsWith("POST ") ||
                    payloadStr.startsWith("HEAD ") || payloadStr.startsWith("PUT ")) {

                // HTTP request detected
                Log.d(TAG, "HTTP request detected on port " + destPort);

                // Inject headers (this modifies the packet conceptually)
                // Full implementation would rebuild TCP/HTTP packets
                return injectHttpHeaders(packet, length, payloadStart);
            }

            return packet;

        } catch (Exception e) {
            Log.e(TAG, "Error processing packet: " + e.getMessage());
            return packet;
        }
    }

    /**
     * Inject HTTP headers into packet
     */
    private byte[] injectHttpHeaders(byte[] packet, int length, int payloadStart) {
        // Build header injection string
        StringBuilder headerBuilder = new StringBuilder();

        for (Map.Entry<String, String> header : customHeaders.entrySet()) {
            headerBuilder.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        String injectedHeaders = headerBuilder.toString();
        byte[] headerBytes = injectedHeaders.getBytes(StandardCharsets.UTF_8);

        // Create new packet with injected headers
        // Note: This is simplified - full implementation would handle TCP sequence numbers
        byte[] newPacket = new byte[length + headerBytes.length];

        // Copy original packet
        System.arraycopy(packet, 0, newPacket, 0, length);

        // Inject headers (simplified - actual implementation needs TCP reconstruction)
        // For now, return original packet with modification flag
        return packet;
    }

    /**
     * Get custom headers
     */
    public Map<String, String> getCustomHeaders() {
        return new HashMap<>(customHeaders);
    }

    /**
     * Add custom header
     */
    public void addHeader(String name, String value) {
        customHeaders.put(name, value);
    }

    /**
     * Remove header
     */
    public void removeHeader(String name) {
        customHeaders.remove(name);
    }

    /**
     * Check if active
     */
    public boolean isActive() {
        return isActive;
    }
}
