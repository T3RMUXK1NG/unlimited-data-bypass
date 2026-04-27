package com.t3rmuxk1ng.unlimiteddatabypass.core;

import java.util.HashMap;
import java.util.Map;

/**
 * ISP PAYLOAD CONFIGURATIONS
 * Real working payloads for bypass
 * GOD TIER EDITION
 */
public class ISPPayload {

    private String name;
    private String country;
    private String primaryDNS;
    private String secondaryDNS;
    private String freeHost;
    private String injectHost;
    private String proxyHost;
    private int proxyPort;
    private String testUrl;
    private String userAgent;
    private String[] alternateFreeHosts;
    private Map<String, String> customHeaders;

    public ISPPayload() {
        this.customHeaders = new HashMap<>();
        this.userAgent = "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
        this.testUrl = "https://www.google.com/generate_204";
    }

    // Getters
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getPrimaryDNS() { return primaryDNS != null ? primaryDNS : "8.8.8.8"; }
    public String getSecondaryDNS() { return secondaryDNS != null ? secondaryDNS : "8.8.4.4"; }
    public String getFreeHost() { return freeHost; }
    public String getInjectHost() { return injectHost; }
    public String getProxyHost() { return proxyHost; }
    public int getProxyPort() { return proxyPort; }
    public String getTestUrl() { return testUrl; }
    public String getUserAgent() { return userAgent; }
    public String[] getAlternateFreeHosts() { return alternateFreeHosts != null ? alternateFreeHosts : new String[]{}; }
    public Map<String, String> getCustomHeaders() { return customHeaders; }
    public String getWorkingFreeHost() { return freeHost; }
    public void setWorkingFreeHost(String host) { this.freeHost = host; }

    public boolean hasProxy() {
        return proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0;
    }

    // Builder Pattern
    public static class Builder {
        private ISPPayload payload;

        public Builder() {
            payload = new ISPPayload();
        }

        public Builder setName(String name) {
            payload.name = name;
            return this;
        }

        public Builder setCountry(String country) {
            payload.country = country;
            return this;
        }

        public Builder setDNS(String primary, String secondary) {
            payload.primaryDNS = primary;
            payload.secondaryDNS = secondary;
            return this;
        }

        public Builder setFreeHost(String host) {
            payload.freeHost = host;
            return this;
        }

        public Builder setInjectHost(String host) {
            payload.injectHost = host;
            return this;
        }

        public Builder setProxy(String host, int port) {
            payload.proxyHost = host;
            payload.proxyPort = port;
            return this;
        }

        public Builder setTestUrl(String url) {
            payload.testUrl = url;
            return this;
        }

        public Builder setUserAgent(String ua) {
            payload.userAgent = ua;
            return this;
        }

        public Builder setAlternateHosts(String... hosts) {
            payload.alternateFreeHosts = hosts;
            return this;
        }

        public Builder addHeader(String key, String value) {
            payload.customHeaders.put(key, value);
            return this;
        }

        public ISPPayload build() {
            return payload;
        }
    }

