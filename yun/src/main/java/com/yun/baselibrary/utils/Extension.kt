package com.yun.baselibrary.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.blankj.utilcode.util.ToastUtils
import com.yun.baselibrary.http.ExceptionHandle
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.reflect.Type

fun logV(message: String) {
    Logger.v(message)
}
fun logD(message: Any) {
    Logger.d(message)
}
fun logI(message: String) {
    Logger.i(message)
}
fun logW(message: String) {
    Logger.w(message)
}
fun logE(message: String) {
    Logger.e(message)
}

fun showToast(message: String){
    if (!TextUtils.isEmpty(message))
        ToastUtils.showShort(message)
}

fun Context.dp2px(dp: Int): Int {
    val scale = resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}

fun Context.px2dp(px: Int): Int {
    val scale = resources.displayMetrics.density
    return (px / scale + 0.5f).toInt()
}

/**
 *  px--sp
 */
fun Context.px2sp(px: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (px / fontScale + 0.5f).toInt()
}

fun Context.sp2px(sp: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (sp * fontScale + 0.5f).toInt()
}

val Context.screenWidth
    get() = resources.displayMetrics.widthPixels

val Context.screenHeight
    get() = resources.displayMetrics.heightPixels


//点击监听-----
inline fun View.click(crossinline block: () -> Unit) = setOnClickListener { block() }


inline fun <reified T : Context> Context.newIntent() {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

inline fun <reified T : Activity> Activity.newIntent(finishFlag: Boolean = false) {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
    if(finishFlag) {
        this.finish()
    }
}

inline fun TextView.setEditTextHintSize(hintText: String?, size: Int){
    val ss = SpannableString(hintText) //定义hint的值
    val ass = AbsoluteSizeSpan(size, true) //设置字体大小 true表示单位是sp
    ss.setSpan(ass, 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.hint = SpannableString(ss)
}
fun TextView.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun TextView.onTextChanged(onTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            onTextChanged.invoke(p0.toString())
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })
}
@MainThread
inline fun <T> LiveData<T>.newObserve(
    owner: LifecycleOwner,
    crossinline onChanged: (T) -> Unit
): Observer<T> {
    val wrappedObserver = Observer<T> { t -> onChanged.invoke(t) }
    observe(owner, wrappedObserver)
    return wrappedObserver
}


fun ViewModel.launchTest(
    block: suspend CoroutineScope.() -> Unit,
    onError: (e: Throwable) -> Unit = {},
    onComplete: () -> Unit = {}
) {
    viewModelScope.launch(
        CoroutineExceptionHandler { _, throwable ->
            run {
                // 这里统一处理错误
                 ExceptionHandle.handleException(throwable).message
                onError(throwable)
            }
        }
    ) {
        try {
            block.invoke(this)
        } finally {
            onComplete()
        }
    }
}
