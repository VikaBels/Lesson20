package com.example.lesson20

import okhttp3.Request
import okhttp3.RequestBody

fun getRequest(okHttpRequestBody: RequestBody, url: String): Request {
    return Request.Builder()
        .post(okHttpRequestBody)
        .url(url)
        .build()
}