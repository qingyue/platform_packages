/**
 * 
 */
package com.onyx.android.launcher;

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
import android.text.TextUtils;
import android.util.Log;

import com.onyx.android.sdk.data.sys.OnyxAppPreference;
import com.onyx.android.sdk.data.sys.OnyxKeyValueItem;
import com.onyx.android.sdk.data.sys.OnyxSysCenter;

/**
 * @author joy
 *
 */
public class OnyxSysProvider extends ContentProvider
{
    private static final String TAG = "OnyxSysProvider";
    
    private static final String sDBName = "onyx.sys.db";
    private static final int sDBVersion = 2;
    
    private static HashMap<String, String> sAppPreferenceProjectionMap;
    private static HashMap<String, String> sKeyValueItemProjectionMap;
    
    private static final UriMatcher sUriMatcher;
    private static class MatcherResult {
        public static final int APP_PREFERENCES = 1;
        public static final int APP_PREFERENCE_ID = 2;
        public static final int KEY_VALUE_ITEMS = 3;
        public static final int KEY_VALUE_ITEM_ID = 4;
    }
    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(OnyxSysCenter.PROVIDER_AUTHORITY, OnyxAppPreference.DB_TABLE_NAME, MatcherResult.APP_PREFERENCES);
        sUriMatcher.addURI(OnyxSysCenter.PROVIDER_AUTHORITY, OnyxAppPreference.DB_TABLE_NAME + "/#", MatcherResult.APP_PREFERENCE_ID);
        sUriMatcher.addURI(OnyxSysCenter.PROVIDER_AUTHORITY, OnyxKeyValueItem.DB_TABLE_NAME, MatcherResult.KEY_VALUE_ITEMS);
        sUriMatcher.addURI(OnyxSysCenter.PROVIDER_AUTHORITY, OnyxKeyValueItem.DB_TABLE_NAME + "/#", MatcherResult.KEY_VALUE_ITEM_ID);
        
        sAppPreferenceProjectionMap = new HashMap<String, String>();
        sAppPreferenceProjectionMap.put(OnyxAppPreference.Columns._ID, OnyxAppPreference.Columns._ID);
        sAppPreferenceProjectionMap.put(OnyxAppPreference.Columns.FILE_EXTENSION, OnyxAppPreference.Columns.FILE_EXTENSION);
        sAppPreferenceProjectionMap.put(OnyxAppPreference.Columns.APP_NAME, OnyxAppPreference.Columns.APP_NAME);
        sAppPreferenceProjectionMap.put(OnyxAppPreference.Columns.ACTIVITY_PACKAGE_NAME, OnyxAppPreference.Columns.ACTIVITY_PACKAGE_NAME);
        sAppPreferenceProjectionMap.put(OnyxAppPreference.Columns.ACTIVITY_CLASS_NAME, OnyxAppPreference.Columns.ACTIVITY_CLASS_NAME);
        
