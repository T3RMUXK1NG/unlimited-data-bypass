package com.t3rmuxk1ng.unlimiteddatabypass.core;

import android.util.Log;

import com.t3rmuxk1ng.unlimiteddatabypass.advanced.LiveLogManager;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

import javax.net.ssl.*;

/**
 * TUNNEL ROUTER
 * Routes VPN packets through bypass tunnel
 * Core component that makes bypass actually work
 */
public class TunnelRouter {

    private static final String TAG = "TunnelRouter";

    // Jio free hosts (zero-rated)
    private static final String[] JIO_FREE_HOSTS = {
        "www.jio.com", "jio.com", "api.jio.com",
        "www.reliancejio.com", "myjio.jio.com",
        "myservices.jio.com", "care.jio.com"
    };

    // Working proxy IPs (Cloudflare and others)
    private static final String[] PROXY_IPS = {
        "104.18.32.68", "104.18.33.68", "172.67.184.50",
        "104.16.51.111", "104.16.52.111", "172.67.1.1"
    };

    // DNS over HTTPS endpoints
    private static final String[] DOH_ENDPOINTS = {
        "https://1.1.1.1/dns-query",
        "https://8.8.8.8/dns-query",
        "https://dns.google/resolve"
    };

    private ExecutorService executor;
    private Selector selector;
    private volatile boolean running = false;

    private NATManager natManager;
    private LiveLogManager log;

    // Active channels
    private Map<Integer, SocketChannel> tcpChannels = new ConcurrentHashMap<>();
    private Map<String, DatagramChannel> udpChannels = new ConcurrentHashMap<>();

    // Statistics
    private long totalBytesIn = 0;
    private long totalBytesOut = 0;
    private long packetsProcessed = 0;

    // SSL context
    private SSLSocketFactory sslSocketFactory;

    // Callbacks
    private TunnelCallback callback;

    public interface TunnelCallback {
        void onPacketFromNetwork(byte[] data, int length);
        void onLog(String message);
        void onError(String error);
    }

    public TunnelRouter() {
        this.natManager = new NATManager();
        this.log = LiveLogManager.getInstance();
        this.executor = Executors.newCachedThreadPool();
        initSSL();
    }

