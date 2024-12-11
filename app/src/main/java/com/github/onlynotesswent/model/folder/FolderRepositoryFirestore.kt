package com.github.onlynotesswent.model.folder

import android.content.Context
import android.util.Log
import com.github.onlynotesswent.model.cache.CacheDatabase
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.utils.NetworkUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FolderRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val cache: CacheDatabase,
    private val context: Context
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
      withContext(Dispatchers.IO) { folderDao.addFolder(folder) }
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
      withContext(Dispatchers.IO) { folderDao.addFolders(folders) }
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
      withContext(Dispatchers.IO) { folderDao.deleteFolderById(folderId) }
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

  override suspend fun deleteFoldersFromUid(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(Dispatchers.IO) { folderDao.deleteFoldersFromUid() }
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
          if (useCache) withContext(Dispatchers.IO) { folderDao.getFolderById(folderId) } else null

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
          withContext(Dispatchers.IO) {
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

  override suspend fun getFoldersFromUid(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedFolders: List<Folder> =
          if (useCache) withContext(Dispatchers.IO) { folderDao.getFoldersFromUid() }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedFolders)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreFolders =
          withContext(Dispatchers.IO) {
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

  override suspend fun getRootFoldersFromUid(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedFolders: List<Folder> =
          if (useCache) withContext(Dispatchers.IO) { folderDao.getRootFoldersFromUid() }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedFolders)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreFolders =
          withContext(Dispatchers.IO) {
            db.collection(folderCollectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId && it.parentFolderId == null } // Only root folders
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

  override suspend fun updateFolder(
      folder: Folder,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(Dispatchers.IO) { folderDao.addFolder(folder) }
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
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedFolders: List<Folder> =
          if (useCache) withContext(Dispatchers.IO) { folderDao.getSubfoldersOf(parentFolderId) }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedFolders)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreFolders =
          withContext(Dispatchers.IO) {
            db.collection(folderCollectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.parentFolderId == parentFolderId }
          }

      // Sync Firestore with cache
      val updatedFolders =
          if (useCache) syncFoldersFirestoreWithCache(firestoreFolders, cachedFolders)
          else firestoreFolders

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
        onSuccess = { subFolders ->
          subFolders.forEach { subFolder ->
            CoroutineScope(Dispatchers.IO).launch {
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
          parentFolderId = document.getString("parentFolderId"),
          visibility = Visibility.fromString(document.getString("visibility")!!),
          lastModified = document.getTimestamp("lastModified")!!)
    } catch (e: Exception) {
      Log.e(TAG, "Error converting document to Folder", e)
      null
    }
  }
}
