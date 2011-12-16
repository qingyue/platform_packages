/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Copyright (C) 2011 Freescale Semiconductor,Inc. */

package com.android.settings.wifi;

import com.android.settings.R;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

/* ATH_WAPI +++ */
import android.os.SystemProperties;
/* ATH_WAPI ---*/

class AccessPoint extends Preference {
    private static final int[] STATE_SECURED = {R.attr.state_encrypted};
    private static final int[] STATE_NONE = {};

    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;
    /* ATH_WAPI +++ */
    static final int SECURITY_WAPI_PSK = SECURITY_EAP+1;
    static final int SECURITY_WAPI_EAP = SECURITY_EAP+2;
    /* ATH_WAPI ---*/

    final String ssid;
    final int security;
    final int networkId;
// Atheros +++
    String bssid = null;
    String freq = null;
// Atheros ---

    private WifiConfiguration mConfig;
    private int mRssi;
    private WifiInfo mInfo;
    private DetailedState mState;
    private ImageView mSignal;

    /* ATH_WAPI +++ */
    public static boolean getSysWapiSupported() {
	return SystemProperties.get("wifi.wapi.supported", "false").equals("true");
    }
    /* ATH_WAPI --- */

// Atheros +++
    public static boolean isWifiEngEnabled() {
	return SystemProperties.get("wifi.eng.enabled", "false").equals("true");
    }
// Atheros +++

    static int getSecurity(WifiConfiguration config) {
	if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
	    return SECURITY_PSK;
	}
	if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
		config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
	    return SECURITY_EAP;
	}
	/* ATH_WAPI +++ */
	if (getSysWapiSupported() && config.allowedKeyManagement.get(KeyMgmt.WAPI_PSK)) {
	    return SECURITY_WAPI_PSK;
	}
	if (getSysWapiSupported() && config.allowedKeyManagement.get(KeyMgmt.WAPI_CERT)) {
	    return SECURITY_WAPI_EAP;
	}
	/* ATH_WAPI --- */
	return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    private static int getSecurity(ScanResult result) {
	if (result.capabilities.contains("WEP")) {
	    return SECURITY_WEP;
	/* ATH_WAPI +++ */
	} else if (getSysWapiSupported() && result.capabilities.contains("WAPI-PSK")) {
	    return SECURITY_WAPI_PSK;
	} else if (getSysWapiSupported() && result.capabilities.contains("WAPI-CERT")) {
	    return SECURITY_WAPI_EAP;
	/* ATH_WAPI --- */
	} else if (result.capabilities.contains("PSK")) {
	    return SECURITY_PSK;
	} else if (result.capabilities.contains("EAP")) {
	    return SECURITY_EAP;
	}
	return SECURITY_NONE;
    }

    AccessPoint(Context context, WifiConfiguration config) {
	super(context);
	setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
	ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
	security = getSecurity(config);
	networkId = config.networkId;
	mConfig = config;
	mRssi = Integer.MAX_VALUE;
// Atheros +++
	bssid = config.BSSID;
// Atheros ---
    }
// Atheros +++
    private int freq2chan(int freq) {
	if (freq >= 2412 && freq <= 2472)
		return (freq - 2407)/5;
	else if (freq == 2484)
		return 14;
	else
		return 0;
    }
