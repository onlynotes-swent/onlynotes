package com.github.onlynotesswent.model.flashcard

import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.google.firebase.Timestamp
import org.junit.Test

class FlashcardTest {

  @Test
  fun `test decks`() {
    val deck =
        Deck(
            id = "1",
            name = "deck",
            userId = "2",
            folderId = "3",
            visibility = Visibility.PRIVATE,
            lastModified = Timestamp(0, 0),
            flashcardIds = emptyList())
    assert(deck.id == "1")
    assert(deck.name == "deck")
    assert(deck.userId == "2")
    assert(deck.folderId == "3")
    assert(deck.flashcardIds.isEmpty())
  }

  @Test
  fun `test flashcard creation`() {
    val flashcard =
        Flashcard(
            id = "1",
            front = "front",
            back = "back",
            latexFormula = "test",
            hasImage = true,
            fakeBacks = listOf("fake1", "fake2"),
            userId = "2",
            folderId = "3",
            noteId = "4")

    assert(flashcard.id == "1")
    assert(flashcard.front == "front")
    assert(flashcard.back == "back")
    assert(flashcard.latexFormula == "test")
    assert(flashcard.hasImage)
    assert(flashcard.fakeBacks == listOf("fake1", "fake2"))
    assert(flashcard.userId == "2")
    assert(flashcard.folderId == "3")
    assert(flashcard.noteId == "4")
  }

  @Test
  fun `test methode from User Flashcard`() {
    val now = Timestamp.now()
    var userFlashcard = UserFlashcard(id = "1", lastReviewed = now, level = 3)
    userFlashcard = userFlashcard.increaseLevel()
    assert(userFlashcard.level == 4)
    userFlashcard = userFlashcard.decreaseLevel()
    assert(userFlashcard.level == 3)
    userFlashcard = userFlashcard.resetLevel()
    assert(userFlashcard.level == UserFlashcard.MIN_FLASHCARD_LEVEL)
    userFlashcard = userFlashcard.decreaseLevel()
    assert(userFlashcard.level == UserFlashcard.MIN_FLASHCARD_LEVEL)
    userFlashcard = userFlashcard.updateLastReviewed()
    assert(userFlashcard.lastReviewed >= now)
    userFlashcard = userFlashcard.copy(level = UserFlashcard.MAX_FLASHCARD_LEVEL)
    userFlashcard = userFlashcard.increaseLevel()
    assert(userFlashcard.level == UserFlashcard.MAX_FLASHCARD_LEVEL)
    userFlashcard = userFlashcard.decreaseLevel()
    assert(userFlashcard.level == UserFlashcard.MAX_FLASHCARD_LEVEL - 1)
  }

  @Test
  fun `testWeight function`() {
    val flashcards =
        listOf(
            UserFlashcard(id = "1", level = 0),
            UserFlashcard(id = "1", level = 1),
            UserFlashcard(id = "2", level = 2),
            UserFlashcard(id = "3", level = 3),
            UserFlashcard(id = "4", level = 4),
            UserFlashcard(id = "5", level = 5))
    val totalWeight = UserFlashcard.totalWeight(flashcards)
    var newTotalWeight = 0
    for (flashcard in flashcards) {
      val fWeight = flashcard.fromLevelToWeight()
      assert(fWeight >= 0)
      newTotalWeight += fWeight
    }
    assert(totalWeight == newTotalWeight)
  }

  @Test
  fun `testRandomFlashcard function`() {
    val flashcards =
        listOf(
            UserFlashcard(id = "1", level = 0),
            UserFlashcard(id = "1", level = 1),
            UserFlashcard(id = "2", level = 2),
            UserFlashcard(id = "3", level = 3),
            UserFlashcard(id = "4", level = 4),
            UserFlashcard(id = "5", level = 5),
        )
    val flashcard1 = UserFlashcard.selectRandomFlashcardLinear(flashcards)
    val flashcard2 =
        UserFlashcard.selectRandomFlashcardLinear(flashcards.filter { it != flashcard1 })
    assert(flashcard1 != flashcard2)
    assert(flashcard1 in flashcards)
    assert(flashcard2 in flashcards)
  }
}