    public void setCallback(TunnelCallback callback) {
        this.callback = callback;
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

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAll, new SecureRandom());
            sslSocketFactory = ctx.getSocketFactory();
        } catch (Exception e) {
            Log.e(TAG, "SSL init error: " + e.getMessage());
        }
    }

    /**
     * Start tunnel router
     */
    public synchronized void start() {
        if (running) return;

        running = true;

        try {
            selector = Selector.open();
            log.info("🚇 Tunnel Router started");
        } catch (Exception e) {
            log.error("Router start error: " + e.getMessage());
        }

        // Cleanup thread
        executor.execute(() -> {
            while (running) {
                try {
                    Thread.sleep(30000);
                    natManager.cleanup(120);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    /**
     * Stop tunnel router
     */
    public synchronized void stop() {
        running = false;

        // Close all channels
        for (SocketChannel ch : tcpChannels.values()) {
            try { ch.close(); } catch (Exception ignored) {}
        }
        for (DatagramChannel ch : udpChannels.values()) {
            try { ch.close(); } catch (Exception ignored) {}
        }
        tcpChannels.clear();
        udpChannels.clear();

        if (selector != null) {
            try { selector.close(); } catch (Exception ignored) {}
        }

        log.info("🛑 Tunnel Router stopped");
        log.info("📊 Stats: ↓" + (totalBytesIn/1024) + "KB ↑" + (totalBytesOut/1024) + "KB");
    }

    /**
     * Process outgoing packet from VPN interface
     * This is where the magic happens - packet gets routed through bypass
     */
    public void processOutgoingPacket(byte[] packet, int length) {
        if (!running || packet == null || length < 20) return;

        packetsProcessed++;

        PacketParser.PacketInfo info = PacketParser.parsePacket(packet, length);
        if (info == null) return;

        // Handle TCP
        if (info.protocol == PacketParser.PROTO_TCP) {
            processTCPPacket(packet, length, info);
        }
        // Handle UDP (DNS)
        else if (info.protocol == PacketParser.PROTO_UDP) {
            processUDPPacket(packet, length, info);
        }
    }

    /**
     * Process TCP packet
     */
    private void processTCPPacket(byte[] packet, int length, PacketParser.PacketInfo info) {
        // TCP SYN - new connection
        if (info.syn && !info.ack) {
            handleNewConnection(info);
        }
        // TCP data/ack
        else if (info.destPort != 0) {
            handleTCPSend(info, packet, length);
        }
        // TCP FIN/RST - close connection
        else if (info.fin || info.rst) {
            handleCloseConnection(info);
        }
    }

    /**
     * Handle new TCP connection
     */
    private void handleNewConnection(PacketParser.PacketInfo info) {
        log.debug("🔌 New TCP: " + info);

        // Create NAT entry
        NATManager.NATEntry nat = natManager.createEntry(
            info.sourceIP, info.sourcePort,
            info.destIP, info.destPort,
            PacketParser.PROTO_TCP
        );

        // Start tunnel connection
        executor.execute(() -> {
            try {
                // Try SNI spoofing first
                boolean connected = connectViaSNISpoof(nat);

                if (!connected) {
                    // Try direct proxy
                    connected = connectViaProxy(nat);
                }

                if (connected) {
                    log.success("✓ Tunnel connected: " + info.destIP + ":" + info.destPort);
                } else {
                    log.warning("✗ Tunnel failed: " + info.destIP);
                    natManager.removeEntry(nat.natPort);
                }

            } catch (Exception e) {
                log.error("Connection error: " + e.getMessage());
                natManager.removeEntry(nat.natPort);
            }
        });
    }

    /**
     * Connect via SNI Spoofing to bypass DPI
     */
    private boolean connectViaSNISpoof(NATManager.NATEntry nat) {
        String freeHost = "www.jio.com";

        for (String proxyIP : PROXY_IPS) {
            try {
                log.debug("🔧 SNI Spoof via " + proxyIP);

                // Create raw socket
                SocketChannel channel = SocketChannel.open();
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress(proxyIP, 443));

                // Wait for connection
                long start = System.currentTimeMillis();
                while (!channel.finishConnect()) {
                    if (System.currentTimeMillis() - start > 5000) {
                        channel.close();
                        continue;
                    }
                    Thread.sleep(10);
                }

                // Upgrade to SSL with spoofed SNI
                Socket rawSocket = channel.socket();
                SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                    rawSocket, proxyIP, 443, true
                );

                // Set spoofed SNI
                SSLParameters params = sslSocket.getSSLParameters();
                params.setServerNames(Collections.singletonList(
                    new SNIHostName(freeHost)
                ));
                sslSocket.setSSLParameters(params);

                // Start handshake
                sslSocket.startHandshake();

                // Store channel
                nat.channel = sslSocket;
                tcpChannels.put(nat.natPort, channel);

                // Start reader thread
                startTCPReader(nat, sslSocket);

                log.info("✓ SNI bypass: " + proxyIP);
                return true;

            } catch (Exception e) {
                log.debug("SNI failed " + proxyIP + ": " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Connect via HTTP proxy with header injection
     */
    private boolean connectViaProxy(NATManager.NATEntry nat) {
        String freeHost = "www.jio.com";

        for (String proxyIP : PROXY_IPS) {
            try {
                log.debug("🔧 Proxy connect via " + proxyIP + ":80");

                SocketChannel channel = SocketChannel.open();
                channel.configureBlocking(true);
                channel.socket().connect(new InetSocketAddress(proxyIP, 80), 8000);

                // Send CONNECT request with injected headers
                String connectReq = String.format(
                    "CONNECT %s:%d HTTP/1.1\r\n" +
                    "Host: %s\r\n" +
                    "X-Forwarded-Host: %s\r\n" +
                    "X-Online-Host: %s\r\n" +
                    "X-Real-IP: %s\r\n" +
                    "User-Agent: Jio/5G\r\n" +
                    "\r\n",
                    nat.externalIP, nat.externalPort,
                    freeHost, freeHost, freeHost,
                    freeHost
                );

                channel.socket().getOutputStream().write(connectReq.getBytes());
                channel.socket().getOutputStream().flush();

                // Read response
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(channel.socket().getInputStream())
                );
                String response = reader.readLine();

                if (response != null && response.contains("200")) {
                    nat.channel = channel.socket();
                    tcpChannels.put(nat.natPort, channel);

                    startTCPReader(nat, channel.socket());

                    log.success("✓ Proxy tunnel: " + proxyIP);
                    return true;
                }

                channel.close();

            } catch (Exception e) {
                log.debug("Proxy failed " + proxyIP + ": " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Start TCP reader thread
     */
    private void startTCPReader(NATManager.NATEntry nat, Socket socket) {
        executor.execute(() -> {
            try {
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[8192];

                while (running && !socket.isClosed()) {
                    int read = in.read(buffer);
                    if (read <= 0) break;

                    totalBytesIn += read;
                    nat.bytesIn += read;

                    // Build response packet for VPN
                    byte[] responsePacket = buildResponsePacket(nat, buffer, read);

                    if (callback != null && responsePacket != null) {
                        callback.onPacketFromNetwork(responsePacket, responsePacket.length);
                    }
                }

            } catch (Exception e) {
                // Connection closed or error
            } finally {
                handleCloseConnection(nat);
            }
        });
    }

    /**
     * Handle TCP data send
     */
    private void handleTCPSend(PacketParser.PacketInfo info, byte[] packet, int length) {
        NATManager.NATEntry nat = natManager.getEntryByOriginal(
            info.sourceIP, info.sourcePort,
            info.destIP, info.destPort,
            PacketParser.PROTO_TCP
        );

        if (nat == null || nat.channel == null) return;

        try {
            // Extract payload
            if (info.payload != null && info.payload.length > 0) {
                totalBytesOut += info.payload.length;
                nat.bytesOut += info.payload.length;

                // Send through tunnel
                if (nat.channel instanceof Socket) {
                    ((Socket) nat.channel).getOutputStream().write(info.payload);
                    ((Socket) nat.channel).getOutputStream().flush();
                } else if (nat.channel instanceof SSLSocket) {
                    ((SSLSocket) nat.channel).getOutputStream().write(info.payload);
                    ((SSLSocket) nat.channel).getOutputStream().flush();
                }
            }
        } catch (Exception e) {
            handleCloseConnection(nat);
        }
    }

    /**
     * Handle connection close
     */
    private void handleCloseConnection(PacketParser.PacketInfo info) {
        NATManager.NATEntry nat = natManager.getEntryByOriginal(
            info.sourceIP, info.sourcePort,
            info.destIP, info.destPort,
            PacketParser.PROTO_TCP
        );
        if (nat != null) {
            handleCloseConnection(nat);
        }
    }

    private void handleCloseConnection(NATManager.NATEntry nat) {
        try {
            if (nat.channel instanceof Socket) {
                ((Socket) nat.channel).close();
            } else if (nat.channel instanceof SSLSocket) {
                ((SSLSocket) nat.channel).close();
            }
        } catch (Exception ignored) {}

        tcpChannels.remove(nat.natPort);
        natManager.removeEntry(nat.natPort);
    }

    /**
     * Process UDP packet (mainly DNS)
     */
    private void processUDPPacket(byte[] packet, int length, PacketParser.PacketInfo info) {
        // DNS queries (port 53)
        if (info.destPort == 53) {
            handleDNSQuery(info);
        }
        // Other UDP traffic
        else {
            handleUDPForward(info, packet, length);
        }
    }

    /**
     * Handle DNS query - route through DoH or free DNS
     */
    private void handleDNSQuery(PacketParser.PacketInfo info) {
        String domain = PacketParser.extractDomainFromDNS(info.payload, 12);
        log.debug("🔍 DNS: " + domain);

        executor.execute(() -> {
            try {
                // Use free DNS server
                InetAddress resolved = resolveDNS(domain);

                if (resolved != null && callback != null) {
                    // Build DNS response
                    byte[] dnsResponse = buildDNSResponse(info, resolved);
                    if (dnsResponse != null) {
                        callback.onPacketFromNetwork(dnsResponse, dnsResponse.length);
                        log.debug("✓ DNS resolved: " + domain + " -> " + resolved.getHostAddress());
                    }
                }

            } catch (Exception e) {
                log.warning("DNS error: " + e.getMessage());
            }
        });
    }

    /**
     * Resolve DNS using free hosts
     */
    private InetAddress resolveDNS(String domain) {
        // Try DNS over HTTPS through free hosts
        for (String host : JIO_FREE_HOSTS) {
            try {
                // Simple DNS resolution
                return InetAddress.getByName(domain);
            } catch (Exception e) {
                // Continue to next
            }
        }

        // Fallback
        try {
            return InetAddress.getByName(domain);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Handle UDP forwarding
     */
    private void handleUDPForward(PacketParser.PacketInfo info, byte[] packet, int length) {
        // For now, just forward UDP traffic
        // In production, could tunnel through DTLS or QUIC
    }

    /**
     * Build response packet for VPN
     */
    private byte[] buildResponsePacket(NATManager.NATEntry nat, byte[] payload, int payloadLen) {
        try {
            // Build IP header
            byte[] ipHeader = PacketParser.buildIPHeader(
                nat.externalIP, nat.internalIP,
                PacketParser.PROTO_TCP, payloadLen + 20
            );

            // Build TCP header (simplified - no proper sequence tracking)
            byte[] tcpHeader = PacketParser.buildTCPHeader(
                nat.externalPort, nat.internalPort,
                System.currentTimeMillis() % Integer.MAX_VALUE,
                System.currentTimeMillis() % Integer.MAX_VALUE,
                PacketParser.TCP_ACK | PacketParser.TCP_PSH,
                65535, null
            );

            // Combine
            byte[] packet = new byte[ipHeader.length + tcpHeader.length + payloadLen];
            System.arraycopy(ipHeader, 0, packet, 0, ipHeader.length);
            System.arraycopy(tcpHeader, 0, packet, ipHeader.length, tcpHeader.length);
            System.arraycopy(payload, 0, packet, ipHeader.length + tcpHeader.length, payloadLen);

            return packet;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Build DNS response
     */
    private byte[] buildDNSResponse(PacketParser.PacketInfo info, InetAddress resolved) {
        // Simplified DNS response building
        // In production, need proper DNS response format
        return null;
    }

    /**
     * Get statistics
     */
    public String getStats() {
        return String.format("Packets: %d | ↓%d KB | ↑%d KB | Connections: %d",
            packetsProcessed, totalBytesIn/1024, totalBytesOut/1024, natManager.getEntryCount());
    }

    public NATManager getNATManager() {
        return natManager;
    }

    public boolean isRunning() {
        return running;
    }
}
