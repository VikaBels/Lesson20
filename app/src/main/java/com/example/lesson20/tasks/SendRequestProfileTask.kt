package com.example.lesson20.tasks

import android.os.AsyncTask
import android.util.Log
import com.example.lesson20.MESSAGE_INTERRUPTED_EXCEPTION
import com.example.lesson20.MESSAGE_NO_INTERNET_EXCEPTION
import com.example.lesson20.TAG_INTERRUPTED_EXCEPTION
import com.example.lesson20.TYPE_CONTENT
import com.example.lesson20.models.SingInRequestBodyProfile
import com.example.lesson20.models.SingInResponseBodyProfile
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SendRequestProfileTask(
    private val token: String
) : AsyncTask<Void?, String?, Void?>() {
    companion object {
        const val URL_PROFILE =
            "https://pub.zame-dev.org/senla-training-addition/lesson-20.php?method=profile"
    }

    private val gson = Gson()

    private val client = OkHttpClient
        .Builder()
        .build()

    override fun onProgressUpdate(vararg values: String?) {
        super.onProgressUpdate(*values)
    }

    override fun doInBackground(vararg params: Void?): Void? {
        try {
            //CHANGE TOKEN!!!
            sendRequestProfile(token)
        } catch (ex: InterruptedException) {
            Log.e(TAG_INTERRUPTED_EXCEPTION, MESSAGE_INTERRUPTED_EXCEPTION, ex)
        }
        return null
    }

    private fun sendRequestProfile(token: String) {
        println("token: $token")

        val requestBody = SingInRequestBodyProfile(
            token = token
        )

        val requestBodyString = gson.toJson(requestBody)
        val okHttpRequestBody = requestBodyString.toRequestBody(TYPE_CONTENT.toMediaType())

        val response = client
            .newCall(getRequestProfile(okHttpRequestBody))
            .execute()

        try {
            if (response.isSuccessful) {
                val responseBodyString = response.body?.string()
                val singInResponseBody = gson.fromJson(
                    responseBodyString,
                    SingInResponseBodyProfile::class.java
                )

                println("firstName:${singInResponseBody.firstName}")
                println("lastName:${singInResponseBody.lastName}")
                println("birthDate:${singInResponseBody.birthDate}")
                println("notes:${singInResponseBody.notes}")

            } else {
                Log.e(TAG_INTERRUPTED_EXCEPTION, "${response.code}")
            }
        } catch (ex: IOException) {
            Log.e(
                TAG_INTERRUPTED_EXCEPTION,
                MESSAGE_NO_INTERNET_EXCEPTION, ex
            )
        }
    }

    private fun getRequestProfile(okHttpRequestBody: RequestBody): Request {
        return Request.Builder()
            .post(okHttpRequestBody)
            .url(URL_PROFILE)
            .build()
    }
}