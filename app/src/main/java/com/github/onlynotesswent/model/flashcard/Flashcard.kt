package com.github.onlynotesswent.model.flashcard

import com.github.onlynotesswent.model.flashcard.Flashcard.Type
import com.google.firebase.Timestamp

/**
 * Represents a flashcard.
 *
 * @property id The ID of the flashcard.
 * @property front The front side of the flashcard which contains the question.
 * @property back The back side of the flashcard which contains the answer.
 * @property lastReviewed The timestamp of the last review of the flashcard.
 * @property userId The ID of the user who created the flashcard.
 * @property folderId The ID of the folder that the flashcard belongs to.
 * @property noteId The ID of the note that the flashcard belongs to.
 */
abstract class Flashcard(
    open val id: String,
    open val front: String, // The front side of the flashcard which contains the question.
    open val back: String, // The back side of the flashcard which contains the answer.
    open val lastReviewed: Timestamp?,
    open val userId: String,
    open val folderId: String?,
    open val noteId: String?,
    open val type: Type
) {
  /** The type of the flashcard. */
  enum class Type {
    TEXT,
    IMAGE,
    MCQ;

    companion object {
        /**
         * Converts the given string to a [Type].
         *
         * @param type The string to convert.
         * @return The [Type] corresponding to the given string.
         * @throws IllegalArgumentException If the given string does not correspond to a [Type].
         */
      fun fromString(type: String): Type {
        return when (type) {
          TEXT.toString() -> TEXT
          IMAGE.toString() -> IMAGE
          MCQ.toString() -> MCQ
          else -> throw IllegalArgumentException("Invalid flashcard type")
        }
      }
    }
  }

  /** Converts the flashcard to a map. */
  abstract fun toMap(): Map<String, Any?>

  companion object {
    /**
     * Creates a flashcard from the given type and map.
     *
     * @param type The type of the flashcard.
     * @param map The map containing the flashcard data.
     * @return The flashcard created from the given type and map.
     * @throws IllegalArgumentException If the map does not contain valid flashcard data.
     */
    fun from(type: Type, map: Map<String, Any?>): Flashcard {
      return try {
          when (type) {
              Type.TEXT ->
                  TextFlashcard(
                      map["id"] as String,
                      map["front"] as String,
                      map["back"] as String,
                      map["lastReviewed"] as Timestamp?,
                      map["userId"] as String,
                      map["folderId"] as String?,
                      map["noteId"] as String?
                  )

              Type.IMAGE ->
                  ImageFlashcard(
                      map["id"] as String,
                      map["front"] as String,
                      map["back"] as String,
                      map["imageUrl"] as String,
                      map["lastReviewed"] as Timestamp?,
                      map["userId"] as String,
                      map["folderId"] as String?,
                      map["noteId"] as String?
                  )

              Type.MCQ ->
                  MCQFlashcard(
                      map["id"] as String,
                      map["front"] as String,
                      map["back"] as String,
                      map["fakeBacks"] as List<String>,
                      map["lastReviewed"] as Timestamp?,
                      map["userId"] as String,
                      map["folderId"] as String?,
                      map["noteId"] as String?
                  )
          }
      }   catch (e: Exception) {
          throw IllegalArgumentException("Invalid flashcard data")
      }
    }
  }
}

/** Represents a text flashcard. */
data class TextFlashcard(
    override val id: String,
    override val front: String,
    override val back: String,
    override val lastReviewed: Timestamp?,
    override val userId: String,
    override val folderId: String?,
    override val noteId: String?
) : Flashcard(id, front, back, lastReviewed, userId, folderId, noteId, Type.TEXT) {
  override fun toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "front" to front,
        "back" to back,
        "lastReviewed" to lastReviewed,
        "userId" to userId,
        "folderId" to folderId,
        "noteId" to noteId,
        "type" to type.toString())
  }
}

/**
 * Represents an image flashcard.
 *
 * @property imageUrl The URL of the image.
 */
data class ImageFlashcard(
    override val id: String,
    override val front: String,
    override val back: String,
    val imageUrl: String,
    override val lastReviewed: Timestamp?,
    override val userId: String,
    override val folderId: String?,
    override val noteId: String?
) : Flashcard(id, front, back, lastReviewed, userId, folderId, noteId, Type.IMAGE) {
  override fun toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "front" to front,
        "back" to back,
        "imageUrl" to imageUrl,
        "lastReviewed" to lastReviewed,
        "userId" to userId,
        "folderId" to folderId,
        "noteId" to noteId,
        "type" to type.toString())
  }
}

/**
 * Represents a multiple-choice question flashcard.
 *
 * @property fakeBacks The fake answers that are displayed when the user is reviewing the flashcard.
 *   The correct answer is stored in the [back] property. NB: the [fakeBacks] list can contain more
 *   fake answers than the number of choices displayed to the user.
 */
data class MCQFlashcard(
    override val id: String,
    override val front: String,
    override val back: String,
    val fakeBacks: List<String>, // The fake answers that are displayed for MCQ flashcards
    override val lastReviewed: Timestamp?,
    override val userId: String,
    override val folderId: String?,
    override val noteId: String?,
) : Flashcard(id, front, back, lastReviewed, userId, folderId, noteId, Type.MCQ) {
  override fun toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "front" to front,
        "back" to back,
        "fakeBacks" to fakeBacks,
        "lastReviewed" to lastReviewed,
        "userId" to userId,
        "folderId" to folderId,
        "noteId" to noteId,
        "type" to type.toString())
  }
}

