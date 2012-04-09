/**
 * 
 */
package com.onyx.android.launcher.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

import com.onyx.android.launcher.R;

/**
 * @author qingyue
 *
 */
public class DialogProgressBarRotundity extends Dialog
{

    public DialogProgressBarRotundity(Context context)
    {
        super(context, R.style.dialog_progress_style);
        
        View view = View.inflate(context, R.layout.dialog_progressbar_rotundity, null);
        
        this.setContentView(view);
        
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
