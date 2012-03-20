/**
 * 
 */
package com.onyx.android.sdk.ui.data;

import java.util.ArrayList;

import android.util.Log;

/**
 * @author joy
 *
 */
public class GridViewPaginator
{
    private static final String TAG = "OnyxPagedAdapter";
    
    public interface OnStateChangedListener
    {
        void onStateChanged();
    }
    public interface OnPageIndexChangedListener
    {
        void onPageIndexChanged();
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
    
    private ArrayList<OnPageIndexChangedListener> mOnPageIndexChangedListenerList = new ArrayList<OnPageIndexChangedListener>();
    private void notifyPageIndexChanged()
    {
        for (OnPageIndexChangedListener l : mOnPageIndexChangedListenerList) {
            l.onPageIndexChanged();
        }
    }
    public void registerOnPageIndexChangedListener(OnPageIndexChangedListener l)
    {
        mOnPageIndexChangedListenerList.add(l);
    }
    public void unregisterOnPageIndexChangedListener(OnPageIndexChangedListener l)
    {
        mOnPageIndexChangedListenerList.remove(l);
    }
    
    // total item count
    private int mItemCount = 0;
    // number of children in a page
    private int mPageSize = 0;
    private int mPageIndex = -1;
    
    public GridViewPaginator()
    {
    }
    
    public int getItemCount()
    {
        return mItemCount;
    }
    public void setItemCount(int value)
    {
        if (value == mItemCount) {
            return;
        }
        
        mItemCount = value;
        this.notifyStateChanged();
    }
    
    public int getPageSize()
    {
        return mPageSize;
    }
    public void setPageSize(int value) 
    {
        Log.d(TAG, "setPageSize: " + value);
        if (value < 0) {
            throw new IllegalArgumentException();
        }
        
        if (value == mPageSize) {
            return;
        }
        
        if (value == 0) {
            mPageIndex = 0;
        }
        else {
            int first_item_index = mPageSize * mPageIndex;
            mPageIndex = first_item_index / value;
        }
        
        mPageSize = value;        
        this.notifyStateChanged();
        this.notifyPageIndexChanged();
    }
    
    public int getPageCount()
    {
       // dynamic calculating total page number by item count and page size
        if (this.getPageSize() <= 0) {
            return 0;
        }
        
        int page_count = this.getItemCount() / this.getPageSize();
        int mod = this.getItemCount() % this.getPageSize();
        if (mod != 0) {
            page_count++;
        }
        return page_count;
    }
    
    public int getPageIndex()
    {
        return mPageIndex;
    }
    /**
     * caller must be sure new index is in the page count range
     * 
     * @param value
     */
    public void setPageIndex(int value)
    {
        if ((value < 0) || (value >= this.getPageCount())) {
            throw new IndexOutOfBoundsException();
        }
        
        if (mPageIndex != value) {
            mPageIndex = value;
            this.notifyStateChanged();
            this.notifyPageIndexChanged();
        }
    }
    
    public int getItemCountInCurrentPage()
    {
        if (this.getPageSize() <= 0) {
            return 0;
        }
        
        if (this.canNextPage()) {
            return this.getPageSize();
        }
        else {
            if (this.getItemCount() == 0) {
                return 0;
            }

            int mod = this.getItemCount() % this.getPageSize();
            if (mod == 0) {
                mod = this.getPageSize();
            }
            return mod;
        }
    }
    
    public boolean canNextPage()
    {
        return mPageIndex < (this.getPageCount() - 1);
    }
    
    /**
     * return next page's index
     * @return
     */
    public int nextPage()
    {
        if (!this.canNextPage()) {
            return mPageIndex;
        }
        
        this.setPageIndex(mPageIndex + 1);
        return mPageIndex;
    }
    
    public boolean canPrevPage()
    {
        return mPageIndex > 0;
    }
    
    /**
     * return prev page's index
     * @return
     */
    public int prevPage()
    {
        if (!this.canPrevPage()) {
            return mPageIndex;
        }
        
        this.setPageIndex(mPageIndex - 1);
        return mPageIndex;
    }
    
    public void initializePageData(int itemCount, int pageSize)
    {
        mPageSize = pageSize;
        mItemCount = itemCount;
        mPageIndex = 0;
        
        this.notifyStateChanged();
        this.notifyPageIndexChanged();
    }

    /**
     * translating index in specified page to the index in total items 
     * @param itemIndexInPage
     * @param pageIndex
     * @return
     */
    public int getItemIndex(int itemIndexInPage, int pageIndex)
    {
        return itemIndexInPage + (this.getPageSize() * pageIndex);
    }
    
    /**
     * get index in total items
     * 
     * @param itemIndexInCurrentPage
     * @return
     */
    public int getAbsoluteIndex(int itemIndexInCurrentPage)
    {
        return itemIndexInCurrentPage + (this.getPageSize() * this.getPageIndex());
    }
}
