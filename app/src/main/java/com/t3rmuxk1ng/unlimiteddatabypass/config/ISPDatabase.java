package com.t3rmuxk1ng.unlimiteddatabypass.config;

import android.telephony.TelephonyManager;

import com.t3rmuxk1ng.unlimiteddatabypass.models.ISPConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ISP DATABASE - ALL CARRIER CONFIGURATIONS
 * Contains bypass settings for ISPs worldwide
 * GOD TIER EDITION
 */
public class ISPDatabase {

    private static final String TAG = "ISPDatabase";
    private static List<ISPConfig> ispList = new ArrayList<>();
    private static Map<String, ISPConfig> ispMap = new HashMap<>();
    
    static {
        initializeISPs();
    }
    
    private static void initializeISPs() {
        // ==================== INDIAN ISPs ====================
        
        // JIO INDIA
        ISPConfig jio = new ISPConfig.Builder()
            .setName("Jio (India)")
            .setCountry("India")
            .setMccMnc("405")
            .setOperatorCode("JIO")
            .setAPN("jionet", "", "80")
            .setDNS("49.44.128.1", "49.44.128.2")
            .addHeader("Host", "www.jio.com")
            .addHeader("X-Forwarded-For", "127.0.0.1")
            .setHostHeader("www.jio.com")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.jio.com")
            .setSNIHost("www.jio.com")
            .setTunnel("SSL", 443)
            .setSpeed(true, 1000)
            .enableAllBypassMethods()
            .build();
        ispList.add(jio);
        ispMap.put("JIO", jio);
        ispMap.put("Reliance Jio", jio);
        ispMap.put("jio", jio);
        
        // AIRTEL INDIA
        ISPConfig airtel = new ISPConfig.Builder()
            .setName("Airtel (India)")
            .setCountry("India")
            .setMccMnc("404")
            .setOperatorCode("AIRTEL")
            .setAPN("airtelgprs.com", "", "80")
            .setDNS("202.56.230.5", "202.56.240.5")
            .addHeader("Host", "www.airtel.in")
            .addHeader("X-Online-Host", "www.airtel.in")
            .setHostHeader("www.airtel.in")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.airtel.in")
            .setSNIHost("www.airtel.in")
            .setTunnel("SSL", 443)
            .setSpeed(true, 900)
            .enableAllBypassMethods()
            .build();
        ispList.add(airtel);
        ispMap.put("AIRTEL", airtel);
        ispMap.put("Airtel", airtel);
        ispMap.put("Bharti Airtel", airtel);
        
        // VI (VODAFONE IDEA) INDIA
        ISPConfig vi = new ISPConfig.Builder()
            .setName("Vi (India)")
            .setCountry("India")
            .setMccMnc("404")
            .setOperatorCode("VI")
            .setAPN("www.vodafone.in", "", "80")
            .setDNS("202.56.250.5", "202.56.250.6")
            .addHeader("Host", "www.vodafone.in")
            .addHeader("X-Forwarded-For", "127.0.0.1")
            .setHostHeader("www.vodafone.in")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.vodafoneidea.com")
            .setSNIHost("www.vodafoneidea.com")
            .setTunnel("SSL", 443)
            .setSpeed(true, 800)
            .enableAllBypassMethods()
            .build();
        ispList.add(vi);
        ispMap.put("VI", vi);
        ispMap.put("Vodafone", vi);
        ispMap.put("Idea", vi);
        ispMap.put("Vodafone Idea", vi);
        
        // BSNL INDIA
        ISPConfig bsnl = new ISPConfig.Builder()
            .setName("BSNL (India)")
            .setCountry("India")
            .setMccMnc("404")
            .setOperatorCode("BSNL")
            .setAPN("bsnlnet", "", "80")
            .setDNS("218.248.255.145", "218.248.255.146")
            .addHeader("Host", "www.bsnl.co.in")
            .setHostHeader("www.bsnl.co.in")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.bsnl.co.in")
            .setSNIHost("www.bsnl.co.in")
            .setTunnel("SSL", 443)
            .setSpeed(false, 100)
            .enableAllBypassMethods()
            .build();
        ispList.add(bsnl);
        ispMap.put("BSNL", bsnl);
        ispMap.put("BSNL Mobile", bsnl);
        
        // ==================== USA ISPs ====================
        
        // T-MOBILE USA
        ISPConfig tmoblie = new ISPConfig.Builder()
            .setName("T-Mobile (USA)")
            .setCountry("USA")
            .setMccMnc("310")
            .setOperatorCode("TMOBILE")
            .setAPN("fast.t-mobile.com", "", "80")
            .setDNS("208.67.222.222", "208.67.220.220")
            .addHeader("Host", "www.t-mobile.com")
            .setHostHeader("www.t-mobile.com")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.t-mobile.com")
            .setSNIHost("www.t-mobile.com")
            .setTunnel("SSL", 443)
            .setSpeed(true, 2000)
            .enableAllBypassMethods()
            .build();
        ispList.add(tmoblie);
        ispMap.put("T-Mobile", tmoblie);
        ispMap.put("TMOBILE", tmoblie);
        
        // AT&T USA
        ISPConfig att = new ISPConfig.Builder()
            .setName("AT&T (USA)")
            .setCountry("USA")
            .setMccMnc("310")
            .setOperatorCode("ATT")
            .setAPN("phone", "", "80")
            .setDNS("68.94.156.1", "68.94.157.1")
            .addHeader("Host", "www.att.com")
            .setHostHeader("www.att.com")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.att.com")
            .setSNIHost("www.att.com")
            .setTunnel("SSL", 443)
            .setSpeed(true, 1500)
            .enableAllBypassMethods()
            .build();
        ispList.add(att);
        ispMap.put("AT&T", att);
        ispMap.put("ATT", att);
        
        // VERIZON USA
        ISPConfig verizon = new ISPConfig.Builder()
            .setName("Verizon (USA)")
            .setCountry("USA")
            .setMccMnc("311")
            .setOperatorCode("VERIZON")
            .setAPN("vzwinternet", "", "80")
            .setDNS("4.2.2.1", "4.2.2.2")
            .addHeader("Host", "www.verizon.com")
            .setHostHeader("www.verizon.com")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.verizon.com")
            .setSNIHost("www.verizon.com")
            .setTunnel("SSL", 443)
            .setSpeed(true, 1500)
            .enableAllBypassMethods()
            .build();
        ispList.add(verizon);
        ispMap.put("Verizon", verizon);
        ispMap.put("VERIZON", verizon);
        
        // ==================== AFRICAN ISPs ====================
        
        // MTN AFRICA
        ISPConfig mtn = new ISPConfig.Builder()
            .setName("MTN (Africa)")
            .setCountry("Africa")
            .setMccMnc("621")
            .setOperatorCode("MTN")
            .setAPN("web.gprs.mtnnigeria.net", "", "80")
            .setDNS("10.199.212.120", "10.199.212.121")
            .addHeader("Host", "www.mtn.com")
            .setHostHeader("www.mtn.com")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.mtn.com")
            .setSNIHost("www.mtn.com")
            .setTunnel("SSL", 443)
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build();
        ispList.add(mtn);
        ispMap.put("MTN", mtn);
        ispMap.put("MTN Nigeria", mtn);
        
        // AIRTEL AFRICA
        ISPConfig airtelAfrica = new ISPConfig.Builder()
            .setName("Airtel (Africa)")
            .setCountry("Africa")
            .setMccMnc("621")
            .setOperatorCode("AIRTEL_AF")
            .setAPN("internet.ng.airtel.com", "", "80")
            .setDNS("10.200.212.120", "10.200.212.121")
            .addHeader("Host", "www.airtel.com")
            .setHostHeader("www.airtel.com")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.airtel.com")
            .setSNIHost("www.airtel.com")
            .setTunnel("SSL", 443)
            .setSpeed(true, 400)
            .enableAllBypassMethods()
            .build();
        ispList.add(airtelAfrica);
        ispMap.put("Airtel Africa", airtelAfrica);
        
        // ==================== PHILIPPINES ISPs ====================
        
        // GLOBE PHILIPPINES
        ISPConfig globe = new ISPConfig.Builder()
            .setName("Globe (Philippines)")
            .setCountry("Philippines")
            .setMccMnc("515")
            .setOperatorCode("GLOBE")
            .setAPN("http.globe.com.ph", "", "80")
            .setDNS("202.52.128.2", "202.52.128.3")
            .addHeader("Host", "www.globe.com.ph")
            .setHostHeader("www.globe.com.ph")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.globe.com.ph")
            .setSNIHost("www.globe.com.ph")
            .setTunnel("SSL", 443)
            .setSpeed(true, 600)
            .enableAllBypassMethods()
            .build();
        ispList.add(globe);
        ispMap.put("Globe", globe);
        ispMap.put("GLOBE", globe);
        
        // SMART PHILIPPINES
        ISPConfig smart = new ISPConfig.Builder()
            .setName("Smart (Philippines)")
            .setCountry("Philippines")
            .setMccMnc("515")
            .setOperatorCode("SMART")
            .setAPN("smartbro", "", "80")
            .setDNS("202.57.96.1", "202.57.96.2")
            .addHeader("Host", "www.smart.com.ph")
            .setHostHeader("www.smart.com.ph")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.smart.com.ph")
            .setSNIHost("www.smart.com.ph")
            .setTunnel("SSL", 443)
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build();
        ispList.add(smart);
        ispMap.put("Smart", smart);
        ispMap.put("SMART", smart);
        
        // ==================== INDONESIA ISPs ====================
        
        // TELKOMSEL INDONESIA
        ISPConfig telkomsel = new ISPConfig.Builder()
            .setName("Telkomsel (Indonesia)")
            .setCountry("Indonesia")
            .setMccMnc("510")
            .setOperatorCode("TELKOMSEL")
            .setAPN("telkomsel", "", "80")
            .setDNS("202.3.208.11", "202.3.210.11")
            .addHeader("Host", "www.telkomsel.com")
            .setHostHeader("www.telkomsel.com")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.telkomsel.com")
            .setSNIHost("www.telkomsel.com")
            .setTunnel("SSL", 443)
            .setSpeed(true, 700)
            .enableAllBypassMethods()
            .build();
        ispList.add(telkomsel);
        ispMap.put("Telkomsel", telkomsel);
        ispMap.put("TELKOMSEL", telkomsel);
        
        // XL INDONESIA
        ISPConfig xl = new ISPConfig.Builder()
            .setName("XL Axiata (Indonesia)")
            .setCountry("Indonesia")
            .setMccMnc("510")
            .setOperatorCode("XL")
            .setAPN("internet", "", "80")
            .setDNS("202.152.0.2", "202.152.0.3")
            .addHeader("Host", "www.xl.co.id")
            .setHostHeader("www.xl.co.id")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.xl.co.id")
            .setSNIHost("www.xl.co.id")
            .setTunnel("SSL", 443)
            .setSpeed(true, 600)
            .enableAllBypassMethods()
            .build();
        ispList.add(xl);
        ispMap.put("XL", xl);
        ispMap.put("XL Axiata", xl);
        
        // ==================== BRAZIL ISPs ====================
        
        // VIVO BRAZIL
        ISPConfig vivo = new ISPConfig.Builder()
            .setName("Vivo (Brazil)")
            .setCountry("Brazil")
            .setMccMnc("724")
            .setOperatorCode("VIVO")
            .setAPN("zap.vivo.com.br", "", "80")
            .setDNS("187.6.0.1", "187.6.0.2")
            .addHeader("Host", "www.vivo.com.br")
            .setHostHeader("www.vivo.com.br")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.vivo.com.br")
            .setSNIHost("www.vivo.com.br")
            .setTunnel("SSL", 443)
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build();
        ispList.add(vivo);
        ispMap.put("Vivo", vivo);
        ispMap.put("VIVO", vivo);
        
        // CLARO BRAZIL
        ISPConfig claro = new ISPConfig.Builder()
            .setName("Claro (Brazil)")
            .setCountry("Brazil")
            .setMccMnc("724")
            .setOperatorCode("CLARO")
            .setAPN("claro.com.br", "", "80")
            .setDNS("200.230.210.2", "200.230.210.3")
            .addHeader("Host", "www.claro.com.br")
            .setHostHeader("www.claro.com.br")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.claro.com.br")
            .setSNIHost("www.claro.com.br")
            .setTunnel("SSL", 443)
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build();
        ispList.add(claro);
        ispMap.put("Claro", claro);
        ispMap.put("CLARO", claro);
        
        // ==================== UK ISPs ====================
        
        // EE UK
        ISPConfig ee = new ISPConfig.Builder()
            .setName("EE (UK)")
            .setCountry("UK")
            .setMccMnc("234")
            .setOperatorCode("EE")
            .setAPN("everywhere", "", "80")
            .setDNS("87.194.0.51", "87.194.0.52")
            .addHeader("Host", "www.ee.co.uk")
            .setHostHeader("www.ee.co.uk")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.ee.co.uk")
            .setSNIHost("www.ee.co.uk")
            .setTunnel("SSL", 443)
            .setSpeed(true, 1000)
            .enableAllBypassMethods()
            .build();
        ispList.add(ee);
        ispMap.put("EE", ee);
        
        // VODAFONE UK
        ISPConfig vodafoneUk = new ISPConfig.Builder()
            .setName("Vodafone (UK)")
            .setCountry("UK")
            .setMccMnc("234")
            .setOperatorCode("VODAFONE_UK")
            .setAPN("wap.vodafone.co.uk", "", "80")
            .setDNS("10.206.133.11", "10.206.133.12")
            .addHeader("Host", "www.vodafone.co.uk")
            .setHostHeader("www.vodafone.co.uk")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.vodafone.co.uk")
            .setSNIHost("www.vodafone.co.uk")
            .setTunnel("SSL", 443)
            .setSpeed(true, 900)
            .enableAllBypassMethods()
            .build();
        ispList.add(vodafoneUk);
        ispMap.put("Vodafone UK", vodafoneUk);
        
        // ==================== PAKISTAN ISPs ====================
        
        // JAZZ PAKISTAN
        ISPConfig jazz = new ISPConfig.Builder()
            .setName("Jazz (Pakistan)")
            .setCountry("Pakistan")
            .setMccMnc("410")
            .setOperatorCode("JAZZ")
            .setAPN("jazzconnect.mobilinkworld.com", "", "80")
            .setDNS("119.160.1.1", "119.160.1.2")
            .addHeader("Host", "www.jazz.com.pk")
            .setHostHeader("www.jazz.com.pk")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.jazz.com.pk")
            .setSNIHost("www.jazz.com.pk")
            .setTunnel("SSL", 443)
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build();
        ispList.add(jazz);
        ispMap.put("Jazz", jazz);
        ispMap.put("JAZZ", jazz);
        
        // ZONG PAKISTAN
        ISPConfig zong = new ISPConfig.Builder()
            .setName("Zong (Pakistan)")
            .setCountry("Pakistan")
            .setMccMnc("410")
            .setOperatorCode("ZONG")
            .setAPN("zonginternet", "", "80")
            .setDNS("202.125.140.2", "202.125.140.3")
            .addHeader("Host", "www.zong.com.pk")
            .setHostHeader("www.zong.com.pk")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.zong.com.pk")
            .setSNIHost("www.zong.com.pk")
            .setTunnel("SSL", 443)
            .setSpeed(true, 500)
            .enableAllBypassMethods()
            .build();
        ispList.add(zong);
        ispMap.put("Zong", zong);
        ispMap.put("ZONG", zong);
        
        // ==================== BANGLADESH ISPs ====================
        
        // GRAMEENPHONE BANGLADESH
        ISPConfig gp = new ISPConfig.Builder()
            .setName("Grameenphone (Bangladesh)")
            .setCountry("Bangladesh")
            .setMccMnc("470")
            .setOperatorCode("GP")
            .setAPN("gpinternet", "", "80")
            .setDNS("202.56.4.120", "202.56.4.121")
            .addHeader("Host", "www.grameenphone.com")
            .setHostHeader("www.grameenphone.com")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.grameenphone.com")
            .setSNIHost("www.grameenphone.com")
            .setTunnel("SSL", 443)
            .setSpeed(true, 400)
            .enableAllBypassMethods()
            .build();
        ispList.add(gp);
        ispMap.put("Grameenphone", gp);
        ispMap.put("GP", gp);
        
        // BANGLALINK BANGLADESH
        ISPConfig banglalink = new ISPConfig.Builder()
            .setName("Banglalink (Bangladesh)")
            .setCountry("Bangladesh")
            .setMccMnc("470")
            .setOperatorCode("BANGLALINK")
            .setAPN("blweb", "", "80")
            .setDNS("10.10.10.10", "10.10.10.11")
            .addHeader("Host", "www.banglalink.net")
            .setHostHeader("www.banglalink.net")
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .setBugHost("www.banglalink.net")
            .setSNIHost("www.banglalink.net")
            .setTunnel("SSL", 443)
            .setSpeed(true, 400)
            .enableAllBypassMethods()
            .build();
        ispList.add(banglalink);
        ispMap.put("Banglalink", banglalink);
    }
    
