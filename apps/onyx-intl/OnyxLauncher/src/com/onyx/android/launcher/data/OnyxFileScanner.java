/**
 * 
 */
package com.onyx.android.launcher.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.onyx.android.sdk.data.cms.OnyxLibraryItem;

/**
 * @author joy
 *
 */
public class OnyxFileScanner
{
    private static final String TAG = "OnyxFileScanner";
    
    private Context mContext = null;
    
    public OnyxFileScanner(Context context)
    {
        mContext = context;
    }
    
    public synchronized void scanDirectories(String[] directories)
    {
        Log.d(TAG, "scanDirectories starting");
        
        long start = System.currentTimeMillis();
        
        ArrayList<ContentValues> value_collection = new ArrayList<ContentValues>();
        
        Log.d(TAG, "start iterating directories");
        for (String d : directories) {
            Log.d(TAG, "target directory: " + d);
            
            // clear old data
            Log.d(TAG, "clear old data: " + d);
            String where = OnyxLibraryItem.Columns.PATH + " like '" + d + "%'";
            mContext.getContentResolver().delete(OnyxLibraryItem.CONTENT_URI, where, null);
            
            File dir = new File(d);
            if (!dir.exists()) {
                Log.d(TAG, "directory not exist: " + d);
                continue;
            }
            
            Log.d(TAG, "scanning directory: " + d);
            this.scanDirectoryHelper(dir, value_collection);
            Log.d(TAG, "scanning directory finished");
        }
        
        Log.d(TAG, "iterating directories finished");
        ContentValues[] value_array = new ContentValues[value_collection.size()];
        value_collection.toArray(value_array);
        
        Log.d(TAG, "insert scanning result");
        mContext.getContentResolver().bulkInsert(OnyxLibraryItem.CONTENT_URI, value_array);
        
        long end = System.currentTimeMillis();
        
        Log.d(TAG, "scanDirectories finished");
        Log.d(TAG, "item count: " + value_collection.size() + "\n");
        Log.d(TAG, "scan time: " + (end - start) + "ms\n");
    }
    
    private void scanDirectoryHelper(File dir, Collection<ContentValues> valueCollection)
    {
        File[] files = dir.listFiles();
        if (files == null) {
            Log.d(TAG, "list directory files failed: " + dir.getAbsolutePath());
            return;
        }
        
        ArrayList<File> sub_dirs = new ArrayList<File>();
        
        for (File f : files) {
            if (f.isHidden()) {
                continue;
            }
            
            if (f.isDirectory()) {
                sub_dirs.add(f);
            }
            else if (f.isFile()) {
                valueCollection.add(OnyxLibraryItem.Columns.createColumnData(f));
            }
            else {
                assert(false);
                continue;
            }
        }
        
        for (File d : sub_dirs) {
            this.scanDirectoryHelper(d, valueCollection);
        }
    }
}
