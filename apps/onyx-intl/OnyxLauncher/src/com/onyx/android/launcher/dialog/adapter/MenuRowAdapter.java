/**
 * 
 */
package com.onyx.android.launcher.dialog.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.data.util.RefValue;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.OnStateChangedListener;
import com.onyx.android.sdk.ui.data.OnyxPagedAdapter;
import com.onyx.android.sdk.ui.menu.OnyxMenuItem;
import com.onyx.android.sdk.ui.menu.OnyxMenuRow;

/**
 * @author joy
 *
 */
public class MenuRowAdapter extends OnyxPagedAdapter
{
    private static final String TAG = "MenuRowAdapter";
    private static final int sItemMinWidth = 90;
    private static final int sItemMinHeight = 70;
    
    private LayoutInflater mInflater = null;
    /**
     * each OnyxMenuRow will occupy entire one row or multiple rows, supplement by empty grid item views
     * so there is a mapping between OnyxMenuItems in OnyxMenuRow and non-empty grid item views
     */
    private ArrayList<OnyxMenuRow> mMenuRows = null;

    public MenuRowAdapter(Context context, OnyxGridView gridView, ArrayList<OnyxMenuRow> menuRows)
    {
        super(gridView);
        
        this.getPageLayout().setItemMinWidth(sItemMinWidth);
        this.getPageLayout().setItemMinHeight(sItemMinHeight);

        mInflater = LayoutInflater.from(context);
        mMenuRows = menuRows;
        
        this.getPageLayout().registerOnStateChangedListener(new OnStateChangedListener()
        {
            
            @Override
            public void onStateChanged()
            {
                Log.d(TAG, "on state of page layout changed");
                
                MenuRowAdapter.this.setupRowsOfGridView();
            }
        });
        
        this.setupRowsOfGridView();
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
    public boolean isEnabled(int position)
    {
        int idx = this.getPaginator().getAbsoluteIndex(position);
        if (this.getPageLayout().getLayoutColumnCount() <= 0) {
            return false;
        }
        
        RefValue<OnyxMenuItem> item = new RefValue<OnyxMenuItem>();
        if (!map(idx, this.getPageLayout().getLayoutColumnCount(), item)) {
            return false;
        }
        else {
            return item.getValue().getEnabled();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View ret_view = null;
        if (convertView != null) {
            ret_view = convertView;
        } else {
            ret_view = mInflater.inflate(R.layout.dialog_menu_others_gridview_item, null);
        }
        
        TextView title = (TextView) ret_view.findViewById(R.id.textview_menu);
        ImageView imageView = (ImageView) ret_view.findViewById(R.id.imageview_menu);
        imageView.getLayoutParams().width = this.getPageLayout().getItemMinWidth();
        imageView.getLayoutParams().height = this.getPageLayout().getItemMinHeight();
        
        int idx = this.getPaginator().getAbsoluteIndex(position);
        if (this.getPageLayout().getLayoutColumnCount() <= 0) {
            title.setText("");
            imageView.setImageResource(R.drawable.transparent);

            return ret_view;
        }
        
        RefValue<OnyxMenuItem> item = new RefValue<OnyxMenuItem>();
        if (!map(idx, this.getPageLayout().getLayoutColumnCount(), item)) {
            title.setText("");
            imageView.setImageResource(R.drawable.transparent);

            return ret_view;
        }
        else {
          imageView.setImageResource(item.getValue().getImageResourceId());
          
          title.setText(item.getValue().getTextResourceId());
          title.getLayoutParams().width = this.getPageLayout().getItemMinWidth();
          
          if (!item.getValue().getEnabled()) {
              TextView shelter = (TextView) ret_view.findViewById(R.id.textview_shelter);
              if (shelter.getVisibility() == View.GONE) {
                  shelter.setVisibility(View.VISIBLE);
            }
              title.setTextColor(Color.rgb(100, 100, 100));
          }
          
          ret_view.setTag(item.getValue());

          return ret_view;
        }
    }
    
    private void setupRowsOfGridView()
    {
        int columns = this.getPageLayout().getLayoutColumnCount();
        if (columns > 0) {
            Log.d(TAG, "page layout columns: " + columns);
            
            int layout_item_count = 0;
            for (OnyxMenuRow r : mMenuRows) {
                layout_item_count += r.getMenuItems().size();
                
                int mod = r.getMenuItems().size() % columns;
                if (mod > 0) {
                    layout_item_count += (columns - mod);
                }
            }
            
            Log.d(TAG, "layout item count: " + layout_item_count + ", page size: " + 
                    this.getPageLayout().getLayoutRowCount() + ", " +
                    this.getPageLayout().getLayoutColumnCount());
            
            this.getPaginator().initializePageData(layout_item_count, 
                    this.getPageLayout().getLayoutRowCount() * this.getPageLayout().getLayoutColumnCount());
        }
    }
    
    private boolean map(int index, int columns, RefValue<OnyxMenuItem> result)
    {
        if (columns <= 0) {
            return false;
        }
        
        for (OnyxMenuRow r : mMenuRows) {
            if (index < r.getMenuItems().size()) {
                result.setValue(r.getMenuItems().get(index));
                return true;
            }
            
            int mod = r.getMenuItems().size() % columns;
            int rows = (r.getMenuItems().size() / columns) + (mod > 0 ? 1 : 0);
            
            int layout_size = rows * columns;
            if (index < layout_size) {
                return false;
            }
            
            index -= layout_size;
        }
        
        return false;
    }
}
