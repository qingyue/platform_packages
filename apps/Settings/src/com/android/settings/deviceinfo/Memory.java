/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Environment;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;
import android.net.Uri;


import com.android.settings.R;

import java.io.File;
import java.util.List;

public class Memory extends PreferenceActivity implements OnCancelListener {
    private static final String TAG = "Memory";
    private static final boolean localLOGV = false;

    private static final String MEMORY_SD_SIZE = "memory_sd_size";

    private static final String MEMORY_SD_AVAIL = "memory_sd_avail";

    private static final String MEMORY_SD_MOUNT_TOGGLE = "memory_sd_mount_toggle";

    private static final String MEMORY_SD_FORMAT = "memory_sd_format";

    private static final String MEMORY_SD_GROUP = "memory_sd";

    private static final String MEMORY_EXTSD_SIZE = "memory_extsd_size";

    private static final String MEMORY_EXTSD_AVAIL = "memory_extsd_avail";

    private static final String MEMORY_EXTSD_MOUNT_TOGGLE = "memory_extsd_mount_toggle";

    private static final String MEMORY_EXTSD_FORMAT = "memory_extsd_format";

    private static final String MEMORY_EXTSD_GROUP = "memory_extsd";

    private static final String MEMORY_UDISK_SIZE = "memory_udisk_size";

    private static final String MEMORY_UDISK_AVAIL = "memory_udisk_avail";

    private static final String MEMORY_UDISK_MOUNT_TOGGLE = "memory_udisk_mount_toggle";

    private static final String MEMORY_UDISK_FORMAT = "memory_udisk_format";

    private static final String MEMORY_UDISK_GROUP = "memory_udisk";

    private static final int DLG_CONFIRM_UNMOUNT = 1;
    private static final int DLG_ERROR_UNMOUNT = 2;

    private Resources mRes;

    private Preference mSdSize;
    private Preference mSdAvail;
    private Preference mSdMountToggle;
    private Preference mSdFormat;
    private PreferenceGroup mSdMountPreferenceGroup;
    boolean mSdMountToggleAdded = true;

    private Preference mExtSdSize;
    private Preference mExtSdAvail;
    private Preference mExtSdMountToggle;
    private Preference mExtSdFormat;
    private PreferenceGroup mExtSdMountPreferenceGroup;
    boolean mExtSdMountToggleAdded = true;

    private Preference mUdiskSize;
    private Preference mUdiskAvail;
    private Preference mUdiskMountToggle;
    private Preference mUdiskFormat;
    private PreferenceGroup mUdiskMountPreferenceGroup;
    boolean mUdiskMountToggleAdded = true;

    // Access using getMountService()
    private IMountService mMountService = null;

    private StorageManager mStorageManager = null;

