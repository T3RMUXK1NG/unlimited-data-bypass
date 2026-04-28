package com.t3rmuxk1ng.unlimiteddatabypass.core;

import android.util.Log;

import com.t3rmuxk1ng.unlimiteddatabypass.advanced.LiveLogManager;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

import javax.net.ssl.*;

/**
 * DNS PROXY
 * Handles DNS queries through bypass tunnel
 */
public class DnsProxy {

    private static final String TAG = "DnsProxy";

    // Free DNS servers that work on Jio
    private static final String[] FREE_DNS_SERVERS = {
        "1.1.1.1", "1.0.0.1",       // Cloudflare
        "8.8.8.8", "8.8.4.4",       // Google
        "9.9.9.9",                  // Quad9
        "208.67.222.222"            // OpenDNS
    };

    // DNS over HTTPS endpoints
    private static final String DOH_CLOUDFLARE = "https://1.1.1.1/dns-query";
    private static final String DOH_GOOGLE = "https://dns.google/dns-query";

    private LiveLogManager log;
    private ExecutorService executor;
    private boolean running = false;

    // DNS cache
    private ConcurrentHashMap<String, DNSCacheEntry> dnsCache = new ConcurrentHashMap<>();

    // Callback
    private DNSCallback callback;

    public interface DNSCallback {
        void onDNSResponse(byte[] originalQuery, byte[] responseData, int length);
    }

    private static class DNSCacheEntry {
        String ip;
        long timestamp;
        int ttl;

        DNSCacheEntry(String ip, int ttl) {
            this.ip = ip;
            this.ttl = ttl;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid() {
            return System.currentTimeMillis() - timestamp < (ttl * 1000L);
        }
    }

    public DnsProxy() {
        this.log = LiveLogManager.getInstance();
        this.executor = Executors.newCachedThreadPool();
    }

    public void setCallback(DNSCallback callback) {
        this.callback = callback;
    }

    public void start() {
        running = true;
        log.info("📡 DNS Proxy started");
    }

    public void stop() {
        running = false;
        dnsCache.clear();
        log.info("📡 DNS Proxy stopped");
    }

