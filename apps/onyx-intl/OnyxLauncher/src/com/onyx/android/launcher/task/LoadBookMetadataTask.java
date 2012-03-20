/**
 * 
 */
package com.onyx.android.launcher.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.sdk.data.cms.OnyxCmsCenter;
import com.onyx.android.sdk.data.cms.OnyxMetadata;
import com.onyx.android.sdk.data.util.FileUtil;
import com.onyx.android.sdk.data.util.RefValue;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class LoadBookMetadataTask extends AsyncTask<Void, GridItemData, Void>
{
    private static final String TAG = "LoadBookMetadataTask";
    
    Activity mActivity = null;
    OnyxGridView mGridView = null;
    ArrayList<GridItemData> mItems = new ArrayList<GridItemData>();
    
    private LoadBookMetadataTask(Activity activity, OnyxGridView gridView, Collection<GridItemData> items)
    {
        mActivity = activity;
        mGridView = gridView;
        mItems.addAll(items);
    }
    
    public static void runTask(Activity hostActivity, OnyxGridView gridView, RefValue<LoadBookMetadataTask> resultTask)
    {
        GridItemBaseAdapter adapter = (GridItemBaseAdapter)gridView.getPagedAdapter();
        
        if ((adapter.getPaginator().getItemCount() > 0) && (adapter.getPaginator().getPageSize() > 0)) {
            int start = adapter.getPaginator().getPageIndex() * adapter.getPaginator().getPageSize();
            int end = Math.min(start + adapter.getPaginator().getPageSize() - 1,
                    adapter.getPaginator().getItemCount() - 1);
            Log.d(TAG, "total Item: " + adapter.getItems().size() +
                    ", dst range: [" + start + ", " + end + "]");
            
            List<GridItemData> sub_items = adapter.getItems().subList(start, end + 1);
            resultTask.setValue(new LoadBookMetadataTask(hostActivity, gridView, sub_items));
            resultTask.getValue().execute();
        }
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        Cursor cursor = null;
        try {
            Log.d(TAG, "loading library metadatas");

            long time_start = System.currentTimeMillis();
            
            long time_md5 = 0;
            long time_db = 0; 
            long time_thumbnail = 0;

            if (this.isCancelled()) {
                return null;
            }                
            
            for (GridItemData item : mItems) {
                if (this.isCancelled()) {
                    return null;
                }
                
                if (!(item instanceof BookItemData)) {
                    continue;
                }
                
                BookItemData book = (BookItemData)item;
                File file = GridItemManager.getFileFromURI(book.getURI()); 
                if (file.isDirectory() || file.isHidden()) {
                    continue;
                }
                
                Log.d(TAG, "current item: " + book.getText());
                if ((book.getMetadata() == null) || this.isDataObsolete(book.getMetadata(), file)) {
                    Log.d(TAG, "metadata obsolete");
                    if (this.isCancelled()) {
                        return null;
                    } 
                    long time_point = System.currentTimeMillis();
                    String md5 = FileUtil.computeMD5(file); 
                    time_md5 += System.currentTimeMillis() - time_point;
                    
                    if (book.getMetadata() == null) {
                        book.setMetadata(new OnyxMetadata());
                        Log.d(TAG, "set metadata on " + book.getURI().getName());
                    }
                    
                    book.getMetadata().setMD5(md5);
                    book.getMetadata().setName(file.getName());
                    book.getMetadata().setLocation(file.getAbsolutePath());
                    book.getMetadata().setSize(file.length());
                    book.getMetadata().setlastModified(file.lastModified());
                    book.getMetadata().setNativeAbsolutePath(file.getAbsolutePath());
                    
                    if (this.isCancelled()) {
                        return null;
                    }
                    time_point = System.currentTimeMillis();
                    Log.d(TAG, "get metadata from db");
                    if (OnyxCmsCenter.getMetadata(mActivity, book.getMetadata())) {
                        Log.d(TAG, "get metadata from db: success");
                        if (!TextUtils.isEmpty(book.getMetadata().getLastAccess())) {
                            Log.d(TAG, "last access: " + book.getMetadata().getLastAccess());
                        }
                        this.publishProgress(book);
                    }
                    time_db += System.currentTimeMillis() - time_point;
                    time_point = System.currentTimeMillis();
                    Log.d(TAG, "thumbnail not null: " + (book.getThumbnail() != null));
                    if (book.getThumbnail() == null) {
                        Bitmap b = this.getThumbnail(book.getMetadata());
                        Log.d(TAG, "get thumbnail from db: " + (b != null));
                        if (b != null) {
                            book.setThumbnail(b);
                            this.publishProgress(book);
                        }
                    }
                    time_thumbnail += System.currentTimeMillis() - time_point;
                }
                else {
                    if (this.isCancelled()) {
                        return null;
                    }
                    // metadata can be altered at else place, so always try sync with cms db  
                    long time_point = System.currentTimeMillis();
                    Log.d(TAG, "get metadata from db");
                    if (OnyxCmsCenter.getMetadata(mActivity, book.getMetadata())) {
                        Log.d(TAG, "get metadata from db: success");
                        this.publishProgress(book);
                    }
                    time_db += System.currentTimeMillis() - time_point;
                    time_point = System.currentTimeMillis();
                    Log.d(TAG, "thumbnail not null: " + (book.getThumbnail() != null));
                    if (book.getThumbnail() == null) {
                        Bitmap b = this.getThumbnail(book.getMetadata());
                        Log.d(TAG, "get thumbnail from db: " + (b != null));
                        if (b != null) {
                            book.setThumbnail(b);
                            this.publishProgress(book);
                        }
                    }
                    time_thumbnail += System.currentTimeMillis() - time_point;
                }
            }

            long time_end = System.currentTimeMillis();

            Log.d(TAG, "items loaded, count: " + mItems.size());
            Log.d(TAG, "md5 time: " + time_md5 + "ms\n");
            Log.d(TAG, "db load time: " + time_db + "ms\n");
            Log.d(TAG, "thumbnail load time: " + time_thumbnail + "ms\n");
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
    
    private boolean isDataObsolete(OnyxMetadata metadata, File file)
    {
        return (metadata.getLastModified() != file.lastModified()) ||
                (metadata.getSize() != file.length());
    }
    
    /**
     * if fail, return null
     * 
     * @param metadata
     * @return
     */
    private Bitmap getThumbnail(OnyxMetadata metadata)
    {
        RefValue<Bitmap> thumbnail = new RefValue<Bitmap>();
        if (!OnyxCmsCenter.getThumbnail(mActivity, metadata, thumbnail)) {
            return null;
        }
        
        return thumbnail.getValue();
    }
    
    @Override
    protected void onProgressUpdate(GridItemData... values)
    {
        if (this.isCancelled()) {
            return;
        }
        mGridView.getPagedAdapter().notifyDataSetChanged();
    }

}
