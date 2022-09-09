package com.example.lesson20.tasks

import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lesson20.*
import com.example.lesson20.models.App.Companion.getClient
import com.example.lesson20.models.App.Companion.getGson
import com.example.lesson20.models.App.Companion.getInstanceApp
import com.example.lesson20.models.LoginRequestBody
import com.example.lesson20.models.LoginResponseBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException

class SendRequestLoginTask(
    private val email: String,
    private val password: String
) : AsyncTask<Void?, String?, LoginResponseBody?>() {
    companion object {
        private const val URL_LOGIN =
            "https://pub.zame-dev.org/senla-training-addition/lesson-21.php?method=login"

        const val RESULT_LOGIN_REQUEST = "RESULT_LOGIN_REQUEST"
        const val BROADCAST_ACTION_RESPONSE_LOGIN =
            "SendRequestLoginTask.BROADCAST_ACTION_RESPONSE_LOGIN"
    }

    override fun doInBackground(vararg p0: Void?): LoginResponseBody? {
        var loginResponseBody: LoginResponseBody? = null

        try {
            loginResponseBody = startRequestLogin()
        } catch (ex: InterruptedException) {
            Log.e(TAG_INTERRUPTED_EXCEPTION, MESSAGE_INTERRUPTED_EXCEPTION, ex)
        }

        return loginResponseBody
    }


    override fun onPostExecute(result: LoginResponseBody?) {
        super.onPostExecute(result)
        val responseBody: LoginResponseBody?

        responseBody = result ?: LoginResponseBody(ERROR_STATUS, null)

        sendBroadcastResponseBody(responseBody)
    }

    private fun sendBroadcastResponseBody(responseBody: LoginResponseBody?) {
        val intent = Intent(BROADCAST_ACTION_RESPONSE_LOGIN)
        intent.putExtra(RESULT_LOGIN_REQUEST, responseBody)
        LocalBroadcastManager.getInstance(getInstanceApp()).sendBroadcast(intent)
    }

    private fun startRequestLogin(): LoginResponseBody? {
        val requestBody = LoginRequestBody(
            email = email,
            password = password
        )

        val requestBodyString = getGson().toJson(requestBody)
        val okHttpRequestBody = requestBodyString.toRequestBody(TYPE_CONTENT.toMediaType())

        var singInResponseBody: LoginResponseBody? = null

        try {
            val response = getClient()
                .newCall(getRequestLogin(okHttpRequestBody))
                .execute()

            if (response.isSuccessful) {
                val responseBodyString = response.body?.string()

                if (!responseBodyString.isNullOrEmpty()) {
                    singInResponseBody = getGson().fromJson(
                        responseBodyString,
                        LoginResponseBody::class.java
                    )
                }

            } else {
                Log.e(TAG_INTERRUPTED_EXCEPTION, "${response.code}")
            }
        } catch (e: SocketTimeoutException) {
            Log.e(
                TAG_INTERRUPTED_EXCEPTION,
                MESSAGE_PROBLEM_WITH_SOCKET, e
            )
        } catch (ex: IOException) {
            Log.e(
                TAG_INTERRUPTED_EXCEPTION,
                MESSAGE_NO_INTERNET_EXCEPTION, ex
            )
        }

        return singInResponseBody
    }

    private fun getRequestLogin(okHttpRequestBody: RequestBody): Request {
        return Request.Builder()
            .post(okHttpRequestBody)
            .url(URL_LOGIN)
            .build()
    }
}

