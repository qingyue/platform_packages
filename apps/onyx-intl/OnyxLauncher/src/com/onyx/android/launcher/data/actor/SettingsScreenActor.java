/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import android.app.Activity;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.dialog.DialogScreenUpdate;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class SettingsScreenActor extends AbstractItemActor
{

    public SettingsScreenActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(((OnyxItemURI)parentURI.clone()).append("Screen Update"), 
                "Screen Update", 
                R.drawable.screen_update_setting));
    }

    /* (non-Javadoc)
     * @see com.onyx.android.homework.data.actor.AbstractGridItemActor#process(com.onyx.android.sdk.ui.OnyxGridView, com.onyx.android.sdk.data.ItemURI)
     */
    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri, Activity hostActivity)
    {
        new DialogScreenUpdate(hostActivity).show();
        
        return false;
    }

}
