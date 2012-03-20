/**
 * 
 */
package com.onyx.android.sdk.ui.data;

import java.util.ArrayList;

import android.util.Log;

import com.onyx.android.sdk.ui.OnyxGridView;

/**
 * @author joy
 *
 */
public class GridViewPageLayout
{
    private static final String TAG = "GridViewPageLayout";
    
    /**
     * assuming just two Mode, be careful while adding new one 
     * 
     * @author joy
     *
     */
    public enum GridViewMode
    {
        Thumbnail, Detail,
    }
    
    public interface OnStateChangedListener
    {
        void onStateChanged();
    }
    
    private ArrayList<OnStateChangedListener> mOnStateChangedListenerList = new ArrayList<OnStateChangedListener>();
    private void notifyStateChanged()
    {
        for (OnStateChangedListener l : mOnStateChangedListenerList) {
            l.onStateChanged();
        }
    }
    public void registerOnStateChangedListener(OnStateChangedListener l)
    {
        mOnStateChangedListenerList.add(l);
    }
    public void unregisterOnStateChangedListener(OnStateChangedListener l)
    {
        mOnStateChangedListenerList.remove(l);
    }
    
    /**
     * predefined item's basic layout properties
     */
    private int mItemMinWidth = 100;
    private int mItemMinHeight = 100;
    private int mItemThumbnailMinHeight = 100;
    private int mItemDetailMinHeight = 70;
    private int mHorizontalSpacing = 0;
    private int mVerticalSpacing = 0;
    
    /**
     * runtime item's layout properties calculated by {@linkplain onGridViewSizeChanged}
     */
    private int mItemCurrentWidth = 0;
    private int mItemCurrentHeight = 0;
    private int mLayoutColumnCount = 0;
    private int mLayoutRowCount = 0;
    
    private GridViewMode mViewMode = GridViewMode.Thumbnail;
    
    /**
     * depends on GridView's size to do layout
     */
    private OnyxGridView mGridView = null;
    
    public GridViewPageLayout(OnyxGridView gridView)
    {
        mGridView = gridView;
        mGridView.registerOnSizeChangedListener(new OnyxGridView.OnSizeChangedListener()
        {
            
            @Override
            public void onSizeChanged()
            {
                GridViewPageLayout.this.setupLayout(mGridView);
            }
        });
        
        this.setupLayout(mGridView);
    }
    
    public int getItemMinWidth()
    {
        return mItemMinWidth;
    }
    public void setItemMinWidth(int value)
    {
        mItemMinWidth = value;
    }
    
    public int getItemMinHeight()
    {
        return mItemMinHeight;
    }
    public void setItemMinHeight(int value)
    {
        mItemMinHeight = value;
    }
    
    public int getItemThumbnailMinHeight()
    {
        return mItemThumbnailMinHeight;
    }
    public void setItemThumbnailMinHeight(int value)
    {
        mItemThumbnailMinHeight = value;
    }
    
    public int getItemDetailMinHeight()
    {
        return mItemDetailMinHeight;
    }
    public void setItemDetailMinHeight(int value)
    {
        mItemDetailMinHeight = value;
    }
    
    public int getHorizontalSpacing()
    {
        return mHorizontalSpacing;
    }
    public void setHorizontalSpacing(int value)
    {
        mHorizontalSpacing = value;
    }
    
    public int getVerticalSpacing()
    {
        return mVerticalSpacing;
    }
    public void setVerticalSpacing(int value)
    {
        mVerticalSpacing = value;
    }
    
    public int getItemCurrentWidth()
    {
        return mItemCurrentWidth;
    }
    
    public int getItemCurrentHeight()
    {
        return mItemCurrentHeight;
    }
    
    public int getLayoutColumnCount()
    {
        return mLayoutColumnCount;
    }
    
    public int getLayoutRowCount()
    {
        return mLayoutRowCount;
    }
    
    public GridViewMode getViewMode()
    {
        return mViewMode;
    }
    public void setViewMode(GridViewMode viewMode)
    {
        if (mViewMode == viewMode) {
            return;
        }
        
        mViewMode = viewMode;
        
        if (mViewMode == GridViewMode.Thumbnail) {
            mItemMinHeight = mItemThumbnailMinHeight;
        }
        else if (mViewMode == GridViewMode.Detail) {
            mItemMinHeight = mItemDetailMinHeight;
        }
        else {
            assert(false);
            throw new IllegalArgumentException();
        }
        
        this.setupLayout(mGridView);
    }
    
    /**
     * items' layout depending both on gridview's size and item's min_width, min_height and view mode,
     * so need to re-layout every time when gridview's size changed
     * 
     * @param gridView
     */
    public void setupLayout(OnyxGridView gridView)
    {
        if ((gridView.getHeight() == 0) || (gridView.getWidth() == 0)) {
            return;
        }

        final int items_region_width = gridView.getWidth() - gridView.getListPaddingLeft() - gridView.getListPaddingRight();
        final int items_region_height = gridView.getHeight() - gridView.getListPaddingTop() - gridView.getListPaddingBottom();

        if (mViewMode == GridViewMode.Detail) {
            mLayoutColumnCount = 1;
            mItemMinHeight = mItemDetailMinHeight;
        }
        else {
            assert(mViewMode == GridViewMode.Thumbnail);
            mLayoutColumnCount = ((items_region_width - mItemMinWidth) / (mItemMinWidth + mHorizontalSpacing)) + 1; 
            mItemMinHeight = mItemThumbnailMinHeight;
        }
        
        mLayoutRowCount = ((items_region_height - mItemMinHeight) / (mItemMinHeight + mVerticalSpacing)) + 1;
        
        if ((mLayoutColumnCount == 0) || (mLayoutRowCount == 0)) {
            return;
        }
        
        mItemCurrentWidth = (items_region_width - (mLayoutColumnCount - 1) * mHorizontalSpacing) / mLayoutColumnCount;
        mItemCurrentHeight = (items_region_height - (mLayoutRowCount - 1) * mVerticalSpacing) / mLayoutRowCount;
        
        gridView.setVerticalSpacing(mVerticalSpacing);
        
        // when we set column width and horizontal spacing, and has setNumColumns() to be AUTO_FIT, 
        // then there is no need to explicitly call setNumColumns(), all can be automatic handled by Android,
        // sometimes more is worse, we will have problem if inappropriately call setNumColumns() here
        gridView.setHorizontalSpacing(mHorizontalSpacing);
        gridView.setColumnWidth(mItemCurrentWidth);

        Log.d(TAG, "gridView: (" + gridView.getWidth() + ", " + gridView.getHeight() + 
                "), region: (" + items_region_width + ", " + items_region_height +
                "), page row: " + mLayoutRowCount + ", column:" + mLayoutColumnCount);
        Log.d(TAG, "current min width: " + mItemMinWidth + ", column width: " + mItemCurrentWidth + 
                ", horizontal spacing: " + mHorizontalSpacing);
        Log.d(TAG, "current min height: " + mItemMinHeight + ", row height: " + mItemCurrentHeight + 
                ", horizontal spacing: " + mVerticalSpacing);

        this.notifyStateChanged();
    }
}
