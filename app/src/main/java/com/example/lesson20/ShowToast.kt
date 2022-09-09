package com.example.lesson20

import android.content.Context
import android.widget.Toast

fun showErrorToast(context: Context, idResource: Int) {
    val textError = context.getString(idResource)
    val duration = Toast.LENGTH_SHORT

    val toast = Toast.makeText(context, textError, duration)
    toast.show()
}
