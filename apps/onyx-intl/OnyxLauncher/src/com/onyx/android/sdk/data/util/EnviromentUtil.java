/**
 * 
 */
package com.onyx.android.sdk.data.util;

import java.io.File;

/**
 * @author joy
 *
 */
public class EnviromentUtil
{
    /**
     * wrapper of android.os.Environment.getExternalStorageDirectory
     * 
     * @return
     */
    public static File getExternalStorageDirectory()
    {
        return android.os.Environment.getExternalStorageDirectory();
    }
    
    /**
     * directory of removable SD card, can be different from getExternalStorageDirectory() according to devices
     * 
     * @return
     */
    public static File getRemovableSDCardDirectory()
    {
        File storage_root = getExternalStorageDirectory();

        // if system has an emulated SD card(/mnt/sdcard) provided by device's NAND flash, 
        // then real SD card will be mounted as a child directory(/mnt/sdcard/extsd) in it, which names "extsd" here
        final String SDCARD_MOUNTED_FOLDER = "extsd";
        
        File extsd = new File(storage_root, SDCARD_MOUNTED_FOLDER);
        if (extsd.exists()) {
            return extsd;
        }
        else {
            return storage_root;
        }
    }
    
    public static boolean isFileOnRemovableSDCard(File file)
    {
        return file.getAbsolutePath().startsWith(getRemovableSDCardDirectory().getAbsolutePath());
    }
    
    
}
