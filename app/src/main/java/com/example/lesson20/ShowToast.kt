package com.example.lesson20

import android.content.Context
import android.widget.Toast

fun showToastNoInternet(context: Context) {
    val textError = context.getString(R.string.error_no_internet)
    val duration = Toast.LENGTH_SHORT

    val toast = Toast.makeText(context, textError, duration)
    toast.show()
}