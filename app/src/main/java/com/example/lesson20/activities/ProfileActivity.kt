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
import com.example.lesson20.utils.getTextError

class ProfileActivity : AppCompatActivity() {
    companion object {
        const val PROFILE_RESPONSE_BODY_KEY = "PROFILE_RESPONSE_BODY_KEY"
    }

    private var bindingProfile: ActivityProfileBinding? = null
    private var arguments: Bundle? = null
    private var profileResponseBody: ProfileResponseBody? = null

    private val cancellationTokenSourceProfile: CancellationTokenSource = CancellationTokenSource()
    private val profileTask = ProfileTask(App.getGson(), App.getClient())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bindingProfile = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindingProfile.root)

        this.bindingProfile = bindingProfile
        arguments = intent.extras

        setupListeners(bindingProfile)

        if (!isInstanceStateInfoExist(savedInstanceState)) {
            setVisibleProgressbar(true)
            startServerProfileTask()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (profileResponseBody != null) {
            outState.putParcelable(PROFILE_RESPONSE_BODY_KEY, profileResponseBody)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingProfile = null
        cancellationTokenSourceProfile.cancel()
    }

    private fun isInstanceStateInfoExist(savedInstanceState: Bundle?): Boolean {
        return if (savedInstanceState != null && savedInstanceState.containsKey(PROFILE_RESPONSE_BODY_KEY)) {
            val profileResponseBody =
                savedInstanceState.getParcelable<ProfileResponseBody>(PROFILE_RESPONSE_BODY_KEY)

            isProfileInfoExist(profileResponseBody)
        } else false
    }

    private fun isProfileInfoExist(profileResponseBody: ProfileResponseBody?): Boolean {
        return if (profileResponseBody != null) {
            saveProfileResponseBody(profileResponseBody)
            onReceiveResult(profileResponseBody)
            true
        } else false
    }

    private fun saveProfileResponseBody(profileResponseBody: ProfileResponseBody) {
        this.profileResponseBody = profileResponseBody
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

            profileTask.startTask(cancellationTokenSourceProfile.token, token)
                .continueWith({

                    onReceiveResult(it.result)
                    profileResponseBody = it.result

                    if (it.error != null) {
                        setTextError(getTextError(it.error))
                    }

                }, Task.UI_THREAD_EXECUTOR)

        } else {
            setVisibleProgressbar(false)
            setTextError(resources.getString(R.string.error_unexpected))
        }
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

    private fun getEmail(): String {
        return getSendingInfo(KEY_FOR_SEND_EMAIL).orEmpty()
    }

    private fun setInfoAboutPerson(responseBody: ProfileResponseBody?) {
        if (responseBody != null) {
            bindingProfile?.textViewEmail?.text =
                getString(R.string.txt_view_email, getEmail())

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