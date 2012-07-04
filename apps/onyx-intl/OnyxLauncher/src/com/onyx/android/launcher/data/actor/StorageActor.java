/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.launcher.data.FileIconFactory;
import com.onyx.android.launcher.dialog.DialogApplicationOpenList;
import com.onyx.android.launcher.dialog.DialogApplicationOpenList.OnApplicationSelectedListener;
import com.onyx.android.launcher.util.EventedArrayList;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.data.sys.OnyxAppPreference;
import com.onyx.android.sdk.data.sys.OnyxAppPreferenceCenter;
import com.onyx.android.sdk.data.util.ActivityUtil;
import com.onyx.android.sdk.data.util.EnviromentUtil;
import com.onyx.android.sdk.data.util.FileUtil;
import com.onyx.android.sdk.data.util.SDCardRemovedException;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData;
import com.onyx.android.sdk.ui.data.FileItemData.FileType;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 * 
 */
public class StorageActor extends ItemContainerActor
{
    public static class GoUpLevelItem extends GridItemData
    {
        public GoUpLevelItem(OnyxItemURI uri, int textId, int imageResourceId)
        {
            super(uri, textId, imageResourceId);
        }

    }
	
    private static final String TAG = "StorageActor";

    private static final GoUpLevelItem sGoUpItem = new GoUpLevelItem(null,
            R.string.Go_up, R.drawable.go_up);

    /**
     * root directory corresponding to StorageURI
     * 
     * @return
     */
    public static File getStorageRootDirectory()
    {
        return EnviromentUtil.getExternalStorageDirectory();
    }

