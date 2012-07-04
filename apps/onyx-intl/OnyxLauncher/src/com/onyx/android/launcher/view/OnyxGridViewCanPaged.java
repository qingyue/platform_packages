package com.onyx.android.launcher.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.OnyxGridView.OnAdapterChangedListener;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;


public class OnyxGridViewCanPaged extends RelativeLayout {
	private OnyxGridView mGridView = null;
	private Button mButtonPrev = null;
	private Button mButtonNext = null;

	private boolean mButtonIsVisible = true;
	
	public OnyxGridViewCanPaged(Context context) {
		super(context, null);
	}

	public OnyxGridViewCanPaged(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.init(context);
    }

	private void init(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.gridview_can_page, null);
		mGridView = (OnyxGridView) view.findViewById(R.id.gridview);
		mButtonNext = (Button) view.findViewById(R.id.button_next_page);
		mButtonPrev = (Button) view.findViewById(R.id.button_prev_page);

        mGridView.registerOnAdapterChangedListener(new OnAdapterChangedListener() {
			
			@Override
			public void onAdapterChanged() {
				mGridView.getPagedAdapter().registerDataSetObserver(new DataSetObserver() {
					@Override
					public void onChanged() {
						if (mGridView.getPagedAdapter() != null && 
								mGridView.getPagedAdapter().getPaginator().getPageCount() > 1) {
							mButtonNext.setVisibility(View.VISIBLE);
							mButtonPrev.setVisibility(View.VISIBLE);
							mButtonIsVisible = true;
						}
						else {
							mButtonNext.setVisibility(View.GONE);
							mButtonPrev.setVisibility(View.GONE);
							mButtonIsVisible = false;
						}

						if (mButtonIsVisible) {
							ScreenUpdateManager.invalidate(mButtonPrev, UpdateMode.GU);
							if (mGridView.getPagedAdapter().getPaginator().canPrevPage()) {
								if (!mButtonPrev.isEnabled()) {
									mButtonPrev.setEnabled(true);
								}
							}
							else {
								if (mButtonPrev.isEnabled()) {	
									mButtonPrev.setEnabled(false);
								}
							}

							if (mGridView.getPagedAdapter().getPaginator().canNextPage()) {
								if (!mButtonNext.isEnabled()) {
									ScreenUpdateManager.invalidate(mButtonNext, UpdateMode.GU);
									mButtonNext.setEnabled(true);
								}
							}
							else {
								if (mButtonNext.isEnabled()) {
									ScreenUpdateManager.invalidate(mButtonNext, UpdateMode.GU);
									mButtonNext.setEnabled(false);
								}
							}
						}
					}
				});
			}
		});

        mButtonNext.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mGridView.getPagedAdapter().getPaginator().canNextPage()) {
					mGridView.getPagedAdapter().getPaginator().nextPage();
					mButtonNext.requestFocusFromTouch();
				}
			}
		});

        mButtonPrev.setOnClickListener(new View.OnClickListener() {

        	@Override
        	public void onClick(View v) {
        		if (mGridView.getPagedAdapter().getPaginator().canPrevPage()) {
        			mGridView.getPagedAdapter().getPaginator().prevPage();
        			mButtonPrev.requestFocusFromTouch();
        		}
        	}
        });

        this.addView(view);
	}

	public OnyxGridView getGridView() {
		return mGridView;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		ScreenUpdateManager.invalidate(this, UpdateMode.DW);
		return super.onKeyDown(keyCode, event);
	}
}
