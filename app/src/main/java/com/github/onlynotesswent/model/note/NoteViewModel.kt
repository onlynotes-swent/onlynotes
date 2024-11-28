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

  /**
   * Gets all public Note documents.
   *
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getPublicNotes(onSuccess: (List<Note>) -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.getPublicNotes(
        onSuccess = {
          onSuccess(it)
          _publicNotes.value = it
        },
        onFailure = onFailure)
  }

  /**
   * Gets all Note documents from a user, specified by their ID.
   *
   * @param userId The user ID.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getNotesFrom(
      userId: String,
      onSuccess: (List<Note>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getNotesFrom(
        userId = userId,
        onSuccess = {
          onSuccess(it)
          _userNotes.value = it
        },
        onFailure = onFailure)
  }

  /**
   * Gets all root Note documents from a user, specified by their ID.
   *
   * @param userId The user ID.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getRootNotesFrom(
      userId: String,
      onSuccess: (List<Note>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getRootNotesFrom(
        userId = userId,
        onSuccess = {
          onSuccess(it)
          _userRootNotes.value = it
        },
        onFailure = onFailure)
  }

  /**
   * Gets a Note document with ID noteId.
   *
   * @param noteId The ID of the Note document to be fetched.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getNoteById(
      noteId: String,
      onSuccess: (Note) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getNoteById(
        id = noteId,
        onSuccess = {
          onSuccess(it)
          _selectedNote.value = it
        },
        onFailure = onFailure)
  }

  /**
   * Adds a Note document.
   *
   * @param note The Note document to be added.
   * @param onSuccess The function to call when the addition is successful.
   * @param onFailure The function to call when the addition fails.
   */
  fun addNote(note: Note, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.addNote(
        note = note,
        onSuccess = {
          onSuccess()
          getRootNotesFrom(note.userId)
        },
        onFailure = onFailure)
  }

  /**
   * Updates a Note document.
   *
   * @param note The Note document to be updated.
   * @param onSuccess The function to call when the update is successful.
   * @param onFailure The function to call when the update fails.
   */
  fun updateNote(note: Note, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.updateNote(
        note = note,
        onSuccess = {
          onSuccess()
          getRootNotesFrom(note.userId)
          if (note.folderId != null) {
            getNotesFromFolder(note.folderId)
          }
        },
        onFailure = onFailure)
  }

  /**
   * Deletes a Note document by its ID.
   *
   * @param noteId The ID of the Note document to be deleted.
   * @param userId The user ID.
   */
  fun deleteNoteById(
      noteId: String,
      userId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.deleteNoteById(
        id = noteId,
        onSuccess = {
          onSuccess()
          getRootNotesFrom(userId)
        },
        onFailure = onFailure)
  }

  /**
   * Deletes all Note documents from a user, specified by their userId.
   *
   * @param userId The user ID.
   * @param onSuccess The function to call when the deletion is successful.
   * @param onFailure The function to call when the deletion fails.
   */
  fun deleteNotesByUserId(
      userId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.deleteNotesByUserId(
        userId = userId,
        onSuccess = {
          onSuccess()
          getRootNotesFrom(userId)
        },
        onFailure = onFailure)
  }

  /**
   * Retrieves all notes from a folder.
   *
   * @param folderId The ID of the folder to retrieve notes from.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getNotesFromFolder(
      folderId: String,
      onSuccess: (List<Note>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getNotesFromFolder(
        folderId = folderId,
        onSuccess = {
          onSuccess(it)
          _folderNotes.value = it
        },
        onFailure = onFailure)
  }

  /**
   * Deletes all notes from a folder.
   *
   * @param folderId The ID of the folder to delete notes from.
   * @param onSuccess The function to call when the deletion is successful.
   * @param onFailure The function to call when the deletion fails.
   */
  fun deleteNotesFromFolder(
      folderId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.deleteNotesFromFolder(
        folderId = folderId,
        onSuccess = {
          onSuccess()
          getNotesFromFolder(folderId)
        },
        onFailure = onFailure)
  }
}
