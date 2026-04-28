package com.t3rmuxk1ng.unlimiteddatabypass.core;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NAT MANAGER
 * Network Address Translation for VPN routing
 * Maps internal VPN addresses to external connections
 */
public class NATManager {

    private static final String TAG = "NATManager";

    // VPN internal network
    private static final String VPN_NETWORK = "10.8.0.0";
    private static final String VPN_MASK = "255.255.255.0";

    // Port range for NAT
    private static final int NAT_PORT_START = 40000;
    private static final int NAT_PORT_END = 60000;

    // Connection mapping: NAT port -> Connection info
    private Map<Integer, NATEntry> natTable = new ConcurrentHashMap<>();

    // Reverse mapping: original connection -> NAT port
    private Map<String, Integer> reverseTable = new ConcurrentHashMap<>();

    // Current NAT port
    private int currentPort = NAT_PORT_START;
    private Random random = new Random();

    /**
     * NAT Entry - stores connection info
     */
    public static class NATEntry {
        public String internalIP;
        public int internalPort;
        public String externalIP;
        public int externalPort;
        public int natPort;
        public int protocol;
        public long lastActivity;
        public long bytesIn;
        public long bytesOut;
        public Object channel; // Channel for async I/O

        public NATEntry(String intIP, int intPort, String extIP, int extPort, int proto) {
            this.internalIP = intIP;
            this.internalPort = intPort;
            this.externalIP = extIP;
            this.externalPort = extPort;
            this.protocol = proto;
            this.lastActivity = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("NAT[%d] %s:%d -> %s:%d",
                natPort, internalIP, internalPort, externalIP, externalPort);
        }
    }

    /**
     * Create NAT entry for outgoing connection
     */
    public synchronized NATEntry createEntry(String internalIP, int internalPort,
                                              String externalIP, int externalPort, int protocol) {
        // Check if entry already exists
        String key = makeKey(internalIP, internalPort, externalIP, externalPort, protocol);
        if (reverseTable.containsKey(key)) {
            int existingPort = reverseTable.get(key);
            NATEntry existing = natTable.get(existingPort);
            if (existing != null) {
                existing.lastActivity = System.currentTimeMillis();
                return existing;
            }
        }

        // Find available port
        int natPort = findAvailablePort();

        // Create entry
        NATEntry entry = new NATEntry(internalIP, internalPort, externalIP, externalPort, protocol);
        entry.natPort = natPort;

        // Store in tables
        natTable.put(natPort, entry);
        reverseTable.put(key, natPort);

        return entry;
    }

    /**
     * Get NAT entry by port
     */
    public NATEntry getEntryByNatPort(int natPort) {
        NATEntry entry = natTable.get(natPort);
        if (entry != null) {
            entry.lastActivity = System.currentTimeMillis();
        }
        return entry;
    }

    /**
     * Get NAT entry by original connection
     */
    public NATEntry getEntryByOriginal(String internalIP, int internalPort,
                                        String externalIP, int externalPort, int protocol) {
        String key = makeKey(internalIP, internalPort, externalIP, externalPort, protocol);
        Integer natPort = reverseTable.get(key);
        if (natPort != null) {
            return natTable.get(natPort);
        }
        return null;
    }

    /**
     * Remove NAT entry
     */
    public synchronized void removeEntry(int natPort) {
        NATEntry entry = natTable.remove(natPort);
        if (entry != null) {
            String key = makeKey(entry.internalIP, entry.internalPort,
                                 entry.externalIP, entry.externalPort, entry.protocol);
            reverseTable.remove(key);
        }
    }

    /**
     * Clean up expired entries (older than timeout seconds)
     */
    public synchronized void cleanup(int timeoutSeconds) {
        long now = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000L;

        for (Map.Entry<Integer, NATEntry> e : natTable.entrySet()) {
            if (now - e.getValue().lastActivity > timeout) {
                removeEntry(e.getKey());
            }
        }
    }

    /**
     * Get all active entries
     */
    public Map<Integer, NATEntry> getAllEntries() {
        return new HashMap<>(natTable);
    }

    /**
     * Get entry count
     */
    public int getEntryCount() {
        return natTable.size();
    }

    /**
     * Update bytes transferred
     */
    public void updateStats(int natPort, long bytesIn, long bytesOut) {
        NATEntry entry = natTable.get(natPort);
        if (entry != null) {
            entry.bytesIn += bytesIn;
            entry.bytesOut += bytesOut;
            entry.lastActivity = System.currentTimeMillis();
        }
    }

    /**
     * Check if IP is in VPN network
     */
    public static boolean isVPNIP(String ip) {
        if (ip == null) return false;
        return ip.startsWith("10.8.0.");
    }

    /**
     * Find available NAT port
     */
    private int findAvailablePort() {
        // Try sequential first
        for (int i = 0; i < NAT_PORT_END - NAT_PORT_START; i++) {
            int port = NAT_PORT_START + ((currentPort - NAT_PORT_START + i) % (NAT_PORT_END - NAT_PORT_START));
            if (!natTable.containsKey(port)) {
                currentPort = port + 1;
                if (currentPort >= NAT_PORT_END) {
                    currentPort = NAT_PORT_START;
                }
                return port;
            }
        }

        // Fallback to random
        int port;
        int attempts = 0;
        do {
            port = NAT_PORT_START + random.nextInt(NAT_PORT_END - NAT_PORT_START);
            attempts++;
        } while (natTable.containsKey(port) && attempts < 100);

        return port;
    }

    /**
     * Make key for reverse lookup
     */
    private String makeKey(String intIP, int intPort, String extIP, int extPort, int proto) {
        return intIP + ":" + intPort + "->" + extIP + ":" + extPort + ":" + proto;
    }

    /**
     * Get statistics
     */
    public String getStats() {
        long totalIn = 0, totalOut = 0;
        for (NATEntry entry : natTable.values()) {
            totalIn += entry.bytesIn;
            totalOut += entry.bytesOut;
        }
        return String.format("NAT: %d connections, ↓%d KB, ↑%d KB",
            natTable.size(), totalIn / 1024, totalOut / 1024);
    }
}
