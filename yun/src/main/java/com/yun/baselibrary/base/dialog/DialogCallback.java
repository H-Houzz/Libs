package com.yun.baselibrary.base.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;

import androidx.annotation.NonNull;

public interface DialogCallback {
    @NonNull
    Dialog bindDialog(Activity activity);

    void setWindowStyle(Window window);
}
