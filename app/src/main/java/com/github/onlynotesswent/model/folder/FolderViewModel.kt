package com.github.onlynotesswent.model.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.onlynotesswent.model.note.NoteViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FolderViewModel(private val repository: FolderRepository) : ViewModel() {

  // TODO Left here for possible future use cases
  private val _publicFolders = MutableStateFlow<List<Folder>>(emptyList())
  val publicFolders: StateFlow<List<Folder>> = _publicFolders.asStateFlow()

  // TODO Left here for possible future use cases
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

  // Dragged folder
  private val _draggedFolder = MutableStateFlow<Folder?>(null)
  val draggedFolder: StateFlow<Folder?> = _draggedFolder.asStateFlow()

  init {
    repository.init {}
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { FolderViewModel(FolderRepositoryFirestore(Firebase.firestore)) }
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
   * Sets the dragged folder.
   *
   * @param folder The dragged folder.
   */
  fun draggedFolder(folder: Folder?) {
    _draggedFolder.value = folder
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
   */
  fun addFolder(folder: Folder, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.addFolder(
        folder = folder,
        onSuccess = {
          onSuccess()
          getRootFoldersFromUid(folder.userId)
        },
        onFailure = onFailure)
  }

  /**
   * Deletes a folder by its ID.
   *
   * @param folderId The ID of the folder to delete.
   * @param userId The ID of the user that owns the folder.
   * @param onSuccess The function to call when the folder is deleted successfully.
   * @param onFailure The function to call when the folder fails to be deleted.
   */
  fun deleteFolderById(
      folderId: String,
      userId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.deleteFolderById(
        folderId = folderId,
        onSuccess = {
          onSuccess()
          getRootFoldersFromUid(userId)
        },
        onFailure = onFailure)
  }

  /**
   * Deletes all folders from a user.
   *
   * @param userId The ID of the user to delete folders notes for.
   * @param onSuccess The function to call when the folders are deleted successfully.
   * @param onFailure The function to call when the folders fail to be deleted.
   */
  fun deleteFoldersByUserId(
      userId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.deleteFoldersByUserId(
        userId = userId,
        onSuccess = {
          onSuccess()
          getRootFoldersFromUid(userId)
        },
        onFailure = onFailure)
  }

  /**
   * Retrieves all folders owned by a user.
   *
   * @param userId The ID of the user to retrieve folders for.
   * @param onSuccess The function to call when the folders are retrieved successfully.
   * @param onFailure The function to call when the folders fail to be retrieved.
   */
  fun getFoldersFromUid(
      userId: String,
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getFoldersFromUid(
        userId = userId,
        onSuccess = {
          onSuccess(it)
          _userFolders.value = it
        },
        onFailure = onFailure)
  }

  /**
   * Retrieves all root folders owned by a user.
   *
   * @param userId The ID of the user to retrieve root folders for.
   * @param onSuccess The function to call when the root folders are retrieved successfully.
   * @param onFailure The function to call when the root folders fail to be retrieved.
   */
  fun getRootFoldersFromUid(
      userId: String,
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getRootFoldersFromUid(
        userId = userId,
        onSuccess = {
          onSuccess(it)
          _userRootFolders.value = it
        },
        onFailure = onFailure)
  }

  /**
   * Retrieves a folder by its ID.
   *
   * @param folderId The ID of the folder to retrieve.
   * @param onSuccess The function to call when the folder is retrieved successfully.
   * @param onFailure The function to call when the folder fails to be retrieved.
   */
  fun getFolderById(
      folderId: String,
      onSuccess: (Folder) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getFolderById(
        folderId = folderId,
        onSuccess = {
          onSuccess(it)
          _selectedFolder.value = it
        },
        onFailure = onFailure)
  }

  /**
   * Updates an existing folder.
   *
   * @param folder The folder with updated information.
   * @param onSuccess The function to call when the folder is updated successfully.
   * @param onFailure The function to call when the folder fails to be updated.
   */
  fun updateFolder(
      folder: Folder,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.updateFolder(
        folder = folder,
        onSuccess = {
          onSuccess()
          getRootFoldersFromUid(folder.userId)
          if (folder.parentFolderId != null) {
            getSubFoldersOf(folder.parentFolderId)
          }
        },
        onFailure = onFailure)
  }

  /**
   * Retrieves all children folders of a parent folder.
   *
   * @param parentFolderId The ID of the parent folder.
   * @param onSuccess The function to call when the children folders are retrieved successfully.
   * @param onFailure The function to call when the children folders fail to be retrieved.
   */
  fun getSubFoldersOf(
      parentFolderId: String,
      onSuccess: (List<Folder>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getSubFoldersOf(
        parentFolderId = parentFolderId,
        onSuccess = {
          onSuccess(it)
          _folderSubFolders.value = it
        },
        onFailure = onFailure)
  }

  fun getSubFoldersOfNoStateUpdate(
      parentFolderId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getSubFoldersOf(
        parentFolderId = parentFolderId, onSuccess = { onSuccess(it) }, onFailure = onFailure)
  }

  /**
   * Retrieves all public folders.
   *
   * @param onSuccess The function to call when the public folders are retrieved successfully.
   * @param onFailure The function to call when the public folders fail to be retrieved.
   */
  fun getPublicFolders(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.getPublicFolders(
        onSuccess = {
          onSuccess()
          _publicFolders.value = it
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
   */
  fun deleteFolderContents(
      folder: Folder,
      noteViewModel: NoteViewModel,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.deleteFolderContents(
        folder = folder,
        noteViewModel = noteViewModel,
        onSuccess = {
          onSuccess()
          getSubFoldersOf(folder.id)
        },
        onFailure = onFailure)
  }
}
