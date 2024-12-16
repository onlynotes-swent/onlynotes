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
    assert(userFlashcard.lastReviewed > now)
    userFlashcard = userFlashcard.copy(level = UserFlashcard.MAX_FLASHCARD_LEVEL)
    userFlashcard = userFlashcard.increaseLevel()
    assert(userFlashcard.level == UserFlashcard.MAX_FLASHCARD_LEVEL)
    userFlashcard = userFlashcard.decreaseLevel()
    assert(userFlashcard.level == UserFlashcard.MAX_FLASHCARD_LEVEL - 1)
  }

    @Test
    fun `fromLevelToFrequency function work properly`(){
        var total=0.0
        val epsilon= 1e-3
        for (i in 0..UserFlashcard.MAX_FLASHCARD_LEVEL){
            val userFlashcard = UserFlashcard(id = "1", lastReviewed = Timestamp.now(), level = i)
            val frequency = userFlashcard.fromLevelToFrequency()
            total+=frequency
            assert(frequency >= 0.0)
            assert(frequency <= 1.0)
        }
        assert(total<=1+epsilon&&total>=1-epsilon)
    }
}
