package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
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
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteViewModelTest {
  private lateinit var noteRepository: NoteRepository
  private lateinit var noteViewModel: NoteViewModel

  private val testNote =
      Note(
          id = "1",
          title = "title",
          content = "content",
          date = Timestamp.now(),
          visibility = Note.Visibility.DEFAULT,
          userId = "1",
          folderId = "1",
          noteClass = Note.Class("CS-100", "Sample Class", 2024, "path"),
          image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

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
    noteViewModel.getPublicNotes()
    verify(noteRepository).getPublicNotes(any(), any())
  }

  @Test
  fun getNotesFromCallsRepository() {
    noteViewModel.getNotesFrom("1")
    verify(noteRepository).getNotesFrom(eq("1"), any(), any())
  }

  @Test
  fun getRootNotesFromCallsRepository() {
    noteViewModel.getRootNotesFrom("1")
    verify(noteRepository).getRootNotesFrom(eq("1"), any(), any())
  }

  @Test
  fun getNoteByIdCallsRepository() {
    noteViewModel.getNoteById("1")
    verify(noteRepository).getNoteById(eq("1"), any(), any())
  }

  @Test
  fun addNoteCallsRepository() {
    noteViewModel.addNote(testNote, "1")
    verify(noteRepository).addNote(eq(testNote), any(), any())
  }

  @Test
  fun updateNoteCallsRepository() {
    noteViewModel.updateNote(testNote, "1")
    verify(noteRepository).updateNote(eq(testNote), any(), any())
  }

  @Test
  fun deleteNoteByIdCallsRepository() {
    noteViewModel.deleteNoteById("1", "1")
    verify(noteRepository).deleteNoteById(eq("1"), any(), any())
  }

  @Test
  fun deleteNotesFromUser() {
    noteViewModel.deleteNotesByUserId("1")
    verify(noteRepository).deleteNotesByUserId(eq("1"), any(), any())
  }

  @Test
  fun getNotesFromFolderCallsRepository() {
    noteViewModel.getNotesFromFolder("1")
    verify(noteRepository).getNotesFromFolder(eq("1"), any(), any())
  }
}
