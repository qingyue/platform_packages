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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
/* ATH_WAPI +++ */
import android.net.wifi.WifiConfiguration.Protocol;
/* ATH_WAPI --- */
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

class WifiDialog extends AlertDialog implements View.OnClickListener,
        TextWatcher, AdapterView.OnItemSelectedListener {
    private static final String KEYSTORE_SPACE = "keystore://";

    static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    static final int BUTTON_FORGET = DialogInterface.BUTTON_NEUTRAL;

    final boolean edit;
    private final DialogInterface.OnClickListener mListener;
    private final AccessPoint mAccessPoint;

    private View mView;
    private TextView mSsid;
    private int mSecurity;
    private TextView mPassword;

    private Spinner mEapMethod;
    private Spinner mEapCaCert;
    private Spinner mPhase2;
    private Spinner mEapUserCert;
    /* ATH_WAPI +++ */
    private Spinner mSpinSecurity;
    /* ATH_WAPI --- */
    private TextView mEapIdentity;
    private TextView mEapAnonymous;

    static boolean requireKeyStore(WifiConfiguration config) {
        String values[] = {config.ca_cert.value(), config.client_cert.value(),
                config.private_key.value()};
        for (String value : values) {
            if (value != null && value.startsWith(KEYSTORE_SPACE)) {
                return true;
            }
        }
        return false;
    }

    WifiDialog(Context context, DialogInterface.OnClickListener listener,
            AccessPoint accessPoint, boolean edit) {
        super(context);
        this.edit = edit;
        mListener = listener;
        mAccessPoint = accessPoint;
        mSecurity = (accessPoint == null) ? AccessPoint.SECURITY_NONE : accessPoint.security;
    }

    WifiConfiguration getConfig() {
        if (mAccessPoint != null && mAccessPoint.networkId != -1 && !edit) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();

        if (mAccessPoint == null) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mSsid.getText().toString());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else if (mAccessPoint.networkId == -1) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mAccessPoint.ssid);
        } else {
            config.networkId = mAccessPoint.networkId;
        }

        switch (mSecurity) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPassword.length() != 0) {
                    int length = mPassword.length();
                    String password = mPassword.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                return config;

            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                return config;
            /* ATH_WAPI +++ */
            case AccessPoint.SECURITY_WAPI_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WAPI_PSK);
                config.allowedProtocols.set(Protocol.WAPI);
                if (mPassword.length() != 0) {
                    CheckBox hexCheckBox = (CheckBox)mView.findViewById(R.id.hexadecimal_password);
                    String password = mPassword.getText().toString();
                    if (hexCheckBox.isChecked()) {
                        password = password.trim();
                        if (password.length() > 64) {
                            password = password.substring(0, 64);
                        }

			String psk = new String();
			for (int i = 0; i < password.length(); ++i ) {
			    char c = password.charAt(i);
			    psk += Integer.toHexString((int)c);
			}
			int cnt = 64 - psk.length();
			while (cnt-- > 0) {
			    psk += "0";
			}

                        config.preSharedKey = psk;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                return config;
            case AccessPoint.SECURITY_WAPI_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WAPI_CERT);
                config.allowedProtocols.set(Protocol.WAPI);
                /*
                config.eap.setValue((String) mEapMethod.getSelectedItem());

                config.phase2.setValue((mPhase2.getSelectedItemPosition() == 0) ? "" :
                        "auth=" + mPhase2.getSelectedItem());
                config.private_key.setValue((mEapUserCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.USER_PRIVATE_KEY +
                        (String) mEapUserCert.getSelectedItem());
                config.identity.setValue((mEapIdentity.length() == 0) ? "" :
                        mEapIdentity.getText().toString());
                config.anonymous_identity.setValue((mEapAnonymous.length() == 0) ? "" :
                        mEapAnonymous.getText().toString());
                if (mPassword.length() != 0) {
                    config.password.setValue(mPassword.getText().toString());
                }
                */
                config.ca_cert.setValue((mEapCaCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                        (String) mEapCaCert.getSelectedItem());
                config.client_cert.setValue((mEapUserCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                        (String) mEapUserCert.getSelectedItem());
                 return config;
            /* ATH_WAPI --- */
            case AccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                config.eap.setValue((String) mEapMethod.getSelectedItem());

                config.phase2.setValue((mPhase2.getSelectedItemPosition() == 0) ? "" :
                        "auth=" + mPhase2.getSelectedItem());
                config.ca_cert.setValue((mEapCaCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
                        (String) mEapCaCert.getSelectedItem());
                config.client_cert.setValue((mEapUserCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                        (String) mEapUserCert.getSelectedItem());
                config.private_key.setValue((mEapUserCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.USER_PRIVATE_KEY +
                        (String) mEapUserCert.getSelectedItem());
                config.identity.setValue((mEapIdentity.length() == 0) ? "" :
                        mEapIdentity.getText().toString());
                config.anonymous_identity.setValue((mEapAnonymous.length() == 0) ? "" :
                        mEapAnonymous.getText().toString());
                if (mPassword.length() != 0) {
                    config.password.setValue(mPassword.getText().toString());
                }
                return config;
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.wifi_dialog, null);
        setView(mView);
        setInverseBackgroundForced(true);

        Context context = getContext();
        Resources resources = context.getResources();

        /* ATH_WAPI +++ */
        boolean wapi_supported = AccessPoint.getSysWapiSupported();
        if (wapi_supported) {
            String[] type = resources.getStringArray(R.array.wifi_wapi_security); 
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, type);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mSpinSecurity = (Spinner) mView.findViewById(R.id.security);
            mSpinSecurity.setAdapter(adapter);
        }
        /* ATH_WAPI --- */
        // Atheros +++
	boolean wifiEng = AccessPoint.isWifiEngEnabled();
        // Atheros ---

        if (mAccessPoint == null) {
            setTitle(R.string.wifi_add_network);
            mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
            mSsid = (TextView) mView.findViewById(R.id.ssid);
            mSsid.addTextChangedListener(this);
            ((Spinner) mView.findViewById(R.id.security)).setOnItemSelectedListener(this);
            setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
        } else {
            setTitle(mAccessPoint.ssid);
            ViewGroup group = (ViewGroup) mView.findViewById(R.id.info);

	// Atheros +++
            WifiInfo info = mAccessPoint.getInfo();
            if (wifiEng && mAccessPoint.bssid != null && mAccessPoint.freq != null) {
                    addRow(group, R.string.wifi_bssid, mAccessPoint.bssid);
                    addRow(group, R.string.wifi_channel, mAccessPoint.freq);
            }
	// Atheros ---

            DetailedState state = mAccessPoint.getState();
            if (state != null) {
                addRow(group, R.string.wifi_status, Summary.get(getContext(), state));
            }

            String[] type = resources.getStringArray(R.array.wifi_security);
            /* ATH_WAPI +++ */
            if (wapi_supported) {
                type = resources.getStringArray(R.array.wifi_wapi_security);
            }
            /* ATH_WAPI --- */
            addRow(group, R.string.wifi_security, type[mAccessPoint.security]);

            int level = mAccessPoint.getLevel();
            if (level != -1) {
                String[] signal = resources.getStringArray(R.array.wifi_signal);
// Atheros +++
		if (wifiEng) {
		    addRow(group, R.string.wifi_signal, signal[level]+"("+mAccessPoint.getRawRssi()+")");
	        } else
	            addRow(group, R.string.wifi_signal, signal[level]);
// Atheros ---
            }

	// Atheros +++
//            WifiInfo info = mAccessPoint.getInfo();
	// Atheros +++
            if (info != null) {
                addRow(group, R.string.wifi_speed, info.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
                // TODO: fix the ip address for IPv6.
                int address = info.getIpAddress();
                if (address != 0) {
                    addRow(group, R.string.wifi_ip_address, Formatter.formatIpAddress(address));
                }
            }

            if (mAccessPoint.networkId == -1 || edit) {
                showSecurityFields();
            }

            if (edit) {
                setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
            } else {
                if (state == null && level != -1) {
                    setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_connect), mListener);
                }
                if (mAccessPoint.networkId != -1) {
                    setButton(BUTTON_FORGET, context.getString(R.string.wifi_forget), mListener);
                }
            }
        }

        setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getString(R.string.wifi_cancel), mListener);

        super.onCreate(savedInstanceState);

        if (getButton(BUTTON_SUBMIT) != null) {
            validate();
        }
    }

    private void addRow(ViewGroup group, int name, String value) {
        View row = getLayoutInflater().inflate(R.layout.wifi_dialog_row, group, false);
        ((TextView) row.findViewById(R.id.name)).setText(name);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        group.addView(row);
    }
// Atheros +++
    private boolean validate_eap() {

        if ( (mEapCaCert.getSelectedItemPosition() == 0 ||
	      mEapUserCert.getSelectedItemPosition() == 0)) {
		if (mEapIdentity.length() > 0 &&  mPassword.length() > 0)
			return true;
	} else if (mEapCaCert.getSelectedItemPosition() > 0 &&
			mEapUserCert.getSelectedItemPosition() >= 0) {
		return true;
	} else
		return false;

	return false;
    }
// Atheros ---
    private void validate() {
        // TODO: make sure this is complete.
        if ((mSsid != null && mSsid.length() == 0) ||
                ((mAccessPoint == null || mAccessPoint.networkId == -1) &&
                ((mSecurity == AccessPoint.SECURITY_WEP && mPassword.length() == 0) ||
                (mSecurity == AccessPoint.SECURITY_PSK && mPassword.length() < 8) ||
	// Atheros +++
		(mSecurity == AccessPoint.SECURITY_EAP && validate_eap() != true)
	// Atheros ---
        /* ATH_WAPI +++ */
                || (mSecurity == AccessPoint.SECURITY_WAPI_PSK && mPassword.length() < 8)
                || (mSecurity == AccessPoint.SECURITY_WAPI_EAP &&
                    ((mEapCaCert.getSelectedItemPosition() == 0) ||
                    (mEapUserCert.getSelectedItemPosition() == 0)))
        /* ATH_WAPI --- */
                 ))) {
            getButton(BUTTON_SUBMIT).setEnabled(false);
        } else {
            getButton(BUTTON_SUBMIT).setEnabled(true);
        }
    }

    public void onClick(View view) {
        mPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    
    public void afterTextChanged(Editable editable) {
        if (getButton(BUTTON_SUBMIT) != null) {
            validate();
        }
    }

    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        mSecurity = position;
        showSecurityFields();
        validate();
    }
//Atheros +++
    private AdapterView.OnItemSelectedListener eapListener = new AdapterView.OnItemSelectedListener() {
	public void onItemSelected(AdapterView parent, View view, int position, long id) {
	    validate();
	}
	public void onNothingSelected(AdapterView parent) {
	}
    };
//Atheros ---
    public void onNothingSelected(AdapterView parent) {
    }

    private void showSecurityFields() {
        if (mSecurity == AccessPoint.SECURITY_NONE) {
            mView.findViewById(R.id.fields).setVisibility(View.GONE);
            return;
        }

        /* ATH_WAPI +++ */
        CheckBox hexCheckBox = (CheckBox)mView.findViewById(R.id.hexadecimal_password);
        if (hexCheckBox != null) {
            hexCheckBox.setVisibility(mSecurity==AccessPoint.SECURITY_WAPI_PSK ? View.VISIBLE : View.GONE);
        }
        if (mSecurity == AccessPoint.SECURITY_WAPI_EAP) {
            mView.findViewById(R.id.fields).setVisibility(View.GONE);
            mView.findViewById(R.id.wapi_eap_fields).setVisibility(View.VISIBLE);
            mEapCaCert = (Spinner) mView.findViewById(R.id.wapi_ca_cert);
            mEapUserCert = (Spinner) mView.findViewById(R.id.wapi_user_cert);

            mEapCaCert.setOnItemSelectedListener(eapListener);
            mEapUserCert.setOnItemSelectedListener(eapListener);

            loadCertificates(mEapCaCert, Credentials.USER_CERTIFICATE);
            loadCertificates(mEapUserCert, Credentials.USER_CERTIFICATE);

            if (mAccessPoint != null && mAccessPoint.networkId != -1) {
                WifiConfiguration config = mAccessPoint.getConfig();
                setCertificate(mEapCaCert, Credentials.USER_CERTIFICATE,
                               config.ca_cert.value());
                setCertificate(mEapUserCert, Credentials.USER_CERTIFICATE,
                               config.client_cert.value());
            }
            return;
        } else {
            mView.findViewById(R.id.wapi_eap_fields).setVisibility(View.GONE);
        }
        /* ATH_WAPI --- */

        mView.findViewById(R.id.fields).setVisibility(View.VISIBLE);

        if (mPassword == null) {
            mPassword = (TextView) mView.findViewById(R.id.password);
            mPassword.addTextChangedListener(this);
            ((CheckBox) mView.findViewById(R.id.show_password)).setOnClickListener(this);

            if (mAccessPoint != null && mAccessPoint.networkId != -1) {
                mPassword.setHint(R.string.wifi_unchanged);
            }
        }

        if (mSecurity != AccessPoint.SECURITY_EAP) {
            mView.findViewById(R.id.eap).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.eap).setVisibility(View.VISIBLE);

        if (mEapMethod == null) {
            mEapMethod = (Spinner) mView.findViewById(R.id.method);
            mPhase2 = (Spinner) mView.findViewById(R.id.phase2);
            mEapCaCert = (Spinner) mView.findViewById(R.id.ca_cert);
            mEapUserCert = (Spinner) mView.findViewById(R.id.user_cert);
            mEapIdentity = (TextView) mView.findViewById(R.id.identity);
            mEapAnonymous = (TextView) mView.findViewById(R.id.anonymous);
// Atheros +++
	    mEapIdentity.addTextChangedListener(this);
	    mEapCaCert.setOnItemSelectedListener(eapListener);
	    mEapUserCert.setOnItemSelectedListener(eapListener);
// Atheros ---
            loadCertificates(mEapCaCert, Credentials.CA_CERTIFICATE);
            loadCertificates(mEapUserCert, Credentials.USER_PRIVATE_KEY);

            if (mAccessPoint != null && mAccessPoint.networkId != -1) {
                WifiConfiguration config = mAccessPoint.getConfig();
                setSelection(mEapMethod, config.eap.value());
                setSelection(mPhase2, config.phase2.value());
                setCertificate(mEapCaCert, Credentials.CA_CERTIFICATE,
                        config.ca_cert.value());
                setCertificate(mEapUserCert, Credentials.USER_PRIVATE_KEY,
                        config.private_key.value());
                mEapIdentity.setText(config.identity.value());
                mEapAnonymous.setText(config.anonymous_identity.value());
            }
        }
    }

    private void loadCertificates(Spinner spinner, String prefix) {
        String[] certs = KeyStore.getInstance().saw(prefix);
        Context context = getContext();
        String unspecified = context.getString(R.string.wifi_unspecified);

        if (certs == null || certs.length == 0) {
            certs = new String[] {unspecified};
        } else {
            String[] array = new String[certs.length + 1];
            array[0] = unspecified;
            System.arraycopy(certs, 0, array, 1, certs.length);
            certs = array;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, certs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setCertificate(Spinner spinner, String prefix, String cert) {
        prefix = KEYSTORE_SPACE + prefix;
        if (cert != null && cert.startsWith(prefix)) {
            setSelection(spinner, cert.substring(prefix.length()));
        }
    }

    private void setSelection(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; --i) {
                if (value.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }
}
