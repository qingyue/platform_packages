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

package com.example.CameraVideoPlay;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentValues;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.graphics.drawable.Drawable;
import android.content.Intent;
import android.content.IntentFilter;

import android.provider.MediaStore;
import android.provider.MediaStore.Video;

import android.net.Uri;

import android.os.Bundle;
//import android.os.SystemProperties;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Environment;
import android.os.StatFs;

import android.view.View;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;

import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.AudioManager;

import android.text.format.DateFormat;


import android.widget.Button;

import android.util.Log;




import java.io.IOException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FilenameFilter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;
// ----------------------------------------------------------------------

public class CameraVideoPlay extends Activity implements OnClickListener,
        ShutterButton.OnShutterButtonListener{

    private static final String TAG = "CameraVideoPlay";

    private static final int INIT_RECORDER = 3;
    private static final int CLEAR_SCREEN_DELAY = 4;
    private static final int UPDATE_RECORD_TIME = 5;

    
    private static final int STORAGE_STATUS_OK = 0;
    
    private static final long LOW_STORAGE_THRESHOLD = 512L * 1024L;
    private static final long NO_STORAGE_ERROR = -1L;
    private static final long CANNOT_STAT_ERROR = -2L;    
    
    private android.hardware.Camera mCameraDevice;    
            
    private ShutterButton mShutterButton;
    private SurfaceView mVideoPreview;
    private SurfaceHolder mSurfaceHolder_Camera = null;

    private SurfaceView mVideoView;
    private SurfaceHolder mSurfaceHolder_playback = null;
    private MediaPlayer mMediaPlayer = null;    

	private boolean mVideoStoped = false;	
	private String VideoFile = "";
	private Uri			 mUri;
	private String[]     FileNameInDir = null;
	private List<String> VideoFileList = null;	
    
        
        
    private Parameters mParameters;
    private MediaRecorderProfile mProfile;
    private MediaRecorder mMediaRecorder;
    private final Handler mHandler = new MainHandler();        
    private FileDescriptor mCameraVideoFileDescriptor;
    private Uri mCurrentVideoUri;
               
    boolean mPreviewing = false; // True if preview is started.
    private boolean mStartPreviewFail = false;
    boolean mPausing = false;
    private boolean mMediaRecorderRecording = false;

       
	private boolean mVideoErr = false;

    // The video duration limit. 0 menas no limit.
    private int mMaxVideoDurationInMs;
    private int mStorageStatus = STORAGE_STATUS_OK;
    private String mCameraVideoFilename;
    private long mRecordingStartTime;
    private String mCurrentVideoFilename;
    
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.i(TAG,"onCreate ");

        readVideoPreferences() ;
        /*
         * To reduce startup time, we start the preview in another thread.
         * We make sure the preview is started at the end of onCreate.
         */
         
        Thread startPreviewThread = new Thread(new Runnable() {
            public void run() {
                try {
                    mStartPreviewFail = false;
                    startPreview();
                } catch (CameraHardwareException e) {
                    // In eng build, we throw the exception so that test tool
                    // can detect it and report it
                    if ("eng".equals(Build.TYPE)) {
                        throw new RuntimeException(e);
                    }
                    mStartPreviewFail = true;
                }
            }
        });
        startPreviewThread.start();
                
        mVideoPreview = (SurfaceView) findViewById(R.id.camera_preview);

        // don't set mSurfaceHolder here. We have it set ONLY within
        // surfaceCreated / surfaceDestroyed, other parts of the code
        // assume that when it is set, the surface is also set.
        SurfaceHolder holder = mVideoPreview.getHolder();
        holder.addCallback(mCamera_Callback);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        
        
        ViewGroup rootView = (ViewGroup) findViewById(R.id.Camera_videoplay);
        LayoutInflater inflater = this.getLayoutInflater();
        View controlBar_camera   = inflater.inflate(R.layout.camera_control, rootView);
        View controlBar_playback = inflater.inflate(R.layout.playback_control, rootView);
        
        
        
        mShutterButton = (ShutterButton) findViewById(R.id.shutter_button);
        mShutterButton.setImageResource(R.drawable.btn_ic_video_record);
        mShutterButton.setOnShutterButtonListener(this);
        mShutterButton.requestFocus();
        
        // Make sure preview is started.
        try {
            startPreviewThread.join();
            if (mStartPreviewFail) {
                Log.i(TAG,"mStartPreviewFail=ture");
                return;
            }
        } catch (InterruptedException ex) {
            // ignore
        }
        
        /****************************************************************************/
        Log.i(TAG,"playback ");
        
        Button button = (Button)findViewById(R.id.playback_button);
        button.setOnClickListener(this);   
        
  		File fileDir;
  		int fileIndex = 0;
   		fileDir = new File("/sdcard");
        FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                	Log.i(TAG,"FilenameFilter:"+name);
                    return name.endsWith(".mp4")||name.endsWith(".m4v")||name.endsWith(".avi");
                }
        };  
        FileNameInDir = fileDir.list(filter);    
       
        VideoFileList = new ArrayList<String>();
        for(fileIndex = 0;fileIndex < FileNameInDir.length;fileIndex++){
        	Log.i(TAG,"fileIndex: "+fileIndex+" filename:"+FileNameInDir[fileIndex]);
        	VideoFileList.add("/sdcard" + "/" + FileNameInDir[fileIndex]);
        }        
        
 
        VideoFile = "/sdcard" + "/" + FileNameInDir[0];
        if((VideoFile == null)&&(VideoFile == "")){
        	mVideoErr = true;
        	return;
        }
        
        
        Log.i(TAG,"playback success ");
        mVideoView = (SurfaceView) findViewById(R.id.playback_view);
        
        SurfaceHolder playback_holder = mVideoView.getHolder();
        playback_holder.addCallback(mPlayback_Callback);
        playback_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        mUri = Uri.fromFile(new File(VideoFile));                
    }
    
    /*********************************  camera ***************************/
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case INIT_RECORDER: {
                    initializeRecorder();
                    break;
                }

                default:
                    Log.v(TAG, "Unhandled message: " + msg.what);
                    break;
            }
        }
    }

    public void onShutterButtonFocus(ShutterButton button, boolean pressed) {
        // Do nothing (everything happens in onShutterButtonClick).
    }

    public void onShutterButtonClick(ShutterButton button) {
        switch (button.getId()) {
            case R.id.shutter_button:
                if (mMediaRecorderRecording) {
                    stopVideoRecording();
                    initializeRecorder();
                } else if (mMediaRecorder != null) {
                    // If the click comes before recorder initialization, it is
                    // ignored. If users click the button during initialization,
                    // the event is put in the queue and record will be started
                    // eventually.
                    startVideoRecording();
                }            
                break;
        }
    }

    SurfaceHolder.Callback mCamera_Callback = new SurfaceHolder.Callback()
    {
    
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // Make sure we have a surface in the holder before proceeding.
            if (holder.getSurface() == null) {
                Log.d(TAG, "holder.getSurface() == null");
                return;
            }
            if (mPausing) {
                // We're pausing, the screen is off and we already stopped
                // video recording. We don't want to start the camera again
                // in this case in order to conserve power.
                // The fact that surfaceChanged is called _after_ an onPause appears
                // to be legitimate since in that case the lockscreen always returns
                // to portrait orientation possibly triggering the notification.
                return;
            }
            // The mCameraDevice will be null if it is fail to connect to the
            // camera hardware. In this case we will show a dialog and then
            // finish the activity, so it's OK to ignore it.
            if (mCameraDevice == null) return;

            // Set preview display if the surface is being created. Preview was
            // already started.
            if (holder.isCreating()) {
                setPreviewDisplay(holder);
                mCameraDevice.unlock();
                mHandler.sendEmptyMessage(INIT_RECORDER);
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder_Camera = holder;
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.v(TAG, "surfaceDestroyed");
            mSurfaceHolder_Camera = null;
        }
    };


    
    
    private void readVideoPreferences() {
        boolean videoQualityHigh =true;
        int minutes = 10;

        // 1 minute = 60000ms
        mMaxVideoDurationInMs = 60000 * minutes;

        mProfile = new MediaRecorderProfile(videoQualityHigh);
    }
    
    private void setPreviewDisplay(SurfaceHolder holder) {
        try {
            mCameraDevice.setPreviewDisplay(holder);
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("setPreviewDisplay failed", ex);
        }
    }
    
    private void closeCamera() {
        Log.v(TAG, "closeCamera");
        if (mCameraDevice == null) {
            Log.d(TAG, "already stopped.");
            return;
        }
        // If we don't lock the camera, release() will fail.
        mCameraDevice.lock();
        CameraHolder.instance().release();
        mCameraDevice = null;
        mPreviewing = false;
    }
    
    private void setCameraParameters() {
        mParameters = mCameraDevice.getParameters();

        mParameters.setPreviewSize(mProfile.mVideoWidth, mProfile.mVideoHeight);
        mParameters.setPreviewFrameRate(mProfile.mVideoFps);
        mCameraDevice.setParameters(mParameters);

    }

        
    private void initializeRecorder() {
        Log.v(TAG, "initializeRecorder");
        if (mMediaRecorder != null) return;

        // We will call initializeRecorder() again when the alert is hidden.
        // If the mCameraDevice is null, then this activity is going to finish
        if ( mCameraDevice == null) return;


        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(mCameraDevice);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(mProfile.mOutputFormat);
        mMediaRecorder.setMaxDuration(mMaxVideoDurationInMs);

        // Set output file.
        if (mStorageStatus != STORAGE_STATUS_OK) {
            mMediaRecorder.setOutputFile("/dev/null");
        } else {
            // Try Uri in the intent first. If it doesn't exist, use our own
            // instead.
            if (mCameraVideoFileDescriptor != null) {
                mMediaRecorder.setOutputFile(mCameraVideoFileDescriptor);
            } else {
                createVideoPath();
                mMediaRecorder.setOutputFile(mCameraVideoFilename);
            }
        }

        // Use the same frame rate for both, since internally
        // if the frame rate is too large, it can cause camera to become
        // unstable. We need to fix the MediaRecorder to disable the support
        // of setting frame rate for now.
        mMediaRecorder.setVideoFrameRate(mProfile.mVideoFps);
        mMediaRecorder.setVideoSize(
                mProfile.mVideoWidth, mProfile.mVideoHeight);
        mMediaRecorder.setVideoEncodingBitRate(mProfile.mVideoBitrate);
        //mMediaRecorder.setParameters(String.format(
        //        "video-param-encoding-bitrate=%d", mProfile.mVideoBitrate));
        mMediaRecorder.setAudioEncodingBitRate(mProfile.mAudioBitrate);
        //mMediaRecorder.setParameters(String.format(
        //        "audio-param-encoding-bitrate=%d", mProfile.mAudioBitrate));
        mMediaRecorder.setAudioChannels(mProfile.mAudioChannels);
        //mMediaRecorder.setParameters(String.format(
        //        "audio-param-number-of-channels=%d", mProfile.mAudioChannels));
        mMediaRecorder.setAudioSamplingRate(mProfile.mAudioSamplingRate);
        //mMediaRecorder.setParameters(String.format(
        //        "audio-param-sampling-rate=%d", mProfile.mAudioSamplingRate));
        mMediaRecorder.setVideoEncoder(mProfile.mVideoEncoder);
        mMediaRecorder.setAudioEncoder(mProfile.mAudioEncoder);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder_Camera.getSurface());

        // Set maximum file size.
        // remaining >= LOW_STORAGE_THRESHOLD at this point, reserve a quarter
        // of that to make it more likely that recording can complete
        // successfully.
        long maxFileSize = getAvailableStorage() - LOW_STORAGE_THRESHOLD / 4;

        Log.i(TAG,"maxFileSize" + maxFileSize);

        try {
            mMediaRecorder.setMaxFileSize(maxFileSize);
        } catch (RuntimeException exception) {
            // We are going to ignore failure of setMaxFileSize here, as
            // a) The composer selected may simply not support it, or
            // b) The underlying media framework may not handle 64-bit range
            // on the size restriction.
        }

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare failed for " + mCameraVideoFilename);
            releaseMediaRecorder();
            throw new RuntimeException(e);
        }
        mMediaRecorderRecording = false;
    }    
    
    private void startVideoRecording() {
        Log.v(TAG, "startVideoRecording");
        if (!mMediaRecorderRecording) {

            if (mStorageStatus != STORAGE_STATUS_OK) {
                Log.v(TAG, "Storage issue, ignore the start request");
                return;
            }

            // Check mMediaRecorder to see whether it is initialized or not.
            if (mMediaRecorder == null) {
                Log.e(TAG, "MediaRecorder is not initialized.");
                return;
            }

            try {
                mMediaRecorder.start(); // Recording is now started
            } catch (RuntimeException e) {
                Log.e(TAG, "Could not start media recorder. ", e);
                return;
            }
            mMediaRecorderRecording = true;
            mRecordingStartTime = SystemClock.uptimeMillis();
            updateRecordingIndicator(false);
//            mRecordingTimeView.setText("");
//            mRecordingTimeView.setVisibility(View.VISIBLE);
//            updateRecordingTime();
//            keepScreenOn();
//            mGripper.setVisibility(View.INVISIBLE);
        }
    }
    
    private void stopVideoRecording() {
        Log.v(TAG, "stopVideoRecording");
        boolean needToRegisterRecording = false;
        if (mMediaRecorderRecording || mMediaRecorder != null) {
            if (mMediaRecorderRecording && mMediaRecorder != null) {
                try {
                    mMediaRecorder.setOnErrorListener(null);
                    mMediaRecorder.setOnInfoListener(null);
                    mMediaRecorder.stop();
                } catch (RuntimeException e) {
                    Log.e(TAG, "stop fail: " + e.getMessage());
                }

                mCurrentVideoFilename = mCameraVideoFilename;
                Log.v(TAG, "Setting current video filename: "
                        + mCurrentVideoFilename);
                needToRegisterRecording = true;
                mMediaRecorderRecording = false;
            }
            releaseMediaRecorder();
            updateRecordingIndicator(true);
//            mRecordingTimeView.setVisibility(View.GONE);
//            keepScreenOnAwhile();
//            mGripper.setVisibility(View.VISIBLE);
        }
//        if (needToRegisterRecording && mStorageStatus == STORAGE_STATUS_OK) {
//            registerVideo();
//        }

        mCameraVideoFilename = null;
        mCameraVideoFileDescriptor = null;
    }
    
    private void createVideoPath() {
        long dateTaken = System.currentTimeMillis();
        String title = createName(dateTaken);
        String suffix = ".3gp";
        String mimestring = "video/3gpp";
        if(mProfile.mOutputFormat == MediaRecorder.OutputFormat.MPEG_4) {
            suffix = ".mp4";
            mimestring = "video/mp4";
        }

        String displayName = title + suffix; // Used when emailing.
        String cameraDirPath = Environment.getExternalStorageDirectory().toString()+"/dcim/camera";
        File cameraDir = new File(cameraDirPath);
        cameraDir.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                getString(R.string.video_file_name_format));
        Date date = new Date(dateTaken);
        String filepart = dateFormat.format(date);
        String filename = cameraDirPath + "/" + filepart + suffix;
        ContentValues values = new ContentValues(7);
        values.put(Video.Media.TITLE, title);
        values.put(Video.Media.DISPLAY_NAME, displayName);
        values.put(Video.Media.DATE_TAKEN, dateTaken);
        values.put(Video.Media.MIME_TYPE, mimestring);
        values.put(Video.Media.DATA, filename);
        mCameraVideoFilename = filename;
        Log.v(TAG, "Current camera video filename: " + mCameraVideoFilename);
