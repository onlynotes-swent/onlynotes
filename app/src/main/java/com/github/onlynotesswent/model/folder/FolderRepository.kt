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
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun addFolder(
      folder: Folder,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean = false
  )

  /**
   * Deletes a folder by its ID.
   *
   * @param folderId The ID of the folder to delete.
   * @param onSuccess Callback to be invoked when the folder is deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun deleteFolderById(
      folderId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean = false
  )

  /**
   * Deletes all folders belonging to a user.
   *
   * @param userId The ID of the user that owns the folders.
   * @param onSuccess Callback to be invoked if the folders are deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun deleteFoldersByUserId(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean = false
  )

  // TODO not sure if this is needed
  /**
   * Retrieves a folder by its ID.
   *
   * @param folderId The ID of the folder to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved folder.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun getFolderById(
      folderId: String,
      onSuccess: (Folder) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean = false
  )

  /**
   * Retrieves all folders owned by a user.
   *
   * @param userId The ID of the user to retrieve folders for.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun getFoldersFromUid(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean = false
  )

  /**
   * Retrieves all root folders owned by a user.
   *
   * @param userId The ID of the user to retrieve folders for.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  fun getRootFoldersFromUid(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean = false
  )

  /**
   * Updates an existing folder.
   *
   * @param folder The folder with updated information.
   * @param onSuccess Callback to be invoked when the update is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun updateFolder(
      folder: Folder,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean = false
  )

  /**
   * Retrieves all folders that are children of a parent folder.
   *
   * @param parentFolderId The ID of the parent folder.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  fun getSubFoldersOf(
      parentFolderId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean = false
  )

  /**
   * Retrieves all public folders.
   *
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getPublicFolders(onSuccess: (List<Folder>) -> Unit, onFailure: (Exception) -> Unit)
}
