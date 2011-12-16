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

package com.android.settings;

import com.android.settings.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.EthernetManager;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;
import android.util.Log;

public class EthernetEnabler implements Preference.OnPreferenceChangeListener {
    private final Context mContext; 
    private final CheckBoxPreference mCheckBox;
    private final CharSequence mOriginalSummary;

    private final EthernetManager mEthernetManager;
    private final IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (EthernetManager.ETHERNET_STATE_CHANGED_ACTION.equals(action)) {
                handleEthernetStateChanged(intent.getIntExtra(
                        EthernetManager.EXTRA_ETHERNET_STATE, EthernetManager.ETHERNET_STATE_UNKNOWN));
            } else if (EthernetManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                handleStateChanged(((NetworkInfo) intent.getParcelableExtra(
                        EthernetManager.EXTRA_NETWORK_INFO)).getDetailedState());
            }
        }
    };

    public EthernetEnabler(Context context, CheckBoxPreference checkBox) {
        mContext = context;
        mCheckBox = checkBox;
        mOriginalSummary = checkBox.getSummary();
        checkBox.setPersistent(false);

        mEthernetManager = (EthernetManager) context.getSystemService(Context.ETHERNET_SERVICE);
        mIntentFilter = new IntentFilter(EthernetManager.ETHERNET_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(EthernetManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public void resume() {
        // Wi-Fi state is sticky, so just let the receiver update UI
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mCheckBox.setOnPreferenceChangeListener(this);
    }
    
    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        mCheckBox.setOnPreferenceChangeListener(null);
    }
    
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean enable = (Boolean) value;

        if (mEthernetManager.setEthernetEnabled(enable)) {
            mCheckBox.setEnabled(false);
        } else {
            mCheckBox.setSummary(R.string.ethernet_error);
        }

        // Don't update UI to opposite state until we're sure
        return false;
    }
    
    private void handleEthernetStateChanged(int state) {
        switch (state) {
            case EthernetManager.ETHERNET_STATE_ENABLING:
                mCheckBox.setSummary(R.string.ethernet_starting);
                mCheckBox.setEnabled(false);
                break;
            case EthernetManager.ETHERNET_STATE_ENABLED:
                mCheckBox.setChecked(true);
                mCheckBox.setSummary(null);
                mCheckBox.setEnabled(true);
                break;
            case EthernetManager.ETHERNET_STATE_DISABLING:
                mCheckBox.setSummary(R.string.ethernet_stopping);
                mCheckBox.setEnabled(false);
                break;
            case EthernetManager.ETHERNET_STATE_DISABLED:
                mCheckBox.setChecked(false);
                mCheckBox.setSummary(mOriginalSummary);
                mCheckBox.setEnabled(true);
                break;
            default:
                mCheckBox.setChecked(false);
                mCheckBox.setSummary(R.string.ethernet_error);
                mCheckBox.setEnabled(true);
        }
    }

    private void handleStateChanged(NetworkInfo.DetailedState state) {
        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the check box as an optimization.
        if (state != null && mCheckBox.isChecked()) {
        
            String[] formats = mContext.getResources().getStringArray(R.array.wifi_status );
            String   summary;
            int index = state.ordinal();

            if (index >= formats.length || formats[index].length() == 0) {
                summary = null;
            }else
            {
                summary = String.format(formats[index]);
            } 
            
            mCheckBox.setSummary(summary);
        }
    }
}
