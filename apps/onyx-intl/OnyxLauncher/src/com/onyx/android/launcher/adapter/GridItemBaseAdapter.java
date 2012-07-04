package com.onyx.android.launcher.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.onyx.android.launcher.data.OnyxItemSorter;
import com.onyx.android.sdk.data.AscDescOrder;
import com.onyx.android.sdk.data.OnyxItemURI;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.OnyxPagedAdapter;

public abstract class GridItemBaseAdapter extends OnyxPagedAdapter
{
    @SuppressWarnings("unused")
    private final static String sTag = "OnyxBaseAdapter";

    public interface OnHostURIChangedListener
    {
        void onHostURIChanged();
    }

    public interface OnItemFilledListener
    {
        void onItemFilled();
    }

    // initialize to avoid null checking
    private OnHostURIChangedListener mOnHostURIChangedListener = new OnHostURIChangedListener()
    {

        @Override
        public void onHostURIChanged()
        {
            // do nothing
        }
    };

    public void setOnHostURIChangedListener(OnHostURIChangedListener l)
    {
        mOnHostURIChangedListener = l;
    }

    // initialize to avoid null checking
    private OnItemFilledListener mOnItemFilledListener = new OnItemFilledListener()
    {

        @Override
        public void onItemFilled()
        {
            // do nothing
        }
    };

    public void setOnItemFilledListener(OnItemFilledListener l)
    {
        mOnItemFilledListener = l;
    }

    // if mItems is empty, then we could not deduce host URI from it's children,
    // so store it explicitly
    private OnyxItemURI mHostURI = null;
    private ArrayList<GridItemData> mItems = new ArrayList<GridItemData>();
    private boolean mMultipleSelectionMode = false;
    private ArrayList<GridItemData> mSelectedItems = new ArrayList<GridItemData>();

    protected GridItemBaseAdapter(OnyxGridView gridView)
    {
        super(gridView);
    }

    public OnyxItemURI getHostURI()
    {
        return mHostURI;
    }

    public ArrayList<GridItemData> getItems()
    {
        return mItems;
    }

    public void fillItems(OnyxItemURI hostURI, GridItemData[] items)
    {
        mItems.clear();

        mHostURI = hostURI;
        this.mOnHostURIChangedListener.onHostURIChanged();

        for (GridItemData i : items) {
            mItems.add(i);
        }
        this.getPaginator().initializePageData(mItems.size(),
                this.getPaginator().getPageSize());

        mOnItemFilledListener.onItemFilled();
    }

    public void fillItems(OnyxItemURI hostURI,
            Collection<? extends GridItemData> items)
    {
        mItems.clear();

        mHostURI = hostURI;
        this.mOnHostURIChangedListener.onHostURIChanged();

        mItems.addAll(items);
        this.getPaginator().initializePageData(mItems.size(),
                this.getPaginator().getPageSize());

        mOnItemFilledListener.onItemFilled();
    }

    public void appendItem(GridItemData data)
    {
        mItems.add(data);
        this.getPaginator().setItemCount(mItems.size());
    }

    public void appendItems(GridItemData[] data)
    {
        ArrayList<GridItemData> list = new ArrayList<GridItemData>();
        for (GridItemData d : data) {
            list.add(d);
        }

        mItems.addAll(list);
        this.getPaginator().setItemCount(mItems.size());
    }

    public void appendItems(Collection<? extends GridItemData> data)
    {
        mItems.addAll(data);
        this.getPaginator().setItemCount(mItems.size());
    }

    /**
     * item must come from Adapter's collection
     * 
     * @param item
     * @return
     */
    public boolean removeItem(GridItemData item)
    {
        if (mItems.remove(item)) {
            this.getPaginator().setItemCount(mItems.size());
            return true;
        }

        return false;
    }

    /**
     * items must come from Adapter's collection
     * 
     * @param items
     * @return
     */
    public boolean removeItems(Collection<? extends GridItemData> items)
    {
        if (mItems.removeAll(items)) {
            this.getPaginator().setItemCount(mItems.size());
            return true;
        }

        return false;
    }

    public void sortItems(SortOrder order, AscDescOrder ascOrder)
    {
        Comparator<GridItemData> comp = OnyxItemSorter.getComparator(order,
                ascOrder);
        if (comp != null) {
            Collections.sort(mItems, comp);
            this.notifyDataSetChanged();
        }
    }

    /**
     * return -1 if failed
     * 
     * @param uri
     * @return
     */
    public boolean locateItemInGridView(OnyxItemURI uri)
    {
        int i = 0;
        for (; i < mItems.size(); i++) {
            if (mItems.get(i).getURI().equals(uri)) {
                break;
            }
        }

        if (i >= mItems.size()) {
            return false;
        }
        
        return this.locatePageByItemIndex(i);
    }

    public boolean getMultipleSelectionMode()
    {
        return mMultipleSelectionMode;
    }

    public void setMultipleSelectionMode(boolean value)
    {
        if (mMultipleSelectionMode != value) {
            mMultipleSelectionMode = value;
            this.notifyDataSetChanged();
        }
    }

    public ArrayList<GridItemData> getSelectedItems()
    {
        return mSelectedItems;
    }

    public void addSelectedItems(GridItemData item)
    {
        if (mSelectedItems.contains(item)) {
            mSelectedItems.remove(item);
            this.notifyDataSetChanged();
            return;
        }

        mSelectedItems.add(item);
        this.notifyDataSetChanged();
    }

    public void cleanSelectedItems()
    {
        mSelectedItems.clear();
        this.notifyDataSetChanged();
    }
}
