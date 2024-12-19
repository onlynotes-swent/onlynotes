package com.github.onlynotesswent.model.deck

data class PlayDeckHistory(
    // The list of all flashcards in the deck.
    // The fist element and last element will always be null
    // to simply the logic of the circular list with the pager
    val listOfAllFlashcard: List<String?>,
    val currentFlashcardId: String,
    val indexOfCurrentFlashcard: Int,
    val size: Int,
) {
  constructor(
      currentFlashcardId: String
  ) : this(
      listOfAllFlashcard =
          List(MAX_LIST_LENGTH) {
            if (it == 0) {
              currentFlashcardId
            } else {
              null
            }
          },
      currentFlashcardId = currentFlashcardId,
      indexOfCurrentFlashcard = 0,
      size = 1)

  companion object {
    const val MAX_LIST_LENGTH = 6
    // because the first and last element are always null  and
    // one null element witch limit the next and previous flashcard
    const val MAX_SIZE = MAX_LIST_LENGTH - 3
  }

  /**
   * Adds a new flashcard to the list and stays with the current flashcard.
   *
   * @param flashcardId The ID of the flashcard to add.
   */
  fun stayWithNewFlashcard(flashcardId: String): PlayDeckHistory {
    val nextIndex = getIndexForward()
    val twiceNextIndex = getIndexForward(nextIndex)
    if (size == MAX_SIZE) {
      return this.copy(
          listOfAllFlashcard =
              listOfAllFlashcard.replaceAt(nextIndex, flashcardId).replaceAt(twiceNextIndex, null),
      )
    }
    return this.copy(
        listOfAllFlashcard = listOfAllFlashcard.replaceAt(nextIndex, flashcardId), size = size + 1)
  }

  /**
   * Moves forward to the next flashcard, adds the twice next flashcard to the list, and removes the
   * thrice next flashcard if the list is full.
   *
   * @param twiceNextId The ID of the flashcard to add.
   * @return The updated PlayDeckHistory.
   * @throws NullPointerException if the next flashcard is null, and we are trying to generate the
   *   twice next flashcard.
   */
  fun goForwardWithTwiceNextFlashcard(twiceNextId: String): PlayDeckHistory {
    val nextIndex = getIndexForward()
    val twiceNextIndex = getIndexForward(nextIndex)
    val thriceNextIndex = getIndexForward(twiceNextIndex)

    if (size == MAX_SIZE) {
      return this.copy(
          listOfAllFlashcard =
              listOfAllFlashcard
                  .replaceAt(twiceNextIndex, twiceNextId)
                  .replaceAt(thriceNextIndex, null) // use thrice next flashcard as buffer
                  .replaceAt(0, null), // use first page as buffer when max size is reached
          currentFlashcardId = listOfAllFlashcard[nextIndex]!!,
          indexOfCurrentFlashcard = nextIndex,
      )
    }
    return PlayDeckHistory(
        listOfAllFlashcard = listOfAllFlashcard.replaceAt(twiceNextIndex, twiceNextId),
        currentFlashcardId = listOfAllFlashcard[nextIndex]!!,
        indexOfCurrentFlashcard = nextIndex,
        size = size + 1)
  }

  /**
   * Goes back to the previous flashcard.
   *
   * @return The updated PlayDeckHistory.
   */
  fun goBack(): PlayDeckHistory {
    val index = getIndexBackward()
    if (listOfAllFlashcard[index] == null) {
      throw IllegalStateException("you shouldn't go back to a null flashcard")
    }
    return PlayDeckHistory(
        listOfAllFlashcard = listOfAllFlashcard,
        currentFlashcardId = listOfAllFlashcard[index]!!,
        indexOfCurrentFlashcard = index,
        size = size)
  }

  /**
   * Goes forward to the next flashcard.
   *
   * @return The updated PlayDeckHistory.
   */
  fun goForward(): PlayDeckHistory {
    val index = getIndexForward()
    if (listOfAllFlashcard[index] == null) {
      throw IllegalStateException("you shouldn't go forward to a null flashcard")
    }
    return PlayDeckHistory(
        listOfAllFlashcard = listOfAllFlashcard,
        currentFlashcardId = listOfAllFlashcard[index]!!,
        indexOfCurrentFlashcard = index,
        size = size)
  }

  /**
   * Checks if the user can go back to the previous flashcard.
   *
   * @return true if the user can go back, false otherwise.
   */
  fun canGoBack(): Boolean {
    val index = getIndexBackward()
    return if (index == -1) false else listOfAllFlashcard[index] != null
  }

  /**
   * Checks if the user can go forward to the next flashcard.
   *
   * @return true if the user can go forward, false otherwise.
   */
  fun canGoForward(): Boolean {
    val index = getIndexForward()
    return listOfAllFlashcard[index] != null
  }

  /**
   * Checks if the user can go forward to the twice next flashcard.
   *
   * @return true if the user can go forward, false otherwise.
   */
  fun canGoTwiceForward(): Boolean {
    val index = getIndexForward(getIndexForward())
    return canGoForward() && listOfAllFlashcard[index] != null
  }

  /**
   * Gets the index of the next flashcard.
   *
   * @param index The index of the current flashcard.
   */
  fun getIndexForward(index: Int = indexOfCurrentFlashcard) =
      if (index == MAX_LIST_LENGTH - 2) 1 else index + 1

  /**
   * Gets the index of the previous flashcard.
   *
   * @param index The index of the current flashcard.
   */
  private fun getIndexBackward(index: Int = indexOfCurrentFlashcard) =
      if (index == 1 && listOfAllFlashcard[0] == null) MAX_LIST_LENGTH - 2 else index - 1
}

/**
 * Replaces the element at the given index with the given flashcard ID.
 *
 * @param i The index of the element to replace.
 * @param flashcardId The flashcard ID to replace the element with.
 */
private fun <E> List<E>.replaceAt(i: Int, flashcardId: E): List<E?> {
  return this.mapIndexed { index, e ->
    if (index == i) {
      flashcardId
    } else {
      e
    }
  }
}
