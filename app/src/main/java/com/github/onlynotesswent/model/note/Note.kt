package com.github.onlynotesswent.model.note

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.google.firebase.Timestamp
import java.security.MessageDigest

/**
 * Represents a note stored in the database.
 *
 * @property id A unique identifier for this note.
 * @property title The title of the note.
 * @property date The timestamp of when the note was created.
 * @property lastModified The timestamp of when the note was last modified.
 * @property visibility The visibility setting for the note.
 * @property noteCourse The [Course] object associated with this note. If the note is not associated
 *   with a course, this value is `null`.
 * @property userId The unique identifier of the user who created the note.
 * @property folderId The unique identifier of the folder the note is stored in. If the note is not
 *   assigned to a folder, this value is `null`.
 * @property comments A collection of comments associated with the note.
 */
@Entity
data class Note(
    @PrimaryKey val id: String,
    val title: String,
    val date: Timestamp,
    val lastModified: Timestamp,
    val visibility: Visibility = Visibility.DEFAULT,
    @Embedded val noteCourse: Course? = null,
    val userId: String,
    val folderId: String? = null,
    val comments: CommentCollection = CommentCollection()
) {
  /**
   * Checks if the note is owned by the user with the given Id.
   *
   * @param uid The Id of the user to check.
   * @return `true` if the note is owned by the user; `false` otherwise.
   */
  fun isOwner(uid: String): Boolean {
    return userId == uid
  }

  companion object {
    // Note title max length
    private const val TITLE_MAX_LENGTH = 35

    /**
     * Formats the note title by trimming leading whitespace and truncating it to the maximum
     * allowed length.
     *
     * @param title The note title to format.
     * @return The formatted note title.
     */
    fun formatTitle(title: String): String {
      return title.trimStart().take(TITLE_MAX_LENGTH)
    }
  }

  /** Represents a list of Comments for a Note. The class is immutable. */
  class CommentCollection(val commentsList: List<Comment> = emptyList()) {
    /**
     * Retrieves a list of comments made by a specific user.
     *
     * @param userId The unique identifier of the user whose comments are to be retrieved.
     * @return A list of [Comment]s associated with the specified user ID. If the user has not made
     *   any comments, an empty list is returned.
     */
    fun getUserComments(userId: String): List<Comment> {
      return commentsList.filter { it.userId == userId }
    }

    /**
     * Adds a new comment to the CommentCollection with a unique ID.
     *
     * @param userId The ID of the user adding the comment.
     * @param userName The username of the added comment user.
     * @param content The text content of the comment.
     * @return An updated CommentCollection including the new comment.
     */
    fun addCommentByUser(userId: String, userName: String, content: String): CommentCollection {
      val mutableCommentsList = commentsList.toMutableList()
      mutableCommentsList.add(
          0,
          Comment(
              generateCommentId(userId, content),
              userId,
              userName,
              content,
              Timestamp.now(),
              Timestamp.now()))
      return CommentCollection(mutableCommentsList.toList())
    }

    /**
     * Edits an existing comment in the CommentCollection by its ID.
     *
     * @param commentId The ID of the comment to be edited.
     * @param content The new content for the comment.
     * @return An updated CommentCollection with the modified comment.
     */
    fun editCommentByCommentId(commentId: String, content: String): CommentCollection {
      val updatedCommentsList =
          commentsList.map {
            if (it.commentId == commentId)
                Comment(
                    generateCommentId(it.userId, content),
                    it.userId,
                    it.userName,
                    content,
                    it.creationDate,
                    Timestamp.now())
            else it
          }
      return CommentCollection(updatedCommentsList)
    }

    /**
     * Deletes a comment from the CommentCollection by its ID.
     *
     * @param commentId The ID of the comment to be deleted.
     * @return An updated CommentCollection excluding the deleted comment.
     */
    fun deleteCommentByCommentId(commentId: String): CommentCollection {
      return CommentCollection(commentsList.filter { it.commentId != commentId })
    }

    /**
     * Generates a unique ID based on the user ID, content, and current timestamp.
     *
     * @param userId The unique identifier for the user.
     * @param content The content of the comment.
     * @return A SHA-256 hash string that uniquely represents this comment instance.
     */
    private fun generateCommentId(userId: String, content: String): String {
      val timestamp = System.currentTimeMillis()
      val input = "$userId$content$timestamp"
      val digest = MessageDigest.getInstance("SHA-256")
      val hashBytes = digest.digest(input.toByteArray())
      return hashBytes.joinToString("") { "%02x".format(it) }
    }
  }

  /**
   * Represents a comment stored in the CommentCollection.
   *
   * @property commentId A unique identifier for this comment, generated by hashing the user ID,
   *   content, and timestamp.
   * @property userId The unique identifier of the user who posted the comment.
   * @property userName The display name of the user who posted the comment.
   * @property content The text content of the comment.
   * @property creationDate The timestamp of when the comment was initially created.
   * @property editedDate The timestamp of the most recent edit to the comment.
   */
  data class Comment(
      val commentId: String,
      val userId: String,
      val userName: String,
      val content: String,
      val creationDate: Timestamp,
      val editedDate: Timestamp
  ) {
    /**
     * Checks if the comment has not been edited since its creation.
     *
     * @return `true` if the comment has not been edited (i.e., `creationDate` is equal to
     *   `editedDate`); `false` otherwise.
     */
    fun isUnedited(): Boolean {
      return creationDate == editedDate
    }

    /**
     * Checks if the comment is owned by the user with the given Id.
     *
     * @param uid The Id of the user to check.
     * @return `true` if the comment is owned by the user; `false` otherwise.
     */
    fun isOwner(uid: String): Boolean {
      return userId == uid
    }
  }
}
