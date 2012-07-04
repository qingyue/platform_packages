package com.onyx.android.launcher.data.actor;

import java.util.ArrayList;

import android.app.Activity;

import com.onyx.android.launcher.adapter.OnyxGridAppsItemAdapter;
import com.onyx.android.launcher.data.GridItemManager;
import com.onyx.android.launcher.util.EventedArrayList;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

public class AppsItemContainerActor extends AbstractItemActor
{
    
    public AppsItemContainerActor(GridItemData itemData)
    {
        super(itemData);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public boolean isItemContainer(OnyxItemURI uri)
    {
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
    
    @Override
    public boolean doSearch(OnyxGridView gridView, OnyxItemURI uri, String pattern)
    {
        throw new RuntimeException();
    }
    
    @Override
    public boolean search(Activity hostActivity, OnyxItemURI uri, String pattern, EventedArrayList<GridItemData> result)
    {
        return this.dfsSearch(hostActivity, uri, pattern, result);
    }
    
    protected boolean dfsSearch(Activity hostActivity, OnyxItemURI uri, String pattern, EventedArrayList<GridItemData> result) 
    {
        ArrayList<AbstractItemActor> children = GridItemManager.getChildren(uri);
        for (AbstractItemActor actor : children) {
            if (actor.getData().match(pattern)) {
                result.add(actor.getData());
                mOnSearchProgressed.onSearchProgressed();
            }
        }
        
        for (AbstractItemActor actor : children) {
            actor.search(hostActivity, actor.getData().getURI(), pattern, result);
        }
        
        return true;
    }

}
