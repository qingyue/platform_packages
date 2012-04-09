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
public class OnyxAppPreference
{
    public static final String DB_TABLE_NAME = "app_preference";
    public static final Uri CONTENT_URI = Uri.parse("content://" + OnyxSysCenter.PROVIDER_AUTHORITY + "/" + DB_TABLE_NAME);
    
    public static class Columns implements BaseColumns
    {
        public static final String FILE_EXTENSION = "file_extension";
        public static final String APP_NAME = "app_name";
        public static final String ACTIVITY_PACKAGE_NAME = "activity_package_name";
        public static final String ACTIVITY_CLASS_NAME = "activity_class_name";
        
        // need read at runtime
        private static boolean sInitedColumnIndexes = false; 
        private static int sColumnID = -1;
        private static int sColumnFileExtension = -1;
        private static int sColumnAppName = -1;
        private static int sColumnActivityPackageName = -1;
        private static int sColumnActivityClassName = -1;
        
        public static ContentValues createColumnData(OnyxAppPreference preference) 
        {
            ContentValues values = new ContentValues();
            values.put(FILE_EXTENSION, preference.getFileExtension());
            values.put(APP_NAME, preference.getAppName());
            values.put(ACTIVITY_PACKAGE_NAME, preference.getAppPackageName());
            values.put(ACTIVITY_CLASS_NAME, preference.getAppClassName()); 
            return values;
        }
        
        public static OnyxAppPreference readColumnData(Cursor c)
        {
            if (!sInitedColumnIndexes) {
                sColumnID = c.getColumnIndex(_ID);
                sColumnFileExtension = c.getColumnIndex(FILE_EXTENSION);
                sColumnAppName = c.getColumnIndex(APP_NAME);
                sColumnActivityPackageName = c.getColumnIndex(ACTIVITY_PACKAGE_NAME);
                sColumnActivityClassName = c.getColumnIndex(ACTIVITY_CLASS_NAME);
                
                sInitedColumnIndexes = true;
            }
            
            long id = c.getLong(sColumnID);
            String ext = c.getString(sColumnFileExtension);
            String app = c.getString(sColumnAppName);
            String pkg = c.getString(sColumnActivityPackageName);
            String cls = c.getString(sColumnActivityClassName);
            
            OnyxAppPreference p = new OnyxAppPreference();
            p.setId(id);
            p.setFileExtension(ext);
            p.setAppName(app);
            p.setActivityPackageName(pkg);
            p.setActivityClassName(cls);
            
            return p;
        }
    }
    
    // -1 should never be valid DB value
    private static final int sInvalidID = -1;
    
    private long mId = sInvalidID;
    // upper case
    private String mFileExtension = null;
    private String mAppName = null;
    private String mActvityPackageName = null;
    private String mActivityClassName = null;
    
    public OnyxAppPreference()
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
    /**
     * upper case file extension
     * @return
     */
    public String getFileExtension()
    {
        return mFileExtension;
    }
    public void setFileExtension(String fileExtension)
    {
        this.mFileExtension = fileExtension.toUpperCase();
    }
    public String getAppName()
    {
        return mAppName;
    }
    public void setAppName(String appName)
    {
        mAppName = appName;
    }
    public String getAppPackageName()
    {
        return mActvityPackageName;
    }
    public void setActivityPackageName(String activityPackageName)
    {
        this.mActvityPackageName = activityPackageName;
    }
    public String getAppClassName()
    {
        return mActivityClassName;
    }
    public void setActivityClassName(String activityClassName)
    {
        this.mActivityClassName = activityClassName;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (!(obj instanceof OnyxAppPreference)) {
            return false;
        }
        
        OnyxAppPreference dst = (OnyxAppPreference)obj;
        if (!mFileExtension.equals(dst.mFileExtension)) {
            return false;
        }
        if (!mAppName.equals(dst.mAppName)) {
            return false;
        }
        if (!mActvityPackageName.equals(dst.mActvityPackageName)) {
            return false;
        }
        if (!mActivityClassName.equals(dst.mActivityClassName)) {
            return false;
        }
        
        return true;
    }
}
