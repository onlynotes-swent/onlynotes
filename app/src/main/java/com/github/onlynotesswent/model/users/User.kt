package com.github.onlynotesswent.model.users

import com.google.firebase.Timestamp

/**
 * Data class representing a user.
 *
 * @property firstName The first name of the user.
 * @property lastName The last name of the user.
 * @property userName The username of the user.
 * @property email The email address of the user.
 * @property uid The unique identifier of the user.
 * @property dateOfJoining The date when the user joined.
 * @property rating The rating of the user.
 */
data class User(
    val firstName: String,
    val lastName: String,
    val userName: String,
    val email: String,
    val uid: String,
    val dateOfJoining: Timestamp,
    val rating: Double,
    val profilePicture: String= "",
    )
