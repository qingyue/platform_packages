/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.util.EventedArrayList;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class DesktopActor extends ItemContainerActor
{
    private static final String TAG = "DesktopActor";
    
    LibraryActor mLibrary = null;
    StorageActor mStorage = null;
    RecentDocumentsActor mRecentDocuments = null;
    DictionaryActor mDictionary = null;
    ScribbleActor mScribble = null;
    NotesActor mNotes = null;
    WebSitesActor mWebSites = null;
    ApplicationsActor mApplications = null;
    SettingsActor mSettings = null;
    
    public DesktopActor(OnyxItemURI parentURI)
    {
        super(new GridItemData(((OnyxItemURI)parentURI.clone()).append("Desktop"), 
               R.string.Desktop, 
                R.drawable.icon));
        
        mLibrary = new LibraryActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mLibrary);
        
        mStorage = new StorageActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mStorage);
        
        mRecentDocuments = new RecentDocumentsActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mRecentDocuments);
        
        mDictionary = new DictionaryActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mDictionary);
        
        mScribble = new ScribbleActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mScribble);
        
        mNotes = new NotesActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mNotes);
        
        mWebSites = new WebSitesActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mWebSites);
        
        mApplications = new ApplicationsActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mApplications);
        
        mSettings = new SettingsActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mSettings);
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
    
    public SettingsActor getSettingsActor()
    {
        assert(mSettings != null);
        return mSettings;
    }
    
    public ApplicationsActor getApplicationsActor()
    {
        assert(mApplications != null);
        return mApplications;
    }
    
    public RecentDocumentsActor getRecentDocumentsActor()
    {
        assert(mRecentDocuments != null);
        return mRecentDocuments;
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
}
