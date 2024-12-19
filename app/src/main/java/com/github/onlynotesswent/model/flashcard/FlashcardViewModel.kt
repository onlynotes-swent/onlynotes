package com.github.onlynotesswent.model.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.onlynotesswent.model.deck.Deck
import com.google.firebase.Firebase
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

  // The flashcards in the selected deck
  private val _deckFlashcards = MutableStateFlow<List<Flashcard>>(emptyList())
  val deckFlashcards: StateFlow<List<Flashcard>> = _deckFlashcards.asStateFlow()

  // The flashcards in the selected folder
  private val _folderFlashcards = MutableStateFlow<List<Flashcard>>(emptyList())
  val folderFlashcards: StateFlow<List<Flashcard>> = _folderFlashcards.asStateFlow()

  // The flashcards in the selected note
  private val _noteFlashcards = MutableStateFlow<List<Flashcard>>(emptyList())
  val noteFlashcards: StateFlow<List<Flashcard>> = _noteFlashcards.asStateFlow()

  /** Initializes the FlashcardViewModel and the repository. */
  init {
    repository.init {}
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { FlashcardViewModel(FlashcardRepositoryFirestore(Firebase.firestore)) }
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

  /** Deselects the selected flashcard. */
  fun deselectFlashcard() {
    _selectedFlashcard.value = null
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
   * Retrieves all flashcards for the given deck.
   *
   * @param deck The deck to retrieve flashcards from.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun fetchFlashcardsFromDeck(
      deck: Deck,
      onSuccess: (List<Flashcard>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getFlashcardsById(
        deck.flashcardIds,
        onSuccess = {
          _deckFlashcards.value = it
          onSuccess(it)
        },
        onFailure)
  }

  /**
   * Retrieves all flashcards for the given user.
   *
   * @param userId The identifier of the user.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getFlashcardsFromUser(
      userId: String,
      onSuccess: (List<Flashcard>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getFlashcardsFrom(
        userId,
        {
          onSuccess(it)
          _userFlashcards.value = it
        },
        onFailure)
  }

  /**
   * Retrieves the flashcard with the given identifier.
   *
   * @param id The identifier of the flashcard to retrieve.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getFlashcardById(
      id: String,
      onSuccess: (Flashcard?) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getFlashcardById(
        id,
        {
          onSuccess(it)
          _selectedFlashcard.value = it
        },
        onFailure)
  }

  /**
   * Retrieves all flashcards in the given folder.
   *
   * @param folderId The identifier of the folder.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getFlashcardsByFolder(
      folderId: String,
      onSuccess: (List<Flashcard>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getFlashcardsByFolder(
        folderId,
        {
          onSuccess(it)
          _folderFlashcards.value = it
        },
        onFailure)
  }

  /**
   * Retrieves all flashcards for the given note.
   *
   * @param noteId The identifier of the note.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getFlashcardsByNote(
      noteId: String,
      onSuccess: (List<Flashcard>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getFlashcardsByNote(
        noteId,
        {
          onSuccess(it)
          _noteFlashcards.value = it
        },
        onFailure)
  }

  /**
   * Adds the given flashcard.
   *
   * @param flashcard The flashcard to add.
   * @param onSuccess The function to call when the addition is successful.
   * @param onFailure The function to call when the addition fails.
   */
  fun addFlashcard(
      flashcard: Flashcard,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.addFlashcard(
        flashcard,
        {
          onSuccess()
          getFlashcardsFromUser(flashcard.userId)
        },
        onFailure)
  }

  /**
   * Updates the given flashcard.
   *
   * @param flashcard The flashcard to update.
   * @param onSuccess The function to call when the update is successful.
   * @param onFailure The function to call when the update fails.
   */
  fun updateFlashcard(
      flashcard: Flashcard,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.updateFlashcard(
        flashcard,
        {
          onSuccess()
          getFlashcardsFromUser(flashcard.userId)
        },
        onFailure)
  }

  /**
   * Deletes the given flashcard.
   *
   * @param flashcard The flashcard to delete.
   * @param onSuccess The function to call when the deletion is successful.
   * @param onFailure The function to call when the deletion fails.
   */
  fun deleteFlashcard(
      flashcard: Flashcard,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.deleteFlashcard(flashcard, onSuccess, onFailure)
  }
}
