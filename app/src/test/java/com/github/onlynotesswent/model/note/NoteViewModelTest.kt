package com.github.onlynotesswent.model.note

import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteViewModelTest {
  private lateinit var mockNoteRepository: NoteRepository
  private lateinit var noteViewModel: NoteViewModel

  private val testNote =
      Note(
          id = "1",
          title = "title",
          date = Timestamp.now(),
          lastModified = Timestamp.now(),
          visibility = Visibility.PUBLIC,
          userId = "1",
          folderId = "1",
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
      )

  private val friendNote =
      Note(
          id = "2",
          title = "title2",
          date = Timestamp.now(),
          lastModified = Timestamp.now(),
          visibility = Visibility.FRIENDS,
          userId = "1",
          folderId = "1",
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
      )

  @Before
  fun setUp() {
    mockNoteRepository = mock(NoteRepository::class.java)
    noteViewModel = NoteViewModel(mockNoteRepository)
  }

  @Test
  fun getNewUid() {
    `when`(mockNoteRepository.getNewUid()).thenReturn("uid")
    assertThat(noteViewModel.getNewUid(), `is`("uid"))
  }

  @Test
  fun initCallsRepository() {
    verify(mockNoteRepository).init(any())
  }

  @Test
  fun getPublicNotesCallsRepository() {
    `when`(mockNoteRepository.getPublicNotes(any(), any())).thenAnswer {
      val onSuccess: (List<Note>) -> Unit = it.getArgument(0)
      onSuccess(listOf(testNote))
    }
    noteViewModel.getPublicNotes()
    assertEquals(noteViewModel.publicNotes.value, listOf(testNote))
  }

  @Test
  fun getNotesFromFollowingListCallsRepository() {
    `when`(mockNoteRepository.getNotesFromFollowingList(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Note>) -> Unit = it.getArgument(1)
      onSuccess(listOf(friendNote))
    }
    noteViewModel.getNotesFromFollowingList(listOf("1"))
    assertEquals(noteViewModel.friendsNotes.value, listOf(friendNote))
  }

  @Test
  fun getNotesFromUidCallsRepository() = runTest {
    `when`(mockNoteRepository.getNotesFromUid(any(), any(), any(), any())).thenAnswer {
      val onSuccess: (List<Note>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testNote))
    }
    noteViewModel.getNotesFromUid(testNote.userId)
    assertEquals(noteViewModel.userNotes.value, listOf(testNote))
  }

  @Test
  fun getRootNotesFromUidCallsRepository() = runTest {
    `when`(mockNoteRepository.getRootNotesFromUid(any(), any(), any(), any())).thenAnswer {
      val onSuccess: (List<Note>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testNote))
    }
    noteViewModel.getRootNotesFromUid(testNote.userId)
    assertEquals(noteViewModel.userRootNotes.value, listOf(testNote))
  }

  @Test
  fun getNoteByIdCallsRepository() = runTest {
    `when`(mockNoteRepository.getNoteById(any(), any(), any(), any())).thenAnswer {
      val onSuccess: (Note) -> Unit = it.getArgument(1)
      onSuccess(testNote)
    }
    noteViewModel.getNoteById(testNote.userId)
    assertEquals(noteViewModel.selectedNote.value, testNote)
  }

  @Test
  fun addNoteCallsRepository() = runTest {
    `when`(mockNoteRepository.addNote(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    noteViewModel.addNote(testNote, { onSuccessCalled = true })
    assert(onSuccessCalled)

    // To test default parameters
    noteViewModel.addNote(testNote)
    verify(mockNoteRepository, times(2)).addNote(eq(testNote), any(), any(), any())
  }

  @Test
  fun updateNoteCallsRepository() = runTest {
    `when`(mockNoteRepository.updateNote(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    noteViewModel.updateNote(testNote, { onSuccessCalled = true })
    assert(onSuccessCalled)

    // To test default parameters
    noteViewModel.updateNote(testNote)
    verify(mockNoteRepository, times(2)).updateNote(eq(testNote), any(), any(), any())
  }

  @Test
  fun deleteNoteByIdCallsRepository() = runTest {
    `when`(mockNoteRepository.deleteNoteById(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    noteViewModel.deleteNoteById(testNote.id, testNote.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)

    // To test default parameters
    noteViewModel.deleteNoteById("1", "1")
    verify(mockNoteRepository, times(2)).deleteNoteById(eq("1"), any(), any(), any())
  }

  @Test
  fun deleteNotesFromUser() = runTest {
    `when`(mockNoteRepository.deleteNotesFromUid(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    noteViewModel.deleteNotesFromUid(testNote.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)

    // To test default parameters
    noteViewModel.deleteNotesFromUid("1")
    verify(mockNoteRepository, times(2)).deleteNotesFromUid(eq("1"), any(), any(), any())
  }

  @Test
  fun getNotesFromUidFolderCallsRepository() = runTest {
    `when`(mockNoteRepository.getNotesFromFolder(any(), anyOrNull(), any(), any(), any()))
        .thenAnswer {
          val onSuccess: (List<Note>) -> Unit = it.getArgument(1)
          onSuccess(listOf(testNote))
        }
    noteViewModel.getNotesFromFolder(testNote.folderId!!, null)
    assertEquals(noteViewModel.folderNotes.value, listOf(testNote))
  }

  @Test
  fun deleteNotesFromFolderCallsRepository() = runTest {
    noteViewModel.deleteNotesFromFolder("1")
    verify(mockNoteRepository).deleteNotesFromFolder(eq("1"), any(), any(), any())
  }

  @Test
  fun updateNoteUpdatesStatesWhenSuccess() = runTest {
    `when`(mockNoteRepository.updateNote(eq(testNote), any(), any(), any())).thenAnswer { invocation
      ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    noteViewModel.updateNote(testNote)

    verify(mockNoteRepository).updateNote(eq(testNote), any(), any(), any())
    verify(mockNoteRepository).getRootNotesFromUid(eq("1"), any(), any(), any())
    verify(mockNoteRepository).getNotesFromFolder(eq("1"), anyOrNull(), any(), any(), any())
  }

  @Test
  fun draggedNoteUpdatesCorrectly() {
    noteViewModel.draggedNote(testNote)
    assertThat(noteViewModel.draggedNote.value, `is`(testNote))
  }
}
