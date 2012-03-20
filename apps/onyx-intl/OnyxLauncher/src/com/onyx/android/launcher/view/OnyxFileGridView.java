/**
 * 
 */
package com.onyx.android.launcher.view;

import java.util.ArrayList;

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

import com.onyx.android.launcher.OnyxBaseActivity;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.data.CopyService;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.dialog.DialogPageSeekBar;
import com.onyx.android.launcher.task.CopyFileTask;
import com.onyx.android.launcher.task.CutFileTask;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.FileItemData;
import com.onyx.android.sdk.ui.data.OnyxPagedAdapter;

/**
 * custom gridview control supporting file operation 
 * 
 * @author joy
 *
 */
public class OnyxFileGridView extends LinearLayout
{
    private Context mContext = null;
    
    private OnyxGridView mGridView = null;
    private Button mButtonProgress = null;
    private Button mButtonPreviousPage = null;
    private Button mButtonNextPage = null;
    private Button mButtonPaste = null;
    private Button mButtonCopy = null;
    private Button mButtonCut = null;
    private Button mButtonDelete = null;
    private Button mButtonCancel = null;
    private DialogPageSeekBar mDialogPageSeekBar = null;

    private FileOperationHandler mFileOperationHandler = null;

    private boolean mCanPaste = false;

    public OnyxFileGridView(Context context)
    {
        this(context, null);
    }

