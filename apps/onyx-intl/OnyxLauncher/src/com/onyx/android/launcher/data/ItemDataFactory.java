/**
 * 
 */
package com.onyx.android.launcher.data;

import java.io.File;

import com.onyx.android.sdk.data.cms.OnyxLibraryItem;
import com.onyx.android.sdk.data.cms.OnyxMetadata;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData.FileType;

/**
 * @author joy
 *
 */
public class ItemDataFactory
{
    /**
     * return null if file not exist
     * 
     * @param item
     * @return
     */
    public static BookItemData create(OnyxLibraryItem item)
    {
        File f = new File(item.getPath());
        if (!f.exists()) {
            return null;
        }
        
        FileType t = f.isDirectory() ? FileType.Directory : FileType.NormalFile;
        
        return new BookItemData(GridItemManager.getURIFromFilePath(item.getPath()), t,
                item.getName(), FileIconFactory.getIconByFileName(item.getName()));
    }
    
    /**
     * return null if file not exist
     * 
     * @param data
     * @return
     */
    public static BookItemData create(OnyxMetadata data)
    {
        File f = new File(data.getLocation());
        if (!f.exists()) {
            return null;
        }
        
        FileType t = f.isDirectory() ? FileType.Directory : FileType.NormalFile;
        
        BookItemData book = new BookItemData(GridItemManager.getURIFromFilePath(data.getLocation()), t,
                data.getName(), FileIconFactory.getIconByFileName(data.getName()));
        book.setMetadata(data);
        return book;
    }
}
