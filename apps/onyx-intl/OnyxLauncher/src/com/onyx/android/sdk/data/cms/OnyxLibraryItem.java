/**
 * 
 */
package com.onyx.android.sdk.data.cms;

import java.io.File;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.onyx.android.sdk.data.util.FileUtil;
import com.onyx.android.sdk.data.util.NotImplementedException;

/**
 * @author joy
 *
 */
public class OnyxLibraryItem
{
    public static final String DB_TABLE_NAME = "library_items";
    public static final Uri CONTENT_URI = Uri.parse("content://" + OnyxCmsCenter.PROVIDER_AUTHORITY + "/" + DB_TABLE_NAME);
    
    public static class Columns implements BaseColumns {
        public static final String PATH = "path";
        public static final String NAME = "name";
        public static final String SIZE = "size";
        public static final String TYPE = "type";
        public static final String ACCESS_TIME = "access_time";
        
        // need read at runtime
        private static boolean sInitedColumnIndexes = false; 
        private static int sColumnID = -1;
        private static int sColumnPath = -1;
        private static int sColumnName = -1;
        private static int sColumnSize = -1;
        private static int sColumnType = -1;
        private static int sColumAccessTime = -1;
        
        // TODO: default sort by file name, which may conflict with file title
        public static final String DEFAULT_ORDER_BY = Columns.NAME;
        
        public static ContentValues createColumnData(File file)
        {
            ContentValues values = new ContentValues();
            values.put(PATH, file.getAbsolutePath());
            values.put(NAME, file.getName());
            values.put(SIZE, file.length());
            values.put(TYPE, FileUtil.getFileExtension(file.getName()));
            values.put(ACCESS_TIME,file.lastModified());
            
            
            return values;
        }
        
        public static OnyxLibraryItem readColumnData(ContentValues columnData)
        {
            throw new NotImplementedException();
        }
        
        public static void readColumnData(Cursor c, OnyxLibraryItem item)
        {
            if (!sInitedColumnIndexes) {
                sColumnID = c.getColumnIndex(_ID);
                sColumnPath = c.getColumnIndex(PATH);
                sColumnName = c.getColumnIndex(NAME);
                sColumnSize = c.getColumnIndex(SIZE);
                sColumnType = c.getColumnIndex(TYPE);
                sColumAccessTime = c.getColumnIndex(ACCESS_TIME);
                
                sInitedColumnIndexes = true;
            }
            
            long id = c.getLong(sColumnID);
            String path = c.getString(sColumnPath);
            String name = c.getString(sColumnName);
            long size = c.getLong(sColumnSize);
            String type = c.getString(sColumnType);
            long access_time = c.getLong(sColumAccessTime);
            
            item.setId(id);
            item.setPath(path);
            item.setName(name);
            item.setSize(size);
            item.setType(type);
            item.setAccessTime(access_time);
        }
        
        public static OnyxLibraryItem readColumnData(Cursor c)
        {
            OnyxLibraryItem item = new OnyxLibraryItem();
            readColumnData(c, item); 
            return item;
        }
    }
    
    private long mId = 0;
    private String mPath = null;
    private String mName = null;
    private long mSize = 0;
    private String mType = null;
    private long mAccessTime = 0;
    
    public OnyxLibraryItem()
    {
    }
  
	public OnyxLibraryItem(File file) 
    {
        mPath = file.getAbsolutePath();
        mName = file.getName();
        mSize = file.length();
        mType = FileUtil.getFileExtension(file.getName());
        mAccessTime = file.lastModified();
    }
    
    public long getId()
    {
        return mId;
    }
    public void setId(long id)
    {
        mId = id;
    }
    
    public String getPath()
    {
        return mPath;
    }
    public void setPath(String path)
    {
        mPath = path;
    }
    
    public String getName()
    {
        return mName;
    }
    public void setName(String name)
    {
        this.mName = name;
    }
    
    public void setAccessTime(long access_time) {
  		this.mAccessTime = access_time;
  	}
    public long getAccessTime(){
    	return mAccessTime;
    }
    
  	public void setType(String type) {
  		this.mType = type;
  	}
  	public String getType(){
  		return mType;
  	}
  	
  	public void setSize(long size) {
  		this.mSize = size;
  	}
  	public long getSize(){
  		return mSize;
  	}
}
