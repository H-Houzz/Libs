package com.yun.baselibrary.http.common

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.yun.baselibrary.been.BaseResponse
import com.yun.baselibrary.http.AppException
import com.yun.baselibrary.http.ExceptionHandle
import com.yun.baselibrary.utils.showToast

/**
 * 网络状态,加载中,成功,失败
 */
sealed class ResultState<out T> {
    object Loading : ResultState<Nothing>()
    data class Success<out T>(val data: T,var code:Int =0, val msg:String = "") : ResultState<T>()
    data class Error(val error: AppException? = null) :
        ResultState<Nothing>()
}

/**
 * 处理返回值
 * @param result 请求结果
 */
fun <T> MutableLiveData<ResultState<T>>.paresResult(result: BaseResponse<T>) {
    result.data?.let {
        value = ResultState.Success(
            it,
            result.code,
            result.msg
        )
    }
}
/**
 * 不处理返回值 直接返回请求结果
 * @param result 请求结果
 */
fun <T> MutableLiveData<ResultState<T>>.paresResult(result: T) {
    value = ResultState.Success(result)
}

/**
 * 异常转换异常处理
 */
fun <T> MutableLiveData<ResultState<T>>.paresException(e: Throwable, paresCode: Boolean = true) {
    val error = ExceptionHandle.handleException(e)
    if (paresCode && !TextUtils.isEmpty(error.message) && !e.message.toString().contains("Job was cancelled"))
        showToast("${error.message.toString()}")
    this.value = ResultState.Error(error)
}

