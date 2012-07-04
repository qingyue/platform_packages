/**
 * 
 */
package com.onyx.android.launcher;

import java.util.ArrayList;
import java.util.Collection;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;

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
import com.onyx.android.sdk.ui.data.GridViewPaginator.OnStateChangedListener;
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
    public static AscDescOrder AscOrder = AscDescOrder.Asc;

    private OnyxFileGridView mFileGridView = null;
    private ImageButton mButtonHome = null;
    private ImageButton mButtonSortBy = null;
    private ImageButton mButtonChangeView = null;
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
        mButtonHome = (ImageButton)this.findViewById(R.id.button_home);
        mButtonSortBy = (ImageButton)this.findViewById(R.id.button_sort_by);
        mButtonChangeView = (ImageButton)findViewById(R.id.button_change_view);

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
                        SortOrder.AccessTime, },
                        SortOrder.AccessTime, AscOrder);
                dlg.setOnSortByListener(new DialogSortBy.OnSortByListener()
                {

                    @Override
                    public void onSortBy(SortOrder order, AscDescOrder ascOrder)
                    {
                        mAdapter.sortItems(order, ascOrder);

                        RecentDocumentsActivity.SortPolicy = order;
                    }
                });

                dlg.show();
            }
        });

        if (RecentDocumentsActivity.ViewMode == GridViewMode.Detail) {
        	mButtonChangeView.setImageResource(R.drawable.gridlittle);
        }
        else {
        	mButtonChangeView.setImageResource(R.drawable.listbulletslittle);
        }

        mButtonChangeView.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (mAdapter.getPageLayout().getViewMode() == GridViewMode.Thumbnail) {
                    RecentDocumentsActivity.this.changeViewMode(GridViewMode.Detail);
                    mButtonChangeView.setImageResource(R.drawable.gridlittle);
                }
                else {
                    RecentDocumentsActivity.this.changeViewMode(GridViewMode.Thumbnail);
                    mButtonChangeView.setImageResource(R.drawable.listbulletslittle);
                }
            }
        });

        mAdapter = new RecentDocumentsAdapter(this, mFileGridView.getGridView());
        mAdapter.getPageLayout().setViewMode(RecentDocumentsActivity.ViewMode);
        Collection<BookItemData> books = CmsCenterHelper.getRecentReadings(this); 
        mAdapter.fillItems(GridItemManager.getRecentDocumentsURI(), books);
        mAdapter.getPaginator().registerOnPageIndexChangedListener(new OnPageIndexChangedListener()
        {
            
            @Override
            public void onPageIndexChanged()
            {
                ScreenUpdateManager.invalidate(RecentDocumentsActivity.this.getGridView(), UpdateMode.GC);
            }
        });
        mAdapter.getPaginator().registerOnStateChangedListener(new OnStateChangedListener() {

			@Override
			public void onStateChanged() {
				ScreenUpdateManager.invalidate(mFileGridView.getGridView(), UpdateMode.GU);
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

		if (mAdapter.getMultipleSelectionMode() && (mAdapter.getSelectedItems().size() > 0) 
				&& this.getSelectedGridItem() != null) {
			ArrayList<FileItemData> items = new ArrayList<FileItemData>(mAdapter.getSelectedItems().size());
			for (GridItemData i : mAdapter.getSelectedItems()) {
				items.add((FileItemData) i);
			}
			mFileOperationHandler.setSourceItems(items);

			suites.add(StandardMenuFactory.getFileOperationMenuSuite(
					mFileOperationHandler, new FileOperationMenuItem[] {
							FileOperationMenuItem.Rename,
							FileOperationMenuItem.Copy,
							FileOperationMenuItem.Cut,
							FileOperationMenuItem.Remove,
							FileOperationMenuItem.Property }));

			return suites;
		} else if (this.getSelectedGridItem() != null && (this.getSelectedGridItem() instanceof FileItemData)) {
			ArrayList<FileItemData> items = new ArrayList<FileItemData>();
			items.add((FileItemData) this.getSelectedGridItem());
			mFileOperationHandler.setSourceItems(items);

			if (mAdapter.getMultipleSelectionMode()) {
				suites.add(StandardMenuFactory.getFileOperationMenuSuite(
						mFileOperationHandler, new FileOperationMenuItem[] {
								FileOperationMenuItem.Rename,
								FileOperationMenuItem.Copy,
								FileOperationMenuItem.Cut,
								FileOperationMenuItem.Remove,
								FileOperationMenuItem.Property,
								FileOperationMenuItem.GotoFolder }));
			}
			else {
				suites.add(StandardMenuFactory.getFileOperationMenuSuite(
						mFileOperationHandler, new FileOperationMenuItem[] {
								FileOperationMenuItem.Rename,
								FileOperationMenuItem.Copy,
								FileOperationMenuItem.Cut,
								FileOperationMenuItem.Remove,
								FileOperationMenuItem.Property,
								FileOperationMenuItem.GotoFolder,
								FileOperationMenuItem.Multiple }));
			}

			return suites;
		}
		if(!mAdapter.getMultipleSelectionMode()) {
			mFileOperationHandler.getSourceItems().clear();

			suites.add(StandardMenuFactory.getFileOperationMenuSuite(
					mFileOperationHandler, new FileOperationMenuItem[] { 
							FileOperationMenuItem.Multiple }));

			return suites;
		}

		return suites;
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
