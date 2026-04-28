package com.t3rmuxk1ng.unlimiteddatabypass.core;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * PACKET PARSER
 * Parse IP/TCP/UDP packets for VPN routing
 */
public class PacketParser {

    // IP Protocol types
    public static final int PROTO_TCP = 6;
    public static final int PROTO_UDP = 17;
    public static final int PROTO_ICMP = 1;

    // Packet info container
    public static class PacketInfo {
        public int version;
        public int protocol;
        public String sourceIP;
        public String destIP;
        public int sourcePort;
        public int destPort;
        public byte[] payload;
        public int headerLength;
        public int totalLength;
        public int flags;
        public long sequence;
        public long ackSequence;
        public int windowSize;
        public boolean syn;
        public boolean ack;
        public boolean fin;
        public boolean rst;
        public boolean psh;

        @Override
        public String toString() {
            return String.format("%s:%d -> %s:%d [%s]",
                sourceIP, sourcePort, destIP, destPort,
                protocol == PROTO_TCP ? "TCP" : protocol == PROTO_UDP ? "UDP" : "IP");
        }
    }

    // TCP Flags
    private static final int TCP_FIN = 0x01;
    private static final int TCP_SYN = 0x02;
    private static final int TCP_RST = 0x04;
    private static final int TCP_PSH = 0x08;
    private static final int TCP_ACK = 0x10;

    /**
     * Parse IP packet
     */
    public static PacketInfo parsePacket(byte[] data, int length) {
        if (data == null || length < 20) return null;

        PacketInfo info = new PacketInfo();

        try {
            // IP Header
            int firstByte = data[0] & 0xFF;
            info.version = (firstByte >> 4) & 0x0F;

            if (info.version != 4) return null; // Only IPv4 for now

            info.headerLength = (firstByte & 0x0F) * 4;
            info.totalLength = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
            info.protocol = data[9] & 0xFF;

            // Source IP
            info.sourceIP = String.format("%d.%d.%d.%d",
                data[12] & 0xFF, data[13] & 0xFF,
                data[14] & 0xFF, data[15] & 0xFF);

            // Dest IP
            info.destIP = String.format("%d.%d.%d.%d",
                data[16] & 0xFF, data[17] & 0xFF,
                data[18] & 0xFF, data[19] & 0xFF);

            // Parse TCP/UDP
            if (info.protocol == PROTO_TCP && length >= info.headerLength + 20) {
                parseTCP(data, info);
            } else if (info.protocol == PROTO_UDP && length >= info.headerLength + 8) {
                parseUDP(data, info);
            }

            return info;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse TCP header
     */
    private static void parseTCP(byte[] data, PacketInfo info) {
        int offset = info.headerLength;

        info.sourcePort = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
        info.destPort = ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);

        info.sequence = ((long)(data[offset + 4] & 0xFF) << 24) |
                        ((long)(data[offset + 5] & 0xFF) << 16) |
                        ((long)(data[offset + 6] & 0xFF) << 8) |
                        ((long)(data[offset + 7] & 0xFF));

        info.ackSequence = ((long)(data[offset + 8] & 0xFF) << 24) |
                           ((long)(data[offset + 9] & 0xFF) << 16) |
                           ((long)(data[offset + 10] & 0xFF) << 8) |
                           ((long)(data[offset + 11] & 0xFF));

        int dataOffset = ((data[offset + 12] >> 4) & 0x0F) * 4;
        info.flags = data[offset + 13] & 0xFF;

        info.syn = (info.flags & TCP_SYN) != 0;
        info.ack = (info.flags & TCP_ACK) != 0;
        info.fin = (info.flags & TCP_FIN) != 0;
        info.rst = (info.flags & TCP_RST) != 0;
        info.psh = (info.flags & TCP_PSH) != 0;

        info.windowSize = ((data[offset + 14] & 0xFF) << 8) | (data[offset + 15] & 0xFF);

        // Payload
        int payloadStart = info.headerLength + dataOffset;
        if (info.totalLength > payloadStart) {
            info.payload = new byte[info.totalLength - payloadStart];
            System.arraycopy(data, payloadStart, info.payload, 0, info.payload.length);
        }

        info.headerLength = info.headerLength + dataOffset;
    }

    /**
     * Parse UDP header
     */
    private static void parseUDP(byte[] data, PacketInfo info) {
        int offset = info.headerLength;

        info.sourcePort = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
        info.destPort = ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);

        int udpLength = ((data[offset + 4] & 0xFF) << 8) | (data[offset + 5] & 0xFF);

        // Payload
        int payloadStart = offset + 8;
        if (udpLength > 8) {
            info.payload = new byte[udpLength - 8];
            System.arraycopy(data, payloadStart, info.payload, 0, info.payload.length);
        }

        info.headerLength = payloadStart;
    }

    /**
     * Build IP header
     */
    public static byte[] buildIPHeader(String sourceIP, String destIP, int protocol, int payloadLength) {
        byte[] header = new byte[20];
        int totalLength = 20 + payloadLength;

        // Version 4, IHL 5 (20 bytes)
        header[0] = 0x45;

        // Total length
        header[2] = (byte) ((totalLength >> 8) & 0xFF);
        header[3] = (byte) (totalLength & 0xFF);

        // Protocol
        header[9] = (byte) protocol;

        // Source IP
        String[] srcParts = sourceIP.split("\\.");
        header[12] = (byte) Integer.parseInt(srcParts[0]);
        header[13] = (byte) Integer.parseInt(srcParts[1]);
        header[14] = (byte) Integer.parseInt(srcParts[2]);
        header[15] = (byte) Integer.parseInt(srcParts[3]);

        // Dest IP
        String[] dstParts = destIP.split("\\.");
        header[16] = (byte) Integer.parseInt(dstParts[0]);
        header[17] = (byte) Integer.parseInt(dstParts[1]);
        header[18] = (byte) Integer.parseInt(dstParts[2]);
        header[19] = (byte) Integer.parseInt(dstParts[3]);

        // Calculate checksum
        int checksum = calculateChecksum(header, 0, 20);
        header[10] = (byte) ((checksum >> 8) & 0xFF);
        header[11] = (byte) (checksum & 0xFF);

        return header;
    }

