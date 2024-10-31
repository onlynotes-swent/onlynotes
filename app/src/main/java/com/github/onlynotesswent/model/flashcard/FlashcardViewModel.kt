package com.github.onlynotesswent.model.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlashcardViewModel(private val repository: FlashcardRepository) {

  private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
  val flashcards: StateFlow<List<Flashcard>> = _flashcards.asStateFlow()

  private val _flashcard = MutableStateFlow<Flashcard?>(null)
  val flashcard: StateFlow<Flashcard?> = _flashcard.asStateFlow()

  private val _selectedFolderFlashcards = MutableStateFlow<List<Flashcard>>(emptyList())
  val selectedFolderFlashcards: StateFlow<List<Flashcard>> = _selectedFolderFlashcards.asStateFlow()

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FlashcardViewModel(FlashcardRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  /**
   * Selects a flashcard.
   *
   * @param flashcard The flashcard to be selected.
   */
  fun selectFlashcard(flashcard: Flashcard) {
    _flashcard.value = flashcard
  }

  /**
   * Generates a new unique ID.
   *
   * @return A new unique ID.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /** Initializes the FlashcardViewModel and the repository. */
  // TODO: Once we change user uid to firebase auth uid, we can call getFlashcards with the uid for
  // the success callback
  fun init() {
    repository.init {}
  }

  /**
   * Retrieves all flashcards for the given user.
   *
   * @param userId The identifier of the user.
   */
  fun getFlashcards(userId: String) {
    repository.getFlashcards(userId, { _flashcards.value = it }, {})
  }

  /**
   * Retrieves the flashcard with the given identifier.
   *
   * @param id The identifier of the flashcard to retrieve.
   */
  fun getFlashcardById(id: String) {
    repository.getFlashcardById(id, { _flashcard.value = it }, {})
  }

  /**
   * Retrieves all flashcards in the given folder.
   *
   * @param folderId The identifier of the folder.
   */
  fun getFlashcardsByFolder(folderId: String) {
    repository.getFlashcardsByFolder(folderId, { _selectedFolderFlashcards.value = it }, {})
  }

  /**
   * Adds the given flashcard.
   *
   * @param flashcard The flashcard to add.
   */
  fun addFlashcard(flashcard: Flashcard) {
    repository.addFlashcard(flashcard, { getFlashcards(flashcard.userId) }, {})
  }

  /**
   * Updates the given flashcard.
   *
   * @param flashcard The flashcard to update.
   */
  fun updateFlashcard(flashcard: Flashcard) {
    repository.updateFlashcard(flashcard, { getFlashcards(flashcard.userId) }, {})
  }

  /**
   * Deletes the given flashcard.
   *
   * @param flashcard The flashcard to delete.
   */
  fun deleteFlashcard(flashcard: Flashcard) {
    repository.deleteFlashcard(flashcard, {}, {})
  }
}
