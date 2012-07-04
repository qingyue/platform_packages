package com.onyx.android.launcher.data.actor;

import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.OnyxGridAppsItemAdapter;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.util.EventedArrayList;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;


/**
 * @author dxw
 *
 */

public class DesktopShortCutsActor extends AppsItemContainerActor
{
    private static final String TAG = "DesktopShortCutsActor";
    
    LibraryActor mLibrary = null;
    StorageActor mStorage = null;
    MusicActor mMusic = null;
    ImageActor mImage = null;
    FavoriteActor mFavorite = null;

    public DesktopShortCutsActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(((OnyxItemURI) parentURI.clone()).append("ShortCuts"),
                R.string.ShortCuts,
                R.drawable.icon));

        mMusic = new MusicActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mMusic);

        mImage = new ImageActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mImage);

        mFavorite = new FavoriteActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mFavorite);
        
        mLibrary = new LibraryActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mLibrary);
        
        mStorage = new StorageActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mStorage);
    }
    
    public LibraryActor getLibraryActor()
    {
        assert(mLibrary != null);
        return mLibrary;
    }
    
    public StorageActor getStorageActor()
    {
        assert(mStorage != null);
        return mStorage;
    }
    
    public MusicActor getMusicActor()
    {
        assert(mMusic != null);
        return mMusic;
    }
    
    public ImageActor getImageActor()
    {
        assert(mImage != null);
        return mImage;
    }
    
    public FavoriteActor getFavoriteActor()
    {
        assert(mFavorite != null);
        return mFavorite;
    }
    
    
    @Override
    public boolean search(Activity hostActivity, OnyxItemURI uri, String pattern,
            EventedArrayList<GridItemData> result)
    {
        ArrayList<AbstractItemActor> actors = GridItemManager.getChildren(this.getData().getURI());
        for (AbstractItemActor actor : actors) {
            if (actor.getData().match(pattern)) {
                result.add(actor.getData());
            }
        }
        
        for (AbstractItemActor a : actors) {
            if ((a != mLibrary) && (a != mStorage)) {
                if (!a.search(hostActivity, a.getData().getURI(), pattern, result)) {
                    Log.w(TAG, "search failed: " + a.getData().getURI().toString());
                }
            }
        }
        
        if (!mStorage.search(hostActivity, mStorage.getData().getURI(), pattern, result)) {
            Log.w(TAG, "search failed: " + mStorage.getData().getURI().toString());
        }
        
        return true;
    }
    
    
    @Override
    public boolean process(OnyxGridView gridView, OnyxItemURI uri,
            Activity hostActivity)
    {
        ArrayList<AbstractItemActor> actors = GridItemManager.getChildren(this.getData().getURI());
        if (actors.isEmpty()) {
            return true;
        }
        
        assert(gridView.getAdapter() instanceof OnyxGridAppsItemAdapter);
        OnyxGridAppsItemAdapter adapter = (OnyxGridAppsItemAdapter)gridView.getAdapter();
        
        ArrayList<GridItemData> item_list = new ArrayList<GridItemData>();
        for (AbstractItemActor a : actors) {
              item_list.add(a.getData());  
        } 
        
        adapter.fillItems(this.getData().getURI(), item_list);
        
        return true;
    }

}
