package com.github.onlynotesswent.model.flashcard.deck

import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.google.firebase.Timestamp

/**
 * Represents a flashcard deck.
 *
 * @property id The ID of the deck.
 * @property name The name of the deck.
 * @property userId The ID of the user who created the deck.
 * @property folderId The ID of the folder that the deck belongs to.
 * @property visibility The visibility of the deck.
 * @property lastModified The timestamp of the last modification of the deck.
 * @property flashcardIds The IDs of the flashcards that the deck contains.
 * @property description The description of the deck.
 */
data class Deck(
    val id: String,
    val name: String,
    val userId: String,
    val folderId: String?,
    val visibility: Visibility,
    val lastModified: Timestamp,
    val description: String = "",
    val flashcardIds: List<String> = emptyList()
) {
  enum class PlayMode {
    FLASHCARD,
    MATCH,
    MCQ,
    ALL;

    companion object {
      fun fromString(s: String?): PlayMode {
        return entries.find { it.toString() == s } ?: FLASHCARD
      }
    }
  }

  enum class SortMode {
    ALPHABETICAL,
    REVIEW,
    LEVEL;

    enum class Order {
      HIGH_LOW,
      LOW_HIGH;

      fun next(): Order {
        return when (this) {
          HIGH_LOW -> LOW_HIGH
          LOW_HIGH -> HIGH_LOW
        }
      }
    }

    fun toReadableString(): String {
      return when (this) {
        ALPHABETICAL -> "Alphabetical"
        REVIEW -> "Last Review"
        LEVEL -> "Level"
      }
    }

    fun sort(flashcards: List<Flashcard>, sortOrder: Order): List<Flashcard> {
      return when (this) {
        ALPHABETICAL -> flashcards.sortedBy { card -> card.front.lowercase().trim() }
        REVIEW -> flashcards.sortedBy { card -> card.lastReviewed }
        LEVEL -> flashcards // TODO once level is implemented, for now does nothing
      }.let { if (sortOrder == Order.HIGH_LOW) it.reversed() else it }
    }
  }

  companion object {
    private const val DESCRIPTION_MAX_LEN = 200
    private const val TITLE_MAX_LEN = 75

    fun formatDescription(s: String): String {
      return s.trimStart().take(DESCRIPTION_MAX_LEN)
    }

    fun formatTitle(s: String): String {
      return s.trimStart().take(TITLE_MAX_LEN)
    }
  }
}
