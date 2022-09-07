package com.example.lesson20.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.PatternsCompat
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lesson20.*
import com.example.lesson20.databinding.ActivityMainBinding
import com.example.lesson20.models.App
import com.example.lesson20.models.LoginResponseBody
import com.example.lesson20.tasks.SendRequestLoginTask
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    private var bindingMain: ActivityMainBinding? = null
    private var loginAsyncTask: SendRequestLoginTask? = null

    private val serverResponseLoginReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val loginResponseBody =
                intent.getParcelableExtra<LoginResponseBody>(RESULT_LOGIN_REQUEST)

            setVisibleProgressbar(false)

            checkServerResponse(loginResponseBody)
        }
    }

    override fun onStart() {
        super.onStart()
        clearAllFields()
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
            serverResponseLoginReceiver,
            IntentFilter(BROADCAST_ACTION_RESPONSE_LOGIN)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(App.getInstanceApp())
            .unregisterReceiver(serverResponseLoginReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingMain = null
        loginAsyncTask?.cancel(false)
    }

    private fun clearAllFields() {
        bindingMain?.apply {
            textViewError.isVisible = false

            editTextEmail.text = null
            editTextPassword.text = null

            editTextPassword.clearFocus()
            editTextEmail.clearFocus()
        }
    }

    private fun setupListeners(bindingMain: ActivityMainBinding) {
        bindingMain.buttonLogin.setOnClickListener {
            checkInputData(bindingMain)
        }
    }

    private fun checkInputData(bindingMain: ActivityMainBinding) {
        if (isAllFieldValid(bindingMain) == true) {
            setVisibleProgressbar(true)
            startServerLoginTask()
        }
    }

    private fun getEmail(): String? {
        val email = bindingMain?.editTextEmail?.text?.toString()
        return if (!email.isNullOrEmpty()) {
            email
        } else null
    }

    private fun getPassword(): String? {
        val password = bindingMain?.editTextPassword?.text?.toString()
        return if (!password.isNullOrEmpty()) {
            password
        } else null
    }

    private fun startServerLoginTask() {
        val email = getEmail()
        val password = getPassword()

        if (email != null && password != null) {
            loginAsyncTask = SendRequestLoginTask(email, password)
            loginAsyncTask?.execute()
        }
    }

    private fun setVisibleProgressbar(isVisible: Boolean) {
        bindingMain?.progressBar?.isVisible = isVisible
    }

    private fun isAllFieldValid(bindingMain: ActivityMainBinding): Boolean? {
        val email = bindingMain.editTextEmail.text
        val password = bindingMain.editTextPassword.text

        var allFieldIsValid: Boolean? = null

        bindingMain.inputLayoutEmail.error =
            getString(R.string.error_empty_email_field).takeIf { email.isNullOrEmpty() }
        bindingMain.inputLayoutPassword.error =
            getString(R.string.error_empty_password_field).takeIf { password.isNullOrEmpty() }

        if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            allFieldIsValid = isEmailValid(email, bindingMain.inputLayoutEmail)
        }

        return allFieldIsValid
    }

    private fun isEmailValid(email: CharSequence, inputLayoutEmail: TextInputLayout): Boolean {
        val isEmailValid: Boolean
        if (PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            isEmailValid = true
        } else {
            inputLayoutEmail.error =
                resources.getString(R.string.error_not_valid_email_field)
            isEmailValid = false
        }
        return isEmailValid
    }

    private fun checkServerResponse(loginResponseBody: LoginResponseBody?) {
        val token = loginResponseBody?.token
        val status = loginResponseBody?.status

        if (token != null) {
            startProfileActivity(token)
        } else {
            if (status == ERROR_STATUS) {
                setVisibleTextError(false)
                showToastNoInternet(this)
            } else {
                setVisibleTextError(true)
            }
        }
    }

    private fun setVisibleTextError(isVisible: Boolean) {
        bindingMain?.textViewError?.isVisible = isVisible
    }

    private fun startProfileActivity(token: String) {
        val intent = Intent(this, ProfileActivity::class.java)
        val email = bindingMain?.editTextEmail?.text?.toString()

        intent.apply {
            putExtra(KEY_FOR_SEND_TOKEN, token)
            putExtra(KEY_FOR_SEND_EMAIL, email)
        }

        startActivity(intent)
    }
}
