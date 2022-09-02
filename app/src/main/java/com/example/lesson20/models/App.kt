package com.example.lesson20.models

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        self = this
    }

    companion object {
        private lateinit var self: App

        fun getInstanceApp(): App {
            return self
        }
    }
}