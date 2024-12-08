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
  /**
   * Represents the play mode of a deck.
   */
  enum class PlayMode {
    FLASHCARD,
    MATCH,
    MCQ,
    ALL;

    companion object {
      /**
       * Converts a string to a PlayMode.
       *
       * @param s The string to convert.
       * @return The converted PlayMode.
       */
      fun fromString(s: String?): PlayMode {
        return entries.find { it.toString() == s } ?: FLASHCARD
      }
    }
  }

    /**
     * Represents the sort mode of a deck.
     */
  enum class SortMode {
    ALPHABETICAL,
    REVIEW,
    LEVEL;

    /**
     * Represents the order of sorting.
     */
    enum class Order {
      HIGH_LOW,
      LOW_HIGH;

        /**
         * Returns the next order.
         *
         * @return The next order.
         */
      fun next(): Order {
        return when (this) {
          HIGH_LOW -> LOW_HIGH
          LOW_HIGH -> HIGH_LOW
        }
      }
    }

    /**
     * Converts the sort mode to a readable string.
     *
     * @return The readable string.
     */
    fun toReadableString(): String {
      return when (this) {
        ALPHABETICAL -> "Alphabetical"
        REVIEW -> "Last Review"
        LEVEL -> "Level"
      }
    }

    /**
     * Sorts the flashcards with the given [sortOrder] by returning a sorted copy of [flashcards].
     *
     * @param flashcards The flashcards to sort.
     * @param sortOrder The order of sorting.
     * @return The sorted flashcards.
     */
    fun sort(flashcards: List<Flashcard>, sortOrder: Order): List<Flashcard> {
      return when (this) {
        ALPHABETICAL -> flashcards.sortedBy { card -> card.front.lowercase().trim() }
        REVIEW -> flashcards.sortedBy { card -> card.lastReviewed }
        LEVEL -> flashcards // TODO once level is implemented, for now does nothing
      }.let { if (sortOrder == Order.HIGH_LOW) it.reversed() else it }
    }
  }

  companion object {
      // The maximum length of the description
    private const val DESCRIPTION_MAX_LEN = 200
        // The maximum length of the title
    private const val TITLE_MAX_LEN = 75

    /**
     * Formats the description by trimming it and taking the first [DESCRIPTION_MAX_LEN] characters.
     *
     * @param s The description to format.
     * @return The formatted description.
     */
    fun formatDescription(s: String): String {
      return s.trimStart().take(DESCRIPTION_MAX_LEN)
    }

    /**
     * Formats the title by trimming it and taking the first [TITLE_MAX_LEN] characters.
     *
     * @param s The title to format.
     * @return The formatted title.
     */
    fun formatTitle(s: String): String {
      return s.trimStart().take(TITLE_MAX_LEN)
    }
  }
}
