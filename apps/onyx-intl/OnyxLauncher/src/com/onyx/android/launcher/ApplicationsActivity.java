/**
 * 
 */
package com.onyx.android.launcher;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.onyx.android.launcher.adapter.ApplicationsAdapter;
import com.onyx.android.launcher.adapter.ApplicationsAdapter.AppItem;
import com.onyx.android.launcher.view.OnyxPagedGridViewHost;
import com.onyx.android.sdk.data.util.ActivityUtil;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridViewPaginator.OnPageIndexChangedListener;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * @author joy
 *
 */
public class ApplicationsActivity extends OnyxBaseActivity
{
    private static final String TAG = "ApplicationsActivity";
    
    private OnyxGridView mGridView = null;
    private ApplicationsAdapter mAdapter = null;

    @Override
    public OnyxGridView getGridView()
    {
        return mGridView;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_applications);
        
        mGridView = ((OnyxPagedGridViewHost)this.findViewById(R.id.gridview_applications)).getGridView();
        Button button_home = (Button)this.findViewById(R.id.button_home);
        
        mGridView.setOnItemClickListener(new OnItemClickListener()
        {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                AppItem app = (AppItem)view.getTag();
                ActivityUtil.startActivitySafely(ApplicationsActivity.this, app.getIntent());
            }
        });
        
        button_home.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                LauncherActivity.goLauncherHome(ApplicationsActivity.this);
                ApplicationsActivity.this.finish();
            }
        });
        
        mAdapter = new ApplicationsAdapter(this, mGridView);
        ArrayList<ApplicationsAdapter.AppItem> apps = this.loadAllApps();
        if (apps != null) {
            mAdapter.setAppItems(apps);
        }
        mAdapter.getPaginator().registerOnPageIndexChangedListener(new OnPageIndexChangedListener()
        {
            
            @Override
            public void onPageIndexChanged()
            {
                ScreenUpdateManager.invalidate(ApplicationsActivity.this.getGridView(), UpdateMode.GC);
            }
        });
        mGridView.setAdapter(mAdapter);
        
        this.registerLongPressListener();
        
        ScreenUpdateManager.invalidate(this.getGridView(), UpdateMode.GC);
    }
    
    /**
     * return null if failed
     * 
     * @return
     */
    private ArrayList<ApplicationsAdapter.AppItem> loadAllApps()
    {
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        final PackageManager package_manager = this.getPackageManager();
        List<ResolveInfo> apps = package_manager.queryIntentActivities(intent, 0);
        if (apps == null) {
            return null;
        }
        
        Log.d(TAG, "apps count: " + apps.size());
        ArrayList<ApplicationsAdapter.AppItem> result = new ArrayList<AppItem>();
        for (ResolveInfo a : apps) {
            result.add(new AppItem(package_manager, a));
        }
        
        return result;
    }

}
