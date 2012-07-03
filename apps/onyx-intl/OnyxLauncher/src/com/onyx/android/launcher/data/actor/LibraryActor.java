/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.onyx.android.launcher.LibraryActivity;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.data.CmsCenterHelper;
import com.onyx.android.launcher.util.EventedArrayList;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.data.util.ActivityUtil;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class LibraryActor extends ItemContainerActor
{
    private static final String TAG = "LibraryActor";
    
    public LibraryActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(((OnyxItemURI)parentURI.clone()).append("Library"), 
                R.string.library, 
                R.drawable.books));
    }
    
    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri,
            Activity hostActivity)
    {
        Intent intent = new Intent(hostActivity, LibraryActivity.class);
        return ActivityUtil.startActivitySafely(hostActivity, intent);
    }
    
    @Override
    public boolean doSearch(OnyxGridView gridView, OnyxItemURI uri,
            String pattern)
    {
        // TODO Auto-generated method stub
        return super.doSearch(gridView, uri, pattern);
    }
    
    @Override
    public boolean search(Activity hostActivity, OnyxItemURI uri, String pattern, EventedArrayList<GridItemData> result)
    {
        Cursor cursor = null;
        try {
            Log.d(TAG, "loading library items");

            long time_start = System.currentTimeMillis();
            
            CmsCenterHelper.searchLibraryItem(hostActivity, pattern, result);
            
            long time_end = System.currentTimeMillis();

            Log.d(TAG, "items loaded, count: " + result.size());
            Log.d(TAG, "total time: " + (time_end - time_start) + "ms\n");
            
            return true;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
