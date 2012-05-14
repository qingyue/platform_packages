/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import android.app.Activity;

import com.onyx.android.launcher.util.EventedArrayList;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public abstract class AbstractItemActor
{
    public interface OnSearchProgressed
    {
        public void onSearchProgressed();
    }
    
	// initialize to avoid null checking
    protected OnSearchProgressed mOnSearchProgressed = new OnSearchProgressed()
    {
        
        @Override
        public void onSearchProgressed()
        {
            // do nothing
        }
    };
    public void setOnSearchProgressedListener(OnSearchProgressed l)
    {
        mOnSearchProgressed = l;
    }
    
    private GridItemData mItemData = null;
    
    public AbstractItemActor(GridItemData itemData)
    {
        mItemData = itemData;
    }
    
    public GridItemData getData()
    {
        assert(mItemData != null);
        return mItemData;
    }
    
    /**
     * sometimes we need to know this, to make a different decision,
     * you can find all references to see where this is used
     * 
     * @param uri
     * @return
     */
    public boolean isItemContainer(OnyxItemURI uri)
    {
        return false;
    }
    
    public boolean process(OnyxGridView gridView, OnyxItemURI uri, Activity hostActivity)
    {
        return false;
    }
    public boolean doSearch(OnyxGridView gridView, OnyxItemURI uri, String pattern)
    {
        return false;
    }
    /**
     * search may take a long time, so use EventedArrayList to get intermediate result
     * 
     * @param hostActivity
     * @param uri
     * @param pattern
     * @param result
     * @return
     */
    public boolean search(Activity hostActivity, OnyxItemURI uri, String pattern, EventedArrayList<GridItemData> result)
    {
        return false;
    }
}
