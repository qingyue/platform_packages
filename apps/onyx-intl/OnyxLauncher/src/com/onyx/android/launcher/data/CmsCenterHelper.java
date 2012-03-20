/**
 * 
 */
package com.onyx.android.launcher.data;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.onyx.android.launcher.OnyxCmsProvider;
import com.onyx.android.launcher.util.EventedArrayList;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.data.cms.OnyxCmsCenter;
import com.onyx.android.sdk.data.cms.OnyxLibraryItem;
import com.onyx.android.sdk.data.cms.OnyxMetadata;
import com.onyx.android.sdk.data.util.FileUtil;
import com.onyx.android.sdk.data.util.RefValue;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class CmsCenterHelper
{
    private static final String TAG = "OnyxCMSCenterHelper";
    
    private static ArrayList<BookItemData> sRecentReadings = null;
    
    public static boolean getLibraryItems(Activity activity, SortOrder sortOrder, EventedArrayList<GridItemData> result)
    {
        ArrayList<OnyxLibraryItem> items = new ArrayList<OnyxLibraryItem>();
        if (!OnyxCmsCenter.getLibraryItems(activity, sortOrder, items)) {
            return false;
        }
        
        for (OnyxLibraryItem i : items) {
            result.add(ItemDataFactory.create(i));
        }
        
        return true;
    }
    
    public static boolean searchLibraryItem(Activity activity, String pattern, EventedArrayList<GridItemData> result)
    {
        ArrayList<OnyxLibraryItem> items = new ArrayList<OnyxLibraryItem>();
        if (!OnyxCmsCenter.getLibraryItems(activity, items)) {
            return false;
        }
        
        for (OnyxLibraryItem i : items) {
            GridItemData data = ItemDataFactory.create(i);
            if (data.match(pattern)) {
                result.add(data);
            }
        }
        
        return true;
    }
    
    /**
     * get file's meta data
     * 
     * @param activity
     * @param file
     * @param result
     * @param errMsg
     * @return
     */
    public static boolean getOrCreateMetadata(Activity activity, BookItemData item,
            RefValue<OnyxMetadata> result, RefValue<String> errMsg)
    {
        OnyxMetadata data = new OnyxMetadata();
        try {
            File file = GridItemManager.getFileFromURI(item.getURI());
            long time_point = System.currentTimeMillis();
            String md5 = FileUtil.computeMD5(file);
            long time_md5 = System.currentTimeMillis() - time_point;
            Log.d(TAG, "times md5: " + time_md5);

            data.setMD5(md5);
            data.setName(file.getName());
            data.setLocation(file.getAbsolutePath());
            data.setSize(file.length());
            data.setlastModified(file.lastModified());
            data.setNativeAbsolutePath(file.getAbsolutePath());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.w(TAG, "exception caught: ", e);
            errMsg.setValue("open file failed");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "exception caught: ", e);
            errMsg.setValue("open file failed");
            return false;
        } 
        
        if (OnyxCmsCenter.getMetadata(activity, data)) {
            result.setValue(data);
            return true;
        }
        else {
            if (!OnyxCmsCenter.insertMetadata(activity, data)) {
                errMsg.setValue("db insert falied");
                return false;
            }
            result.setValue(data);
            return true;
        }
    }
    
    /**
     * never return null, return an empty collection if failed
     * 
     * @param activity
     * @return
     */
    public static ArrayList<BookItemData> getRecentReadings(Activity activity)
    {
        if (sRecentReadings != null) {
            return sRecentReadings;
        }
        
        ArrayList<OnyxMetadata> datas = new ArrayList<OnyxMetadata>();
        if (!OnyxCmsCenter.getRecentReadings(activity, datas)) {
            return new ArrayList<BookItemData>();
        }
        Log.d(TAG, "history item count: " + datas.size());
        
        sRecentReadings = new ArrayList<BookItemData>();
        for (OnyxMetadata d : datas) {
            BookItemData book = ItemDataFactory.create(d);
            if (book != null) {
                sRecentReadings.add(book);
            }
        }
        
        return sRecentReadings;
    }
    
    /**
     * update item's Last Access field
     * 
     * @param activity
     * @param item
     * @param metadata
     * @return
     */
    public static boolean updateRecentReading(Activity activity, BookItemData item, OnyxMetadata metadata)
    {
        String last_access = OnyxMetadata.DATE_FORMAT.format(new Date());
        Log.d(TAG, "updating recent reading: " + item.getText() + ", " + last_access);
        if (OnyxCmsCenter.getMetadata(activity, metadata)) {
            metadata.setLastAccess(last_access);
            if (!OnyxCmsCenter.updateMetadata(activity, metadata)) {
                return false;
            }
        }
        else {
            metadata.setLastAccess(last_access);
            if (!OnyxCmsCenter.insertMetadata(activity, metadata)) {
                return false;
            }
        }
        Log.d(TAG, "update success");
        
        if (item.getMetadata() != null) {
            // TRY update item's last access data
            item.getMetadata().setLastAccess(last_access);
        }
        
        GridItemData remove = null;
        
        if (sRecentReadings == null) {
            getRecentReadings(activity);
            if (sRecentReadings == null) {
                return false;
            }
        }
        
        for (GridItemData i : sRecentReadings) {
            if (i.getURI().equals(item.getURI())) {
                remove = i;
                break;
            }
        } 
        if (remove != null) {
            sRecentReadings.remove(remove);
        }
        
        sRecentReadings.add(0, item);
        
        return true;
    }
    
    public static boolean removeRecentReading(Activity activity, BookItemData item)
    {
        if (sRecentReadings == null) {
            assert(false);
            return false;
        }
        
        if (item.getMetadata() == null) {
            assert(false);
            return false;
        }
        
        if (OnyxCmsCenter.getMetadata(activity, item.getMetadata())) {
            final String erased = "";
            item.getMetadata().setLastAccess(erased);
            if (!OnyxCmsCenter.updateMetadata(activity, item.getMetadata())) {
                return false;
            }
        }
        
        GridItemData remove = null;
        for (GridItemData i : sRecentReadings) {
            if (i.getURI().equals(item.getURI())) {
                remove = i;
                break;
            }
        } 
        if (remove != null) {
            sRecentReadings.remove(remove);
        }
        
        return true;
    }
    
    /**
     * only for OnyxLauncher, using internal storing knowledge of thumbnail to optimize read time
     * 
     * @param context
     * @param metadata
     * @param result
     * @return
     */
    public static boolean getThumbnail(Context context, OnyxMetadata metadata, RefValue<Bitmap> result)
    {
        if (metadata == null) {
            assert(false);
            return false;
        }
        
        String path = OnyxCmsProvider.getThumbnailFile(context, metadata.getMD5());
        File f = new File(path);
        if (!f.exists()) {
            return false;
        }
        
        Bitmap b = BitmapFactory.decodeFile(path);
        if (b == null) {
            return false;
        }
        
        result.setValue(b);
        return true;
    }
}