    public static String strSelect = null;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
        }

        addPreferencesFromResource(R.xml.device_info_memory);
        
        mRes = getResources();
        mSdSize = findPreference(MEMORY_SD_SIZE);
        mSdAvail = findPreference(MEMORY_SD_AVAIL);
        mSdMountToggle = findPreference(MEMORY_SD_MOUNT_TOGGLE);
        mSdFormat = findPreference(MEMORY_SD_FORMAT);
        mSdMountPreferenceGroup = (PreferenceGroup)findPreference(MEMORY_SD_GROUP);

        mExtSdSize = findPreference(MEMORY_EXTSD_SIZE);
        mExtSdAvail = findPreference(MEMORY_EXTSD_AVAIL);
        mExtSdMountToggle = findPreference(MEMORY_EXTSD_MOUNT_TOGGLE);
        mExtSdFormat = findPreference(MEMORY_EXTSD_FORMAT);
        mExtSdMountPreferenceGroup = (PreferenceGroup)findPreference(MEMORY_EXTSD_GROUP);

        mUdiskSize = findPreference(MEMORY_UDISK_SIZE);
        mUdiskAvail = findPreference(MEMORY_UDISK_AVAIL);
        mUdiskMountToggle = findPreference(MEMORY_UDISK_MOUNT_TOGGLE);
        mUdiskFormat = findPreference(MEMORY_UDISK_FORMAT);
        mUdiskMountPreferenceGroup = (PreferenceGroup)findPreference(MEMORY_UDISK_GROUP);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);

        updateMemoryStatus();
    }

    StorageEventListener mStorageListener = new StorageEventListener() {

        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(TAG, "Received storage state changed notification that " +
                    path + " changed state from " + oldState +
                    " to " + newState);
            updateMemoryStatus();
        }
    };
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        super.onDestroy();
    }

    private synchronized IMountService getMountService() {
       if (mMountService == null) {
           IBinder service = ServiceManager.getService("mount");
           if (service != null) {
               mMountService = IMountService.Stub.asInterface(service);
           } else {
               Log.e(TAG, "Can't get mount service");
           }
       }
       return mMountService;
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSdMountToggle) {
            strSelect = Environment.getExternalSDStorageDirectory().toString();
            String status = Environment.getExternalSDStorageState();
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                unmount();
            } else {
                mount();
            }
            return true;
        } 
        else if (preference == mSdFormat) {
            strSelect = Environment.getExternalSDStorageDirectory().toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(this, com.android.settings.MediaFormat.class);
            intent.setData(Uri.parse(Environment.getExternalSDStorageDirectory().toString()));
            startActivity(intent);
            return true;
        }
        else if (preference == mExtSdMountToggle) {
            strSelect = Environment.getExternalExtSDStorageDirectory().toString();
            String status = Environment.getExternalExtSDStorageState();
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                unmount();
            } else {
                mount();
            }
            return true;
        } 
        else if (preference == mExtSdFormat) {
            strSelect = Environment.getExternalExtSDStorageDirectory().toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(this, com.android.settings.MediaFormat.class);
            intent.setData(Uri.parse(Environment.getExternalExtSDStorageDirectory().toString()));
            startActivity(intent);
            return true;
        }
        else if (preference == mUdiskMountToggle) {
            Log.i(TAG, "***yyg:umount udisk!");
            strSelect = Environment.getExternalUDiskStorageDirectory().toString();
            String status = Environment.getExternalUDiskStorageState();
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                unmount();
            } else {
                mount();
            }
            return true;
        } 
        else if (preference == mUdiskFormat) {
            strSelect = Environment.getExternalUDiskStorageDirectory().toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(this, com.android.settings.MediaFormat.class);
            intent.setData(Uri.parse(Environment.getExternalUDiskStorageDirectory().toString()));
            startActivity(intent);
            return true;
        }
        
        return false;
    }
     
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateMemoryStatus();
        }
    };

    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case DLG_CONFIRM_UNMOUNT:
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.dlg_confirm_unmount_title)
                    .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            doUnmount(true);
                        }})
                    .setNegativeButton(R.string.cancel, null)
                    .setMessage(R.string.dlg_confirm_unmount_text)
                    .setOnCancelListener(this)
                    .create();
        case DLG_ERROR_UNMOUNT:
            return new AlertDialog.Builder(this                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            )
            .setTitle(R.string.dlg_error_unmount_title)
            .setNeutralButton(R.string.dlg_ok, null)
            .setMessage(R.string.dlg_error_unmount_text)
            .setOnCancelListener(this)
            .create();
        }
        return null;
    }

    private void doUnmount(boolean force) {
        String dir = strSelect;
        Log.i(TAG, "***yyg:doUnmount " + dir);
        // Present a toast here
        Toast.makeText(this, R.string.unmount_inform_text, Toast.LENGTH_SHORT).show();
        IMountService mountService = getMountService();
        try {
            if (dir.equals(Environment.getExternalSDStorageDirectory().toString())) {
                mSdMountToggle.setEnabled(false);
                mSdMountToggle.setTitle(mRes.getString(R.string.sd_ejecting_title));
                mSdMountToggle.setSummary(mRes.getString(R.string.sd_ejecting_summary));
                if (Environment.getExternalSDStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    mountService.unmountVolume(dir, force);
                }
            }
            else if (dir.equals(Environment.getExternalExtSDStorageDirectory().toString())) {
                mExtSdMountToggle.setEnabled(false);
                mExtSdMountToggle.setTitle(mRes.getString(R.string.extsd_ejecting_title));
                mExtSdMountToggle.setSummary(mRes.getString(R.string.extsd_ejecting_summary));
                if (Environment.getExternalExtSDStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    mountService.unmountVolume(dir, force);
                }
            }
            else if (dir.equals(Environment.getExternalUDiskStorageDirectory().toString())) {
                mUdiskMountToggle.setEnabled(false);
                mUdiskMountToggle.setTitle(mRes.getString(R.string.udisk_ejecting_title));
                mUdiskMountToggle.setSummary(mRes.getString(R.string.udisk_ejecting_summary));
                if (Environment.getExternalUDiskStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    mountService.unmountVolume(dir, force);
                }
            }
        } catch (RemoteException e) {
            // Informative dialog to user that
            // unmount failed.
            showDialogInner(DLG_ERROR_UNMOUNT);
        }
    }

    private void showDialogInner(int id) {
        removeDialog(id);
        showDialog(id);
    }

    private boolean hasAppsAccessingStorage() throws RemoteException {
        String path = strSelect;
        IMountService mountService = getMountService();
        int stUsers[] = mountService.getStorageUsers(path);
        if (stUsers != null && stUsers.length > 0) {
            return true;
        }
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ApplicationInfo> list = am.getRunningExternalApplications();
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

    private void unmount() {
        // Check if external media is in use.
        try {
           if (hasAppsAccessingStorage()) {
               if (localLOGV) Log.i(TAG, "Do have storage users accessing media");
               // Present dialog to user
               showDialogInner(DLG_CONFIRM_UNMOUNT);
           } else {
               doUnmount(true);
           }
        } catch (RemoteException e) {
            // Very unlikely. But present an error dialog anyway
            Log.e(TAG, "Is MountService running?");
            showDialogInner(DLG_ERROR_UNMOUNT);
        }
    }

    private void mount() {
        String dir = strSelect;
        IMountService mountService = getMountService();
        try {
            if ((mountService != null) && (dir != null)) {
                mountService.mountVolume(dir);
            } else {
                Log.e(TAG, "Mount service is null, can't mount");
            }
        } catch (RemoteException ex) {
        }
    }

    private void updateMemoryStatus() {
        String status_sd = Environment.getExternalSDStorageState();
        String status_extsd = Environment.getExternalExtSDStorageState();
        String status_udisk = Environment.getExternalUDiskStorageState();
        String readOnly_sd = "";
        String readOnly_extsd = "";
        String readOnly_udisk = "";
        
        if (status_sd.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            status_sd = Environment.MEDIA_MOUNTED;
            readOnly_sd = mRes.getString(R.string.read_only);
        }
        if (status_extsd.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            status_extsd = Environment.MEDIA_MOUNTED;
            readOnly_extsd = mRes.getString(R.string.read_only);
        }
        if (status_udisk.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            status_udisk = Environment.MEDIA_MOUNTED;
            readOnly_udisk = mRes.getString(R.string.read_only);
        }
 
        if (status_sd.equals(Environment.MEDIA_MOUNTED)) {
            if (!Environment.isExternalStorageRemovable()) {
                // This device has built-in storage that is not removable.
                // There is no reason for the user to unmount it.
                if (mSdMountToggleAdded) {
                    mSdMountPreferenceGroup.removePreference(mSdMountToggle);
                    mSdMountToggleAdded = false;
                }
            }
            try {
                File path = Environment.getExternalSDStorageDirectory();
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();
                
                mSdSize.setSummary(formatSize(totalBlocks * blockSize));
                mSdAvail.setSummary(formatSize(availableBlocks * blockSize) + readOnly_sd);

                mSdMountToggle.setEnabled(true);
                mSdMountToggle.setTitle(mRes.getString(R.string.sd_eject));
                mSdMountToggle.setSummary(mRes.getString(R.string.sd_eject_summary));

            } catch (IllegalArgumentException e) {
                // this can occur if the SD card is removed, but we haven't received the 
                // ACTION_MEDIA_REMOVED Intent yet.
                status_sd = Environment.MEDIA_REMOVED;
            }
            
        } else {
            mSdSize.setSummary(mRes.getString(R.string.sd_unavailable));
            mSdAvail.setSummary(mRes.getString(R.string.sd_unavailable));

            if (!Environment.isExternalStorageRemovable()) {
                if (status_sd.equals(Environment.MEDIA_UNMOUNTED)) {
                    if (!mSdMountToggleAdded) {
                        mSdMountPreferenceGroup.addPreference(mSdMountToggle);
                        mSdMountToggleAdded = true;
                    }
                }
            }

            if (status_sd.equals(Environment.MEDIA_UNMOUNTED) ||
                status_sd.equals(Environment.MEDIA_NOFS) ||
                status_sd.equals(Environment.MEDIA_UNMOUNTABLE) ) {
                mSdMountToggle.setEnabled(true);
                mSdMountToggle.setTitle(mRes.getString(R.string.sd_mount));
                mSdMountToggle.setSummary(mRes.getString(R.string.sd_mount_summary));
            } else {
                mSdMountToggle.setEnabled(false);
                mSdMountToggle.setTitle(mRes.getString(R.string.sd_mount));
                mSdMountToggle.setSummary(mRes.getString(R.string.sd_insert_summary));
            }
        }

        if (status_extsd.equals(Environment.MEDIA_MOUNTED)) {
            Log.i(TAG, "Extsd is mounted!");
            if (!Environment.isExternalExtSDStorageRemovable()) {
                // This device has built-in storage that is not removable.
                // There is no reason for the user to unmount it.
                if (mExtSdMountToggleAdded) {
                    mExtSdMountPreferenceGroup.removePreference(mExtSdMountToggle);
                    mExtSdMountToggleAdded = false;
                }
            }
            try {
                File path = Environment.getExternalExtSDStorageDirectory();
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();

                Log.i(TAG, "The block size is " + blockSize);
                
                mExtSdSize.setSummary(formatSize(totalBlocks * blockSize));
                mExtSdAvail.setSummary(formatSize(availableBlocks * blockSize) + readOnly_extsd);

                mExtSdMountToggle.setEnabled(true);
                mExtSdMountToggle.setTitle(mRes.getString(R.string.extsd_eject));
                mExtSdMountToggle.setSummary(mRes.getString(R.string.extsd_eject_summary));

            } catch (IllegalArgumentException e) {
                // this can occur if the EXTSD card is removed, but we haven't received the 
                // ACTION_MEDIA_REMOVED Intent yet.
                status_extsd = Environment.MEDIA_REMOVED;
            }
            
        } else {
            mExtSdSize.setSummary(mRes.getString(R.string.extsd_unavailable));
            mExtSdAvail.setSummary(mRes.getString(R.string.extsd_unavailable));


            if (!Environment.isExternalStorageRemovable()) {
                if (status_extsd.equals(Environment.MEDIA_UNMOUNTED)) {
                    if (!mExtSdMountToggleAdded) {
                        mExtSdMountPreferenceGroup.addPreference(mExtSdMountToggle);
                        mExtSdMountToggleAdded = true;
                    }
                }
            }

            if (status_extsd.equals(Environment.MEDIA_UNMOUNTED) ||
                status_extsd.equals(Environment.MEDIA_NOFS) ||
                status_extsd.equals(Environment.MEDIA_UNMOUNTABLE) ) {
                mExtSdMountToggle.setEnabled(true);
                mExtSdMountToggle.setTitle(mRes.getString(R.string.extsd_mount));
                mExtSdMountToggle.setSummary(mRes.getString(R.string.extsd_mount_summary));
            } else {
                mExtSdMountToggle.setEnabled(false);
                mExtSdMountToggle.setTitle(mRes.getString(R.string.extsd_mount));
                mExtSdMountToggle.setSummary(mRes.getString(R.string.extsd_insert_summary));
            }
        }

        if (status_udisk.equals(Environment.MEDIA_MOUNTED)) {
            Log.i(TAG, "The udisk is mounted!");
            if (!Environment.isExternalUDiskStorageRemovable()) {
                // This device has built-in storage that is not removable.
                // There is no reason for the user to unmount it.
                if (mUdiskMountToggleAdded) {
                    mUdiskMountPreferenceGroup.removePreference(mUdiskMountToggle);
                    mUdiskMountToggleAdded = false;
                }
            }
            try {
                File path = Environment.getExternalUDiskStorageDirectory();
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();
                
                mUdiskSize.setSummary(formatSize(totalBlocks * blockSize));
                mUdiskAvail.setSummary(formatSize(availableBlocks * blockSize) + readOnly_udisk);

                mUdiskMountToggle.setEnabled(true);
                mUdiskMountToggle.setTitle(mRes.getString(R.string.udisk_eject));
                mUdiskMountToggle.setSummary(mRes.getString(R.string.udisk_eject_summary));

            } catch (IllegalArgumentException e) {
                // this can occur if the U Disk is removed, but we haven't received the 
                // ACTION_MEDIA_REMOVED Intent yet.
                status_udisk= Environment.MEDIA_REMOVED;
            }
            
        } else {
            mUdiskSize.setSummary(mRes.getString(R.string.udisk_unavailable));
            mUdiskAvail.setSummary(mRes.getString(R.string.udisk_unavailable));


            if (!Environment.isExternalStorageRemovable()) {
                if (status_udisk.equals(Environment.MEDIA_UNMOUNTED)) {
                    if (!mUdiskMountToggleAdded) {
                        mUdiskMountPreferenceGroup.addPreference(mUdiskMountToggle);
                        mUdiskMountToggleAdded = true;
                    }
                }
            }

            if (status_udisk.equals(Environment.MEDIA_UNMOUNTED) ||
                status_udisk.equals(Environment.MEDIA_NOFS) ||
                status_udisk.equals(Environment.MEDIA_UNMOUNTABLE) ) {
                mUdiskMountToggle.setEnabled(true);
                mUdiskMountToggle.setTitle(mRes.getString(R.string.udisk_mount));
                mUdiskMountToggle.setSummary(mRes.getString(R.string.udisk_mount_summary));
            } else {
                mUdiskMountToggle.setEnabled(false);
                mUdiskMountToggle.setTitle(mRes.getString(R.string.udisk_mount));
                mUdiskMountToggle.setSummary(mRes.getString(R.string.udisk_insert_summary));
            }
        }

        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        findPreference("memory_internal_avail").setSummary(formatSize(availableBlocks * blockSize));
    }
    
    private String formatSize(long size) {
        return Formatter.formatFileSize(this, size);
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }
    
}
