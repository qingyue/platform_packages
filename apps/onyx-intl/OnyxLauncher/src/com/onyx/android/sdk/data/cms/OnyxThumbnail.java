/**
 * 
 */
package com.onyx.android.sdk.data.cms;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author joy
 *
 */
public class OnyxThumbnail
{
    public static final String DB_TABLE_NAME = "library_thumbnails";
    public static final Uri CONTENT_URI = Uri.parse("content://" + OnyxCmsCenter.PROVIDER_AUTHORITY + "/" + DB_TABLE_NAME);
    
    public static class Columns implements BaseColumns
    {
        /**
         * _data is used by Android convention
         */
        public static String _DATA = "_data";
        public static String SOURCE_MD5 = "SOURCE_MD5";
        
        // need read at runtime
        private static boolean sInitedColumnIndexes = false; 
        private static int sColumnID = -1;
        private static int sColumnDATA = -1;
        private static int sColumnSourceMD5 = -1;
        
        /**
         * need know from outside the directory of application
         * @param sourceMD5
         * @param AppDir
         * @return
         */
        public static ContentValues createColumnData(String sourceMD5)
        {
            ContentValues values = new ContentValues();
            values.put(SOURCE_MD5, sourceMD5);
            
            return values;
        }
        
        public static OnyxThumbnail readColumnData(Cursor c)
        {
            if (!sInitedColumnIndexes) {
                sColumnID = c.getColumnIndex(_ID);
                sColumnDATA = c.getColumnIndex(_DATA);
                sColumnSourceMD5 = c.getColumnIndex(SOURCE_MD5);
                
                sInitedColumnIndexes = true;
            }
            
            long id = c.getLong(sColumnID);
            String data = c.getString(sColumnDATA);
            String md5 = c.getString(sColumnSourceMD5);
            
            OnyxThumbnail thumbnail = new OnyxThumbnail();
            thumbnail.mId = id;
            thumbnail.mData = data;
            thumbnail.mSourceMD5 = md5;
            
            return thumbnail;
        }
    }
    
    // -1 should never be valid DB value
    private static final int INVALID_ID = -1;
    
    private long mId = INVALID_ID;
    private String mData = null;
    private String mSourceMD5 = null;
    
    public OnyxThumbnail()
    {
    }
    
    public long getId()
    {
        return mId;
    }
    public String getData()
    {
        return mData;
    }
    public String getSourceMD5()
    {
        return mSourceMD5;
    }
    
}
