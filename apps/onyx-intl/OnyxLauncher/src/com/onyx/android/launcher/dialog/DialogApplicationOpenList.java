/**
 * 
 */
package com.onyx.android.launcher.dialog;

import java.util.List;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.dialog.adapter.GridViewApplicationOpenListAdapter;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.data.sys.OnyxAppPreferenceCenter;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * 
 * @author joy
 *
 */
public class DialogApplicationOpenList extends OnyxDialogBase
{
    @SuppressWarnings("unused")
    private static final String TAG = "DialogApplicationOpenList";
    
    public interface OnApplicationSelectedListener
    {
        public void onApplicationSelected(ResolveInfo info, boolean makeDefault);
    }
    
    // initialize to avoid null checking
    private OnApplicationSelectedListener mOnApplicationSelectedListener = new OnApplicationSelectedListener()
    {
        
        @Override
        public void onApplicationSelected(ResolveInfo info, boolean makeDefault)
        {
            // do nothing
        }
    };
    
    public void setOnApplicationSelectedListener(OnApplicationSelectedListener l)
    {
        mOnApplicationSelectedListener = l;
    }

    private OnyxGridView mGridView = null;
    private Button mButtonNext = null;
    private Button mButtonPrevious = null;
    private TextView mTextViewPage = null;
    private CheckBox mCheckBoxDefaultOpen = null;
    private Context mContext = null;
    private View mView = null;
    private GridViewApplicationOpenListAdapter mAdapter = null;
    
    private String mExt = null;

    public DialogApplicationOpenList(Context context, List<ResolveInfo> resolveInfoList, String ext)
    {
        super(context);

        mView = getLayoutInflater().inflate(R.layout.dialog_application_openlist, null);
        this.setContentView(mView);
        
        mContext = context;
        mExt =ext;
        mGridView = (OnyxGridView)this.findViewById(R.id.gridview_openlist);
        mButtonNext = (Button)this.findViewById(R.id.button_next);
        mButtonPrevious = (Button)this.findViewById(R.id.button_prev);
        mTextViewPage = (TextView)this.findViewById(R.id.textview_dialog_application_page);
        mCheckBoxDefaultOpen = (CheckBox)this.findViewById(R.id.checkbox_default_open);
        
        mGridView.setOnItemClickListener(new OnItemClickListener()
        { 
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                
                ResolveInfo resolve_info = (ResolveInfo) view.getTag();
                
                if (mCheckBoxDefaultOpen.isChecked()) {
                    String appName = resolve_info.activityInfo.applicationInfo.loadLabel(mContext.getPackageManager()).toString();
                    String pkg = resolve_info.activityInfo.packageName;
                    String cls = resolve_info.activityInfo.name;

                    if (OnyxAppPreferenceCenter.setAppPreference(mContext, mExt, appName, pkg, cls)) {
                        Toast.makeText(mContext, R.string.Succeed_setting, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(mContext, R.string.Fail_setting, Toast.LENGTH_SHORT).show();
                    }
                }
                
                ResolveInfo info = GridViewApplicationOpenListAdapter.getViewTag(view);
                mOnApplicationSelectedListener.onApplicationSelected(info, false);
                DialogApplicationOpenList.this.dismiss();
            }
        });
        
        mButtonNext.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                int height = mGridView.getHeight();
                
                if (mAdapter.getPaginator().canNextPage()) {
                    mAdapter.getPaginator().nextPage();
                }
                
                if (height != mGridView.getLayoutParams().height) {
                    mGridView.getLayoutParams().height = height;
                }
            }
        });
        
        mButtonPrevious.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                if (mAdapter.getPaginator().canPrevPage()) {
                    mAdapter.getPaginator().prevPage();
                }
            }
        });
        
        mAdapter = new GridViewApplicationOpenListAdapter(context, mGridView, resolveInfoList);
        mAdapter.registerDataSetObserver(new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                DialogApplicationOpenList.this.updateTextViewPage();
            }
            
            @Override
            public void onInvalidated()
            {
                DialogApplicationOpenList.this.updateTextViewPage();
            }
        });
        
        mGridView.setAdapter(mAdapter);
        mAdapter.getPaginator().setPageSize(resolveInfoList.size());
    }
    
    private void updateTextViewPage()
    {
        final int current_page = mAdapter.getPaginator().getPageIndex() + 1;
        final int page_count = (mAdapter.getPaginator().getPageCount() != 0) ?
                mAdapter.getPaginator().getPageCount() : 1;

        mTextViewPage.setText(String.valueOf(current_page) + "/" + String.valueOf(page_count));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	ScreenUpdateManager.invalidate(mView, UpdateMode.GU);
    	return super.onKeyDown(keyCode, event);
    }
}
