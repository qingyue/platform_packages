package com.onyx.android.launcher.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.onyx.android.launcher.R;
import com.onyx.android.launcher.view.OnyxDialogBase;

public class DialogFileOperations extends OnyxDialogBase
{
	private Button mButtonCancel = null;
	private boolean mIsShowing = true;

    public DialogFileOperations(Context context)
    {
        super(context);

        this.setContentView(R.layout.dialog_file_operations);

        mButtonCancel = (Button)findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFileOperations.this.cancel();
			}
		});
    }

    @Override
    public void cancel() {
    	mIsShowing = false;
    	super.cancel();
    }

    @Override
    public void dismiss() {
    	mIsShowing = false;
    	super.dismiss();
    }

    public boolean getIsShowing() {
    	return this.mIsShowing;
    }
}
