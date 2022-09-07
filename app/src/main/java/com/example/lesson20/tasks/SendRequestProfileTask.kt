package com.example.lesson20.tasks

import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lesson20.*
import com.example.lesson20.models.App
import com.example.lesson20.models.ProfileRequestBody
import com.example.lesson20.models.ProfileResponseBody
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException

class SendRequestProfileTask(
    private val token: String
) : AsyncTask<Void?, String?, ProfileResponseBody?>() {
    companion object {
        const val URL_PROFILE =
            "https://pub.zame-dev.org/senla-training-addition/lesson-20.php?method=profile"
    }

    private val gson = Gson()

    private val client = OkHttpClient
        .Builder()
        .build()

    override fun doInBackground(vararg params: Void?): ProfileResponseBody? {
        var objectResponseBodyProfile: ProfileResponseBody? = null

        try {
            objectResponseBodyProfile = sendRequestProfile(token)
        } catch (ex: InterruptedException) {
            Log.e(TAG_INTERRUPTED_EXCEPTION, MESSAGE_INTERRUPTED_EXCEPTION, ex)
        }

        return objectResponseBodyProfile
    }

    override fun onPostExecute(result: ProfileResponseBody?) {
        super.onPostExecute(result)

        sendBroadcastPersonInfo(result)
    }

    private fun sendBroadcastPersonInfo(objectResponseBodyProfile: ProfileResponseBody?) {
        val intent = Intent(BROADCAST_ACTION_RESPONSE_PROFILE)
        intent.putExtra(RESULT_PROFILE_REQUEST, objectResponseBodyProfile)
        LocalBroadcastManager.getInstance(App.getInstanceApp()).sendBroadcast(intent)
    }

    private fun sendRequestProfile(token: String): ProfileResponseBody? {
        val requestBody = ProfileRequestBody(
            token = token
        )

        val requestBodyString = gson.toJson(requestBody)
        val okHttpRequestBody = requestBodyString.toRequestBody(TYPE_CONTENT.toMediaType())

        var singInResponseBody: ProfileResponseBody? = null

        try {

            val response = client
                .newCall(getRequestProfile(okHttpRequestBody))
                .execute()

            if (response.isSuccessful) {
                val responseBodyString = response.body?.string()
                singInResponseBody = gson.fromJson(
                    responseBodyString,
                    ProfileResponseBody::class.java
                )

            } else {
                Log.e(TAG_INTERRUPTED_EXCEPTION, "${response.code}")
            }
        } catch (ex: IOException) {
            Log.e(
                TAG_INTERRUPTED_EXCEPTION,
                MESSAGE_NO_INTERNET_EXCEPTION, ex
            )
        }

        return singInResponseBody
    }

    private fun getRequestProfile(okHttpRequestBody: RequestBody): Request {
        return Request.Builder()
            .post(okHttpRequestBody)
            .url(URL_PROFILE)
            .build()
    }
}