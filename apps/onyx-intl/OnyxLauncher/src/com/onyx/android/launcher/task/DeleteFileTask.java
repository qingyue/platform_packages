package com.onyx.android.launcher.task;

import java.io.File;

import android.os.AsyncTask;
import android.widget.Toast;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.data.FileOperationHandler;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.SDFileFactory;
import com.onyx.android.launcher.dialog.DialogFileOperations;
import com.onyx.android.sdk.ui.data.FileItemData;

public class DeleteFileTask extends AsyncTask<File, Void, Void>
{
    private FileOperationHandler mFileHandler = null;
    private DialogFileOperations mDialogFileOperations = null;

    public DeleteFileTask(FileOperationHandler fileHandler)
    {
        mFileHandler = fileHandler;
    }

    @Override
    protected Void doInBackground(File... params)
    {
        for (FileItemData i : mFileHandler.getSourceItems()) {
        	if (mDialogFileOperations == null && !mDialogFileOperations.getIsShowing()) {
        		this.cancel(true);
        		return null;
        	}

            File f = GridItemManager.getFileFromURI(i.getURI());

            if (!SDFileFactory.delete(f, mDialogFileOperations)) {
                this.cancel(true);
                return null;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
        if (mDialogFileOperations.isShowing()) {
            mDialogFileOperations.cancel();
        }

        if (this.isCancelled()) {
            Toast.makeText(mFileHandler.getContext(), R.string.delete_fail, Toast.LENGTH_SHORT);
            return;
        }
        
        GridItemBaseAdapter adapter = mFileHandler.getAdapter();
        adapter.removeItems(mFileHandler.getSourceItems());
    }

    @Override
    protected void onPreExecute()
    {
        mDialogFileOperations = new DialogFileOperations(mFileHandler.getContext());
        mDialogFileOperations.show();
    }
}
