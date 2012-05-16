/**
 * 
 */
package com.onyx.android.launcher;

import java.util.ArrayList;
import java.util.Collection;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.onyx.android.launcher.adapter.RecentDocumentsAdapter;
import com.onyx.android.launcher.data.CmsCenterHelper;
import com.onyx.android.launcher.data.CopyService;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.StandardMenuFactory;
import com.onyx.android.launcher.data.StandardMenuFactory.FileOperationMenuItem;
import com.onyx.android.launcher.dialog.DialogSortBy;
import com.onyx.android.launcher.view.OnyxFileGridView;
import com.onyx.android.sdk.data.AscDescOrder;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;
import com.onyx.android.sdk.ui.data.GridViewPaginator.OnPageIndexChangedListener;
import com.onyx.android.sdk.ui.menu.OnyxMenuSuite;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * @author joy
 *
 */
public class RecentDocumentsActivity extends OnyxBaseActivity
{
    private static final String TAG = "RecentDocumentsActivity";

    public static SortOrder SortPolicy = SortOrder.Name;
    public static GridViewMode ViewMode = GridViewMode.Thumbnail;

    private OnyxFileGridView mFileGridView = null;
    private Button mButtonHome = null;
    private Button mButtonSortBy = null;
    private Button mButtonChangeView = null;
    private RecentDocumentsAdapter mAdapter = null;

    private FileOperationHandler mFileOperationHandler = null;

    @Override
    public OnyxGridView getGridView()
    {
        return mFileGridView.getGridView();
    }

    @Override
    public void changeViewMode(GridViewMode viewMode)
    {
        super.changeViewMode(viewMode);

        RecentDocumentsActivity.ViewMode = viewMode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_recent_documents);

        mFileGridView = (OnyxFileGridView)this.findViewById(R.id.gridview_recent_documents);
        mButtonHome = (Button)this.findViewById(R.id.button_home);
        mButtonSortBy = (Button)this.findViewById(R.id.button_sort_by);
        mButtonChangeView = (Button)findViewById(R.id.button_change_view);

        mFileGridView.setCanPaste(false);
        
