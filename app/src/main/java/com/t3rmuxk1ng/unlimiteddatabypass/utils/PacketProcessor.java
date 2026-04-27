package com.t3rmuxk1ng.unlimiteddatabypass.utils;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * PACKET PROCESSOR
 * Processes network packets at low level
 */
public class PacketProcessor {

    private static final String TAG = "PacketProcessor";

    // IP Protocol numbers
    public static final int PROTOCOL_ICMP = 1;
    public static final int PROTOCOL_TCP = 6;
    public static final int PROTOCOL_UDP = 17;

    private Context context;

    public PacketProcessor(Context context) {
        this.context = context;
    }

    /**
     * Parse IP packet header
     */
    public IPPacket parseIPPacket(byte[] data, int length) {
        if (data == null || length < 20) {
            return null;
        }

        IPPacket packet = new IPPacket();
        ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);

        // Version and IHL (Header Length)
        byte versionIhl = buffer.get(0);
        packet.version = (versionIhl >> 4) & 0x0F;
        packet.headerLength = (versionIhl & 0x0F) * 4;

        if (packet.headerLength < 20) {
            return null;
        }

        // Type of Service
        packet.tos = buffer.get(1) & 0xFF;

        // Total Length
        packet.totalLength = ((buffer.get(2) & 0xFF) << 8) | (buffer.get(3) & 0xFF);

        // Identification
        packet.identification = ((buffer.get(4) & 0xFF) << 8) | (buffer.get(5) & 0xFF);

        // Flags and Fragment Offset
        int flagsFragment = ((buffer.get(6) & 0xFF) << 8) | (buffer.get(7) & 0xFF);
        packet.flags = (flagsFragment >> 13) & 0x07;
        packet.fragmentOffset = flagsFragment & 0x1FFF;

        // TTL
        packet.ttl = buffer.get(8) & 0xFF;

        // Protocol
        packet.protocol = buffer.get(9) & 0xFF;

        // Header Checksum
        packet.checksum = ((buffer.get(10) & 0xFF) << 8) | (buffer.get(11) & 0xFF);

        // Source IP
        packet.sourceIP = String.format("%d.%d.%d.%d",
                buffer.get(12) & 0xFF, buffer.get(13) & 0xFF,
                buffer.get(14) & 0xFF, buffer.get(15) & 0xFF);

        // Destination IP
        packet.destIP = String.format("%d.%d.%d.%d",
                buffer.get(16) & 0xFF, buffer.get(17) & 0xFF,
                buffer.get(18) & 0xFF, buffer.get(19) & 0xFF);

        // Parse transport layer
        if (packet.headerLength < length) {
            parseTransportLayer(packet, data, length);
        }

        return packet;
    }

    /**
     * Parse transport layer (TCP/UDP)
     */
    private void parseTransportLayer(IPPacket packet, byte[] data, int length) {
        int offset = packet.headerLength;

        if (offset >= length) return;

        switch (packet.protocol) {
            case PROTOCOL_TCP:
                parseTCPHeader(packet, data, length, offset);
                break;
            case PROTOCOL_UDP:
                parseUDPHeader(packet, data, length, offset);
                break;
        }
    }

    /**
     * Parse TCP header
     */
    private void parseTCPHeader(IPPacket packet, byte[] data, int length, int offset) {
        if (offset + 20 > length) return;

        ByteBuffer buffer = ByteBuffer.wrap(data, offset, length - offset);

        packet.sourcePort = ((buffer.get(0) & 0xFF) << 8) | (buffer.get(1) & 0xFF);
        packet.destPort = ((buffer.get(2) & 0xFF) << 8) | (buffer.get(3) & 0xFF);
        packet.seqNumber = buffer.getInt(4);
        packet.ackNumber = buffer.getInt(8);
        packet.tcpHeaderLength = ((buffer.get(12) >> 4) & 0x0F) * 4;
        packet.tcpFlags = buffer.get(13) & 0xFF;
        packet.windowSize = ((buffer.get(14) & 0xFF) << 8) | (buffer.get(15) & 0xFF);

        packet.transport = "TCP";
    }

    /**
     * Parse UDP header
     */
    private void parseUDPHeader(IPPacket packet, byte[] data, int length, int offset) {
        if (offset + 8 > length) return;

        ByteBuffer buffer = ByteBuffer.wrap(data, offset, length - offset);

        packet.sourcePort = ((buffer.get(0) & 0xFF) << 8) | (buffer.get(1) & 0xFF);
        packet.destPort = ((buffer.get(2) & 0xFF) << 8) | (buffer.get(3) & 0xFF);
        packet.udpLength = ((buffer.get(4) & 0xFF) << 8) | (buffer.get(5) & 0xFF);

        packet.transport = "UDP";
    }

    /**
     * Build IP packet
     */
    public byte[] buildIPPacket(IPPacket packet, byte[] payload) {
        int totalLength = 20 + (payload != null ? payload.length : 0);
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        // Version (4) and IHL (5 = 20 bytes)
        buffer.put((byte) 0x45);

        // TOS
        buffer.put((byte) packet.tos);

        // Total Length
        buffer.putShort((short) totalLength);

        // Identification
        buffer.putShort((short) packet.identification);

        // Flags and Fragment Offset
        buffer.putShort((short) ((packet.flags << 13) | packet.fragmentOffset));

        // TTL
        buffer.put((byte) packet.ttl);

        // Protocol
        buffer.put((byte) packet.protocol);

        // Checksum (placeholder)
        buffer.putShort((short) 0);

        // Source IP
        String[] srcParts = packet.sourceIP.split("\\.");
        for (String part : srcParts) {
            buffer.put((byte) Integer.parseInt(part));
        }

        // Dest IP
        String[] dstParts = packet.destIP.split("\\.");
        for (String part : dstParts) {
            buffer.put((byte) Integer.parseInt(part));
        }

        // Payload
        if (payload != null) {
            buffer.put(payload);
        }

        // Calculate and insert checksum
        int checksum = calculateChecksum(buffer.array(), 0, 20);
        buffer.putShort(10, (short) checksum);

        return buffer.array();
    }

    /**
     * Calculate IP checksum
     */
    private int calculateChecksum(byte[] data, int offset, int length) {
        int sum = 0;
        int count = length;

        for (int i = offset; i < offset + length; i += 2) {
            int word;
            if (i + 1 >= offset + length) {
                word = (data[i] & 0xFF) << 8;
            } else {
                word = ((data[i] & 0xFF) << 8) | (data[i + 1] & 0xFF);
            }
            sum += word;
        }

        while ((sum >> 16) != 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }

        return ~sum & 0xFFFF;
    }

    /**
     * IP Packet model
     */
    public static class IPPacket {
        public int version;
        public int headerLength;
        public int tos;
        public int totalLength;
        public int identification;
        public int flags;
        public int fragmentOffset;
        public int ttl;
        public int protocol;
        public int checksum;
        public String sourceIP;
        public String destIP;

        // Transport layer
        public String transport;
        public int sourcePort;
        public int destPort;
        public int seqNumber;
        public int ackNumber;
        public int tcpHeaderLength;
        public int tcpFlags;
        public int windowSize;
        public int udpLength;

        @Override
        public String toString() {
            return String.format("%s: %s:%d -> %s:%d (proto=%d, len=%d)",
                    transport != null ? transport : "IP",
                    sourceIP, sourcePort,
                    destIP, destPort,
                    protocol, totalLength);
        }
    }
}
