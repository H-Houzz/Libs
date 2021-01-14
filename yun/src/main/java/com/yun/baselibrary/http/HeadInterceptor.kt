package com.yun.baselibrary.http

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.URLEncoder

/**
 * 头部参数拦截器，传入heads
 */
class HeadInterceptor(private val headers: Map<String, String>? = null) :
    Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request()
            .newBuilder()
        if (headers != null && headers.isNotEmpty()) {
            val keys = headers.keys
            for (headerKey in keys) {
                headers[headerKey]?.let {
                    builder.addHeader(headerKey, URLEncoder.encode(it,"UTF-8")).build()
                }
            }
        }
        //请求信息
        return chain.proceed(builder.build())
    }

}