package com.github.onlynotesswent.model.folder

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.onlynotesswent.model.common.Visibility
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

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
    @get:PropertyName("isDeckFolder") val isDeckFolder: Boolean = false,
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
