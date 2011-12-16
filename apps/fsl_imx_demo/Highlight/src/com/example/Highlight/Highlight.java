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

package com.example.Highlight;

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
import java.util.LinkedList;

public class Highlight extends Activity {
    /** The view responsible for drawing the window. */
    SampleView mView;
    private final static String TAG = "Highlight";
    private final static LinkedList<Rect> mQueueRect = new LinkedList<Rect>();
        
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView = new SampleView(this);
		setContentView(mView);
		mView.requestFocus();
	}
	
    @Override 
    public boolean onTrackballEvent(MotionEvent event) {
    	mView.onTrackballEvent(event);
        return true;
    }
    
	private static class SampleView extends View {
        private static final int UPDATE_MODE_PARTIAL = EINK_AUTO_MODE_REGIONAL| EINK_WAIT_MODE_NOWAIT | EINK_WAVEFORM_MODE_DU | EINK_UPDATE_MODE_PARTIAL;
        private static final int UPDATE_MODE_FULL    = EINK_AUTO_MODE_REGIONAL| EINK_WAIT_MODE_WAIT | EINK_WAVEFORM_MODE_DU | EINK_UPDATE_MODE_FULL;
        
		private Paint mPaint;
		private Rect  bounds;
		private int mTranslateX = 0;
		private int mTranslateY = 30;
        private float mCurX = 0;
        private float mCurY = 0;
		private int mTextSize = 25;
        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        		
        private float startX,startY,endX,endY;
        private int   preendX,preendY,currentX,currentY;
        private int   preupdatelength,updatelength;
        
        /** Used as a pulse to gradually fade the contents of the window. */
        private static final int UPDATE_MSG = 1;  
        /** How often to fade the contents of the window (in ms). */
        private static final int UPDATE_DELAY = 20;
        boolean mUpdateFlag = false;

        
                        
		private static final String[] TEXT  = { 
          "EXCLUSIVE DAILY CONTENT"
		, "The Reader Daily Edition is the premier digital "
		, "reader for The Wall Street Journal. At the Reader"
		, "Store, subscribe to exclusive Wall Street "
		, "Journal Plus content."
		, "ONE MILLION REASONS"
		, "Together with Google Books, the Reader Store "
		, "brings you access to over 1 million eBooks."
		, "MORE THAN JUST eBOOKS"
		, "Multiple formats supported including PDF, "
		, "Microsoft Word, BBeB Book and more. Reader Touch "
		, "and Daily Editions play select audio files."
		, "BORROW FROM YOUR LIBRARY"
		, "Access your local public library to browse, check"
		, "out and download eBooks. Best of all it's free "
		, "and open 24/7." };
		
		
		
		public SampleView(Context context) {
			super(context);
           
			bounds = new Rect();
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
            mPaint.setDither(true);


			mPaint.setStrokeWidth(5);
			mPaint.setTextSize(mTextSize);


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
            
            Bitmap newBitmap = Bitmap.createBitmap(curW, curH, Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas();
            newCanvas.setBitmap(newBitmap);
            if (mBitmap != null) {
                newCanvas.drawBitmap(mBitmap, 0, 0, null);
            }
            mBitmap = newBitmap;
            mCanvas = newCanvas;
            showAllText(); 
        }
        		
		@Override
		protected void onDraw(Canvas canvas) {
		    //Log.i(TAG, ("onDraw \n"));
            canvas.drawBitmap(mBitmap, 0, 0, null);
            
		}


        private void showAllText() 
        {
            mCanvas.drawColor(Color.WHITE);
            for( int i = 0; i < TEXT.length; i++ )
			{
                drawText(i, 0, TEXT[i].length(),Color.WHITE,Color.BLACK);
			}			
        }

        private void showSelectText(int length) 
        {
            for( int i = 0; i < TEXT.length; i++ )
			{
			    if(length >= TEXT[i].length())
			    {
                    drawText(i, 0, TEXT[i].length(),Color.BLACK, Color.WHITE);
                    length = length - TEXT[i].length();
                }
                else
                {
                    if(length > 0) 
                    {   drawText(i, 0, length, Color.BLACK,Color.WHITE);
                        drawText(i, length, TEXT[i].length(), Color.WHITE,Color.BLACK);
                        length = 0;
                    }
                    else   
                    {        
                        length = 0;
                        drawText(i, length, TEXT[i].length(), Color.WHITE,Color.BLACK);
                    }
                }
                
			}
        }
        
        private synchronized void drawText(int i, int start, int end, int backcolor,int textcolor) {
                 
            Rect mTextRect   = new Rect();
            Rect mTextRect_a = new Rect();
            if(backcolor==Color.BLACK) 
            {

			     mPaint.getTextBounds(TEXT[i], 0, end, mTextRect);
			     mTextRect.offset(0, (int)((i+1)*(int)(mTranslateY)));

			     mTextRect.top = i*mTranslateY+8;
			     mTextRect.bottom = mTextRect.top+ mTextSize;
				 mTextRect.left  = 0;		    
                 mPaint.setColor(backcolor);
                 mCanvas.drawRect(mTextRect, mPaint);
                              
                 mPaint.setColor(textcolor);
                 mCanvas.drawText(TEXT[i], 0, end, mTextRect.left, (i+1)*(int)(mTranslateY), mPaint);
            
                 Log.i(TAG, ("add:left="+ mTextRect.left +" top="+ mTextRect.top +" right="+ mTextRect.right +" bottom="+ mTextRect.bottom +"")); 

            }
            else
            {
                
                if(start > 0)
                {

			         mPaint.getTextBounds(TEXT[i], 0, end, mTextRect);
			         mTextRect.offset(0, (int)((i+1)*(int)(mTranslateY)));

			         mTextRect.top = i*mTranslateY+8;
			         mTextRect.bottom = mTextRect.top+ mTextSize;
				     mTextRect.left  = 0;	
                		    
                     mPaint.setColor(backcolor);
                     mCanvas.drawRect(mTextRect, mPaint);
                              
                     mPaint.setColor(textcolor);
                     mCanvas.drawText(TEXT[i], 0, end, mTextRect.left, (i+1)*(int)(mTranslateY), mPaint);
                    
			         mPaint.getTextBounds(TEXT[i], 0, start, mTextRect_a);
			         mTextRect_a.offset(0, (int)((i+1)*(int)(mTranslateY)));

			         mTextRect_a.top = i*mTranslateY+8;
			         mTextRect_a.bottom = mTextRect_a.top+ mTextSize;
				     mTextRect_a.left  = 0;			    
                     mPaint.setColor(Color.BLACK);
                     mCanvas.drawRect(mTextRect_a, mPaint);
                              
                     mPaint.setColor(Color.WHITE);
                     mCanvas.drawText(TEXT[i], 0, start, mTextRect_a.left, (i+1)*(int)(mTranslateY), mPaint);
                                     
                }
                else    //start = 0;
                {
			        mPaint.getTextBounds(TEXT[i], 0, end, mTextRect);
			        mTextRect.offset(0, (int)((i+1)*(int)(mTranslateY)));

			        mTextRect.top = i*mTranslateY+8;
			        mTextRect.bottom = mTextRect.top+ mTextSize;
				    mTextRect.left  = 0;		    
                    mPaint.setColor(backcolor);
                    mCanvas.drawRect(mTextRect, mPaint);
                              
                    mPaint.setColor(textcolor);
                    mCanvas.drawText(TEXT[i], 0, end, mTextRect.left, (i+1)*(int)(mTranslateY), mPaint);

                }
            }
            mQueueRect.addLast(mTextRect);
        }

        public synchronized void update() {
            if (mCanvas != null) {

                Rect  AllRect=new Rect();
                if(mQueueRect.size()>0)
                {
                    AllRect = mQueueRect.removeFirst();

                    while(mQueueRect.size()>0)
                    {
                        Rect  tempRect=new Rect();

                        tempRect = mQueueRect.removeFirst();
                    
                        if(tempRect.left   < AllRect.left)      AllRect.left   = tempRect.left;
                        if(tempRect.top    < AllRect.top)       AllRect.top    = tempRect.top;
                        if(tempRect.right  > AllRect.right)     AllRect.right  = tempRect.right;
                        if(tempRect.bottom > AllRect.bottom)    AllRect.bottom = tempRect.bottom;
                        
                    }

                    invalidate(AllRect,UPDATE_MODE_PARTIAL);                
                }

/*
                while(mQueueRect.size()>0)
                {
                    Rect  tempRect=new Rect();
                    tempRect = mQueueRect.getFirst();
                    Log.i(TAG, ("move:left="+ tempRect.left +" top="+ tempRect.top +" right="+ tempRect.right +" bottom="+ tempRect.bottom +"")); 
                    invalidate(tempRect,UPDATE_MODE_PARTIAL);
                    mQueueRect.removeFirst();
                    
                }               
*/                               
            }
        }
                
        /**
         * Start up the pulse to update the screen, clearing any existing pulse to
         * ensure that we don't have multiple pulses running at a time.
         */
        void startUpdating() {
            mQueueRect.clear();
            mHandler.removeMessages(UPDATE_MSG);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(UPDATE_MSG), UPDATE_DELAY);
        }
        
        /**
         * Stop the pulse to fade the screen.
         */
        void stopUpdating() {
            mQueueRect.clear();
            mHandler.removeMessages(UPDATE_MSG);
        }
        
        private Handler mHandler = new Handler() {
            @Override public void handleMessage(Message msg) {
                switch (msg.what) {
                    // Upon receiving the update pulse, we have the view perform a
                    // update and then enqueue a new message to pulse at the desired
                    // next time.
                    case UPDATE_MSG: {
                                             
                        if(mUpdateFlag == false)
                        {
                        	stopUpdating();
                        }else
                        {
                            update(); 
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
        		return onTrackballEvent(event);
        }
        
        
        @Override 
        public boolean onTrackballEvent(MotionEvent event) {

            final float scaleX = event.getXPrecision();
            final float scaleY = event.getYPrecision();
                            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mCurX = 0;
                    mCurY = 0;

                    startX = mCurX + event.getX()*scaleX;
                	startY = mCurY + event.getY()*scaleY;

                	preendX = 0;
                	preendY = 0;
                	preupdatelength = 0;
                    

                    if(mUpdateFlag) 
                    {   
                        //showSelectText(updatelength,Color.BLACK);
                        updatelength = 0;
                        mUpdateFlag = false;
                        Log.i(TAG, ("mUpdateFlag = false "));
                        invalidate(UPDATE_MODE_FULL);
                    }
                    else           
                    {
                        showAllText();
                        mUpdateFlag = true;
                        Log.i(TAG, ("mUpdateFlag = true "));
                        invalidate(UPDATE_MODE_FULL);
                        startUpdating();
                    }
//                    Log.i(TAG, ("startX="+startX +"startY="+ startY +""));

                    break;
                case MotionEvent.ACTION_MOVE:
                	if( mUpdateFlag )
                	{
                            		 
                		endX = mCurX + event.getX()*scaleX;
                    	endY = mCurY + event.getY()*scaleY;
                    	mCurX = endX;
                    	mCurY = endY;
                    
                		//Log.i(TAG, ("endX="+endX +"endY="+ endY +""));
                    	// invalid size
                    
                		if(endX < startX || endY < startY) return true;
                    	currentX = (int)(endX - startX);
                    	currentY = (int)(endY - startY);
                    	updatelength = 0;
                		int scale =4;
                		int k = (currentY - preendY)/((int)(mTranslateY)*scale) ;
                		for( int i = 0; i<k && i < TEXT.length; i++ )
                		{
                			updatelength += TEXT[i].length();
                		}
				    
                		if(k < TEXT.length)
                		{
                			if((currentX - preendX)/((int)mTranslateY*scale) < TEXT[k].length())
                			{
                				updatelength += (currentX - preendX)/((int)mTranslateY*scale);
                			}else
                			{
                				updatelength += TEXT[k].length();
                			}
                		}
                		//Log.i(TAG, ("updatelength="+updatelength +" preupdatelength="+preupdatelength +""));
                		//showSelectText(updatelength, Color.BLACK);
                    
                		int actuallength; 
                		int i = 0;
                		int textStart;
                		if(preupdatelength < updatelength)
                		{
                			actuallength = updatelength - preupdatelength;
                			textStart = preupdatelength;
                			for( i = 0; i < TEXT.length; i++ )				        
                			{
                				if(textStart >= TEXT[i].length()) 
                				{
                					textStart = textStart - TEXT[i].length();
                				}else
                				{
                					break;
                				}
                			}

                			while(i < TEXT.length &&( textStart + actuallength > TEXT[i].length() ))
                			{
                				drawText(i, textStart, TEXT[i].length(), Color.BLACK, Color.WHITE);
                				//invalidate(mTextRect,UPDATE_MODE_PARTIAL); 
                				actuallength = actuallength - (TEXT[i].length() - 	textStart);
                				textStart = 0;	
                				i++;
                			}
                			if(i < TEXT.length) 
                			{ 
                				drawText(i, textStart, textStart + actuallength, Color.BLACK, Color.WHITE);
                				//invalidate(mTextRect,UPDATE_MODE_PARTIAL); 
                			}
                        
                		}
                    
                		if(preupdatelength > updatelength)
                		{
                			actuallength = preupdatelength - updatelength;
                			textStart = updatelength;
                			for( i = 0; i < TEXT.length; i++ )
                			{
                				if(textStart >= TEXT[i].length()) 
                				{
                					textStart = textStart - TEXT[i].length();
                				}else
                				{
                					break;
                				}
                			}
                			while(i < TEXT.length &&( textStart + actuallength > TEXT[i].length() ))
                			{
                				drawText(i, textStart, TEXT[i].length(), Color.WHITE, Color.BLACK);	
                                //invalidate(mTextRect,UPDATE_MODE_PARTIAL); 					    
                				actuallength = actuallength - (TEXT[i].length() - 	textStart);
                				textStart = 0;	
                				i++;
                			}
                        
                        
                			if(i < TEXT.length) 
                			{ 
                				drawText(i, textStart, textStart + actuallength, Color.WHITE, Color.BLACK);  
                				//invalidate(mTextRect,UPDATE_MODE_PARTIAL); 
                			}
                        
                		}                    
	
                		preupdatelength = updatelength;
                
                	}

                    break;
                case MotionEvent.ACTION_UP:
              	
                    break;
            }
            
            
            return true;
        }	
	
	
	}		
}
