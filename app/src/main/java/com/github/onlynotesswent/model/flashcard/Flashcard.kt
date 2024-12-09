package com.github.onlynotesswent.model.flashcard

/**
 * Represents a flashcard.
 *
 * @property id The ID of the flashcard.
 * @property front The front side of the flashcard which contains the question.
 * @property back The back side of the flashcard which contains the answer.
 * @property latexFormula The latex formula for the flashcard, if any.
 * @property hasImage A flag indicating if the flashcard has an image.
 * @property fakeBacks The fake backs of the flashcard for MCQs, if any.
 * @property lastReviewed The timestamp of the last review of the flashcard.
 * @property userId The ID of the user who created the flashcard.
 * @property folderId The ID of the folder that the flashcard belongs to.
 * @property noteId The ID of the note that the flashcard belongs to.
 */
data class Flashcard(
    val id: String,
    val front: String, // The front side of the flashcard which contains the question.
    val back: String, // The back side of the flashcard which contains the answer.
    val latexFormula: String = "", // The latex formula for the flashcard, if any.
    val hasImage: Boolean = false, // A flag indicating if the flashcard has an image.
    val fakeBacks: List<String> = emptyList(), // The fake backs of the flashcard for MCQs, if any.
    val userId: String,
    val folderId: String?,
    val noteId: String?
) {
  /**
   * Checks if the flashcard is a multiple choice question (MCQ).
   *
   * @return true if the flashcard is a MCQ, false otherwise.
   */
  fun isMCQ(): Boolean {
    return fakeBacks.isNotEmpty()
  }
}
