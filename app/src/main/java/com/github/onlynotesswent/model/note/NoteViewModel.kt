package com.github.onlynotesswent.model.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

  private val _publicNotes = MutableStateFlow<List<Note>>(emptyList())
  val publicNotes: StateFlow<List<Note>> = _publicNotes.asStateFlow()

  private val _userNotes = MutableStateFlow<List<Note>>(emptyList())
  val userNotes: StateFlow<List<Note>> = _userNotes.asStateFlow()

  private val _folderNotes = MutableStateFlow<List<Note>>(emptyList())
  val folderNotes: StateFlow<List<Note>> = _folderNotes.asStateFlow()

  private val _folderId = MutableStateFlow<String?>(null)
  val folderId: StateFlow<String?> = _folderId.asStateFlow()

  private val _selectedNote = MutableStateFlow<Note?>(null)
  val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

  init {
    repository.init { getPublicNotes() }
  }

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { NoteViewModel(NoteRepositoryFirestore(Firebase.firestore)) }
    }
  }

  /**
   * Sets the folder ID.
   *
   * @param folderId The folder ID.
   */
  fun selectedFolderId(folderId: String?) {
    _folderId.value = folderId
  }

  /**
   * Sets the selected Note document.
   *
   * @param selectedNote The selected Note document.
   */
  fun selectedNote(selectedNote: Note) {
    _selectedNote.value = selectedNote
  }

  /**
   * Generates a new unique ID.
   *
   * @return A new unique ID.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /** Gets all public Note documents. */
  fun getPublicNotes() {
    repository.getPublicNotes(onSuccess = { _publicNotes.value = it }, onFailure = {})
  }

  /** Gets all Note documents from a user, specified by their ID.
   *
   * @param userID The user ID.
   * */
  fun getNotesFrom(userID: String) {
    repository.getNotesFrom(userID, onSuccess = { _userNotes.value = it }, onFailure = {})
  }

  /**
   * Gets a Note document with ID noteId.
   *
   * @param noteId The ID of the Note document to be fetched.
   */
  fun getNoteById(noteId: String) {
    repository.getNoteById(id = noteId, onSuccess = { _selectedNote.value = it }, onFailure = {})
  }

  /**
   * Adds a Note document.
   *
   * @param note The Note document to be added.
   * @param userID The user ID.
   */
  fun addNote(note: Note, userID: String) {
    repository.addNote(note = note, onSuccess = {
      //getNotesFrom(userID)
      getNotesFromFolder(note.folderId!!)
                                                }, onFailure = {})
  }

  /**
   * Updates a Note document.
   *
   * @param note The Note document to be updated.
   * @param userID The user ID.
   */
  fun updateNote(note: Note, userID: String) {
    repository.updateNote(note = note, onSuccess = { getNotesFrom(userID) }, onFailure = {})
  }

  /**
   * Deletes a Note document by its ID.
   *
   * @param noteId The ID of the Note document to be deleted.
   * @param userID The user ID.
   */
  fun deleteNoteById(noteId: String, userID: String) {
    repository.deleteNoteById(id = noteId, onSuccess = { getNotesFrom(userID) }, onFailure = {})
  }

  /**
   * Retrieves all notes from a folder.
   *
   * @param folderId The ID of the folder to retrieve notes from.
   */
  fun getNotesFromFolder(folderId: String) {
    repository.getNotesFromFolder(folderId, onSuccess = { _folderNotes.value = it }, onFailure = {})
  }
}
