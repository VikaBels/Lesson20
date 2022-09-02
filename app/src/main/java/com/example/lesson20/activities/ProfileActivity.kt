package com.example.lesson20.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lesson20.KEY_FOR_SEND_TOKEN_ACTIVITY
import com.example.lesson20.databinding.ActivityProfileBinding
import com.example.lesson20.tasks.SendRequestProfileTask

class ProfileActivity : AppCompatActivity() {
    private var bindingProfile: ActivityProfileBinding? = null

    private lateinit var help:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bindingProfile = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindingProfile.root)

        getToken()

        startServerTaskForProfile(help)
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingProfile = null
    }

    private fun getToken(){
        val arguments = intent.extras
        val token = arguments?.get(KEY_FOR_SEND_TOKEN_ACTIVITY).toString()
        help = token
        println("2 activity: $token")
    }

    private fun startServerTaskForProfile(token: String) {
        val helperAsyncTask = SendRequestProfileTask(token)
        helperAsyncTask.execute()
    }
}