        mFileGridView.getGridView().setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                GridItemData item = (GridItemData)view.getTag();
                if (item != null) {
                    if (mAdapter.getMultipleSelectionMode()) {
                        mAdapter.addSelectedItems(item);
                    }
                    else if (GridItemManager.processURI(mFileGridView.getGridView(), item.getURI(), RecentDocumentsActivity.this)) {
                        if (item instanceof BookItemData) {
                            BookItemData book = (BookItemData)item;
                            assert(book.getMetadata() != null);
                            CmsCenterHelper.updateRecentReading(RecentDocumentsActivity.this, book, book.getMetadata());
                        }
                    }
                }
            }
        });

        mButtonHome.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                LauncherActivity.goLauncherHome(RecentDocumentsActivity.this);
                RecentDocumentsActivity.this.finish();
            }
        });

        mButtonSortBy.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                DialogSortBy dlg = new DialogSortBy(RecentDocumentsActivity.this, 
                        new SortOrder[] { SortOrder.Name,
                        SortOrder.FileType, SortOrder.Size, 
                        SortOrder.AccessTime, }
                        );
                dlg.setOnSortByListener(new DialogSortBy.OnSortByListener()
                {

                    @Override
                    public void onSortBy(SortOrder order)
                    {
                        mAdapter.sortItems(order, AscDescOrder.Asc);

                        RecentDocumentsActivity.SortPolicy = order;
                    }
                });

                dlg.show();
            }
        });

        if (RecentDocumentsActivity.ViewMode == GridViewMode.Detail) {
            mButtonChangeView.setText("Thumbnail");
        }
        else {
            mButtonChangeView.setText("Detail");
        }

        mButtonChangeView.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (mAdapter.getPageLayout().getViewMode() == GridViewMode.Thumbnail) {
                    RecentDocumentsActivity.this.changeViewMode(GridViewMode.Detail);
                    mButtonChangeView.setText("Thumbnail");
                }
                else {
                    RecentDocumentsActivity.this.changeViewMode(GridViewMode.Thumbnail);
                    mButtonChangeView.setText("Detail");
                }
            }
        });

        mAdapter = new RecentDocumentsAdapter(this, mFileGridView.getGridView());
        mAdapter.getPageLayout().setViewMode(RecentDocumentsActivity.ViewMode);
        Collection<BookItemData> books = CmsCenterHelper.getRecentReadings(this); 
        mAdapter.fillItems(null, books);
        mAdapter.getPaginator().registerOnPageIndexChangedListener(new OnPageIndexChangedListener()
        {
            
            @Override
            public void onPageIndexChanged()
            {
                ScreenUpdateManager.invalidate(RecentDocumentsActivity.this.getGridView(), UpdateMode.GC);
            }
        });

        mFileGridView.getGridView().setAdapter(mAdapter);

        mFileOperationHandler = new FileOperationHandler(this, mAdapter) {
            @Override
            public void onCopy()
            {
                super.onCopy();
                mFileGridView.onPreparePaste();
            }
            
            @Override
            public void onCut()
            {
                super.onCut();
                mFileGridView.onPreparePaste();
            }
        };
        
        if (CopyService.getSourceItems() != null) {
            mFileGridView.onPreparePaste();
        }
        
        this.initGridViewItemNavigation();
        this.registerLongPressListener();
    }

    @Override
    public ArrayList<OnyxMenuSuite> getContextMenuSuites()
    {
        ArrayList<OnyxMenuSuite> suites = super.getContextMenuSuites();
        if (mAdapter.getMultipleSelectionMode() && (mAdapter.getSelectedItems().size() > 0)) {
            ArrayList<FileItemData> items = new ArrayList<FileItemData>(mAdapter.getSelectedItems().size());
            for (GridItemData i : mAdapter.getSelectedItems()) {
                items.add((FileItemData)i);
            }
            mFileOperationHandler.setSourceItems(items);
            suites.add(StandardMenuFactory.getFileOperationMenuSuite(mFileOperationHandler));
        }
        else if (this.getSelectedGridItem() != null &&
                (this.getSelectedGridItem() instanceof FileItemData)) {
            ArrayList<FileItemData> items = new ArrayList<FileItemData>();
            items.add((FileItemData)this.getSelectedGridItem());
            mFileOperationHandler.setSourceItems(items);
            suites.add(StandardMenuFactory.getFileOperationMenuSuite(mFileOperationHandler, 
                    new FileOperationMenuItem[] { FileOperationMenuItem.Rename, 
                  FileOperationMenuItem.Copy, FileOperationMenuItem.Cut, 
                  FileOperationMenuItem.Remove }));
        }

        return suites;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.home_option_menu_select_mutiple:
            mAdapter.setMultipleSelectionMode(true);
            mFileGridView.onPrepareMultipleSelection();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if (((OnyxFileGridView)findViewById(R.id.gridview_recent_documents)).getButtonCancel().isFocused() == true
			|| ((OnyxFileGridView)findViewById(R.id.gridview_recent_documents)).getButtonCopy().isFocused() == true
			|| ((OnyxFileGridView)findViewById(R.id.gridview_recent_documents)).getButtonCut().isFocused() == true
			|| ((OnyxFileGridView)findViewById(R.id.gridview_recent_documents)).getButtonDelete().isFocused() == true
			|| ((OnyxFileGridView)findViewById(R.id.gridview_recent_documents)).getButtonPaste().isFocused() == true) {
    		ScreenUpdateManager.invalidate(((OnyxFileGridView)findViewById(R.id.gridview_recent_documents)), UpdateMode.GU);
		}
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mAdapter.getMultipleSelectionMode()) {
                mFileGridView.onCancelMultipleSelection();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
