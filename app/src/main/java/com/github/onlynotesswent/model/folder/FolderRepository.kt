package com.github.onlynotesswent.model.folder

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
  fun getFoldersFromUid(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all root folders owned by a user.
   *
   * @param userId The ID of the user to retrieve folders for.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getRootFoldersFromUid(
      userId: String,
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
}
