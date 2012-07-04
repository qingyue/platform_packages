/**
 * 
 */
package com.onyx.android.launcher.data;

import java.lang.reflect.Method;

import android.content.Intent;
import android.util.Log;

import com.onyx.android.launcher.OnyxBaseActivity;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.dialog.DialogScreenRotation;
import com.onyx.android.sdk.data.util.ActivityUtil;
import com.onyx.android.sdk.ui.menu.OnyxMenuItem;
import com.onyx.android.sdk.ui.menu.OnyxMenuItem.OnMenuItemClickListener;
import com.onyx.android.sdk.ui.menu.OnyxMenuRow;
import com.onyx.android.sdk.ui.menu.OnyxMenuSuite;


/**
 * @author joy
 *
 */
public class StandardMenuFactory
{
    private static final String TAG = "StandardMenuFactory";
    
    public enum FileOperationMenuItem { New, NewFolder, Rename, Copy, Cut, Remove, Property, GotoFolder, Multiple}
    
    public interface IFileOperationHandler
    {
        void onNewFile();
        void onNewFolder();
        void onRename();
        void onCopy();
        void onCut();
        void onRemove();
        void onProperty();
        void onGotoFolder();
    }
    
    /**
     * FileOperationMenuItem.Property will be always enabled
     * 
     * @param handler
     * @param enabledItems
     * @return
     */
    public static OnyxMenuSuite getFileOperationMenuSuite(final IFileOperationHandler handler, 
            FileOperationMenuItem[] enabledItems)
    {
        OnyxMenuSuite suite = new OnyxMenuSuite(R.string.menu_suite_file, R.drawable.ic_menu_archive);
        
        OnyxMenuRow row = new OnyxMenuRow();
        
        OnyxMenuItem item = null;
        
        boolean enabled = testContains(enabledItems, FileOperationMenuItem.New); 
        item = new OnyxMenuItem(R.string.menu_file_new, R.drawable.new_file, enabled);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {

            @Override
            public void onClick()
            {
                handler.onNewFile();
            }
        });
        row.getMenuItems().add(item);
            
