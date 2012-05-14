package com.onyx.android.launcher.task;

import java.io.File;

import android.os.AsyncTask;
import android.widget.Toast;

import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.SDFileFactory;
import com.onyx.android.launcher.dialog.DialogProgressBarRotundity;
import com.onyx.android.sdk.ui.data.FileItemData;

public class DeleteFileTask extends AsyncTask<File, Void, Void>
{
    private FileOperationHandler mFileHandler = null;
    private DialogProgressBarRotundity mDialogProgress = null;

    public DeleteFileTask(FileOperationHandler fileHandler)
    {
        mFileHandler = fileHandler;
    }

    @Override
    protected Void doInBackground(File... params)
    {
        for (FileItemData i : mFileHandler.getSourceItems()) {
            File f = GridItemManager.getFileFromURI(i.getURI());

            if (!SDFileFactory.delete(f)) {
                this.cancel(true);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
        if (mDialogProgress.isShowing()) {
            mDialogProgress.cancel();
        }

        if (this.isCancelled()) {
            Toast.makeText(mFileHandler.getContext(), "delete fail", Toast.LENGTH_SHORT);
            return;
        }
        
        GridItemBaseAdapter adapter = mFileHandler.getAdapter();
        adapter.removeItems(mFileHandler.getSourceItems());
    }

    @Override
    protected void onPreExecute()
    {
        mDialogProgress = new DialogProgressBarRotundity(mFileHandler.getContext());
        mDialogProgress.show();
    }
}
