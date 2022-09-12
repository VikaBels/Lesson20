package com.example.lesson20.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lesson20.*
import com.example.lesson20.databinding.ActivityProfileBinding
import com.example.lesson20.models.App
import com.example.lesson20.models.App.Companion.getDateFormat
import com.example.lesson20.models.ProfileResponseBody
import com.example.lesson20.tasks.ProfileTask
import com.example.lesson20.tasks.ProfileTask.Companion.BROADCAST_ACTION_RESPONSE_PROFILE
import com.example.lesson20.tasks.ProfileTask.Companion.RESULT_PROFILE_REQUEST

class ProfileActivity : AppCompatActivity() {
    private var bindingProfile: ActivityProfileBinding? = null

    private val serverResponseProfileReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val profileResponseBody =
                intent.getParcelableExtra<ProfileResponseBody>(RESULT_PROFILE_REQUEST)

            onReceiveResult(profileResponseBody)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startServerProfileTask()

        val bindingProfile = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindingProfile.root)

        this.bindingProfile = bindingProfile

        setupListeners(bindingProfile)

        setVisibleProgressbar(true)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(App.getInstanceApp()).registerReceiver(
            serverResponseProfileReceiver,
            IntentFilter(BROADCAST_ACTION_RESPONSE_PROFILE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(App.getInstanceApp())
            .unregisterReceiver(serverResponseProfileReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingProfile = null
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
            //top-level fun????
            val profileTask = ProfileTask(token)
            profileTask.startTask()

        } else {
            showErrorToast(this, resources.getString(R.string.error_unexpected))
        }
    }

    private fun getSendingInfo(key: String): String? {
        val arguments = intent.extras
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

    private fun getValidEmail(): String? {
        val email: String? = if (!getSendingInfo(KEY_FOR_SEND_EMAIL).isNullOrEmpty()) {
            getSendingInfo(KEY_FOR_SEND_EMAIL)
        } else {
            ""
        }
        return email
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