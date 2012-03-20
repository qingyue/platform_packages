package com.onyx.android.launcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;

public class SettingsAdapter extends GridItemBaseAdapter
{
    private LayoutInflater mInflater = null;

    private static final int sItemMinWidth = 145;
    private static final int sItemMinHeight = 45;
    private static final int sHorizontalSpacing = 10;
    private static final int sVerticalSpacing = 10;

    public SettingsAdapter(Context context, OnyxGridView gridView)
    {
        super(gridView);
        
        mInflater = LayoutInflater.from(context);
        
        this.getPageLayout().setViewMode(GridViewMode.Detail);

        this.getPageLayout().setItemMinWidth(sItemMinWidth);
        this.getPageLayout().setItemMinHeight(sItemMinHeight);
        this.getPageLayout().setHorizontalSpacing(sHorizontalSpacing);
        this.getPageLayout().setVerticalSpacing(sVerticalSpacing);
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        int idx = this.getPaginator().getItemIndex(position, this.getPaginator().getPageIndex());
        GridItemData item_data = this.getItems().get(idx);

        Holder holder = new Holder();

        View ret_view = mInflater.inflate(R.layout.gridview_item_settings, null);

        holder.textview_detail_launcher_name = (TextView)ret_view.findViewById(R.id.textviewr_settings_gridview_itme_name);
        holder.imageview_detail_launcher = (ImageView)ret_view.findViewById(R.id.imageview_settings_gridview_item_cover);

        LayoutParams imageview_params = new LayoutParams(this.getPageLayout().getItemCurrentHeight(),
                this.getPageLayout().getItemCurrentHeight());
        holder.imageview_detail_launcher.setLayoutParams(imageview_params);

        OnyxGridView.LayoutParams ret_view_params = new OnyxGridView.LayoutParams(this.getPageLayout().getItemCurrentWidth(),
                        this.getPageLayout().getItemCurrentHeight());
        ret_view.setLayoutParams(ret_view_params);

        ret_view.setTag(item_data);

        if (item_data.getBitmap() != null) {
            holder.imageview_detail_launcher.setImageBitmap(item_data.getBitmap());
        }
        else {
            holder.imageview_detail_launcher.setImageResource(item_data.getImageResourceId());
        }

        if (holder.textview_detail_launcher_name != null) {
            holder.textview_detail_launcher_name.setText(item_data.getText());
        }

        return ret_view;
    }

    public final class Holder{
        public TextView textview_detail_launcher_name = null;
        public ImageView imageview_detail_launcher = null;
    }
}