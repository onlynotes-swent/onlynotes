package com.github.onlynotesswent.model.folder

import android.content.Context
import android.util.Log
import com.github.onlynotesswent.model.cache.CacheDatabase
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.utils.NetworkUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FolderRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val cache: CacheDatabase,
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FolderRepository {

  private val folderCollectionPath = "folders"
  private val noteDao = cache.noteDao()
  private val folderDao = cache.folderDao()

  companion object {
    private const val TAG = "FolderRepositoryFirestore"
  }

  override fun getNewFolderId(): String {
    return db.collection(folderCollectionPath).document().id
  }

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override suspend fun addFolder(
      folder: Folder,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(dispatcher) { folderDao.addFolder(folder) }
    }

    db.collection(folderCollectionPath).document(folder.id).set(folder).addOnCompleteListener {
        result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e: Exception ->
          Log.e(TAG, "Failed to add folder: ${e.message}")
          onFailure(e)
        }
      }
    }
  }

  override suspend fun addFolders(
      folders: List<Folder>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(dispatcher) { folderDao.addFolders(folders) }
    }

    val batch = db.batch()
    folders.forEach { folder ->
      batch.set(db.collection(folderCollectionPath).document(folder.id), folder)
    }

    batch.commit().addOnCompleteListener { result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e: Exception ->
          Log.e(TAG, "Failed to add folders: ${e.message}")
          onFailure(e)
        }
      }
    }
  }

  override suspend fun deleteFolderById(
      folderId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(dispatcher) { folderDao.deleteFolderById(folderId) }
    }

    db.collection(folderCollectionPath).document(folderId).delete().addOnCompleteListener { result
      ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e: Exception ->
          Log.e(TAG, "Failed to delete folder: ${e.message}")
          onFailure(e)
        }
      }
    }
  }

  override suspend fun deleteAllFoldersFromUserId(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(dispatcher) { folderDao.deleteFoldersFromUid(userId) }
    }

    db.collection(folderCollectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userFolders =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId }
        userFolders.forEach { folder ->
          db.collection(folderCollectionPath).document(folder.id).delete()
        }
        onSuccess()
      } else {
        task.exception?.let { e: Exception ->
          Log.e(TAG, "Failed to retrieve folders from user: ${e.message}")
          onFailure(e)
        }
      }
    }
  }

  override suspend fun getFolderById(
      folderId: String,
      onSuccess: (Folder) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedFolder: Folder? =
          if (useCache) withContext(dispatcher) { folderDao.getFolderById(folderId) } else null

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        if (cachedFolder != null) {
          onSuccess(cachedFolder)
          return
        }
        throw Exception("Folder not found")
      }

      // If device is online, fetch from Firestore
      val firestoreFolder =
          withContext(dispatcher) {
            db.collection(folderCollectionPath).document(folderId).get().await().let {
              documentSnapshotToFolder(it)
            }
          } ?: throw Exception("Folder not found")

      // Sync Firestore with cache
      val updatedFolder =
          if (useCache) syncFolderFirestoreWithCache(firestoreFolder, cachedFolder)
          else firestoreFolder

      onSuccess(updatedFolder)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to retrieve folder by ID: ${e.message}")
      onFailure(e)
    }
  }

  override suspend fun getFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedFolders: List<Folder> =
          if (useCache) withContext(dispatcher) { folderDao.getFoldersFromUserId(userId) }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedFolders)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreFolders =
          withContext(dispatcher) {
            db.collection(folderCollectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId }
          }

      // Sync Firestore with cache
      val updatedFolders =
          if (useCache) syncFoldersFirestoreWithCache(firestoreFolders, cachedFolders)
          else firestoreFolders

      onSuccess(updatedFolders)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to retrieve folders from user: ${e.message}")
      onFailure(e)
    }
  }

  override suspend fun getRootNoteFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedFolders: List<Folder> =
          if (useCache) withContext(dispatcher) { folderDao.getRootNoteFoldersFromUserId(userId) }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedFolders)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreFolders =
          withContext(dispatcher) {
            db.collection(folderCollectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId && it.parentFolderId == null && !it.isDeckFolder }
          }

      // Sync Firestore with cache
      val updatedFolders =
          if (useCache) syncFoldersFirestoreWithCache(firestoreFolders, cachedFolders)
          else firestoreFolders

      onSuccess(updatedFolders)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to retrieve root folders from user: ${e.message}")
      onFailure(e)
    }
  }

  override suspend fun getRootDeckFoldersFromUserId(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedFolders: List<Folder> =
          if (useCache) withContext(dispatcher) { folderDao.getRootDeckFoldersFromUserId(userId) }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedFolders)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreFolders =
          withContext(dispatcher) {
            db.collection(folderCollectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId && it.parentFolderId == null && it.isDeckFolder }
          }

      // Sync Firestore with cache
      val updatedFolders =
          if (useCache) syncFoldersFirestoreWithCache(firestoreFolders, cachedFolders)
          else firestoreFolders

      onSuccess(updatedFolders)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to retrieve root folders from user: ${e.message}")
      onFailure(e)
    }
  }

  override suspend fun getDeckFoldersByName(
      name: String,
      userId: String,
      onFolderNotFound: () -> Unit,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedFolders: List<Folder> =
          if (useCache) withContext(dispatcher) { folderDao.getRootDeckFoldersFromUserId(userId) }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        if (cachedFolders.isEmpty()) {
          onFolderNotFound()
          return
        }
        onSuccess(cachedFolders)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreFolders =
          withContext(dispatcher) {
            db.collection(folderCollectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId && it.name == name && it.isDeckFolder }
          }

      // Sync Firestore with cache
      val updatedFolders =
          if (useCache) syncFoldersFirestoreWithCache(firestoreFolders, cachedFolders)
          else firestoreFolders

      if (updatedFolders.isNotEmpty()) {
        onSuccess(updatedFolders)
      } else {
        onFolderNotFound()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to retrieve deck folders by name: ${e.message}")
      onFailure(e)
    }
  }

  override suspend fun updateFolder(
      folder: Folder,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(dispatcher) { folderDao.addFolder(folder) }
    }

    db.collection(folderCollectionPath).document(folder.id).set(folder).addOnCompleteListener {
        result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to update folder: ${e.message}")
        }
      }
    }
  }

  override suspend fun getSubFoldersOf(
      parentFolderId: String,
      userViewModel: UserViewModel?,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedFolders: List<Folder> =
          if (useCache) withContext(dispatcher) { folderDao.getSubfoldersOf(parentFolderId) }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedFolders)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreFolders =
          withContext(dispatcher) {
            db.collection(folderCollectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.parentFolderId == parentFolderId }
          }

      // Sync Firestore with cache
      var updatedFolders =
          if (useCache) syncFoldersFirestoreWithCache(firestoreFolders, cachedFolders)
          else firestoreFolders

      // Only return folders visible to the current user.
      if (userViewModel != null) {
        updatedFolders = updatedFolders.filter { it.isVisibleTo(userViewModel.currentUser.value!!) }
      }

      onSuccess(updatedFolders)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to retrieve subfolders of folder: ${e.message}")
      onFailure(e)
    }
  }

  override fun getPublicFolders(onSuccess: (List<Folder>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(folderCollectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val publicFolders =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.visibility == Visibility.PUBLIC }
        onSuccess(publicFolders)
      } else {
        task.exception?.let { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to retrieve public folders: ${e.message}")
        }
      }
    }
  }

  override fun getFoldersFromFollowingList(
      followingListIds: List<String>,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // The current user is not following anyone
    if (followingListIds.isEmpty()) {
      onSuccess(emptyList())
      return
    }

    db.collection(folderCollectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val followingFolders =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.visibility == Visibility.FRIENDS && it.userId in followingListIds }
        onSuccess(followingFolders)
      } else {
        task.exception?.let { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to retrieve friends only folders: ${e.message}")
        }
      }
    }
  }

  override suspend fun deleteFolderContents(
      folder: Folder,
      noteViewModel: NoteViewModel,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    getSubFoldersOf(
        parentFolderId = folder.id,
        userViewModel = null,
        onSuccess = { subFolders ->
          subFolders.forEach { subFolder ->
            CoroutineScope(dispatcher).launch {
              deleteFolderContents(
                  folder = subFolder,
                  noteViewModel = noteViewModel,
                  onSuccess = {},
                  onFailure = {
                    onFailure(it)
                    Log.e(TAG, "Failed to delete folder contents: ${it.message}")
                  },
                  useCache = useCache)

              // Update the cache if needed
              if (useCache) {
                noteDao.deleteNotesFromFolder(subFolder.id)
                folderDao.deleteFolderById(subFolder.id)
              }

              noteViewModel.deleteNotesFromFolder(subFolder.id)
              deleteFolderById(
                  folderId = subFolder.id,
                  onSuccess = {},
                  onFailure = {
                    onFailure(it)
                    Log.e(TAG, "Failed to delete folderContents: ${it.message}")
                  },
                  useCache = useCache)
            }
          }
          onSuccess()
        },
        onFailure = { e: Exception ->
          Log.e(TAG, "Failed to delete folder contents: ${e.message}")
          onFailure(e)
        },
        useCache = useCache)
  }

  override suspend fun deleteFolderContents(
      folder: Folder,
      deckViewModel: DeckViewModel,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    getSubFoldersOf(
        parentFolderId = folder.id,
        userViewModel = null,
        onSuccess = { subFolders ->
          subFolders.forEach { subFolder ->
            CoroutineScope(Dispatchers.IO).launch {
              deleteFolderContents(
                  folder = subFolder,
                  deckViewModel = deckViewModel,
                  onSuccess = {},
                  onFailure = {
                    onFailure(it)
                    Log.e(TAG, "Failed to delete folder contents: ${it.message}")
                  },
                  useCache = useCache)

              // Update the cache if needed
              if (useCache) {
                // TODO: add deck cache
                folderDao.deleteFolderById(subFolder.id)
              }

              deckViewModel.deleteDecksFromFolder(subFolder.id)
              deleteFolderById(
                  folderId = subFolder.id,
                  onSuccess = {},
                  onFailure = {
                    onFailure(it)
                    Log.e(TAG, "Failed to delete folderContents: ${it.message}")
                  },
                  useCache = useCache)
            }
          }
          onSuccess()
        },
        onFailure = { e: Exception ->
          Log.e(TAG, "Failed to delete folder contents: ${e.message}")
          onFailure(e)
        },
        useCache = useCache)
  }

  override suspend fun getSavedFoldersByIds(
      savedFoldersIds: List<String>,
      currentUser: User,
      onSuccess: (List<Folder>, List<String>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedFolders: List<Folder> =
          if (useCache) withContext(Dispatchers.IO) { folderDao.getFoldersByIds(savedFoldersIds) }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedFolders, emptyList())
        return
      }

      // If device is online, fetch from Firestore
      val saveableIdList = mutableListOf<String>()
      val firestoreFolders =
          withContext(Dispatchers.IO) {
            db.collection(folderCollectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { documentSnapshotToFolder(it) }
                .filter {
                  // Make sure the folder is saved and is visible to the current user
                  if (it.id in savedFoldersIds && it.isVisibleTo(currentUser)) {
                    saveableIdList.add(it.id)
                    true
                  } else {
                    false
                  }
                }
          }

      // If some folders are not found in Firestore, also return the list of missing folders
      val missingFolders = savedFoldersIds.filter { it !in saveableIdList }

      if (useCache) {
        // Update cache with newest saved data, to ensure that the cache is up to date and
        // that non available saved folders are deleted
        withContext(Dispatchers.IO) {
          cache.folderDao().addFolders(firestoreFolders)
          cache.folderDao().deleteFoldersByIds(missingFolders)
        }
      }

      // Return the list of folders and the list of missing folders. Cached folders will be at this
      // point the same as firestoreFolders if useCache is true
      onSuccess(firestoreFolders, missingFolders)
    } catch (e: Exception) {
      Log.e(TAG, "Error getting folders from list", e)
      onFailure(e)
    }
  }

  /**
   * Synchronizes the Firestore database with the local cache based on `lastModified` field. Should
   * be called only if the userId of the folder is the current user.
   *
   * @param firestoreFolder The folder from Firestore.
   * @param cachedFolder The folder from the local cache.
   * @return Latest version of the folder.
   */
  private suspend fun syncFolderFirestoreWithCache(
      firestoreFolder: Folder,
      cachedFolder: Folder?
  ): Folder {
    val updatedFolder =
        if (cachedFolder == null || firestoreFolder.lastModified > cachedFolder.lastModified)
            firestoreFolder // Firestore has the newest data
        else cachedFolder // Local database has the newest data

    // Update firestore and the cache with newest data
    addFolder(updatedFolder, {}, {}, true)

    return updatedFolder
  }

  /**
   * Synchronizes the Firestore database with the local cache based on `lastModified` field. Should
   * be called only if the userId of the folders is the current user.
   *
   * @param firestoreFolders The list of folders from Firestore.
   * @param cachedFolders The list of folders from the local cache.
   * @return Latest version of the list of folders.
   */
  private suspend fun syncFoldersFirestoreWithCache(
      firestoreFolders: List<Folder>,
      cachedFolders: List<Folder>
  ): List<Folder> {
    val updatedFolders =
        (firestoreFolders + cachedFolders)
            .groupBy { it.id }
            .map { (_, folder) -> folder.maxByOrNull { it.lastModified }!! }

    // Update firestore and cache with newest data
    addFolders(updatedFolders, {}, {}, true)

    return updatedFolders
  }

  /**
   * Converts a DocumentSnapshot to a Folder object.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Folder object. If the conversion fails, null is returned.
   */
  fun documentSnapshotToFolder(document: DocumentSnapshot): Folder? {
    return try {
      Folder(
          id = document.id,
          name = document.getString("name")!!,
          userId = document.getString("userId")!!,
          isDeckFolder = document.getBoolean("isDeckFolder") ?: false,
          parentFolderId = document.getString("parentFolderId"),
          visibility = Visibility.fromString(document.getString("visibility")!!),
          lastModified = document.getTimestamp("lastModified")!!)
    } catch (e: Exception) {
      Log.e(TAG, "Error converting document to Folder", e)
      null
    }
  }
}
