package com.example.lesson20

import android.app.Application
import com.google.gson.Gson
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.*

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        self = this
    }

    companion object {
        private lateinit var self: App

        private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

        private val gson = Gson()
        private val client = OkHttpClient
            .Builder()
            .build()

        fun getInstanceApp(): App {
            return self
        }

        fun getDateFormat(): SimpleDateFormat {
            return dateFormat
        }

        fun getGson(): Gson {
            return gson
        }

        fun getClient(): OkHttpClient {
            return client
        }
    }
}