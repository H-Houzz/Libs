package com.yun.baselibrary.http.common

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yun.baselibrary.http.AppException
import com.yun.baselibrary.utils.newObserve

/**
 *
 */
typealias StatefulLiveData<T> = LiveData<ResultState<T>>
typealias StatefulMutableLiveData<T> = MutableLiveData<ResultState<T>>
typealias SingLiveLiveData<T> = SingleLiveEvent<ResultState<T>>
@MainThread
inline fun <T> StatefulLiveData<T>.observeState(
    owner: LifecycleOwner,
    crossinline onLading: () -> Unit = {},
    crossinline onSuccess: (T) -> Unit = {},
    crossinline onError: (AppException?) -> Unit = {}
) {
    newObserve(owner) { state ->
        when (state) {
            is ResultState.Loading -> onLading.invoke()
            is ResultState.Success -> onSuccess(state.data)
            is ResultState.Error -> onError(state.error)
        }
    }
}


@MainThread
inline fun <T> StatefulLiveData<T>.observeStateCode(
    owner: LifecycleOwner,
    crossinline onLading: () -> Unit = {},
    crossinline onSuccess: (T, code: Int, msg: String) -> Unit,
    crossinline onError: (AppException?) -> Unit = {}
) {
    newObserve(owner) { state ->
        when (state) {
            is ResultState.Loading -> onLading.invoke()
            is ResultState.Success -> onSuccess(state.data, state.code, state.msg)
            is ResultState.Error -> onError(state.error)
        }
    }
}