package com.onyx.android.sdk.data.cms;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import com.onyx.android.sdk.data.util.NotImplementedException;

public class OnyxItemHistoryRecord
{
    public static final String DB_TABLE_NAME = "library_history";
    public static final Uri CONTENT_URI = Uri.parse("content://" + OnyxCmsCenter.PROVIDER_AUTHORITY + "/" + DB_TABLE_NAME);
    
    /**
     * only store reading history longer than the threshold 5 * 60s.
     * considering being configured by user if necessary 
     */
    public static int HISTORY_THRESHOLD = 300;
    
    public static class ItemHistoryColumns implements BaseColumns 
    {
        public static final String Path = "Path";
        public static final String Time = "Time";
        public static final String Duration = "Duration";
        
        public static ContentValues createColumnData(OnyxItemHistoryRecord historyRecord)
        {
            throw new NotImplementedException();
        }
        
        public static OnyxItemHistoryRecord parseColumnsData(ContentValues columnData)
        {
            throw new NotImplementedException();
        }
    }
    
    private String mPath = null;
    private String mTime = null;
    private int mDuration = 0;
    
    public String getPath()
    {
        return mPath;
    }
    public void setPath(String path)
    {
        this.mPath = path;
    }
    public String getTime()
    {
        return mTime;
    }
    public void setTime(String time)
    {
        this.mTime = time;
    }
    public int getDuration()
    {
        return mDuration;
    }
    public void setDuration(int duration)
    {
        this.mDuration = duration;
    }

}
