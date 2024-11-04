package com.github.onlynotesswent.model.users

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Data class representing a user.
 *
 * @property firstName The first name of the user.
 * @property lastName The last name of the user.
 * @property userName The unique username of the user, can be modified.
 * @property email The email address of the user.
 * @property uid The unique identifier of the user, cannot be modified.
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
    val rating: Double = 0.0,
    val friends: Friends = Friends(),
    val bio: String = ""
) {
  fun userHandle() = "@${userName.ifEmpty { "unknown" }}"

  fun fullName() = "$firstName $lastName"

  fun dateToString(): String =
      SimpleDateFormat("d/M/yyyy", Locale.FRANCE).format(dateOfJoining.toDate())
}

/**
 * Data class representing a user's friends (following and followers), represented by lists of UIDs.
 *
 * @constructor Creates a Friends object with empty lists.
 * @property following The list of users that the user is following.
 * @property followers The list of users that are following the user.
 */
data class Friends(
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList()
)
