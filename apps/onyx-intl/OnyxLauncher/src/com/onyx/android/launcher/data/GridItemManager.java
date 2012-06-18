/**
 * 
 */
package com.onyx.android.launcher.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.util.Log;

import com.onyx.android.launcher.data.actor.AbstractItemActor;
import com.onyx.android.launcher.data.actor.DesktopActor;
import com.onyx.android.launcher.data.actor.SettingsActor;
import com.onyx.android.launcher.data.actor.StorageActor;
import com.onyx.android.launcher.util.EventedArrayList;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;

/**
 * @author joy
 *
 */
public class GridItemManager
{
    private static final String TAG = "GridItemManager";
    
    /**
     * sorted by insert order
     * @author joy
     *
     * @param <TKey>
     * @param <TValue>
     */
    private class OrderedHashMap<TKey, TValue> {
        private ArrayList<TKey> mKeySequence = new ArrayList<TKey>();
        private HashMap<TKey, TValue> mDict = new HashMap<TKey, TValue>();
        
        public OrderedHashMap()
        {
        }
        
        // get key list sorted by insert order
        public ArrayList<TKey> getKeySequence()
        {
            return mKeySequence;
        }
        
        public boolean containsKey(Object key)
        {
            return mDict.containsKey(key);
        }
        
        public void put(TKey key, TValue value)
        {
            if (!mDict.containsKey(key)) {
                mKeySequence.add(key);
                mDict.put(key, value);
            }
        }
        
        public TValue get(Object key)
        {
            return mDict.get(key);
        }
    }
    
    private static OrderedHashMap<String, AbstractItemActor> OurURIActors = 
            new GridItemManager().new OrderedHashMap<String, AbstractItemActor>();
    private static DesktopActor OurDesktopActor = null;

    public static void initializeDesktop(OnyxGridView gridView, Activity hostActivity)
    {
        if (OurDesktopActor == null) {
            OurDesktopActor = new DesktopActor(OnyxItemURI.ROOT);
            RegisterURIActor(OurDesktopActor);
        }

        OurDesktopActor.process(gridView, OurDesktopActor.getData().getURI(), hostActivity);
    }

    public static boolean isDesktop(OnyxItemURI uri)
    {
        if (OurDesktopActor == null) {
            assert(false);
            return false;
        }

        return OurDesktopActor.getData().getURI().toString().compareTo(uri.toString()) == 0;
    }

    public static OnyxItemURI getDesktopURI()
    {
        if (OurDesktopActor == null) {
            assert(false);
            throw new IllegalArgumentException();
        }

        return OurDesktopActor.getData().getURI();
    }

    public static OnyxItemURI getLibraryURI()
    {
        if (OurDesktopActor == null) {
            assert(false);
            throw new IllegalArgumentException();
        }

        return OurDesktopActor.getLibraryActor().getData().getURI();
    }

    public static OnyxItemURI getStorageURI()
    {
        if (OurDesktopActor == null) {
            assert(false);
            throw new IllegalArgumentException();
        }

        return OurDesktopActor.getStorageActor().getData().getURI();
    }

    public static OnyxItemURI getSettingsURI()
    {
        if (OurDesktopActor == null) {
            assert(false);
            throw new IllegalArgumentException();
        }

        return OurDesktopActor.getSettingsActor().getData().getURI();
    }

    public static OnyxItemURI getApplicationsURI()
    {
        if (OurDesktopActor == null) {
            assert(false);
            throw new IllegalArgumentException();
        }

        return OurDesktopActor.getApplicationsActor().getData().getURI();
    }

    public static OnyxItemURI getRecentDocumentsURI()
    {
        if (OurDesktopActor == null) {
            assert(false);
            throw new IllegalArgumentException();
        }

        return OurDesktopActor.getRecentDocumentsActor().getData().getURI();
    }

    public static ArrayList<GridItemData> getSettings()
    {
        if (OurDesktopActor == null) {
            assert(false);
            throw new IllegalArgumentException();
        }

        SettingsActor settings = OurDesktopActor.getSettingsActor();

        ArrayList<GridItemData> result = new ArrayList<GridItemData>();

        ArrayList<AbstractItemActor> actors = GridItemManager.getChildren(settings.getData().getURI());
        for (AbstractItemActor a : actors) {
            result.add(a.getData());
        } 

        return result;
    }
    
