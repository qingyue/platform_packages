/**
 * 
 */
package com.onyx.android.launcher;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.data.StandardMenuFactory;
import com.onyx.android.launcher.dialog.DialogContextMenu;
import com.onyx.android.launcher.dialog.DialogScreenRotation;
import com.onyx.android.sdk.data.util.ActivityUtil;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;
import com.onyx.android.sdk.ui.menu.OnyxMenuSuite;
import com.onyx.android.sdk.ui.util.OnyxFocusFinder;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * a OnysBaseActivity must have a OnyxGridView as main GridView
 * 
 * @author joy
 *
 */
public abstract class OnyxBaseActivity extends Activity
{
    private static final String TAG = "OnyxBaseActivity";

    /**
     * get Activity's main GridView
     * 
     * @return
     */
    public abstract OnyxGridView getGridView();

    /**
     * get current selected item, null stands for non-selection
     * 
     * @return
     */
    public GridItemData getSelectedGridItem()
    {
        if (this.getGridView().getSelectedView() != null) {
            return (GridItemData)this.getGridView().getSelectedView().getTag();
        }
        return null;
    }

    public void changeViewMode(GridViewMode viewMode)
    {
        ScreenUpdateManager.invalidate(this.getGridView(), UpdateMode.GU);
        if (this.getGridView().getPagedAdapter().getPageLayout().getViewMode() != viewMode) {
            this.getGridView().getPagedAdapter().getPageLayout().setViewMode(viewMode);

            if (viewMode == GridViewMode.Thumbnail) {
                this.getGridView().setNumColumns(GridView.AUTO_FIT);
            } else {
                assert (viewMode == GridViewMode.Detail);
                this.getGridView().setNumColumns(1);
            }
        }
    }
    
    public void registerLongPressListener()
    {
        if (this.getGridView() == null) {
            assert(false);
            return;
        }
        
        this.getGridView().setOnItemLongClickListener(new OnItemLongClickListener()
        {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                return true;
            }
        });
        this.getGridView().registerOnLongPressListener(new OnyxGridView.OnLongPressListener()
        {
            
            @Override
            public void onLongPress()
            {
            	ArrayList<OnyxMenuSuite> suites = OnyxBaseActivity.this.getContextMenuSuites();
                new DialogContextMenu(OnyxBaseActivity.this, suites).show();
            }
        });
    }
    
    /**
     * should be light weight method
     * 
     * never return null, when return empty collection, means there is no context menu in this activity
     * 
     * @return
     */
    public ArrayList<OnyxMenuSuite> getContextMenuSuites()
    {
        ArrayList<OnyxMenuSuite> suites = new ArrayList<OnyxMenuSuite>();
        suites.add(StandardMenuFactory.getSystemMenuSuite(this));
        return suites;
    }

    @Override
    protected void onResume() {
    	Log.d(TAG, "onResume");

    	super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestory called on thread: " + Thread.currentThread().getName());

        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
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
                    int reverse_direction = OnyxFocusFinder.getReverseDirection(direction);
                    dst_view = OnyxFocusFinder.findFartherestViewInDirection(this.getCurrentFocus(), reverse_direction);

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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.home_option_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        ArrayList<OnyxMenuSuite> suites = this.getContextMenuSuites();
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == R.id.home_option_menu_other) {
                if (suites == null || suites.size() == 0) {
                    menu.getItem(i).setEnabled(false);
                }
                else {
                    menu.getItem(i).setEnabled(true);
                }
            }
        }

        OnyxBaseActivity.this.getWindow().getDecorView().requestFocusFromTouch();

        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public void openOptionsMenu()
    {
        // TODO Auto-generated method stub
        super.openOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.home_option_menu_screen_rotation:
        	DialogScreenRotation rotation = new DialogScreenRotation(this);
        	rotation.show();
            return true;
        case R.id.home_option_menu_storage_settings:
            this.mountSdCard();
            return true;
        case R.id.home_option_menu_other:
            ArrayList<OnyxMenuSuite> suites = this.getContextMenuSuites();
            Log.d(TAG, "menu suites: " + suites.size());
            new DialogContextMenu(this, suites).show();
            return true;
        case R.id.home_option_menu_search:
            this.onSearchRequested();
            return true;
        case R.id.home_option_menu_exit:
            this.finish();
            return true;
        case R.id.home_option_menu_select_mutiple:
            GridItemBaseAdapter adapter = (GridItemBaseAdapter)this.getGridView().getPagedAdapter();
            if (!adapter.getMultipleSelectionMode()) {
                adapter.setMultipleSelectionMode(true);
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSearchRequested()
    {
        GridItemBaseAdapter adapter = (GridItemBaseAdapter)this.getGridView().getPagedAdapter();

        Bundle app_data = new Bundle();
        app_data.putString(SearchResultActivity.HOST_URI, adapter.getHostURI().toString());
        this.startSearch(null, false, app_data, false);

        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id) {
        case 0:
        default:
            return super.onCreateDialog(id);
        }
    }

    protected void disabledMenuMultiple(Menu menu)
    {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == R.id.home_option_menu_select_mutiple) {
                menu.getItem(i).setEnabled(false);
            }
        }
    }
    
    protected void initGridViewItemNavigation()
    {
        this.getGridView().setCrossVertical(true);
        this.getGridView().setCrossHorizon(false);
    }

    private void mountSdCard()
    {
        Intent i = new Intent(android.provider.Settings.ACTION_MEMORY_CARD_SETTINGS);
        ActivityUtil.startActivitySafely(this, i);
    }
}
