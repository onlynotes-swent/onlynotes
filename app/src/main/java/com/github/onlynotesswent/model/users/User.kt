package com.github.onlynotesswent.model.users

import com.google.firebase.Timestamp


data class User(
    val name: String,
    val email: String,
    val uid: String,
    val dateOfJoining: Timestamp,
    val rating: Double
)


