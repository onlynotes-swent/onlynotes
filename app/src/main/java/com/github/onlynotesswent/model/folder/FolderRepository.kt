package com.github.onlynotesswent.model.folder

import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.Friends
import com.github.onlynotesswent.model.user.UserViewModel

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
  suspend fun addFolder(
      folder: Folder,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Adds a list of folders.
   *
   * @param folders The list of folders to add.
   * @param onSuccess Callback to be invoked when the folders are added successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folders
   *   is the current user.
   */
  suspend fun addFolders(
      folders: List<Folder>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
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
  suspend fun deleteFolderById(
      folderId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
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
  suspend fun deleteFoldersFromUid(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves a folder by its ID.
   *
   * @param folderId The ID of the folder to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved folder.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  suspend fun getFolderById(
      folderId: String,
      onSuccess: (Folder) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
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
  suspend fun getFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves all root note folders owned by a user.
   *
   * @param userId The ID of the user to retrieve folders for.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  suspend fun getRootNoteFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves all root deck folders owned by a user.
   *
   * @param userId The ID of the user to retrieve decks for.
   * @param onSuccess Callback to be invoked with the retrieved decks.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  suspend fun getRootDeckFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves a folder by its name.
   *
   * @param name The name of the folder to retrieve.
   * @param userId The ID of the user that owns the folder.
   * @param onFolderNotFound Callback to be invoked if the folder is not found.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  suspend fun getDeckFoldersByName(
      name: String,
      userId: String,
      onFolderNotFound: () -> Unit,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
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
  suspend fun updateFolder(
      folder: Folder,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves all folders that are children of a parent folder.
   *
   * @param parentFolderId The ID of the parent folder.
   * @param userViewModel The user view model. If the function can only be called by a user that is
   *   the owner of the folder, this parameter should be null.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  suspend fun getSubFoldersOf(
      parentFolderId: String,
      userViewModel: UserViewModel?,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves all public folders.
   *
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getPublicFolders(onSuccess: (List<Folder>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all friends only folders from a list of following users.
   *
   * @param followingListIds The list of users Ids to retrieve friends only folders from.
   * @param onSuccess Callback to be invoked with the retrieved folders.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getFoldersFromFollowingList(
      followingListIds: List<String>,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes all elements of a folder.
   *
   * @param folder The folder to delete elements from.
   * @param noteViewModel The ViewModel that provides the list of notes to delete.
   * @param onSuccess Callback to be invoked when the subfolders are deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  suspend fun deleteFolderContents(
      folder: Folder,
      noteViewModel: NoteViewModel,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves a list of saved folders by their IDs. This only returns folders that are saveable
   * which means they are public or the user is following the folder's author. The list of currently
   * saved folders that don't comply is also returned.
   *
   * @param savedFoldersIds The list of folder IDs to retrieve.
   * @param friends The user's friends.
   * @param onSuccess Callback to be invoked with the retrieved folders and the list of missing
   *   folders.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folders
   *   is the current user.
   */
  suspend fun getSavedFoldersByIds(
      savedFoldersIds: List<String>,
      friends: Friends,
      onSuccess: (List<Folder>, List<String>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )
}
