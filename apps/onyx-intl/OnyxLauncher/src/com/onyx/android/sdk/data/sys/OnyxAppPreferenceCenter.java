/**
 * 
 */
package com.onyx.android.sdk.data.sys;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.onyx.android.sdk.data.util.FileUtil;

/**
 * @author joy
 *
 */
public class OnyxAppPreferenceCenter
{
    private static final String TAG = "OnyxApplicationCenter";
    
    private static boolean sInitialized = false;
    /**
     * file extension to application map, extensions are UPPER CASE
     */
    private static HashMap<String, OnyxAppPreference> sFileApplicationMap = new HashMap<String, OnyxAppPreference>(); 
    
    /**
     * initializing application preference data, must be called at first before calling else methods
     * 
     * @param context
     */
    public static synchronized boolean init(Context context)
    {
        if (sInitialized) {
            assert(false);
            return true;
        }
        
        ArrayList<OnyxAppPreference> apps = new ArrayList<OnyxAppPreference>();
        if (!getAppPreferences(context, apps)) {
            return false;
        }

        for (OnyxAppPreference a : apps) {
            sFileApplicationMap.put(a.getFileExtension().toUpperCase(), a);
        }
        sInitialized = true;
        
        return true;
    }
    
    public static OnyxAppPreference getApplicationPreference(File file)
    {
        if (!sInitialized) {
            return null;
        }
        
        String ext = FileUtil.getFileExtension(file);
        if (TextUtils.isEmpty(ext)) {
            return null;
        }
        
        return sFileApplicationMap.get(ext.toUpperCase());
    }
    
    public static OnyxAppPreference getApplicationPreference(String ext)
    {
        if (!sInitialized) {
            return null;
        }
        
        return sFileApplicationMap.get(ext.toUpperCase());
    }
    
    /**
     * set application preference relating to file extension
     * 
     * @param file
     * @param app
     * @return
     */
    public static boolean setAppPreference(Context context, String ext, String appName, String pkg, String cls)
    {
        if (!sInitialized) {
            return false;
        }
        
        String up_ext = ext.toUpperCase();
        
        OnyxAppPreference app = new OnyxAppPreference();
        app.setFileExtension(up_ext);
        app.setAppName(appName);
        app.setActivityPackageName(pkg);
        app.setActivityClassName(cls);
        
        if (sFileApplicationMap.containsKey(up_ext)) {
            OnyxAppPreference old = sFileApplicationMap.get(up_ext);
            app.setId(old.getId());
            
            if (old.equals(app)) {
                return true;
            }
            
            if (!updateAppPreference(context, app)) {
                return false;
            }
        }
        else { 
            if (!insertAppPreference(context, app)) {
                return false;
            }
        }
        
        sFileApplicationMap.put(up_ext, app);
        return true;
    }
    
    public static boolean removeAppPreference(Context context, String ext)
    {
        if (!sInitialized) {
            return false;
        }
            
        String up_ext = ext.toUpperCase();
        
        if (!sFileApplicationMap.containsKey(up_ext)) {
            return true;
        }
        
        OnyxAppPreference app = sFileApplicationMap.get(up_ext);
        if (!deleteAppPreference(context, app)) {
            return false;
        }
        
        sFileApplicationMap.remove(up_ext);
        return true;
    }
    
    private static boolean getAppPreferences(Context context, Collection<OnyxAppPreference> result)
    {
        Cursor c = null;
        try {
            long time_start = System.currentTimeMillis();
            
            long time_point = time_start; 
            c = context.getContentResolver().query(OnyxAppPreference.CONTENT_URI, null, null, null, null);
            long time_db_load = System.currentTimeMillis() - time_point;
            
            if (c == null) {
                return false;
            }
            
            time_point = System.currentTimeMillis();
            if (c.moveToFirst()) {
                result.add(OnyxAppPreference.Columns.readColumnData(c));
                
                while (c.moveToNext()) {
                    if (Thread.interrupted()) {
                        return false;
                    }
                    
                    result.add(OnyxAppPreference.Columns.readColumnData(c));
                }
            }
            long time_db_read = System.currentTimeMillis() - time_point;
            
            long time_end = System.currentTimeMillis();
            
            Log.d(TAG, "items loaded, count: " + result.size());
            Log.d(TAG, "db load time: " + time_db_load + "ms\n");
            Log.d(TAG, "read time: " + time_db_read + "ms\n");
            Log.d(TAG, "total time: " + (time_end - time_start) + "ms\n");
            
            return true;
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }
    
    private static boolean insertAppPreference(Context context, OnyxAppPreference preference)
    {
        Uri result = context.getContentResolver().insert(OnyxAppPreference.CONTENT_URI,
                OnyxAppPreference.Columns.createColumnData(preference));
        if (result == null) {
            return false;
        }
        
        String id = result.getLastPathSegment();
        if (id == null) {
            return false;
        }
        preference.setId(Long.parseLong(id));
        
        return true;
    }
    
    private static boolean updateAppPreference(Context context, OnyxAppPreference preference)
    {
        Uri row = Uri.withAppendedPath(OnyxAppPreference.CONTENT_URI, String.valueOf(preference.getId()));
        int count = context.getContentResolver().update(row,
                OnyxAppPreference.Columns.createColumnData(preference), null, null);
        if (count <= 0) {
            return false;
        }
        
        assert(count == 1);
        return true;
    }
    
    private static boolean deleteAppPreference(Context context, OnyxAppPreference preference)
    {
        Uri row = Uri.withAppendedPath(OnyxAppPreference.CONTENT_URI, String.valueOf(preference.getId()));
        int count = context.getContentResolver().delete(row, null, null);
        if (count <= 0) {
            return false;
        }
        
        assert(count == 1);
        return true;
    }
}
