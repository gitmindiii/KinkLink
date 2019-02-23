package com.kinklink.helper;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.kinklink.R;

public class Progress extends Dialog {
    public Progress(Context context) {
        super(context, R.style.ProgressBarTheme);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.custom_progress_dialog_layout);
    }
}
