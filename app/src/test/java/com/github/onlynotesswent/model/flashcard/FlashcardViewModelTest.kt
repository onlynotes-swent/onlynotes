package com.github.onlynotesswent.model.flashcard

import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
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
      TextFlashcard(
          id = "1",
          front = "front",
          back = "back",
          lastReviewed = Timestamp.now(),
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
    verify(flashcardRepository, timeout(1000)).init(any())
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
    `when`(flashcardRepository.getFlashcardsFrom(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Flashcard>) -> Unit = it.getArgument(1)
      onSuccess(listOf(flashcard))
    }
    flashcardViewModel.getFlashcardsFrom(flashcard.userId, { assert(true) })
    assertEquals(flashcardViewModel.userFlashcards.value, listOf(flashcard))
  }

  @Test
  fun getFlashcardByIdCallsRepository() {
    `when`(flashcardRepository.getFlashcardById(any(), any(), any())).thenAnswer {
      val onSuccess: (Flashcard) -> Unit = it.getArgument(1)
      onSuccess(flashcard)
    }

    flashcardViewModel.getFlashcardById(flashcard.id, { assert(true) })
    assertEquals(flashcardViewModel.selectedFlashcard.value, flashcard)
  }

  @Test
  fun getFlashcardsByFolderCallsRepository() {
    `when`(flashcardRepository.getFlashcardsByFolder(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Flashcard>) -> Unit = it.getArgument(1)
      onSuccess(listOf(flashcard))
    }
    flashcardViewModel.getFlashcardsByFolder(flashcard.folderId!!, { assert(true) })
    assertEquals(flashcardViewModel.folderFlashcards.value, listOf(flashcard))
  }

  @Test
  fun getFlashcardsByNoteCallsRepository() {
    `when`(flashcardRepository.getFlashcardsByNote(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Flashcard>) -> Unit = it.getArgument(1)
      onSuccess(listOf(flashcard))
    }
    flashcardViewModel.getFlashcardsByNote(flashcard.noteId!!, { assert(true) })
    assertEquals(flashcardViewModel.noteFlashcards.value, listOf(flashcard))
  }

  @Test
  fun addFlashcardCallsRepository() {
    `when`(flashcardRepository.addFlashcard(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    flashcardViewModel.addFlashcard(flashcard, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun updateFlashcardCallsRepository() {
    `when`(flashcardRepository.updateFlashcard(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    flashcardViewModel.updateFlashcard(flashcard, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun deleteFlashcardCallsRepository() {
    flashcardViewModel.deleteFlashcard(flashcard)
    verify(flashcardRepository).deleteFlashcard(eq(flashcard), any(), any())
  }
}