    /**
     * Handle DNS query from VPN
     */
    public void handleQuery(byte[] data, int length, String sourceIP, int sourcePort) {
        if (!running || length < 12) return;

        try {
            // Parse DNS query
            DNSQuery query = parseDNSQuery(data, length);
            if (query == null) return;

            log.debug("🔍 DNS Query: " + query.domain);

            // Check cache first
            DNSCacheEntry cached = dnsCache.get(query.domain);
            if (cached != null && cached.isValid()) {
                log.debug("✓ DNS Cache hit: " + query.domain + " -> " + cached.ip);
                sendCachedResponse(query, cached, data, sourceIP, sourcePort);
                return;
            }

            // Resolve asynchronously
            executor.execute(() -> {
                try {
                    String resolvedIP = resolveDomain(query.domain);

                    if (resolvedIP != null) {
                        // Cache result
                        dnsCache.put(query.domain, new DNSCacheEntry(resolvedIP, 300));

                        // Build and send response
                        byte[] response = buildDNSResponse(data, length, query, resolvedIP);
                        if (response != null && callback != null) {
                            callback.onDNSResponse(data, response, response.length);
                            log.debug("✓ DNS Resolved: " + query.domain + " -> " + resolvedIP);
                        }
                    } else {
                        log.warning("✗ DNS Failed: " + query.domain);
                    }

                } catch (Exception e) {
                    log.error("DNS resolve error: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("DNS query error: " + e.getMessage());
        }
    }

    /**
     * Parse DNS query
     */
    private DNSQuery parseDNSQuery(byte[] data, int length) {
        try {
            DNSQuery query = new DNSQuery();

            // Transaction ID
            query.transactionId = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);

            // Flags
            query.flags = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
            query.isQuery = (query.flags & 0x8000) == 0;

            // Questions count
            int qdcount = ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);

            if (qdcount == 0) return null;

            // Parse domain name
            StringBuilder domain = new StringBuilder();
            int pos = 12;

            while (pos < length) {
                int labelLen = data[pos] & 0xFF;
                if (labelLen == 0) {
                    pos++;
                    break;
                }

                if ((labelLen & 0xC0) == 0xC0) {
                    // Pointer - skip
                    pos += 2;
                    break;
                }

                pos++;
                for (int i = 0; i < labelLen && pos < length; i++) {
                    domain.append((char) (data[pos++] & 0xFF));
                }
                if (pos < length && (data[pos] & 0xFF) != 0) {
                    domain.append('.');
                }
            }

            query.domain = domain.toString();

            // Query type and class
            if (pos + 4 <= length) {
                query.qtype = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
                query.qclass = ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF);
            }

            return query;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Resolve domain using free DNS
     */
    private String resolveDomain(String domain) {
        // Try each DNS server
        for (String dnsServer : FREE_DNS_SERVERS) {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setSoTimeout(3000);

                // Build DNS query
                byte[] query = buildDNSQueryPacket(domain);
                DatagramPacket packet = new DatagramPacket(
                    query, query.length,
                    InetAddress.getByName(dnsServer), 53
                );

                socket.send(packet);

                // Receive response
                byte[] response = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(response, response.length);
                socket.receive(responsePacket);
                socket.close();

                // Parse response
                String ip = parseDNSResponse(responsePacket.getData(), responsePacket.getLength());
                if (ip != null) {
                    return ip;
                }

            } catch (Exception e) {
                // Try next server
            }
        }

        // Fallback to system DNS
        try {
            return InetAddress.getByName(domain).getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Build DNS query packet
     */
    private byte[] buildDNSQueryPacket(String domain) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // Transaction ID
            dos.writeShort(0x1234);

            // Flags: standard query
            dos.writeShort(0x0100);

            // Questions: 1
            dos.writeShort(1);

            // Answers: 0
            dos.writeShort(0);

            // Authority: 0
            dos.writeShort(0);

            // Additional: 0
            dos.writeShort(0);

            // Domain name
            String[] labels = domain.split("\\.");
            for (String label : labels) {
                dos.writeByte(label.length());
                dos.writeBytes(label);
            }
            dos.writeByte(0);

            // Type A
            dos.writeShort(1);

            // Class IN
            dos.writeShort(1);

            return baos.toByteArray();

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse DNS response
     */
    private String parseDNSResponse(byte[] data, int length) {
        try {
            // Skip to answer section
            int pos = 12;

            // Skip question section
            while (pos < length) {
                int labelLen = data[pos] & 0xFF;
                if (labelLen == 0) {
                    pos++;
                    break;
                }
                if ((labelLen & 0xC0) == 0xC0) {
                    pos += 2;
                    break;
                }
                pos += labelLen + 1;
            }
            pos += 4; // Skip QTYPE and QCLASS

            // Parse answers
            int ancount = ((data[6] & 0xFF) << 8) | (data[7] & 0xFF);

            for (int i = 0; i < ancount && pos < length; i++) {
                // Skip name (might be pointer)
                if ((data[pos] & 0xC0) == 0xC0) {
                    pos += 2;
                } else {
                    while (pos < length) {
                        int labelLen = data[pos] & 0xFF;
                        if (labelLen == 0) {
                            pos++;
                            break;
                        }
                        pos += labelLen + 1;
                    }
                }

                // Type
                int type = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
                pos += 2;

                // Class
                pos += 2;

                // TTL
                pos += 4;

                // RDLENGTH
                int rdlength = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
                pos += 2;

                // RDATA - for type A (IPv4)
                if (type == 1 && rdlength == 4) {
                    return String.format("%d.%d.%d.%d",
                        data[pos] & 0xFF, data[pos + 1] & 0xFF,
                        data[pos + 2] & 0xFF, data[pos + 3] & 0xFF);
                }

                pos += rdlength;
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Build DNS response for VPN
     */
    private byte[] buildDNSResponse(byte[] originalQuery, int queryLen, DNSQuery query, String ip) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Copy transaction ID
            baos.write(originalQuery[0]);
            baos.write(originalQuery[1]);

            // Flags: response, authoritative
            baos.write(0x81);
            baos.write(0x80);

            // Questions: 1
            baos.write(0x00);
            baos.write(0x01);

            // Answers: 1
            baos.write(0x00);
            baos.write(0x01);

            // Authority: 0
            baos.write(0x00);
            baos.write(0x00);

            // Additional: 0
            baos.write(0x00);
            baos.write(0x00);

            // Copy question section
            baos.write(originalQuery, 12, queryLen - 12 - 4);

            // Type and class
            baos.write(0x00);
            baos.write(0x01); // Type A
            baos.write(0x00);
            baos.write(0x01); // Class IN

            // Answer section
            // Name pointer to question
            baos.write(0xC0);
            baos.write(0x0C);

            // Type A
            baos.write(0x00);
            baos.write(0x01);

            // Class IN
            baos.write(0x00);
            baos.write(0x01);

            // TTL (300 seconds)
            baos.write(0x00);
            baos.write(0x00);
            baos.write(0x01);
            baos.write(0x2C);

            // RDLENGTH (4 for IPv4)
            baos.write(0x00);
            baos.write(0x04);

            // IP address
            String[] parts = ip.split("\\.");
            for (String part : parts) {
                baos.write(Integer.parseInt(part));
            }

            return baos.toByteArray();

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Send cached response
     */
    private void sendCachedResponse(DNSQuery query, DNSCacheEntry cached,
                                    byte[] originalQuery, String sourceIP, int sourcePort) {
        // Build response using cached IP
        byte[] response = buildDNSResponse(originalQuery, originalQuery.length, query, cached.ip);
        if (response != null && callback != null) {
            callback.onDNSResponse(originalQuery, response, response.length);
        }
    }

    /**
     * Clear DNS cache
     */
    public void clearCache() {
        dnsCache.clear();
    }

    /**
     * Get cache size
     */
    public int getCacheSize() {
        return dnsCache.size();
    }

    /**
     * DNS Query container
     */
    private static class DNSQuery {
        int transactionId;
        int flags;
        boolean isQuery;
        String domain;
        int qtype;
        int qclass;
    }
}
