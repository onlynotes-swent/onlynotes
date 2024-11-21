package com.github.onlynotesswent.model.note

import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
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
import org.mockito.kotlin.times
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteViewModelTest {
  private lateinit var noteRepository: NoteRepository
  private lateinit var noteViewModel: NoteViewModel

  private val testNote =
      Note(
          id = "1",
          title = "title",
          date = Timestamp.now(),
          visibility = Visibility.DEFAULT,
          userId = "1",
          folderId = "1",
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
      )

  @Before
  fun setUp() {
    noteRepository = mock(NoteRepository::class.java)
    noteViewModel = NoteViewModel(noteRepository)
  }

  @Test
  fun getNewUid() {
    `when`(noteRepository.getNewUid()).thenReturn("uid")
    assertThat(noteViewModel.getNewUid(), `is`("uid"))
  }

  @Test
  fun initCallsRepository() {
    verify(noteRepository).init(any())
  }

  @Test
  fun getPublicNotesCallsRepository() {
    `when`(noteRepository.getPublicNotes(any(), any())).thenAnswer {
      val onSuccess: (List<Note>) -> Unit = it.getArgument(0)
      onSuccess(listOf(testNote))
    }
    noteViewModel.getPublicNotes()
    assertEquals(noteViewModel.publicNotes.value, listOf(testNote))
  }

  @Test
  fun getNotesFromCallsRepository() {
    `when`(noteRepository.getNotesFrom(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Note>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testNote))
    }
    noteViewModel.getNotesFrom(testNote.userId)
    assertEquals(noteViewModel.userNotes.value, listOf(testNote))
  }

  @Test
  fun getRootNotesFromCallsRepository() {
    `when`(noteRepository.getRootNotesFrom(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Note>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testNote))
    }
    noteViewModel.getRootNotesFrom(testNote.userId)
    assertEquals(noteViewModel.userRootNotes.value, listOf(testNote))
  }

  @Test
  fun getNoteByIdCallsRepository() {
    `when`(noteRepository.getNoteById(any(), any(), any())).thenAnswer {
      val onSuccess: (Note) -> Unit = it.getArgument(1)
      onSuccess(testNote)
    }
    noteViewModel.getNoteById(testNote.userId)
    assertEquals(noteViewModel.selectedNote.value, testNote)
  }

  @Test
  fun addNoteCallsRepository() {
    `when`(noteRepository.addNote(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    noteViewModel.addNote(testNote, { onSuccessCalled = true })
    assert(onSuccessCalled)

    // To test default parameters
    noteViewModel.addNote(testNote)
    verify(noteRepository, times(2)).addNote(eq(testNote), any(), any())
  }

  @Test
  fun updateNoteCallsRepository() {
    `when`(noteRepository.updateNote(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    noteViewModel.updateNote(testNote, { onSuccessCalled = true })
    assert(onSuccessCalled)

    // To test default parameters
    noteViewModel.updateNote(testNote)
    verify(noteRepository, times(2)).updateNote(eq(testNote), any(), any())
  }

  @Test
  fun deleteNoteByIdCallsRepository() {
    `when`(noteRepository.deleteNoteById(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    noteViewModel.deleteNoteById(testNote.id, testNote.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)

    // To test default parameters
    noteViewModel.deleteNoteById("1", "1")
    verify(noteRepository, times(2)).deleteNoteById(eq("1"), any(), any())
  }

  @Test
  fun deleteNotesFromUser() {
    `when`(noteRepository.deleteNotesByUserId(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    noteViewModel.deleteNotesByUserId(testNote.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)

    // To test default parameters
    noteViewModel.deleteNotesByUserId("1")
    verify(noteRepository, times(2)).deleteNotesByUserId(eq("1"), any(), any())
  }

  @Test
  fun getNotesFromFolderCallsRepository() {
    `when`(noteRepository.getNotesFromFolder(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Note>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testNote))
    }
    noteViewModel.getNotesFromFolder(testNote.folderId!!)
    assertEquals(noteViewModel.folderNotes.value, listOf(testNote))

  }
}
