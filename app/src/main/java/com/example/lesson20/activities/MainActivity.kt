package com.example.lesson20.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lesson20.*
import com.example.lesson20.databinding.ActivityMainBinding
import com.example.lesson20.models.App
import com.example.lesson20.tasks.SendRequestLoginTask
import com.example.lesson20.tasks.SendRequestProfileTask
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    private var bindingMain: ActivityMainBinding? = null

    private val txtViewErrorReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val isErrorRequest = intent.getBooleanExtra(EXTRA_RESULT_TEXT_VIEW, false)
            setVisibleError(isErrorRequest)
        }
    }

    private val progressBarReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val isInvisibleBar = intent.getBooleanExtra(EXTRA_RESULT_PROGRESS_BAR, false)
            setVisibleProgressbar(isInvisibleBar)
        }
    }

    private val tokenReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val currentToken = intent.getStringExtra(EXTRA_RESULT_TOKEN)
            checkToken(currentToken)
        }
    }

    private fun checkToken(token: String?) {
        if (token.isNullOrEmpty()) {
            println("empty")
        } else {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(KEY_FOR_SEND_TOKEN_ACTIVITY, token)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bindingMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMain.root)

        this.bindingMain = bindingMain

        setupListeners(bindingMain)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(App.getInstanceApp()).registerReceiver(
            txtViewErrorReceiver,
            IntentFilter(BROADCAST_ACTION_SHOW_ERROR)
        )
        LocalBroadcastManager.getInstance(App.getInstanceApp()).registerReceiver(
            progressBarReceiver,
            IntentFilter(BROADCAST_ACTION_PROGRESS_BAR)
        )
        LocalBroadcastManager.getInstance(App.getInstanceApp()).registerReceiver(
            tokenReceiver,
            IntentFilter(BROADCAST_ACTION_TOKEN)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(App.getInstanceApp())
            .unregisterReceiver(txtViewErrorReceiver)
        LocalBroadcastManager.getInstance(App.getInstanceApp())
            .unregisterReceiver(progressBarReceiver)
        LocalBroadcastManager.getInstance(App.getInstanceApp())
            .unregisterReceiver(tokenReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingMain = null
    }

    //rename
    private fun setVisibleError(isErrorRequest: Boolean) {
        bindingMain?.textViewError?.isVisible = isErrorRequest
    }

    private fun setupListeners(bindingMain: ActivityMainBinding) {
        bindingMain.buttonLogin.setOnClickListener {
            checkInputData(bindingMain)
        }
    }

    private lateinit var currentEmail: String
    private lateinit var currentPassword: String

    private fun checkInputData(bindingMain: ActivityMainBinding) {
        if (isAllFieldValid(bindingMain) == true) {
            setVisibleProgressbar(true)
            //"jane@domain.tld", "12345"
            startServerTask(currentEmail, currentPassword)
        }
    }

    private fun setVisibleProgressbar(isVisible: Boolean) {
        bindingMain?.progressBar?.isVisible = isVisible
    }

    private fun setErrorOnField(
        fieldIsEmpty: Boolean,
        inputLayout: TextInputLayout,
        errorMessage: String,
    ) {
        if (fieldIsEmpty) {
            inputLayout.error = errorMessage
        } else {
            inputLayout.error = null
        }
    }

    private fun isAllFieldValid(bindingMain: ActivityMainBinding): Boolean? {
        val isEmptyEmailField = bindingMain.editTextEmail.text.isNullOrEmpty()
        val isEmptyPasswordField = bindingMain.editTextPassword.text.isNullOrEmpty()

        var allFieldIsValid: Boolean? = null

        setErrorOnField(
            isEmptyEmailField,
            bindingMain.inputLayoutEmail,
            resources.getString(R.string.error_empty_email_field)
        )

        setErrorOnField(
            isEmptyPasswordField,
            bindingMain.inputLayoutPassword,
            resources.getString(R.string.error_empty_password_field)
        )


        if (!isEmptyEmailField && !isEmptyPasswordField) {
            allFieldIsValid = bindingMain.editTextEmail.text?.let {
                isEmailValid(
                    it,
                    bindingMain.inputLayoutEmail
                )
            }

            //CHANGE!!!!!! - delete toString
            currentEmail = bindingMain.editTextEmail.text.toString()
            currentPassword = bindingMain.editTextPassword.text.toString()
        }

        return allFieldIsValid
    }


    private fun isEmailValid(email: CharSequence, inputLayoutEmail: TextInputLayout): Boolean {
        val isEmailValid: Boolean
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isEmailValid = true
        } else {
            inputLayoutEmail.error =
                resources.getString(R.string.error_not_valid_email_field)
            isEmailValid = false
        }
        return isEmailValid
    }

    //add cancel AsyncTask
    private fun startServerTask(email: String, password: String) {
        val helperAsyncTask = SendRequestLoginTask(email, password)
        helperAsyncTask.execute()
    }

}
