package com.onyx.android.launcher.data.actor;

import android.app.Activity;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

public class FavoriteActor extends AbstractItemActor
{

	public FavoriteActor(OnyxItemURI parentURI) 
	{
	    super(new GridItemData(
	            ((OnyxItemURI) parentURI.clone()).append("Favorite"), 
	            R.string.Favorite,
	            R.drawable.favorite));
	}

	@Override
	public boolean process(OnyxGridView gridView, OnyxItemURI uri,
	        Activity hostActivity) 
	{
		// TODO Auto-generated method stub
	    return false;
	}

}
