package com.github.onlynotesswent.model.flashcard

import com.google.firebase.Timestamp

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
}
