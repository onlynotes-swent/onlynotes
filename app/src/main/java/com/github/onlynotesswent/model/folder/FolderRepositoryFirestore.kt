package com.github.onlynotesswent.model.folder

import android.util.Log
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.note.NoteViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FolderRepositoryFirestore(private val db: FirebaseFirestore) : FolderRepository {

  private val folderCollectionPath = "folders"

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

  override fun addFolder(folder: Folder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    Log.e(TAG, "Adding folder: $folder")
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
      onFailure: (Exception) -> Unit
  ) {
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
      onFailure: (Exception) -> Unit
  ) {
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
      onFailure: (Exception) -> Unit
  ) {
    db.collection(folderCollectionPath).document(folderId).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val folder = task.result?.let { documentSnapshotToFolder(it) }
        if (folder != null) {
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
      onFailure: (Exception) -> Unit
  ) {
    db.collection(folderCollectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userFolders =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId }
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
      onFailure: (Exception) -> Unit
  ) {
    db.collection(folderCollectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val rootFolders =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId && it.parentFolderId == null } // Only root folders
        onSuccess(rootFolders)
      } else {
        task.exception?.let { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to retrieve root folders from user: ${e.message}")
        }
      }
    }
  }

  override fun updateFolder(folder: Folder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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
      onFailure: (Exception) -> Unit
  ) {
    db.collection(folderCollectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userFolders =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.parentFolderId == parentFolderId }
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

  override fun deleteFolderContents(
      folder: Folder,
      noteViewModel: NoteViewModel,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getSubFoldersOf(
        folder.id,
        onSuccess = { subFolders ->
          subFolders.forEach { subFolder ->
            deleteFolderContents(subFolder, noteViewModel, onSuccess = {}, onFailure = onFailure)
            noteViewModel.deleteNotesFromFolder(subFolder.id)
            deleteFolderById(subFolder.id, onSuccess = {}, onFailure = onFailure)
          }
          onSuccess()
        },
        onFailure = { e: Exception ->
          onFailure(e)
          Log.e(TAG, "Failed to delete folder contents: ${e.message}")
        })
  }

  /**
   * Converts a DocumentSnapshot to a Folder object.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Folder object.
   */
  fun documentSnapshotToFolder(document: DocumentSnapshot): Folder? {
      Log.e(TAG, "Converting document to Folder: $document")
      Log.e(TAG, "Document id: ${document.id} name: ${document.getString("name")} userId ${document.getString("userId")} parentFolderId ${document.getString("parentFolderId")} visibility ${document.getString("visibility")}")
      return try {
          Folder(
              id = document.id,
              name = document.getString("name")!!,
              userId = document.getString("userId")!!,
              parentFolderId = document.getString("parentFolderId"),
              visibility = Visibility.fromString(document.getString("visibility")!!))
      } catch (e: Exception) {
          Log.e(TAG, "Error converting document to Folder", e)
          null
      }
  }

  companion object {
    private const val TAG = "FolderRepositoryFirestore"
  }
}
