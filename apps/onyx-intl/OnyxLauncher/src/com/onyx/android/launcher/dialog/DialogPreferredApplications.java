package com.onyx.android.launcher.dialog;

import java.util.List;

import android.content.pm.ResolveInfo;
import android.view.View;
import android.widget.Toast;

import com.onyx.android.launcher.OnyxBaseActivity;
import com.onyx.android.launcher.adapter.SelectionAdapter;
import com.onyx.android.launcher.view.DialogBaseSettings;
import com.onyx.android.sdk.data.sys.OnyxAppPreferenceCenter;
import com.onyx.android.sdk.data.util.IntentFactory;

public class DialogPreferredApplications extends DialogBaseSettings
{
    private SelectionAdapter mAdapter = null;
    private List<ResolveInfo> mListInfos = null;
    private String mExt = null;

    private int mSelection = -1;
    private OnyxBaseActivity mActivity = null;

    public DialogPreferredApplications(OnyxBaseActivity activity, List<ResolveInfo> listiInfos, String ext, String defaultAppName)
    {
        super(activity);

        mActivity = activity;
        mListInfos = listiInfos;
        mExt = ext;

        String[] app_names = new String[mListInfos.size()];
        for (int i = 0; i < mListInfos.size(); i++) {
            if (defaultAppName.equals(mListInfos.get(i).activityInfo.applicationInfo.loadLabel(mActivity.getPackageManager()))) {
                mSelection = i;
            }
            app_names[i] = mListInfos.get(i).activityInfo.applicationInfo.loadLabel(mActivity.getPackageManager()).toString();
        }

        mAdapter = new SelectionAdapter(mActivity, this.getGridView(), app_names, mSelection);
        this.getGridView().setAdapter(mAdapter);

        mAdapter.getPaginator().setPageSize(mListInfos.size());

        this.getButtonSet().setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                DialogPreferredApplications.this.dismiss();
                
                int selection = mAdapter.getSelection();
                if (selection >= 0) {
                    ResolveInfo resolve_info = mListInfos.get(selection);
                    String name = resolve_info.activityInfo.applicationInfo.loadLabel(mActivity.getPackageManager()).toString();
                    String pkg = resolve_info.activityInfo.packageName;
                    String cls = resolve_info.activityInfo.name;

                    if (OnyxAppPreferenceCenter.setAppPreference(mActivity, mExt, name, pkg, cls)) {
                        Toast.makeText(mActivity, "Successfully set", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(mActivity, "Fail set", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if (OnyxAppPreferenceCenter.removeAppPreference(mActivity, mExt)) {
                        Toast.makeText(mActivity, "Successfully remove", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(mActivity, "Fail remove", Toast.LENGTH_SHORT).show();
                    }
                }

                mActivity.sendBroadcast(IntentFactory.getIntentFrontPreferredApplications());
                
                mAdapter.notifyDataSetChanged();
            }
        });

        this.getButtonCancel().setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                DialogPreferredApplications.this.cancel();
            }
        });

        this.getTextViewTitle().setText("Settings preferred applications");
        mAdapter.getPaginator().setPageSize(mListInfos.size());
    }
}
