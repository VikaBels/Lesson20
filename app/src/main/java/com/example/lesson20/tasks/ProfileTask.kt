package com.example.lesson20.tasks

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import bolts.Task
import com.example.lesson20.*
import com.example.lesson20.models.App
import com.example.lesson20.models.ProfileRequestBody
import com.example.lesson20.models.ProfileResponseBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileTask(
    private val token: String
) {
    companion object {
        private const val URL_PROFILE =
            "https://pub.zame-dev.org/senla-training-addition/lesson-21.php?method=profile"

        const val RESULT_PROFILE_REQUEST = "RESULT_PROFILE_REQUEST"
        const val BROADCAST_ACTION_RESPONSE_PROFILE =
            "SendRequestProfileTask.BROADCAST_ACTION_RESPONSE_PROFILE"
    }

    private fun getProfileResponseBody(): ProfileResponseBody? {
        var objectResponseBodyProfile: ProfileResponseBody? = null

        try {
            objectResponseBodyProfile = sendRequestProfile(token)
        } catch (ex: InterruptedException) {
            Log.e(TAG_INTERRUPTED_EXCEPTION, MESSAGE_INTERRUPTED_EXCEPTION, ex)
        }

        return objectResponseBodyProfile
    }

    private fun getResponseBody(result: Task<ProfileResponseBody?>?): ProfileResponseBody? {
        val responseBody = result?.result
        sendBroadcastPersonInfo(responseBody)
        return responseBody
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

        val requestBodyString = App.getGson().toJson(requestBody)
        val okHttpRequestBody = requestBodyString.toRequestBody(TYPE_CONTENT.toMediaType())

        var singInResponseBody: ProfileResponseBody? = null


        val response = App.getClient()
            .newCall(getRequest(okHttpRequestBody, URL_PROFILE))
            .execute()

        if (response.isSuccessful) {
            val responseBodyString = response.body?.string()

            if (!responseBodyString.isNullOrEmpty()) {
                singInResponseBody = App.getGson().fromJson(
                    responseBodyString,
                    ProfileResponseBody::class.java
                )
            }

        } else {
            Log.e(TAG_INTERRUPTED_EXCEPTION, "${response.code}")
        }

        return singInResponseBody
    }

    fun startTask() {
        Task.callInBackground {
            getProfileResponseBody()
        }.onSuccess({
            getResponseBody(it)
        }, Task.UI_THREAD_EXECUTOR)
            .continueWith({

                if (it.error != null) {
                    getResponseBody(null)
                    showErrorToast(App.getInstanceApp(), getTextError(it))
                }

            }, Task.UI_THREAD_EXECUTOR)
    }

    private fun getTextError(profileResponseBody: Task<ProfileResponseBody?>): String? {
        val textError = when (profileResponseBody.error.javaClass.name) {
            ERROR_TYPE_UNKNOWN_HOST_EXCEPTION -> {
                MESSAGE_NO_INTERNET_EXCEPTION
            }
            ERROR_TYPE_SOCKET_TIMEOUT_EXCEPTION -> {
                MESSAGE_PROBLEM_WITH_SOCKET
            }
            else -> {
                profileResponseBody.error.message
            }
        }
        return textError
    }
}