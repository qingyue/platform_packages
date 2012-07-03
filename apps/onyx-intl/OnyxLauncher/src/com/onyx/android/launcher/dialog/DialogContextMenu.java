/**
 * 
 */
package com.onyx.android.launcher.dialog;

import java.util.ArrayList;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.onyx.android.launcher.OnyxBaseActivity;
import com.onyx.android.launcher.R;
import com.onyx.android.launcher.data.StandardMenuFactory;
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
	private ContextMenuGridView mGridViewSuiteTitle = null;
	private ContextMenuGridView mGridViewSuiteContent = null;
	private ContextMenuGridView mGridViewSystemMenu = null;

	private int mGridViewSystemMenuRowItems= 0;

	public DialogContextMenu(final OnyxBaseActivity activity, ArrayList<OnyxMenuSuite> menuSuites)
	{
		super(activity);

		this.setContentView(R.layout.dialog_context_menu);

		mGridViewSuiteTitle = (ContextMenuGridView)this.findViewById(R.id.gridview_suite_title);
		mGridViewSuiteContent = (ContextMenuGridView)this.findViewById(R.id.gridview_suite_content);
		mGridViewSystemMenu = (ContextMenuGridView)this.findViewById(R.id.gridview_system_menu);

		mGridViewSuiteTitle.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				OnyxMenuSuite suite = (OnyxMenuSuite)view.getTag(); 
				MenuRowAdapter adapter = new MenuRowAdapter(DialogContextMenu.this.getContext(),
						mGridViewSuiteContent, suite.getMenuRows());
				ScreenUpdateManager.invalidate(mGridViewSuiteContent, UpdateMode.GU);
				ScreenUpdateManager.invalidate(mGridViewSuiteTitle, UpdateMode.GU);

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
				OnyxMenuItem item = (OnyxMenuItem)view.getTag();

				if (item != null) {
					if (!item.getEnabled()) {
						return;
					}
					DialogContextMenu.this.dismiss();

					item.notifyClick();
				}
			}
		});

		MenuSuiteAdapter title_adapter = new MenuSuiteAdapter(activity, mGridViewSuiteTitle, menuSuites);
		mGridViewSuiteTitle.setAdapter(title_adapter);

		if (menuSuites.size() > 0) {
			MenuRowAdapter content_adapter = new MenuRowAdapter(activity, mGridViewSuiteContent, 
					menuSuites.get(0).getMenuRows());
			mGridViewSuiteContent.setAdapter(content_adapter);
		}

		MenuRowAdapter system_menu_adapter = new MenuRowAdapter(activity, 
				mGridViewSystemMenu, 
				StandardMenuFactory.getSystemMenuSuite(activity).getMenuRows());
		mGridViewSystemMenu.setAdapter(system_menu_adapter);
		mGridViewSystemMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				OnyxMenuItem item = (OnyxMenuItem)view.getTag();
				if (item != null) {
					DialogContextMenu.this.dismiss();

					item.notifyClick();
				}
			}

		});

		//Get menu items number to bottom bar
		mGridViewSystemMenuRowItems = StandardMenuFactory
				.getSystemMenuSuite(activity).getMenuRows().get(0).getMenuItems().size();

		mGridViewSystemMenu.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_LEFT:
						if (mGridViewSystemMenu.getSelectedItemPosition() == 0) {
							ScreenUpdateManager.invalidate(mGridViewSystemMenu, UpdateMode.DW);
							if (mGridViewSystemMenuRowItems > 1) {
								mGridViewSystemMenu.setSelection(mGridViewSystemMenuRowItems - 1);
							}
							return true;
						}
						return false;
					case KeyEvent.KEYCODE_DPAD_RIGHT:
						if (mGridViewSystemMenuRowItems > 1) {
							if (mGridViewSystemMenu.getSelectedItemPosition() == (mGridViewSystemMenuRowItems - 1)) {
								ScreenUpdateManager.invalidate(mGridViewSystemMenu, UpdateMode.DW);
								mGridViewSystemMenu.setSelection(0);
								return true;
							}
						}
						return false;
					case KeyEvent.KEYCODE_DPAD_UP:
						ContextMenuGridView gridView = (ContextMenuGridView)OnyxFocusFinder
						.findFartherestViewInDirection(DialogContextMenu.this.getCurrentFocus(), View.FOCUS_UP);

						if (gridView != null) {
							Rect rect = OnyxFocusFinder.getAbsoluteFocusedRect(DialogContextMenu.this.getCurrentFocus());
							gridView.searchAndSelectNextFocusableChildItem(View.FOCUS_UP, rect);
							int position = gridView.getSelectedItemPosition();

							for (int i = position; i >= 0; i--) {
								if (gridView.getChildAt(i).getTag() != null) {
									ScreenUpdateManager.invalidate(gridView, UpdateMode.DW);
									gridView.setSelection(i);
									return true;
								}
							}
						}
						return false;

					default:
						break;
					}
				}

				return false;
			}
		});

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
					ContextMenuGridView gridView = (ContextMenuGridView)OnyxFocusFinder
							.findFartherestViewInDirection(this.getCurrentFocus(), reverse_direction);

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

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		this.getWindow().getDecorView().requestFocusFromTouch();

		if (mGridViewSuiteTitle != null && mGridViewSuiteTitle.getChildCount() > 0) {
			mGridViewSuiteTitle.setSelection(0);
		}
		else if (mGridViewSystemMenu != null&& mGridViewSystemMenu.getChildCount() > 0) {
			mGridViewSystemMenu.setSelection(0);
		}
	}
}
