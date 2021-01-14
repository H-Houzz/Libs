package com.yun.baselibrary.been

data class BaseResponse<T>(
    var code: Int,
    var msg: String,
    var data: T
)