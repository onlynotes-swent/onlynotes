package com.github.onlynotesswent.model.note

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.onlynotesswent.model.cache.getNoteDatabase
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

  init {
    repository.init { getPublicNotes() }
  }

  companion object {
    fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
      initializer {
        NoteViewModel(NoteRepositoryFirestore(Firebase.firestore, getNoteDatabase(context)))
      }
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
   * @param useCache Whether to update data from cache. Should be true only if [userID] is the
   *   current user.
   */
  fun getNotesFrom(userID: String, useCache: Boolean = false) {
    repository.getNotesFrom(
        userID, onSuccess = { _userNotes.value = it }, onFailure = {}, useCache = useCache)
  }

  /**
   * Gets all root Note documents from a user, specified by their ID.
   *
   * @param userID The user ID.
   * @param useCache Whether to update data from cache. Should be true only if [userID] is the
   *   current user.
   */
  fun getRootNotesFrom(userID: String, useCache: Boolean = false) {
    repository.getRootNotesFrom(
        userID, onSuccess = { _userRootNotes.value = it }, onFailure = {}, useCache = useCache)
  }

  /**
   * Gets a Note document with ID noteId.
   *
   * @param noteId The ID of the Note document to be fetched.
   * @param useCache Whether to update data from cache. Should be true only if userIf of the note is
   *   the current user.
   */
  fun getNoteById(noteId: String, useCache: Boolean = false) {
    repository.getNoteById(
        id = noteId, onSuccess = { _selectedNote.value = it }, onFailure = {}, useCache = useCache)
  }

  /**
   * Adds a Note document.
   *
   * @param note The Note document to be added.
   * @param userID The user ID.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  fun addNote(note: Note, userID: String, useCache: Boolean = false) {
    repository.addNote(
        note = note, onSuccess = { getRootNotesFrom(userID) }, onFailure = {}, useCache = useCache)
  }

  /**
   * Updates a Note document.
   *
   * @param note The Note document to be updated.
   * @param userID The user ID.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  fun updateNote(note: Note, userID: String, useCache: Boolean = false) {
    repository.updateNote(
        note = note, onSuccess = { getRootNotesFrom(userID) }, onFailure = {}, useCache = useCache)
  }

  /**
   * Deletes a Note document by its ID.
   *
   * @param noteId The ID of the Note document to be deleted.
   * @param userID The user ID.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  fun deleteNoteById(noteId: String, userID: String, useCache: Boolean = false) {
    repository.deleteNoteById(
        id = noteId, onSuccess = { getRootNotesFrom(userID) }, onFailure = {}, useCache = useCache)
  }

  /**
   * Deletes all Note documents from a user, specified by their userId.
   *
   * @param userId The user ID.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun deleteNotesByUserId(userId: String, useCache: Boolean = false) {
    repository.deleteNotesByUserId(
        userId, onSuccess = { getRootNotesFrom(userId) }, onFailure = {}, useCache = useCache)
  }

  /**
   * Retrieves all notes from a folder.
   *
   * @param folderId The ID of the folder to retrieve notes from.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun getNotesFromFolder(folderId: String, useCache: Boolean = false) {
    repository.getNotesFromFolder(
        folderId, onSuccess = { _folderNotes.value = it }, onFailure = {}, useCache = useCache)
  }
}
