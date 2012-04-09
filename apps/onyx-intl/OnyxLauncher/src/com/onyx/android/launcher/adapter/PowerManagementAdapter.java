/**
 * 
 */
package com.onyx.android.launcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridItemData;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;

/**
 * @author Administrator
 *
 */
public class PowerManagementAdapter extends GridItemBaseAdapter
{
    private LayoutInflater mInflater = null;
    private GridItemData mItemData = null;
    private String mCurrentSetup = null;

    private boolean mIsInit = true;

    private static final int sItemMinWidth = 145;
    private static final int sItemMinHeight = 140;
    private static final int sHorizontalSpacing = 0;
    private static final int sVerticalSpacing = 0;
    private static final int sItemDetailMinHeight = 60;

    public PowerManagementAdapter(Context context, OnyxGridView gridView, String initSetup)
    {
        super(gridView);

        mInflater = LayoutInflater.from(context);
        mCurrentSetup = initSetup;

        this.getPageLayout().setItemMinWidth(sItemMinWidth);
        this.getPageLayout().setItemMinHeight(sItemMinHeight);
        this.getPageLayout().setItemDetailMinHeight(sItemDetailMinHeight);
        this.getPageLayout().setHorizontalSpacing(sHorizontalSpacing);
        this.getPageLayout().setVerticalSpacing(sVerticalSpacing);

        this.getPageLayout().setViewMode(GridViewMode.Detail);
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
        View ret_view = null;

        int idx = this.getPaginator().getItemIndex(position, this.getPaginator().getPageIndex());
        GridItemData item_data = this.getItems().get(idx);

        ret_view = mInflater.inflate(R.layout.activity_powermanagement_adapter_view, null);
        TextView textview_time = (TextView)ret_view.findViewById(R.id.textview_gridview_item_name);
        textview_time.setText(item_data.getText());

        CheckBox cb = (CheckBox) ret_view.findViewById(R.id.checkbox_multi);

        if (mIsInit) {
            if (item_data.getTag().toString().equals(mCurrentSetup)) {
                cb.setChecked(true);
                mIsInit = false;
            }
        }

        if (mItemData != null && mItemData.getText() != null) {
            if (item_data.getText().toString().equals(mItemData.getText().toString())) {
                cb.setChecked(true);
            }
        }

        if (ret_view.getLayoutParams() == null) {
            OnyxGridView.LayoutParams params = new OnyxGridView.LayoutParams(this.getPageLayout().getItemCurrentWidth(),
                    this.getPageLayout().getItemCurrentHeight());
            ret_view.setLayoutParams(params);  
        }
        else {
            ret_view.getLayoutParams().width = this.getPageLayout().getItemCurrentWidth();
            ret_view.getLayoutParams().height = this.getPageLayout().getItemCurrentHeight();
        }

        ret_view.setTag(item_data);

        return ret_view;
    }

    public void setGridItemData(GridItemData item_data)
    {
        this.mItemData = item_data;
        this.notifyDataSetChanged();
    }
}
