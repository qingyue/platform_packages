/**
 * 
 */
package com.onyx.android.launcher.dialog;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.view.DialogBaseSettings;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * 
 * @author qingyue
 *
 */
public class DialogScreenRotation extends DialogBaseSettings
{
	Activity mHostActivity = null;
	
	private Button mButton_0 = null;
	private Button mButton_90 = null;
	private Button mButton_180 = null;
	private Button mButton_270 = null;

	public DialogScreenRotation(Activity hostActivity)
	{
		super(hostActivity);
		
		mHostActivity = hostActivity;

		this.setContentView(R.layout.dialog_screen_rotation);
		mButton_0 = (Button)findViewById(R.id.button_0);
		mButton_90 = (Button)findViewById(R.id.button_90);
		mButton_180 = (Button)findViewById(R.id.button_180);
		mButton_270 = (Button)findViewById(R.id.button_270);

		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager windowManager = getWindow().getWindowManager();
		windowManager.getDefaultDisplay().getMetrics(metrics);
		LayoutParams params = getWindow().getAttributes();

		if (metrics.widthPixels > metrics.heightPixels) {
			params.width = (int) (metrics.heightPixels * 0.5);
			params.height = (int) (metrics.heightPixels * 0.5);
		}
		else {
			params.width = (int) (metrics.widthPixels * 0.5);
			params.height = (int) (metrics.widthPixels * 0.5);
		}

		if (params.width < 230 || params.height < 230) {
			params.width = 230;
			params.height = 230;
		}

		mButton_0.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DialogScreenRotation.this.dismiss();
			}
		});

		mButton_90.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DialogScreenRotation.this.rotation_90();
			}
		});

		mButton_180.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DialogScreenRotation.this.rotation_180();
			}
		});

		mButton_270.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				DialogScreenRotation.this.rotation_270();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			DialogScreenRotation.this.rotation_180();

			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			this.dismiss();

			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			DialogScreenRotation.this.rotation_90();

			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			DialogScreenRotation.this.rotation_270();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void rotation_90()
	{
		ScreenUpdateManager.invalidate(mHostActivity.getWindow().getDecorView(), UpdateMode.GC);
		if (ScreenUpdateManager.getWindowRotation() != Surface.ROTATION_90) {
			if (ScreenUpdateManager.getWindowRotation() == Surface.ROTATION_0) {
				ScreenUpdateManager.setWindowRotation(Surface.ROTATION_90);
			}
			else if (ScreenUpdateManager.getWindowRotation() == Surface.ROTATION_180) {
				ScreenUpdateManager.setWindowRotation(Surface.ROTATION_270);
			}
			else if (ScreenUpdateManager.getWindowRotation() == Surface.ROTATION_270) {
				ScreenUpdateManager.setWindowRotation(Surface.ROTATION_0);
			}
		}
		else {
			ScreenUpdateManager.setWindowRotation(Surface.ROTATION_180);
		}

		DialogScreenRotation.this.dismiss();
	}

	private void rotation_180()
	{
		ScreenUpdateManager.invalidate(mHostActivity.getWindow().getDecorView(), UpdateMode.GC);
		if (ScreenUpdateManager.getWindowRotation() != Surface.ROTATION_180) {
			if (ScreenUpdateManager.getWindowRotation() == Surface.ROTATION_0) {
				ScreenUpdateManager.setWindowRotation(Surface.ROTATION_180);
			}
			else if (ScreenUpdateManager.getWindowRotation() == Surface.ROTATION_270) {
				ScreenUpdateManager.setWindowRotation(Surface.ROTATION_90);
			}
			else if (ScreenUpdateManager.getWindowRotation() == Surface.ROTATION_90) {
				ScreenUpdateManager.setWindowRotation(Surface.ROTATION_270);
			}
		}
		else {
			ScreenUpdateManager.setWindowRotation(Surface.ROTATION_0);
		}

		DialogScreenRotation.this.dismiss();
	}

	private void rotation_270()
	{
		ScreenUpdateManager.invalidate(mHostActivity.getWindow().getDecorView(), UpdateMode.GC);
		if (ScreenUpdateManager.getWindowRotation() != Surface.ROTATION_270) {
			if (ScreenUpdateManager.getWindowRotation() == Surface.ROTATION_0) {
				ScreenUpdateManager.setWindowRotation(Surface.ROTATION_270);
			}
			else if (ScreenUpdateManager.getWindowRotation() == Surface.ROTATION_90) {
				ScreenUpdateManager.setWindowRotation(Surface.ROTATION_0);
			}
			else if (ScreenUpdateManager.getWindowRotation() == Surface.ROTATION_180) {
				ScreenUpdateManager.setWindowRotation(Surface.ROTATION_90);
			}
		}
		else {
			ScreenUpdateManager.setWindowRotation(Surface.ROTATION_180);
		}

		DialogScreenRotation.this.dismiss();
	}
}
