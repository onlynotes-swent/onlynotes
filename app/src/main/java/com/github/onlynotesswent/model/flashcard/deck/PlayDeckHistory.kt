package com.github.onlynotesswent.model.flashcard.deck

import android.util.Log

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
            if (it == 1) {
              currentFlashcardId
            } else {
              null
            }
          },
      currentFlashcardId = currentFlashcardId,
      indexOfCurrentFlashcard = 1,
      size = 1)

  companion object {
    const val MAX_LIST_LENGTH = 100
    // because the first and last element are always null  and
    // one null element witch limit the next and previous flashcard
    const val MAX_SIZE = MAX_LIST_LENGTH - 3
  }

  /**
   * epic
   *
   * @param flashcardId The ID of the flashcard to add.
   */
  fun goForwardWithNewFlashcard(flashcardId: String): PlayDeckHistory {
    val index = getIndexForward()
    Log.e("PlayDeckHistory", "goForwardWithNewFlashcard: $index")

    val nextIndex = if (index == MAX_LIST_LENGTH - 2) 1 else index + 1

    if (size == MAX_SIZE) {
      return PlayDeckHistory(
          listOfAllFlashcard =
              listOfAllFlashcard.replaceAt(index, flashcardId).replaceAt(nextIndex, null),
          currentFlashcardId = flashcardId,
          indexOfCurrentFlashcard = index,
          size = size)
    }
    return PlayDeckHistory(
        listOfAllFlashcard = listOfAllFlashcard.replaceAt(index, flashcardId),
        currentFlashcardId = flashcardId,
        indexOfCurrentFlashcard = index,
        size = size + 1)
  }

  /**
   * Goes back to the previous flashcard.
   *
   * @return The updated PlayDeckHistory.
   */
  fun goBack(): PlayDeckHistory {
    val index = getIndexBackward()
    Log.e("PlayDeckHistory", "goBack: $index")
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
    Log.e("PlayDeckHistory", "goForward: $index")
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
    Log.e("PlayDeckHistory", "can go back: $index")
    return listOfAllFlashcard[index] != null
  }

  /** Checks if the user can go forward to the next flashcard. */
  fun canGoForward(): Boolean {
    val index = getIndexForward()
    Log.e("PlayDeckHistory", "can go forward: $index")
    return listOfAllFlashcard[index] != null
  }

  private fun getIndexForward() =
      if (indexOfCurrentFlashcard == MAX_LIST_LENGTH - 2) 1 else indexOfCurrentFlashcard + 1

  private fun getIndexBackward() =
      if (indexOfCurrentFlashcard == 1) MAX_LIST_LENGTH - 2 else indexOfCurrentFlashcard - 1
}

private fun <E> List<E>.replaceAt(i: Int, flashcardId: E): List<E?> {
  return this.mapIndexed { index, e ->
    if (index == i) {
      flashcardId
    } else {
      e
    }
  }
}
