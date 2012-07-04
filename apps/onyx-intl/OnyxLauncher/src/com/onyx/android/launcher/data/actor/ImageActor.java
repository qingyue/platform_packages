package com.onyx.android.launcher.data.actor;

import android.app.Activity;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

public class ImageActor extends AbstractItemActor
{

    public ImageActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(
                ((OnyxItemURI) parentURI.clone()).append("Image"),
                R.string.Image,
                R.drawable.image));
    }

    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri,
            Activity hostActivity)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
