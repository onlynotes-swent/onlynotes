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

  private val note =
      Note(
          id = "1",
          type = Type.NORMAL_TEXT,
          title = "title",
          content = "content",
          date = Timestamp.now(),
          public = true,
          userId = "1",
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
  fun getNotesCallsRepository() {
    noteViewModel.getNotes("1")
    verify(noteRepository).getNotes(eq("1"), any(), any())
  }

  @Test
  fun getNoteByIdCallsRepository() {
    noteViewModel.getNoteById("1")
    verify(noteRepository).getNoteById(eq("1"), any(), any())
  }

  @Test
  fun addNoteCallsRepository() {
    noteViewModel.addNote(note, "1")
    verify(noteRepository).addNote(eq(note), any(), any())
  }

  @Test
  fun updateNoteCallsRepository() {
    noteViewModel.updateNote(note, "1")
    verify(noteRepository).updateNote(eq(note), any(), any())
  }

  @Test
  fun deleteNoteByIDCallsRepository() {
    noteViewModel.deleteNoteById("1", "1")
    verify(noteRepository).deleteNoteById(eq("1"), any(), any())
  }
}
