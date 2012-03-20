/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import android.app.Activity;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.dialog.DialogStartupSettings;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class SettingsStartupActor extends AbstractItemActor
{

    public SettingsStartupActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(((OnyxItemURI)parentURI.clone()).append("Startup Settings"), 
                "Startup Settings", 
                R.drawable.startup));
    }

    /* (non-Javadoc)
     * @see com.onyx.android.homework.data.actor.AbstractGridItemActor#process(com.onyx.android.sdk.ui.OnyxGridView, com.onyx.android.sdk.data.ItemURI)
     */
    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri, Activity hostActivity)
    {
        new DialogStartupSettings(hostActivity).show();

        return false;
    }

}
