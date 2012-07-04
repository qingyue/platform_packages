package com.onyx.android.launcher.data.actor;

import android.app.Activity;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

public class MusicActor extends AbstractItemActor 
{

    public MusicActor(OnyxItemURI parentURI) 
	{
        super(new GridItemData(
                ((OnyxItemURI) parentURI.clone()).append("Music"), 
                R.string.Music,
                R.drawable.music1));
	}

    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri,
            Activity hostActivity) 
    {
        // TODO Auto-generated method stub
        return false;
    }

}
