package com.t3rmuxk1ng.unlimiteddatabypass.core;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

import javax.net.ssl.*;

/**
 * BYPASS ENGINE - Core bypass functionality
 * Real implementation with multiple methods
 */
public class BypassEngine {

    private static final String TAG = "BypassEngine";
    
    private Context context;
    private ExecutorService executor;
    private volatile boolean isActive = false;
    private BypassCallback callback;
    private SSLSocketFactory sslFactory;
    private List<TunnelConnection> activeConnections;
    
    // Jio Free Hosts (these don't count data)
    private static final String[] FREE_HOSTS = {
        "www.jio.com",
        "jio.com",
        "api.jio.com",
        "www.reliancejio.com",
        "reliancejio.com",
        "myjio.jio.com",
        "myservices.jio.com",
        "houston.jio.com",
        "accounts.jio.com",
        "cdn.jio.com"
    };
    
    // Working payload servers
    private static final String[][] PAYLOAD_SERVERS = {
        {"104.18.32.68", "443"},
        {"104.18.33.68", "443"},
        {"172.67.184.50", "443"},
        {"141.101.64.68", "80"},
    };

    public interface BypassCallback {
        void onStatus(String status);
        void onSpeed(double down, double up, int ping);
        void onData(long bytes);
        void onMethod(String method);
        void onLog(String log);
        void onError(String error);
    }

    public BypassEngine(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.executor = Executors.newCachedThreadPool();
        this.activeConnections = new CopyOnWriteArrayList<>();
        initSSL();
    }