        enabled = testContains(enabledItems, FileOperationMenuItem.NewFolder); 
        item = new OnyxMenuItem(R.string.menu_file_new_folder, R.drawable.new_folder, enabled);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {

            @Override
            public void onClick()
            {
                handler.onNewFolder();
            }
        });
        row.getMenuItems().add(item);
            
        enabled = testContains(enabledItems, FileOperationMenuItem.Rename); 
        item = new OnyxMenuItem(R.string.menu_file_rename, R.drawable.file_rename, enabled);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {

            @Override
            public void onClick()
            {
                handler.onRename();
            }
        });
        row.getMenuItems().add(item);

        if (row.getMenuItems().size() > 0) {
            suite.getMenuRows().add(row);
        }
        
        row = new OnyxMenuRow();
        
        enabled = testContains(enabledItems, FileOperationMenuItem.Copy); 
        item = new OnyxMenuItem(R.string.menu_file_copy, R.drawable.file_copy, enabled);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {

            @Override
            public void onClick()
            {
                handler.onCopy();
            }
        });
        row.getMenuItems().add(item);
            
        enabled = testContains(enabledItems, FileOperationMenuItem.Cut); 
        item = new OnyxMenuItem(R.string.menu_file_cut, R.drawable.file_cut, enabled);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {

            @Override
            public void onClick()
            {
                handler.onCut();
            }
        });
        row.getMenuItems().add(item);
            
        enabled = testContains(enabledItems, FileOperationMenuItem.Remove);
        item = new OnyxMenuItem(R.string.menu_file_remove, R.drawable.remove, enabled);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {

            @Override
            public void onClick()
            {
                handler.onRemove();
            }
        });
        row.getMenuItems().add(item);
        
        if (row.getMenuItems().size() > 0) {
            suite.getMenuRows().add(row);
        }
        
        row = new OnyxMenuRow();

        enabled = testContains(enabledItems, FileOperationMenuItem.Property);
        item = new OnyxMenuItem(R.string.menu_file_property, R.drawable.file, enabled);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {

            @Override
            public void onClick()
            {
                handler.onProperty();
            }
        });
        row.getMenuItems().add(item);

        enabled = testContains(enabledItems, FileOperationMenuItem.GotoFolder);
        item = new OnyxMenuItem(R.string.menu_file_goto_folder, R.drawable.file, enabled);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {

            @Override
            public void onClick()
            {
                handler.onGotoFolder();
            }
        });
        row.getMenuItems().add(item);

        enabled = testContains(enabledItems, FileOperationMenuItem.Multiple);
    	item = new OnyxMenuItem(R.string.select_mutiple, R.drawable.multi, enabled);
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

    		@Override
    		public void onClick() {
    			if (handler instanceof FileOperationHandler) {
    				GridItemBaseAdapter adapter = ((FileOperationHandler)handler).getAdapter();
    				if (!adapter.getMultipleSelectionMode()) {
    					adapter.setMultipleSelectionMode(true);
    				}
				}
    		}
    	});
    	row.getMenuItems().add(item);
        
        suite.getMenuRows().add(row);
        
        return suite;
    }
    
    public static OnyxMenuSuite getFileOperationMenuSuite(final IFileOperationHandler handler)
    {
        return getFileOperationMenuSuite(handler, new FileOperationMenuItem[] { FileOperationMenuItem.New,
                FileOperationMenuItem.NewFolder, FileOperationMenuItem.Rename, 
                FileOperationMenuItem.Copy, FileOperationMenuItem.Cut, 
                FileOperationMenuItem.Remove, FileOperationMenuItem.Property, FileOperationMenuItem.GotoFolder });
    }

    public static OnyxMenuSuite getSystemMenuSuite(final OnyxBaseActivity activity) {
    	OnyxMenuSuite suite = new OnyxMenuSuite(-1, -1);
    	OnyxMenuRow row = new OnyxMenuRow();

    	OnyxMenuItem item = null;

    	item = new OnyxMenuItem(R.string.Screen_Rotation, R.drawable.screen_rotation, true);
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

    		@Override
    		public void onClick() {
    			DialogScreenRotation rotation = new DialogScreenRotation(activity);
    			rotation.show();
    		}
    	});
    	row.getMenuItems().add(item);

    	item = new OnyxMenuItem(R.string.Safely_Remove_SD, R.drawable.sd, true);
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

    		@Override
    		public void onClick() {
    			Intent i = new Intent(android.provider.Settings.ACTION_MEMORY_CARD_SETTINGS);
    			ActivityUtil.startActivitySafely(activity, i);
    		}
    	});
    	row.getMenuItems().add(item);

    	item = new OnyxMenuItem(R.string.Search, R.drawable.file_search, true);
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

    		@Override
    		public void onClick() {
    			activity.onSearchRequested();
    		}
    	});
    	row.getMenuItems().add(item);

    	item = new OnyxMenuItem(R.string.Exit, R.drawable.exit, true);
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

    		@Override
    		public void onClick() {
    			activity.finish();
    		}
    	});
    	row.getMenuItems().add(item);

    	item = new OnyxMenuItem(R.string.menu_system_notification, R.drawable.ic_menu_notifications, true);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {
            
            @Override
            public void onClick()
            {
                try {
                    Object service  = activity.getSystemService("statusbar");
                    Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                    Method expand = statusbarManager.getMethod("expand");
                    expand.invoke(service);
                 } catch (Exception e) {
                     Log.w(TAG, e);
                 }

            }
        });
        row.getMenuItems().add(item);

    	suite.getMenuRows().add(row);
    	return suite;
    }

    public static OnyxMenuSuite getDummyMenuSuite()
    {
        OnyxMenuSuite suite = new OnyxMenuSuite(R.string.menu_suite_file, R.drawable.file_copy);
        
        OnyxMenuRow row = new OnyxMenuRow();
        row.getMenuItems().add(new OnyxMenuItem(R.string.menu_file_new,
                R.drawable.new_file, true));
        row.getMenuItems().add(new OnyxMenuItem(R.string.menu_file_new_folder,
                R.drawable.new_folder, false));
        row.getMenuItems().add(new OnyxMenuItem(R.string.menu_file_rename,
                R.drawable.file_rename, true));
        suite.getMenuRows().add(row);
        
        return suite;
    }
    
    private static <T> boolean testContains(T[] array, T value)
    {
        for (T e : array) {
            if (e.equals(value)) {
                return true;
            }
        }
        
        return false;
    }
}
