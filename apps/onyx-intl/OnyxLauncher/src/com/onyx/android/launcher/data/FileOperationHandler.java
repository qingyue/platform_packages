/**
 * 
 */
package com.onyx.android.launcher.data;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.onyx.android.launcher.StorageActivity;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.data.StandardMenuFactory.IFileOperationHandler;
import com.onyx.android.launcher.dialog.DialogFileNew;
import com.onyx.android.launcher.dialog.DialogFileNewFolder;
import com.onyx.android.launcher.dialog.DialogFileProperty;
import com.onyx.android.launcher.dialog.DialogFileRemove;
import com.onyx.android.launcher.dialog.DialogFileRename;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.data.FileItemData;

/**
 * @author joy
 *
 */
public class FileOperationHandler implements IFileOperationHandler
{
    private Context mContext = null;
    private GridItemBaseAdapter mAdapter = null;
    private ArrayList<FileItemData> mSourceItems = new ArrayList<FileItemData>();
    
    public FileOperationHandler(Context context, GridItemBaseAdapter adapter)
    {
        mContext = context;
        mAdapter = adapter;
    }
    
    public Context getContext()
    {
        return mContext;
    }
    public GridItemBaseAdapter getAdapter()
    {
        return mAdapter;
    }
    
    public ArrayList<FileItemData> getSourceItems()
    {
        return mSourceItems;
    }
    
    public void setSourceItems(ArrayList<FileItemData> items)
    {
        mSourceItems.clear();
        mSourceItems.addAll(items);
    }
    
    @Override
    public void onNewFile()
    {
        new DialogFileNew(this.getContext(), this).show();
    }

    @Override
    public void onNewFolder()
    {
        new DialogFileNewFolder(this.getContext(), this).show();
    }

    @Override
    public void onRename()
    {
        if (mSourceItems.size() > 1) {
            Toast.makeText(mContext, "can rename only one item at one time", Toast.LENGTH_SHORT);
            return;
        }
        
        new DialogFileRename(this.getContext(), this, mSourceItems.get(0)).show();
    }

    @Override
    public void onCopy()
    {
        CopyService.copy(this.getSourceItems());
    }

    @Override
    public void onCut()
    {
        CopyService.cut(this.getSourceItems());
    }

    @Override
    public void onRemove()
    {
        DialogFileRemove dialogDeleteFile = new DialogFileRemove(this.getContext(), this);
        dialogDeleteFile.show();
    }
    
    @Override
    public void onProperty()
    {
        if (mSourceItems.size() > 1) {
            Toast.makeText(mContext, "more than 1 item", Toast.LENGTH_SHORT);
            return;
        }
        DialogFileProperty dlg = new DialogFileProperty(this.getContext(), mSourceItems.get(0));
        dlg.show();
    }
    
    @Override
    public void onGotoFolder()
    {
        if (mSourceItems.size() > 1) {
            Toast.makeText(mContext, "more than 1 item", Toast.LENGTH_SHORT);
            return;
        }
        if (mContext instanceof Activity) {
            OnyxItemURI uri = mSourceItems.get(0).getURI();
            if (uri.isChildOf(GridItemManager.getStorageURI())) {
                OnyxItemURI uri_folder = uri.getParent();
                StorageActivity.startStorageActivity((Activity)mContext, uri_folder);
            }
        }
    }
}
