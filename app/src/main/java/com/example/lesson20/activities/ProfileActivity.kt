package com.example.lesson20.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import bolts.CancellationTokenSource
import bolts.Task
import com.example.lesson20.*
import com.example.lesson20.databinding.ActivityProfileBinding
import com.example.lesson20.App.Companion.getDateFormat
import com.example.lesson20.models.ProfileResponseBody
import com.example.lesson20.tasks.ProfileTask
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ProfileActivity : AppCompatActivity() {
    private var bindingProfile: ActivityProfileBinding? = null
    private var arguments: Bundle? = null

    private val cancellationTokenSourceProfile: CancellationTokenSource = CancellationTokenSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bindingProfile = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindingProfile.root)

        this.bindingProfile = bindingProfile
        arguments = intent.extras

        setupListeners(bindingProfile)

        startServerProfileTask()

        setVisibleProgressbar(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingProfile = null
        cancellationTokenSourceProfile.cancel()
    }

    private fun onReceiveResult(profileResponseBody: ProfileResponseBody?) {
        changeVisibleElements()
        setInfoAboutPerson(profileResponseBody)
    }

    private fun setupListeners(bindingProfile: ActivityProfileBinding) {
        bindingProfile.buttonLogout.setOnClickListener {
            finish()
        }
    }

    private fun startServerProfileTask() {
        val token = getSendingInfo(KEY_FOR_SEND_TOKEN)
        if (!token.isNullOrEmpty()) {

            val profileTask = ProfileTask()

            profileTask.startTask(cancellationTokenSourceProfile.token, token)
                .continueWith({

                    onReceiveResult(it?.result)

                    if (it.error != null) {
                        setTextError(getTextError(it))
                    }

                }, Task.UI_THREAD_EXECUTOR)

        } else {
            setTextError(resources.getString(R.string.error_unexpected))
        }
    }

    private fun getTextError(profileResponseBody: Task<ProfileResponseBody?>): String? {
        val textError = when (profileResponseBody.error) {
            is UnknownHostException -> {
                resources.getString(R.string.error_no_internet)
            }
            is SocketTimeoutException -> {
                resources.getString(R.string.error_problem_with_socket)
            }
            else -> {
                profileResponseBody.error.message
            }
        }
        return textError
    }

    private fun setTextError(textError: String?) {
        bindingProfile?.textViewError?.text = textError
    }

    private fun getSendingInfo(key: String): String? {
        return arguments?.getString(key)
    }

    private fun changeVisibleElements() {
        setVisibleProgressbar(false)
        setVisibleButton(true)
    }

    private fun setVisibleProgressbar(isVisible: Boolean) {
        bindingProfile?.progressBar?.isVisible = isVisible
    }

    private fun setVisibleButton(isVisible: Boolean) {
        bindingProfile?.buttonLogout?.isVisible = isVisible
    }

    private fun getValidEmail(): String {
        return getSendingInfo(KEY_FOR_SEND_EMAIL).orEmpty()
    }

    private fun setInfoAboutPerson(responseBody: ProfileResponseBody?) {
        if (responseBody != null) {
            bindingProfile?.textViewEmail?.text =
                getString(R.string.txt_view_email, getValidEmail())

            bindingProfile?.textViewFirstName?.text =
                getString(R.string.txt_view_first_name, responseBody.firstName)

            bindingProfile?.textViewLastName?.text =
                getString(R.string.txt_view_last_name, responseBody.lastName)

            bindingProfile?.textViewBirthDate?.text =
                getString(R.string.txt_view_birth_data, getFormattedDate(responseBody))

            bindingProfile?.textViewNotes?.text =
                getString(R.string.txt_view_notes, responseBody.notes)
        }
    }

    private fun getFormattedDate(responseBody: ProfileResponseBody): String {
        return getDateFormat().format(responseBody.birthDate)
    }
}