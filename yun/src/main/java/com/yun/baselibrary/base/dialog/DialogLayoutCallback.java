package com.yun.baselibrary.base.dialog;

import android.view.View;
import android.view.Window;

public interface DialogLayoutCallback {
    int bindTheme();

    int bindLayout();

    void initView(BaseDialogFragment dialog, View contentView);

    void setWindowStyle(Window window);

    void onCancel(BaseDialogFragment dialog);

    void onDismiss(BaseDialogFragment dialog);
}
