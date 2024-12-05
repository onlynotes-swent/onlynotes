package com.github.onlynotesswent.model.folder

import com.github.onlynotesswent.model.note.NoteViewModel

interface FolderRepository {

  /**
   * Generates a new folder ID.
   *
   * @return The new folder ID.
   */
  fun getNewFolderId(): String

  /**
   * Initializes the repository.
   *
   * @param onSuccess Callback to be invoked when initialization is successful.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Creates a new folder.
   *
   * @param folder The folder to create.
   * @param onSuccess Callback to be invoked when the folder is created successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun addFolder(folder: Folder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes a folder by its ID.
   *
   * @param folderId The ID of the folder to delete.
   * @param onSuccess Callback to be invoked when the folder is deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteFolderById(folderId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes all folders belonging to a user.
   *
   * @param userId The ID of the user that owns the folders.
   * @param onSuccess Callback to be invoked if the folders are deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteFoldersByUserId(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  // TODO not sure if this is needed
  /**
   * Retrieves a folder by its ID.
   *
   * @param folderId The ID of the folder to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved folder.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getFolderById(folderId: String, onSuccess: (Folder) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all folders owned by a user.
   *
   * @param userId The ID of the user to retrieve folders for.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all root note folders owned by a user.
   *
   * @param userId The ID of the user to retrieve folders for.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getRootNoteFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all root deck folders owned by a user.
   *
   * @param userId The ID of the user to retrieve decks for.
   * @param onSuccess Callback to be invoked with the retrieved decks.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getRootDeckFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves a folder by its name.
   *
   * @param name The name of the folder to retrieve.
   * @param userId The ID of the user that owns the folder.
   * @param onFolderNotFound Callback to be invoked if the folder is not found.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getDeckFoldersByName(
      name: String,
      userId: String,
      onFolderNotFound: () -> Unit,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Updates an existing folder.
   *
   * @param folder The folder with updated information.
   * @param onSuccess Callback to be invoked when the update is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun updateFolder(folder: Folder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all folders that are children of a parent folder.
   *
   * @param parentFolderId The ID of the parent folder.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getSubFoldersOf(
      parentFolderId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all public folders.
   *
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getPublicFolders(onSuccess: (List<Folder>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes all elements of a folder.
   *
   * @param folder The folder to delete elements from.
   * @param noteViewModel The ViewModel that provides the list of notes to delete.
   * @param onSuccess Callback to be invoked when the subfolders are deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteFolderContents(
      folder: Folder,
      noteViewModel: NoteViewModel,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
