package com.t3rmuxk1ng.unlimiteddatabypass.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * NETWORK HELPER
 * Utility class for network operations
 */
public class NetworkHelper {

    private static final String TAG = "NetworkHelper";

    private Context context;
    private ConnectivityManager connectivityManager;
    private TelephonyManager telephonyManager;
    private WifiManager wifiManager;

    public NetworkHelper(Context context) {
        this.context = context;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Check if network is available
     */
    public boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
    }

    /**
     * Get network type
     */
    public String getNetworkType() {
        if (connectivityManager == null) return "Unknown";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return "None";

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) return "Unknown";

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return "WiFi";
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return getCellularNetworkType();
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return "Ethernet";
            }
        }

        return "Unknown";
    }

    /**
     * Get cellular network type (2G/3G/4G/5G)
     */
    public String getCellularNetworkType() {
        if (telephonyManager == null) return "Unknown";

        int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                networkType = telephonyManager.getDataNetworkType();
            } else {
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.getSubtype() > 0) {
                    networkType = info.getSubtype();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting network type: " + e.getMessage());
        }

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";

            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";

            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G LTE";

            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";

            default:
                return "Unknown";
        }
    }

    /**
     * Get carrier name
     */
    public String getCarrierName() {
        if (telephonyManager == null) return "Unknown";

        try {
            return telephonyManager.getNetworkOperatorName();
        } catch (Exception e) {
            Log.e(TAG, "Error getting carrier name: " + e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Get SIM operator
     */
    public String getSimOperator() {
        if (telephonyManager == null) return "";

        try {
            return telephonyManager.getSimOperator();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get network operator
     */
    public String getNetworkOperator() {
        if (telephonyManager == null) return "";

        try {
            return telephonyManager.getNetworkOperator();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if roaming
     */
    public boolean isRoaming() {
        if (telephonyManager == null) return false;

        try {
            return telephonyManager.isNetworkRoaming();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get WiFi signal strength
     */
    public int getWifiSignalStrength() {
        if (wifiManager == null) return 0;

        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get WiFi SSID
     */
    public String getWifiSSID() {
        if (wifiManager == null) return "";

        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            return ssid;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get network speed in Mbps
     */
    public double[] getNetworkSpeed() {
        if (connectivityManager == null) return new double[]{0, 0};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return new double[]{0, 0};

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) return new double[]{0, 0};

            // Get in Kbps, convert to Mbps
            double downSpeed = capabilities.getLinkDownstreamBandwidthKbps() / 1000.0;
            double upSpeed = capabilities.getLinkUpstreamBandwidthKbps() / 1000.0;

            return new double[]{downSpeed, upSpeed};
        }

        return new double[]{0, 0};
    }

    /**
     * Check if 5G network
     */
    public boolean is5GNetwork() {
        return "5G".equals(getCellularNetworkType());
    }

    /**
     * Check if WiFi connected
     */
    public boolean isWifiConnected() {
        return "WiFi".equals(getNetworkType());
    }

    /**
     * Check if mobile data connected
     */
    public boolean isMobileDataConnected() {
        String type = getNetworkType();
        return !"WiFi".equals(type) && !"Ethernet".equals(type) && !"None".equals(type) && !"Unknown".equals(type);
    }
}
