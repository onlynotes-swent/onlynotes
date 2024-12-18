package com.github.onlynotesswent.model.flashcard.deck

import org.junit.Test

class PlayDeckHistoryTest {

  @Test
  fun `test goForwardWithNewFlashcard`() {
    val playDeckHistory = PlayDeckHistory(currentFlashcardId = "1")
    val newPlayDeckHistory = playDeckHistory.goForwardWithNewFlashcard("2")
    assert(newPlayDeckHistory.listOfAllFlashcard[1] == "1")
    assert(newPlayDeckHistory.listOfAllFlashcard[2] == "2")
    assert(newPlayDeckHistory.listOfAllFlashcard[3] == null)
    assert(newPlayDeckHistory.listOfAllFlashcard[0] == null)
    assert(newPlayDeckHistory.currentFlashcardId == "2")
    assert(newPlayDeckHistory.indexOfCurrentFlashcard == 2)
    assert(newPlayDeckHistory.size == 2)
  }

  @Test
  fun `test goForwardWithNewFlashcard go back and forward`() {
    val playDeckHistory = PlayDeckHistory(currentFlashcardId = "1")
    assert(playDeckHistory.size == 1)
    assert(!playDeckHistory.canGoBack())

    val newPlayDeckHistory = playDeckHistory.goForwardWithNewFlashcard("2")
    assert(newPlayDeckHistory.size == 2)
    assert(newPlayDeckHistory.canGoBack())
    assert(!newPlayDeckHistory.canGoForward())
    assert(newPlayDeckHistory.currentFlashcardId == "2")

    val newPlayDeckHistory2 = newPlayDeckHistory.goBack()
    assert(newPlayDeckHistory2.size == 2)
    assert(!newPlayDeckHistory2.canGoBack())
    assert(newPlayDeckHistory2.canGoForward())

    val newPlayDeckHistory3 = newPlayDeckHistory2.goForward()
    assert(newPlayDeckHistory3.size == 2)
    assert(newPlayDeckHistory3.canGoBack())
    assert(!newPlayDeckHistory3.canGoForward())
  }
}
