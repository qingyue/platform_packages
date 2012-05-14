/**
 * 
 */
package com.onyx.android.launcher.dialog.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.OnyxPagedAdapter;
import com.onyx.android.sdk.ui.menu.OnyxMenuSuite;

/**
 * @author joy
 *
 */
public class MenuSuiteAdapter extends OnyxPagedAdapter
{
    private LayoutInflater mInflater = null;
    private ArrayList<OnyxMenuSuite> mMenuSuites = null;
    
    private static final int sItemMinWidth = 90;
    private static final int sItemMinHeight = 70;

    public MenuSuiteAdapter(Context context, OnyxGridView gridView, ArrayList<OnyxMenuSuite> menuSuites)
    {
        super(gridView);
        
        this.getPageLayout().setItemMinWidth(sItemMinWidth);
        this.getPageLayout().setItemMinHeight(sItemMinHeight);

        mInflater = LayoutInflater.from(context);
        mMenuSuites = menuSuites;
        
        this.getPaginator().initializePageData(mMenuSuites.size(), 0);
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
        } else {
            ret_view = mInflater.inflate(R.layout.dialog_menu_others_gridview_item, null);
        }
        
        int idx = this.getPaginator().getAbsoluteIndex(position);
        OnyxMenuSuite suite = mMenuSuites.get(idx);

        TextView title = (TextView) ret_view.findViewById(R.id.textview_menu);
        ImageView imageView = (ImageView) ret_view.findViewById(R.id.imageview_menu);

        imageView.setImageResource(suite.getImageResourceId());
        imageView.getLayoutParams().width = this.getPageLayout().getItemMinWidth();
        imageView.getLayoutParams().height = this.getPageLayout().getItemMinHeight();
        
        title.setText(suite.getTextResourceId());
        title.getLayoutParams().width = this.getPageLayout().getItemMinWidth();
        
        ret_view.setTag(suite);

        return ret_view;
    }

}
