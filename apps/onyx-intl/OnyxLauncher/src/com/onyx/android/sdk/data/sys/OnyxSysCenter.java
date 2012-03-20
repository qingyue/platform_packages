/**
 * 
 */
package com.onyx.android.sdk.data.sys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


/**
 * @author joy
 *
 */
public class OnyxSysCenter
{
    private static final String TAG = "OnyxSysCenter";
    
    public static final String PROVIDER_AUTHORITY = "com.onyx.android.launcher.OnyxSysProvider";
    
    private static final String KEY_DEFAULT_FONT_FAMILY = "sys.defaut_font_family";
    private static final String KEY_SCREEN_UPDATE_GC_INTERVAL = "sys.screen_update_gc_interval";
    private static final String KEY_SUSPEND_INTERVAL = "sys.suspend_interval";
    private static final String KEY_SHUTDOWN_INTERVAL = "sys.shutdown_interval";
    private static final String KEY_TIMEZONE = "sys.timezone";
    
    private static boolean sInitialized = false;
    private static Map<String, OnyxKeyValueItem> sItemMap = new HashMap<String, OnyxKeyValueItem>();
    
    /**
     * first to call to initializing data before using else methods
     * 
     * @param context
     * @return
     */
    public static synchronized boolean init(Context context)
    {
        if (sInitialized) {
            assert(false);
            return true;
        }
        
        ArrayList<OnyxKeyValueItem> items = new ArrayList<OnyxKeyValueItem>();
        
        if (!queryKeyValueItems(context, items)) {
            return false;
        }
        
        for (OnyxKeyValueItem i : items) {
            sItemMap.put(i.getKey(), i);
        }
        
        sInitialized = true; 
        return true;
    }
    
    /**
     * return null when fail
     * @return
     */
    public static String getDefaultFontFamily()
    {
        if (!sInitialized) {
            return null;
        }
        
        return getStringValue(KEY_DEFAULT_FONT_FAMILY);
    }
    public static boolean setDefaultFontFamily(Context context, String fontFamily)
    {
        if (!sInitialized) {
            return false;
        }
        
        return setStringValue(context, KEY_DEFAULT_FONT_FAMILY, fontFamily);
    }
    
    /**
     * return -1 when fail
     * @return
     */
    public static int getScreenUpdateGCInterval()
    {
        if (!sInitialized) {
            return -1;
        }
        
        return getIntValue(KEY_SCREEN_UPDATE_GC_INTERVAL);
    }
    public static boolean setScreenUpdateGCInterval(Context context, int interval)
    {
        if (!sInitialized) {
            return false;
        }
        
        return setIntValue(context, KEY_SCREEN_UPDATE_GC_INTERVAL, interval);
    }
    
    /**
     * return -1 when fail
     * @return
     */
    public static int getSuspendInterval()
    {
        if (!sInitialized) {
            return -1;
        }
        
        return getIntValue(KEY_SUSPEND_INTERVAL);
    }
    public static boolean setSuspendInterval(Context context, int interval)
    {
        if (!sInitialized) {
            return false;
        }
        
        return setIntValue(context, KEY_SUSPEND_INTERVAL, interval);
    }
    
    /**
     * return -1 when fail
     * @return
     */
    public static int getShutdownInterval()
    {
        if (!sInitialized) {
            return -1;
        }
        
        return getIntValue(KEY_SHUTDOWN_INTERVAL);
    }
    public static boolean setShutdownInterval(Context context, int interval)
    {
        if (!sInitialized) {
            return false;
        }
        
        return setIntValue(context, KEY_SHUTDOWN_INTERVAL, interval);
    }
    
    /**
     * return null when fail
     * @return
     */
    public static String getTimezone()
    {
        if (!sInitialized) {
            return null;
        }
        
        return getStringValue(KEY_TIMEZONE);
    }
    public static boolean setTimezone(Context context, String timezone)
    {

        if (!sInitialized) {
            return false;
        }
        
        return setStringValue(context, KEY_TIMEZONE, timezone);
    }
    
    /**
     * return null when fail
     * @param key
     * @return
     */
    private static String getStringValue(String key)
    {
        if (!sInitialized) {
            return null;
        }
        
        OnyxKeyValueItem item = sItemMap.get(key);
        if (item == null) {
            return null;
        }
        
        return item.getValue();
    }
    private static boolean setStringValue(Context context, String key, String value)
    {
        if (!sInitialized) {
            return false;
        }
        
        OnyxKeyValueItem item = sItemMap.get(KEY_DEFAULT_FONT_FAMILY);
        if (item == null) {
            item = new OnyxKeyValueItem();
            item.setKey(KEY_DEFAULT_FONT_FAMILY);
            item.setValue(value);
            
            if (!insert(context, item)) {
                return false;
            }
            
            sItemMap.put(item.getKey(), item);
            return true;
        }
        else {
            String old = item.getValue();
            item.setValue(value);
            if (!update(context, item)) {
                item.setValue(old);
                return false;
            }
            
            return true;
        }
    }
    
    /**
     * return -1 when fail
     * @param key
     * @return
     */
    private static int getIntValue(String key)
    {
        if (!sInitialized) {
            return -1;
        }
        
        String value = getStringValue(key);
        if (value == null) {
            return -1;
        }
        
        return Integer.parseInt(value);
    }
    private static boolean setIntValue(Context context, String key, int value)
    {
        if (!sInitialized) {
            return false;
        }
        
        return setStringValue(context, key, String.valueOf(value));
    }
    
    private static boolean queryKeyValueItems(Context context, Collection<OnyxKeyValueItem> result)
    {
        Cursor c = null;
        try {
            long time_start = System.currentTimeMillis();
            
            long time_point = time_start; 
            c = context.getContentResolver().query(OnyxKeyValueItem.CONTENT_URI, null, null, null, null);
            long time_db_load = System.currentTimeMillis() - time_point;
            
            if (c == null) {
                return false;
            }
            
            time_point = System.currentTimeMillis();
            if (c.moveToFirst()) {
                result.add(OnyxKeyValueItem.Columns.readColumnData(c));
                
                while (c.moveToNext()) {
                    result.add(OnyxKeyValueItem.Columns.readColumnData(c));
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
    private static boolean insert(Context context, OnyxKeyValueItem item)
    {
        Uri result = context.getContentResolver().insert(OnyxKeyValueItem.CONTENT_URI,
                OnyxKeyValueItem.Columns.createColumnData(item));
        if (result == null) {
            return false;
        }
        
        String id = result.getLastPathSegment();
        if (id == null) {
            return false;
        }
        item.setId(Long.parseLong(id));
        
        return true;
    }
    private static boolean update(Context context, OnyxKeyValueItem item)
    {
        Uri row = Uri.withAppendedPath(OnyxKeyValueItem.CONTENT_URI, String.valueOf(item.getId()));
        int count = context.getContentResolver().update(row,
                OnyxKeyValueItem.Columns.createColumnData(item), null, null);
        if (count <= 0) {
            return false;
        }
        
        assert(count == 1);
        return true;
    }
    @SuppressWarnings("unused")
    private static boolean delete(Context context, OnyxKeyValueItem item)
    {
        Uri row = Uri.withAppendedPath(OnyxKeyValueItem.CONTENT_URI, String.valueOf(item.getId()));
        int count = context.getContentResolver().delete(row, null, null);
        if (count <= 0) {
            return false;
        }
        
        assert(count == 1);
        return true;
    }
}
