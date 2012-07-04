/**
 * 
 */
package com.onyx.android.launcher;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ViewSwitcher;

import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.adapter.LibraryAdapter;
import com.onyx.android.launcher.adapter.LibraryFirstLetterAdapter;
import com.onyx.android.launcher.data.CmsCenterHelper;
import com.onyx.android.launcher.data.CopyService;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.StandardMenuFactory;
import com.onyx.android.launcher.data.StandardMenuFactory.FileOperationMenuItem;
import com.onyx.android.launcher.dialog.DialogProgressBarRotundity;
import com.onyx.android.launcher.dialog.DialogSortBy;
import com.onyx.android.launcher.dialog.DialogTagCloud;
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
import com.onyx.android.sdk.ui.data.GridViewPaginator.OnStateChangedListener;
import com.onyx.android.sdk.ui.menu.OnyxMenuSuite;
import com.onyx.android.sdk.ui.util.OnyxFocusFinder;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * @author joy
 *
 */
public class LibraryActivity extends OnyxBaseActivity
{
    private static final String TAG = "LibraryActivity";

    private static final class LoadLibraryItemsTask extends AsyncTask<Void, ArrayList<GridItemData>, Void> 
    {
        private LibraryActivity mActivity = null;
        private boolean mWaitLoadMetadata = true;
        
        public LoadLibraryItemsTask(LibraryActivity activity)
        {
            mActivity = activity;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            Cursor cursor = null;
            try {
                Log.d(TAG, "loading library items");

                long time_start = System.currentTimeMillis();
                
                final ArrayList<GridItemData> progress_items = new ArrayList<GridItemData>();
                
                final EventedArrayList<GridItemData> load_result = new EventedArrayList<GridItemData>(); 
                load_result.SetOnAddedListener(new EventedArrayList.OnAddedListener()
                {

                    @Override
                    public void onAdded()
                    {
                        progress_items.add(load_result.get(load_result.size() - 1));
                        
                        if (progress_items.size() > 20) {
                            LoadLibraryItemsTask.this.safePublishProgress(progress_items);
                            progress_items.clear();
                        }
                        
                    }
                });
                
                if (this.isCancelled()) {
                    return null;
                }  
                CmsCenterHelper.getLibraryItems(mActivity, LibraryActivity.SortPolicy, LibraryActivity.AscOrder, load_result);
                
                if (this.isCancelled()) {
                    return null;
                } 
                this.safePublishProgress(progress_items);

                long time_end = System.currentTimeMillis();

                Log.d(TAG, "items loaded, count: " + load_result.size());
                Log.d(TAG, "total time: " + (time_end - time_start) + "ms\n");

                return null;
            }
            catch (Throwable tr) {
                Log.w(TAG, "exception caught: ", tr);
            }
            finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
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
        protected void onCancelled()
        {
            super.onCancelled();
        }
        
        @Override
        protected void onPreExecute()
        {
            if (this.isCancelled()) {
                return;
            }
            
            ArrayList<GridItemData> empty_list = new ArrayList<GridItemData>();
            mActivity.mAdapter.fillItems(GridItemManager.getLibraryURI(), empty_list);
        }
        
        @Override
        protected void onProgressUpdate(ArrayList<GridItemData>... values)
        {
            if (this.isCancelled()) {
                return;
            }
            
            ArrayList<GridItemData> items = values[0];
            mActivity.mAdapter.appendItems(items);
//            mAdapter.sortItems(LibraryActivity.SortPolicy,LibraryActivity.AscOrder);
            
            if (mActivity.mProgressDialog.isShowing()) {
                ScreenUpdateManager.postInvalidate(mActivity.getWindow().getDecorView(), UpdateMode.GU);
                mActivity.mProgressDialog.cancel();
            }
            
            if (mActivity.mFileGridView.getGridView().getVisibility() != View.VISIBLE) {
                mActivity.mFileGridView.getGridView().setVisibility(View.VISIBLE);
            }
            
            if (mWaitLoadMetadata && mActivity.mAdapter.getPaginator().getPageCount() > 1) {
                Log.d(TAG, "load book metadata in onProgressUpdate()");
                ScreenUpdateManager.invalidate(mActivity.getGridView(), UpdateMode.GU);
                mActivity.loadBookMetadataAsync();
                mWaitLoadMetadata = false;
            }
        }
        
        @Override
        protected void onPostExecute(Void result)
        {
            if (this.isCancelled()) {
                return;
            } 
            mActivity.mViewSwitcher.setDisplayedChild(1);

            if (mActivity.mProgressDialog.isShowing()) {
                mActivity.mProgressDialog.cancel();
            }
            
            if (mActivity.mFileGridView.getGridView().getVisibility() != View.VISIBLE) {
                mActivity.mFileGridView.getGridView().setVisibility(View.VISIBLE);
            }
            
            if (mWaitLoadMetadata) {
                Log.d(TAG, "load book metadata in onPostExecute()");
                ScreenUpdateManager.invalidate(mActivity.getGridView(), UpdateMode.GC);
                mActivity.loadBookMetadataAsync();
            }

            mActivity.mFileGridView.getGridView().setSelection(0);
        }
        
    }
    
