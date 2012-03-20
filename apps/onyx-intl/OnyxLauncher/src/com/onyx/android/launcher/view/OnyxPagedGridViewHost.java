/**
 * 
 */
package com.onyx.android.launcher.view;

import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.dialog.DialogPageSeekBar;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.OnyxPagedAdapter;

/**
 * custom control of a OnyxGridView with page navigation buttons
 * 
 * @author joy
 *
 */
public class OnyxPagedGridViewHost extends LinearLayout
{
    private Context mContext = null;
    
    private OnyxGridView mGridView = null;
    private Button mButtonProgress = null;
    private Button mButtonPreviousPage = null;
    private Button mButtonNextPage = null;
    
    private DialogPageSeekBar mDialogPageSeekBar = null;

    public OnyxPagedGridViewHost(Context context)
    {
        this(context, null);
    }

    public OnyxPagedGridViewHost(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        
        this.init(context);
    }
    
    public OnyxGridView getGridView()
    {
        return mGridView;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        if (mDialogPageSeekBar != null && mDialogPageSeekBar.isShowing()) {
            Window windowDialogPageSeekBar = mDialogPageSeekBar.getWindow();
            WindowManager.LayoutParams params = windowDialogPageSeekBar.getAttributes();

            params.width = windowDialogPageSeekBar.getWindowManager().getDefaultDisplay().getWidth();
            params.y = windowDialogPageSeekBar.getWindowManager().getDefaultDisplay().getHeight();

            windowDialogPageSeekBar.setAttributes(params);
        }
    }
    
    private void init(Context context)
    {
        mContext = context;
        
        View view = LayoutInflater.from(context).inflate(R.layout.control_paged_gridview, null); 
        
        mGridView = (OnyxGridView)view.findViewById(R.id.gridview_content);
        mButtonProgress = (Button)view.findViewById(R.id.button_progress);
        mButtonPreviousPage = (Button)view.findViewById(R.id.button_previous_page);
        mButtonNextPage = (Button)view.findViewById(R.id.button_next_page);

        mGridView.registerOnAdapterChangedListener(new OnyxGridView.OnAdapterChangedListener()
        {
            
            @Override
            public void onAdapterChanged()
            {
                final OnyxPagedAdapter adapter = mGridView.getPagedAdapter();

                adapter.registerDataSetObserver(new DataSetObserver()
                {
                    @Override
                    public void onChanged()
                    {
                        OnyxPagedGridViewHost.this.updatemTextViewProgress();
                    }

                    @Override
                    public void onInvalidated()
                    {
                        OnyxPagedGridViewHost.this.updatemTextViewProgress();
                    }
                });
            }
        });
        
        mButtonProgress.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                mDialogPageSeekBar = new DialogPageSeekBar(OnyxPagedGridViewHost.this.mContext,
                        mGridView.getPagedAdapter());
                mDialogPageSeekBar.show();
            }
        });
        
        mButtonPreviousPage.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                if (mGridView.getPagedAdapter().getPaginator().canPrevPage()) {
                    mGridView.getPagedAdapter().getPaginator().prevPage();
                }
                mButtonPreviousPage.setFocusable(true);
                mButtonPreviousPage.setFocusableInTouchMode(true);
                mButtonPreviousPage.requestFocus();
            }
        });
        
        mButtonNextPage.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                if (mGridView.getPagedAdapter().getPaginator().canNextPage()) {
                    mGridView.getPagedAdapter().getPaginator().nextPage();
                }
                mButtonNextPage.setFocusable(true);
                mButtonNextPage.setFocusableInTouchMode(true);
                mButtonNextPage.requestFocus();
            }
        });

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        this.addView(view, params);
    }
    
    private void updatemTextViewProgress()
    {
        final int current_page = mGridView.getPagedAdapter().getPaginator().getPageIndex() + 1;
        final int page_count = (mGridView.getPagedAdapter().getPaginator().getPageCount() != 0) ? 
                mGridView.getPagedAdapter().getPaginator().getPageCount() : 1;

        mButtonProgress.setText(String.valueOf(current_page) + "/" + String.valueOf(page_count));
    }
    

}
