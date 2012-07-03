/**
 * 
 */
package com.onyx.android.launcher.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.onyx.android.launcher.OnyxApplication;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.SelectionAdapter;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * @author joy
 *
 */
public class DialogBaseSettings extends OnyxDialogBase
{
    private Button mButtonPreviousPage = null;
    private Button mButtonNextPage = null;
    private Button mButtonSet = null;
    private Button mButtonCancel = null;
    private TextView mTextViewProgress = null;
    private TextView mTextViewTitle = null;
    private OnyxGridView mGridView = null;
    private SelectionAdapter mAdapter = null;
    private View mView = null;
    
    private static final int sUnselection = -1;

    public DialogBaseSettings(Context context)
    {
        super(context);

        mView = getLayoutInflater().inflate(R.layout.dialog_settings_selection_template, null);
        this.setContentView(mView);
        
        mButtonPreviousPage = (Button) this.findViewById(R.id.button_previous_dialogpaged);
        mButtonNextPage = (Button) this.findViewById(R.id.button_next_dialogpaged);
        mButtonSet = (Button) this.findViewById(R.id.button_set_dialogpaged);
        mButtonCancel = (Button) this.findViewById(R.id.button_cancel_dialogpaged);
        mTextViewProgress = (TextView) this.findViewById(R.id.textview_paged_dialogpaged);
        mTextViewTitle = (TextView) this.findViewById(R.id.textview_title);
        mGridView = (OnyxGridView) this.findViewById(R.id.gridview_dialogpaged);

        mButtonPreviousPage.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (mGridView.getPagedAdapter().getPaginator().canPrevPage()) {
                    mGridView.getPagedAdapter().getPaginator().prevPage();
                }
            }
        });

        mButtonNextPage.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                int height = mGridView.getHeight();

                if (mGridView.getPagedAdapter().getPaginator().canNextPage()) {
                    mGridView.getPagedAdapter().getPaginator().nextPage();
                }

                if (height != mGridView.getLayoutParams().height) {
                    mGridView.getLayoutParams().height = height;
                }
            }
        });

        mGridView.registerOnAdapterChangedListener(new OnyxGridView.OnAdapterChangedListener()
        {

            @Override
            public void onAdapterChanged()
            {
                mAdapter = (SelectionAdapter) mGridView.getAdapter();
                DialogBaseSettings.this.setSelection(mAdapter.getSelection());

                mAdapter.registerDataSetObserver(new DataSetObserver()
                {
                    @Override
                    public void onChanged()
                    {
                        DialogBaseSettings.this.updateTextViewProgress();
                        ScreenUpdateManager.invalidate(mView, UpdateMode.GU);
                    }

                    @Override
                    public void onInvalidated()
                    {
                        DialogBaseSettings.this.updateTextViewProgress();
                        ScreenUpdateManager.invalidate(mView, UpdateMode.GU);
                    }
                });
            }
        });

        mGridView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                int selection = (Integer) view.getTag();

                int idx = mGridView.getPagedAdapter().getPaginator().getAbsoluteIndex(position);

                if (selection == idx) {
                    DialogBaseSettings.this.setSelection(sUnselection);
                }
                else {
                    DialogBaseSettings.this.setSelection(idx);
                }
            }
        });

    }

    public TextView getTextViewTitle()
    {
        return mTextViewTitle;
    }

    public OnyxGridView getGridView()
    {
        return mGridView;
    }
    
    public Button getButtonSet()
    {
        return mButtonSet;
    }

    public Button getButtonCancel()
    {
        return mButtonCancel;
    }
    
    private void updateTextViewProgress()
    {
        final int current_page = mGridView.getPagedAdapter().getPaginator().getPageIndex() + 1;
        final int page_count = (mGridView.getPagedAdapter().getPaginator().getPageCount() != 0) ? 
                mGridView.getPagedAdapter().getPaginator().getPageCount() : 1;

                mTextViewProgress.setText(String.valueOf(current_page) + OnyxApplication.getInstance().getResources().getString(R.string.slahs) + String.valueOf(page_count));
    }

    private void setSelection(int position)
    {
        mAdapter.setSelection(position);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	ScreenUpdateManager.invalidate(mView, UpdateMode.GU);
    	return super.onKeyDown(keyCode, event);
    }
}
