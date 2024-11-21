package com.github.onlynotesswent.model.note

import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.google.firebase.Timestamp
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteViewModelTest {
  @Mock private lateinit var mockNoteRepository: NoteRepository
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
    MockitoAnnotations.openMocks(this)
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
    noteViewModel.getPublicNotes()
    verify(mockNoteRepository).getPublicNotes(any(), any())
  }

  @Test
  fun getNotesFromCallsRepository() {
    noteViewModel.getNotesFrom("1")
    verify(mockNoteRepository).getNotesFrom(eq("1"), any(), any())
  }

  @Test
  fun getRootNotesFromCallsRepository() {
    noteViewModel.getRootNotesFrom("1")
    verify(mockNoteRepository).getRootNotesFrom(eq("1"), any(), any())
  }

  @Test
  fun getNoteByIdCallsRepository() {
    noteViewModel.getNoteById("1")
    verify(mockNoteRepository).getNoteById(eq("1"), any(), any())
  }

  @Test
  fun addNoteCallsRepository() {
    noteViewModel.addNote(testNote, "1")
    verify(mockNoteRepository).addNote(eq(testNote), any(), any())
  }

  @Test
  fun updateNoteCallsRepository() {
    noteViewModel.updateNote(testNote, "1")
    verify(mockNoteRepository).updateNote(eq(testNote), any(), any())
  }

  @Test
  fun deleteNoteByIdCallsRepository() {
    noteViewModel.deleteNoteById("1", "1")
    verify(mockNoteRepository).deleteNoteById(eq("1"), any(), any())
  }

  @Test
  fun deleteNotesFromUser() {
    noteViewModel.deleteNotesByUserId("1")
    verify(mockNoteRepository).deleteNotesByUserId(eq("1"), any(), any())
  }

  @Test
  fun getNotesFromFolderCallsRepository() {
    noteViewModel.getNotesFromFolder("1")
    verify(mockNoteRepository).getNotesFromFolder(eq("1"), any(), any())
  }

  @Test
  fun deleteNotesFromFolderCallsRepository() {
    noteViewModel.deleteNotesFromFolder("1")
    verify(mockNoteRepository).deleteNotesFromFolder(eq("1"), any(), any())
  }
}
