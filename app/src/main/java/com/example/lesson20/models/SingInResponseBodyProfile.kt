package com.example.lesson20.models

//change type Data
data class SingInResponseBodyProfile(
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val notes: String,
)