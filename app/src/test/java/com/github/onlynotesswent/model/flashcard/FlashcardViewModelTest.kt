package com.github.onlynotesswent.model.flashcard

import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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
          folderId = "3",
          noteId = "4")

  @Before
  fun setUp() {
    flashcardRepository = mock(FlashcardRepository::class.java)

    // Initialize FirebaseApp with Robolectric context
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    FirebaseApp.initializeApp(context)

    flashcardViewModel = FlashcardViewModel(flashcardRepository)
  }

  @Test
  fun initCallsRepository() {
    verify(flashcardRepository, timeout(1000)).init(any(), any())
  }

  @Test
  fun selectFlashcardUpdatesSelectedFlashcard() {
    flashcardViewModel.selectFlashcard(flashcard)
    assertThat(flashcardViewModel.selectedFlashcard.value, `is`(flashcard))
  }

  @Test
  fun getNewUid() {
    `when`(flashcardRepository.getNewUid()).thenReturn(flashcard.id)
    assertThat(flashcardViewModel.getNewUid(), `is`(flashcard.id))
  }

  @Test
  fun getFlashcardsFromCallsRepository() {
    flashcardViewModel.getFlashcardsFrom(flashcard.userId)
    verify(flashcardRepository).getFlashcardsFrom(eq(flashcard.userId), any(), any())
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
  fun getFlashcardsByNoteCallsRepository() {
    flashcardViewModel.getFlashcardsByNote(flashcard.noteId)
    verify(flashcardRepository).getFlashcardsByNote(eq(flashcard.noteId), any(), any())
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
