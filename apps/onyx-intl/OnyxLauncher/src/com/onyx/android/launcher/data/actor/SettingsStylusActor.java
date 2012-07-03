/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import android.app.Activity;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class SettingsStylusActor extends AbstractItemActor
{

    public SettingsStylusActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(((OnyxItemURI)parentURI.clone()).append("Stylus Calibration"), 
                R.string.Stylus_Calibration, 
                R.drawable.screen_calibration));
    }

    /* (non-Javadoc)
     * @see com.onyx.android.homework.data.actor.AbstractGridItemActor#process(com.onyx.android.sdk.ui.OnyxGridView, com.onyx.android.sdk.data.ItemURI)
     */
    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri, Activity hostActivity)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