    public static SortOrder SortPolicy = SortOrder.Name;
    public static AscDescOrder AscOrder = AscDescOrder.Asc;
    public static GridViewMode ViewMode = GridViewMode.Thumbnail;
    
    private OnyxFileGridView mFileGridView = null;
    private ImageButton mButtonHome = null;
    private ImageButton mButtonSortBy = null;
    private ImageButton mButtonChangeView = null;
    private DialogProgressBarRotundity mProgressDialog = null;
    private ViewSwitcher mViewSwitcher = null;
    private LibraryAdapter mAdapter = null;
    
    private FileOperationHandler mFileOperationHandler = null;
    
    private BroadcastReceiver mSDCardUnmountedReceiver = null;

    private LoadLibraryItemsTask mLoadLibraryItemsTask = null;
    private LoadBookMetadataTask mLoadBookMetadataTask = null;
    
    private OnyxGridView mFirstLetterGridView = null;
    private LibraryFirstLetterAdapter mLetterAdapter = null;
    
    private ImageButton mButtonTag = null;
    
    
    @Override
    public OnyxGridView getGridView()
    {
        return mFileGridView.getGridView();
    }
    
    @Override
    public void changeViewMode(GridViewMode viewMode)
    {
        super.changeViewMode(viewMode);
        
        LibraryActivity.ViewMode = viewMode;
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
		}
		if (this.getSelectedGridItem() != null && (this.getSelectedGridItem() instanceof FileItemData)) {
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_library);
        
        mFileGridView = (OnyxFileGridView)this.findViewById(R.id.gridview_library);
        mButtonHome = (ImageButton)this.findViewById(R.id.button_home);
        mButtonSortBy = (ImageButton)this.findViewById(R.id.button_sort_by);
        mButtonChangeView = (ImageButton)this.findViewById(R.id.button_change_view);
        
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
                    else {
                        // db operating may fail, but should not interfere with item opening function
                        boolean res = false;
                        RefValue<OnyxMetadata> meta_data = new RefValue<OnyxMetadata>();
                        RefValue<String> err_msg = new RefValue<String>();
                        if (item instanceof BookItemData) {
                            res = CmsCenterHelper.getOrCreateMetadata(LibraryActivity.this, 
                                    (BookItemData)item, meta_data, err_msg);
                        }           
                        if (GridItemManager.processURI(LibraryActivity.this.getGridView(),
                                item.getURI(), LibraryActivity.this)) {
                            if ((item instanceof BookItemData) && res) {
                                assert(meta_data.getValue() != null);
                                CmsCenterHelper.updateRecentReading(LibraryActivity.this, 
                                        (BookItemData)item, meta_data.getValue());
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
                mAdapter.setOnItemFilledListener(new GridItemBaseAdapter.OnItemFilledListener()
                {
                    
                    @Override
                    public void onItemFilled()
                    {
                        LibraryActivity.this.loadBookMetadataAsync();
                    }
                });
                mAdapter.getPaginator().registerOnPageIndexChangedListener(new OnPageIndexChangedListener()
                {
                    
                    @Override
                    public void onPageIndexChanged()
                    {
                        LibraryActivity.this.loadBookMetadataAsync();
                        ScreenUpdateManager.invalidate(LibraryActivity.this.getGridView(), UpdateMode.GC);
                    }
                });
                mAdapter.getPaginator().registerOnStateChangedListener(new OnStateChangedListener() {

					@Override
					public void onStateChanged() {
						ScreenUpdateManager.invalidate(mFileGridView.getGridView(), UpdateMode.GU);
					}
				});
            }
        });

