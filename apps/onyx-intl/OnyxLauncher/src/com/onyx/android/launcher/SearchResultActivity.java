/**
 * 
 */
package com.onyx.android.launcher;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ViewSwitcher;

import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.adapter.SearchResultAdapter;
import com.onyx.android.launcher.data.CmsCenterHelper;
import com.onyx.android.launcher.data.CopyService;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.StandardMenuFactory;
import com.onyx.android.launcher.data.StandardMenuFactory.FileOperationMenuItem;
import com.onyx.android.launcher.dialog.DialogSortBy;
import com.onyx.android.launcher.task.LoadBookMetadataTask;
import com.onyx.android.launcher.util.EventedArrayList;
import com.onyx.android.launcher.view.OnyxFileGridView;
import com.onyx.android.sdk.data.AscDescOrder;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.data.cms.OnyxMetadata;
import com.onyx.android.sdk.data.util.FileUtil;
import com.onyx.android.sdk.data.util.IntentFilterFactory;
import com.onyx.android.sdk.data.util.RefValue;
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
public class SearchResultActivity extends OnyxBaseActivity
{
    private static final String TAG = "SearchResultActivity";
    
    private static final class SearchArgument 
    {
        public OnyxItemURI uri = null;
        public String pattern = null;
        
        public SearchArgument(OnyxItemURI u, String p)
        {
            uri = u;
            pattern = p;
        }
    }
    
    private static final class SearchTask extends AsyncTask<SearchArgument, ArrayList<GridItemData>, ArrayList<GridItemData>>
    {
        private SearchResultActivity mActivity = null;
        private boolean mWaitLoadMetadata = true;
        
        public SearchTask(SearchResultActivity activity)
        {
            mActivity = activity;
        }

        @Override
        protected ArrayList<GridItemData> doInBackground(SearchArgument... params)
        {
            try {
                SearchArgument arg = params[0];

                final ArrayList<GridItemData> progress_items = new ArrayList<GridItemData>();

                final EventedArrayList<GridItemData> search_result = new EventedArrayList<GridItemData>(); 
                search_result.SetOnAddedListener(new EventedArrayList.OnAddedListener()
                {

                    @Override
                    public void onAdded()
                    {
                        progress_items.add(search_result.get(search_result.size() - 1));
                        if (progress_items.size() > 1) {
                            SearchTask.this.safePublishProgress(progress_items);
                            progress_items.clear();
                        }

                    }
                });

                Log.d(TAG, "search starting");

                long time_start = System.currentTimeMillis();

                GridItemManager.search(mActivity, arg.uri, arg.pattern, search_result);

                long time_end = System.currentTimeMillis();

                Log.d(TAG, "item found: " + search_result.size());
                Log.d(TAG, "search takes time: " + (time_end - time_start) + "ms");

                return search_result;
            }
            catch (Throwable tr) {
                Log.w(TAG, "exception caught: ", tr);
            }
            
            return null;
        }

        /**
         * do asynchronous safe publish
         * @param values
         */
        @SuppressWarnings("unchecked")
        private void safePublishProgress(ArrayList<GridItemData> values)
        {
            ArrayList<GridItemData> temp = new ArrayList<GridItemData>();
            temp.addAll(values);
            this.publishProgress(temp);
        }
        
        @Override
        protected void onPreExecute()
        {
            if (this.isCancelled()) {
                return;
            }
            
            GridItemBaseAdapter adapter = (GridItemBaseAdapter)mActivity.mFileGridView.getGridView().getPagedAdapter();
                
            ArrayList<GridItemData> empty_list = new ArrayList<GridItemData>();
            adapter.fillItems(GridItemManager.getLibraryURI(), empty_list);
        }
        
        @Override
        protected void onProgressUpdate(ArrayList<GridItemData>... values)
        {
            if (this.isCancelled()) {
                return;
            }
            
            GridItemBaseAdapter adapter = (GridItemBaseAdapter)mActivity.mFileGridView.getGridView().getPagedAdapter();
            
            ArrayList<GridItemData> items = values[0];
            adapter.appendItems(items);
            
            if (mWaitLoadMetadata && adapter.getPaginator().getPageCount() > 1) {
                    mActivity.loadBookMetadataAsync();
                    mWaitLoadMetadata = false;
            }
        }
        
