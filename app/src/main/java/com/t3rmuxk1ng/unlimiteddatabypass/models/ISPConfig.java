package com.t3rmuxk1ng.unlimiteddatabypass.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ISP CONFIGURATION MODEL
 * Contains all ISP-specific bypass settings
 */
public class ISPConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    // Basic Info
    private String name;
    private String country;
    private String mccMnc;
    private String operatorCode;
    
    // APN Configuration
    private String apnName;
    private String apnProxy;
    private String apnPort;
    private String apnUser;
    private String apnPass;
    private String apnServer;
    private String apnMmsc;
    private String apnType;
    
    // DNS Configuration
    private String primaryDNS;
    private String secondaryDNS;
    private String tertiaryDNS;
    private String[] customDNS;
    
    // Proxy Configuration
    private String proxyHost;
    private int proxyPort;
    private String proxyType; // HTTP, SOCKS4, SOCKS5
    
    // Header Injection
    private List<HTTPHeader> headers;
    private String hostHeader;
    private String xForwardedFor;
    private String userAgent;
    
    // Bypass Methods
    private boolean apnBypassSupported;
    private boolean dnsBypassSupported;
    private boolean headerBypassSupported;
    private boolean proxyBypassSupported;
    private boolean tunnelBypassSupported;
    private boolean vpnBypassSupported;
    
    // Speed Optimization
    private boolean supports5G;
    private boolean supportsLTE;
    private int maxSpeedMbps;
    private String speedProfile; // FAST, BALANCED, POWER_SAVING
    
    // Special Configuration
    private String customPayload;
    private String bugHost;
    private String sniHost;
    private String tunnelType; // SSH, SSL, DIRECT
    private int tunnelPort;
    
    // Constructor
    public ISPConfig() {
        this.headers = new ArrayList<>();
        this.customDNS = new String[8];
    }
    
    // Builder Pattern
    public static class Builder {
        private ISPConfig config;
        
        public Builder() {
            config = new ISPConfig();
        }
        
        public Builder setName(String name) {
            config.name = name;
            return this;
        }
        
        public Builder setCountry(String country) {
            config.country = country;
            return this;
        }
        
        public Builder setMccMnc(String mccMnc) {
            config.mccMnc = mccMnc;
            return this;
        }
        
        public Builder setOperatorCode(String code) {
            config.operatorCode = code;
            return this;
        }
        
        public Builder setAPN(String name, String proxy, String port) {
            config.apnName = name;
            config.apnProxy = proxy;
            config.apnPort = port;
            return this;
        }
        
        public Builder setDNS(String primary, String secondary) {
            config.primaryDNS = primary;
            config.secondaryDNS = secondary;
            return this;
        }
        
        public Builder setProxy(String host, int port, String type) {
            config.proxyHost = host;
            config.proxyPort = port;
            config.proxyType = type;
            return this;
        }
        
        public Builder addHeader(String name, String value) {
            config.headers.add(new HTTPHeader(name, value));
            return this;
        }
        
        public Builder setHostHeader(String host) {
            config.hostHeader = host;
            return this;
        }
        
        public Builder setUserAgent(String ua) {
            config.userAgent = ua;
            return this;
        }
        
        public Builder setBugHost(String host) {
            config.bugHost = host;
            return this;
        }
        
        public Builder setSNIHost(String host) {
            config.sniHost = host;
            return this;
        }
        
        public Builder setTunnel(String type, int port) {
            config.tunnelType = type;
            config.tunnelPort = port;
            return this;
        }
        
        public Builder setSpeed(boolean supports5G, int maxSpeed) {
            config.supports5G = supports5G;
            config.maxSpeedMbps = maxSpeed;
            return this;
        }
        
        public Builder enableAllBypassMethods() {
            config.apnBypassSupported = true;
            config.dnsBypassSupported = true;
            config.headerBypassSupported = true;
            config.proxyBypassSupported = true;
            config.tunnelBypassSupported = true;
            config.vpnBypassSupported = true;
            return this;
        }
        
        public ISPConfig build() {
            return config;
        }
    }
    
    // Getters
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getMccMnc() { return mccMnc; }
    public String getOperatorCode() { return operatorCode; }
    public String getApnName() { return apnName; }
    public String getApnProxy() { return apnProxy; }
    public String getApnPort() { return apnPort; }
    public String getPrimaryDNS() { return primaryDNS; }
    public String getSecondaryDNS() { return secondaryDNS; }
    public String getProxyHost() { return proxyHost; }
    public int getProxyPort() { return proxyPort; }
    public String getProxyType() { return proxyType; }
    public List<HTTPHeader> getHeaders() { return headers; }
    public String getHostHeader() { return hostHeader; }
    public String getUserAgent() { return userAgent; }
    public String getBugHost() { return bugHost; }
    public String getSniHost() { return sniHost; }
    public String getTunnelType() { return tunnelType; }
    public int getTunnelPort() { return tunnelPort; }
    public boolean isApnBypassSupported() { return apnBypassSupported; }
    public boolean isDnsBypassSupported() { return dnsBypassSupported; }
    public boolean isHeaderBypassSupported() { return headerBypassSupported; }
    public boolean isProxyBypassSupported() { return proxyBypassSupported; }
    public boolean isTunnelBypassSupported() { return tunnelBypassSupported; }
    public boolean isVpnBypassSupported() { return vpnBypassSupported; }
    public boolean isSupports5G() { return supports5G; }
    public int getMaxSpeedMbps() { return maxSpeedMbps; }
    
    /**
     * HTTP Header Model
     */
    public static class HTTPHeader implements Serializable {
        private String name;
        private String value;
        
        public HTTPHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public String getValue() { return value; }
        
        @Override
        public String toString() {
            return name + ": " + value;
        }
    }
}
