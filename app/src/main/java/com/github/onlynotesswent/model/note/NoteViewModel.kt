package com.github.onlynotesswent.model.note

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.onlynotesswent.model.cache.NoteDatabase
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

  private val _publicNotes = MutableStateFlow<List<Note>>(emptyList())
  val publicNotes: StateFlow<List<Note>> = _publicNotes.asStateFlow()

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
    fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
      initializer {
        NoteViewModel(
            NoteRepositoryFirestore(
                Firebase.firestore, NoteDatabase.getNoteDatabase(context), context))
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
   * Sets the dragged Note document.
   *
   * @param draggedNote The dragged Note document.
   */
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
          _publicNotes.value = it
          onSuccess(it)
        },
        onFailure = onFailure)
  }

  /**
   * Gets all Note documents from a user, specified by their ID.
   *
   * @param userId The user ID.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun getNotesFromUid(
      userId: String,
      onSuccess: (List<Note>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getNotesFromUid(
          userId = userId,
          onSuccess = {
            _userNotes.value = it
            onSuccess(it)
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Gets all root Note documents from a user, specified by their ID.
   *
   * @param userId The user ID.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun getRootNotesFromUid(
      userId: String,
      onSuccess: (List<Note>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getRootNotesFromUid(
          userId = userId,
          onSuccess = {
            _userRootNotes.value = it
            onSuccess(it)
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Gets a Note document with ID noteId.
   *
   * @param noteId The ID of the Note document to be fetched.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  fun getNoteById(
      noteId: String,
      onSuccess: (Note) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getNoteById(
          id = noteId,
          onSuccess = {
            _selectedNote.value = it
            onSuccess(it)
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Adds a Note document.
   *
   * @param note The Note document to be added.
   * @param onSuccess The function to call when the addition is successful.
   * @param onFailure The function to call when the addition fails.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  fun addNote(
      note: Note,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.addNote(
          note = note,
          onSuccess = {
            getRootNotesFromUid(note.userId)
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Updates a Note document.
   *
   * @param note The Note document to be updated.
   * @param onSuccess The function to call when the update is successful.
   * @param onFailure The function to call when the update fails.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  fun updateNote(
      note: Note,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.updateNote(
          note = note,
          onSuccess = {
            getRootNotesFromUid(note.userId)
            if (note.folderId != null) {
              getNotesFromFolder(note.folderId)
            }
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Deletes a Note document by its ID.
   *
   * @param noteId The ID of the Note document to be deleted.
   * @param userId The user ID.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  fun deleteNoteById(
      noteId: String,
      userId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.deleteNoteById(
          id = noteId,
          onSuccess = {
            getRootNotesFromUid(userId)
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Deletes all Note documents from a user, specified by their userId.
   *
   * @param userId The user ID.
   * @param onSuccess The function to call when the deletion is successful.
   * @param onFailure The function to call when the deletion fails.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun deleteNotesFromUid(
      userId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.deleteNotesFromUid(
          userId = userId,
          onSuccess = {
            getRootNotesFromUid(userId)
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Retrieves all notes from a folder.
   *
   * @param folderId The ID of the folder to retrieve notes from.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun getNotesFromFolder(
      folderId: String,
      onSuccess: (List<Note>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getNotesFromFolder(
          folderId = folderId,
          onSuccess = {
            _folderNotes.value = it
            onSuccess(it)
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Deletes all notes from a folder.
   *
   * @param folderId The ID of the folder to delete notes from.
   * @param onSuccess The function to call when the deletion is successful.
   * @param onFailure The function to call when the deletion fails.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun deleteNotesFromFolder(
      folderId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.deleteNotesFromFolder(
          folderId = folderId,
          onSuccess = {
            getNotesFromFolder(folderId)
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }
}