    /**
     * Detect ISP from operator name
     */
    public static ISPConfig detectISP(String operatorName, String mccMnc) {
        if (operatorName == null || operatorName.isEmpty()) {
            return getDefaultISP();
        }
        
        // Try exact match
        ISPConfig config = ispMap.get(operatorName);
        if (config != null) {
            return config;
        }
        
        // Try case-insensitive match
        for (Map.Entry<String, ISPConfig> entry : ispMap.entrySet()) {
            if (operatorName.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        
        // Try MCC/MNC match
        if (mccMnc != null && !mccMnc.isEmpty()) {
            for (ISPConfig isp : ispList) {
                if (isp.getMccMnc() != null && mccMnc.startsWith(isp.getMccMnc())) {
                    return isp;
                }
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
     * Get default ISP (Jio as default for India)
     */
    public static ISPConfig getDefaultISP() {
        return ispList.get(0); // Returns Jio by default
    }
    
    /**
     * Get all ISPs
     */
    public static List<ISPConfig> getAllISPs() {
        return new ArrayList<>(ispList);
    }
    
    /**
     * Get ISPs by country
     */
    public static List<ISPConfig> getISPsByCountry(String country) {
        List<ISPConfig> result = new ArrayList<>();
        for (ISPConfig isp : ispList) {
            if (isp.getCountry().equalsIgnoreCase(country)) {
                result.add(isp);
            }
        }
        return result;
    }
}
