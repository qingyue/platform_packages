package com.onyx.android.launcher;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.onyx.android.launcher.adapter.BookmarkGridViewAdapter;
import com.onyx.android.launcher.adapter.BookrackGridViewAdapter;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.adapter.OnyxGridAppsItemAdapter;
import com.onyx.android.launcher.adapter.OnyxGridItemAdapter;
import com.onyx.android.launcher.data.CmsCenterHelper;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.dialog.DialogContextMenu;
import com.onyx.android.launcher.dialog.DialogRecentReadingRemove;
import com.onyx.android.launcher.view.OnyxGridViewCanPaged;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.data.sys.OnyxSysCenter;
import com.onyx.android.sdk.data.util.ActivityUtil;
import com.onyx.android.sdk.data.util.RefValue;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;
import com.onyx.android.sdk.ui.menu.OnyxMenuSuite;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdatePolicy;

public class LauncherActivity extends OnyxBaseActivity
{
    private static final String TAG = "LauncherActivity";
    public static SortOrder SortPolicy = SortOrder.Name;

    private OnyxGridItemAdapter mAdapter = null;
    private OnyxGridView mGridViewMain = null;
    private OnyxGridView mGridViewBookrack = null;
    private BookrackGridViewAdapter mAdapterBookrack = null;
    
    private FileOperationHandler mFileOperationHandler = null;
    
    private OnyxGridView mGridViewShortCuts = null;
    private OnyxGridAppsItemAdapter mShortCutsAdapter = null;
    
