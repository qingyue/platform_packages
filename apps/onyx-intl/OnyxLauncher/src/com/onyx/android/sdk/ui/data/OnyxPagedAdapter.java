/**
 * 
 */
package com.onyx.android.sdk.ui.data;

import android.widget.BaseAdapter;

import com.onyx.android.sdk.ui.OnyxGridView;

/**
 * @author joy
 *
 */
public abstract class OnyxPagedAdapter extends BaseAdapter
{
    @SuppressWarnings("unused")
    private static final String TAG = "OnyxPagedAdapter";
    
    private OnyxGridView mGridView = null;
    private GridViewPaginator mPaginator = new GridViewPaginator();
    private GridViewPageLayout mPageLayout = null;
    
    public OnyxPagedAdapter(OnyxGridView gridView)
    {
        mGridView = gridView;
        mPageLayout = new GridViewPageLayout(gridView);
        
        mPageLayout.registerOnStateChangedListener(new GridViewPageLayout.OnStateChangedListener()
        {
            
            @Override
            public void onStateChanged()
            {
                int size = mPageLayout.getLayoutRowCount() * mPageLayout.getLayoutColumnCount();
                if (mPaginator.getPageSize() != size) {
                    mPaginator.setPageSize(size);
                }
            }
        });
        
        mPaginator.registerOnStateChangedListener(new GridViewPaginator.OnStateChangedListener()
        {
            
            @Override
            public void onStateChanged()
            {
                OnyxPagedAdapter.this.notifyDataSetChanged();
            }
        });

        mPaginator.registerOnPageIndexChangedListener(new GridViewPaginator.OnPageIndexChangedListener()
        {

            @Override
            public void onPageIndexChanged()
            {
                if (OnyxPagedAdapter.this.getGridView().getChildCount() > 0) {
                    OnyxPagedAdapter.this.getGridView().setSelection(0);
                }
            }
        });
    }
    
    @Override
    public int getCount()
    {
        return mPaginator.getItemCountInCurrentPage();
    }
    
    public OnyxGridView getGridView()
    {
        return mGridView;
    }
    
    public GridViewPaginator getPaginator()
    {
        return mPaginator;
    }
    
    public GridViewPageLayout getPageLayout()
    {
        return mPageLayout;
    }
    
}
