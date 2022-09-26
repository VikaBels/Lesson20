package com.example.lesson20.utils

import android.content.Context
import android.widget.Toast

fun showToastError(textError: String?,context: Context) {
    val duration = Toast.LENGTH_SHORT
    val toast = Toast.makeText(context, textError, duration)
    toast.show()
}