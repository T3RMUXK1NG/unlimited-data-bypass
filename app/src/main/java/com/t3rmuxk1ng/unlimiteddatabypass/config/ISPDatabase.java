package com.t3rmuxk1ng.unlimiteddatabypass.config;

import com.t3rmuxk1ng.unlimiteddatabypass.models.ISPConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * ISP DATABASE - ALL CARRIER CONFIGURATIONS
 * Contains bypass settings for ISPs worldwide
 * GOD TIER EDITION
 */
public class ISPDatabase {

    private static final String TAG = "ISPDatabase";
    private static List<ISPConfig> ispList = new ArrayList<>();
    
    static {
        initializeISPs();
    }
    
    private static void initializeISPs() {
        // ==================== INDIAN ISPs ====================
        
        ispList.add(new ISPConfig.Builder()
            .setName("Jio (India)")
            .setCountry("India")
            .setAPN("jionet", "", "80")
            .setDNS("49.44.128.1", "49.44.128.2")
            .setHostHeader("www.jio.com")
            .setSpeed(true, 1000)
            .enableAllBypassMethods()
            .build());
        
        ispList.add(new ISPConfig.Builder()
            .setName("Airtel (India)")
            .setCountry("India")
            .setAPN("airtelgprs.com", "", "80")
            .setDNS("202.56.230.5", "202.56.240.5")
            .setHostHeader("www.airtel.in")
            .setSpeed(true, 900)
            .enableAllBypassMethods()
            .build());
        
        ispList.add(new ISPConfig.Builder()
            .setName("Vi (India)")
            .setCountry("India")
            .setAPN("www.vodafone.in", "", "80")
            .setDNS("202.56.250.5", "202.56.250.6")
            .setHostHeader("www.vodafone.in")
            .setSpeed(true, 800)
            .enableAllBypassMethods()
            .build());
        
        ispList.add(new ISPConfig.Builder()
            .setName("BSNL (India)")
            .setCountry("India")
            .setAPN("bsnlnet", "", "80")
            .setDNS("218.248.255.145", "218.248.255.146")
            .setHostHeader("www.bsnl.co.in")
            .setSpeed(false, 100)
            .enableAllBypassMethods()
            .build());
        
        // ==================== USA ISPs ====================
        
        ispList.add(new ISPConfig.Builder()
            .setName("T-Mobile (USA)")
            .setCountry("USA")
            .setAPN("fast.t-mobile.com", "", "80")
            .setDNS("208.67.222.222", "208.67.220.220")
            .setHostHeader("www.t-mobile.com")
            .setSpeed(true, 2000)
            .enableAllBypassMethods()
            .build());
        
        ispList.add(new ISPConfig.Builder()
            .setName("AT&T (USA)")
            .setCountry("USA")
            .setAPN("phone", "", "80")
            .setDNS("68.94.156.1", "68.94.157.1")
            .setHostHeader("www.att.com")
            .setSpeed(true, 1500)
            .enableAllBypassMethods()
            .build());
        
        ispList.add(new ISPConfig.Builder()
            .setName("Verizon (USA)")
            .setCountry("USA")
            .setAPN("vzwinternet", "", "80")
            .setDNS("4.2.2.1", "4.2.2.2")
            .setHostHeader("www.verizon.com")
            .setSpeed(true, 1500)
            .enableAllBypassMethods()
            .build());
        
        // ==================== AFRICAN ISPs ====================
        
        ispList.add(new ISPConfig.Builder()
            .setName("MTN (Africa)")
            .setCountry("Africa")
            .setAPN("web.gprs.mtnnigeria.net", "", "80")
            .setDNS("10.199.212.120", "10.199.212.121")
            .setHostHeader("www.mtn.com")
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build());
        
        // ==================== PHILIPPINES ISPs ====================
        
        ispList.add(new ISPConfig.Builder()
            .setName("Globe (Philippines)")
            .setCountry("Philippines")
            .setAPN("http.globe.com.ph", "", "80")
            .setDNS("202.52.128.2", "202.52.128.3")
            .setHostHeader("www.globe.com.ph")
            .setSpeed(true, 600)
            .enableAllBypassMethods()
            .build());
        
        ispList.add(new ISPConfig.Builder()
            .setName("Smart (Philippines)")
            .setCountry("Philippines")
            .setAPN("smartbro", "", "80")
            .setDNS("202.57.96.1", "202.57.96.2")
            .setHostHeader("www.smart.com.ph")
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build());
        
        // ==================== INDONESIA ISPs ====================
        
        ispList.add(new ISPConfig.Builder()
            .setName("Telkomsel (Indonesia)")
            .setCountry("Indonesia")
            .setAPN("telkomsel", "", "80")
            .setDNS("202.3.208.11", "202.3.210.11")
            .setHostHeader("www.telkomsel.com")
            .setSpeed(true, 700)
            .enableAllBypassMethods()
            .build());
        
        // ==================== BRAZIL ISPs ====================
        
        ispList.add(new ISPConfig.Builder()
            .setName("Vivo (Brazil)")
            .setCountry("Brazil")
            .setAPN("zap.vivo.com.br", "", "80")
            .setDNS("187.6.0.1", "187.6.0.2")
            .setHostHeader("www.vivo.com.br")
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build());
        
        // ==================== UK ISPs ====================
        
        ispList.add(new ISPConfig.Builder()
            .setName("EE (UK)")
            .setCountry("UK")
            .setAPN("everywhere", "", "80")
            .setDNS("87.194.0.51", "87.194.0.52")
            .setHostHeader("www.ee.co.uk")
            .setSpeed(true, 1000)
            .enableAllBypassMethods()
            .build());
        
        // ==================== PAKISTAN ISPs ====================
        
        ispList.add(new ISPConfig.Builder()
            .setName("Jazz (Pakistan)")
            .setCountry("Pakistan")
            .setAPN("jazzconnect.mobilinkworld.com", "", "80")
            .setDNS("119.160.1.1", "119.160.1.2")
            .setHostHeader("www.jazz.com.pk")
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build());
        
        ispList.add(new ISPConfig.Builder()
            .setName("Zong (Pakistan)")
            .setCountry("Pakistan")
            .setAPN("zonginternet", "", "80")
            .setDNS("202.125.140.2", "202.125.140.3")
            .setHostHeader("www.zong.com.pk")
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build());
        
        // ==================== BANGLADESH ISPs ====================
        
        ispList.add(new ISPConfig.Builder()
            .setName("Grameenphone (Bangladesh)")
            .setCountry("Bangladesh")
            .setAPN("gpinternet", "", "80")
            .setDNS("202.56.4.120", "202.56.4.121")
            .setHostHeader("www.grameenphone.com")
            .setSpeed(true, 400)
            .enableAllBypassMethods()
            .build());
    }
    