    /**
     * Build TCP header
     */
    public static byte[] buildTCPHeader(int sourcePort, int destPort, long seq, long ack,
                                         int flags, int windowSize, byte[] options) {
        int optionsLen = options != null ? options.length : 0;
        int headerLen = 20 + optionsLen;
        byte[] header = new byte[headerLen];

        // Source port
        header[0] = (byte) ((sourcePort >> 8) & 0xFF);
        header[1] = (byte) (sourcePort & 0xFF);

        // Dest port
        header[2] = (byte) ((destPort >> 8) & 0xFF);
        header[3] = (byte) (destPort & 0xFF);

        // Sequence
        header[4] = (byte) ((seq >> 24) & 0xFF);
        header[5] = (byte) ((seq >> 16) & 0xFF);
        header[6] = (byte) ((seq >> 8) & 0xFF);
        header[7] = (byte) (seq & 0xFF);

        // Ack
        header[8] = (byte) ((ack >> 24) & 0xFF);
        header[9] = (byte) ((ack >> 16) & 0xFF);
        header[10] = (byte) ((ack >> 8) & 0xFF);
        header[11] = (byte) (ack & 0xFF);

        // Data offset (5 = 20 bytes, no options)
        int dataOffset = headerLen / 4;
        header[12] = (byte) (dataOffset << 4);

        // Flags
        header[13] = (byte) flags;

        // Window
        header[14] = (byte) ((windowSize >> 8) & 0xFF);
        header[15] = (byte) (windowSize & 0xFF);

        // Copy options if any
        if (options != null && options.length > 0) {
            System.arraycopy(options, 0, header, 20, options.length);
        }

        return header;
    }

    /**
     * Build UDP header
     */
    public static byte[] buildUDPHeader(int sourcePort, int destPort, int payloadLength) {
        byte[] header = new byte[8];
        int totalLength = 8 + payloadLength;

        header[0] = (byte) ((sourcePort >> 8) & 0xFF);
        header[1] = (byte) (sourcePort & 0xFF);
        header[2] = (byte) ((destPort >> 8) & 0xFF);
        header[3] = (byte) (destPort & 0xFF);
        header[4] = (byte) ((totalLength >> 8) & 0xFF);
        header[5] = (byte) (totalLength & 0xFF);

        return header;
    }

    /**
     * Calculate IP checksum
     */
    public static int calculateChecksum(byte[] data, int offset, int length) {
        int sum = 0;

        for (int i = offset; i < offset + length; i += 2) {
            int word = ((data[i] & 0xFF) << 8);
            if (i + 1 < offset + length) {
                word |= (data[i + 1] & 0xFF);
            }
            sum += word;
        }

        while ((sum >> 16) != 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }

        return ~sum & 0xFFFF;
    }

    /**
     * Calculate TCP checksum (with pseudo header)
     */
    public static int calculateTCPChecksum(byte[] ipHeader, byte[] tcpHeader, byte[] payload) {
        int sum = 0;

        // Pseudo header
        sum += ((ipHeader[12] & 0xFF) << 8) | (ipHeader[13] & 0xFF);
        sum += ((ipHeader[14] & 0xFF) << 8) | (ipHeader[15] & 0xFF);
        sum += ((ipHeader[16] & 0xFF) << 8) | (ipHeader[17] & 0xFF);
        sum += PROTO_TCP;
        int tcpLength = tcpHeader.length + (payload != null ? payload.length : 0);
        sum += tcpLength;

        // TCP header
        for (int i = 0; i < tcpHeader.length; i += 2) {
            int word = ((tcpHeader[i] & 0xFF) << 8);
            if (i + 1 < tcpHeader.length) {
                word |= (tcpHeader[i + 1] & 0xFF);
            }
            sum += word;
        }

        // Payload
        if (payload != null) {
            for (int i = 0; i < payload.length; i += 2) {
                int word = ((payload[i] & 0xFF) << 8);
                if (i + 1 < payload.length) {
                    word |= (payload[i + 1] & 0xFF);
                }
                sum += word;
            }
        }

        while ((sum >> 16) != 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }

        return ~sum & 0xFFFF;
    }

    /**
     * Extract domain from DNS query
     */
    public static String extractDomainFromDNS(byte[] data, int offset) {
        try {
            StringBuilder domain = new StringBuilder();
            int pos = offset;

            while (pos < data.length) {
                int len = data[pos] & 0xFF;
                if (len == 0) break;

                if ((len & 0xC0) == 0xC0) {
                    // Compressed pointer - not handling for simplicity
                    break;
                }

                pos++;
                for (int i = 0; i < len && pos < data.length; i++) {
                    domain.append((char) (data[pos++] & 0xFF));
                }
                if (pos < data.length && (data[pos] & 0xFF) != 0) {
                    domain.append('.');
                }
            }

            return domain.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
