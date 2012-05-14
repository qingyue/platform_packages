/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import android.app.Activity;
import android.content.Intent;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.RecentDocumentsActivity;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.data.util.ActivityUtil;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class RecentDocumentsActor extends AbstractItemActor
{

    public RecentDocumentsActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(((OnyxItemURI)parentURI.clone()).append("Recent Documents"), 
                "Recent Documents", 
                R.drawable.recent_document));
    }

    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri, Activity hostActivity)
    {
        Intent intent = new Intent(hostActivity, RecentDocumentsActivity.class);
        return ActivityUtil.startActivitySafely(hostActivity, intent);
    }

}
