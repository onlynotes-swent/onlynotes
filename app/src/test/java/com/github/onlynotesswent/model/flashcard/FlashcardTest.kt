package com.github.onlynotesswent.model.flashcard

import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import org.junit.Test

class FlashcardTest {

  @Test
  fun `test decks`() {
    val deck = Deck(id = "1", name = "deck", userId = "2", folderId = "3")
    assert(deck.id == "1")
    assert(deck.name == "deck")
    assert(deck.userId == "2")
    assert(deck.folderId == "3")
    assert(deck.flashcardIds.isEmpty())
  }

  @Test
  fun `test flashcard creation`() {
    val flashcard =
        MCQFlashcard(
            id = "1",
            front = "front",
            back = "back",
            fakeBacks = listOf("fake1", "fake2"),
            lastReviewed = Timestamp(0, 0),
            userId = "2",
            folderId = "3",
            noteId = "4")

    assert(flashcard.id == "1")
    assert(flashcard.front == "front")
    assert(flashcard.back == "back")
    assert(flashcard.fakeBacks == listOf("fake1", "fake2"))
    assert(flashcard.lastReviewed == Timestamp(0, 0))
    assert(flashcard.userId == "2")
    assert(flashcard.folderId == "3")
    assert(flashcard.noteId == "4")
  }

  @Test
  fun `test flashcard equality and hash`() {
    val flashcard1 =
        MCQFlashcard(
            id = "1",
            front = "front",
            back = "back",
            fakeBacks = listOf("fake1", "fake2"),
            lastReviewed = Timestamp(0, 0),
            userId = "2",
            folderId = "3",
            noteId = "4")
    val flashcard2 =
        MCQFlashcard(
            id = "1",
            front = "front",
            back = "back",
            fakeBacks = listOf("fake1", "fake2"),
            lastReviewed = Timestamp(0, 0),
            userId = "2",
            folderId = "3",
            noteId = "4")
    assertEquals(flashcard1, flashcard2)
    assertEquals(flashcard1.hashCode(), flashcard2.hashCode())
  }

  @Test
  fun `test flashcard to from map`() {
    val flashcard =
        MCQFlashcard(
            id = "1",
            front = "front",
            back = "back",
            fakeBacks = listOf("fake1", "fake2"),
            lastReviewed = Timestamp(0, 0),
            userId = "2",
            folderId = "3",
            noteId = "4")
    val mcqMap = flashcard.toMap()
    val mcqNewFlashcard = Flashcard.from(Flashcard.Type.MCQ, mcqMap)
    assertEquals(flashcard, mcqNewFlashcard)

    val textFlashcard =
        TextFlashcard(
            id = "1",
            front = "front",
            back = "back",
            lastReviewed = Timestamp(0, 0),
            userId = "2",
            folderId = "3",
            noteId = "4")

    val textMap = textFlashcard.toMap()
    val newTextFlashcard = Flashcard.from(Flashcard.Type.TEXT, textMap)
    assertEquals(textFlashcard, newTextFlashcard)

    val imageFlashcard =
        ImageFlashcard(
            id = "1",
            front = "front",
            back = "back",
            imageUrl = "url",
            lastReviewed = Timestamp(0, 0),
            userId = "2",
            folderId = "3",
            noteId = "4")

    val imageMap = imageFlashcard.toMap()
    val newImageFlashcard = Flashcard.from(Flashcard.Type.IMAGE, imageMap)
    assertEquals(imageFlashcard, newImageFlashcard)
  }

  @Test
  fun `test flashcard type from string`() {
    assertEquals(Flashcard.Type.TEXT, Flashcard.Type.fromString("TEXT"))
    assertEquals(Flashcard.Type.IMAGE, Flashcard.Type.fromString("IMAGE"))
    assertEquals(Flashcard.Type.MCQ, Flashcard.Type.fromString("MCQ"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test flashcard type from string exception`() {
    Flashcard.Type.fromString("INVALID")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test flashcard type from string exception empty`() {
    Flashcard.Type.fromString("")
  }
}
