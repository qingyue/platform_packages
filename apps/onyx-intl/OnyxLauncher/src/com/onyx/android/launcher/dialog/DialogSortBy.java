/**
 * 
 */
package com.onyx.android.launcher.dialog;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.dialog.adapter.GridViewSortByAdapter;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.data.AscDescOrder;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

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
    private View mView = null;
    private RadioGroup mRadioGroup = null;
    private RadioButton mRadioButtonAsc;
    private RadioButton mRadioButtonDesc;
    private AscDescOrder mAscOrder = AscDescOrder.Asc;

    private SortOrder mOrder = null;

    public interface OnSortByListener
    {
        public void onSortBy(SortOrder order, AscDescOrder mAscOrder);
    }

    private OnSortByListener mOnSortByListener = new OnSortByListener()
    {

        @Override
        public void onSortBy(SortOrder order, AscDescOrder mAscOrder)
        {
            // do nothing
        }
    };

    public void setOnSortByListener(OnSortByListener l)
    {
        mOnSortByListener = l;
    }

    public DialogSortBy(Context context, SortOrder[] orders, SortOrder order, AscDescOrder ascDesc)
    {
        super(context);

        mView = getLayoutInflater().inflate(R.layout.dialog_sort_by, null);
        this.setContentView(mView);

        mGridView = (OnyxGridView) this.findViewById(R.id.gridview);
        mImageViewPrevious = (ImageView) this
                .findViewById(R.id.imageview_previous);
        mImageViewNext = (ImageView) this.findViewById(R.id.imageview_next);
        mButtonCancel = (Button) this.findViewById(R.id.button_cancel);
        mAdapter = new GridViewSortByAdapter(context, mGridView, orders);
        mGridView.setAdapter(mAdapter);
        mRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroupOrder);
        mRadioButtonAsc = (RadioButton) this.findViewById(R.id.radioAsc);
        mRadioButtonDesc = (RadioButton) this.findViewById(R.id.radioDesc);
        if(ascDesc == AscDescOrder.Asc){
        	mRadioButtonAsc.setChecked(true);
        }else{
        	mRadioButtonDesc.setChecked(true);
        }
        mAscOrder = ascDesc;
        mOrder = order;
        mRadioGroup
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                {

                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId)
                    {
                        switch (checkedId) {
                        case R.id.radioAsc:
                        	mAscOrder = AscDescOrder.Asc;
                            break;
                        case R.id.radioDesc:
                        	mAscOrder = AscDescOrder.Desc;
                            break;
                        default:
                            break;
                        }
                    }
                });

        mGridView.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                DialogSortBy.this.dismiss();
                mOnSortByListener.onSortBy((SortOrder) view.getTag(),
                        (AscDescOrder) mAscOrder);
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
        } else {
            mImageViewNext.setVisibility(View.GONE);
            mImageViewPrevious.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        ScreenUpdateManager.invalidate(mView, UpdateMode.GU);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);

      DialogSortBy.this.getWindow().getDecorView().requestFocusFromTouch();
      switch(mOrder) {
      case Name :
      	mGridView.setSelection(0);
      	break;
      case FileType :
      	mGridView.setSelection(1);
      	break;
      case Size :
      	mGridView.setSelection(2);
      	break;
      case AccessTime :
      	mGridView.setSelection(3);
      	break;
      }
    }
}
