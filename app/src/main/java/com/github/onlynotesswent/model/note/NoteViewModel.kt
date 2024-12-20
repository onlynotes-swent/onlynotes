package com.github.onlynotesswent.model.note

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.onlynotesswent.model.cache.CacheDatabase
import com.github.onlynotesswent.model.user.UserRepositoryFirestore
import com.github.onlynotesswent.model.user.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

  private val _publicNotes = MutableStateFlow<List<Note>>(emptyList())
  val publicNotes: StateFlow<List<Note>> = _publicNotes.asStateFlow()

  private val _friendsNotes = MutableStateFlow<List<Note>>(emptyList())
  val friendsNotes: StateFlow<List<Note>> = _friendsNotes.asStateFlow()

  private val _userNotes = MutableStateFlow<List<Note>>(emptyList())
  val userNotes: StateFlow<List<Note>> = _userNotes.asStateFlow()

  // root notes from a user displayed on overview Screen
  private val _userRootNotes = MutableStateFlow<List<Note>>(emptyList())
  val userRootNotes: StateFlow<List<Note>> = _userRootNotes.asStateFlow()

  // Saved notes from a user
  private val _userSavedNotes = MutableStateFlow<List<Note>>(emptyList())
  val userSavedNotes: StateFlow<List<Note>> = _userSavedNotes.asStateFlow()

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
                Firebase.firestore, CacheDatabase.getDatabase(context), context))
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
   * Gets all friends only Note documents from a list of following users.
   *
   * @param followingListIds The list of users Ids to retrieve friends only notes from.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getNotesFromFollowingList(
      followingListIds: List<String>,
      onSuccess: (List<Note>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getNotesFromFollowingList(
        followingListIds = followingListIds,
        onSuccess = {
          _friendsNotes.value = it
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
      useCache: Boolean = true
  ) {
    viewModelScope.launch {
      repository.addNote(
          note = note,
          onSuccess = {
            getRootNotesFromUid(note.userId, useCache = true)
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
      useCache: Boolean = true
  ) {
    viewModelScope.launch {
      repository.updateNote(
          note = note,
          onSuccess = {
            getRootNotesFromUid(note.userId)
            if (note.folderId != null) {
              getNotesFromFolder(note.folderId, null, useCache = true)
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
      useCache: Boolean = true
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
      useCache: Boolean = true
  ) {
    viewModelScope.launch {
      repository.deleteAllNotesFromUserId(
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
   * @param userViewModel The user view model. If the function can only be called by a user that is
   *   the owner of the note/folder, this parameter should be null.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun getNotesFromFolder(
      folderId: String,
      userViewModel: UserViewModel?,
      onSuccess: (List<Note>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getNotesFromFolder(
          folderId = folderId,
          userViewModel = userViewModel,
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
      useCache: Boolean = true
  ) {
    viewModelScope.launch {
      repository.deleteNotesFromFolder(
          folderId = folderId,
          onSuccess = {
            getNotesFromFolder(folderId, null, useCache = true)
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Adds a new note to the userSavedNotes list. This note must not already be on the list.
   *
   * @param note The note to add to the user's saved notes list.
   * @param userViewModel The userViewModel to use for adding the note's id to the user's saved
   *   notes list in cloud.
   * @param onSuccess The function to call when the addition is successful.
   * @param onFailure The function to call when the addition fails.
   */
  fun addCurrentUserSavedNote(
      note: Note,
      userViewModel: UserViewModel,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    // If the user's saved notes list is empty, retrieve it from the userViewModel to avoid
    // overriding it (in case the user hasn't tried viewing his saved notes yet, and so the list
    // is not yet fetched). Otherwise, use the saved notes list already in the viewModel.
    if (_userSavedNotes.value.isEmpty()) {
      getCurrentUserSavedNotes(
          userViewModel,
          onSuccess = { setCurrentUserSavedNotes(userViewModel, it + note, onSuccess, onFailure) },
          onFailure = onFailure)
    } else {
      setCurrentUserSavedNotes(userViewModel, _userSavedNotes.value + note, onSuccess, onFailure)
    }
  }

  /**
   * Deletes a note from the userSavedNotes list.
   *
   * @param noteId The id of the note to delete from the user's saved notes list.
   * @param userViewModel The userViewModel to use for deleting the note's id from the user's saved
   *   notes list in cloud.
   * @param onSuccess The function to call when the deletion is successful.
   * @param onFailure The function to call when the deletion fails.
   */
  fun deleteCurrentUserSavedNote(
      noteId: String,
      userViewModel: UserViewModel,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    // If the user's saved notes list is empty, retrieve it from the userViewModel to avoid
    // overriding it (in case the user hasn't tried viewing his saved notes yet, and so the list
    // is not yet fetched). Otherwise, use the saved notes list already in the viewModel.
    if (_userSavedNotes.value.isEmpty()) {
      getCurrentUserSavedNotes(
          userViewModel,
          onSuccess = { savedNotes ->
            setCurrentUserSavedNotes(
                userViewModel, savedNotes.filter { it.id != noteId }, onSuccess, onFailure)
          },
          onFailure = onFailure)
    } else {
      setCurrentUserSavedNotes(
          userViewModel, _userSavedNotes.value.filter { it.id != noteId }, onSuccess, onFailure)
    }
  }

  /**
   * Retrieves the list of saved notes of the current user.
   *
   * @param userViewModel The userViewModel to use for retrieving the saved notes.
   * @param onSuccess The function to call when the retrieval is successful.
   * @param onFailure The function to call when the retrieval fails.
   * @param useCache Whether to update data from cache.
   */
  fun getCurrentUserSavedNotes(
      userViewModel: UserViewModel,
      onSuccess: (List<Note>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = true
  ) {
    // Get the list of saved document IDs of type NOTE for the current user from the userViewModel,
    // and retrieve them
    userViewModel.getSavedDocumentIdsOfType(
        documentType = UserRepositoryFirestore.SavedDocumentType.NOTE,
        onSuccess = { documentIds ->
          viewModelScope.launch {
            repository.getSavedNotesByIds(
                savedNotesIds = documentIds,
                currentUser = userViewModel.currentUser.value!!,
                onSuccess = { savedNotes, nonSaveableNotesIds ->
                  _userSavedNotes.value = savedNotes

                  // Delete the no longer saveable (deleted or privated) notes from
                  // the user's saved notes list
                  for (noteId in nonSaveableNotesIds) {
                    userViewModel.deleteSavedDocumentIdOfType(
                        documentType = UserRepositoryFirestore.SavedDocumentType.NOTE,
                        documentId = noteId)
                  }

                  onSuccess(savedNotes)
                },
                onFailure = onFailure,
                useCache = useCache)
          }
        },
        onFailure = onFailure)
  }

  /**
   * Sets the current user's saved notes list with the given list of notes.
   *
   * @param userViewModel The userViewModel to use for updating the user's saved notes list in
   *   cloud.
   * @param notes The new list of saved notes for the user.
   * @param onSuccess The function to call when the addition is successful.
   * @param onFailure The function to call when the addition fails.
   */
  fun setCurrentUserSavedNotes(
      userViewModel: UserViewModel,
      notes: List<Note>,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    userViewModel.setSavedDocumentIdsOfType(
        documentType = UserRepositoryFirestore.SavedDocumentType.NOTE,
        documentIds = notes.map { it.id },
        onSuccess = {
          _userSavedNotes.value = notes
          onSuccess()
        },
        onFailure = onFailure)
  }
}
