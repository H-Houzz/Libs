package com.yun;

import android.app.Application;

import com.yun.baselibrary.utils.MMKVUtils;

/**
 * @Author: HOU
 * @Date: 2021/2/25 10:46 AM
 * @Desc
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MMKVUtils.init(this,"yun");
    }
}
