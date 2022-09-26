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
import com.example.lesson20.tasks.ProfileRepository
import com.example.lesson20.utils.getTextError
import com.example.lesson20.utils.showToastError

class ProfileActivity : AppCompatActivity() {
    companion object {
        const val KEY_PROFILE_RESPONSE_BODY = "KEY_PROFILE_RESPONSE_BODY"
    }

    private var bindingProfile: ActivityProfileBinding? = null
    private var profileResponseBody: ProfileResponseBody? = null

    private var email: String? = null
    private var token: String? = null

    private val cancellationTokenSourceProfile: CancellationTokenSource = CancellationTokenSource()
    private val profileRepository = ProfileRepository(App.getGson(), App.getClient())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bindingProfile = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindingProfile.root)

        this.bindingProfile = bindingProfile

        setTransmissionData()
        checkTransmissionData()

        setupListeners(bindingProfile)

        if (!isInfoExist(savedInstanceState)) {
            setVisibleProgressbar(true)
            startServerProfileRepository()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (profileResponseBody != null) {
            outState.putParcelable(KEY_PROFILE_RESPONSE_BODY, profileResponseBody)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingProfile = null
        cancellationTokenSourceProfile.cancel()
    }

    private fun setTransmissionData() {
        email = intent.extras?.getString(KEY_FOR_SEND_EMAIL).orEmpty()
        token = intent.extras?.getString(KEY_FOR_SEND_TOKEN)
    }

    private fun checkTransmissionData() {
        if (email.isNullOrEmpty() || token.isNullOrEmpty()) {
            showToastError(resources.getString(R.string.error_transmission), this)
        }
    }

    private fun isInfoExist(savedInstanceState: Bundle?): Boolean {
        val profileResponseBody =
            savedInstanceState?.getParcelable<ProfileResponseBody>(KEY_PROFILE_RESPONSE_BODY)

        return isProfileInfoExist(profileResponseBody)
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

    private fun onReceiveResult(profileResponseBody: ProfileResponseBody) {
        this.profileResponseBody = profileResponseBody
        changeVisibleElements()
        setInfoAboutPerson(profileResponseBody)
    }

    private fun setupListeners(bindingProfile: ActivityProfileBinding) {
        bindingProfile.buttonLogout.setOnClickListener {
            finish()
        }
    }

    private fun startServerProfileRepository() {
        val token = this.token

        if (!token.isNullOrEmpty()) {

            profileRepository.startTask(cancellationTokenSourceProfile.token, token)
                .continueWith({

                    when {
                        it.result != null -> {
                            onReceiveResult(it.result)
                        }
                        it.error != null -> {
                            setTextError(getTextError(it.error))
                        }
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

    private fun setInfoAboutPerson(responseBody: ProfileResponseBody) {
        bindingProfile?.textViewEmail?.text =
            getString(R.string.txt_view_email, this.email)

        bindingProfile?.textViewFirstName?.text =
            getString(R.string.txt_view_first_name, responseBody.firstName)

        bindingProfile?.textViewLastName?.text =
            getString(R.string.txt_view_last_name, responseBody.lastName)

        bindingProfile?.textViewBirthDate?.text =
            getString(R.string.txt_view_birth_data, getFormattedDate(responseBody))

        bindingProfile?.textViewNotes?.text =
            getString(R.string.txt_view_notes, responseBody.notes)
    }

    private fun getFormattedDate(responseBody: ProfileResponseBody): String {
        return getDateFormat().format(responseBody.birthDate)
    }
}