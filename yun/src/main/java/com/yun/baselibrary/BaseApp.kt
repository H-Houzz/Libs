package com.yun.baselibrary

import android.content.Context
import android.util.Log
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.LogStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import kotlin.properties.Delegates

class BaseApp {

    companion object {
        var CONTEXT: Context by Delegates.notNull()
        var isDebug: Boolean by Delegates.notNull()
    }

    fun init(context: Context, debug: Boolean) {
        CONTEXT = context
        isDebug = debug
        initLogger()
    }

    /**
     * 初始化日志框架
     */
    private fun initLogger() {
        val logStrategy = object : LogStrategy {
            private val prefix = arrayOf(". ", " .")
            private var index = 0
            override fun log(priority: Int, tag: String?, message: String) {
                index = index xor 1
                Log.println(priority, prefix[index] + tag!!, message)
            }
        }
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .logStrategy(logStrategy)
            .showThreadInfo(false)  //（可选）是否显示线程信息。 默认值为true
            .methodCount(1)         // （可选）要显示的方法行数。 默认2
            .methodOffset(1)        // （可选）隐藏内部方法调用到偏移量。 默认5
            .tag("yun")        //（可选）每个日志的全局标记。 默认PRETTY_LOGGER
            .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))

    }
}