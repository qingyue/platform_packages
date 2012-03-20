/**
 * 
 */
package com.onyx.android.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import com.onyx.android.sdk.data.cms.OnyxCmsCenter;
import com.onyx.android.sdk.data.cms.OnyxLibraryItem;
import com.onyx.android.sdk.data.cms.OnyxMetadata;
import com.onyx.android.sdk.data.cms.OnyxThumbnail;

/**
 * @author joy
 *
 */
public class OnyxCmsProvider extends ContentProvider
{
    private static final String TAG = "OnyxCmsProvider";
    
    private static final String sDBName = "onyx.cms.db";
    private static final int sDBVersion = 2;
    
    private static HashMap<String, String> sItemProjectionMap;
    private static HashMap<String, String> sMetadataProjectionMap;
    private static HashMap<String, String> sThumbnailProjectionMap;
    
    private static final UriMatcher sUriMatcher;
    private static class MatcherResult {
        public static final int ITEMS = 1;
        public static final int ITEM_ID = 2;
        public static final int METADATAS = 3;
        public static final int METADATA_ID = 4;
        public static final int THUMBNAILS = 5;
        public static final int THUMBNAIL_ID = 6;
    }
    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(OnyxCmsCenter.PROVIDER_AUTHORITY, OnyxLibraryItem.DB_TABLE_NAME, MatcherResult.ITEMS);
        sUriMatcher.addURI(OnyxCmsCenter.PROVIDER_AUTHORITY, OnyxLibraryItem.DB_TABLE_NAME + "/#", MatcherResult.ITEM_ID);
        sUriMatcher.addURI(OnyxCmsCenter.PROVIDER_AUTHORITY, OnyxMetadata.DB_TABLE_NAME, MatcherResult.METADATAS);
        sUriMatcher.addURI(OnyxCmsCenter.PROVIDER_AUTHORITY, OnyxMetadata.DB_TABLE_NAME + "/#", MatcherResult.METADATA_ID);
        sUriMatcher.addURI(OnyxCmsCenter.PROVIDER_AUTHORITY, OnyxThumbnail.DB_TABLE_NAME, MatcherResult.THUMBNAILS);
        sUriMatcher.addURI(OnyxCmsCenter.PROVIDER_AUTHORITY, OnyxThumbnail.DB_TABLE_NAME + "/#", MatcherResult.THUMBNAIL_ID);
        
        sItemProjectionMap = new HashMap<String, String>();
        sItemProjectionMap.put(OnyxLibraryItem.Columns._ID, OnyxLibraryItem.Columns._ID);
        sItemProjectionMap.put(OnyxLibraryItem.Columns.PATH, OnyxLibraryItem.Columns.PATH);
        sItemProjectionMap.put(OnyxLibraryItem.Columns.NAME, OnyxLibraryItem.Columns.NAME);
        