        sKeyValueItemProjectionMap = new HashMap<String, String>();
        sKeyValueItemProjectionMap.put(OnyxKeyValueItem.Columns._ID, OnyxKeyValueItem.Columns._ID);
        sKeyValueItemProjectionMap.put(OnyxKeyValueItem.Columns.KEY, OnyxKeyValueItem.Columns.KEY);
        sKeyValueItemProjectionMap.put(OnyxKeyValueItem.Columns.VALUE, OnyxKeyValueItem.Columns.VALUE);
    }
    
    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context)
        {
            super(context, sDBName, null, sDBVersion);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("CREATE TABLE " + OnyxAppPreference.DB_TABLE_NAME + " ("
                    + OnyxAppPreference.Columns._ID + " INTEGER PRIMARY KEY,"
                    + OnyxAppPreference.Columns.FILE_EXTENSION + " TEXT,"
                    + OnyxAppPreference.Columns.APP_NAME + " TEXT,"
                    + OnyxAppPreference.Columns.ACTIVITY_PACKAGE_NAME + " TEXT,"
                    + OnyxAppPreference.Columns.ACTIVITY_CLASS_NAME + " TEXT"
                    + ");");
            
            db.execSQL("CREATE TABLE " + OnyxKeyValueItem.DB_TABLE_NAME + " ("
                    + OnyxKeyValueItem.Columns._ID + " INTEGER PRIMARY KEY,"
                    + OnyxKeyValueItem.Columns.KEY + " TEXT,"
                    + OnyxKeyValueItem.Columns.VALUE + " TEXT"
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
        if ((matcher_result == MatcherResult.APP_PREFERENCES) ||
                (matcher_result == MatcherResult.APP_PREFERENCE_ID)) {
            builder.setTables(OnyxAppPreference.DB_TABLE_NAME);
            builder.setProjectionMap(sAppPreferenceProjectionMap);
            
            if (matcher_result == MatcherResult.APP_PREFERENCE_ID) {
                builder.appendWhere(OnyxAppPreference.Columns._ID + "=" + uri.getPathSegments().get(1));
            }
        }
        else if ((matcher_result == MatcherResult.KEY_VALUE_ITEMS) ||
                (matcher_result == MatcherResult.KEY_VALUE_ITEM_ID)) {
            builder.setTables(OnyxKeyValueItem.DB_TABLE_NAME);
            builder.setProjectionMap(sAppPreferenceProjectionMap);
            
            if (matcher_result == MatcherResult.KEY_VALUE_ITEM_ID) {
                builder.appendWhere(OnyxKeyValueItem.Columns._ID + "=" + uri.getPathSegments().get(1));
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
    public Uri insert(Uri uri, ContentValues values)
    {
        String dst_table = null;
        String dst_null_column_hack = null;
        Uri dst_content_uri = null;
        
        final int match_result = sUriMatcher.match(uri);
        if (match_result == MatcherResult.APP_PREFERENCES) {
            dst_table = OnyxAppPreference.DB_TABLE_NAME;
            dst_null_column_hack = OnyxAppPreference.Columns.FILE_EXTENSION;
            dst_content_uri = OnyxAppPreference.CONTENT_URI;
        }
        else if (match_result == MatcherResult.KEY_VALUE_ITEMS) {
            dst_table = OnyxKeyValueItem.DB_TABLE_NAME;
            dst_null_column_hack = OnyxKeyValueItem.Columns.KEY;
            dst_content_uri = OnyxKeyValueItem.CONTENT_URI;
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
        getContext().getContentResolver().notifyChange(ret, null);

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
            case MatcherResult.APP_PREFERENCES:
                count = db.delete(OnyxAppPreference.DB_TABLE_NAME, selection, selectionArgs);
                break;
            case MatcherResult.APP_PREFERENCE_ID: {
                String id = uri.getPathSegments().get(1);
                String where = OnyxAppPreference.Columns._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = where + " AND (" + selection + ")";
                }
                count = db.delete(OnyxAppPreference.DB_TABLE_NAME, where, selectionArgs);
                break;
            }
            case MatcherResult.KEY_VALUE_ITEMS:
                count = db.delete(OnyxKeyValueItem.DB_TABLE_NAME, selection, selectionArgs);
                break;
            case MatcherResult.KEY_VALUE_ITEM_ID: {
                String id = uri.getPathSegments().get(1);
                String where = OnyxKeyValueItem.Columns._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = where + " AND (" + selection + ")";
                }
                count = db.delete(OnyxKeyValueItem.DB_TABLE_NAME, where, selectionArgs);
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
            case MatcherResult.APP_PREFERENCES:
                count = db.update(OnyxAppPreference.DB_TABLE_NAME, values, selection, selectionArgs);
                break;
            case MatcherResult.APP_PREFERENCE_ID: {
                String id = uri.getPathSegments().get(1);
                String where = OnyxAppPreference.Columns._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = where + " AND (" + selection + ")";
                }
                count = db.update(OnyxAppPreference.DB_TABLE_NAME, values, where, selectionArgs);
                break;
            }
            case MatcherResult.KEY_VALUE_ITEMS:
                count = db.update(OnyxKeyValueItem.DB_TABLE_NAME, values, selection, selectionArgs);
                break;
            case MatcherResult.KEY_VALUE_ITEM_ID: {
                String id = uri.getPathSegments().get(1);
                String where = OnyxKeyValueItem.Columns._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where = where + " AND (" + selection + ")";
                }
                count = db.update(OnyxKeyValueItem.DB_TABLE_NAME, values, where, selectionArgs);
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
}
