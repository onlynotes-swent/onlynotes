package com.github.onlynotesswent.model.users

import com.google.firebase.Timestamp

/**
 * Data class representing a user.
 *
 * @property name The name of the user.
 * @property email The email address of the user.
 * @property uid The unique identifier of the user.
 * @property dateOfJoining The date when the user joined.
 * @property rating The rating of the user.
 */
data class User(
    val name: String,
    val email: String,
    val uid: String,
    val dateOfJoining: Timestamp,
    val rating: Double
)
