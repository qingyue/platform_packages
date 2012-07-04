/**
 * 
 */
package com.onyx.android.launcher.data.actor;

import java.util.ArrayList;

import android.app.Activity;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.OnyxGridItemAdapter;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.util.EventedArrayList;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class DesktopActor extends ItemContainerActor
{
    @SuppressWarnings("unused")
    private static final String TAG = "DesktopActor";
    
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
        
        mRecentDocuments = new RecentDocumentsActor(this.getData().getURI());
        GridItemManager.RegisterURIActor(mRecentDocuments);
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
        
        assert(gridView.getAdapter() instanceof OnyxGridItemAdapter);
        OnyxGridItemAdapter adapter = (OnyxGridItemAdapter)gridView.getAdapter();
        
        ArrayList<GridItemData> item_list = new ArrayList<GridItemData>();
        for (AbstractItemActor a : actors) {
            if(a != mWebSites && a != mRecentDocuments) {
              item_list.add(a.getData());  
            }
            
        } 
        
        adapter.fillItems(this.getData().getURI(), item_list);
        
        return true;
    }
}
