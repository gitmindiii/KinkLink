package com.kinklink.helper;

import android.content.Context;
import android.widget.Toast;

public class CustomToast {
    private static CustomToast mInstance;
    private static Context mCtx;
    private static Toast toast;

    public static CustomToast getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CustomToast(context);
            toast = new Toast(context);
        }
        return mInstance;
    }

    private CustomToast(Context context) {
        mCtx = context;
    }

    public void showToast(Context context, String message) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();

    }
}
