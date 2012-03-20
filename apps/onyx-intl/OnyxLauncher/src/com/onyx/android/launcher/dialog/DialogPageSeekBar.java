/**
 * 
 */
package com.onyx.android.launcher.dialog;

import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.onyx.android.launcher.R;
import com.onyx.android.sdk.ui.data.OnyxPagedAdapter;

/**
 * @author qingyue
 *
 */
public class DialogPageSeekBar extends Dialog
{
    private static final String TAG = "DialogPageSeekBar";
    
    private SeekBar mSeekBarPage = null;
    private EditText mEditTextSkipPage = null;
    private Button mButtonSkip = null;
    private TextView mTextViewAllPage = null;
    private OnyxPagedAdapter mAdapter = null;
    private Context mContext = null;
    
    private GestureDetector mGestureDetector = null;

    public DialogPageSeekBar(Context context, OnyxPagedAdapter adapter)
    {
        super(context, R.style.dialog_no_title);
        
        mAdapter = adapter;
        mContext = context;

        View view = View.inflate(context, R.layout.dialog_page_seekbar, (ViewGroup)findViewById(R.id.layout_pages_seekbar));

        mSeekBarPage = (SeekBar)view.findViewById(R.id.seekbar_page);
        mTextViewAllPage = (TextView)view.findViewById(R.id.textview_allpage);
        mEditTextSkipPage = (EditText)view.findViewById(R.id.edittext_pages);
        mButtonSkip = (Button)view.findViewById(R.id.button_skip_page);

        this.initProgressBar();

        mButtonSkip.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                DialogPageSeekBar.this.judgeSkipPage();
                DialogPageSeekBar.this.cancel();
            }
        });

        mSeekBarPage.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
            private int mPage = mAdapter.getPaginator().getPageIndex() + 1;
            private static final int mMinUnits = 100;
            private static final int mOffset = mMinUnits / 10;
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                mAdapter.getPaginator().setPageIndex(mPage);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser)
            {
                if (((mPage + 1) * mMinUnits) + mOffset < progress || ((mPage + 1) * mMinUnits) - mOffset > progress) {
                    if (fromUser) {
                        int page_index = 0;

                        if (progress < seekBar.getMax()) {
                            int current_progress = (mAdapter.getPaginator().getPageIndex() + 1) * mMinUnits;

                            if ((progress < current_progress) && (progress >= (current_progress - mMinUnits))) {
                                if (progress >= mMinUnits) {
                                    progress -= mMinUnits;
                                }
                            }

                            page_index = progress / mMinUnits;
                        }
                        else {
                            page_index = progress / mMinUnits - 1;
                        }
                        assert (page_index >= 0);
                        mPage = page_index;

                        progress = (mPage + 1) * mMinUnits;
                    }
                    seekBar.setProgress(progress);
                }
            }
        });

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();

        params.width = window.getWindowManager().getDefaultDisplay().getWidth();
        params.y = window.getWindowManager().getDefaultDisplay().getHeight();

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                |WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        window.setAttributes(params);

        this.setContentView(view);

        mAdapter.registerDataSetObserver(new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                DialogPageSeekBar.this.initProgressBar();
            }
        });
        
        mEditTextSkipPage.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (KeyEvent.ACTION_DOWN == event.getAction()) {
                    DialogPageSeekBar.this.judgeSkipPage();
                    
                    InputMethodManager inputMethodManager = 
                            (InputMethodManager)mEditTextSkipPage.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(mEditTextSkipPage.getWindowToken(), 0);
                    
                    DialogPageSeekBar.this.cancel();
                }

                return true;
            }
        });
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetector(this.getContext(), new SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e)
                {
                    Log.d(TAG, "single tap: " + e.getX() + ", " + e.getY());
                    // if single tap happens outside of dialog, then auto close dialog for convenience
                    // assume dialog at bottom of the screen
                    int out_range = -10;
                    if (e.getY() < out_range) {
                        DialogPageSeekBar.this.dismiss();
                        return true;
                    }
                    else {
                        return super.onSingleTapConfirmed(e);
                    }
                }
            });
        }
        
        return mGestureDetector.onTouchEvent(event);
    }

    private void initProgressBar()
    {
        final int current_page = mAdapter.getPaginator().getPageIndex() + 1;
        final int page_count = (mAdapter.getPaginator().getPageCount() != 0) ? 
                mAdapter.getPaginator().getPageCount() : 1;

        mTextViewAllPage.setText(String.valueOf(page_count));
        mEditTextSkipPage.setText(String.valueOf(current_page));

        mSeekBarPage.setMax(page_count * 100);
        // due to seekbar's bug, we have to reset seekbar's value to force updating UI
        mSeekBarPage.setProgress(0);
        mSeekBarPage.setProgress(current_page * 100);
    }

    private void judgeSkipPage()
    {
        if (!mEditTextSkipPage.getText().toString().equals("")) {
            int page = Integer.parseInt(mEditTextSkipPage.getText().toString());
            if (page <= Integer.parseInt(mTextViewAllPage.getText().toString()) && page > 0) {
                mAdapter.getPaginator().setPageIndex(page - 1);
                mSeekBarPage.setProgress(page * 100);
            }
            else {
                Toast.makeText(mContext, "Exceed the total number of pages", 1000).show();
            }
        }
        else {
            Toast.makeText(mContext, "Please enter the number", 1000).show();
        }
    }
}