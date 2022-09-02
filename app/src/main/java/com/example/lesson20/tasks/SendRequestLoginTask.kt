package com.example.lesson20.tasks

import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lesson20.*
import com.example.lesson20.models.App.Companion.getInstanceApp
import com.example.lesson20.models.SingInRequestBodyLogin
import com.example.lesson20.models.SingInRequestBodyProfile
import com.example.lesson20.models.SingInResponseBodyLogin
import com.example.lesson20.models.SingInResponseBodyProfile
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SendRequestLoginTask(
    private val email: String,
    private val password: String
) : AsyncTask<Void?, String?, Void?>() {
    companion object {
        const val URL_LOGIN =
            "https://pub.zame-dev.org/senla-training-addition/lesson-20.php?method=login"
    }

    ///???
    private var currentToken: String? =null

    private val gson = Gson()

    private val client = OkHttpClient
        .Builder()
        .build()

    override fun onProgressUpdate(vararg values: String?) {
        super.onProgressUpdate(*values)
        sendBroadcastForProgressBar()

        if (values[0].isNullOrEmpty()) {
            sendBroadcastError(true)
        } else {
            sendBroadcastError(false)
        }

        //send token
        currentToken?.let { sendBroadcastToken(it) }
    }

    override fun doInBackground(vararg p0: Void?): Void? {
        try {
            startRequestLogin()
        } catch (ex: InterruptedException) {
            Log.e(TAG_INTERRUPTED_EXCEPTION, MESSAGE_INTERRUPTED_EXCEPTION, ex)
        }
        return null
    }

    private fun sendBroadcastError(isVisible: Boolean) {
        val intent = Intent(BROADCAST_ACTION_SHOW_ERROR)
        intent.putExtra(EXTRA_RESULT_TEXT_VIEW, isVisible)
        LocalBroadcastManager.getInstance(getInstanceApp()).sendBroadcast(intent)
    }

    private fun sendBroadcastForProgressBar() {
        val intent = Intent(BROADCAST_ACTION_PROGRESS_BAR)
        intent.putExtra(EXTRA_RESULT_PROGRESS_BAR, false)
        LocalBroadcastManager.getInstance(getInstanceApp()).sendBroadcast(intent)
    }

    private fun sendBroadcastToken(token: String) {
        val intent = Intent(BROADCAST_ACTION_TOKEN)
        intent.putExtra(EXTRA_RESULT_TOKEN, token)
        LocalBroadcastManager.getInstance(getInstanceApp()).sendBroadcast(intent)
    }

    private fun startRequestLogin() {
        val requestBody = SingInRequestBodyLogin(
            email = email,
            password = password
        )

        val requestBodyString = gson.toJson(requestBody)
        val okHttpRequestBody = requestBodyString.toRequestBody(TYPE_CONTENT.toMediaType())

        val response = client
            .newCall(getRequestLogin(okHttpRequestBody))
            .execute()

        try {
            if (response.isSuccessful) {
                val responseBodyString = response.body?.string()
                val singInResponseBody = gson.fromJson(
                    responseBodyString,
                    SingInResponseBodyLogin::class.java
                )

                //Для того,чтобы вывести ошибку над полями
                //Передаём в метод onProgressUpdate, потому что тут
                //вызывать нельзя,не главный поток

                //maybe change????
                currentToken = singInResponseBody.token
                publishProgress(singInResponseBody.token)

//                if (singInResponseBody.status == "ok") {
//                    sendRequestProfile(singInResponseBody.token)
//                }

            } else {
                Log.e(TAG_INTERRUPTED_EXCEPTION, "${response.code}")
            }
        } catch (ex: IOException) {
            Log.e(TAG_INTERRUPTED_EXCEPTION, MESSAGE_NO_INTERNET_EXCEPTION, ex)
        }

    }


    private fun getRequestLogin(okHttpRequestBody: RequestBody): Request {
        return Request.Builder()
            .post(okHttpRequestBody)
            .url(URL_LOGIN)
            .build()
    }
}

