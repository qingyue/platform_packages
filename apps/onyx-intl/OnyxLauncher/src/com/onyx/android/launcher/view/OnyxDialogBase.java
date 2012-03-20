package com.onyx.android.launcher.view;

import android.app.Dialog;
import android.content.Context;

import com.onyx.android.launcher.R;

public class OnyxDialogBase extends Dialog
{
    public OnyxDialogBase(Context context)
    {
        super(context, R.style.dialog_no_title);
    }
}