// Atheros ---

    AccessPoint(Context context, ScanResult result) {
	super(context);
	setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
	ssid = result.SSID;
	security = getSecurity(result);
	networkId = -1;
	mRssi = result.level;
// Atheros +++
	bssid = result.BSSID;
	freq = freq2chan(result.frequency)+" ("+result.frequency+"Mhz)";
// Atheros ---
    }

    @Override
    protected void onBindView(View view) {
	setTitle(ssid);
	mSignal = (ImageView) view.findViewById(R.id.signal);
	if (mRssi == Integer.MAX_VALUE) {
	    mSignal.setImageDrawable(null);
	} else {
	    mSignal.setImageResource(R.drawable.wifi_signal);
	    mSignal.setImageState((security != SECURITY_NONE) ?
		    STATE_SECURED : STATE_NONE, true);
	}
	refresh();
	super.onBindView(view);
    }

    @Override
    public int compareTo(Preference preference) {
	if (!(preference instanceof AccessPoint)) {
	    return 1;
	}
	AccessPoint other = (AccessPoint) preference;
	// Active one goes first.
	if (mInfo != other.mInfo) {
	    return (mInfo != null) ? -1 : 1;
	}
	// Reachable one goes before unreachable one.
	if ((mRssi ^ other.mRssi) < 0) {
	    return (mRssi != Integer.MAX_VALUE) ? -1 : 1;
	}
	// Configured one goes before unconfigured one.
	if ((networkId ^ other.networkId) < 0) {
	    return (networkId != -1) ? -1 : 1;
	}
	// Sort by signal strength.
	int difference = WifiManager.compareSignalLevel(other.mRssi, mRssi);
	if (difference != 0) {
	    return difference;
	}
	// Sort by ssid.
	return ssid.compareToIgnoreCase(other.ssid);
    }

    boolean update(ScanResult result) {
	// We do not call refresh() since this is called before onBindView().
	if (ssid.equals(result.SSID) && security == getSecurity(result)) {
	// Atheros +++
	    bssid = result.BSSID;
	    freq = freq2chan(result.frequency)+" ("+result.frequency+"Mhz)";
	// Atheros ---
	    if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
		mRssi = result.level;
	    }
	    return true;
	}
	return false;
    }

    void update(WifiInfo info, DetailedState state) {
	boolean reorder = false;
	if (info != null && networkId != -1 && networkId == info.getNetworkId()) {
	    reorder = (mInfo == null);
	    mRssi = info.getRssi();
	    mInfo = info;
	    mState = state;
	    refresh();
	} else if (mInfo != null) {
	    reorder = true;
	    mInfo = null;
	    mState = null;
	    refresh();
	}
	if (reorder) {
	    notifyHierarchyChanged();
	}
    }

    int getLevel() {
	if (mRssi == Integer.MAX_VALUE) {
	    return -1;
	}
	return WifiManager.calculateSignalLevel(mRssi, 4);
    }

/* Atheros +++ */
    String getRawRssi() {
        if (mRssi == Integer.MAX_VALUE) {
            return null;
        }
        return Integer.toString(mRssi);
    }
/* Atheros --- */
    WifiConfiguration getConfig() {
	return mConfig;
    }

    WifiInfo getInfo() {
	return mInfo;
    }

    DetailedState getState() {
	return mState;
    }

    static String removeDoubleQuotes(String string) {
	int length = string.length();
	if ((length > 1) && (string.charAt(0) == '"')
		&& (string.charAt(length - 1) == '"')) {
	    return string.substring(1, length - 1);
	}
	return string;
    }

    static String convertToQuotedString(String string) {
	return "\"" + string + "\"";
    }

    private void refresh() {
	if (mSignal == null) {
	    return;
	}
	Context context = getContext();
	mSignal.setImageLevel(getLevel());
// Atheros +++
        String rssi = getRawRssi();
        boolean displayRssi = isWifiEngEnabled() && (rssi != null);
// Atheros ---
        if (mState != null) {
// Atheros +++
            if (displayRssi)
                 setSummary(Summary.get(context, mState)+" "+rssi);
            else
// Atheros ---
            setSummary(Summary.get(context, mState));
        } else {
            String status = null;
            if (mRssi == Integer.MAX_VALUE) {
                status = context.getString(R.string.wifi_not_in_range);
            } else if (mConfig != null) {
                status = context.getString((mConfig.status == WifiConfiguration.Status.DISABLED) ?
                        R.string.wifi_disabled : R.string.wifi_remembered);
            }

            if (security == SECURITY_NONE) {
// Atheros +++
                if (displayRssi)
                    setSummary(rssi);
                else
// Atheros ---
                setSummary(status);
            } else {
                String format = context.getString((status == null) ?
                        R.string.wifi_secured : R.string.wifi_secured_with_status);
                String[] type = context.getResources().getStringArray(R.array.wifi_security);
                /* ATH_WAPI +++ */
                if (getSysWapiSupported()) {
                    type = context.getResources().getStringArray(R.array.wifi_wapi_security);
                }
                /* ATH_WAPI --- */
// Atheros +++
                if (displayRssi)
                    setSummary(String.format(format, type[security], status)+" "+rssi);
                else
// Atheros ---
                setSummary(String.format(format, type[security], status));
            }
	}
    }
}
