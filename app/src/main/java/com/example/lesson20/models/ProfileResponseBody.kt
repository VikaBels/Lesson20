package com.example.lesson20.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProfileResponseBody(
    val firstName: String,
    val lastName: String,
    val birthDate: Long,
    val notes: String,
) : Parcelable