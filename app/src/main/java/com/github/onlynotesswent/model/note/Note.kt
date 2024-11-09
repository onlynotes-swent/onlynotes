package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import com.google.firebase.Timestamp
import java.security.MessageDigest

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val date: Timestamp,
    val visibility: Visibility,
    val noteClass: Class,
    val userId: String,
    val folderId: String? = null, // if note not assigned to a folder, folderId is null
    val image: Bitmap,
    val comments: CommentCollection = CommentCollection()
) {
  enum class Visibility {
    PUBLIC,
    FRIENDS,
    PRIVATE;

    companion object {
      val DEFAULT = PUBLIC
      val READABLE_STRINGS = Visibility.values().map { it.toReadableString() }

      fun fromReadableString(readableString: String): Visibility {
        return values().find { it.toReadableString() == readableString }
            ?: throw IllegalArgumentException("Invalid visibility string")
      }

      fun fromString(string: String): Visibility {
        return values().find { it.toString() == string }
            ?: throw IllegalArgumentException("Invalid visibility string")
      }
    }

    fun toReadableString(): String {
      return when (this) {
        PUBLIC -> "Public"
        FRIENDS -> "Friends Only"
        PRIVATE -> "Private"
      }
    }
  }

  /**
   * Represents a class that a note belongs to.
   *
   * @param classCode The code of the class.
   * @param className The name of the class.
   * @param classYear The year of the class.
   * @param publicPath The public path of the class.
   */
  data class Class(
      val classCode: String,
      val className: String,
      val classYear: Int,
      val publicPath: String
  )

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
    fun addComment(userId: String, userName: String, content: String): CommentCollection {
      val mutableCommentsList = commentsList.toMutableList()
      mutableCommentsList.add(
          0,
          Comment(
              generateId(userId, content),
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
    fun editComment(commentId: String, content: String): CommentCollection {
      val updatedCommentsList =
          commentsList.map {
            if (it.commentId == commentId)
                Comment(
                    generateId(it.userId, content),
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
    fun deleteComment(commentId: String): CommentCollection {
      return CommentCollection(commentsList.filter { it.commentId != commentId })
    }

    /**
     * Generates a unique ID based on the user ID, content, and current timestamp.
     *
     * @param userId The unique identifier for the user.
     * @param content The content of the comment.
     * @return A SHA-256 hash string that uniquely represents this comment instance.
     */
    private fun generateId(userId: String, content: String): String {
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
  }
}
