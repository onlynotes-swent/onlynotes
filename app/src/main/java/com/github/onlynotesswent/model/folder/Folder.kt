package com.github.onlynotesswent.model.folder

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.user.User
import com.google.firebase.Timestamp

/**
 * Represents a folder that contains notes.
 *
 * @param id The Id of the folder.
 * @param name The name of the folder.
 * @param userId The Id of the user that owns the folder.
 * @param parentFolderId The Id of the parent folder. Has default value null.
 * @param visibility The visibility of the folder. Has default value Visibility.DEFAULT.
 * @param isDeckFolder A flag indicating if the folder is a deck folder. Has default value false.
 * @param lastModified The timestamp of when the folder was last modified.
 */
@Entity
data class Folder(
    @PrimaryKey val id: String,
    val name: String,
    val userId: String,
    val parentFolderId: String? = null,
    val visibility: Visibility = Visibility.DEFAULT,
    val isDeckFolder: Boolean = false,
    val lastModified: Timestamp
) {
  /**
   * Checks if the folder is owned by the user with the given Id.
   *
   * @param uid The Id of the user to check.
   * @return True if the folder is owned by the user, false otherwise.
   */
  fun isOwner(uid: String): Boolean {
    return userId == uid
  }

  /**
   * Checks if the folder is visible to the given user. This is the case if the user is the owner,
   * if the folder is public, or if it is shared with friends and the user is following the owner.
   *
   * @param user The user to check.
   * @return True if the folder is visible to the user, false otherwise.
   */
  fun isVisibleTo(user: User): Boolean {
    return isOwner(user.uid) ||
        visibility == Visibility.PUBLIC ||
        (visibility == Visibility.FRIENDS && userId in user.friends.following)
  }

  companion object {
    // folder name max length
    private const val FOLDER_NAME_MAX_LENGTH = 28

    /**
     * Formats the folder name by trimming leading whitespace and truncating it to the maximum
     * allowed length.
     *
     * @param name The folder name to format.
     * @return The formatted folder name.
     */
    fun formatName(name: String): String {
      return name.trimStart().take(FOLDER_NAME_MAX_LENGTH)
    }
  }
}