    /**
     * Get all ISP payloads
     */
    public static ISPPayload[] getAllPayloads() {
        return new ISPPayload[]{
                // ==================== JIO INDIA ====================
                new ISPPayload.Builder()
                        .setName("Jio (India)")
                        .setCountry("India")
                        .setDNS("49.44.128.1", "49.44.128.2")
                        .setFreeHost("www.jio.com")
                        .setInjectHost("jio.com")
                        .setTestUrl("https://www.jio.com")
                        .setAlternateHosts(
                                "www.jio.com",
                                "api.jio.com",
                                "myservices.jio.com",
                                "www.reliancejio.com",
                                "jiofi.local.html"
                        )
                        .addHeader("X-Jio-Client", "true")
                        .addHeader("X-Forwarded-For", "10.0.0.1")
                        .build(),

                // ==================== AIRTEL INDIA ====================
                new ISPPayload.Builder()
                        .setName("Airtel (India)")
                        .setCountry("India")
                        .setDNS("202.56.230.5", "202.56.240.5")
                        .setFreeHost("live.airtelworld.com")
                        .setInjectHost("airtel.in")
                        .setTestUrl("https://live.airtelworld.com")
                        .setAlternateHosts(
                                "live.airtelworld.com",
                                "www.airtel.in",
                                "one.airtel.in",
                                "airtelxstream.in",
                                "wynk.in"
                        )
                        .addHeader("X-Airtel-Client", "true")
                        .addHeader("X-Forwarded-Host", "live.airtelworld.com")
                        .build(),

                // ==================== VI (VODAFONE IDEA) ====================
                new ISPPayload.Builder()
                        .setName("Vi (India)")
                        .setCountry("India")
                        .setDNS("202.56.250.5", "202.56.250.6")
                        .setFreeHost("www.vodafone.in")
                        .setInjectHost("vodafone.in")
                        .setTestUrl("https://www.vodafone.in")
                        .setAlternateHosts(
                                "www.vodafone.in",
                                "www.ideacellular.com",
                                "vodafoneplay.in",
                                "vi.in"
                        )
                        .addHeader("X-Vodafone-Client", "true")
                        .build(),

                // ==================== BSNL INDIA ====================
                new ISPPayload.Builder()
                        .setName("BSNL (India)")
                        .setCountry("India")
                        .setDNS("218.248.255.145", "218.248.255.146")
                        .setFreeHost("www.bsnl.co.in")
                        .setInjectHost("bsnl.co.in")
                        .setTestUrl("https://www.bsnl.co.in")
                        .setAlternateHosts(
                                "www.bsnl.co.in",
                                "portal.bsnl.in",
                                "selfcare.bsnl.co.in"
                        )
                        .addHeader("X-BSNL-Client", "true")
                        .build(),

                // ==================== T-MOBILE USA ====================
                new ISPPayload.Builder()
                        .setName("T-Mobile (USA)")
                        .setCountry("USA")
                        .setDNS("208.67.222.222", "208.67.220.220")
                        .setFreeHost("www.t-mobile.com")
                        .setInjectHost("t-mobile.com")
                        .setTestUrl("https://www.t-mobile.com")
                        .setAlternateHosts(
                                "www.t-mobile.com",
                                "my.t-mobile.com",
                                "tmobile.com"
                        )
                        .build(),

                // ==================== AT&T USA ====================
                new ISPPayload.Builder()
                        .setName("AT&T (USA)")
                        .setCountry("USA")
                        .setDNS("68.94.156.1", "68.94.157.1")
                        .setFreeHost("www.att.com")
                        .setInjectHost("att.com")
                        .setTestUrl("https://www.att.com")
                        .setAlternateHosts(
                                "www.att.com",
                                "att.net",
                                "myatt.att.com"
                        )
                        .build(),

                // ==================== MTN AFRICA ====================
                new ISPPayload.Builder()
                        .setName("MTN (Africa)")
                        .setCountry("Africa")
                        .setDNS("10.199.212.120", "10.199.212.121")
                        .setFreeHost("www.mtn.com")
                        .setInjectHost("mtn.com")
                        .setTestUrl("https://www.mtn.com")
                        .setAlternateHosts(
                                "www.mtn.com",
                                "mtnonline.com",
                                "mtn.ng"
                        )
                        .build(),

                // ==================== GLOBE PHILIPPINES ====================
                new ISPPayload.Builder()
                        .setName("Globe (Philippines)")
                        .setCountry("Philippines")
                        .setDNS("202.52.128.2", "202.52.128.3")
                        .setFreeHost("www.globe.com.ph")
                        .setInjectHost("globe.com.ph")
                        .setTestUrl("https://www.globe.com.ph")
                        .setAlternateHosts(
                                "www.globe.com.ph",
                                "globe.ph",
                                "globe.com.ph"
                        )
                        .build(),

                // ==================== JAZZ PAKISTAN ====================
                new ISPPayload.Builder()
                        .setName("Jazz (Pakistan)")
                        .setCountry("Pakistan")
                        .setDNS("119.160.1.1", "119.160.1.2")
                        .setFreeHost("www.jazz.com.pk")
                        .setInjectHost("jazz.com.pk")
                        .setTestUrl("https://www.jazz.com.pk")
                        .setAlternateHosts(
                                "www.jazz.com.pk",
                                "jazz.com.pk",
                                "mobilink.com.pk"
                        )
                        .build(),

                // ==================== GENERIC (AUTO DETECT) ====================
                new ISPPayload.Builder()
                        .setName("Auto Detect")
                        .setCountry("Global")
                        .setDNS("1.1.1.1", "1.0.0.1")
                        .setFreeHost("www.google.com")
                        .setTestUrl("https://www.google.com/generate_204")
                        .setAlternateHosts(
                                "www.google.com",
                                "www.cloudflare.com",
                                "www.microsoft.com",
                                "www.apple.com"
                        )
                        .build(),
        };
    }

    /**
     * Get ISP by name
     */
    public static ISPPayload getByName(String name) {
        for (ISPPayload payload : getAllPayloads()) {
            if (payload.getName().equalsIgnoreCase(name)) {
                return payload;
            }
        }
        return getAllPayloads()[0]; // Return first (Jio) as default
    }

    /**
     * Get all ISP names
     */
    public static String[] getAllNames() {
        ISPPayload[] payloads = getAllPayloads();
        String[] names = new String[payloads.length];
        for (int i = 0; i < payloads.length; i++) {
            names[i] = payloads[i].getName();
        }
        return names;
    }
}
