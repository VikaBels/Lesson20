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
import com.example.lesson20.tasks.LoginTask
import com.example.lesson20.tasks.LoginTask.Companion.BROADCAST_ACTION_RESPONSE_LOGIN
import com.example.lesson20.tasks.LoginTask.Companion.RESULT_LOGIN_REQUEST
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    private var bindingMain: ActivityMainBinding? = null

    private val serverResponseLoginReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val loginResponseBody =
                intent.getParcelableExtra<LoginResponseBody>(RESULT_LOGIN_REQUEST)

            onReceiveResult(loginResponseBody)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bindingMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMain.root)

        this.bindingMain = bindingMain

        setupListeners(bindingMain)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(App.getInstanceApp()).registerReceiver(
            serverResponseLoginReceiver,
            IntentFilter(BROADCAST_ACTION_RESPONSE_LOGIN)
        )
        clearAllFields()
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(App.getInstanceApp())
            .unregisterReceiver(serverResponseLoginReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingMain = null
    }

    private fun onReceiveResult(loginResponseBody: LoginResponseBody?) {
        setVisibleProgressbar(false)
        checkServerResponse(loginResponseBody)
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
            startServerLoginTask()
        }
    }

    private fun startServerLoginTask() {
        val email = getEmail()
        val password = getPassword()

        val isEmailValid = email?.let { isEmailValid(it, bindingMain?.inputLayoutEmail) }

        if (email != null && password != null && isEmailValid == true) {

            val loginTask = LoginTask(email, password)
            loginTask.startTask()

            setVisibleProgressbar(true)
        }
    }

    private fun getEmail(): String? {
        val email = bindingMain?.editTextEmail?.text?.toString()

        changeVisibleErrorLogin(email)

        return if (!email.isNullOrEmpty()) {
            email
        } else null
    }

    private fun changeVisibleErrorLogin(email: String?) {
        bindingMain?.inputLayoutEmail?.error =
            getString(R.string.error_empty_email_field).takeIf { email.isNullOrEmpty() }
    }

    private fun getPassword(): String? {
        val password = bindingMain?.editTextPassword?.text?.toString()

        changeVisibleErrorPassword(password)

        return if (!password.isNullOrEmpty()) {
            password
        } else null
    }

    private fun changeVisibleErrorPassword(password: String?) {
        bindingMain?.inputLayoutPassword?.error =
            getString(R.string.error_empty_password_field).takeIf { password.isNullOrEmpty() }
    }

    private fun setVisibleProgressbar(isVisible: Boolean) {
        bindingMain?.progressBar?.isVisible = isVisible
    }

    private fun isEmailValid(email: CharSequence, inputLayoutEmail: TextInputLayout?): Boolean {
        val isEmailValid: Boolean
        if (PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            isEmailValid = true
        } else {
            inputLayoutEmail?.error =
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
            if (status == KEY_ERROR_EXIST) {
                setVisibleTextError(false)
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