        mButtonHome.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                LauncherActivity.goLauncherHome(LibraryActivity.this);
                LibraryActivity.this.finish();
            }
        });
        mButtonSortBy.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogSortBy dlg = new DialogSortBy(LibraryActivity.this, 
                        new SortOrder[] { SortOrder.Name,
                        SortOrder.FileType, SortOrder.Size, 
                        SortOrder.AccessTime, },
                LibraryActivity.SortPolicy, AscOrder);
                dlg.setOnSortByListener(new DialogSortBy.OnSortByListener()
                {
                    
                    @Override
                    public void onSortBy(SortOrder order, AscDescOrder ascOrder)
                    {
                        mAdapter.sortItems(order, ascOrder);
                        
                        LibraryActivity.SortPolicy = order;
                        LibraryActivity.AscOrder = ascOrder;
                        mLetterAdapter.notifyDataSetInvalidated();
                        ScreenUpdateManager.invalidate(mFileGridView, ScreenUpdateManager.UpdateMode.GC);
                    }
                });
                
                dlg.show();
            }
        });
        
        if (StorageActivity.ViewMode == GridViewMode.Detail) {
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
                    LibraryActivity.this.changeViewMode(GridViewMode.Detail);
                    mButtonChangeView.setImageResource(R.drawable.gridlittle);
                }
                else {
                    LibraryActivity.this.changeViewMode(GridViewMode.Thumbnail);
                    mButtonChangeView.setImageResource(R.drawable.listbulletslittle);
                }
            }
        });

        mViewSwitcher = (ViewSwitcher)findViewById(R.id.viewswitcher_library_title_left);
        mViewSwitcher.setDisplayedChild(0);
        
        mSDCardUnmountedReceiver = new BroadcastReceiver()
        {
            
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "receive broadcast: " + intent.getAction() + ", " + intent.getDataString());
                
                if (mLoadLibraryItemsTask != null) {
                    mLoadLibraryItemsTask.cancel(true);
                    mLoadLibraryItemsTask = null;
                    Log.d(TAG, "onReceive: mLoadLibraryItemsTask stopped");
                }
                
                if (mLoadBookMetadataTask != null) {
                    mLoadBookMetadataTask.cancel(true);
                    mLoadBookMetadataTask = null;
                    Log.d(TAG, "onReceive: mLoadBookMetadataTask stopped");
                }
                
                long time_start = System.currentTimeMillis();
                String sd_path = FileUtil.getFilePathFromUri(intent.getDataString());
                OnyxItemURI sd_root = GridItemManager.getURIFromFilePath(sd_path);
                ArrayList<GridItemData> left = new ArrayList<GridItemData>();
                
                ArrayList<GridItemData> items = mAdapter.getItems();
                
                String str_root_uri = sd_root.toString();
                Log.d(TAG, "unmounted sdcard: " + str_root_uri);
                for (GridItemData i : items) {
                    if (!i.getURI().isChildOf(sd_root)) {
                        left.add(i);
                    }
                }
                mAdapter.fillItems(mAdapter.getHostURI(), left);
                
                long time_remove = System.currentTimeMillis() - time_start;
                
                Log.d(TAG, "items left: " + left.size());
                Log.d(TAG, "filter time: " + time_remove + "ms");
            }
        };
        IntentFilter filter = IntentFilterFactory.getSDCardUnmountedFilter();
        registerReceiver(this.mSDCardUnmountedReceiver, filter);

        mAdapter = new LibraryAdapter(this, mFileGridView.getGridView());
        mAdapter.getPageLayout().setViewMode(LibraryActivity.ViewMode);
        mFileGridView.getGridView().setAdapter(mAdapter);
        mFileGridView.getGridView().setVisibility(View.INVISIBLE);
        
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
        
        mLoadLibraryItemsTask = new LoadLibraryItemsTask(this);
        mLoadLibraryItemsTask.execute();
        
        mFirstLetterGridView = (OnyxGridView) findViewById(R.id.letter_gridview);
        
        mFirstLetterGridView.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (LibraryActivity.SortPolicy == SortOrder.Name) {
                    String letter = mLetterAdapter.getItems().get(position).getText();
                    for (int i = 0; i < mAdapter.getItems().size(); i++) {
                        String name = mAdapter.getItems().get(i).getText();
                        if (name.regionMatches(true, 0, letter, 0, 1)) {
                            mAdapter.locatePageByItemIndex(i);
                            break;
                        }
                    }
                }
            }
        });
        
        String[] letters = LibraryActivity.this.getResources().getStringArray(R.array.letter);
        ArrayList<GridItemData> items = new ArrayList<GridItemData>();
        for (int i = 0; i < letters.length; i++) {
            GridItemData item_data = new GridItemData(null, letters[i], null);
            items.add(item_data);
        }
        mLetterAdapter = new LibraryFirstLetterAdapter(this,mFirstLetterGridView);
        mLetterAdapter.fillItems(null, items);
        mFirstLetterGridView.setAdapter(mLetterAdapter);
        
        mProgressDialog = new DialogProgressBarRotundity(this);
        mProgressDialog.show();
        
        if (CopyService.getSourceItems() != null) {
            mFileGridView.onPreparePaste();
        }
        
        mButtonTag = (ImageButton)findViewById(R.id.button_tag);
        mButtonTag.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogTagCloud tagCloud = new DialogTagCloud(LibraryActivity.this);
                tagCloud.show();
                
            }
        });
        
       

        this.initGridViewItemNavigation();
        this.registerLongPressListener();
    }
    
    @Override
    protected void onDestroy()
    {
        if (mSDCardUnmountedReceiver != null) {
            unregisterReceiver(mSDCardUnmountedReceiver);
        }
        
        if (mLoadLibraryItemsTask != null) {
            mLoadLibraryItemsTask.cancel(true);
            mLoadLibraryItemsTask = null;
        }
        
        if (mLoadBookMetadataTask != null) {
            mLoadBookMetadataTask.cancel(true);
            mLoadBookMetadataTask = null;
        }
        
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if (((OnyxFileGridView)findViewById(R.id.gridview_library)).getButtonCancel().isFocused() == true
			|| ((OnyxFileGridView)findViewById(R.id.gridview_library)).getButtonCopy().isFocused() == true
			|| ((OnyxFileGridView)findViewById(R.id.gridview_library)).getButtonCut().isFocused() == true
			|| ((OnyxFileGridView)findViewById(R.id.gridview_library)).getButtonDelete().isFocused() == true
			|| ((OnyxFileGridView)findViewById(R.id.gridview_library)).getButtonPaste().isFocused() == true) {
    		ScreenUpdateManager.invalidate(((OnyxFileGridView)findViewById(R.id.gridview_library)), UpdateMode.GU);
		}

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mAdapter.getMultipleSelectionMode()) {
                mFileGridView.onCancelMultipleSelection();
                return true;
            }
        }
        
        if ((keyCode == KeyEvent.KEYCODE_DPAD_UP) || 
                (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) ||
                (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ||
                (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
            ScreenUpdateManager.invalidate(this.getWindow().getDecorView(), UpdateMode.DW);
            
            if (this.getCurrentFocus() != null) {
                int direction = 0;
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    direction = View.FOCUS_UP;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    direction = View.FOCUS_DOWN;
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    direction = View.FOCUS_LEFT;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    direction = View.FOCUS_RIGHT;
                    break;
                default:
                    assert(false);
                    throw new IndexOutOfBoundsException();
                }

                View dst_view = this.getCurrentFocus().focusSearch(direction);
                if (dst_view == null) {
                    if (direction == View.FOCUS_RIGHT) {
                        dst_view = this.getCurrentFocus().focusSearch(View.FOCUS_LEFT);
                    }
                    else {
                        int reverse_direction = OnyxFocusFinder.getReverseDirection(direction);
                        dst_view = OnyxFocusFinder.findFartherestViewInDirection(this.getCurrentFocus(),
                                reverse_direction);
                    }

                    Rect rect = OnyxFocusFinder.getAbsoluteFocusedRect(this.getCurrentFocus());

                    if (dst_view instanceof OnyxGridView) {
                        // simply requestFocus() wont work here, use the method provided by OnyxGridView instead
                        ((OnyxGridView)dst_view).searchAndSelectNextFocusableChildItem(direction, rect);
                    }
                    else {
                        dst_view.requestFocus(direction, rect);
                    }

                    return true;
                }
            }
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void initGridViewItemNavigation()
    {

        mFirstLetterGridView.setCrossVertical(true);
        mFirstLetterGridView.setCrossHorizon(true);
    }

    private void loadBookMetadataAsync()
    {
        if (mLoadBookMetadataTask != null) {
            mLoadBookMetadataTask = null;
        }
        
        RefValue<LoadBookMetadataTask> result = new RefValue<LoadBookMetadataTask>();
        LoadBookMetadataTask.runTask(this, mFileGridView.getGridView(), result);
        
        if (result.getValue() != null) {
            mLoadBookMetadataTask = result.getValue();
        }
    }
}
