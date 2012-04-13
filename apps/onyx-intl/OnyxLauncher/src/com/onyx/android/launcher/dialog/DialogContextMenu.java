/**
 * 
 */
package com.onyx.android.launcher.dialog;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.dialog.adapter.MenuRowAdapter;
import com.onyx.android.launcher.dialog.adapter.MenuSuiteAdapter;
import com.onyx.android.launcher.view.ContextMenuGridView;
import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.ui.menu.OnyxMenuItem;
import com.onyx.android.sdk.ui.menu.OnyxMenuSuite;
import com.onyx.android.sdk.ui.util.OnyxFocusFinder;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * @author joy
 *
 */
public class DialogContextMenu extends OnyxDialogBase
{
    ContextMenuGridView mGridViewSuiteTitle = null;
    ContextMenuGridView mGridViewSuiteContent = null;

    public DialogContextMenu(Context context, ArrayList<OnyxMenuSuite> menuSuites)
    {
        super(context);

        this.setContentView(R.layout.dialog_context_menu);

        mGridViewSuiteTitle = (ContextMenuGridView)this.findViewById(R.id.gridview_suite_title);
        mGridViewSuiteContent = (ContextMenuGridView)this.findViewById(R.id.gridview_suite_content);

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
                mGridViewSuiteTitle.requestFocusFromTouch();
                mGridViewSuiteTitle.setSelection(position);
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

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = getWindow().getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        LayoutParams params = getWindow().getAttributes();

        if (metrics.widthPixels > metrics.heightPixels) {
        	params.width = (int) (metrics.widthPixels * 0.6); 
        }
        else {
        	params.width = (int) (metrics.widthPixels * 0.9);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if ((keyCode == KeyEvent.KEYCODE_DPAD_UP) || 
                (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) ||
                (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ||
                (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
            ScreenUpdateManager.invalidate(this.getWindow().getDecorView(), UpdateMode.DW);
            
            if (this.getCurrentFocus() != null) {
                int direction = 0;
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    direction = View.FOCUS_UP;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    direction = View.FOCUS_DOWN;
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    direction = View.FOCUS_LEFT;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    direction = View.FOCUS_RIGHT;
                    break;
                default:
                    assert(false);
                    throw new IndexOutOfBoundsException();
                }

                View dst_view = this.getCurrentFocus().focusSearch(direction);
                if (dst_view == null) { 
                    int reverse_direction = OnyxFocusFinder.getReverseDirection(direction);
                    ContextMenuGridView gridView = (ContextMenuGridView)OnyxFocusFinder.findFartherestViewInDirection(this.getCurrentFocus(), reverse_direction);

                    Rect rect = OnyxFocusFinder.getAbsoluteFocusedRect(this.getCurrentFocus());
                    gridView.searchAndSelectNextFocusableChildItem(direction, rect);

                    if (gridView.getChildAt(gridView.getSelectedItemPosition()).getTag() == null) {
						for (int i = gridView.getSelectedItemPosition(); i >= 0; i--) {
							if (gridView.getChildAt(i).getTag() != null) {
								ScreenUpdateManager.invalidate(gridView, UpdateMode.DW);
								gridView.setSelection(i);
								return true;
							}
						}
					}

                    return true;
                }
            }
        }
    	return super.onKeyDown(keyCode, event);
    }
}
