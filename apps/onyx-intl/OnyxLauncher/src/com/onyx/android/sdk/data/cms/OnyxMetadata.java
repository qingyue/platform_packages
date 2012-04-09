package com.onyx.android.sdk.data.cms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.onyx.android.sdk.data.util.NotImplementedException;

public class OnyxMetadata
{
    public static final String DB_TABLE_NAME = "library_metadatas";
    public static final Uri CONTENT_URI = Uri.parse("content://" + OnyxCmsCenter.PROVIDER_AUTHORITY + "/" + DB_TABLE_NAME);
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static final String AUTHOR_SEPERATOR = "\\|";
    
    public static class Columns implements BaseColumns
    {
        // TODO: partial columns
        public static String MD5 = "MD5";
        public static String NAME = "Name";
        public static String TITLE = "Title";
        public static String AUTHORS = "Authors";
        public static String LOCATION = "Location";
        public static String NATIVE_ABSOLUTE_PATH = "NativeAbsolutePath";
        public static String LAST_ACCESS = "LastAccess";
        public static String LANGUAGE = "Language";
        public static String ENCODING = "Encoding";
        public static String TAGS = "Tags";
        public static String SIZE = "Size";
        
        // need read at runtime
        private static boolean sInitedColumnIndexes = false; 
        private static int sColumnID = -1;
        private static int sColumnMD5 = -1;
        private static int sColumnName = -1;
        private static int sColumnTitle = -1;
        private static int sColumnAuthors = -1;
        private static int sColumnLocation = -1;
        private static int sColumnNativeAbsolutePath = -1;
        private static int sColumnLastAccess = -1;
        private static int sColumnLanguage = -1;
        private static int sColumnEncoding = -1;
        private static int sColumnTags = -1;
        private static int sColumnSize = -1;
        
        public static ContentValues createColumnData(OnyxMetadata metadata)
        {
            ContentValues values = new ContentValues();
            values.put(MD5, metadata.getMD5());
            values.put(NAME, metadata.getName());
            values.put(TITLE, metadata.getTitle());
            values.put(AUTHORS, metadata.getAuthors() == null ? "" :
                OnyxMetadata.convertAuthorsToString(metadata.getAuthors()));
            values.put(LOCATION, metadata.getLocation());
            values.put(NATIVE_ABSOLUTE_PATH, metadata.getNativeAbsolutePath());
            values.put(LAST_ACCESS, metadata.getLastAccess());
            values.put(LANGUAGE, metadata.getLanguage());
            values.put(ENCODING, metadata.getEncoding());
            values.put(TAGS, metadata.getTags() == null ? "" : metadata.getTags().toString());
            values.put(SIZE, metadata.getSize());
            
            return values;
        }
        
        public static OnyxMetadata readColumnData(ContentValues columnData)
        {
            throw new NotImplementedException();
        }
        
        public static void readColumnData(Cursor c, OnyxMetadata data)
        {
            if (!sInitedColumnIndexes) {
                sColumnID = c.getColumnIndex(_ID);
                sColumnMD5 = c.getColumnIndex(MD5);
                sColumnName = c.getColumnIndex(NAME);
                sColumnTitle = c.getColumnIndex(TITLE);
                sColumnAuthors = c.getColumnIndex(AUTHORS);
                sColumnLocation = c.getColumnIndex(LOCATION);
                sColumnNativeAbsolutePath = c.getColumnIndex(NATIVE_ABSOLUTE_PATH);
                sColumnLastAccess = c.getColumnIndex(LAST_ACCESS);
                sColumnLanguage = c.getColumnIndex(LANGUAGE);
                sColumnEncoding = c.getColumnIndex(ENCODING);
                sColumnTags = c.getColumnIndex(TAGS);
                sColumnSize = c.getColumnIndex(SIZE);
                
                sInitedColumnIndexes = true;
            }
            
            long id = c.getLong(sColumnID);
            String md5 = c.getString(sColumnMD5);
            String name = c.getString(sColumnName);
            String title = c.getString(sColumnTitle);
            String authors = c.getString(sColumnAuthors);
            String location = c.getString(sColumnLocation);
            String native_absolute_path = c.getString(sColumnNativeAbsolutePath);
            String last_access = c.getString(sColumnLastAccess);
            String language = c.getString(sColumnLanguage);
            String encoding = c.getString(sColumnEncoding);
            @SuppressWarnings("unused")
            String tags = c.getString(sColumnTags);
            long size = c.getLong(sColumnSize);
            
            data.setId(id);
            data.setMD5(md5);
            data.setName(name);
            data.setTitle(title);
            data.setAuthors(OnyxMetadata.convertStringToAuthors(authors));
            data.setLocation(location);
            data.setNativeAbsolutePath(native_absolute_path);
            data.setLastAccess(last_access);
            data.setLanguage(language);
            data.setEncoding(encoding);
            data.setTags(null);
            data.setSize(size);
        }
        
