package com.t3rmuxk1ng.unlimiteddatabypass.models;

import java.io.Serializable;

/**
 * ISP CONFIGURATION MODEL
 * Contains all ISP-specific bypass settings
 */
public class ISPConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    // Basic Info
    private String name;
    private String country;
    
    // APN Configuration
    private String apnName;
    private String apnProxy;
    private String apnPort;
    
    // DNS Configuration
    private String primaryDNS;
    private String secondaryDNS;
    
    // Header Injection
    private String hostHeader;
    
    // Bypass Methods
    private boolean apnBypassSupported;
    private boolean dnsBypassSupported;
    private boolean headerBypassSupported;
    private boolean proxyBypassSupported;
    private boolean tunnelBypassSupported;
    private boolean vpnBypassSupported;
    
    // Speed Optimization
    private boolean supports5G;
    private int maxSpeedMbps;
    
    // Constructor
    public ISPConfig() {}

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
        
        public Builder setHostHeader(String host) {
            config.hostHeader = host;
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
    public String getName() { return name != null ? name : "Unknown ISP"; }
    public String getCountry() { return country != null ? country : "Unknown"; }
    public String getApnName() { return apnName != null ? apnName : "internet"; }
    public String getApnProxy() { return apnProxy != null ? apnProxy : ""; }
    public String getApnPort() { return apnPort != null ? apnPort : "80"; }
    public String getPrimaryDNS() { return primaryDNS != null ? primaryDNS : "8.8.8.8"; }
    public String getSecondaryDNS() { return secondaryDNS != null ? secondaryDNS : "8.8.4.4"; }
    public String getHostHeader() { return hostHeader != null ? hostHeader : ""; }
    public boolean isApnBypassSupported() { return apnBypassSupported; }
    public boolean isDnsBypassSupported() { return dnsBypassSupported; }
    public boolean isHeaderBypassSupported() { return headerBypassSupported; }
    public boolean isProxyBypassSupported() { return proxyBypassSupported; }
    public boolean isTunnelBypassSupported() { return tunnelBypassSupported; }
    public boolean isVpnBypassSupported() { return vpnBypassSupported; }
    public boolean isSupports5G() { return supports5G; }
    public int getMaxSpeedMbps() { return maxSpeedMbps; }
}
