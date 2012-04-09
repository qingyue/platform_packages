/**
 * 
 */
package com.onyx.android.launcher.data;

import java.util.ArrayList;

import com.onyx.android.sdk.ui.data.FileItemData;

/**
 * @author joy
 *
 */
public class CopyService
{
    private static ArrayList<FileItemData> sSourceItems = null;
    private static boolean sIsCut = false;
    
    public static ArrayList<FileItemData> getSourceItems()
    {
        return sSourceItems;
    }
    public static boolean isCut()
    {
        return sIsCut;
    }
    
    public static void copy(ArrayList<FileItemData> items)
    {
        sSourceItems = items;
        sIsCut = false;
    }
    
    public static void cut(ArrayList<FileItemData> items)
    {
        sSourceItems = items;
        sIsCut = true;
    }
    
    public static void clean()
    {
        sSourceItems = null;
    }
}
