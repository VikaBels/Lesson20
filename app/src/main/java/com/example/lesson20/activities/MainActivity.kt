package com.example.lesson20.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.PatternsCompat
import androidx.core.view.isVisible
import bolts.CancellationTokenSource
import bolts.Task
import com.example.lesson20.*
import com.example.lesson20.databinding.ActivityMainBinding
import com.example.lesson20.models.LoginResponseBody
import com.example.lesson20.tasks.LoginTask
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {
    private var bindingMain: ActivityMainBinding? = null

    private val cancellationTokenSourceMain: CancellationTokenSource = CancellationTokenSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bindingMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMain.root)

        this.bindingMain = bindingMain

        setupListeners(bindingMain)
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingMain = null
        cancellationTokenSourceMain.cancel()
    }

    private fun onReceiveResult(loginResponseBody: LoginResponseBody?) {
        setVisibleProgressbar(false)
        checkServerResponse(loginResponseBody)
    }

    private fun setDefaultValues() {
        bindingMain?.apply {
            textViewError.isVisible = false

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
        val email = getValidEmail()
        val password = getValidPassword()

        if (email != null && password != null) {

            val loginTask = LoginTask()

            loginTask.startTask(cancellationTokenSourceMain.token, email, password)
                .continueWith({

                    val loginResponseBody = it.result ?: LoginResponseBody(KEY_ERROR_EXIST, null)
                    onReceiveResult(loginResponseBody)

                    if (it.error != null) {
                        showToastError(getTextError(it))
                    }

                }, Task.UI_THREAD_EXECUTOR)

            setVisibleProgressbar(true)
        }
    }

    private fun showToastError(textError: String?) {
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(this, textError, duration)
        toast.show()
    }

    private fun getValidEmail(): String? {
        val email = bindingMain?.editTextEmail?.text?.toString()

        return when {
            email.isNullOrEmpty() -> {
                showEmailError(R.string.error_empty_email_field)
                null
            }
            !isMatchesEmailPattern(email) -> {
                showEmailError(R.string.error_not_valid_email_field)
                null
            }
            else -> email
        }
    }

    private fun isMatchesEmailPattern(email: String): Boolean {
        return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showEmailError(idResource: Int) {
        bindingMain?.inputLayoutEmail?.error =
            resources.getString(idResource)
    }

    private fun getValidPassword(): String? {
        val password = bindingMain?.editTextPassword?.text?.toString()

        return when {
            password.isNullOrEmpty() -> {
                showPasswordError(R.string.error_empty_password_field)
                null
            }
            else -> password
        }
    }

    private fun showPasswordError(idResource: Int) {
        bindingMain?.inputLayoutPassword?.error =
            getString(idResource)
    }

    private fun getTextError(loginResponseBody: Task<LoginResponseBody?>): String? {
        val textError = when (loginResponseBody.error) {
            is UnknownHostException -> {
                resources.getString(R.string.error_no_internet)
            }
            is SocketTimeoutException -> {
                resources.getString(R.string.error_problem_with_socket)
            }
            else -> {
                loginResponseBody.error.message
            }
        }
        return textError
    }

    private fun setVisibleProgressbar(isVisible: Boolean) {
        bindingMain?.progressBar?.isVisible = isVisible
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
        setDefaultValues()
    }
}
