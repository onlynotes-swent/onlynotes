package com.github.onlynotesswent.model.flashcard.deck

interface DeckRepository {

  /** @return a new unique identifier. */
  fun getNewUid(): String

  /**
   * Initializes the repository.
   *
   * @param onSuccess The callback to be invoked when the initialization is successful.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Retrieves all public decks.
   *
   * @param onSuccess The callback to be invoked when the decks are successfully retrieved.
   * @param onFailure The callback to be invoked if an error occurs.
   */
  fun getPublicDecks(onSuccess: (List<Deck>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all decks from a list of following users.
   *
   * @param followingListIds The list of users to retrieve decks from.
   * @param onSuccess The callback to be invoked when the decks are successfully retrieved.
   * @param onFailure The callback to be invoked if an error occurs.
   */
  fun getDecksFromFollowingList(
      followingListIds: List<String>,
      onSuccess: (List<Deck>) -> Unit,
      onFailure: (Exception) -> Unit
  )
  /**
   * Retrieves all decks for the given user.
   *
   * @param userId The identifier of the user.
   * @param onSuccess The callback to be invoked when the decks are successfully retrieved.
   * @param onFailure The callback to be invoked if an error occurs.
   * @return all decks for the given user.
   */
  fun getDecksFrom(userId: String, onSuccess: (List<Deck>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all root decks from a user (folderId == null).
   *
   * @param userId The ID of the user to retrieve root decks for.
   * @param onSuccess Callback to be invoked with the retrieved root decks.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getRootDecksFromUserId(
      userId: String,
      onSuccess: (List<Deck>) -> Unit,
      onFailure: (Exception) -> Unit,
  )

  /**
   * Retrieves the deck with the given identifier.
   *
   * @param id The identifier of the deck to retrieve.
   * @param onSuccess The callback to be invoked when the deck is successfully retrieved.
   * @param onFailure The callback to be invoked if an error occurs.
   * @return the deck with the given id.
   */
  fun getDeckById(id: String, onSuccess: (Deck) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all decks in the given folder.
   *
   * @param folderId The identifier of the folder.
   * @param onSuccess The callback to be invoked when the decks are successfully retrieved.
   * @param onFailure The callback to be invoked if an error occurs.
   * @return all decks in the given folder.
   */
  fun getDecksByFolder(
      folderId: String,
      onSuccess: (List<Deck>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Updates the given deck.
   *
   * @param deck The deck to update.
   * @param onSuccess The callback to be invoked when the deck is successfully updated.
   * @param onFailure The callback to be invoked if an error occurs.
   */
  fun updateDeck(deck: Deck, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Adds a flashcard to the given deck.
   *
   * @param deckId The identifier of the deck.
   * @param flashcardId The identifier of the flashcard.
   * @param onSuccess The callback to be invoked when the flashcard is successfully added.
   * @param onFailure The callback to be invoked if an error occurs.
   */
  fun addFlashcardToDeck(
      deckId: String,
      flashcardId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Adds multiple flashcards to the given deck.
   *
   * @param deckId The identifier of the deck.
   * @param flashcardIds The identifiers of the flashcards.
   * @param onSuccess The callback to be invoked when the flashcards are successfully added.
   * @param onFailure The callback to be invoked if an error occurs.
   */
  fun addFlashcardsToDeck(
      deckId: String,
      flashcardIds: List<String>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Removes a flashcard from the given deck.
   *
   * @param deckId The identifier of the deck.
   * @param flashcardId The identifier of the flashcard.
   * @param onSuccess The callback to be invoked when the flashcard is successfully removed.
   * @param onFailure The callback to be invoked if an error occurs.
   */
  fun removeFlashcardFromDeck(
      deckId: String,
      flashcardId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes the deck with the given identifier.
   *
   * @param deck The deck to delete.
   * @param onSuccess The callback to be invoked when the deck is successfully deleted.
   * @param onFailure The callback to be invoked if an error occurs.
   */
  fun deleteDeck(deck: Deck, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes all decks from a folder.
   *
   * @param folderId The ID of the folder to delete decks from.
   * @param onSuccess Callback to be invoked if the decks are deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteDecksFromFolder(
      folderId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
  )
}
