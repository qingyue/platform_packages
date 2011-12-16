/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Copyright (c) 2010 Freescale Semiconductor Inc. */

package com.example.fastpageturn;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.util.Log;


public class fastpageturn extends Activity {
	/** Called when the activity is first created. */

	MyView mView;

    private final static String TAG = "fastpageturn";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView = new MyView(this);
		setContentView(mView);
		mView.requestFocus();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (mView.onKeyUp(keyCode, event) == false)
			return super.onKeyUp(keyCode, event);
		else
			return true;
	}


	@Override
	public void onPause(){
	   Log.i(TAG,"onPause");
	   super.onPause();
	}

	@Override
	public void onResume(){
	   Log.i(TAG,"onResume");
	   super.onResume();
	}

	public class MyView extends View {

		private Bitmap mBitmap;
		private final Rect mRect = new Rect();
		private final Rect mlastRect = new Rect();
		private final Bitmap TurnPages[] = new Bitmap[7];
		boolean mUpdateFlag = false;
		boolean mStartFlag = false;

		boolean mFirstPointFlag = true;
		boolean mNewRegionFlag = true;

		boolean mBitmapLoaded = false;

		private int mTurn = TURN_STOP;
		private TimerTask mTimerTask = null;
		private Timer mTimer = null;

		private int mPageIndex = 0;

		/* update mode for handwriting in eink */
		private static final int UPDATE_MODE_PARTIAL = EINK_AUTO_MODE_REGIONAL| EINK_WAIT_MODE_NOWAIT | EINK_WAVEFORM_MODE_DU | EINK_UPDATE_MODE_PARTIAL;;
		private static final int UPDATE_MODE_FULL =EINK_AUTO_MODE_REGIONAL| EINK_WAIT_MODE_WAIT | EINK_WAVEFORM_MODE_DU | EINK_UPDATE_MODE_FULL;

		private static final int TURN_STOP = 0;
		private static final int TURN_FORWARD = 1;
		private static final int TURN_BACKWARD = 2;

		void changeText() {
			if (mTurn == TURN_STOP)
				return;

			if (mTurn == TURN_FORWARD) {
				postInvalidate(UPDATE_MODE_PARTIAL);
				if (mPageIndex < 6)
					mPageIndex++;
				else
					mTurn = TURN_STOP;
			}

			if (mTurn == TURN_BACKWARD) {
				postInvalidate(UPDATE_MODE_PARTIAL);
				if (mPageIndex > 0)
					mPageIndex--;
				else
					mTurn = TURN_STOP;
			}
		}

		public MyView(Context c) {
			super(c);

			mRect.left = 0;
			mRect.top = 0;
			mRect.right = 0;
			mRect.bottom = 0;

			mlastRect.left = 0;
			mlastRect.top = 0;
			mlastRect.right = 0;
			mlastRect.bottom = 0;

			mFirstPointFlag = true;

			Resources r = getResources();
			TurnPages[0] = loadBitmap(r.getDrawable(R.drawable.ebook01));
			TurnPages[1] = loadBitmap(r.getDrawable(R.drawable.ebook02));
			TurnPages[2] = loadBitmap(r.getDrawable(R.drawable.ebook03));
			TurnPages[3] = loadBitmap(r.getDrawable(R.drawable.ebook04));
			TurnPages[4] = loadBitmap(r.getDrawable(R.drawable.ebook05));
			TurnPages[5] = loadBitmap(r.getDrawable(R.drawable.ebook06));
			TurnPages[6] = loadBitmap(r.getDrawable(R.drawable.ebook07));

			mBitmapLoaded = true;

			mTimer = new Timer();
			mTimerTask = new TimerTask() {
				public void run() {
					changeText();
				}
			};
			mTimer.schedule(mTimerTask, 0, 350);

		}

		public Bitmap loadBitmap(Drawable sprite, Bitmap.Config bitmapConfig) {
			int width = sprite.getIntrinsicWidth();
			int height = sprite.getIntrinsicHeight();
			Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
			Canvas canvas = new Canvas(bitmap);
			sprite.setBounds(0, 0, width, height);
			sprite.draw(canvas);
			return bitmap;
		}

		public Bitmap loadBitmap(Drawable sprite) {
			return loadBitmap(sprite, Bitmap.Config.RGB_565);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			int curW = mBitmap != null ? mBitmap.getWidth() : 0;
			int curH = mBitmap != null ? mBitmap.getHeight() : 0;
			if (curW >= w && curH >= h) {
				return;
			}

			if (curW < w)
				curW = w;
			if (curH < h)
				curH = h;

			Bitmap newBitmap = Bitmap.createBitmap(800, 600,
					Bitmap.Config.RGB_565);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			if (mBitmap != null) {
				newCanvas.drawBitmap(mBitmap, 0, 0, null);
			}
			mBitmap = newBitmap;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (mBitmapLoaded) {
				canvas.drawColor(Color.WHITE);
				canvas.drawBitmap(TurnPages[mPageIndex], 0, 0, null);
			}
		}

		public boolean onKeyUp(int keyCode, KeyEvent event) {
            boolean ret=false;

			switch (keyCode) {
			case KeyEvent.KEYCODE_S: // Stop Forward
			     Log.i(TAG, ("KEYCODE_S \n"));
				mTurn = TURN_STOP;
				ret = true;
				break;
			case KeyEvent.KEYCODE_F: // Start FF
				mTurn = TURN_FORWARD;
				Log.i(TAG, ("KEYCODE_F \n"));
				ret = true;
				break;
			case KeyEvent.KEYCODE_R: // Start REW
			    Log.i(TAG, ("KEYCODE_R \n"));
				mTurn = TURN_BACKWARD;
				ret = true;
				break;
			case KeyEvent.KEYCODE_ENDCALL: // Start REW
			    Log.i(TAG, ("KEYCODE_ENDCALL \n"));
				ret = true;
				break;

			case KeyEvent.KEYCODE_DPAD_LEFT: // Start REW
			    if (mPageIndex > 0)
					mPageIndex--;
				else
                    mPageIndex =6;
                Log.i(TAG, ("KEYCODE_DPAD_LEFT "+mPageIndex+""));
                invalidate(UPDATE_MODE_PARTIAL);
				ret = true;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT: // Start REW
			    if (mPageIndex < 6)
					mPageIndex++;
				else
                    mPageIndex =0;
                Log.i(TAG, ("KEYCODE_DPAD_RIGHT "+mPageIndex+""));
                invalidate(UPDATE_MODE_PARTIAL);
				ret = true;
                break;
			}

			return ret;
		}
	}

}
