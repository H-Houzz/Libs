package com.yun.baselibrary.base

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.StringUtils
import com.yun.baselibrary.been.BaseResponse
import com.yun.baselibrary.http.AppException
import com.yun.baselibrary.http.ExceptionHandle
import com.yun.baselibrary.http.common.ResultState
import com.yun.baselibrary.http.common.paresException
import com.yun.baselibrary.http.common.paresResult
import com.yun.baselibrary.loading.LoadingDialog
import com.yun.baselibrary.utils.ScreenManager
import com.yun.baselibrary.utils.logD
import com.yun.baselibrary.utils.logE
import com.yun.baselibrary.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open abstract class BaseViewModel : ViewModel(), LifecycleObserver {

    fun <T> ViewModel.request(
        block: suspend () -> BaseResponse<T>,
        showDialog:Boolean = false,
        errorToast:Boolean = true,
        errorData: Boolean = false,
        success: ((info: BaseResponse<T>) -> Unit) = {},
        error: ((info: AppException) -> Unit) = {},
        loading:(()->Unit) = {}) {
        viewModelScope.launch {
            var dialog: LoadingDialog? = null
            runCatching {
                loading.invoke()
                if (showDialog) {
                    dialog = LoadingDialog()
                    val activity = ActivityUtils.getTopActivity() as FragmentActivity
                    dialog?.let {
                        activity.supportFragmentManager.beginTransaction().add(it, "loading").commitAllowingStateLoss()
                    }
                }
                withContext(Dispatchers.IO) { block() }
            }.onSuccess {
                dialog?.dismissAllowingStateLoss()
                codeInterceptor(it.code)
                if (it.code == 0) {
                    success.invoke(it)
                } else {
                    if (errorToast && !StringUtils.isEmpty(it.msg)) {
                        showToast(it.msg)
                    }
                    error.invoke(
                        AppException(
                            it.code,
                            it.msg,
                            if (errorData) it.data.toString() else ""
                        )
                    )
                }
            }.onFailure {
                logD("错误信息----${it.message.toString()}")
                dialog?.dismissAllowingStateLoss()
                val onFail = ExceptionHandle.handleException(it)
                if (!it.message.toString().contains("Job was cancelled"))
                    showToast("${onFail.message.toString()}")
                error.invoke(onFail)
            }
        }
    }
    abstract fun codeInterceptor(code:Int)
    fun <T> ViewModel.request(
        resultState: MutableLiveData<ResultState<T>>,
        showDialog:Boolean = false,
        errorToast:Boolean = true,
        errorData:Boolean = false,
        block: suspend () -> BaseResponse<T>
    ) {
        viewModelScope.launch {
            var dialog: LoadingDialog? = null
            runCatching {
                resultState.value = ResultState.Loading
                if (showDialog) {
                    dialog = LoadingDialog()
                    val activity = ScreenManager.currentActivity() as FragmentActivity
                    dialog?.let {
                        activity.supportFragmentManager.beginTransaction().add(it, "loading").commitAllowingStateLoss()
                    }
                }
                withContext(Dispatchers.IO) { block() }
            }.onSuccess {
                dialog?.dismissAllowingStateLoss()
                codeInterceptor(it.code)
                if (it.code == 0) {
                    resultState.paresResult(it)
                } else {
                    resultState.paresException(
                        AppException(
                            it.code,
                            it.msg,
                            if (errorData) it.data.toString() else ""
                        ), errorToast)
                }

            }.onFailure {
                logD("错误信息----${it.message.toString()}")
                dialog?.dismissAllowingStateLoss()
                resultState.paresException(it)
            }
        }
    }
}


