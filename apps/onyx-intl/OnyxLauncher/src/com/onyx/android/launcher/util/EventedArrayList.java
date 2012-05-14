/**
 * 
 */
package com.onyx.android.launcher.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * notify when adding item/items
 * 
 * @author joy
 *
 * @param <T>
 */
public final class EventedArrayList<T> extends ArrayList<T> 
{
    /**
     * avoid compiler warning
     */
    private static final long serialVersionUID = -1824168527062665548L;

    public interface OnAddedListener
    {
        public void onAdded();
    }
    
    // initialize to avoid null checking
    private OnAddedListener mOnAddedListener = new OnAddedListener()
    {
        
        @Override
        public void onAdded()
        {
            // do nothing
        }
    };
    public void SetOnAddedListener(OnAddedListener l)
    {
        mOnAddedListener = l;
    }
    
    @Override
    public boolean add(T object) 
    {
        boolean res = super.add(object);
        if (res) {
            mOnAddedListener.onAdded();
        }
        
        return res;
    };
    
    @Override
    public void add(int index, T object) 
    {
        super.add(index, object);
        mOnAddedListener.onAdded();
    };
    
    @Override
    public boolean addAll(Collection<? extends T> collection)
    {
        boolean res = super.addAll(collection);
        if (res) {
            mOnAddedListener.onAdded();
        }
        
        return res;
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends T> collection)
    {
        boolean res = super.addAll(index, collection);
        if (res) {
            mOnAddedListener.onAdded();
        }
        
        return res;
    }
}
