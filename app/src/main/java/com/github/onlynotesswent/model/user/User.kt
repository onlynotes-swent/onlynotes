package com.github.onlynotesswent.model.user

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Locale

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
 * @property friends The user's friends.
 * @property hasProfilePicture A flag indicating if the user has a profile picture.
 * @property bio The user's biography.
 */
data class User(
    val firstName: String,
    val lastName: String,
    val userName: String,
    val email: String,
    val uid: String,
    val dateOfJoining: Timestamp = Timestamp.now(),
    val rating: Double = 0.0,
    val friends: Friends = Friends(),
    val hasProfilePicture: Boolean = false,
    val bio: String = "",
    val pendingFriends: Friends = Friends(),
    // for now it's set to true, will be set to false when the notification feature is
    // implemented
    @get:PropertyName("isAccountPublic") val isAccountPublic: Boolean = false
) {
  /**
   * Returns the user's handle, which is the username prefixed with an '@' symbol.
   *
   * @return The user's handle.
   */
  fun userHandle() = "@${userName.ifEmpty { "unknown" }}"

  /**
   * Returns the user's full name, which is the concatenation of the first and last names separated
   * by a space.
   *
   * @return The user's full name.
   */
  fun fullName() = "$firstName $lastName"

  /**
   * Returns the user's date of joining as a formatted string.
   *
   * @param pattern The pattern to use for formatting the date. Defaults to "d/M/yyyy".
   * @return The user's date of joining as a formatted string.
   */
  fun dateToString(pattern: String = "d/M/yyyy"): String =
      SimpleDateFormat(pattern, Locale.getDefault()).format(dateOfJoining.toDate())

  companion object {
    // username max length
    private const val USERNAME_MAX_LENGTH = 20
    // name max length
    private const val NAME_MAX_LENGTH = 40
    // bio max length
    private const val BIO_MAX_LENGTH = 140

    /**
     * Formats the username by trimming leading and trailing whitespace, removing special
     * characters, and taking the first 20 characters.
     *
     * @param username The username.
     * @return The formatted username.
     */
    fun formatUsername(username: String): String {
      return username.trim().replace(Regex("[^a-zA-Z0-9_-]"), "").take(USERNAME_MAX_LENGTH)
    }
    /**
     * Formats the user's name by trimming leading whitespace, converting the first character of
     * each word to uppercase, and taking the first 40 characters.
     *
     * @param name The user's name.
     * @return The formatted name.
     */
    fun formatName(name: String): String {
      return name
          .trimStart()
          .split(Regex("\\s+"))
          .joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercaseChar() } }
          .take(NAME_MAX_LENGTH)
    }
    /**
     * Formats the user's biography by trimming leading whitespace and taking the first 200
     * characters.
     *
     * @param bio The user's biography.
     * @return The formatted biography.
     */
    fun formatBio(bio: String): String {
      return bio.trimStart().take(BIO_MAX_LENGTH)
    }
  }
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