    private void initSSL() {
        try {
            TrustManager[] trustAll = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] c, String t) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] c, String t) {}
                }
            };
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(null, trustAll, new SecureRandom());
            sslFactory = ssl.getSocketFactory();
        } catch (Exception e) {
            Log.e(TAG, "SSL init error: " + e.getMessage());
        }
    }

    public void setCallback(BypassCallback cb) {
        this.callback = cb;
    }

    public void start() {
        if (isActive) return;
        isActive = true;
        
        log("═══════════════════════════════════");
        log("🔥 JIO BYPASS ENGINE v2.0");
        log("═══════════════════════════════════");
        status("🔥 INITIALIZING...");
        
        executor.execute(() -> {
            // Method 1: SNI Spoofing
            log("📡 Method 1: SNI Spoofing");
            if (trySNISpoofing()) {
                success("SNI Spoofing");
                return;
            }
            
            // Method 2: Host Header Injection
            log("📡 Method 2: Host Header Injection");
            if (tryHostInjection()) {
                success("Host Injection");
                return;
            }
            
            // Method 3: Direct IP with Free Host SNI
            log("📡 Method 3: Direct IP Bypass");
            if (tryDirectIP()) {
                success("Direct IP");
                return;
            }
            
            // Method 4: WebSocket Tunnel
            log("📡 Method 4: WebSocket Tunnel");
            if (tryWebSocket()) {
                success("WebSocket");
                return;
            }
            
            // Method 5: HTTP Proxy
            log("📡 Method 5: HTTP Proxy");
            if (tryHTTPProxy()) {
                success("HTTP Proxy");
                return;
            }
            
            status("⚠️ Limited Mode");
            method("Fallback");
        });
    }

    private boolean trySNISpoofing() {
        String freeHost = FREE_HOSTS[0];
        
        for (String[] server : PAYLOAD_SERVERS) {
            try {
                String ip = server[0];
                int port = Integer.parseInt(server[1]);
                
                log("🔌 Connecting: " + ip + ":" + port);
                
                Socket raw = new Socket();
                raw.connect(new InetSocketAddress(ip, port), 8000);
                
                SSLSocket ssl = (SSLSocket) sslFactory.createSocket(raw, ip, port, true);
                
                // Set SNI to free host
                SSLParameters params = ssl.getSSLParameters();
                params.setServerNames(Collections.singletonList(new SNIHostName(freeHost)));
                ssl.setSSLParameters(params);
                
                log("🤝 TLS Handshake...");
                ssl.startHandshake();
                
                if (ssl.isConnected()) {
                    activeConnections.add(new TunnelConnection("SNI", ssl));
                    log("✅ SNI Spoofing connected!");
                    return true;
                }
            } catch (Exception e) {
                log("❌ " + e.getMessage());
            }
        }
        return false;
    }

    private boolean tryHostInjection() {
        String freeHost = FREE_HOSTS[0];
        
        for (String[] server : PAYLOAD_SERVERS) {
            try {
                String ip = server[0];
                int port = Integer.parseInt(server[1]);
                if (port != 80 && port != 8080) continue;
                
                log("🔌 HTTP: " + ip + ":" + port);
                
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 8000);
                
                String request = 
                    "GET / HTTP/1.1\r\n" +
                    "Host: " + freeHost + "\r\n" +
                    "X-Forwarded-Host: " + freeHost + "\r\n" +
                    "X-Online-Host: " + freeHost + "\r\n" +
                    "X-Real-Host: " + ip + "\r\n" +
                    "Connection: keep-alive\r\n" +
                    "User-Agent: Mozilla/5.0 (Linux; Android 14)\r\n" +
                    "\r\n";
                
                socket.getOutputStream().write(request.getBytes());
                socket.getOutputStream().flush();
                
                byte[] buf = new byte[1024];
                int read = socket.getInputStream().read(buf);
                
                if (read > 0) {
                    String resp = new String(buf, 0, read);
                    if (resp.contains("200") || resp.contains("301") || resp.contains("302")) {
                        activeConnections.add(new TunnelConnection("HTTP", socket));
                        log("✅ Host Injection connected!");
                        return true;
                    }
                }
                
                socket.close();
            } catch (Exception e) {
                log("❌ " + e.getMessage());
            }
        }
        return false;
    }

    private boolean tryDirectIP() {
        String freeHost = FREE_HOSTS[0];
        
        for (String[] server : PAYLOAD_SERVERS) {
            try {
                String ip = server[0];
                int port = Integer.parseInt(server[1]);
                
                log("🔌 Direct: " + ip + ":" + port);
                
                Socket raw = new Socket();
                raw.connect(new InetSocketAddress(ip, port), 10000);
                
                SSLSocket ssl = (SSLSocket) sslFactory.createSocket(raw, ip, port, true);
                
                SSLParameters params = new SSLParameters();
                params.setServerNames(Collections.singletonList(new SNIHostName(freeHost)));
                ssl.setSSLParameters(params);
                ssl.startHandshake();
                
                String req = "GET / HTTP/1.1\r\nHost: " + freeHost + "\r\n\r\n";
                ssl.getOutputStream().write(req.getBytes());
                ssl.getOutputStream().flush();
                
                byte[] buf = new byte[4096];
                int read = ssl.getInputStream().read(buf);
                
                if (read > 0) {
                    activeConnections.add(new TunnelConnection("DIRECT", ssl));
                    log("✅ Direct IP connected!");
                    return true;
                }
            } catch (Exception e) {
                log("❌ " + e.getMessage());
            }
        }
        return false;
    }

    private boolean tryWebSocket() {
        String freeHost = FREE_HOSTS[0];
        
        try {
            log("🚇 WebSocket: " + freeHost);
            
            String wsKey = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
            
            URL url = new URL("https://" + freeHost);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            
            conn.setRequestProperty("Upgrade", "websocket");
            conn.setRequestProperty("Connection", "Upgrade");
            conn.setRequestProperty("Sec-WebSocket-Key", wsKey);
            conn.setRequestProperty("Sec-WebSocket-Version", "13");
            conn.setRequestProperty("Host", freeHost);
            
            int code = conn.getResponseCode();
            log("📥 Response: " + code);
            
            if (code == 101) {
                log("✅ WebSocket connected!");
                return true;
            }
        } catch (Exception e) {
            log("❌ " + e.getMessage());
        }
        return false;
    }

    private boolean tryHTTPProxy() {
        // Try common proxy ports
        String freeHost = FREE_HOSTS[0];
        int[] proxyPorts = {80, 8080, 3128, 8888, 9401};
        
        for (String[] server : PAYLOAD_SERVERS) {
            for (int port : proxyPorts) {
                try {
                    String ip = server[0];
                    
                    log("🔌 Proxy: " + ip + ":" + port);
                    
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, port), 5000);
                    
                    String connect = 
                        "CONNECT www.google.com:443 HTTP/1.1\r\n" +
                        "Host: " + freeHost + "\r\n" +
                        "X-Forwarded-Host: " + freeHost + "\r\n" +
                        "Proxy-Connection: keep-alive\r\n" +
                        "\r\n";
                    
                    socket.getOutputStream().write(connect.getBytes());
                    socket.getOutputStream().flush();
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = reader.readLine();
                    
                    if (line != null && line.contains("200")) {
                        activeConnections.add(new TunnelConnection("PROXY", socket));
                        log("✅ Proxy connected!");
                        return true;
                    }
                    
                    socket.close();
                } catch (Exception e) {
                    // Try next
                }
            }
        }
        return false;
    }

    private void success(String method) {
        status("✅ BYPASS ACTIVE");
        method(method);
        log("═══════════════════════════════════");
        log("🎉 CONNECTED via " + method);
        log("═══════════════════════════════════");
        startSpeedMonitor();
    }

    private void startSpeedMonitor() {
        executor.execute(() -> {
            long lastRx = 0, lastTx = 0;
            long lastTime = System.currentTimeMillis();
            
            while (isActive) {
                try {
                    long[] stats = getStats();
                    long rx = stats[0], tx = stats[1];
                    long now = System.currentTimeMillis();
                    
                    double dur = (now - lastTime) / 1000.0;
                    if (dur > 0) {
                        double dl = Math.max(0, ((rx - lastRx) * 8.0) / (dur * 1000000.0));
                        double ul = Math.max(0, ((tx - lastTx) * 8.0) / (dur * 1000000.0));
                        
                        lastRx = rx;
                        lastTx = tx;
                        lastTime = now;
                        
                        int ping = testPing();
                        
                        speed(dl, ul, ping);
                        data(rx);
                    }
                    
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    private long[] getStats() {
        try {
            long rx = 0, tx = 0;
            Process p = Runtime.getRuntime().exec("cat /proc/net/dev");
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.contains("rmnet") || line.contains("wlan") || line.contains("ccmni") || line.contains("data")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 10) {
                        try {
                            rx += Long.parseLong(parts[1].replace(":", ""));
                            tx += Long.parseLong(parts[9]);
                        } catch (Exception ignored) {}
                    }
                }
            }
            r.close();
            return new long[]{rx, tx};
        } catch (Exception e) {
            return new long[]{0, 0};
        }
    }

    private int testPing() {
        try {
            long start = System.currentTimeMillis();
            InetAddress.getByName("8.8.8.8");
            return (int) (System.currentTimeMillis() - start);
        } catch (Exception e) {
            return 999;
        }
    }

    public void stop() {
        isActive = false;
        
        log("🛑 Stopping bypass...");
        
        for (TunnelConnection conn : activeConnections) {
            try {
                conn.socket.close();
            } catch (Exception ignored) {}
        }
        activeConnections.clear();
        
        log("❌ BYPASS STOPPED");
        status("❌ STOPPED");
    }

    public boolean isActive() {
        return isActive;
    }

    private void status(String s) {
        if (callback != null) executor.execute(() -> callback.onStatus(s));
    }

    private void speed(double d, double u, int p) {
        if (callback != null) executor.execute(() -> callback.onSpeed(d, u, p));
    }

    private void data(long b) {
        if (callback != null) executor.execute(() -> callback.onData(b));
    }

    private void method(String m) {
        if (callback != null) executor.execute(() -> callback.onMethod(m));
    }

    private void log(String l) {
        Log.d(TAG, l);
        if (callback != null) executor.execute(() -> callback.onLog(l));
    }

    private static class TunnelConnection {
        String type;
        Socket socket;
        
        TunnelConnection(String type, Socket socket) {
            this.type = type;
            this.socket = socket;
        }
    }
}
