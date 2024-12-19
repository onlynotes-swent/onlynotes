package com.github.onlynotesswent.model.folder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.onlynotesswent.model.cache.CacheDatabase
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class FolderViewModel(private val repository: FolderRepository) : ViewModel() {

  private val _publicFolders = MutableStateFlow<List<Folder>>(emptyList())
  val publicFolders: StateFlow<List<Folder>> = _publicFolders.asStateFlow()

  private val _friendsFolders = MutableStateFlow<List<Folder>>(emptyList())
  val friendsFolders: StateFlow<List<Folder>> = _friendsFolders.asStateFlow()

  private val _userFolders = MutableStateFlow<List<Folder>>(emptyList())
  val userFolders: StateFlow<List<Folder>> = _userFolders.asStateFlow()

  // Root folders from a user
  private val _userRootFolders = MutableStateFlow<List<Folder>>(emptyList())
  val userRootFolders: StateFlow<List<Folder>> = _userRootFolders.asStateFlow()

  // Sub folders of a folder
  private val _folderSubFolders = MutableStateFlow<List<Folder>>(emptyList())
  val folderSubFolders: StateFlow<List<Folder>> = _folderSubFolders.asStateFlow()

  // Parent folder Id
  private val _parentFolderId = MutableStateFlow<String?>(null)
  val parentFolderId: StateFlow<String?> = _parentFolderId.asStateFlow()

  private val _selectedFolder = MutableStateFlow<Folder?>(null)
  val selectedFolder: StateFlow<Folder?> = _selectedFolder.asStateFlow()

  init {
    repository.init {}
  }

  companion object {
    fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
      initializer {
        FolderViewModel(
            FolderRepositoryFirestore(
                Firebase.firestore, CacheDatabase.getDatabase(context), context))
      }
    }
  }

  /**
   * Sets the parent folder ID.
   *
   * @param parentFolderId The parent folder ID.
   */
  fun selectedParentFolderId(parentFolderId: String?) {
    _parentFolderId.value = parentFolderId
  }

  /**
   * Sets the selected folder.
   *
   * @param folder The selected folder.
   */
  fun selectedFolder(folder: Folder) {
    _selectedFolder.value = folder
  }
  /**
   * Clears the currently selected folder.
   *
   * This function resets the `_selectedFolder` state to `null`, effectively deselecting any folder
   * that was previously selected.
   */
  fun clearSelectedFolder() {
    _selectedFolder.value = null
  }

  /**
   * Generates a new folder ID.
   *
   * @return The new folder ID.
   */
  fun getNewFolderId(): String {
    return repository.getNewFolderId()
  }

  /**
   * Adds a Folder to the repository.
   *
   * @param folder The folder to add.
   * @param onSuccess The function to call when the folder is added successfully.
   * @param onFailure The function to call when the folder fails to be added.
   * @param isDeckView A flag indicating if the folder is a deck view.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun addFolder(
      folder: Folder,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      isDeckView: Boolean = false,
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.addFolder(
          folder = folder,
          onSuccess = {
            getRootFoldersFromUserId(folder.userId, isDeckView)
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Deletes a folder by its ID.
   *
   * @param folderId The ID of the folder to delete.
   * @param userId The ID of the user that owns the folder.
   * @param onSuccess The function to call when the folder is deleted successfully.
   * @param onFailure The function to call when the folder fails to be deleted.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   * @param isDeckView A flag indicating if the folder is a deck view.
   */
  fun deleteFolderById(
      folderId: String,
      userId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false,
      isDeckView: Boolean = false
  ) {
    viewModelScope.launch {
      repository.deleteFolderById(
          folderId = folderId,
          onSuccess = {
            getRootFoldersFromUserId(userId, isDeckView)
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Deletes all folders from a user.
   *
   * @param userId The ID of the user to delete folders notes for.
   * @param onSuccess The function to call when the folders are deleted successfully.
   * @param onFailure The function to call when the folders fail to be deleted.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   * @param isDeckView A flag indicating if the folder is a deck view.
   */
  fun deleteAllFoldersFromUserId(
      userId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false,
      isDeckView: Boolean = false
  ) {
    viewModelScope.launch {
      repository.deleteAllFoldersFromUserId(
          userId = userId,
          onSuccess = {
            getRootFoldersFromUserId(userId, isDeckView)
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Retrieves all folders owned by a user.
   *
   * @param userId The ID of the user to retrieve folders for.
   * @param onSuccess The function to call when the folders are retrieved successfully.
   * @param onFailure The function to call when the folders fail to be retrieved.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun getFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getFoldersFromUserId(
          userId = userId,
          onSuccess = {
            _userFolders.value = it
            onSuccess(it)
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Retrieves all root folders owned by a user.
   *
   * @param userId The ID of the user to retrieve root folders for.
   * @param isDeckView A flag indicating if the folder is a deck view.
   * @param onSuccess The function to call when the root folders are retrieved successfully.
   * @param onFailure The function to call when the root folders fail to be retrieved.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun getRootFoldersFromUserId(
      userId: String,
      isDeckView: Boolean = false,
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    if (isDeckView) {
      getRootDeckFoldersFromUserId(userId, onSuccess, onFailure, useCache)
    } else {
      getRootNoteFoldersFromUserId(userId, onSuccess, onFailure, useCache)
    }
  }

  /**
   * Retrieves all root note folders owned by a user.
   *
   * @param userId The ID of the user to retrieve root folders for.
   * @param onSuccess The function to call when the root folders are retrieved successfully.
   * @param onFailure The function to call when the root folders fail to be retrieved.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun getRootNoteFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getRootNoteFoldersFromUserId(
          userId = userId,
          onSuccess = {
            _userRootFolders.value = it
            onSuccess(it)
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Retrieves all root deck folders owned by a user.
   *
   * @param userId The ID of the user to retrieve root folders for.
   * @param onSuccess The function to call when the root folders are retrieved successfully.
   * @param onFailure The function to call when the root folders fail to be retrieved.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun getRootDeckFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getRootDeckFoldersFromUserId(
          userId = userId,
          onSuccess = {
            _userRootFolders.value = it
            onSuccess(it)
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Retrieves a folder by its name.
   *
   * @param name The name of the folder to retrieve.
   * @param userId The ID of the user that owns the folder.
   * @param onFolderNotFound The function to call when the folder is not found.
   * @param onSuccess The function to call when the folders are retrieved successfully.
   * @param onFailure The function to call when the folder fails to be retrieved.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun getDeckFoldersByName(
      name: String,
      userId: String,
      onFolderNotFound: () -> Unit = {},
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getDeckFoldersByName(
          name = name,
          userId = userId,
          onSuccess = { onSuccess(it) },
          onFolderNotFound = onFolderNotFound,
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Retrieves a folder by its ID.
   *
   * @param folderId The ID of the folder to retrieve.
   * @param onSuccess The function to call when the folder is retrieved successfully.
   * @param onFailure The function to call when the folder fails to be retrieved.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun getFolderById(
      folderId: String,
      onSuccess: (Folder) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getFolderById(
          folderId = folderId,
          onSuccess = {
            _selectedFolder.value = it
            onSuccess(it)
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }
  /**
   * Retrieves a folder by its ID, do not update the state of the viewModel.
   *
   * @param folderId The ID of the folder to retrieve.
   * @param onSuccess The function to call when the folder is retrieved successfully.
   * @param onFailure The function to call when the folder fails to be retrieved.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun getFolderByIdNoStateUpdate(
      folderId: String,
      onSuccess: (Folder) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getFolderById(
          folderId = folderId,
          onSuccess = { onSuccess(it) },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Updates an existing folder.
   *
   * @param folder The folder with updated information.
   * @param onSuccess The function to call when the folder is updated successfully.
   * @param onFailure The function to call when the folder fails to be updated.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   * @param isDeckView A flag indicating if the folder is a deck view.
   */
  fun updateFolder(
      folder: Folder,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false,
      isDeckView: Boolean = false
  ) {
    viewModelScope.launch {
      repository.updateFolder(
          folder = folder,
          onSuccess = {
            getRootFoldersFromUserId(folder.userId, isDeckView)
            if (folder.parentFolderId != null) {
              getSubFoldersOf(folder.parentFolderId)
            }
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Retrieves all children folders of a parent folder.
   *
   * @param parentFolderId The ID of the parent folder.
   * @param onSuccess The function to call when the children folders are retrieved successfully.
   * @param onFailure The function to call when the children folders fail to be retrieved.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun getSubFoldersOf(
      parentFolderId: String,
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getSubFoldersOf(
          parentFolderId = parentFolderId,
          onSuccess = {
            _folderSubFolders.value = it
            onSuccess(it)
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Retrieves the subfolders of a given parent folder without updating the state of the ViewModel.
   *
   * @param parentFolderId The unique ID of the parent folder whose subfolders are to be retrieved.
   * @param onSuccess A callback that receives a list of `Folder` objects on successful retrieval.
   * @param onFailure A callback that receives an `Exception` in case of a failure. Defaults to an
   *   empty lambda if not provided.
   *     @param useCache Whether to update data from cache. Should be true only if userId of the
   *       folder is the current user.
   */
  fun getSubFoldersOfNoStateUpdate(
      parentFolderId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.getSubFoldersOf(
          parentFolderId = parentFolderId,
          onSuccess = { onSuccess(it) },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Retrieves all public folders.
   *
   * @param onSuccess The function to call when the public folders are retrieved successfully.
   * @param onFailure The function to call when the public folders fail to be retrieved.
   */
  fun getPublicFolders(
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getPublicFolders(
        onSuccess = {
          _publicFolders.value = it
          onSuccess(it)
        },
        onFailure = onFailure)
  }

  /**
   * Retrieves all friends only folders from a list of following users.
   *
   * @param followingListIds The list of users IDs to retrieve friends only folders from.
   * @param onSuccess The function to call when the friends folders are retrieved successfully.
   * @param onFailure The function to call when the friends folders fail to be retrieved.
   */
  fun getFoldersFromFollowingList(
      followingListIds: List<String>,
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getFoldersFromFollowingList(
        followingListIds = followingListIds,
        onSuccess = {
          _friendsFolders.value = it
          onSuccess(it)
        },
        onFailure = onFailure)
  }

  /**
   * Deletes all elements from a folder.
   *
   * @param folder The folder to delete notes from.
   * @param noteViewModel The Note view model used to delete the folder notes.
   * @param onSuccess The function to call when the folder contents are deleted successfully.
   * @param onFailure The function to call when the folder contents fail to be deleted.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun deleteFolderContents(
      folder: Folder,
      noteViewModel: NoteViewModel,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.deleteFolderContents(
          folder = folder,
          noteViewModel = noteViewModel,
          onSuccess = {
            getSubFoldersOf(folder.id)
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /**
   * Deletes all elements from a folder.
   *
   * @param folder The folder to delete notes from.
   * @param deckViewModel The Deck view model used to delete the folder decks.
   * @param onSuccess The function to call when the folder contents are deleted successfully.
   * @param onFailure The function to call when the folder contents fail to be deleted.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun deleteFolderContents(
      folder: Folder,
      deckViewModel: DeckViewModel,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
      useCache: Boolean = false
  ) {
    viewModelScope.launch {
      repository.deleteFolderContents(
          folder = folder,
          deckViewModel = deckViewModel,
          onSuccess = {
            getSubFoldersOf(folder.id)
            onSuccess()
          },
          onFailure = onFailure,
          useCache = useCache)
    }
  }

  /** Retrieves a folder by its ID asynchronously. */
  @OptIn(ExperimentalCoroutinesApi::class)
  suspend fun getFolderByIdNoStateUpdateAsync(folderId: String): Folder? {
    return suspendCancellableCoroutine { continuation ->
      getFolderByIdNoStateUpdate(
          folderId,
          onSuccess = { folder ->
            continuation.resume(folder) { throwable -> continuation.cancel(throwable) }
          })
    }
  }

  /**
   * Function checks if the folder is a subfolder of another folder.
   *
   * @param folder The folder to check if it is a subfolder.
   * @param parentFolderId The ID of the parent folder.
   * @return True if the folder is a subfolder, false otherwise.
   */
  private suspend fun isSubFolder(folder: Folder, parentFolderId: String): Boolean {
    var currentFolder = folder
    while (currentFolder.parentFolderId != null) {
      if (currentFolder.parentFolderId == parentFolderId) {
        return true
      }
      currentFolder =
          getFolderByIdNoStateUpdateAsync(currentFolder.parentFolderId!!) ?: return false
    }
    return false
  }

  /**
   * Moves a folder to another folder.
   *
   * @param chosenFolder The folder to move the selected folder to.
   * @param onSubFolderError The function to call when the chosen folder is a subfolder of the
   *   selected folder.
   * @param onSuccess The function to call when the folder is moved successfully.
   */
  fun moveFolder(chosenFolder: Folder?, onSubFolderError: () -> Unit, onSuccess: () -> Unit) {
    viewModelScope.launch {
      if (chosenFolder != null && isSubFolder(chosenFolder, selectedFolder.value!!.id)) {
        onSubFolderError()
      } else {
        updateFolder(
            selectedFolder.value!!.copy(
                parentFolderId = chosenFolder?.id, lastModified = Timestamp.now()))
        clearSelectedFolder()
        onSuccess()
      }
    }
  }
}
