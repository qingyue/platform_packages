/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import android.app.Activity;
import android.content.Intent;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.data.util.ActivityUtil;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class SettingsAppSettingsActor extends AbstractItemActor
{
    public SettingsAppSettingsActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(((OnyxItemURI)parentURI.clone()).append("Application Settings"), 
                "Application Settings", 
                R.drawable.applications));
    }
    
    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri,
            Activity hostActivity)
    {
        Intent i = new Intent();
        i.setClassName("com.android.settings", "com.android.settings.ApplicationSettings");
        return ActivityUtil.startActivitySafely(hostActivity, i);
    }
}
