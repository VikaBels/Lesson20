package com.example.lesson20.tasks

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import bolts.Task
import com.example.lesson20.*
import com.example.lesson20.models.App
import com.example.lesson20.models.LoginRequestBody
import com.example.lesson20.models.LoginResponseBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class LoginTask(
    private val email: String,
    private val password: String
) {
    companion object {
        private const val URL_LOGIN =
            "https://pub.zame-dev.org/senla-training-addition/lesson-21.php?method=login"

        const val RESULT_LOGIN_REQUEST = "RESULT_LOGIN_REQUEST"
        const val BROADCAST_ACTION_RESPONSE_LOGIN =
            "SendRequestLoginTask.BROADCAST_ACTION_RESPONSE_LOGIN"
    }

    private fun getLoginResponseBody(): LoginResponseBody? {
        var loginResponseBody: LoginResponseBody? = null

        try {
            loginResponseBody = startRequestLogin()
        } catch (ex: InterruptedException) {
            Log.e(TAG_INTERRUPTED_EXCEPTION, MESSAGE_INTERRUPTED_EXCEPTION, ex)
        }

        return loginResponseBody
    }

    private fun getResponseBody(result: Task<LoginResponseBody?>?): LoginResponseBody {
        return result?.result ?: LoginResponseBody(KEY_ERROR_EXIST, null)
    }

    private fun sendBroadcastResponseBody(result: Task<LoginResponseBody?>?) {
        val intent = Intent(BROADCAST_ACTION_RESPONSE_LOGIN)
        val responseBody = getResponseBody(result)

        intent.putExtra(RESULT_LOGIN_REQUEST, responseBody)
        LocalBroadcastManager.getInstance(App.getInstanceApp()).sendBroadcast(intent)
    }

    private fun startRequestLogin(): LoginResponseBody? {
        val requestBody = LoginRequestBody(
            email = email,
            password = password
        )

        val requestBodyString = App.getGson().toJson(requestBody)
        val okHttpRequestBody = requestBodyString.toRequestBody(TYPE_CONTENT.toMediaType())

        var singInResponseBody: LoginResponseBody? = null

        val response = App.getClient()
            .newCall(requestUtil(okHttpRequestBody, URL_LOGIN))
            .execute()

        if (response.isSuccessful) {
            val responseBodyString = response.body?.string()

            if (!responseBodyString.isNullOrEmpty()) {
                singInResponseBody = App.getGson().fromJson(
                    responseBodyString,
                    LoginResponseBody::class.java
                )
            }

        } else {
            Log.e(TAG_INTERRUPTED_EXCEPTION, "${response.code}")
        }

        return singInResponseBody
    }

    fun startTask() {
        Task.callInBackground {
            getLoginResponseBody()
        }.onSuccess({
            sendBroadcastResponseBody(it)
            getResponseBody(it)
        }, Task.UI_THREAD_EXECUTOR)
            .continueWith({

                if (it.error != null) {
                    getResponseBody(null)
                    sendBroadcastResponseBody(it)
                    toastUtil(App.getInstanceApp(), getTextError(it))
                }

            }, Task.UI_THREAD_EXECUTOR)
    }

    private fun getTextError(loginResponseBody: Task<LoginResponseBody?>): String? {
        val textError = when (loginResponseBody.error.javaClass.name) {
            ERROR_TYPE_UNKNOWN_HOST_EXCEPTION -> {
                MESSAGE_NO_INTERNET_EXCEPTION
            }
            ERROR_TYPE_SOCKET_TIMEOUT_EXCEPTION -> {
                MESSAGE_PROBLEM_WITH_SOCKET
            }
            else -> {
                loginResponseBody.error.message
            }
        }
        return textError
    }
}
