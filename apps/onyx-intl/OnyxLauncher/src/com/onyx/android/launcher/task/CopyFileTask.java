package com.onyx.android.launcher.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.os.AsyncTask;
import android.widget.Toast;

import com.onyx.android.launcher.OnyxBaseActivity;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.data.FileIconFactory;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.SDFileFactory;
import com.onyx.android.launcher.dialog.DialogFileOperations;
import com.onyx.android.sdk.ui.data.FileItemData;
import com.onyx.android.sdk.ui.data.FileItemData.FileType;

public class CopyFileTask extends AsyncTask<Void, FileItemData, Void>
{
    private OnyxBaseActivity mActivity = null;
    private GridItemBaseAdapter mAdapter = null;
    private File mSourceFile = null;
    private File mFile = null;
    private DialogFileOperations mDialogFileOperations = null;
    private List<File> mFiles = new ArrayList<File>();

    public CopyFileTask(OnyxBaseActivity activity, Collection<FileItemData> items)
    {
        mActivity = activity;
        mAdapter = (GridItemBaseAdapter)mActivity.getGridView().getPagedAdapter();
        
        for (FileItemData i : items) {
            mFiles.add(GridItemManager.getFileFromURI(i.getURI()));
        }
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        mFile = GridItemManager.getFileFromURI(mAdapter.getHostURI());

        for (int i = 0; i < mFiles.size(); i++) {
        	if (mDialogFileOperations == null && !mDialogFileOperations.getIsShowing()) {
        		this.cancel(true);
        		return null;
        	}

            mSourceFile = mFiles.get(i);
            if (mFile.getPath().startsWith(mSourceFile.getPath())) {
            	this.cancel(true);
            	return null;
            }

            if (!SDFileFactory.copy(mSourceFile, mFile, mDialogFileOperations)) {
            	this.cancel(true);
            	return null;
            }

            FileItemData fileItemData = null;
            String str = mFile.getPath() + File.separator + mSourceFile.getName();
            mSourceFile = new File(str);
            if (mSourceFile.isFile()) {
                fileItemData = new FileItemData(GridItemManager.getURIFromFilePath(str), FileType.NormalFile,
                        mSourceFile.getName(), FileIconFactory.getIconByFileName(mSourceFile.getName()));
            }
            else if (mSourceFile.isDirectory()) {
                fileItemData = new FileItemData(GridItemManager.getURIFromFilePath(str), FileType.Directory,
                        mSourceFile.getName(), R.drawable.directory);
            }

            publishProgress(fileItemData);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(FileItemData... values)
    {
        if (!mDialogFileOperations.isShowing()) {
        	CopyFileTask.this.cancel(true);
        }

        mAdapter.appendItems(new FileItemData[] { values[0] });
    }

    @Override
    protected void onPreExecute()
    {
        mDialogFileOperations = new DialogFileOperations(mActivity);
        mDialogFileOperations.show();
    }

    @Override
    protected void onPostExecute(Void result)
    {
        if (mDialogFileOperations.isShowing()) {
            mDialogFileOperations.dismiss();
        }

        mAdapter.notifyDataSetChanged();
        mAdapter.cleanSelectedItems();

        if (this.isCancelled()) {
            Toast.makeText(mActivity, R.string.The_copy_is_fail, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(mActivity, R.string.The_copy_is_complete, Toast.LENGTH_SHORT).show();
    }
}
