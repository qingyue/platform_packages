/**
 * 
 */
package com.onyx.android.sdk.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author joy
 *
 */
public class OnyxItemURI
{
    @SuppressWarnings("unused")
    private static final String TAG = "OnyxItemURI";
    
    private static final char SEPERATOR = '/';
    
    public static final OnyxItemURI ROOT = new OnyxItemURI(new ArrayList<String>());
    
    //  { Desktop, Settings } responding to /Desktop/Settings
    private ArrayList<String> mPathLevels = new ArrayList<String>();
    
    public OnyxItemURI(Collection<String> pathLevels)
    {
        mPathLevels.addAll(pathLevels);
    }
    
    public OnyxItemURI(String[] pathLevels)
    {
        for (String s : pathLevels) {
            mPathLevels.add(s);
        }
    }
    
    /**
     * accept value coming from OnyxItemURI.toString()
     * @param uriString
     * @return
     */
    public static OnyxItemURI createFromString(String uriString)
    {
        if ((uriString == null) || (uriString.length() <= 0)) {
            return null;
        }
        
        if (uriString.charAt(0) == SEPERATOR) {
            uriString = uriString.substring(1);
        }
        
        String[] array = uriString.split(String.valueOf(SEPERATOR));
        return new OnyxItemURI(array);
    }
    
    public ArrayList<String> getPathLevels()
    {
        return mPathLevels;
    }
    
    public String getName()
    {
        if (mPathLevels.size() == 0) {
            // root
            return "";
        }
        else {
            return mPathLevels.get(mPathLevels.size() - 1);
        }
    }
    
    public OnyxItemURI getParent()
    {
        assert(mPathLevels.size() > 0);
        
        if (mPathLevels.size() == 0) {
            return OnyxItemURI.ROOT;
        }
        else {
            ArrayList<String> path_levels = new ArrayList<String>();
            path_levels.addAll(mPathLevels);
            path_levels.remove(path_levels.size() - 1);
            return new OnyxItemURI(path_levels);
        }
    }
    
    public boolean isRoot()
    {
        return mPathLevels.size() == 0;
    }
    
    public boolean isChildOf(OnyxItemURI parent)
    {
        if (this.getPathLevels().size() <= parent.getPathLevels().size()) {
            return false;
        }
        
        for (int i = 0; i < parent.getPathLevels().size(); i++) {
            if (!this.getPathLevels().get(i).equals(parent.getPathLevels().get(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * in place append new level to the end, return self pointer for convenience
     * @param level
     * @return
     */
    public OnyxItemURI append(String level)
    {
        mPathLevels.add(level);
        
        return this;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        
        if (obj instanceof OnyxItemURI) {
            OnyxItemURI uri = (OnyxItemURI)obj;
            if (this.getPathLevels().size() != uri.getPathLevels().size()) {
                return false;
            }
            
            for (int i = 0; i < this.getPathLevels().size(); i++) {
                if (!this.getPathLevels().get(i).equals(uri.getPathLevels().get(i))) {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public Object clone()
    {
        return new OnyxItemURI(this.getPathLevels());
    }
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (String s : mPathLevels)
        {
            sb.append(SEPERATOR).append(s);
        }
        
        return sb.toString();
    }
}
