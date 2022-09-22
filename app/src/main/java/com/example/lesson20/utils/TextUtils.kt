package com.example.lesson20.utils

import com.example.lesson20.App.Companion.getInstanceApp
import com.example.lesson20.R
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun getTextError(profileResponseBodyException: Exception): String? {
    val resources = getInstanceApp().resources

    val textError = when (profileResponseBodyException) {
        is UnknownHostException -> {
            resources.getString(R.string.error_no_internet)
        }
        is SocketTimeoutException -> {
            resources.getString(R.string.error_problem_with_socket)
        }
        else -> {
            profileResponseBodyException.message
        }
    }
    return textError
}