        sMetadataProjectionMap = new HashMap<String, String>();
        sMetadataProjectionMap.put(OnyxMetadata.Columns._ID, OnyxMetadata.Columns._ID);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.MD5, OnyxMetadata.Columns.MD5);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.NAME, OnyxMetadata.Columns.NAME);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.TITLE, OnyxMetadata.Columns.TITLE);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.AUTHORS, OnyxMetadata.Columns.AUTHORS);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.LOCATION, OnyxMetadata.Columns.LOCATION);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.NATIVE_ABSOLUTE_PATH, OnyxMetadata.Columns.NATIVE_ABSOLUTE_PATH);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.LAST_ACCESS, OnyxMetadata.Columns.LAST_ACCESS);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.LANGUAGE, OnyxMetadata.Columns.LANGUAGE);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.ENCODING, OnyxMetadata.Columns.ENCODING);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.TAGS, OnyxMetadata.Columns.TAGS);
        sMetadataProjectionMap.put(OnyxMetadata.Columns.SIZE, OnyxMetadata.Columns.SIZE);
        
        sThumbnailProjectionMap = new HashMap<String, String>();
        sThumbnailProjectionMap.put(OnyxThumbnail.Columns._ID, OnyxThumbnail.Columns._ID);
        sThumbnailProjectionMap.put(OnyxThumbnail.Columns._DATA, OnyxThumbnail.Columns._DATA);
        sThumbnailProjectionMap.put(OnyxThumbnail.Columns.SOURCE_MD5, OnyxThumbnail.Columns.SOURCE_MD5);
    }
    
    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context)
        {
            super(context, sDBName, null, sDBVersion);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("CREATE TABLE " + OnyxLibraryItem.DB_TABLE_NAME + " ("
                    + OnyxLibraryItem.Columns._ID + " INTEGER PRIMARY KEY,"
                    + OnyxLibraryItem.Columns.PATH + " TEXT,"
                    + OnyxLibraryItem.Columns.NAME + " TEXT COLLATE NOCASE"
                    + ");");
            
            db.execSQL("CREATE TABLE " + OnyxMetadata.DB_TABLE_NAME + " ("
                    + OnyxMetadata.Columns._ID + " INTEGER PRIMARY KEY,"
                    + OnyxMetadata.Columns.MD5 + " TEXT,"
                    + OnyxMetadata.Columns.NAME + " TEXT,"
                    + OnyxMetadata.Columns.TITLE + " TEXT,"
                    + OnyxMetadata.Columns.AUTHORS + " TEXT,"
                    + OnyxMetadata.Columns.LOCATION + " TEXT,"
                    + OnyxMetadata.Columns.NATIVE_ABSOLUTE_PATH + " TEXT,"
                    + OnyxMetadata.Columns.LAST_ACCESS + " TEXT,"
                    + OnyxMetadata.Columns.LANGUAGE + " TEXT,"
                    + OnyxMetadata.Columns.ENCODING + " TEXT,"
                    + OnyxMetadata.Columns.TAGS + " TEXT,"
                    + OnyxMetadata.Columns.SIZE + " LONG"
                    + ");");
            
            db.execSQL("CREATE TABLE " + OnyxThumbnail.DB_TABLE_NAME + " ("
                    + OnyxThumbnail.Columns._ID + " INTEGER PRIMARY KEY,"
                    + OnyxThumbnail.Columns._DATA + " TEXT,"
                    + OnyxThumbnail.Columns.SOURCE_MD5 + " TEXT"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            // TODO: data is important, simply dropping is not be accepted 
//            db.execSQL("DROP TABLE IF EXISTS " + OnyxLibraryItem.DB_TABLE_NAME);
//            onCreate(db);
        }
        
    }
    
    private DBHelper mDBHelper = null;
    
    public static String getThumbnailFile(Context context, String sourceMD5)
    {
        String thumbnail_folder = ".thumbnails";
        String preferred_extension = ".jpg";
        String thumbnail_file = context.getFilesDir().getAbsolutePath() + File.separator + thumbnail_folder + 
                File.separator + sourceMD5 + preferred_extension;
        return thumbnail_file;
    }

    @Override
    public boolean onCreate()
    {
        mDBHelper = new DBHelper(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder)
    {
        String order_by = sortOrder;
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder(); 
        
        int matcher_result = sUriMatcher.match(uri);
        if ((matcher_result == MatcherResult.ITEMS) ||
                (matcher_result == MatcherResult.ITEM_ID)) {
            builder.setTables(OnyxLibraryItem.DB_TABLE_NAME);
            builder.setProjectionMap(sItemProjectionMap);
            
            if (TextUtils.isEmpty(order_by)) {
                order_by = OnyxLibraryItem.Columns.DEFAULT_ORDER_BY;
            }
            
            if (matcher_result == MatcherResult.ITEM_ID) {
                builder.appendWhere(OnyxLibraryItem.Columns._ID + "=" + uri.getPathSegments().get(1));
            }
        }
        else if ((matcher_result == MatcherResult.METADATAS) ||
                (matcher_result == MatcherResult.METADATA_ID)) {
            builder.setTables(OnyxMetadata.DB_TABLE_NAME);
            builder.setProjectionMap(sMetadataProjectionMap);
            
            if (matcher_result == MatcherResult.METADATA_ID) {
                builder.appendWhere(OnyxMetadata.Columns._ID + "=" + uri.getPathSegments().get(1));
            }
        }
        else if ((matcher_result == MatcherResult.THUMBNAILS) ||
                (matcher_result == MatcherResult.THUMBNAIL_ID)) {
            builder.setTables(OnyxThumbnail.DB_TABLE_NAME);
            builder.setProjectionMap(sThumbnailProjectionMap);
            
            if (matcher_result == MatcherResult.THUMBNAIL_ID) {
                builder.appendWhere(OnyxThumbnail.Columns._ID + "=" + uri.getPathSegments().get(1));
            }
        }
        else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = builder.query(db, projection, selection, selectionArgs, null, null, order_by);
        
        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        if (sUriMatcher.match(uri) != MatcherResult.ITEMS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        for (ContentValues v : values) {
            if (!this.precheckLibraryitemValues(v)) {
                return 0;
            }
        }
        
        SQLiteDatabase db  = null;
        try {
            db = mDBHelper.getWritableDatabase();
            db.beginTransaction();
            
            for (ContentValues v : values) {
                long id = db.insert(OnyxLibraryItem.DB_TABLE_NAME, OnyxLibraryItem.Columns.NAME, v);
                if (id < 0) {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            
            db.setTransactionSuccessful();
            
            return values.length;
        }
        finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        String dst_table = null;
        String dst_null_column_hack = null;
        Uri dst_content_uri = null;
        
        final int match_result = sUriMatcher.match(uri);
        if (match_result == MatcherResult.ITEMS) {
            if (!this.precheckLibraryitemValues(values)) {
                return null;
            }
            
            dst_table = OnyxLibraryItem.DB_TABLE_NAME;
            dst_null_column_hack = OnyxLibraryItem.Columns.NAME;
            dst_content_uri = OnyxLibraryItem.CONTENT_URI;
        }
        else if (match_result == MatcherResult.METADATAS) {
            dst_table = OnyxMetadata.DB_TABLE_NAME;
            dst_null_column_hack = OnyxMetadata.Columns.NAME;
            dst_content_uri = OnyxMetadata.CONTENT_URI;
        }
        else if (match_result == MatcherResult.THUMBNAILS) {
            dst_table = OnyxThumbnail.DB_TABLE_NAME;
            dst_null_column_hack = OnyxThumbnail.Columns.SOURCE_MD5;
            dst_content_uri = OnyxThumbnail.CONTENT_URI;
            
            String md5 = values.getAsString(OnyxThumbnail.Columns.SOURCE_MD5);
            String thumbnail_file = getThumbnailFile(this.getContext(), md5);
            Log.d(TAG, "creating thumbnail file: " + thumbnail_file);
            if (!this.ensureFileExists(thumbnail_file)) {
                throw new IllegalStateException("Unable to create new file: " + thumbnail_file);
            }
            values.put(OnyxThumbnail.Columns._DATA, thumbnail_file);
        }
        else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        SQLiteDatabase db  = null;
        long id = -1;
        try {
            db = mDBHelper.getWritableDatabase();

            id = db.insert(dst_table, dst_null_column_hack, values);
            if (id < 0) {
                throw new SQLException("Failed to insert row into " + uri);
            }
        }
        finally {
            if (db != null) {
                db.close();
            }
        }

        Uri ret = ContentUris.withAppendedId(dst_content_uri, id);
        this.getContext().getContentResolver().notifyChange(ret, null);

        return ret;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = null;
        try {
            db = mDBHelper.getWritableDatabase();
            int count = 0;
            
            switch (sUriMatcher.match(uri)) {
            case MatcherResult.ITEMS:
                count = db.delete(OnyxLibraryItem.DB_TABLE_NAME, selection, selectionArgs);
                break;
            case MatcherResult.ITEM_ID: {
                String id = uri.getPathSegments().get(1);
                String where = OnyxLibraryItem.Columns._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = where + " AND (" + selection + ")";
                }
                count = db.delete(OnyxLibraryItem.DB_TABLE_NAME, where, selectionArgs);
                break;
            }
            case MatcherResult.METADATAS:
                count = db.delete(OnyxMetadata.DB_TABLE_NAME, selection, selectionArgs);
                break;
            case MatcherResult.METADATA_ID: {
                String id = uri.getPathSegments().get(1);
                String where = OnyxMetadata.Columns._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = where + " AND (" + selection + ")";
                }
                count = db.delete(OnyxMetadata.DB_TABLE_NAME, where, selectionArgs);
                break;
            }
            case MatcherResult.THUMBNAILS:
                count = db.delete(OnyxThumbnail.DB_TABLE_NAME, selection, selectionArgs);
                break;
            case MatcherResult.THUMBNAIL_ID: {
                String id = uri.getPathSegments().get(1);
                String where = OnyxThumbnail.Columns._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = where + " AND (" + selection + ")";
                }
                count = db.delete(OnyxThumbnail.DB_TABLE_NAME, where, selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri); 
            }
            
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs)
    {
        SQLiteDatabase db = null;
        try {
            db = mDBHelper.getWritableDatabase();
            int count = 0;
            
            switch (sUriMatcher.match(uri)) {
            case MatcherResult.ITEMS:
                count = db.update(OnyxLibraryItem.DB_TABLE_NAME, values, selection, selectionArgs);
                break;
            case MatcherResult.ITEM_ID: {
                String id = uri.getPathSegments().get(1);
                String where = OnyxLibraryItem.Columns._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = where + " AND (" + selection + ")";
                }
                count = db.update(OnyxLibraryItem.DB_TABLE_NAME, values, where, selectionArgs);
                break;
            }
            case MatcherResult.METADATAS:
                count = db.update(OnyxMetadata.DB_TABLE_NAME, values, selection, selectionArgs);
                break;
            case MatcherResult.METADATA_ID: {
                String id = uri.getPathSegments().get(1);
                String where = OnyxMetadata.Columns._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = where + " AND (" + selection + ")";
                }
                count = db.update(OnyxMetadata.DB_TABLE_NAME, values, where, selectionArgs);
                break;
            }
            case MatcherResult.THUMBNAILS:
            case MatcherResult.THUMBNAIL_ID:
                // thumbnail has no needs of updating 
                assert(false);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri); 
            }
            
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }
    
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException
    {
        int matcher_result = sUriMatcher.match(uri);
        if (matcher_result == MatcherResult.THUMBNAIL_ID) {
            ParcelFileDescriptor d = this.openFileHelper(uri, mode);
            return d;
        }
        else {
            assert(false);
            return null;
        }
    }
    
    private boolean precheckLibraryitemValues(ContentValues values) 
    {
        if (!values.containsKey(OnyxLibraryItem.Columns.PATH)) {
            throw new IllegalArgumentException("missing value: path");
        }
        if (!values.containsKey(OnyxLibraryItem.Columns.NAME)) {
            throw new IllegalArgumentException("missing value: name");
        }
        
        return true;
    }
    
    private boolean ensureFileExists(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            // we will not attempt to create the first directory in the path
            // (for example, do not create /sdcard if the SD card is not mounted)
            int secondSlash = path.indexOf('/', 1);
            if (secondSlash < 1) return false;
            String directoryPath = path.substring(0, secondSlash);
            File directory = new File(directoryPath);
            if (!directory.exists())
                return false;
            file.getParentFile().mkdirs();
            try {
                return file.createNewFile();
            } catch(IOException ioe) {
                Log.e(TAG, "File creation failed", ioe);
            }
            return false;
        }
    }

}
