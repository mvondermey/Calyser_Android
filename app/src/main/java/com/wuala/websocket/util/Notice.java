package com.wuala.websocket.util;

import android.content.Context;
import android.widget.Toast;

public class Notice {

    private Toast toast;
    private Context context;

    public Notice(Context context) {
        this.context = context;
    }

    public void showToast(String message) {
        if (toast != null) {
            toast.setText(message);
        } else {
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        }
        toast.show();
    }

    public void closeToast() {
        if (toast != null)
            toast.cancel();
    }

}