        @Override
        protected void onPostExecute(ArrayList<GridItemData> result)
        {
            if (this.isCancelled()) {
                return;
            }  
            
            if (result == null) {
                return;
            }
            
            GridItemBaseAdapter adapter = (GridItemBaseAdapter)mActivity.mFileGridView.getGridView().getPagedAdapter();
            
            if (result.size() > adapter.getItems().size()) {
                List<GridItemData> items = result.subList(adapter.getItems().size(), 
                                result.size());
                adapter.appendItems(items);
            }
            
            mActivity.mViewSwitcher.setDisplayedChild(1);
            
            if (mWaitLoadMetadata) {
                mActivity.loadBookMetadataAsync();
                mWaitLoadMetadata = false;
            }
        }
    }
    
    public static final String HOST_URI = "com.onyx.android.launcher.SearchResultActivity.HOST_URI";
    
    private OnyxFileGridView mFileGridView = null;
    private Button mButtonHome = null;
    private Button mButtonSortBy = null;
    private ViewSwitcher mViewSwitcher = null;
    private SearchResultAdapter mAdapter = null;
    
    private FileOperationHandler mFileOperationHandler = null;
    
    private BroadcastReceiver mSDCardUnmountedReceiver = null;
    
    private SearchTask mSearchTask = null;
    private LoadBookMetadataTask mLoadBookMetadataTask = null;
    
    @Override
    public OnyxGridView getGridView()
    {
        return mFileGridView.getGridView();
    }
    
