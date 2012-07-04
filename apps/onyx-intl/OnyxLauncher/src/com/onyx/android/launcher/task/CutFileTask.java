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
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

public class CutFileTask extends AsyncTask<Void, GridItemData, Void>
{
    private OnyxBaseActivity mActivity = null;
    private File mSourceFile = null;
    private File mFile = null;
    private DialogFileOperations mDialogFileOperations = null;
    private List<File> mFiles = new ArrayList<File>();

    public CutFileTask(OnyxBaseActivity activity, Collection<FileItemData> items)
    {
        mActivity = activity;

        for (FileItemData i : items) {
            mFiles.add(GridItemManager.getFileFromURI(i.getURI()));
        }
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        GridItemBaseAdapter adapter = (GridItemBaseAdapter)mActivity.getGridView().getPagedAdapter();
        mFile = GridItemManager.getFileFromURI(adapter.getHostURI());
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

            if (!SDFileFactory.copy(mSourceFile, mFile.getPath(), mDialogFileOperations)) {
                this.cancel(true);
                return null;
            }

            if (!SDFileFactory.delete(mSourceFile, mDialogFileOperations)) {
                this.cancel(true);
                return null;
            }

            GridItemData gridItemData = null;
            String str = mFile.getPath() + File.separator + mSourceFile.getName();
            mSourceFile = new File(str);
            if (mSourceFile.isFile()) {
                gridItemData = new FileItemData(GridItemManager.getURIFromFilePath(str), FileType.NormalFile,
                        mSourceFile.getName(), FileIconFactory.getIconByFileName(mSourceFile.getName()));
            }
            else {
                gridItemData = new FileItemData(GridItemManager.getURIFromFilePath(str), FileType.Directory,
                        mSourceFile.getName(), R.drawable.directory);
            }

            publishProgress(gridItemData);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
        if(mDialogFileOperations.isShowing()) {
            mDialogFileOperations.dismiss();
        }

        mActivity.getGridView().getPagedAdapter().notifyDataSetChanged();
        ((GridItemBaseAdapter)mActivity.getGridView().getPagedAdapter()).cleanSelectedItems();

        if (this.isCancelled()) {
            Toast.makeText(mActivity, R.string.Cut_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(GridItemData... values)
    {
        GridItemBaseAdapter adapter = (GridItemBaseAdapter)mActivity.getGridView().getPagedAdapter();
        adapter.appendItems(new GridItemData[] { values[0] });
        ScreenUpdateManager.invalidate(mActivity.getGridView(), UpdateMode.GU);
    }

    @Override
    protected void onPreExecute()
    {
        mDialogFileOperations = new DialogFileOperations(mActivity);
        mDialogFileOperations.show();
    }
}
