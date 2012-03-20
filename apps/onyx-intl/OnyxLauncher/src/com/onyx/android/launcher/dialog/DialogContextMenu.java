/**
 * 
 */
package com.onyx.android.launcher.dialog;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.onyx.android.launcher.OnyxBaseActivity;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.dialog.adapter.MenuRowAdapter;
import com.onyx.android.launcher.dialog.adapter.MenuSuiteAdapter;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.menu.OnyxMenuItem;
import com.onyx.android.sdk.ui.menu.OnyxMenuSuite;

/**
 * @author joy
 *
 */
public class DialogContextMenu extends OnyxDialogBase
{
    OnyxGridView mGridViewSuiteTitle = null;
    OnyxGridView mGridViewSuiteContent = null;

    public DialogContextMenu(Context context, ArrayList<OnyxMenuSuite> menuSuites)
    {
        super(context);

        this.setContentView(R.layout.dialog_context_menu);

        mGridViewSuiteTitle = (OnyxGridView)this.findViewById(R.id.gridview_suite_title);
        mGridViewSuiteContent = (OnyxGridView)this.findViewById(R.id.gridview_suite_content);

        mGridViewSuiteTitle.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                OnyxMenuSuite suite = (OnyxMenuSuite)view.getTag(); 
                MenuRowAdapter adapter = new MenuRowAdapter(DialogContextMenu.this.getContext(),
                        mGridViewSuiteContent, suite.getMenuRows());
                mGridViewSuiteContent.setAdapter(adapter);
            }
        });

        mGridViewSuiteContent.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                DialogContextMenu.this.dismiss();

                OnyxMenuItem item = (OnyxMenuItem)view.getTag();
                item.notifyClick();
            }
        });

        MenuSuiteAdapter title_adapter = new MenuSuiteAdapter(context, mGridViewSuiteTitle, menuSuites);
        mGridViewSuiteTitle.setAdapter(title_adapter);

        if (menuSuites.size() > 0) {
            MenuRowAdapter content_adapter = new MenuRowAdapter(context, mGridViewSuiteContent, 
                    menuSuites.get(0).getMenuRows());
            mGridViewSuiteContent.setAdapter(content_adapter);
        }

        OnyxBaseActivity activity = (OnyxBaseActivity) context;

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = getWindow().getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        LayoutParams params = getWindow().getAttributes();

        if (activity.getRequestedOrientation() == -1) {
            if (metrics.widthPixels > metrics.heightPixels) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            params.width = (int) (metrics.widthPixels * 0.9);
        }
        else if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            params.width = (int) (metrics.widthPixels * 0.6);
        }
    }

}
