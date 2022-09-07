package com.example.lesson20.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lesson20.*
import com.example.lesson20.databinding.ActivityProfileBinding
import com.example.lesson20.models.App
import com.example.lesson20.models.ProfileResponseBody
import com.example.lesson20.tasks.SendRequestProfileTask
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private var bindingProfile: ActivityProfileBinding? = null
    private var profileAsyncTask: SendRequestProfileTask? = null

    private val serverResponseProfileReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val profileResponseBody =
                intent.getParcelableExtra<ProfileResponseBody>(RESULT_PROFILE_REQUEST)

            changeVisibleElements()

            setInfoAboutPerson(profileResponseBody)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startServerProfileTask()

        val bindingProfile = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindingProfile.root)

        this.bindingProfile = bindingProfile

        setScrollableNotes(bindingProfile)

        setupListeners(bindingProfile)

        setVisibleProgressbar(true)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(App.getInstanceApp()).registerReceiver(
            serverResponseProfileReceiver,
            IntentFilter(BROADCAST_ACTION_RESPONSE_PROFILE)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(App.getInstanceApp())
            .unregisterReceiver(serverResponseProfileReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingProfile = null
        profileAsyncTask?.cancel(false)
    }

    private fun setScrollableNotes(bindingProfile: ActivityProfileBinding) {
        bindingProfile.textViewNotes.movementMethod = ScrollingMovementMethod()
    }

    private fun setupListeners(bindingProfile: ActivityProfileBinding) {
        bindingProfile.buttonLogout.setOnClickListener {
            finish()
        }
    }

    private fun startServerProfileTask() {
        val token = getSendingInfo(KEY_FOR_SEND_TOKEN)
        profileAsyncTask = SendRequestProfileTask(token)
        profileAsyncTask?.execute()
    }

    private fun getSendingInfo(key: String): String {
        val arguments = intent.extras
        return arguments?.get(key).toString()
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

    private fun setInfoAboutPerson(responseBody: ProfileResponseBody?) {
        if (responseBody != null) {
            bindingProfile?.textViewEmail?.text =
                refactorLine(getSendingInfo(KEY_FOR_SEND_EMAIL), R.string.txt_view_email)

            bindingProfile?.textViewFirstName?.text =
                refactorLine(responseBody.firstName, R.string.txt_view_first_name)

            bindingProfile?.textViewLastName?.text =
                refactorLine(responseBody.lastName, R.string.txt_view_last_name)

            bindingProfile?.textViewBirthDate?.text =
                refactorLine(getFormattedDate(responseBody), R.string.txt_view_birth_data)

            bindingProfile?.textViewNotes?.text =
                refactorLine(responseBody.notes, R.string.txt_view_notes)
        } else {
            setVisibleProgressbar(false)
            showToastNoInternet(this)
        }
    }

    private fun refactorLine(text: String?, idResource: Int): String {
        return "${resources.getString(idResource)}$text"
    }

    private fun getFormattedDate(responseBody: ProfileResponseBody): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(responseBody.birthDate)
    }
}