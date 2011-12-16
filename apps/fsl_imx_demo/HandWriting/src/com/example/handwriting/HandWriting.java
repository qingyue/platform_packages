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

/* Copyright (c) 2011 Freescale Semiconductor, Inc. */

package com.example.handwriting;


import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;

public class HandWriting extends Activity {


    private static final int CLEAR_MENU_ID 	   = Menu.FIRST;
    private static final int SLOW_MENU_ID      = Menu.FIRST + 1;
    private static final int FAST_MENU_ID      = Menu.FIRST + 2;
    
    private final static String TAG = "HandWriting";
    
    private Paint       mPaint;
    /** The view responsible for drawing the window. */
    MyView mView;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create and attach the view that is responsible for painting.
        mView = new MyView(this);
        setContentView(mView);
        mView.requestFocus();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, CLEAR_MENU_ID, 0, "Clear").setShortcut('3', 'c');
        menu.add(0, SLOW_MENU_ID, 0, "Slow").setShortcut('4', 's');
        menu.add(0, FAST_MENU_ID, 0, "Fast").setShortcut('5', 'f');
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case CLEAR_MENU_ID:
            	mView.clear();
                return true;
            case FAST_MENU_ID:
            	mView.setUpdateType(0);
                return true;
            case SLOW_MENU_ID:
            	mView.setUpdateType(1);
                return true;                
        }
        return super.onOptionsItemSelected(item);
    }    
    
    @Override 
    public boolean onTrackballEvent(MotionEvent event) {
    	mView.onTrackballEvent(event);
        return true;
    }
    
    
    public class MyView extends View {
        
        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        private Path    mPath;
        private int 	mLastX, mLastY;
        private final Rect mRect = new Rect();
        private final Rect mlastRect = new Rect();
        boolean mUpdateFlag = false;
        boolean mStartFlag  = false;
        boolean mFirstPointFlag = true;
        boolean mNewRegionFlag  = true;
        
        private int mCurX;
        private int mCurY;
        private float mCurSize;
        private int mCurWidth;
        
        private int mWidth;
        private int mHight;
                
        private int mEvent = 0; //0 touch event; 1 trackball 
        private int mUpdateType = 0; //0 fast type; 1 slow type; 
        
        /** Used as a pulse to gradually fade the contents of the window. */
        private static final int UPDATE_MSG = 1;  
        /** How often to fade the contents of the window (in ms). */
        private static final int UPDATE_DELAY = 20;
        /*update mode for handwriting in eink*/
        private static final int UPDATE_MODE_PARTIAL = EINK_AUTO_MODE_REGIONAL| EINK_WAIT_MODE_NOWAIT | EINK_WAVEFORM_MODE_DU | EINK_UPDATE_MODE_PARTIAL;
        private static final int UPDATE_MODE_FULL    = EINK_AUTO_MODE_REGIONAL| EINK_WAIT_MODE_WAIT |  EINK_WAVEFORM_MODE_AUTO  |  EINK_UPDATE_MODE_PARTIAL;
        
        public MyView(Context c) {
            super(c);
            
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(0xFF000000);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(3); 
            
            mPath 	= new Path();
            mRect.left   = 0;
            mRect.top    = 0;
            mRect.right  = 0;
            mRect.bottom = 0; 
            
            mlastRect.left   = 0;
            mlastRect.top    = 0;
            mlastRect.right  = 0;
            mlastRect.bottom = 0;    
            mFirstPointFlag = true;      
            mHight = 0;
            mWidth = 0;   
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            int curW = mBitmap != null ? mBitmap.getWidth() : 0;
            int curH = mBitmap != null ? mBitmap.getHeight() : 0;
            if (curW >= w && curH >= h) {
                return;
            }
            
            if (curW < w) curW = w;
            if (curH < h) curH = h;
            
            mHight = curH;
            mWidth = curW;   
            
            Bitmap newBitmap = Bitmap.createBitmap(curW, curH, Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas();
            newCanvas.setBitmap(newBitmap);
            if (mBitmap != null) {
                newCanvas.drawBitmap(mBitmap, 0, 0, null);
            }
            mBitmap = newBitmap;
            mCanvas = newCanvas;
            mCanvas.drawColor(0xFFFFFFFF);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            
            canvas.drawBitmap(mBitmap, 0, 0, null);
            //canvas.drawPath(mPath, mPaint);
        }

        public void clear() {
            if (mCanvas != null) {
//                Log.i(TAG, ("clear() start\n")); 
                mPath.reset();
            	mBitmap.eraseColor(0xFFFFFFFF);
                invalidate(UPDATE_MODE_FULL );
                mUpdateFlag = false;
                mRect.left   = 0;
                mRect.top    = 0;
                mRect.right  = 0;
                mRect.bottom = 0; 
                
                mlastRect.left   = 0;
                mlastRect.top    = 0;
                mlastRect.right  = 0;
                mlastRect.bottom = 0;  
                
                mEvent =0;
                mFirstPointFlag = true;    
                mNewRegionFlag  = true;  
            }
        }
        
        public void setUpdateType(int type)
        {
            
            switch (type) {
                case 0:
                	mUpdateType = 0;
                    break;
                case 1:
                    mUpdateType = 1;
                    break;
                default:
                    mUpdateType = 0;
                    break;
            }          
        }
        
        public synchronized void update() {
            if (mCanvas != null) {

            	int left   = mRect.left ;
               	int top    = mRect.top ;
               	int right  = mRect.right ;
               	int bottom = mRect.bottom ;
                
                if(left <0)   left =0;
                if(top <0)    top =0;
                if(right <0)  right =0;
                if(bottom <0) bottom =0;
                           	
                           	
               	if(mNewRegionFlag  == true)
               	{
               	    
               		if( mEvent == 1 ) 
                    {
                        Log.i(TAG, ("end  update 1\n"));         
                        mUpdateFlag = false;
                    }

               	}else
               	{
                    Log.i(TAG, ("left=" + left +" top="+ top +" right="+ right+ " bottom="+ bottom +""));            	
                    mCanvas.drawPath(mPath, mPaint);
                    if(mUpdateType == 0)
               		   invalidate(left, top, right, bottom, UPDATE_MODE_PARTIAL);
                    else
               		   invalidate(left, top, right, bottom );
                    mNewRegionFlag  = true;              
               	}
                
            }
        }

        /**
         * Start up the pulse to update the screen, clearing any existing pulse to
         * ensure that we don't have multiple pulses running at a time.
         */
        void startUpdating() {
            mStartFlag  = true;
            mUpdateFlag = true;
            mHandler.removeMessages(UPDATE_MSG);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(UPDATE_MSG), UPDATE_DELAY);
        }
        
        /**
         * Stop the pulse to fade the screen.
         */
        void stopUpdating() {
            mHandler.removeMessages(UPDATE_MSG);
        }
        
        private Handler mHandler = new Handler() {
            @Override public void handleMessage(Message msg) {
                switch (msg.what) {
                    // Upon receiving the update pulse, we have the view perform a
                    // update and then enqueue a new message to pulse at the desired
                    // next time.
                    case UPDATE_MSG: {
                        update();                        
                        if(mUpdateFlag == false)
                        {
                            Log.i(TAG, ("end  update 2\n"));         
                        	stopUpdating();
                        }else
                        {
                        	mHandler.sendMessageDelayed(mHandler.obtainMessage(UPDATE_MSG), UPDATE_DELAY);
                        }
                        break;
                    }
                    default:
                        super.handleMessage(msg);
                }
            }
        };    
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {

            mEvent = 0;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                	startUpdating();
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                	mUpdateFlag = false;
                    break;
            }
      	
            int N = event.getHistorySize();
            for (int i=0; i<N; i++) {
                drawPoint(event.getHistoricalX(i),
                        event.getHistoricalY(i),
                        event.getHistoricalPressure(i),
                        event.getHistoricalSize(i));
            }
            drawPoint(event.getX(), event.getY(),
                    event.getPressure(), event.getSize());
            return true;
        }
        

        @Override 
        public boolean onTrackballEvent(MotionEvent event) {
            
            mEvent = 1;
        	if(mUpdateFlag == false)
        	{
        	    Log.i(TAG, ("start update \n"));            	
        		startUpdating();
        	}
        	
            int N = event.getHistorySize();
            int baseX = mCurX;
            int baseY = mCurY;
            final float scaleX = event.getXPrecision();
            final float scaleY = event.getYPrecision();
            for (int i=0; i<N; i++) {
                drawPoint(baseX+event.getHistoricalX(i)*scaleX,
                        baseY+event.getHistoricalY(i)*scaleY,
                        event.getHistoricalPressure(i),
                        event.getHistoricalSize(i));
            }
            drawPoint(baseX+event.getX()*scaleX, baseY+event.getY()*scaleY,
                    event.getPressure(), event.getSize());
            return true;
        }
        
        private synchronized  void drawPoint(float x, float y, float pressure, float size) {
//        	Log.i(TAG, ("drawPoint() start \n"));      
        	int oldleft   = mRect.left ;
           	int oldtop    = mRect.top ;
           	int oldright  = mRect.right ;
           	int oldbottom = mRect.bottom ;

        	int newleft   = 0 ;
           	int newtop    = 0 ;
           	int newright  = 0 ;
           	int newbottom = 0 ;
           	
        	int targetleft   = 0 ;
           	int targettop    = 0 ;
           	int targetright  = 0 ;
           	int targetbottom = 0 ;
           	
           	
        	mCurX = (int)x;
            mCurY = (int)y;
            
            if(mCurY > mHight) mCurY = mHight;
            if(mCurY < 0     ) mCurY = 0;
            if(mCurX > mWidth) mCurX = mWidth;
            if(mCurX < 0     ) mCurX = 0;
            
            
            mCurSize = size;
            mCurWidth = (int)(mCurSize*(getWidth()/3));
            if (mCurWidth < 1) 	mCurWidth = 1;
            if ( mCanvas != null) {

        
            	newleft   =  mCurX-mCurWidth-2;
            	newtop    =  mCurY-mCurWidth-2;
            	newright  =  mCurX+mCurWidth+2;
            	newbottom =  mCurY+mCurWidth+2;


                if(mFirstPointFlag == true)
                {
                        mFirstPointFlag=false;
            		    oldleft    = newleft;
            		    oldtop     = newtop ;
            		    oldright   = newright;
            		    oldbottom  = newbottom;
                        mPath.reset();
                        mPath.moveTo(mCurX, mCurY);

                        mNewRegionFlag = false;                       
                }else
                {
                
            	   if(mNewRegionFlag==true)
            	   {
            	       if( mStartFlag== true && mEvent== 0)
            	       {
                          mStartFlag = false;      	   
            		      oldleft    = newleft;
            		      oldtop     = newtop ;
            		      oldright   = newright;
            		      oldbottom  = newbottom;
                          mPath.reset();
                          mPath.moveTo(mCurX, mCurY);      
                       }
                       else
                       {
            		      oldleft    = mlastRect.left;
            		      oldtop     = mlastRect.top ;
            		      oldright   = mlastRect.right;
            		      oldbottom  = mlastRect.bottom;
                       }
                       mNewRegionFlag = false; 
            	   }
            	   
            	}
                            	
                mlastRect.left   = newleft;
                mlastRect.top    = newtop;
                mlastRect.right  = newright;
                mlastRect.bottom = newbottom;  
            	
            	{
            	    mPath.lineTo(mCurX, mCurY);
            		if(oldleft < newleft) 	 	targetleft = oldleft;
            		else				  	 	targetleft = newleft;
               	
            		if(oldtop < newtop)   	 	targettop  = oldtop;
            		else				  	 	targettop  = newtop;
               		            	
            		if(oldright < newright)  	targetright = newright;
            		else				  	 	targetright = oldright;
               	
            		if(oldbottom < newbottom)   targetbottom = newbottom;
            		else				  		targetbottom = oldbottom;  
            	}
               	
            	mRect.set(targetleft, targettop, targetright, targetbottom);
            }
//            Log.i(TAG, ("drawPoint() end \n"));     
        }       
    }

}
