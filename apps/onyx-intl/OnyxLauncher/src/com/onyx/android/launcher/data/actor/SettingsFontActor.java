/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import android.app.Activity;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.dialog.DialogFont;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * @author joy
 *
 */
public class SettingsFontActor extends AbstractItemActor
{

    public SettingsFontActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(((OnyxItemURI)parentURI.clone()).append("Default Font"), 
                "Default Font", 
                R.drawable.font_management));
    }

    /* (non-Javadoc)
     * @see com.onyx.android.homework.data.actor.AbstractGridItemActor#process(com.onyx.android.sdk.ui.OnyxGridView, com.onyx.android.sdk.data.ItemURI)
     */
    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri, Activity hostActivity)
    {
        ScreenUpdateManager.invalidate(hostActivity.getWindow().getDecorView(), UpdateMode.GU);
         new DialogFont(hostActivity ).show();
         
        return false;
    }

}