    public OnyxFileGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        
        this.init(context);
    }
    
    public OnyxGridView getGridView()
    {
        return mGridView;
    }
    
    public void onPrepareMultipleSelection()
    {
        GridItemBaseAdapter adapter = (GridItemBaseAdapter) mGridView.getPagedAdapter();
        adapter.setMultipleSelectionMode(true);
        mButtonCancel.setVisibility(View.VISIBLE);
        mButtonDelete.setVisibility(View.VISIBLE);
        mButtonCopy.setVisibility(View.VISIBLE);
        mButtonCut.setVisibility(View.VISIBLE);
    }
    
    public void onPreparePaste()
    {
        if (mButtonPaste.getVisibility() == View.GONE) {
            mButtonPaste.setVisibility(View.VISIBLE);
            if (mCanPaste) {
                mButtonPaste.setEnabled(true);
            }
            else {
                mButtonPaste.setEnabled(false);
            }
        }

        if (mButtonCancel.getVisibility() == View.GONE) {
            mButtonCancel.setVisibility(View.VISIBLE);
        }
    }
    
    public void onCancelMultipleSelection()
    {
        GridItemBaseAdapter adapter = (GridItemBaseAdapter) mGridView.getPagedAdapter();
        adapter.cleanSelectedItems();
        adapter.setMultipleSelectionMode(false);
        mButtonCancel.setVisibility(View.GONE);
        mButtonCopy.setVisibility(View.GONE);
        mButtonCut.setVisibility(View.GONE);
        mButtonDelete.setVisibility(View.GONE);

        if (mButtonPaste.getVisibility() == View.VISIBLE) {
            mButtonPaste.setVisibility(View.GONE);
        }
    }

    public void setCanPaste(boolean canPaste)
    {
        this.mCanPaste = canPaste;
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
        
        View view = LayoutInflater.from(mContext).inflate(R.layout.control_file_gridview, null); 
        
        mGridView = (OnyxGridView)view.findViewById(R.id.gridview_content);
        mButtonProgress = (Button)view.findViewById(R.id.button_progress);
        mButtonPreviousPage = (Button)view.findViewById(R.id.button_previous_page);
        mButtonNextPage = (Button)view.findViewById(R.id.button_next_page);
        mButtonPaste = (Button)view.findViewById(R.id.button_paste);
        mButtonCopy = (Button)view.findViewById(R.id.button_copy);
        mButtonCut = (Button)view.findViewById(R.id.button_cut);
        mButtonDelete = (Button)view.findViewById(R.id.button_delete);
        mButtonCancel = (Button)view.findViewById(R.id.button_cancel);

        mGridView.registerOnAdapterChangedListener(new OnyxGridView.OnAdapterChangedListener()
        {
            
            @Override
            public void onAdapterChanged()
            {
                OnyxPagedAdapter adapter = mGridView.getPagedAdapter();
                adapter.registerDataSetObserver(new DataSetObserver()
                {
                    @Override
                    public void onChanged()
                    {
                        OnyxFileGridView.this.updatemTextViewProgress();
                    }

                    @Override
                    public void onInvalidated()
                    {
                        OnyxFileGridView.this.updatemTextViewProgress();
                    }
                });
            }
        });
        
        mButtonProgress.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                mDialogPageSeekBar = new DialogPageSeekBar(OnyxFileGridView.this.mContext,
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

        mButtonCancel.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            { 
                CopyService.clean();

                if (((GridItemBaseAdapter) mGridView.getPagedAdapter()).getMultipleSelectionMode() == true) {
                    ((GridItemBaseAdapter) mGridView.getPagedAdapter()).setMultipleSelectionMode(false);
                }
                ((GridItemBaseAdapter) mGridView.getPagedAdapter()).cleanSelectedItems();

                if (mButtonCancel.getVisibility() == View.VISIBLE) {
                    mButtonCancel.setVisibility(View.GONE);
                }
                
                if (mButtonPaste.getVisibility() == View.VISIBLE) {
                    mButtonPaste.setVisibility(View.GONE);
                }
                
                if (mButtonCopy.getVisibility() == View.VISIBLE) {
                    mButtonCopy.setVisibility(View.GONE);
                }
                
                if (mButtonCut.getVisibility() == View.VISIBLE) {
                    mButtonCut.setVisibility(View.GONE);
                }
                
                if (mButtonDelete.getVisibility() == View.VISIBLE) {
                    mButtonDelete.setVisibility(View.GONE);
                }
            }
        });

        mButtonCopy.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                if (((GridItemBaseAdapter) mGridView.getPagedAdapter()).getSelectedItems().size() > 0) {
                    OnyxFileGridView.this.setFileOperationsHandlerSourceItems();
                    mFileOperationHandler.onCopy();
                    OnyxFileGridView.this.finishMultipleSelection();
                    OnyxFileGridView.this.showPasteButton();
                }
            }
        });

        mButtonCut.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (((GridItemBaseAdapter) mGridView.getPagedAdapter()).getSelectedItems().size() > 0) {
                    OnyxFileGridView.this.setFileOperationsHandlerSourceItems();
                    mFileOperationHandler.onCut();
                    OnyxFileGridView.this.finishMultipleSelection();
                    OnyxFileGridView.this.showPasteButton();
                }
            }
        });

        mButtonDelete.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (((GridItemBaseAdapter) mGridView.getPagedAdapter()).getSelectedItems().size() > 0) {

                    CopyService.clean();

                    if (mFileOperationHandler.getAdapter() == null) {
                        mFileOperationHandler = new FileOperationHandler(mContext, (GridItemBaseAdapter)mGridView.getPagedAdapter());
                    }
                    OnyxFileGridView.this.setFileOperationsHandlerSourceItems();
                    mFileOperationHandler.onRemove();
                    ((GridItemBaseAdapter) mGridView.getPagedAdapter()).cleanSelectedItems();
                    OnyxFileGridView.this.finishMultipleSelection();
                    mButtonCancel.setVisibility(View.GONE);
                }
            }
        });

        mButtonPaste.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                mButtonPaste.setVisibility(View.GONE);
                mButtonCancel.setVisibility(View.GONE);

                if (!CopyService.isCut()) {
                    CopyFileTask copyFileTask = new CopyFileTask((OnyxBaseActivity)mContext, CopyService.getSourceItems());
                    copyFileTask.execute();
                }
                else {
                    CutFileTask cutFileTask = new CutFileTask((OnyxBaseActivity)mContext, CopyService.getSourceItems());
                    cutFileTask.execute();
                }
                CopyService.clean();
            }
        });

        mFileOperationHandler = new FileOperationHandler(mContext, (GridItemBaseAdapter)mGridView.getPagedAdapter());

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        this.addView(view, params);
    }
    
    private void updatemTextViewProgress()
    {
        final int current_page = mGridView.getPagedAdapter().getPaginator().getPageIndex() + 1;
        final int page_count = mGridView.getPagedAdapter().getPaginator().getPageCount() != 0 ? 
                mGridView.getPagedAdapter().getPaginator().getPageCount() : 1;

        mButtonProgress.setText(String.valueOf(current_page) + "/" + String.valueOf(page_count));
    }

    private void setFileOperationsHandlerSourceItems()
    {
        ArrayList<FileItemData> items = new ArrayList<FileItemData>();
        for (int i = 0; i < ((GridItemBaseAdapter) mGridView.getPagedAdapter()).getSelectedItems().size(); i++) {
            items.add((FileItemData)((GridItemBaseAdapter) mGridView.getPagedAdapter()).getSelectedItems().get(i));
        }
        mFileOperationHandler.setSourceItems(items);
    }

    private void finishMultipleSelection()
    {
        ((GridItemBaseAdapter) mGridView.getPagedAdapter()).setMultipleSelectionMode(false);
        mButtonCopy.setVisibility(View.GONE);
        mButtonCut.setVisibility(View.GONE);
        mButtonDelete.setVisibility(View.GONE);
    }

    private void showPasteButton()
    {
        if (mCanPaste) {
            if (mButtonPaste.getVisibility() == View.GONE) {
                mButtonPaste.setVisibility(View.VISIBLE);
            }
        }
    }
}
