package com.github.onlynotesswent.model.flashcard.deck

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

  // The selected deck
  private val _selectedDeck = MutableStateFlow<Deck?>(null)
  val selectedDeck: StateFlow<Deck?> = _selectedDeck.asStateFlow()

  // The decks in the selected folder
  private val _folderDecks = MutableStateFlow<List<Deck>>(emptyList())
  val folderDecks: StateFlow<List<Deck>> = _folderDecks.asStateFlow()

  /** Initializes the DeckViewModel and the repository. */

    init {
        repository.init {}
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

    /**
     * Generates a new unique ID.
     *
     * @return A new unique ID.
     */
    fun getNewUid(): String {
        return repository.getNewUid()
    }

    /**
     * Retrieves all decks for the given user.
     *
     * @param userId The identifier of the user.
     */
    fun getDecksFrom(userId: String, onSuccess: (List<Deck>) -> Unit={}, onFailure: (Exception) -> Unit={}) {
        repository.getDecksFrom(userId, { decks ->
            _userDecks.value = decks
            onSuccess(decks)
        }, {
            onFailure(it)
        })
    }

    /**
     * Retrieves all decks in the given folder.
     *
     * @param folderId The identifier of the folder.
     */
    fun getDecksByFolder(folderId: String, onSuccess: (List<Deck>) -> Unit={}, onFailure: (Exception) -> Unit={}) {
        repository.getDecksByFolder(folderId, { decks ->
            _folderDecks.value = decks
            onSuccess(decks)
        }, {
            onFailure(it)
        })
    }

    /**
     * Retrieves the deck with the given identifier.
     *
     * @param id The identifier of the deck to retrieve.
     */
    fun getDeckById(id: String, onSuccess: (Deck) -> Unit={}, onFailure: (Exception) -> Unit={}) {
        repository.getDeckById(id, { deck ->
            _selectedDeck.value = deck
            onSuccess(deck)
        }, {
            onFailure(it)
        })
    }

    /**
     * Updates the given deck.
     *
     * @param deck The deck to update.
     */
    fun updateDeck(deck: Deck, onSuccess: () -> Unit={}, onFailure: (Exception) -> Unit={}) {
        repository.updateDeck(deck, {
            onSuccess()
        }, {
            onFailure(it)
        })
    }

    /**
     * Deletes the given deck.
     *
     * @param deck The deck to delete.
     */
    fun deleteDeck(deck: Deck, onSuccess: () -> Unit={}, onFailure: (Exception) -> Unit={}) {
        repository.deleteDeck(deck, {
            onSuccess()
        }, {
            onFailure(it)
        })
    }


}