//        mCurrentVideoValues = values;
    }
    
    private static String createName(long dateTaken) {
        return DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken).toString();
    }
    
    /**
     * Returns
     *
     * @return number of bytes available, or an ERROR code.
     */
    private static long getAvailableStorage() {
        try {
            if (hasStorage()) {
                return NO_STORAGE_ERROR;
            } else {
                String storageDirectory =
                        Environment.getExternalStorageDirectory().toString();
                StatFs stat = new StatFs(storageDirectory);
                return (long) stat.getAvailableBlocks()
                        * (long) stat.getBlockSize();
            }
        } catch (RuntimeException ex) {
            // if we can't stat the filesystem then we don't know how many
            // free bytes exist. It might be zero but just leave it
            // blank since we really don't know.
            return CANNOT_STAT_ERROR;
        }
    }
    
    private static boolean checkFsWritable() {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.
        String directoryName =
                Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        File f = new File(directoryName, ".probe");
        try {
            // Remove stale file if any
            if (f.exists()) {
                f.delete();
            }
            if (!f.createNewFile()) {
                return false;
            }
            f.delete();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean hasStorage() {
        return hasStorage(true);
    }

    public static boolean hasStorage(boolean requireWriteAccess) {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                boolean writable = checkFsWritable();
                return writable;
            } else {
                return true;
            }
        } else if (!requireWriteAccess
                && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private void cleanupEmptyFile() {
        if (mCameraVideoFilename != null) {
            File f = new File(mCameraVideoFilename);
            if (f.length() == 0 && f.delete()) {
                Log.v(TAG, "Empty video file deleted: " + mCameraVideoFilename);
                mCameraVideoFilename = null;
            }
        }
    }
        
    private void releaseMediaRecorder() {
        Log.v(TAG, "Releasing media recorder.");
        if (mMediaRecorder != null) {
            cleanupEmptyFile();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
    
    private void updateRecordingIndicator(boolean showRecording) {
        int drawableId =
                showRecording ? R.drawable.btn_ic_video_record
                        : R.drawable.btn_ic_video_record_stop;
        Drawable drawable = getResources().getDrawable(drawableId);
        mShutterButton.setImageDrawable(drawable);
    }
        
    private void startPreview() throws CameraHardwareException {
        Log.v(TAG, "startPreview");
        if (mPreviewing) {
            // After recording a video, preview is not stopped. So just return.
            return;
        }

        if (mCameraDevice == null) {
            // If the activity is paused and resumed, camera device has been
            // released and we need to open the camera.
            mCameraDevice = CameraHolder.instance().open();
        }

        mCameraDevice.lock();
        setCameraParameters();
        setPreviewDisplay(mSurfaceHolder_Camera);

        try {
            mCameraDevice.startPreview();
            mPreviewing = true;
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        }

        // If setPreviewDisplay has been set with a valid surface, unlock now.
        // If surface is null, unlock later. Otherwise, setPreviewDisplay in
        // surfaceChanged will fail.
        if (mSurfaceHolder_Camera != null) {
            mCameraDevice.unlock();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
        mPausing = true;


        // This is similar to what mShutterButton.performClick() does,
        // but not quite the same.

        stopVideoRecording();

        
        closeCamera();

        mHandler.removeMessages(INIT_RECORDER);
        
    }    

 
 
 
    /*********************************  video player ***************************/
    // all possible internal states
    private static final int STATE_ERROR              = -1;
    private static final int STATE_IDLE               = 0;
    private static final int STATE_PREPARING          = 1;
    private static final int STATE_PREPARED           = 2;
    private static final int STATE_PLAYING            = 3;
    private static final int STATE_PAUSED             = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState  = STATE_IDLE;

    SurfaceHolder.Callback mPlayback_Callback = new SurfaceHolder.Callback()
    {
        public void surfaceChanged(SurfaceHolder holder, int format,
                                    int w, int h)
        {
              Log.i(TAG,"mPlayback_Callback surfaceChanged  ");
        
//            mMediaPlayer.start();    

        }

        public void surfaceCreated(SurfaceHolder holder)
        {
            Log.i(TAG,"mPlayback_Callback surfaceCreated  ");
            mSurfaceHolder_playback = holder;
//            openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder)
        {
            // after we return from this we can't use the surface any more
            mSurfaceHolder_playback = null;
            release(true);
        }
    };    
    
        
    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState  = STATE_IDLE;
            }
        }
    }    
    
    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState  = STATE_IDLE;
        }
    }

    private void openVideo() {
        Log.i(TAG,"openVideo ");
        if (mUri == null || mSurfaceHolder_playback == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // Tell the music playback service to pause 
        // TODO: these constants need to be published somewhere in the framework.

        if(mMediaPlayer !=null ) return;

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        try {
            Log.i(TAG,"open mediaplayer ");
            mMediaPlayer = new MediaPlayer();
            
//            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setDataSource(VideoFile);
            mMediaPlayer.setDisplay(mSurfaceHolder_playback);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepare();
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;

        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            return;
        }
    }
    
    
/*        
    private MediaPlayer.OnCompletionListener mCompletionListener =
        new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            mp.start();
        }
    };
*/
        	



    public void onClick(View v) {
        Log.i(TAG,"onClick in PlaybackActivity");
        if(mVideoErr)         	return;
        
        switch (v.getId()) {
            case R.id.playback_button:

            	if(!mVideoStoped){
            		mVideoStoped = true;
             	    Button button = (Button)findViewById(R.id.playback_button);
            	    button.setText("Stop");
            	    button.invalidate();  
            	    openVideo();
            	    Log.i(TAG,"-------mediaplayer start---------");
                    mMediaPlayer.start();

            	}
            	else{
            		mVideoStoped = false;
             	    Button button = (Button)findViewById(R.id.playback_button);
            	    button.setText("Run");
            	    button.invalidate();            		
            	    Log.i(TAG,"-------mediaplayer stop---------");                    
                    stopPlayback();
            	}
          	
                break;
      
            default:
        	   break;
        }
    }

                
}


//
// DefaultHashMap is a HashMap which returns a default value if the specified
// key is not found.
//
@SuppressWarnings("serial")
class DefaultHashMap<K, V> extends HashMap<K, V> {
    private V mDefaultValue;

    public void putDefault(V defaultValue) {
        mDefaultValue = defaultValue;
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        return (value == null) ? mDefaultValue : value;
    }
}


//
// MediaRecorderProfile reads from system properties to determine the proper
// values for various parameters for MediaRecorder.
//
class MediaRecorderProfile {

    @SuppressWarnings("unused")
    private static final String TAG = "MediaRecorderProfile";
    public final boolean mHiQuality;
    public final int mOutputFormat;
    public final int mVideoEncoder;
    public final int mAudioEncoder;
    public final int mVideoWidth;
    public final int mVideoHeight;
    public final int mVideoFps;
    public final int mVideoBitrate;
    public final int mAudioBitrate;
    public final int mAudioChannels;
    public final int mAudioSamplingRate;

    MediaRecorderProfile(boolean hiQuality) {
        mHiQuality = hiQuality;

        //mOutputFormat = getFromTable("ro.media.enc.hprof.file.format",
        //                             "ro.media.enc.lprof.file.format",
        //                             OUTPUT_FORMAT_TABLE);
        mOutputFormat = MediaRecorder.OutputFormat.MPEG_4;

        //mVideoEncoder = getFromTable("ro.media.enc.hprof.codec.vid",
        //                             "ro.media.enc.lprof.codec.vid",
        //                             VIDEO_ENCODER_TABLE);
        mVideoEncoder = MediaRecorder.VideoEncoder.H264;

        //mAudioEncoder = getFromTable("ro.media.enc.hprof.codec.aud",
        //                             "ro.media.enc.lprof.codec.aud",
        //                             AUDIO_ENCODER_TABLE);
        mAudioEncoder = MediaRecorder.AudioEncoder.AMR_NB;
        mVideoWidth = 320;
        //mVideoWidth = getInt("ro.media.enc.hprof.vid.width",
        //                     "ro.media.enc.lprof.vid.width",
        //                    352, 176);
        mVideoHeight = 240;
        //mVideoHeight = getInt("ro.media.enc.hprof.vid.height",
        //                      "ro.media.enc.lprof.vid.height",
        //                      288, 144);

        mVideoFps = 30;
        //mVideoFps = getInt("ro.media.enc.hprof.vid.fps",
        //                   "ro.media.enc.lprof.vid.fps",
        //                   20, 20);
        mVideoBitrate = 360000;
        //mVideoBitrate = getInt("ro.media.enc.hprof.vid.bps",
        //                       "ro.media.enc.lprof.vid.bps",
        //                       360000, 192000);
        mAudioBitrate = 23450;
        //mAudioBitrate = getInt("ro.media.enc.hprof.aud.bps",
        //                       "ro.media.enc.lprof.aud.bps",
        //                       23450, 23450);
        mAudioChannels = 1;
        //mAudioChannels = getInt("ro.media.enc.hprof.aud.ch",
        //                        "ro.media.enc.lprof.aud.ch",
        //                        1, 1);
        mAudioSamplingRate = 8000;
        //mAudioSamplingRate = getInt("ro.media.enc.hprof.aud.hz",
        //                            "ro.media.enc.lprof.aud.hz",
        //                            8000, 8000);
    }
    /*
    private int getFromTable(String highKey, String lowKey,
                DefaultHashMap<String, Integer> table) {
        String s;
        s = SystemProperties.get(mHiQuality ? highKey : lowKey);
        return table.get(s);
    }

    private int getInt(String highKey, String lowKey, int highDefault,
                int lowDefault) {
        String key = mHiQuality ? highKey : lowKey;
        int defaultValue = mHiQuality ? highDefault : lowDefault;
        return SystemProperties.getInt(key, defaultValue);
    }

    private static final DefaultHashMap<String, Integer>
            OUTPUT_FORMAT_TABLE = new DefaultHashMap<String, Integer>();
    private static final DefaultHashMap<String, Integer>
            VIDEO_ENCODER_TABLE = new DefaultHashMap<String, Integer>();
    private static final DefaultHashMap<String, Integer>
            AUDIO_ENCODER_TABLE = new DefaultHashMap<String, Integer>();

    static {
        OUTPUT_FORMAT_TABLE.put("3gp", MediaRecorder.OutputFormat.THREE_GPP);
        OUTPUT_FORMAT_TABLE.put("mp4", MediaRecorder.OutputFormat.MPEG_4);
        OUTPUT_FORMAT_TABLE.putDefault(MediaRecorder.OutputFormat.DEFAULT);

        VIDEO_ENCODER_TABLE.put("h263", MediaRecorder.VideoEncoder.H263);
        VIDEO_ENCODER_TABLE.put("h264", MediaRecorder.VideoEncoder.H264);
        VIDEO_ENCODER_TABLE.put("m4v", MediaRecorder.VideoEncoder.MPEG_4_SP);
        VIDEO_ENCODER_TABLE.putDefault(MediaRecorder.VideoEncoder.DEFAULT);

        AUDIO_ENCODER_TABLE.put("amrnb", MediaRecorder.AudioEncoder.AMR_NB);
        AUDIO_ENCODER_TABLE.put("amrwb", MediaRecorder.AudioEncoder.AMR_WB);
        AUDIO_ENCODER_TABLE.put("aac", MediaRecorder.AudioEncoder.AAC);
		AUDIO_ENCODER_TABLE.put("mp3", MediaRecorder.AudioEncoder.MP3);
        AUDIO_ENCODER_TABLE.put("aacplus", MediaRecorder.AudioEncoder.AAC_PLUS);
        AUDIO_ENCODER_TABLE.put("eaacplus",
                MediaRecorder.AudioEncoder.EAAC_PLUS);
        AUDIO_ENCODER_TABLE.putDefault(MediaRecorder.AudioEncoder.DEFAULT);
    }
    */
}
