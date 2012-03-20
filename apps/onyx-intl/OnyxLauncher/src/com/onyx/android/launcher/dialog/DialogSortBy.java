/**
 * 
 */
package com.onyx.android.launcher.dialog;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.dialog.adapter.GridViewSortByAdapter;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.ui.OnyxGridView;

/**
 * @author joy
 *
 */
public class DialogSortBy extends OnyxDialogBase
{
    @SuppressWarnings("unused")
    private static final String TAG = "DialogSortBy";
    private Button mButtonCancel = null;
    private ImageView mImageViewPrevious = null;
    private ImageView mImageViewNext = null;
    private GridViewSortByAdapter mAdapter = null;
    private OnyxGridView mGridView = null;

    public interface OnSortByListener
    {
        public void onSortBy(SortOrder order);
    }

    private OnSortByListener mOnSortByListener = new OnSortByListener()
    {

        @Override
        public void onSortBy(SortOrder order)
        {
            // do nothing
        }
    };
    public void setOnSortByListener(OnSortByListener l)
    {
        mOnSortByListener = l;
    }

    public DialogSortBy(Context context, SortOrder[] orders)
    {
        super(context);

        this.setContentView(R.layout.dialog_sort_by);

        mGridView = (OnyxGridView)this.findViewById(R.id.gridview);
        mImageViewPrevious = (ImageView) this.findViewById(R.id.imageview_previous);
        mImageViewNext = (ImageView) this.findViewById(R.id.imageview_next);
        mButtonCancel = (Button)this.findViewById(R.id.button_cancel);
        mAdapter = new GridViewSortByAdapter(context, mGridView, orders);
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                DialogSortBy.this.dismiss();

                mOnSortByListener.onSortBy((SortOrder)view.getTag());
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                DialogSortBy.this.cancel();
            }
        });

        mAdapter.getPaginator().setPageSize(orders.length);
        mAdapter.registerDataSetObserver(new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                DialogSortBy.this.setShowPagingButton();
            }
            
            @Override
            public void onInvalidated()
            {
                DialogSortBy.this.setShowPagingButton();
            }
        });

        mImageViewPrevious.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (mAdapter.getPaginator().canPrevPage()) {
                    mAdapter.getPaginator().prevPage();
                }
            }
        });

        mImageViewNext.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                int height = mGridView.getHeight();

                if (mAdapter.getPaginator().canNextPage()) {
                    mAdapter.getPaginator().nextPage();
                }

                if (height != mGridView.getLayoutParams().height) {
                    mGridView.getLayoutParams().height = height;
                }
            }
        });
    }
    
    private void setShowPagingButton()
    {
        if (mAdapter.getPaginator().getPageCount() > 0) {
            mImageViewNext.setVisibility(View.VISIBLE);
            mImageViewPrevious.setVisibility(View.VISIBLE);
        }
        else {
            mImageViewNext.setVisibility(View.GONE);
            mImageViewPrevious.setVisibility(View.GONE);
        }
    }

}
