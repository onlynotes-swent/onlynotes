package com.github.onlynotesswent.model.flashcard

import com.google.firebase.Timestamp
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class FlashcardViewModelTest {
  private lateinit var flashcardRepository: FlashcardRepository
  private lateinit var flashcardViewModel: FlashcardViewModel

  private val flashcard =
      Flashcard(
          id = "1",
          front = "front",
          back = "back",
          nextReview = Timestamp.now(),
          userId = "2",
          folderId = "3")

  @Before
  fun setUp() {
    flashcardRepository = mock(FlashcardRepository::class.java)
    flashcardViewModel = FlashcardViewModel(flashcardRepository)
  }

  @Test
  fun selectFlashcardUpdatesSelectedFlashcard() {
    flashcardViewModel.selectFlashcard(flashcard)
    assertThat(flashcardViewModel.flashcard.value, `is`(flashcard))
  }

  @Test
  fun getNewUid() {
    `when`(flashcardRepository.getNewUid()).thenReturn(flashcard.id)
    assertThat(flashcardViewModel.getNewUid(), `is`(flashcard.id))
  }

  @Test
  fun getFlashcardsCallsRepository() {
    flashcardViewModel.getFlashcards(flashcard.userId)
    verify(flashcardRepository).getFlashcards(eq(flashcard.userId), any(), any())
  }

  @Test
  fun getFlashcardByIdCallsRepository() {
    flashcardViewModel.getFlashcardById(flashcard.id)
    verify(flashcardRepository).getFlashcardById(eq(flashcard.id), any(), any())
  }

  @Test
  fun getFlashcardsByFolderCallsRepository() {
    flashcardViewModel.getFlashcardsByFolder(flashcard.folderId)
    verify(flashcardRepository).getFlashcardsByFolder(eq(flashcard.folderId), any(), any())
  }

  @Test
  fun addFlashcardCallsRepository() {
    flashcardViewModel.addFlashcard(flashcard)
    verify(flashcardRepository).addFlashcard(eq(flashcard), any(), any())
  }

  @Test
  fun updateFlashcardCallsRepository() {
    flashcardViewModel.updateFlashcard(flashcard)
    verify(flashcardRepository).updateFlashcard(eq(flashcard), any(), any())
  }

  @Test
  fun deleteFlashcardCallsRepository() {
    flashcardViewModel.deleteFlashcard(flashcard)
    verify(flashcardRepository).deleteFlashcard(eq(flashcard), any(), any())
  }
}
