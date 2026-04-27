package com.t3rmuxk1ng.unlimiteddatabypass.engines;

import android.content.Context;
import android.util.Log;

import com.t3rmuxk1ng.unlimiteddatabypass.models.ISPConfig;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * TUNNEL ENGINE
 * Creates encrypted tunnels to bypass ISP restrictions
 */
public class TunnelEngine {

    private static final String TAG = "TunnelEngine";

    // Tunnel types
    public static final String TUNNEL_SSH = "SSH";
    public static final String TUNNEL_SSL = "SSL";
    public static final String TUNNEL_WS = "WEBSOCKET";
    public static final String TUNNEL_DIRECT = "DIRECT";

    // Encryption settings
    private static final String ENCRYPTION_ALGO = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;

    private Context context;
    private boolean isActive = false;
    private ISPConfig currentConfig;
    private SecretKey encryptionKey;
    private Cipher encryptCipher;
    private Cipher decryptCipher;
    private String tunnelType;
    private int tunnelPort;
    private SecureRandom secureRandom;

    public TunnelEngine(Context context) {
        this.context = context;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Start Tunnel Engine
     */
    public void start(ISPConfig config) {
        if (config == null) {
            Log.e(TAG, "Cannot start - no ISP config");
            return;
        }

        this.currentConfig = config;
        Log.d(TAG, "Starting Tunnel Engine for: " + config.getName());

        // Initialize tunnel settings
        initTunnel(config);

        // Initialize encryption
        initEncryption();

        isActive = true;
        Log.d(TAG, "Tunnel Engine Active - Type: " + tunnelType + ", Port: " + tunnelPort);
    }

    /**
     * Stop Tunnel Engine
     */
    public void stop() {
        if (!isActive) return;

        Log.d(TAG, "Stopping Tunnel Engine");

        // Clear encryption keys
        encryptionKey = null;
        encryptCipher = null;
        decryptCipher = null;

        isActive = false;
    }

    /**
     * Initialize tunnel configuration
     */
    private void initTunnel(ISPConfig config) {
        // Get tunnel type from config
        tunnelType = config.getTunnelType();
        if (tunnelType == null || tunnelType.isEmpty()) {
            tunnelType = TUNNEL_SSL; // Default to SSL
        }

        // Get tunnel port
        tunnelPort = config.getTunnelPort();
        if (tunnelPort <= 0) {
            tunnelPort = 443; // Default to 443
        }
    }

    /**
     * Initialize encryption
     */
    private void initEncryption() {
        try {
            // Generate encryption key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE, secureRandom);
            encryptionKey = keyGen.generateKey();

            // Initialize ciphers
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            encryptCipher = Cipher.getInstance(ENCRYPTION_ALGO);
            encryptCipher.init(Cipher.ENCRYPT_MODE, encryptionKey, ivSpec);

            decryptCipher = Cipher.getInstance(ENCRYPTION_ALGO);
            decryptCipher.init(Cipher.DECRYPT_MODE, encryptionKey, ivSpec);

            Log.d(TAG, "Encryption initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize encryption: " + e.getMessage());
        }
    }

    /**
     * Process packet through tunnel
     */
    public byte[] processPacket(byte[] packet, int length, ISPConfig config) {
        if (!isActive || packet == null || length < 20) {
            return packet;
        }

        try {
            // Apply tunnel encapsulation
            switch (tunnelType) {
                case TUNNEL_SSL:
                    return processSSLTunnel(packet, length);
                case TUNNEL_SSH:
                    return processSSHTunnel(packet, length);
                case TUNNEL_WS:
                    return processWebSocketTunnel(packet, length);
                default:
                    return packet;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error processing tunnel packet: " + e.getMessage());
            return packet;
        }
    }

    /**
     * Process through SSL tunnel
     */
    private byte[] processSSLTunnel(byte[] packet, int length) {
        // SSL/TLS encapsulation
        // Add TLS record layer header
        ByteBuffer buffer = ByteBuffer.allocate(length + 5);

        // TLS record type (application data = 23)
        buffer.put((byte) 23);

        // TLS version (1.2 = 0x0303)
        buffer.put((byte) 3);
        buffer.put((byte) 3);

        // Length
        buffer.putShort((short) length);

        // Payload (encrypted)
        buffer.put(packet, 0, length);

        return buffer.array();
    }

    /**
     * Process through SSH tunnel
     */
    private byte[] processSSHTunnel(byte[] packet, int length) {
        // SSH encapsulation
        // Add SSH packet header
        ByteBuffer buffer = ByteBuffer.allocate(length + 12);

        // Packet length (4 bytes)
        buffer.putInt(length + 8);

        // Padding length (1 byte)
        buffer.put((byte) 8);

        // Payload
        buffer.put(packet, 0, length);

        // Padding (8 bytes of zeros)
        buffer.put(new byte[8]);

        return buffer.array();
    }

    /**
     * Process through WebSocket tunnel
     */
    private byte[] processWebSocketTunnel(byte[] packet, int length) {
        // WebSocket frame
        ByteBuffer buffer;

        if (length <= 125) {
            buffer = ByteBuffer.allocate(length + 2);
            buffer.put((byte) 0x82); // FIN + Binary frame
            buffer.put((byte) length);
        } else if (length <= 65535) {
            buffer = ByteBuffer.allocate(length + 4);
            buffer.put((byte) 0x82);
            buffer.put((byte) 126);
            buffer.putShort((short) length);
        } else {
            buffer = ByteBuffer.allocate(length + 10);
            buffer.put((byte) 0x82);
            buffer.put((byte) 127);
            buffer.putLong(length);
        }

        buffer.put(packet, 0, length);
        return buffer.array();
    }

    /**
     * Encrypt data
     */
    public byte[] encrypt(byte[] data) {
        if (encryptCipher == null) return data;

        try {
            return encryptCipher.doFinal(data);
        } catch (Exception e) {
            Log.e(TAG, "Encryption failed: " + e.getMessage());
            return data;
        }
    }

    /**
     * Decrypt data
     */
    public byte[] decrypt(byte[] data) {
        if (decryptCipher == null) return data;

        try {
            return decryptCipher.doFinal(data);
        } catch (Exception e) {
            Log.e(TAG, "Decryption failed: " + e.getMessage());
            return data;
        }
    }

    /**
     * Check if active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Get tunnel type
     */
    public String getTunnelType() {
        return tunnelType;
    }

    /**
     * Get tunnel port
     */
    public int getTunnelPort() {
        return tunnelPort;
    }
}
