/**
 * 
 */
package com.onyx.android.launcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.onyx.android.sdk.ui.OnyxGridView;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * @author qingyue
 *
 */
public class ContextMenuGridView extends OnyxGridView {

	public ContextMenuGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ContextMenuGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ContextMenuGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int selected_item = this.getSelectedItemPosition();
		int column_count = this.getPagedAdapter().getPageLayout().getLayoutColumnCount();

		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			if ((selected_item - column_count) >= 0) {
				for (int i = (selected_item - column_count); i >= 0; i -= column_count) {
					if (this.getChildAt(i).getTag() != null) {
						ScreenUpdateManager.invalidate(this, UpdateMode.DW);
						this.setSelection(i);
						return true;
					}
				}
				for (int i = (selected_item - column_count); 
						i >= (((selected_item - column_count) / column_count) * column_count); 
						i--) {
					if (this.getChildAt(i).getTag() != null) {
						ScreenUpdateManager.invalidate(this, UpdateMode.DW);
						this.setSelection(i);
						return true;
					}
				}
				assert(false);
			}
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			if ((selected_item + column_count) < this.getCount()) {
				System.out.println("KEYCODE_DPAD_DOWN");
				for (int i = (selected_item + column_count); i < this.getCount(); i += column_count) {
					if (this.getChildAt(i).getTag() != null) {
						ScreenUpdateManager.invalidate(this, UpdateMode.DW);
						this.setSelection(i);
						return true;
					}
				}
				for (int i = (selected_item + column_count); 
						i >= (((selected_item + column_count) / column_count) * column_count); 
						i--) {
					if (this.getChildAt(i).getTag() != null) {
						ScreenUpdateManager.invalidate(this, UpdateMode.DW);
						this.setSelection(i);
						return true;
					}
				}
				assert(false);
			}
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if ((selected_item - 1) >= 0) {
				if (column_count == 1) {
					return false;
				}
				if ((selected_item + 1) % column_count == 1) {
					return false;
				}
				if (this.getChildAt(selected_item - 1).getTag() != null) {
					ScreenUpdateManager.invalidate(this, UpdateMode.DW);
					this.setSelection(selected_item - 1);
					return true;
				}
			}
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if ((selected_item + 1) < this.getCount()) {
				if ((selected_item + 1) % column_count == 0) {
					return false;
				}
				if (this.getChildAt(selected_item + 1).getTag() != null) {
					ScreenUpdateManager.invalidate(this, UpdateMode.DW);
					this.setSelection(selected_item + 1);
					return true;
				}
				else {
					int position = ((selected_item + 1) / column_count) > 0?((((selected_item + 1) / column_count) + 1) * column_count):((((selected_item + 1) / column_count)) * column_count);
					this.setSelection(position);
					return false;
				}
			}
		}

		return super.onKeyDown(keyCode, event);
	}
}