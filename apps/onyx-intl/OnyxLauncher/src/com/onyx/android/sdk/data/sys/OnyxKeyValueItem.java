/**
 * 
 */
package com.onyx.android.sdk.data.sys;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author joy
 *
 */
public class OnyxKeyValueItem
{
    public static final String DB_TABLE_NAME = "key_value_item";
    public static final Uri CONTENT_URI = Uri.parse("content://" + OnyxSysCenter.PROVIDER_AUTHORITY + "/" + DB_TABLE_NAME);
    
    public static class Columns implements BaseColumns
    {
        public static final String KEY = "key";
        public static final String VALUE = "value";
        
        // need read at runtime
        private static boolean sInitedColumnIndexes = false; 
        private static int sColumnID = -1;
        private static int sColumnKey = -1;
        private static int sColumnValue = -1;
        
        public static ContentValues createColumnData(OnyxKeyValueItem item)
        {
            ContentValues values = new ContentValues();
            values.put(KEY, item.getKey());
            values.put(VALUE, item.getValue());
            return values;
        }
        
        public static OnyxKeyValueItem readColumnData(Cursor c)
        {
            if (!sInitedColumnIndexes) {
                sColumnID = c.getColumnIndex(_ID);
                sColumnKey = c.getColumnIndex(KEY);
                sColumnValue = c.getColumnIndex(VALUE);
                
                sInitedColumnIndexes = true;
            }
            
            long id = c.getLong(sColumnID);
            String key = c.getString(sColumnKey);
            String value = c.getString(sColumnValue);
            
            OnyxKeyValueItem item = new OnyxKeyValueItem();
            item.setId(id);
            item.setKey(key);
            item.setValue(value);
            
            return item;
        }
    }
    
    // -1 should never be valid DB value
    private static final int sInvalidID = -1;
    
    private long mId = sInvalidID;
    private String mKey = null;
    private String mValue = null;
    
    public OnyxKeyValueItem()
    {
    }
    
    public long getId()
    {
        return mId;
    }
    public void setId(long id)
    {
        mId = id;
    }
    
    public String getKey()
    {
        return mKey;
    }
    public void setKey(String key)
    {
        mKey = key;
    }
    
    public String getValue()
    {
        return mValue;
    }
    public void setValue(String value)
    {
        mValue = value;
    }
}
