package com.github.onlynotesswent.model.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

  private val _notes = MutableStateFlow<List<Note>>(emptyList())
  val notes: StateFlow<List<Note>> = _notes.asStateFlow()

  private val _note = MutableStateFlow<Note?>(null)
  val note: StateFlow<Note?> = _note.asStateFlow()

  init {
    repository
        .init {} // I think we should fetch the user notes when he signs in, so calling getNotes()
    // here. But I may be wrong (we can change this later on).
  }

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(NoteRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  fun selectedNote(note: Note) {
    _note.value = note
  }
  /**
   * Generates a new unique ID.
   *
   * @return A new unique ID.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /** Gets all Note documents. */
  fun getNotes(userID: String) {
    repository.getNotes(userID, onSuccess = { _notes.value = it }, onFailure = {})
  }

  /**
   * Gets a Note document with ID noteId.
   *
   * @param noteId The ID of the Note document to be fetched.
   */
  fun getNoteById(noteId: String) {
    repository.getNoteById(id = noteId, onSuccess = { _note.value = it }, onFailure = {})
  }

  /**
   * Adds a Note document.
   *
   * @param note The Note document to be added.
   * @param userID The user ID.
   */
  fun addNote(note: Note, userID: String) {
    repository.addNote(note = note, onSuccess = { getNotes(userID) }, onFailure = {})
  }

  /**
   * Updates a Note document.
   *
   * @param note The Note document to be updated.
   * @param userID The user ID.
   */
  fun updateNote(note: Note, userID: String) {
    repository.updateNote(note = note, onSuccess = { getNotes(userID) }, onFailure = {})
  }

  /**
   * Deletes a Note document by its ID.
   *
   * @param noteId The ID of the Note document to be deleted.
   * @param userID The user ID.
   */
  fun deleteNoteById(noteId: String, userID: String) {
    repository.deleteNoteById(id = noteId, onSuccess = { getNotes(userID) }, onFailure = {})
  }
}
