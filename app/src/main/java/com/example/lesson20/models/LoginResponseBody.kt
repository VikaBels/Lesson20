package com.example.lesson20.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LoginResponseBody(
    val status: String,
    val token: String?,
) : Parcelable