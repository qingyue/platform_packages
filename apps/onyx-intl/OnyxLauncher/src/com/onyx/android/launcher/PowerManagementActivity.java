/**
 * 
 */
package com.onyx.android.launcher;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;

import com.onyx.android.launcher.adapter.PowerManagementAdapter;
import com.onyx.android.launcher.view.OnyxPagedGridViewHost;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class PowerManagementActivity extends OnyxBaseActivity
{
    private OnyxGridView mGridView = null;
    private ImageButton mButtonHome = null;
    private PowerManagementAdapter mAdapter = null;
    private ContentResolver mResolver = null;

    private static final String TAG = "DisplaySettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_settings_powermanagement);

        mGridView = ((OnyxPagedGridViewHost)findViewById(R.id.gridview_powermanagement)).getGridView(); ;
        mButtonHome = (ImageButton)this.findViewById(R.id.button_home);
        
        mGridView.setOnItemClickListener(new OnItemClickListener()
        {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                GridItemData item = (GridItemData)view.getTag();
                mAdapter.setGridItemData(item);

                int value = Integer.parseInt((String) item.getTag());
                try {
                    Settings.System.putInt(getContentResolver(),
                            SCREEN_OFF_TIMEOUT, value);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "could not persist screen timeout setting", e);
                }
                finally {
                    PowerManagementActivity.this.finish();
                }
            }
        });
        
        mButtonHome.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                LauncherActivity.goLauncherHome(PowerManagementActivity.this);
                PowerManagementActivity.this.finish();
            }
        });

        String[] values = PowerManagementActivity.this.getResources().getStringArray(R.array.screen_timeout_values);
        String[] entries = PowerManagementActivity.this.getResources().getStringArray(R.array.screen_timeout_entries);
        ArrayList<GridItemData> times = new ArrayList<GridItemData>();
        for (int i = 0; i < entries.length; i++) {
            GridItemData item_data = new GridItemData(null, entries[i], null);
            item_data.setTag(values[i]);
            times.add(item_data);
        }
        
        mResolver = getContentResolver();
        String currentSetup = String.valueOf(Settings.System.getInt(
                mResolver, SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE));

        mAdapter = new PowerManagementAdapter(this, mGridView, currentSetup);
        mAdapter.fillItems(null, times);
        mGridView.setAdapter(mAdapter);

        this.registerLongPressListener();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        PowerManagementActivity.this.disabledMenuMultiple(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public OnyxGridView getGridView()
    {
        return mGridView;
    }
}
