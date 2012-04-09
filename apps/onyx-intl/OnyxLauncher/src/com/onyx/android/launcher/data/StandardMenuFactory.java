/**
 * 
 */
package com.onyx.android.launcher.data;

import java.lang.reflect.Method;

import android.app.Activity;
import android.util.Log;

import com.onyx.android.launcher.R;
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
    
    public enum FileOperationMenuItem { New, NewFolder, Rename, Copy, Cut, Remove, Property, GotoFolder, }
    
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
        
        item = new OnyxMenuItem(R.string.menu_file_property, R.drawable.file, true);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {

            @Override
            public void onClick()
            {
                handler.onProperty();
            }
        });
        row.getMenuItems().add(item);
        
        item = new OnyxMenuItem(R.string.menu_file_goto_folder, R.drawable.file, true);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {

            @Override
            public void onClick()
            {
                handler.onGotoFolder();
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
                FileOperationMenuItem.Remove });
    }
    
    public static OnyxMenuSuite getSystemMenuSuite(final Activity hostActivity)
    {
        OnyxMenuSuite suite = new OnyxMenuSuite(R.string.menu_suite_system, R.drawable.ic_menu_preferences);
        
        OnyxMenuRow row = new OnyxMenuRow();
        
        OnyxMenuItem item = new OnyxMenuItem(R.string.menu_system_notification, R.drawable.ic_menu_notifications, true);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {
            
            @Override
            public void onClick()
            {
                try {
                    Object service  = hostActivity.getSystemService("statusbar");
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
