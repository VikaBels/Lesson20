package com.example.lesson20.tasks

import android.util.Log
import bolts.CancellationToken
import bolts.Task
import com.example.lesson20.*
import com.example.lesson20.models.ProfileRequestBody
import com.example.lesson20.models.ProfileResponseBody
import com.example.lesson20.utils.getRequest
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileTask(
    private val gson: Gson,
    private val client: OkHttpClient
) {
    companion object {
        private const val URL_PROFILE =
            "https://pub.zame-dev.org/senla-training-addition/lesson-21.php?method=profile"
    }

    private fun getProfileResponseBody(token: String): ProfileResponseBody? {
        var objectResponseBodyProfile: ProfileResponseBody? = null

        try {
            objectResponseBodyProfile = sendRequestProfile(token)
        } catch (ex: InterruptedException) {
            Log.e(TAG_INTERRUPTED_EXCEPTION, MESSAGE_INTERRUPTED_EXCEPTION, ex)
        }

        return objectResponseBodyProfile
    }

    private fun sendRequestProfile(token: String): ProfileResponseBody? {
        val requestBody = ProfileRequestBody(
            token = token
        )

        val requestBodyString = gson.toJson(requestBody)
        val okHttpRequestBody = requestBodyString.toRequestBody(TYPE_CONTENT.toMediaType())

        var singInResponseBody: ProfileResponseBody? = null

        val response = client
            .newCall(getRequest(okHttpRequestBody, URL_PROFILE))
            .execute()

        if (response.isSuccessful) {
            val responseBodyString = response.body?.string()

            if (!responseBodyString.isNullOrEmpty()) {
                singInResponseBody = gson.fromJson(
                    responseBodyString,
                    ProfileResponseBody::class.java
                )
            }

        } else {
            Log.e(TAG_INTERRUPTED_EXCEPTION, "${response.code}")
        }

        return singInResponseBody
    }

    fun startTask(cancellationToken: CancellationToken, token: String): Task<ProfileResponseBody> {
        return Task
            .callInBackground({
                getProfileResponseBody(token)
            }, cancellationToken)
    }
}