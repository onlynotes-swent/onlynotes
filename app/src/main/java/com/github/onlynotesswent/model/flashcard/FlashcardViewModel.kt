package com.github.onlynotesswent.model.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlashcardViewModel(private val repository: FlashcardRepository) : ViewModel() {

  // The flashcards of the user
  private val _userFlashcards = MutableStateFlow<List<Flashcard>>(emptyList())
  val userFlashcards: StateFlow<List<Flashcard>> = _userFlashcards.asStateFlow()

  // The selected flashcard
  private val _selectedFlashcard = MutableStateFlow<Flashcard?>(null)
  val selectedFlashcard: StateFlow<Flashcard?> = _selectedFlashcard.asStateFlow()

  // The flashcards in the selected folder
  private val _folderFlashcards = MutableStateFlow<List<Flashcard>>(emptyList())
  val folderFlashcards: StateFlow<List<Flashcard>> = _folderFlashcards.asStateFlow()

  // The flashcards in the selected note
  private val _noteFlashcards = MutableStateFlow<List<Flashcard>>(emptyList())
  val noteFlashcards: StateFlow<List<Flashcard>> = _noteFlashcards.asStateFlow()

  /** Initializes the FlashcardViewModel and the repository. */
  // TODO: Once we change user uid to firebase auth uid, we can call getFlashcardsFrom with
  // the uid for the success callback
  init {
    repository.init(FirebaseAuth.getInstance()) {}
  }

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
    _selectedFlashcard.value = flashcard
  }

  /**
   * Generates a new unique ID.
   *
   * @return A new unique ID.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /**
   * Retrieves all flashcards for the given user.
   *
   * @param userId The identifier of the user.
   */
  fun getFlashcardsFrom(userId: String) {
    repository.getFlashcardsFrom(userId, { _userFlashcards.value = it }, {})
  }

  /**
   * Retrieves the flashcard with the given identifier.
   *
   * @param id The identifier of the flashcard to retrieve.
   */
  fun getFlashcardById(id: String) {
    repository.getFlashcardById(id, { _selectedFlashcard.value = it }, {})
  }

  /**
   * Retrieves all flashcards in the given folder.
   *
   * @param folderId The identifier of the folder.
   */
  fun getFlashcardsByFolder(folderId: String) {
    repository.getFlashcardsByFolder(folderId, { _folderFlashcards.value = it }, {})
  }

  /**
   * Retrieves all flashcards for the given note.
   *
   * @param noteId The identifier of the note.
   */
  fun getFlashcardsByNote(noteId: String) {
    repository.getFlashcardsByNote(noteId, { _noteFlashcards.value = it }, {})
  }

  /**
   * Adds the given flashcard.
   *
   * @param flashcard The flashcard to add.
   */
  fun addFlashcard(flashcard: Flashcard) {
    repository.addFlashcard(flashcard, { getFlashcardsFrom(flashcard.userId) }, {})
  }

  /**
   * Updates the given flashcard.
   *
   * @param flashcard The flashcard to update.
   */
  fun updateFlashcard(flashcard: Flashcard) {
    repository.updateFlashcard(flashcard, { getFlashcardsFrom(flashcard.userId) }, {})
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
