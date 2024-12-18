package com.github.onlynotesswent.model.flashcard

import com.google.firebase.Timestamp
import kotlin.random.Random

/**
 * Represents a user flashcard.
 *
 * @property id The ID of the User flashcard. this should be the same as the flashcard id
 * @property level The level of the flashcard.
 * @property lastReviewed The timestamp of the last review of the flashcard.
 */
data class UserFlashcard(
    val id: String,
    val level: Int = DEFAULT_FLASHCARD_LEVEL,
    val lastReviewed: Timestamp = Timestamp.now(),
) {
  companion object {
    const val MAX_FLASHCARD_LEVEL = 5
    const val MIN_FLASHCARD_LEVEL = 0
    const val DEFAULT_FLASHCARD_LEVEL = 1

    fun totalWeight(flashcards: List<UserFlashcard>): Int {
      var total = 0
      for (flashcard in flashcards) {
        total += flashcard.fromLevelToWeight()
      }
      return total
    }

    /**
     * Selects a random flashcard from the list of flashcards. The probability of selecting a
     * flashcard is based on the level of the flashcard. Also this code run O(n) time bases on the
     * number of flashcards use in case of small number of flashcards
     *
     * @param flashcards The list of flashcards to select from.
     * @return The selected flashcard.
     */
    fun selectRandomFlashcardLinear(flashcards: List<UserFlashcard>): UserFlashcard {

      if (flashcards.isEmpty()) {
        throw IllegalArgumentException("The list of flashcards is empty")
      }
      val totalWeight = totalWeight(flashcards)
      val random = Random.nextInt(totalWeight)
      var currentWeight = 0
      for (flashcard in flashcards) {
        currentWeight += flashcard.fromLevelToWeight()
        if (currentWeight >= random) {
          return flashcard
        }
      }
      return flashcards[0]
    }
  }

  /**
   * Increases the level of the flashcard by 1. If the level is already at the maximum level, it
   * will not increase.
   *
   * @return The flashcard with the increased level.
   */
  fun increaseLevel(): UserFlashcard {
    var newLevel = level + 1
    if (newLevel > MAX_FLASHCARD_LEVEL) {
      newLevel = MAX_FLASHCARD_LEVEL
    }
    return this.copy(level = newLevel)
  }

  /**
   * Decreases the level of the flashcard by 1. If the level is already at the minimum level, it
   * will not decrease.
   *
   * @return The flashcard with the decreased level.
   */
  fun decreaseLevel(): UserFlashcard {
    var newLevel = level - 1
    if (newLevel < MIN_FLASHCARD_LEVEL) {
      newLevel = MIN_FLASHCARD_LEVEL
    }
    return this.copy(level = newLevel)
  }

  /**
   * Resets the level of the flashcard to 0.
   *
   * @return The flashcard with the level reset to 0.
   */
  fun resetLevel(): UserFlashcard {
    return this.copy(level = MIN_FLASHCARD_LEVEL)
  }

  /**
   * Updates the last reviewed timestamp of the flashcard.
   *
   * @return The flashcard with the updated last reviewed timestamp.
   */
  fun updateLastReviewed(): UserFlashcard {
    return this.copy(lastReviewed = Timestamp.now())
  }

  /**
   * Converts the level of the flashcard to a weight value. The weight represent how often the
   * flashcard should be reviewed. The higher the weight the more often the flashcard should be
   * reviewed. The weight is calculated as the reverse order of the level.
   *
   * @return The weight value.
   */
  fun fromLevelToWeight(): Int {
    return MAX_FLASHCARD_LEVEL - level + 1
  }
}
