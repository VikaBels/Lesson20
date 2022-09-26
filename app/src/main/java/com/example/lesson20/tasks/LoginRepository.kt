package com.example.lesson20.tasks

import android.util.Log
import bolts.CancellationToken
import bolts.Task
import com.example.lesson20.*
import com.example.lesson20.models.LoginRequestBody
import com.example.lesson20.models.LoginResponseBody
import com.example.lesson20.utils.getRequest
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody

class LoginRepository(
    private val gson: Gson,
    private val client: OkHttpClient
) {
    companion object {
        private const val URL_LOGIN =
            "https://pub.zame-dev.org/senla-training-addition/lesson-21.php?method=login"
    }

    fun startTask(
        cancellationToken: CancellationToken,
        email: String,
        password: String
    ): Task<LoginResponseBody> {
        return Task
            .callInBackground({
                getLoginResponseBody(email, password)
            }, cancellationToken)
    }
    
    private fun getLoginResponseBody(email: String, password: String): LoginResponseBody? {
        var loginResponseBody: LoginResponseBody? = null

        try {
            loginResponseBody = startRequestLogin(email, password)
        } catch (ex: InterruptedException) {
            Log.e(TAG_INTERRUPTED_EXCEPTION, MESSAGE_INTERRUPTED_EXCEPTION, ex)
        }

        return loginResponseBody
    }

    private fun startRequestLogin(email: String, password: String): LoginResponseBody? {
        val requestBody = LoginRequestBody(
            email = email,
            password = password
        )

        val requestBodyString = gson.toJson(requestBody)
        val okHttpRequestBody = requestBodyString.toRequestBody(TYPE_CONTENT.toMediaType())

        var singInResponseBody: LoginResponseBody? = null

        val response = client
            .newCall(getRequest(okHttpRequestBody, URL_LOGIN))
            .execute()

        if (response.isSuccessful) {
            val responseBodyString = response.body?.string()

            if (!responseBodyString.isNullOrEmpty()) {
                singInResponseBody = gson.fromJson(
                    responseBodyString,
                    LoginResponseBody::class.java
                )
            }

        } else {
            Log.e(TAG_INTERRUPTED_EXCEPTION, "${response.code}")
        }

        return singInResponseBody
    }
}