        public static OnyxMetadata readColumnData(Cursor c)
        {
            OnyxMetadata data = new OnyxMetadata();
            readColumnData(c, data);
            return data;
        }
    }
    
    // -1 should never be valid DB value
    private static final int INVALID_ID = -1;
    
    private long mId = INVALID_ID;
    private String mMD5 = null;
    private String mName = null;
    private String mTitle = null;
    private ArrayList<String> mAuthors = null;
    private String mLocation = null;
    private String mNativeAbsolutePath = null;
    private String mDescription = null;
    private String mLastAccess = null;
    private String mPublisher = null;
    private String mLanguage = null;
    private String mEncoding = null;
    private ArrayList<String> mTags = null;
    private long mSize = 0;
    private long mLastModified = 0;
    private int mRating = 0;
    private String mProgress = null;
    
    public OnyxMetadata()
    {
    }
    
    public static String convertAuthorsToString(ArrayList<String> authors)
    {
        if ((authors == null) || (authors.size() <= 0)) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(authors.get(0));
        for (int i = 1; i < authors.size(); i++) {
            sb.append(AUTHOR_SEPERATOR).append(authors.get(i));
        }
        return sb.toString();
    }
    
    public static ArrayList<String> convertStringToAuthors(String authorsString)
    {
        if (authorsString == null) {
            return null;
        }
        
        String[] authors = authorsString.split(AUTHOR_SEPERATOR);
        if ((authors == null) || (authors.length <= 0)) {
            return null;
        }
        
        ArrayList<String> result = new ArrayList<String>();
        for (String a : authors) {
            result.add(a);
        }
        return result;
    }
    
    public boolean isDataFromDB()
    {
        return mId != INVALID_ID;
    }
    public long getId()
    {
        return mId;
    }
    public void setId(long id)
    {
        this.mId = id;
    }
    public String getName()
    {
        return mName;
    }
    public void setName(String name)
    {
        this.mName = name;
    }
    public String getTitle()
    {
        return mTitle;
    }
    public void setTitle(String title)
    {
        this.mTitle = title;
    }
    public ArrayList<String> getAuthors()
    {
        return mAuthors;
    }
    public void setAuthors(ArrayList<String> authors)
    {
        this.mAuthors = authors;
    }
    public String getLocation()
    {
        return mLocation;
    }
    public void setLocation(String location)
    {
        this.mLocation = location;
    }
    public String getNativeAbsolutePath()
    {
        return mNativeAbsolutePath;
    }
    public void setNativeAbsolutePath(String nativeAbsolutePath)
    {
        this.mNativeAbsolutePath = nativeAbsolutePath;
    }
    public String getDescription()
    {
        return mDescription;
    }
    public void setDescription(String description)
    {
        this.mDescription = description;
    }
    public String getLastAccess()
    {
        return mLastAccess;
    }
    public void setLastAccess(String lastAccess)
    {
        this.mLastAccess = lastAccess;
    }
    public String getPublisher()
    {
        return mPublisher;
    }
    public void setPublisher(String publisher)
    {
        this.mPublisher = publisher;
    }
    public String getLanguage()
    {
        return mLanguage;
    }
    public void setLanguage(String language)
    {
        this.mLanguage = language;
    }
    public String getEncoding()
    {
        return mEncoding;
    }
    public void setEncoding(String encoding)
    {
        this.mEncoding = encoding;
    }
    public ArrayList<String> getTags()
    {
        return mTags;
    }
    public void setTags(ArrayList<String> tags)
    {
        this.mTags = tags;
    }
    public String getMD5()
    {
        return mMD5;
    }
    public void setMD5(String md5)
    {
        this.mMD5 = md5;
    }
    public long getSize()
    {
        return mSize;
    }
    public void setSize(long size)
    {
        this.mSize = size;
    }
    public long getLastModified()
    {
        return mLastModified;
    }
    public void setlastModified(long lastModified)
    {
        this.mLastModified = lastModified;
    }
    public int getRating()
    {
        return mRating;
    }
    public void setRating(int rating)
    {
        this.mRating = rating;
    }
    public String getProgress()
    {
        return mProgress;
    }
    public void setProgress(String progress)
    {
        this.mProgress = progress;
    }
}