    /**
     * if filePath not in root storage directory, then return null
     * 
     * @param filePath
     * @return
     */
    public static OnyxItemURI getURIFromFilePath(String filePath)
    {
        String root_path = StorageActor.getStorageRootDirectory().getAbsolutePath();
        
        if (!filePath.startsWith(root_path)) {
            Log.w(TAG, "file not in root directory: (file)" + filePath + ", (root)" + root_path);
            return null;
        }

        final char seperator = '/';
        
        if ((filePath.length() > 0) && (filePath.charAt(filePath.length() - 1) == seperator)) {
            filePath = filePath.substring(0, filePath.length() - 1);
        }
        
        String postfix = filePath.substring(root_path.length());
        if ((postfix.length() > 0) && (postfix.charAt(0) == seperator)) {
            postfix = postfix.substring(1);
        }
        if (postfix.length() == 0) {
            return getStorageURI();
        }
        
        OnyxItemURI uri = (OnyxItemURI)getStorageURI().clone();
        
        String[] array = postfix.split(String.valueOf(seperator)); 
        for (String s : array) {
            uri.append(s);
        }
        
        return uri;
    }
    
    public static File getFileFromURI(OnyxItemURI uri)
    {
        return OurDesktopActor.getStorageActor().getFileFromURI(uri);
    }
    
    public static void RegisterURIActor(AbstractItemActor actor)
    {
        String uri = actor.getData().getURI().toString();
        if (!OurURIActors.containsKey(uri)) {
            OurURIActors.put(uri, actor);
        }
    }
    
    /**
     * return direct children
     * 
     * @param uri
     * @return
     */
    public static ArrayList<AbstractItemActor> getChildren(OnyxItemURI uri)
    {
        ArrayList<AbstractItemActor> children = new ArrayList<AbstractItemActor>();
        
        AbstractItemActor parent_actor = OurURIActors.get(uri.toString());
        
        if (parent_actor == null) {
            return children;
        }
        
        String str_parent_uri = uri.toString();
        
        ArrayList<String> uris = OurURIActors.getKeySequence();
        for (String u : uris) {
            if (u.startsWith(str_parent_uri)) {
                AbstractItemActor actor = OurURIActors.get(u);
                if (actor.getData().getURI().getPathLevels().size() == 
                        (parent_actor.getData().getURI().getPathLevels().size() + 1)) {
                    children.add(actor);
                }
            }
        }
        
        return children;
    }
    
    public static boolean isItemContainer(OnyxItemURI uri)
    {
        AbstractItemActor actor = findAppropriateActorBy(uri);
        if (actor == null) {
            return false;
        }
        
        return actor.isItemContainer(uri);
    }
    
    public static boolean processURI(OnyxGridView gridView, OnyxItemURI uri, Activity hostActivity)
    {
        AbstractItemActor actor = findAppropriateActorBy(uri);
        if (actor == null) {
            return false;
        }

        return actor.process(gridView, uri, hostActivity);
    }
    
    public static void doSearch(OnyxGridView gridView, OnyxItemURI uri, String pattern)
    {
        AbstractItemActor actor = findAppropriateActorBy(uri);
        if (actor == null) {
            return;
        }
        
        actor.doSearch(gridView, uri, pattern);
    }
    
    public static boolean search(Activity hostActivity, OnyxItemURI uri, String pattern, EventedArrayList<GridItemData> result)
    {
        AbstractItemActor actor = findAppropriateActorBy(uri);
        if (actor == null) {
            return false;
        }
        
        return actor.search(hostActivity, uri, pattern, result);
    }
    
    private static AbstractItemActor findAppropriateActorBy(OnyxItemURI uri)
    {
        String uri_string = uri.toString();
        AbstractItemActor actor = OurURIActors.get(uri_string);
        if (actor != null) {
            return actor;
        }
        
        String str_uri = uri.toString();
        
        String most_propriate_uri = null;
        ArrayList<String> uris = OurURIActors.getKeySequence();
        for (String u : uris) {
            if (str_uri.startsWith(u)) {
                if (most_propriate_uri == null) {
                    most_propriate_uri = u;
                }
                else {
                    if (u.length() > most_propriate_uri.length()) {
                        most_propriate_uri = u;
                    }
                }
            }
        }
        
        if (most_propriate_uri == null) {
            assert(false);
            Log.w(TAG, "findAppropriateActorBy() failed");
            return null;
        }
        
        return OurURIActors.get(most_propriate_uri);
    }
}
