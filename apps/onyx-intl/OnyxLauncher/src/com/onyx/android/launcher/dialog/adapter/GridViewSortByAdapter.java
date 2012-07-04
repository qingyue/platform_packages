/**
 * 
 */
package com.onyx.android.launcher.dialog.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.data.GridViewPageLayout.GridViewMode;
import com.onyx.android.sdk.ui.data.OnyxPagedAdapter;

/**
 * @author joy
 * 
 */
public class GridViewSortByAdapter extends OnyxPagedAdapter
{
    private static final int sItemMinWidth = 145;
    private static final int sItemMinHeight = 50;
    private static final int sHorizontalSpacing = 0;
    private static final int sVerticalSpacing = 0;
    private static final int sItemDetailMinHeight = 50;

    private Context mContext = null;
    private SortOrder[] mOrders = null;

    public GridViewSortByAdapter(Context context, OnyxGridView gridView,
            SortOrder[] orders)
    {
        super(gridView);

        mContext = context;
        mOrders = orders;

        this.getPageLayout().setItemMinWidth(sItemMinWidth);
        this.getPageLayout().setItemMinHeight(sItemMinHeight);
        this.getPageLayout().setItemThumbnailMinHeight(sItemMinHeight);
        this.getPageLayout().setItemDetailMinHeight(sItemDetailMinHeight);
        this.getPageLayout().setHorizontalSpacing(sHorizontalSpacing);
        this.getPageLayout().setVerticalSpacing(sVerticalSpacing);
        this.getPageLayout().setViewMode(GridViewMode.Detail);

        this.getPaginator().initializePageData(mOrders.length,
                this.getPaginator().getPageSize());
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

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View ret_view = null;

        int idx = this.getPaginator().getAbsoluteIndex(position);
        SortOrder order = mOrders[idx];

        if ((convertView != null) && (convertView instanceof TextView)) {
            ret_view = convertView;
        } else {
            ret_view = View.inflate(mContext, R.layout.dialog_sort_by_item,
                    null);
        }

        TextView textView = (TextView) ret_view
                .findViewById(R.id.textview_sort_by_name);
        TextView textViewDividing = (TextView) ret_view
                .findViewById(R.id.textview_dividing);

        // TODO: getItemCurrentHeight() maybe 0, need further watch
        final int height = Math.max(
                this.getPageLayout().getItemCurrentHeight(), this
                        .getPageLayout().getItemMinHeight());
        OnyxGridView.LayoutParams params = new OnyxGridView.LayoutParams(this
                .getPageLayout().getItemCurrentWidth(), height);
        ret_view.setLayoutParams(params);

        switch (order) {
        case Name:
            textView.setText(R.string.By_Name);
            break;
        case FileType:
            textView.setText(R.string.By_Type);
            break;
        case Size:
            textView.setText(R.string.By_Size);
            break;
        case AccessTime:
            textView.setText(R.string.By_Access_Time);
            break;
        default:
            textView.setText(R.string.unknown);
            break;
        }

        ret_view.setTag(order);
        if (position == (mOrders.length - 1)) {
            textViewDividing.setVisibility(View.INVISIBLE);
        }

        return ret_view;
    }

}
