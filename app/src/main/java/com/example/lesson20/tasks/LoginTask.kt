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
import okhttp3.Request
import okhttp3.RequestBody
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

    //rename
    private fun onPostExecute(result: Task<LoginResponseBody?>?): LoginResponseBody {
        val responseBody = result?.result ?: LoginResponseBody(ERROR_STATUS, null)

        sendBroadcastResponseBody(responseBody)
        return responseBody
    }

    private fun sendBroadcastResponseBody(responseBody: LoginResponseBody?) {
        val intent = Intent(BROADCAST_ACTION_RESPONSE_LOGIN)
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
            .newCall(getRequestLogin(okHttpRequestBody))
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

    private fun getRequestLogin(okHttpRequestBody: RequestBody): Request {
        return Request.Builder()
            .post(okHttpRequestBody)
            .url(URL_LOGIN)
            .build()
    }

    
    fun startTask() {
        Task.callInBackground {
            getLoginResponseBody()
        }.onSuccess({
            onPostExecute(it)
        }, Task.UI_THREAD_EXECUTOR)
            .continueWith({

                if (it.error != null) {
                    onPostExecute(null)
                    showErrorToast(App.getInstanceApp(), getTextError(it))
                }

            }, Task.UI_THREAD_EXECUTOR)
    }

    //in file. exist in this file and in ProfileTask
    private fun getTextError(help: Task<LoginResponseBody>): String? {

        val textError: String? = when (help.error.javaClass.name) {
            ERROR_TYPE_UNKNOWN_HOST_EXCEPTION -> {
                MESSAGE_NO_INTERNET_EXCEPTION
            }
            ERROR_TYPE_SOCKET_TIMEOUT_EXCEPTION -> {
                MESSAGE_PROBLEM_WITH_SOCKET
            }
            else -> {
                help.error.message
            }
        }
        return textError
    }
}
