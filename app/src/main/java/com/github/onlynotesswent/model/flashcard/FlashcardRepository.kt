package com.github.onlynotesswent.model.flashcard

import com.google.firebase.auth.FirebaseAuth

interface FlashcardRepository {

  /** @return a new unique identifier. */
  fun getNewUid(): String

  /**
   * Initializes the repository.
   *
   * @param onSuccess The callback to be invoked when the initialization is successful.
   */
  fun init(auth: FirebaseAuth, onSuccess: () -> Unit)

  /**
   * Retrieves all flashcards for the given user.
   *
   * @param userId The identifier of the user.
   * @param onSuccess The callback to be invoked when the flashcards are successfully retrieved.
   * @param onFailure The callback to be invoked if an error occurs.
   * @return all flashcards for the given user.
   */
  fun getFlashcards(
      userId: String,
      onSuccess: (List<Flashcard>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves the flashcard with the given identifier.
   *
   * @param id The identifier of the flashcard to retrieve.
   * @param onSuccess The callback to be invoked when the flashcard is successfully retrieved.
   * @param onFailure The callback to be invoked if an error occurs.
   * @return the flashcard with the given id.
   */
  fun getFlashcardById(id: String, onSuccess: (Flashcard?) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all flashcards in the given folder.
   *
   * @param folderId The identifier of the folder.
   * @param onSuccess The callback to be invoked when the flashcards are successfully retrieved.
   * @param onFailure The callback to be invoked if an error occurs.
   * @return all flashcards in the given folder.
   */
  fun getFlashcardsByFolder(
      folderId: String,
      onSuccess: (List<Flashcard>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Adds the given flashcard.
   *
   * @param flashcard The flashcard to add.
   * @param onSuccess The callback to be invoked when the flashcard is successfully added.
   * @param onFailure The callback to be invoked if an error occurs.
   */
  fun addFlashcard(flashcard: Flashcard, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Updates the given flashcard.
   *
   * @param flashcard The flashcard to update.
   * @param onSuccess The callback to be invoked when the flashcard is successfully updated.
   * @param onFailure The callback to be invoked if an error occurs.
   */
  fun updateFlashcard(flashcard: Flashcard, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes the flashcard with the given identifier.
   *
   * @param flashcard The flashcard to delete.
   * @param onSuccess The callback to be invoked when the flashcard is successfully deleted.
   * @param onFailure The callback to be invoked if an error occurs.
   */
  fun deleteFlashcard(flashcard: Flashcard, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