    /**
     * Detect ISP from operator name
     */
    public static ISPConfig detectISP(String operatorName, String mccMnc) {
        if (operatorName == null || operatorName.isEmpty()) {
            return getDefaultISP();
        }
        
        String opLower = operatorName.toLowerCase();
        
        // Try to match by name
        for (ISPConfig isp : ispList) {
            if (isp.getName() != null && 
                isp.getName().toLowerCase().contains(opLower)) {
                return isp;
            }
        }
        
        return getDefaultISP();
    }
    
    /**
     * Get all ISP names
     */
    public static String[] getAllISPNames() {
        String[] names = new String[ispList.size()];
        for (int i = 0; i < ispList.size(); i++) {
            names[i] = ispList.get(i).getName();
        }
        return names;
    }
    
    /**
     * Get ISP by index
     */
    public static ISPConfig getISPByIndex(int index) {
        if (index >= 0 && index < ispList.size()) {
            return ispList.get(index);
        }
        return getDefaultISP();
    }
    
    /**
     * Get default ISP
     */
    public static ISPConfig getDefaultISP() {
        if (!ispList.isEmpty()) {
            return ispList.get(0);
        }
        // Fallback
        return new ISPConfig.Builder()
            .setName("Default ISP")
            .setCountry("Unknown")
            .setAPN("internet", "", "80")
            .setDNS("8.8.8.8", "8.8.4.4")
            .enableAllBypassMethods()
            .build();
    }
    
    /**
     * Get all ISPs
     */
    public static List<ISPConfig> getAllISPs() {
        return new ArrayList<>(ispList);
    }
}