    private Button mRecentButton = null;
    private Button mBookmarkButton = null;
    private OnyxGridView mGridViewBookmark = null;
    @SuppressWarnings("unused")
	private BookmarkGridViewAdapter mBookmarkAdapter = null;
    /**
     * helper function of "go home"
     * 
     * @param from
     */
    public static void goLauncherHome(Activity from)
    {
        Intent intent = new Intent(from, LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        ActivityUtil.startActivitySafely(from, intent);
    }

    @Override
    public OnyxGridView getGridView()
    {
        return mGridViewMain;
    }

    @Override
    public GridItemData getSelectedGridItem()
    {
    	if (mGridViewMain.getSelectedView() != null) {
    		return (GridItemData)mGridViewMain.getSelectedView().getTag();
    	}
    	else if (mGridViewBookrack.getSelectedView() != null) {
    		return (GridItemData)mGridViewBookrack.getSelectedView().getTag();
    	} else if (mGridViewShortCuts.getSelectedView() != null) {
            return (GridItemData) mGridViewShortCuts.getSelectedView().getTag();
        }
    	else {
    		return null;
    	}
    }

    @Override
    public void changeViewMode(GridViewMode viewMode)
    {
        // do nothing
    }
    
    @Override
    public void registerLongPressListener()
    {
    	assert(this.getGridView() == mGridViewMain);

    	super.registerLongPressListener();

    	if (mGridViewBookrack == null) {
    		assert(false);
    		return;
    	}
    	mGridViewBookrack.registerOnLongPressListener(new OnyxGridView.OnLongPressListener()
    	{

    		@Override
    		public void onLongPress()
    		{
    			ArrayList<OnyxMenuSuite> suites = LauncherActivity.this.getContextMenuSuites();
    			new DialogContextMenu(LauncherActivity.this, suites).show();
    		}
    	});
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new OnyxUncaughtExceptionHandler(this));

        setContentView(R.layout.activity_home);

        mGridViewBookrack = (OnyxGridView)findViewById(R.id.gridview_bookrack);
        mGridViewBookrack.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                LauncherActivity.this.startGridViewItem(view);
            }
        });

        mAdapterBookrack = new BookrackGridViewAdapter(this, mGridViewBookrack);
        mGridViewBookrack.setAdapter(mAdapterBookrack);
        mAdapterBookrack.registerDataSetObserver(new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                super.onChanged();
                
                LauncherActivity.this.refreshBookrackThumbnail();
            }
            
            @Override
            public void onInvalidated()
            {
                super.onInvalidated();
               
                LauncherActivity.this.refreshBookrackThumbnail();
            }
        });

        mGridViewMain = ((OnyxGridViewCanPaged) findViewById(R.id.onyxgridviewpaged)).getGridView();
        mGridViewMain.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                LauncherActivity.this.startGridViewItem(view);
            }
        });

        mAdapter = new OnyxGridItemAdapter(this, mGridViewMain); 
        mGridViewMain.setAdapter(mAdapter);

        mGridViewShortCuts = (OnyxGridView) findViewById(R.id.gridview_shortcuts);
        mGridViewShortCuts.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent,View view,
                    int position, long id)
            {
                LauncherActivity.this.startGridViewItem(view);
            }
        });
        mShortCutsAdapter = new OnyxGridAppsItemAdapter(this, mGridViewShortCuts);
        mGridViewShortCuts.setAdapter(mShortCutsAdapter);

        // FileItemData.RegisterCreator();
        // DynamicItemData.RegisterCreator();

        GridItemManager.fillDesktop(mGridViewMain, this);
        GridItemManager.fillShortCuts(mGridViewShortCuts, this);
        
        mFileOperationHandler = new FileOperationHandler(this, mAdapterBookrack) 
        {
            @Override
            public void onRemove()
            {
                new DialogRecentReadingRemove(LauncherActivity.this, mFileOperationHandler).show();
            }
        };

        this.initGridViewItemNavigation();
        this.registerLongPressListener();
        
        mRecentButton = (Button) findViewById(R.id.recent_doc_button);
        mRecentButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LauncherActivity.this,RecentDocumentsActivity.class);
				startActivity(intent);
				
			}
		});
        
        mBookmarkButton =(Button) findViewById(R.id.bookmark_button);
        mBookmarkButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "BookMark",Toast.LENGTH_SHORT).show();
				
			}
		});

        mGridViewBookmark = (OnyxGridView) findViewById(R.id.gridview_bookmark);
        mBookmarkAdapter = new BookmarkGridViewAdapter(this, mGridViewBookmark);
        mGridViewBookmark.setAdapter(mAdapterBookrack);

        Log.d(TAG, "onCreate finished");
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "onResume");
        super.onResume();
        
        GridItemBaseAdapter adapter = (GridItemBaseAdapter)mGridViewBookrack.getPagedAdapter();
        adapter.fillItems(null, CmsCenterHelper.getRecentReadings(this));
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        
		if (!OnyxApplication.UpdatePolicyInitialized) {
		    if (OnyxSysCenter.init(LauncherActivity.this)) {
			    int initValue = OnyxSysCenter.getScreenUpdateGCInterval();
			    if (initValue == -1) {
				    ScreenUpdateManager.setUpdatePolicy(this.getWindow()
							.getDecorView(), UpdatePolicy.GUIntervally, 0);
				} else {
				    ScreenUpdateManager.setUpdatePolicy(this.getWindow()
							.getDecorView(), UpdatePolicy.GUIntervally,
							initValue);
				}
			}

			OnyxApplication.UpdatePolicyInitialized = true;
		}
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        LauncherActivity.this.disabledMenuMultiple(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (this.getGridView().getPagedAdapter().getPaginator().getPageCount() > 0) {
                this.getGridView().getPagedAdapter().getPaginator().setPageIndex(0);
            }
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this.getGridView().getPagedAdapter().getPaginator().canPrevPage()) {
                this.getGridView().getPagedAdapter().getPaginator().prevPage();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void initGridViewItemNavigation()
    {
        mGridViewMain.setCrossVertical(true);
        mGridViewMain.setCrossHorizon(true);
        mGridViewMain.enableOnFling(false);

        mGridViewBookrack.setCrossVertical(true);
        mGridViewBookrack.setCrossHorizon(true);
        mGridViewBookrack.enableOnFling(false);
        
        mGridViewShortCuts.setCrossVertical(true);
        mGridViewShortCuts.setCrossHorizon(true);
        mGridViewShortCuts.enableOnFling(false);
        
        mGridViewBookrack.setCrossVertical(true);
        mGridViewBookrack.setCrossHorizon(true);
        mGridViewBookrack.enableOnFling(false);
    }

    private void startGridViewItem(View itemView)
    {
        GridItemData item = (GridItemData)itemView.getTag();
        if (item == null) {
            // bookrack can have empty item
            return;
        }

        if (this.startURI(item.getURI())) {
            if (item instanceof BookItemData) {
                BookItemData book = (BookItemData)item;
                assert(book.getMetadata() != null);
                CmsCenterHelper.updateRecentReading(this, book, book.getMetadata());
            }
        }
    }
    
    private void refreshBookrackThumbnail()
    {
        Log.d(TAG, "refreshBookrackThumbnail");
        ArrayList<BookItemData> books = CmsCenterHelper.getRecentReadings(this);
        int size = Math.min(books.size(), mAdapterBookrack.getPaginator().getPageSize());
        Log.d(TAG, "book count: " + size);
        // show thumbnail of recent books in Launcher
        for (int i = 0; i < size; i++) {
            BookItemData b = books.get(i);
            if (b.getMetadata() == null) {
                assert(false);
                continue;
            }
            else {
                if (b.getThumbnail() == null) {
                    RefValue<Bitmap> thumbnail = new RefValue<Bitmap>();
                    if (CmsCenterHelper.getThumbnail(this, b.getMetadata(), thumbnail)) {
                        b.setThumbnail(thumbnail.getValue());
                    }
                }
            }
        }
    }

    public boolean startURI(OnyxItemURI uri)
    {
        GridItemBaseAdapter adapter = (GridItemBaseAdapter)mGridViewMain.getPagedAdapter();

        if (uri.equals(GridItemManager.getStorageURI())) {
            // start new Storage Activity can not be elegantly done in StorageActor.process(), 
            // so explicitly start activity here
            return StorageActivity.startStorageActivity(this, GridItemManager.getStorageURI());
        }
        else if (!uri.equals(adapter.getHostURI())) {
            return GridItemManager.processURI(mGridViewMain, uri, this);
        }

        return false;
    }
}
