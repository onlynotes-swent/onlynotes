package com.github.onlynotesswent.model.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeckViewModel(private val repository: DeckRepository) : ViewModel() {

  // The decks of the user
  private val _userDecks = MutableStateFlow<List<Deck>>(emptyList())
  val userDecks: StateFlow<List<Deck>> = _userDecks.asStateFlow()

  private val _userRootDecks = MutableStateFlow<List<Deck>>(emptyList())
  val userRootDecks: StateFlow<List<Deck>> = _userRootDecks.asStateFlow()

  // The selected deck
  private val _selectedDeck = MutableStateFlow<Deck?>(null)
  val selectedDeck: StateFlow<Deck?> = _selectedDeck.asStateFlow()

  // The selected play mode
  private val _selectedPlayMode = MutableStateFlow<Deck.PlayMode?>(null)
  val selectedPlayMode: StateFlow<Deck.PlayMode?> = _selectedPlayMode.asStateFlow()

  // The decks in the selected folder
  private val _folderDecks = MutableStateFlow<List<Deck>>(emptyList())
  val folderDecks: StateFlow<List<Deck>> = _folderDecks.asStateFlow()

  // The public decks
  private val _publicDecks = MutableStateFlow<List<Deck>>(emptyList())
  val publicDecks: StateFlow<List<Deck>> = _publicDecks.asStateFlow()

  // The friends only decks
  private val _friendsDecks = MutableStateFlow<List<Deck>>(emptyList())
  val friendsDecks: StateFlow<List<Deck>> = _friendsDecks.asStateFlow()

  // Dragged deck
  private val _draggedDeck = MutableStateFlow<Deck?>(null)
  val draggedDeck: StateFlow<Deck?> = _draggedDeck.asStateFlow()

  /** Initializes the DeckViewModel and the repository. */
  init {
    repository.init { getPublicDecks() }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { DeckViewModel(DeckRepositoryFirestore(Firebase.firestore)) }
    }
  }

  /**
   * Selects a deck.
   *
   * @param deck The deck to be selected.
   */
  fun selectDeck(deck: Deck) {
    _selectedDeck.value = deck
  }

  /** Clear the selected deck. */
  fun clearSelectedDeck() {
    _selectedDeck.value = null
  }

  /**
   * Sets the dragged Deck document.
   *
   * @param draggedDeck The dragged Deck document.
   */
  fun draggedDeck(draggedDeck: Deck?) {
    _draggedDeck.value = draggedDeck
  }

  /**
   * Selects a deck and a play mode.
   *
   * @param deck The deck to be selected.
   * @param playMode The play mode to be selected.
   */
  fun playDeckWithMode(deck: Deck, playMode: Deck.PlayMode) {
    _selectedDeck.value = deck
    _selectedPlayMode.value = playMode
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
   * Retrieves all public decks.
   *
   * @param onSuccess The callback to be executed when the public decks are retrieved.
   * @param onFailure The callback to be executed when an error occurs.
   */
  fun getPublicDecks(onSuccess: (List<Deck>) -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.getPublicDecks(
        { decks ->
          _publicDecks.value = decks
          onSuccess(decks)
        },
        { onFailure(it) })
  }

  /**
   * Retrieves all friends only decks from a list of following users.
   *
   * @param followingListIds The list of users to retrieve decks from.
   * @param onSuccess The callback to be executed when the decks are retrieved.
   * @param onFailure The callback to be executed when an error occurs.
   */
  fun getDecksFromFollowingList(
      followingListIds: List<String>,
      onSuccess: (List<Deck>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getDecksFromFollowingList(
        followingListIds,
        { decks ->
          _friendsDecks.value = decks
          onSuccess(decks)
        },
        { onFailure(it) })
  }

  /**
   * Retrieves all decks for the given user.
   *
   * @param userId The identifier of the user.
   */
  fun getDecksFrom(
      userId: String,
      onSuccess: (List<Deck>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getDecksFrom(
        userId,
        { decks ->
          _userDecks.value = decks
          onSuccess(decks)
        },
        { onFailure(it) })
  }

  /**
   * Retrieves all root decks from a user (folderId == null).
   *
   * @param userId The ID of the user to retrieve root decks for.
   * @param onSuccess Callback to be invoked with the retrieved root decks.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getRootDecksFromUserId(
      userId: String,
      onSuccess: (List<Deck>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getRootDecksFromUserId(
        userId,
        { decks ->
          _userRootDecks.value = decks
          onSuccess(decks)
        },
        { onFailure(it) })
  }

  /**
   * Retrieves all decks in the given folder.
   *
   * @param folderId The identifier of the folder.
   */
  fun getDecksByFolder(
      folderId: String,
      onSuccess: (List<Deck>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getDecksByFolder(
        folderId,
        { decks ->
          _folderDecks.value = decks
          onSuccess(decks)
        },
        { onFailure(it) })
  }

  /**
   * Retrieves the deck with the given identifier.
   *
   * @param id The identifier of the deck to retrieve.
   */
  fun getDeckById(id: String, onSuccess: (Deck) -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.getDeckById(
        id,
        { deck ->
          _selectedDeck.value = deck
          onSuccess(deck)
        },
        { onFailure(it) })
  }

  /**
   * Updates the given deck.
   *
   * @param deck The deck to update.
   */
  fun updateDeck(deck: Deck, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.updateDeck(deck, { onSuccess() }, { onFailure(it) })
  }

  /**
   * Deletes the given deck.
   *
   * @param deck The deck to delete.
   */
  fun deleteDeck(deck: Deck, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.deleteDeck(deck, { onSuccess() }, { onFailure(it) })
  }

  /**
   * Deletes all decks from a folder.
   *
   * @param folderId The ID of the folder to delete decks from.
   * @param onSuccess The function to call when the deletion is successful.
   * @param onFailure The function to call when the deletion fails.
   */
  fun deleteDecksFromFolder(
      folderId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
  ) {
    repository.deleteDecksFromFolder(
        folderId = folderId,
        onSuccess = {
          getDecksByFolder(folderId)
          onSuccess()
        },
        onFailure = onFailure)
  }

  /**
   * Deletes all decks from the given user.
   *
   * @param userId The identifier of the user.
   * @param onSuccess The callback to be executed when the decks are deleted.
   * @param onFailure The callback to be executed when an error occurs.
   */
  fun deleteAllDecksFromUserId(
      userId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.deleteAllDecksFromUserId(userId, { onSuccess() }, { onFailure(it) })
  }
}
