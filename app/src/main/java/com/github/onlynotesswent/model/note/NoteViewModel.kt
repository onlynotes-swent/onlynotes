package com.github.onlynotesswent.model.note

import android.util.Log
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

  // TODO All notes from a user left here for possible future use cases
  private val _userNotes = MutableStateFlow<List<Note>>(emptyList())
  val userNotes: StateFlow<List<Note>> = _userNotes.asStateFlow()

  // root notes from a user displayed on overview Screen
  private val _userRootNotes = MutableStateFlow<List<Note>>(emptyList())
  val userRootNotes: StateFlow<List<Note>> = _userRootNotes.asStateFlow()

  // Notes belonging to a folder
  private val _folderNotes = MutableStateFlow<List<Note>>(emptyList())
  val folderNotes: StateFlow<List<Note>> = _folderNotes.asStateFlow()

  // folderId state to store the current folder ID
  private val _currentFolderId = MutableStateFlow<String?>(null)
  val currentFolderId: StateFlow<String?> = _currentFolderId.asStateFlow()

  private val _selectedNote = MutableStateFlow<Note?>(null)
  val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

  // Dragged note
  private val _draggedNote = MutableStateFlow<Note?>(null)
  val draggedNote: StateFlow<Note?> = _draggedNote.asStateFlow()

  init {
    repository.init { getPublicNotes() }
  }

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
    _currentFolderId.value = folderId
  }

  fun draggedNote(draggedNote: Note?) {
    _draggedNote.value = draggedNote
  }

  /**
   * Sets the selected Note document.
   *
   * @param selectedNote The selected Note document.
   */
  fun selectedNote(selectedNote: Note?) {
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

  /**
   * Gets all Note documents from a user, specified by their ID.
   *
   * @param userID The user ID.
   */
  fun getNotesFrom(userID: String) {
    repository.getNotesFrom(userID, onSuccess = { _userNotes.value = it }, onFailure = {})
  }

  /**
   * Gets all root Note documents from a user, specified by their ID.
   *
   * @param userID The user ID.
   */
  fun getRootNotesFrom(userID: String) {
    repository.getRootNotesFrom(userID, onSuccess = { _userRootNotes.value = it }, onFailure = {})
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
    repository.addNote(note = note, onSuccess = { getRootNotesFrom(userID) }, onFailure = {})
  }

  /**
   * Updates a Note document.
   *
   * @param note The Note document to be updated.
   * @param userID The user ID.
   */
  fun updateNote(note: Note, userID: String) {
    repository.updateNote(
        note = note,
        onSuccess = {
          getRootNotesFrom(userID)
          if (note.folderId != null) {
            getNotesFromFolder(note.folderId)
          }
        }, onFailure = { })
  }

  /**
   * Deletes a Note document by its ID.
   *
   * @param noteId The ID of the Note document to be deleted.
   * @param userID The user ID.
   */
  fun deleteNoteById(noteId: String, userID: String) {
    repository.deleteNoteById(id = noteId, onSuccess = { getRootNotesFrom(userID) }, onFailure = {})
  }

  /**
   * Deletes all Note documents from a user, specified by their userId.
   *
   * @param userId The user ID.
   */
  fun deleteNotesByUserId(userId: String) {
    repository.deleteNotesByUserId(userId, onSuccess = { getRootNotesFrom(userId) }, onFailure = {})
  }

  /**
   * Retrieves all notes from a folder.
   *
   * @param folderId The ID of the folder to retrieve notes from.
   */
  fun getNotesFromFolder(folderId: String) {
    repository.getNotesFromFolder(folderId, onSuccess = { _folderNotes.value = it }, onFailure = {})
  }

  /**
   * Deletes all notes from a folder.
   *
   * @param folderId The ID of the folder to delete notes from.
   */
  fun deleteNotesFromFolder(folderId: String) {
    repository.deleteNotesFromFolder(
        folderId, onSuccess = { getNotesFromFolder(folderId) }, onFailure = {})
  }
}
