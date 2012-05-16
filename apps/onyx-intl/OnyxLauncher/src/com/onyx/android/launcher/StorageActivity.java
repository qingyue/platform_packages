/**
 * 
 */
package com.onyx.android.launcher;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter.OnHostURIChangedListener;
import com.onyx.android.launcher.adapter.StorageAdapter;
import com.onyx.android.launcher.data.CmsCenterHelper;
import com.onyx.android.launcher.data.CopyService;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.StandardMenuFactory;
import com.onyx.android.launcher.data.StandardMenuFactory.FileOperationMenuItem;
import com.onyx.android.launcher.dialog.DialogPathIndicator;
import com.onyx.android.launcher.dialog.DialogSortBy;
import com.onyx.android.launcher.task.LoadBookMetadataTask;
import com.onyx.android.launcher.view.OnyxFileGridView;
import com.onyx.android.sdk.data.AscDescOrder;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.data.cms.OnyxMetadata;
import com.onyx.android.sdk.data.util.ActivityUtil;
import com.onyx.android.sdk.data.util.EnviromentUtil;
import com.onyx.android.sdk.data.util.FileUtil;
import com.onyx.android.sdk.data.util.IntentFilterFactory;
import com.onyx.android.sdk.data.util.RefValue;
import com.onyx.android.sdk.data.util.SDCardRemovedException;
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
public class StorageActivity extends OnyxBaseActivity
{
    private static final String TAG = "StorageActivity";

    public static final String STARTING_URI = "com.onyx.android.launcher.StorageActivity.STARTING_URI";

    public static SortOrder SortPolicy = SortOrder.Name;
    public static GridViewMode ViewMode = GridViewMode.Thumbnail;

    // root URI when activity startup
    private OnyxItemURI mStartingURI = null;

    private OnyxFileGridView mFileGridView = null;
    private TextView mTextViewPathIndicator = null;
    private Button mButtonHome = null;
    private Button mButtonSortBy = null;
    private Button mButtonChangeView = null;
    private ImageView mImageViewPathIndicator = null;
    private StorageAdapter mAdapter = null;

    private BroadcastReceiver mSDCardUnmountedReceiver = null;

    private LoadBookMetadataTask mLoadBookMetadataTask = null;

    private FileOperationHandler mFileOperationHandler = null;

    /**
     * StorageActivity starting up helper
     * 
     * @param from
     * @param dstStorageURI
     */
    public static boolean startStorageActivity(Activity from, OnyxItemURI dstStorageURI)
    {
        Intent intent = new Intent(from, StorageActivity.class);
        intent.putExtra(StorageActivity.STARTING_URI, dstStorageURI.toString()); 

        return ActivityUtil.startActivitySafely(from, intent);
    }

    @Override
    public OnyxGridView getGridView()
    {
        return mFileGridView.getGridView();
    }

    @Override
    public void changeViewMode(GridViewMode viewMode)
    {
        super.changeViewMode(viewMode);

        StorageActivity.ViewMode = viewMode;
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
            suites.add(StandardMenuFactory.getFileOperationMenuSuite(mFileOperationHandler));
        }
        else {
            suites.add(StandardMenuFactory.getFileOperationMenuSuite(mFileOperationHandler,
                    new FileOperationMenuItem[] { FileOperationMenuItem.New, FileOperationMenuItem.NewFolder }));
        }

