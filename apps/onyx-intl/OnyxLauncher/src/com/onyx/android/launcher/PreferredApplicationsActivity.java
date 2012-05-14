package com.onyx.android.launcher;

import java.io.File;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.onyx.android.launcher.adapter.PreferredApplicationsAdapter;
import com.onyx.android.launcher.dialog.DialogPreferredApplications;
import com.onyx.android.launcher.view.OnyxPagedGridViewHost;
import com.onyx.android.sdk.data.sys.OnyxAppPreference;
import com.onyx.android.sdk.data.sys.OnyxAppPreferenceCenter;
import com.onyx.android.sdk.data.util.IntentFilterFactory;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;
/**
 * 
 * @author qingyue
 *
 */
public class PreferredApplicationsActivity extends OnyxBaseActivity
{
    private OnyxGridView mGridView = null;
    private PreferredApplicationsAdapter mAdapter = null;
    private String[] mFileFormats = null;
    private BroadcastReceiver mReceiver=null;

    @Override
    protected void onResume()
    {
        super.onResume();

        mReceiver=new SetFrontSetting();
        registerReceiver(mReceiver, IntentFilterFactory.getIntentFilterFrontPreferredApplications());
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_preferred_applications);

        mGridView = ((OnyxPagedGridViewHost) findViewById(R.id.gridview_preferred_applications)).getGridView();

        mFileFormats = mGridView.getResources().getStringArray(R.array.file_format);

        this.frontSetting();

        mGridView.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                final Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.content.Intent.ACTION_VIEW);

                String ext = view.getTag().toString();

                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                File file = new File("mnt/sdcard/dummy."+ ext);
                if (type != null) {
                    intent.setDataAndType(Uri.fromFile(file), type);
                }
                else {
                    intent.setData(Uri.fromFile(file));
                }

                List<ResolveInfo> info_list = getPackageManager().queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);

                if (info_list.size() <= 0) {
                    Toast.makeText(PreferredApplicationsActivity.this, "unable to open this type of file", Toast.LENGTH_SHORT).show();
                }
                else {

                    TextView text_view_app = (TextView)view.findViewById(R.id.textivew_default_application);

                    DialogPreferredApplications dlg = new DialogPreferredApplications(PreferredApplicationsActivity.this, info_list, ext, text_view_app.getText().toString());
                    dlg.show();
                }
            }
        });

        ScreenUpdateManager.invalidate(this.getWindow().getDecorView(), UpdateMode.GC); 

        this.registerLongPressListener();
    }

    @Override
    public OnyxGridView getGridView()
    {
        return mGridView;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        PreferredApplicationsActivity.this.disabledMenuMultiple(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    private void frontSetting()
    {
        String[] strings = new String[mFileFormats.length];
        int k = 0;
        for (int i = 0; i < mFileFormats.length; i++) {
            File fake_file = new File("fake." + mFileFormats[i]);
            OnyxAppPreference p = OnyxAppPreferenceCenter.getApplicationPreference(fake_file);
            if (p != null) {
                strings[k] = mFileFormats[i];
                k++;
            }
        }

        for (int i = 0; i < mFileFormats.length; i++) {
            File fake_file = new File("fake." + mFileFormats[i]);
            OnyxAppPreference p = OnyxAppPreferenceCenter.getApplicationPreference(fake_file);
            if (p == null) {
                strings[k] = mFileFormats[i];
                k++;
            }
        }
        mAdapter = new PreferredApplicationsAdapter(this, mGridView, strings);
        mGridView.setAdapter(mAdapter);
    }

    private class SetFrontSetting extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            PreferredApplicationsActivity.this.frontSetting();
        }

    }
}