    public StorageActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(
                ((OnyxItemURI) parentURI.clone()).append("Storage"),
                R.string.Storage, R.drawable.sd));
    }

    @Override
    public boolean isItemContainer(OnyxItemURI uri)
    {
        File f = this.getFileFromURI(uri);
        if (f.isDirectory()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * throw runtime SDCardRemovedException when file to open not exist due to
     * SD card's removing
     */
    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri,
            Activity hostActivity)
    {
        GridItemBaseAdapter adapter = (GridItemBaseAdapter) gridView
                .getPagedAdapter();

        File file = this.getFileFromURI(uri);
        if (file == null) {
            return false;
        }
        Log.d(TAG, "file not null");

        if (!file.exists()) {
            if (EnviromentUtil.isFileOnRemovableSDCard(file)
                    && !Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                Toast.makeText(hostActivity, R.string.SD_card_has_been_removed,
                        Toast.LENGTH_SHORT).show();
                throw new SDCardRemovedException();
            }
            else {
                Toast.makeText(hostActivity,
                        R.string.file_not_exist + file.getAbsolutePath(),
                        Toast.LENGTH_SHORT).show();
            }

            return false;
        }
        Log.d(TAG, "file exist");

        if (file.isDirectory()) {
            Log.d(TAG, "file is directory");

            ArrayList<GridItemData> dir_list = new ArrayList<GridItemData>();
            ArrayList<GridItemData> file_list = new ArrayList<GridItemData>();

            File[] files = file.listFiles();
            if (files != null) {
                Log.d(TAG, "child not empty");

                for (File f : files) {
                    if (f.isHidden()) {
                        continue;
                    }

                    OnyxItemURI copy_uri = ((OnyxItemURI) uri.clone()).append(f
                            .getName());

                    if (f.isDirectory()) {
                        dir_list.add(new FileItemData(copy_uri,
                                FileType.Directory, f.getName(),
                                R.drawable.directory));
                    }
                    else {
                        Bitmap icon = FileIconFactory.getIconByFileName(f
                                .getName());
                        file_list.add(new BookItemData(copy_uri,
                                FileType.NormalFile, f.getName(), icon));
                    }
                }
            }

            Log.d(TAG, "fill items");
            sGoUpItem.setURI(uri.getParent());
            adapter.fillItems(uri, new GridItemData[] { sGoUpItem });
            adapter.appendItems(dir_list);
            adapter.appendItems(file_list);
            adapter.getItems().remove(sGoUpItem);
            adapter.getItems().add(0, sGoUpItem);
            return true;
        }
        else {
            Log.d(TAG, "try openning file: " + file.getAbsolutePath());

            final Intent intent = new Intent();
            intent.setData(Uri.fromFile(file));

            ActivityInfo app_info = null;
            OnyxAppPreference app_preference = OnyxAppPreferenceCenter
                    .getApplicationPreference(file);
            if (app_preference != null) {
                try {
                    app_info = hostActivity.getPackageManager()
                            .getActivityInfo(
                                    new ComponentName(
                                            app_preference.getAppPackageName(),
                                            app_preference.getAppClassName()),
                                    0);
                }
                catch (NameNotFoundException e) {
                    Log.i(TAG, app_preference.getAppName() + " not found");
                    app_info = null;
                }
            }

            if (app_info != null) {
                return ActivityUtil.startActivitySafely(hostActivity, intent,
                        app_info);
            }
            else {
                intent.setAction(android.content.Intent.ACTION_VIEW);

                String type = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(
                                FileUtil.getFileExtension(file));
                if (type != null) {
                    intent.setType(type);
                }

                intent.setDataAndType(Uri.fromFile(file), type);
                List<ResolveInfo> info_list = hostActivity.getPackageManager()
                        .queryIntentActivities(intent,
                                PackageManager.MATCH_DEFAULT_ONLY);

                if (info_list.size() <= 0) {
                    Toast.makeText(hostActivity,
                            R.string.unable_to_open_this_type_of_file,
                            Toast.LENGTH_SHORT).show();
                }
                else if (info_list.size() == 1) {
                    ResolveInfo info = info_list.get(0);
                    return ActivityUtil.startActivitySafely(hostActivity,
                            intent, info.activityInfo);
                }
                else {
                    assert (info_list.size() > 1);

                    final Activity host_activity = hostActivity;

                    DialogApplicationOpenList dlg = new DialogApplicationOpenList(
                            hostActivity, info_list,
                            FileUtil.getFileExtension(file));
                    dlg.setOnApplicationSelectedListener(new OnApplicationSelectedListener()
                    {

                        @Override
                        public void onApplicationSelected(ResolveInfo info,
                                boolean makeDefault)
                        {
                            ActivityUtil.startActivitySafely(host_activity,
                                    intent, info.activityInfo);
                        }

                    });

                    dlg.show();

                    return true;
                }

            }
        }

        return false;
    }

    @Override
    protected boolean dfsSearch(Activity hostActivity, OnyxItemURI uri,
            String pattern, EventedArrayList<GridItemData> result)
    {
        File dir = this.getFileFromURI(uri);
        File[] files = dir.listFiles();

        if (files == null) {
            return false;
        }

        ArrayList<OnyxItemURI> dir_list = new ArrayList<OnyxItemURI>();

        for (File f : files) {
            if (f.isHidden()) {
                continue;
            }

            OnyxItemURI copy_uri = ((OnyxItemURI) uri.clone()).append(f
                    .getName());

            if (f.getName().toUpperCase().contains(pattern.toUpperCase())) {
                if (f.isDirectory()) {
                    result.add(new FileItemData(copy_uri, FileType.Directory, f
                            .getName(), R.drawable.directory));
                }
                else {
                    Bitmap icon = FileIconFactory
                            .getIconByFileName(f.getName());
                    result.add(new BookItemData(copy_uri, FileType.NormalFile,
                            f.getName(), icon));
                }

                mOnSearchProgressed.onSearchProgressed();
            }

            if (f.isDirectory()) {
                dir_list.add(copy_uri);
            }
        }

        for (OnyxItemURI u : dir_list) {
            this.dfsSearch(hostActivity, u, pattern, result);
        }

        return true;
    }

    // always return a storage path
    public File getFileFromURI(OnyxItemURI uri)
    {
        File file_root = getStorageRootDirectory();

        OnyxItemURI storage_root_uri = this.getData().getURI();

        if (!uri.equals(storage_root_uri) && !uri.isChildOf(storage_root_uri)) {
            throw new IllegalArgumentException();
        }

        StringBuilder sb = new StringBuilder(file_root.getAbsolutePath());
        int uri_level_size = uri.getPathLevels().size();
        final char seperator = '/';
        for (int i = storage_root_uri.getPathLevels().size(); i < uri_level_size; i++) {
            sb.append(seperator).append(uri.getPathLevels().get(i));
        }

        return new File(sb.toString());
    }

}
