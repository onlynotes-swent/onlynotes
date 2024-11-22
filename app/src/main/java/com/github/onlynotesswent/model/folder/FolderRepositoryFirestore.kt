package com.github.onlynotesswent.model.folder

import android.util.Log
import com.github.onlynotesswent.model.cache.FolderDatabase
import com.github.onlynotesswent.model.common.Visibility
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FolderRepositoryFirestore(private val db: FirebaseFirestore, cache: FolderDatabase) :
    FolderRepository {

  private val folderCollectionPath = "folders"
  private val folderDao = cache.folderDao()

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

  override fun addFolder(
      folder: Folder,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      folderDao.insertFolder(folder)
    }
    db.collection(folderCollectionPath).document(folder.id).set(folder).addOnCompleteListener {
        result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to add folder: ${e.message}")
        }
      }
    }
  }

  override fun deleteFolderById(
      folderId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      folderDao.deleteFolderById(folderId)
    }
    db.collection(folderCollectionPath).document(folderId).delete().addOnCompleteListener { result
      ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to delete folder: ${e.message}")
        }
      }
    }
  }

  override fun deleteFoldersByUserId(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      folderDao.deleteFolders()
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
          onFailure(e)
          Log.e(TAG, "Failed to retrieve folders from user: ${e.message}")
        }
      }
    }
  }

  override fun getFolderById(
      folderId: String,
      onSuccess: (Folder) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      val cachedData = folderDao.getFolderById(folderId)
      if (cachedData != null) {
        onSuccess(cachedData)
        return
      }
    }
    // If cache is not used or cache is empty, fetch data from Firestore
    db.collection(folderCollectionPath).document(folderId).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val folder = task.result?.let { documentSnapshotToFolder(it) }
        if (folder != null) {
          if (useCache) {
            folderDao.insertFolder(folder)
          }
          onSuccess(folder)
        } else {
          val e = Exception("Folder not found")
          onFailure(e)
          Log.e(TAG, "Failed to find folder: ${e.message}")
        }
      } else {
        task.exception?.let { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to retrieve folder: ${e.message}")
        }
      }
    }
  }

  override fun getFoldersFromUid(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      val cachedData = folderDao.getFolders()
      if (cachedData.isNotEmpty()) {
        onSuccess(cachedData)
        return
      }
    }
    // If cache is not used or cache is empty, fetch data from Firestore
    db.collection(folderCollectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userFolders =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId }
        if (useCache) {
          folderDao.insertFolders(userFolders)
        }
        onSuccess(userFolders)
      } else {
        task.exception?.let { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to retrieve folders from user: ${e.message}")
        }
      }
    }
  }

  override fun getRootFoldersFromUid(
      userId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      val cachedData = folderDao.getRootFolders()
      if (cachedData.isNotEmpty()) {
        onSuccess(cachedData)
        return
      }
    }
    // If cache is not used or cache is empty, fetch data from Firestore
    db.collection(folderCollectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val rootFolders =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId && it.parentFolderId == null } // Only root folders
        if (useCache) {
          folderDao.insertFolders(rootFolders)
        }
        onSuccess(rootFolders)
      } else {
        task.exception?.let { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to retrieve root folders from user: ${e.message}")
        }
      }
    }
  }

  override fun updateFolder(
      folder: Folder,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      folderDao.updateFolder(folder)
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

  override fun getSubFoldersOf(
      parentFolderId: String,
      onSuccess: (List<Folder>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      val cachedData = folderDao.getFoldersFromParentFolder(parentFolderId)
      if (cachedData.isNotEmpty()) {
        onSuccess(cachedData)
        return
      }
    }
    // If cache is not used or cache is empty, fetch data from Firestore
    db.collection(folderCollectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userFolders =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.parentFolderId == parentFolderId }
        if (useCache) {
          folderDao.insertFolders(userFolders)
        }
        onSuccess(userFolders)
      } else {
        task.exception?.let { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to retrieve subfolders of folder: ${e.message}")
        }
      }
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

  /**
   * Converts a DocumentSnapshot to a Folder object.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Folder object.
   */
  fun documentSnapshotToFolder(document: DocumentSnapshot): Folder {
    return Folder(
        id = document.id,
        name = document.getString("name")!!,
        userId = document.getString("userId")!!,
        parentFolderId = document.getString("parentFolderId"),
        Visibility.fromString(document.getString("visibility") ?: Visibility.DEFAULT.toString()))
  }

  companion object {
    const val TAG = "FolderRepositoryFirestore"
  }
}
