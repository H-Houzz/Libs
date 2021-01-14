package com.yun.baselibrary.utils

import android.app.Activity
import java.util.*

object ScreenManager {
    private val activityStack = Stack<Activity>()

    fun pushActivity(activity: Activity){
        activityStack.add(activity)
    }

    fun popActivity(){
        val activity = activityStack.lastElement()
        activity?.finish()
    }
    fun popActivity(activity: Activity?){
        activity?.let {
//            it.finish()
            activityStack.remove(it)
        }
    }

    fun currentActivity():Activity{
        return activityStack.lastElement()
    }

    fun popAllActivityExceptOne(cls: Class<*>) {
        while (true) {
            val activity = currentActivity() ?: break
            if (activity.javaClass == cls) {
                break
            }
            popActivity(activity)
        }
    }

    // 退出栈中所有Activity
    fun popAllActivity() {
        while (true) {
            val activity = currentActivity() ?: break
            popActivity(activity)
        }
    }
}