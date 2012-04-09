/**
 * 
 */
package com.onyx.android.launcher.dialog.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.adapter.GridItemBaseAdapter;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;

/**
 * @author joy
 *
 */
public class ListViewPathIndicatorAdapter extends GridItemBaseAdapter
{
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private static final int sItemMinHeight = 60;
    private static final int sOmit = 1;
    
    public ListViewPathIndicatorAdapter(Context context, OnyxGridView gridView)
    {
        super(gridView);
        
        mContext = context;
        
        mInflater = LayoutInflater.from(mContext);
        
        this.getPageLayout().setItemDetailMinHeight(sItemMinHeight);
        this.getPageLayout().setViewMode(GridViewMode.Detail);

        this.getPaginator().initializePageData(this.getItems().size(), this.getPaginator().getPageSize());
        this.notifyDataSetChanged();
    }
    
    @Override
    public int getCount()
    {
        return super.getCount();
    }

    @Override
    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View ret_view = null;
        if (convertView != null) {
            ret_view = convertView;
        }
        else {
            ret_view = mInflater.inflate(R.layout.dialog_path_indicator_item, null);
        }
        
        TextView textView_pathName = (TextView)ret_view.findViewById(R.id.textview_path_name);
        int idx = this.getPaginator().getItemIndex(position, this.getPaginator().getPageIndex());
        GridItemData item_data = this.getItems().get(idx);
        
        if (this.getPaginator().getPageCount() > 1) {
            if (position > sOmit || this.getPaginator().getPageSize() <= sOmit + 1) {
                int id = this.getItems().size() - this.getPaginator().getPageSize() + position;
                item_data = this.getItems().get(id);
                textView_pathName.setText(item_data.getText());
            }
            else if (position ==sOmit) {
                textView_pathName.setText("����");
                item_data = null;
            }
            else {
                textView_pathName.setText(item_data.getText());
            }
        }
        else {
            textView_pathName.setText(item_data.getText());
        }
        
        final int height = Math.max(this.getPageLayout().getItemMinHeight(), sItemMinHeight);
        textView_pathName.getLayoutParams().height = height;
        ret_view.setTag(item_data);
        
        return ret_view;
    }
    
    public static GridItemData getViewTag(View view)
    {
        return (GridItemData)view.getTag();
    }
}