    @Override
    public void changeViewMode(GridViewMode viewMode)
    {
        // do nothing
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
        else if (this.getSelectedGridItem() != null && (this.getSelectedGridItem() instanceof FileItemData)) {
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
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_search_result);
        
        mFileGridView = (OnyxFileGridView)this.findViewById(R.id.gridview_search_result);
        mButtonHome = (Button)this.findViewById(R.id.button_home);
        mButtonSortBy = (Button)this.findViewById(R.id.button_sort_by);
        
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
                        if (item instanceof FileItemData) {
                            mAdapter.addSelectedItems(item);
                        }
                    }
                    else {
                        if (mAdapter.getSelectedItems().contains(item)) {
                            return;
                        }
                        if (GridItemManager.isItemContainer(item.getURI())) {
                            if (item.getURI().equals(GridItemManager.getStorageURI()) ||
                                    item.getURI().isChildOf(GridItemManager.getStorageURI())) {
                                StorageActivity.startStorageActivity(SearchResultActivity.this, item.getURI());
                            }
                            else {
                                GridItemManager.processURI(SearchResultActivity.this.getGridView(), item.getURI(),
                                        SearchResultActivity.this);
                            }
                        }
                        else {
                         // db operating may fail, but should not interfere with item opening function
                            boolean res = false;
                            RefValue<OnyxMetadata> meta_data = new RefValue<OnyxMetadata>();
                            RefValue<String> err_msg = new RefValue<String>();
                            if (item instanceof BookItemData) {
                                res = CmsCenterHelper.getOrCreateMetadata(SearchResultActivity.this, 
                                        (BookItemData)item, meta_data, err_msg);
                            }           
                            if (GridItemManager.processURI(SearchResultActivity.this.getGridView(),
                                    item.getURI(), SearchResultActivity.this)) {
                                if ((item instanceof BookItemData) && res) {
                                    assert(meta_data.getValue() != null);
                                    CmsCenterHelper.updateRecentReading(SearchResultActivity.this, 
                                            (BookItemData)item, meta_data.getValue());
                                }
                            }
                        }
                    }
                }
            }
        });
        
        mFileGridView.getGridView().registerOnAdapterChangedListener(new OnyxGridView.OnAdapterChangedListener()
        {
            
            @Override
            public void onAdapterChanged()
            {
                final GridItemBaseAdapter adapter = (GridItemBaseAdapter)mFileGridView.getGridView().getPagedAdapter();

                adapter.setOnItemFilledListener(new GridItemBaseAdapter.OnItemFilledListener()
                {
                    
                    @Override
                    public void onItemFilled()
                    {
                        SearchResultActivity.this.loadBookMetadataAsync();
                    }
                });
                adapter.getPaginator().registerOnPageIndexChangedListener(new OnPageIndexChangedListener()
                {
                    
                    @Override
                    public void onPageIndexChanged()
                    {
                        SearchResultActivity.this.loadBookMetadataAsync();
                        ScreenUpdateManager.invalidate(SearchResultActivity.this.getGridView(), UpdateMode.GU);
                    }
                });
            }
        });

        mButtonHome.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                LauncherActivity.goLauncherHome(SearchResultActivity.this);
                SearchResultActivity.this.finish();
            }
        });
        
        mButtonSortBy.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogSortBy dlg = new DialogSortBy(SearchResultActivity.this, 
                        new SortOrder[] { SortOrder.Name,
                        SortOrder.FileType, SortOrder.Size, 
                        SortOrder.AccessTime, }
                );
                dlg.setOnSortByListener(new DialogSortBy.OnSortByListener()
                {
                    
                    @Override
                    public void onSortBy(SortOrder order)
                    {
                        GridItemBaseAdapter adapter = (GridItemBaseAdapter)mFileGridView.getGridView().getPagedAdapter();
                        adapter.sortItems(order, AscDescOrder.Asc);
                    }
                });
                
                dlg.show();
            }
        });

        mViewSwitcher = (ViewSwitcher)findViewById(R.id.viewswitcher_search_result_title_left);
        mViewSwitcher.setDisplayedChild(0);
        
        mAdapter = new SearchResultAdapter(this, mFileGridView.getGridView());
        mAdapter.getPageLayout().setViewMode(GridViewMode.Detail);
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
        
        mSDCardUnmountedReceiver = new BroadcastReceiver()
        {
            
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "receive broadcast: " + intent.getAction() + ", " + intent.getDataString());
                
                if (mSearchTask != null) {
                    mSearchTask.cancel(true);
                    mSearchTask = null;
                    Log.d(TAG, "onReceive: mSearchTask stopped");
                }
                
                if (mLoadBookMetadataTask != null) {
                    mLoadBookMetadataTask.cancel(true);
                    mLoadBookMetadataTask = null;
                    Log.d(TAG, "onReceive: mLoadBookMetadataTask stopped");
                }
                
                GridItemBaseAdapter adapter = (GridItemBaseAdapter)mFileGridView.getGridView().getPagedAdapter();
                
                long time_start = System.currentTimeMillis();
                String sd_path = FileUtil.getFilePathFromUri(intent.getDataString());
                OnyxItemURI sd_root = GridItemManager.getURIFromFilePath(sd_path);
                ArrayList<GridItemData> left = new ArrayList<GridItemData>();
                ArrayList<GridItemData> items = adapter.getItems();
                
                String str_root_uri = sd_root.toString();
                Log.d(TAG, "unmounted sdcard: " + str_root_uri);
                for (GridItemData i : items) {
                    if (!i.getURI().isChildOf(sd_root)) {
                        left.add(i);
                    }
                }
                adapter.fillItems(adapter.getHostURI(), left);
                
                long time_remove = System.currentTimeMillis() - time_start;
                
                Log.d(TAG, "items left: " + left.size());
                Log.d(TAG, "filter time: " + time_remove + "ms");
            }
        };
        IntentFilter filter = IntentFilterFactory.getSDCardUnmountedFilter();
        registerReceiver(this.mSDCardUnmountedReceiver, filter);
        
        this.handleIntent(this.getIntent());
        
        if (CopyService.getSourceItems() != null) {
            mFileGridView.onPreparePaste();
        }
        
        this.initGridViewItemNavigation();
        this.registerLongPressListener();
        
        ScreenUpdateManager.invalidate(this.getGridView(), UpdateMode.GU);
        Log.d(TAG, "onCreate finished");
    }
    
    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        
        if (mSDCardUnmountedReceiver != null) {
            unregisterReceiver(mSDCardUnmountedReceiver);
        }
        
        if (mSearchTask != null) {
            mSearchTask.cancel(true);
            mSearchTask = null;
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mAdapter.getMultipleSelectionMode()) {
                mFileGridView.onCancelMultipleSelection();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
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
    protected void onNewIntent(Intent intent)
    {
        this.setIntent(intent);
        this.handleIntent(intent);
    }
    
    private void handleIntent(Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            
            Bundle app_data = getIntent().getBundleExtra(SearchManager.APP_DATA);
            if (app_data != null) {
                String uri_string = app_data.getString(HOST_URI);
                if (uri_string == null) {
                    return;
                }
                
                OnyxItemURI uri = OnyxItemURI.createFromString(uri_string);
                mSearchTask = new SearchTask(this);
                mSearchTask.execute(new SearchArgument(uri, query));
            }
        }
    }
    
    private void loadBookMetadataAsync()
    {
        if (mLoadBookMetadataTask != null) {
            mLoadBookMetadataTask.cancel(true);
            mLoadBookMetadataTask = null;
        }
        
        RefValue<LoadBookMetadataTask> result = new RefValue<LoadBookMetadataTask>();
        LoadBookMetadataTask.runTask(this, mFileGridView.getGridView(), result);
        
        if (result.getValue() != null) {
            mLoadBookMetadataTask = result.getValue();
        }
    }
}