        return suites;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_storage);

        mFileGridView = (OnyxFileGridView)this.findViewById(R.id.gridview_storage);
        mTextViewPathIndicator = (TextView)this.findViewById(R.id.textview_path_indicator);
        mButtonHome = (Button)findViewById(R.id.button_home);
        mButtonSortBy = (Button)findViewById(R.id.button_sort_by);
        mButtonChangeView = (Button)findViewById(R.id.button_change_view);
        mImageViewPathIndicator = (ImageView)findViewById(R.id.imageview_path_indicator);

        mFileGridView.setCanPaste(true);

        assert(this.getGridView() == mFileGridView.getGridView());
        this.getGridView().registerOnAdapterChangedListener(new OnyxGridView.OnAdapterChangedListener()
        {

            @Override
            public void onAdapterChanged()
            {
                final GridItemBaseAdapter adapter = (GridItemBaseAdapter)StorageActivity.this.getGridView().getPagedAdapter();

                adapter.setOnHostURIChangedListener(new OnHostURIChangedListener()
                {

                    @Override
                    public void onHostURIChanged()
                    {
                        StorageActivity.this.mTextViewPathIndicator.setText(adapter.getHostURI().getName());
                    }
                }); 
                adapter.getPaginator().registerOnPageIndexChangedListener(new OnPageIndexChangedListener()
                {

                    @Override
                    public void onPageIndexChanged()
                    {
                        StorageActivity.this.loadBookMetadataAsync();
                        ScreenUpdateManager.invalidate(StorageActivity.this.getGridView(), UpdateMode.GC);
                    }
                });
            }
        });

        mButtonHome.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                LauncherActivity.goLauncherHome(StorageActivity.this);
                StorageActivity.this.finish();
            }
        });
        mButtonSortBy.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                DialogSortBy dlg = new DialogSortBy(StorageActivity.this, 
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
                        StorageActivity.SortPolicy = order;
                    }
                });

                dlg.show();
            }
        });

        if (StorageActivity.ViewMode == GridViewMode.Detail) {
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
                if (StorageActivity.this.getGridView().getPagedAdapter().getPageLayout().getViewMode() == GridViewMode.Thumbnail) {
                    StorageActivity.this.changeViewMode(GridViewMode.Detail);
                    mButtonChangeView.setText("Thumbnail");
                }
                else {
                    StorageActivity.this.changeViewMode(GridViewMode.Thumbnail);
                    mButtonChangeView.setText("Detail");
                }
            }
        });

        mImageViewPathIndicator.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                GridItemBaseAdapter adapter = (GridItemBaseAdapter)StorageActivity.this.getGridView().getPagedAdapter();
                OnyxItemURI u = adapter.getHostURI();
                new DialogPathIndicator(StorageActivity.this, GridItemManager.getStorageURI(), u).show();
            }
        });

        this.getGridView().setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                Log.d(TAG, "onItemStarting");
                if (mAdapter.getMultipleSelectionMode()) {
                    mAdapter.addSelectedItems((GridItemData)view.getTag());
                }
                else {
                    if (mAdapter.getSelectedItems().contains((GridItemData)view.getTag())) {
                        return;
                    }

                    StorageActivity.this.startGridViewItem(view);
                }
            }
        });

        mSDCardUnmountedReceiver = new BroadcastReceiver()
        {

            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "receive broadcast: " + intent.getAction() + ", " + intent.getDataString());

                String sd_path = FileUtil.getFilePathFromUri(intent.getDataString());

                GridItemBaseAdapter adapter = (GridItemBaseAdapter)StorageActivity.this.getGridView().getPagedAdapter();

                File current_host_folder = GridItemManager.getFileFromURI(adapter.getHostURI());
                if (current_host_folder.getAbsolutePath().startsWith(sd_path)) {
                    StorageActivity.this.finish();
                }
            }
        };

        registerReceiver(this.mSDCardUnmountedReceiver, IntentFilterFactory.getSDCardUnmountedFilter());

        mAdapter = new StorageAdapter(this, this.getGridView());
        mAdapter.getPageLayout().setViewMode(StorageActivity.ViewMode);
        this.getGridView().setAdapter(mAdapter);

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

        if (savedInstanceState != null && savedInstanceState.containsKey("URI")) {
			mStartingURI = GridItemManager.getURIFromFilePath((new File(savedInstanceState.getString("URI")).getPath()));
		}

        this.handleNewIntent();
        this.initGridViewItemNavigation();
        this.registerLongPressListener();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        Log.d(TAG, "onNewIntent");
        this.setIntent(intent);
        this.handleNewIntent();
    }
    
    @Override
    protected void onResume()
    {
        Log.d(TAG, "onResume");
        super.onResume();
    }
    
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
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy");

        if (mSDCardUnmountedReceiver != null) {
            unregisterReceiver(mSDCardUnmountedReceiver);
        }

        if (mLoadBookMetadataTask != null) {
            mLoadBookMetadataTask.cancel(true);
            mLoadBookMetadataTask = null;
        }

        super.onDestroy();

        Log.d(TAG, "onDestroy finished");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if (((OnyxFileGridView)findViewById(R.id.gridview_storage)).getButtonCancel().isFocused() == true
    			|| ((OnyxFileGridView)findViewById(R.id.gridview_storage)).getButtonCopy().isFocused() == true
    			|| ((OnyxFileGridView)findViewById(R.id.gridview_storage)).getButtonCut().isFocused() == true
    			|| ((OnyxFileGridView)findViewById(R.id.gridview_storage)).getButtonDelete().isFocused() == true
    			|| ((OnyxFileGridView)findViewById(R.id.gridview_storage)).getButtonPaste().isFocused() == true) {
    		ScreenUpdateManager.invalidate(((OnyxFileGridView)findViewById(R.id.gridview_storage)), UpdateMode.GU);
    	}

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mAdapter.getMultipleSelectionMode()) {
                mFileGridView.onCancelMultipleSelection();
                return true;
            }

            GridItemBaseAdapter adapter = (GridItemBaseAdapter)this.getGridView().getPagedAdapter();
            if (adapter.getHostURI().isChildOf(GridItemManager.getStorageURI())) {
                this.startURI(adapter.getHostURI().getParent());
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }
    
    private boolean handleNewIntent()
    {
        String root_uri_string = this.getIntent().getStringExtra(STARTING_URI);
        OnyxItemURI uri = OnyxItemURI.createFromString(root_uri_string);
        if (uri == null) {
            assert(false);
            throw new RuntimeException();
        }
        if (!uri.equals(GridItemManager.getStorageURI()) &&
                !uri.isChildOf(GridItemManager.getStorageURI())) {
            assert(false);
            throw new RuntimeException();
        }

        if (mStartingURI == null) {
        	mStartingURI = uri;
		}
        return GridItemManager.processURI(this.getGridView(), mStartingURI, this);
    }

    private void loadBookMetadataAsync()
    {
        if (mLoadBookMetadataTask != null) {
            mLoadBookMetadataTask.cancel(true);
            mLoadBookMetadataTask = null;
        }

        RefValue<LoadBookMetadataTask> result = new RefValue<LoadBookMetadataTask>();
        LoadBookMetadataTask.runTask(this, this.getGridView(), result);

        if (result.getValue() != null) {
            mLoadBookMetadataTask = result.getValue();
        }
    }
    private boolean startGridViewItem(View itemView)
    {
        GridItemData item = (GridItemData)itemView.getTag();

        // db operating may fail, but should not interfere with item opening function
        boolean res = false;
        RefValue<OnyxMetadata> meta_data = new RefValue<OnyxMetadata>();
        RefValue<String> err_msg = new RefValue<String>();
        if (item instanceof BookItemData) {
            res = CmsCenterHelper.getOrCreateMetadata(this, (BookItemData)item, meta_data, err_msg);
        }           
        if (this.startURI(item.getURI())) {
            if ((item instanceof BookItemData) && res) {
                assert(meta_data.getValue() != null);
                CmsCenterHelper.updateRecentReading(this, (BookItemData)item, meta_data.getValue());
            }
            return true;
        }
        
        return false;
    }

    public boolean startURI(OnyxItemURI uri)
    {
        GridItemBaseAdapter adapter = (GridItemBaseAdapter)this.getGridView().getPagedAdapter();

        if (!uri.equals(adapter.getHostURI())) {
            if (GridItemManager.getStorageURI().isChildOf(uri)) {
                this.finish();
                return true;
            }
            
            try {
                OnyxItemURI old_uri = mAdapter.getHostURI();
                boolean res = GridItemManager.processURI(this.getGridView(), uri, this);
                if (!res) {
                    return false;
                }
                if ((uri.equals(GridItemManager.getStorageURI()) || uri.isChildOf(GridItemManager.getStorageURI())) && 
                        GridItemManager.isItemContainer(uri)) {
                    if (uri.isChildOf(old_uri)) {
                        if (this.getGridView().getCount() > 0) {
                            this.getGridView().setSelection(0);
                        }
                    }
                    else if (old_uri.isChildOf(uri)) {
                        OnyxItemURI old_uri_parent_in_current_host_uri = new OnyxItemURI(old_uri.getPathLevels().subList(0, 
                                uri.getPathLevels().size() + 1));
                        mAdapter.locateItemInGridView(old_uri_parent_in_current_host_uri);
                    }
                    this.loadBookMetadataAsync();
                }
                return true;
            }
            catch (SDCardRemovedException e) {
                File current_host_folder = GridItemManager.getFileFromURI(adapter.getHostURI());
                if (EnviromentUtil.isFileOnRemovableSDCard(current_host_folder)) {
                    this.finish();
                }
                return false;
            }
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	OnyxItemURI item_uri = mAdapter.getHostURI();
    	String uri = GridItemManager.getFileFromURI(item_uri).getPath();
    	outState.putString("URI", uri);
    	super.onSaveInstanceState(outState);
    }
}