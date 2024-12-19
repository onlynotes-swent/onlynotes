package com.github.onlynotesswent.model.deck

import com.github.onlynotesswent.model.deck.PlayDeckHistory
import org.junit.Test

class PlayDeckHistoryTest {

  @Test
  fun `test goForwardWithNewFlashcard`() {
    val playDeckHistory = PlayDeckHistory(currentFlashcardId = "1")
    val newPlayDeckHistory = playDeckHistory.stayWithNewFlashcard("2")
    assert(newPlayDeckHistory.listOfAllFlashcard[0] == "1")
    assert(newPlayDeckHistory.listOfAllFlashcard[1] == "2")
    assert(newPlayDeckHistory.listOfAllFlashcard[2] == null)
    assert(newPlayDeckHistory.currentFlashcardId == "1")
    assert(newPlayDeckHistory.indexOfCurrentFlashcard == 0)
    assert(newPlayDeckHistory.size == 2)
  }

  @Test
  fun `test goForwardWithNewFlashcard go back and forward`() {
    val playDeckHistory = PlayDeckHistory(currentFlashcardId = "1")
    assert(playDeckHistory.size == 1)
    assert(!playDeckHistory.canGoBack())

    val newPlayDeckHistory = playDeckHistory.stayWithNewFlashcard("2")
    assert(newPlayDeckHistory.size == 2)
    assert(newPlayDeckHistory.currentFlashcardId == "1")

    val newPlayDeckHistory2 = newPlayDeckHistory.goForwardWithTwiceNextFlashcard("3")
    assert(newPlayDeckHistory2.size == 3)
    assert(newPlayDeckHistory2.currentFlashcardId == "2")
    assert(newPlayDeckHistory2.canGoBack())
    assert(newPlayDeckHistory2.canGoForward())
    assert(!newPlayDeckHistory2.canGoTwiceForward())

    val newPlayDeckHistory3 = newPlayDeckHistory2.goBack()
    assert(newPlayDeckHistory3.size == 3)
    assert(newPlayDeckHistory3.currentFlashcardId == "1")
    assert(!newPlayDeckHistory3.canGoBack())
    assert(newPlayDeckHistory3.canGoForward())
    assert(newPlayDeckHistory3.canGoTwiceForward())

    val newPlayDeckHistory4 = newPlayDeckHistory3.goForward()
    assert(newPlayDeckHistory4.size == 3)
    assert(newPlayDeckHistory4.currentFlashcardId == "2")
    assert(newPlayDeckHistory4.canGoBack())
    assert(newPlayDeckHistory4.canGoForward())
    assert(!newPlayDeckHistory4.canGoTwiceForward())
  }
}
