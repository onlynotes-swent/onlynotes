package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import com.google.firebase.Timestamp
import java.security.MessageDigest

data class Note(
    val id: String,
    val type: Type,
    val title: String,
    val content: String,
    val date: Timestamp,
    val visibility: Visibility,
    val noteClass: Class,
    val userId: String,
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
        else -> "$this (not implemented)" // keep for maintainability
      }
    }
  }

  enum class Type {
    JPEG,
    PNG,
    PDF,
    NORMAL_TEXT
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

  /**
   * Represents a list of Comments for a Note. The class is immutable. It's companion object is used
   * to produce a new CommentCollection to get a new one.
   */
  class CommentCollection(val commentsList: List<Comment> = emptyList()) {
    fun getUserComments(userId: String): List<Comment> {
      return commentsList.filter { it.userId == userId }
    }

    companion object {

      /**
       * Generates a unique ID based on the user ID, content, and current timestamp. (There is
       * nearly no risk of collisions as a user would have to input the exact same comment content
       * at the exact same millisecond under a Note for both comments to have the exact same Id)
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
      /**
       * Adds a new comment to the CommentCollection with a unique ID.
       *
       * @param userId The ID of the user adding the comment.
       * @param content The text content of the comment.
       * @param CommentCollection The current collection of comments.
       * @return An updated CommentCollection including the new comment.
       */
      fun addComment(
          userId: String,
          content: String,
          CommentCollection: CommentCollection
      ): CommentCollection {
        val mutableCommentsList = CommentCollection.commentsList.toMutableList()
        mutableCommentsList.add(0, Comment(generateId(userId, content), userId, content))
        return CommentCollection(mutableCommentsList.toList())
      }
      /**
       * Edits an existing comment in the CommentCollection by its ID.
       *
       * @param commentId The ID of the comment to be edited.
       * @param content The new content for the comment.
       * @param CommentCollection The current collection of comments.
       * @return An updated CommentCollection with the modified comment.
       */
      fun editComment(
          commentId: String,
          content: String,
          CommentCollection: CommentCollection
      ): CommentCollection {
        val mutableCommentsList = CommentCollection.commentsList.toMutableList()
        return CommentCollection(
            mutableCommentsList.map {
              if (it.commentId == commentId)
                  Comment(generateId(it.userId, content), it.userId, content)
              else it
            })
      }
      /**
       * Deletes a comment from the CommentCollection by its ID.
       *
       * @param commentId The ID of the comment to be deleted.
       * @param CommentCollection The current collection of comments.
       * @return An updated CommentCollection excluding the deleted comment.
       */
      fun deleteComment(
          commentId: String,
          CommentCollection: CommentCollection
      ): CommentCollection {
        return CommentCollection(
            CommentCollection.commentsList.filter { it.commentId != commentId })
      }
    }
  }

  /**
   * Represents a comment that is stored in the CommentCollection.
   *
   * @property commentId A unique identifier for this comment, generated by hashing the user ID,
   *   content, and timestamp.
   * @property userId The unique identifier of the user who posted the comment.
   * @property content The text content of the comment.
   */
  data class Comment(val commentId: String, val userId: String, val content: String)
}
