package com.onyx.android.launcher.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.widget.Toast;

import com.onyx.android.launcher.OnyxBaseActivity;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.data.FileIconFactory;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.data.SDFileFactory;
import com.onyx.android.launcher.dialog.DialogCopyFiles;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData;
import com.onyx.android.sdk.ui.data.FileItemData.FileType;
import com.onyx.android.sdk.ui.data.GridItemData;

public class CopyFileTask extends AsyncTask<Void, Map<String, Object>, Void>
{
    private OnyxBaseActivity mActivity = null;
    private GridItemBaseAdapter mAdapter = null;
    private File mSourceFile = null;
    private File mFile = null;
    private DialogCopyFiles mDialogProgressBar = null;
    private int mFileQuantity = 0;
    private int mFileCount = 0;
    private List<File> mFiles = new ArrayList<File>();
    private boolean mIsShowing = false;

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
            mSourceFile = mFiles.get(i);
            int count = 0;
            count = SDFileFactory.fileQuantity(mSourceFile.getPath());
            mFileQuantity = mFileQuantity + count;
        }

        for (int i = 0; i < mFiles.size(); i++) {
            mSourceFile = mFiles.get(i);

            if (mFile.getPath().startsWith(mSourceFile.getPath())) {
                CopyFileTask.this.cancel(true);
                return null;
            }

            if (!CopyFileTask.this.copy(mSourceFile, mFile)) {
                CopyFileTask.this.cancel(true);
                return null;
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Map<String, Object>... values)
    {
        if (!mDialogProgressBar.isShowing()) {
            mIsShowing = false;
        }

        mDialogProgressBar.getProgressBar().setProgress(Integer.parseInt(values[0].get("progress").toString()));
        mDialogProgressBar.getCopyName().setText(values[0].get("file_name").toString());

        if (values[0].get("gridItemData") != null) {
            mAdapter.getItems().add((GridItemData)values[0].get("gridItemData"));
            mAdapter.getPaginator().setItemCount(mAdapter.getPaginator().getItemCount() + 1);
        }
    }

    @Override
    protected void onPreExecute()
    {
        mDialogProgressBar = new DialogCopyFiles(mActivity);
        mDialogProgressBar.show();
        mIsShowing = true;
    }

    @Override
    protected void onPostExecute(Void result)
    {
        if (mDialogProgressBar.isShowing()) {
            mDialogProgressBar.dismiss();
        }

        mAdapter.notifyDataSetChanged();
        mAdapter.cleanSelectedItems();

        if (this.isCancelled()) {
            Toast.makeText(mActivity, "The copy is fail !", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(mActivity, "The copy is complete", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unchecked")
    private boolean copy(File sourceFile, File file)
    {
        File copyFile = new File(file.getPath(), sourceFile.getName());
        if (copyFile.exists()) {
            return false;
        }

        if (!mIsShowing) {
            return false;
        }

        mFileCount ++;

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("progress", (int)(mFileCount / (float) mFileQuantity * 100));
        map.put("file_name", copyFile.getName());

        GridItemData gridItemData = null;
        String str = mFile.getPath() + File.separator + mSourceFile.getName();

        if (copyFile.getParent().equals(GridItemManager.getFileFromURI(mAdapter.getHostURI()).toString())) {
            if (sourceFile.isFile()) {
                gridItemData = new BookItemData(GridItemManager.getURIFromFilePath(str), FileType.NormalFile,
                        copyFile.getName(), FileIconFactory.getIconByFileName(copyFile.getName()));
            }
            else {
                gridItemData = new BookItemData(GridItemManager.getURIFromFilePath(str), FileType.Directory,
                        copyFile.getName(), R.drawable.directory);
            }

            if (copyFile.getPath().startsWith(mSourceFile.getPath())) {
                map.put("gridItemData", null);
            }
            else {
                map.put("gridItemData", gridItemData);
            }
        }

        publishProgress(map);

        if (sourceFile.isFile()) {
            copyFile(sourceFile, copyFile);
        }
        else {
            copyDirectory(sourceFile, copyFile);
        }

        return true;
    }

    private boolean copyFile(File sourceFile, File file)
    {
        try {
            if (!file.createNewFile()) {
                mAdapter.notifyDataSetChanged();
                return false;
            }

            FileInputStream inputStream = new FileInputStream(sourceFile);
            FileOutputStream outputStream = new FileOutputStream(file);
            int i = 0;
            byte[] b = new byte[6120];

            while ((i = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, i);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
        catch (IOException e) {
            return false;
        }

        return true;
    }

    private boolean copyDirectory(File sourceFile, File file)
    {
        if (!file.mkdir()) {
            return false;
        }
        File[] files = sourceFile.listFiles();

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    copy(files[i], file);
                }
                else if (files[i].isDirectory()) {
                    copy(files[i], file);
                }
            }
        }

        return true;
    }
}
