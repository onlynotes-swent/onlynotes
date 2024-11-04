package com.github.onlynotesswent.model.folder

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FolderRepositoryFirestore(private val db: FirebaseFirestore) : FolderRepository {

  private val collectionPath = "folders"

  override fun getNewFolderId(): String {
    return db.collection(collectionPath).document().id
  }

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun addFolder(folder: Folder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(folder.id)
        .set(folder)
        .addOnCompleteListener { result ->
          if(result.isSuccessful) {
            onSuccess()
          } else {
            result.exception?.let { onFailure(it) }
          }
        }
  }

  override fun deleteFolderById(folderId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(folderId)
        .delete()
        .addOnCompleteListener { result ->
          if(result.isSuccessful) {
            onSuccess()
          } else {
            result.exception?.let { onFailure(it) }
          }
        }
  }

  override fun getFolderById(
      folderId: String,
      onSuccess: (Folder) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).document(folderId).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val folder = task.result?.let { documentSnapshotToFolder(it) }
        if (folder != null) {
          onSuccess(folder)
        } else {
          onFailure(Exception("Folder not found"))
        }
      } else {
        task.exception?.let { onFailure(it) }
      }
    }
  }

  override fun getFoldersFrom(
    userId: String,
    onSuccess: (List<Folder>) -> Unit,
    onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userFolders =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToFolder(document) }
                .filter { it.userId == userId }
        onSuccess(userFolders)
      } else {
        task.exception?.let { onFailure(it) }
      }
    }
  }

  override fun updateFolder(folder: Folder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(folder.id)
        .set(folder)
        .addOnCompleteListener { result ->
          if(result.isSuccessful) {
            onSuccess()
          } else {
            result.exception?.let { onFailure(it) }
          }
        }
  }

    override fun getFoldersByParentFolderId(
        parentFolderId: String,
        onSuccess: (List<Folder>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(collectionPath).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userFolders =
                    task.result.documents
                        .mapNotNull { document -> documentSnapshotToFolder(document) }
                        .filter { it.parentFolderId == parentFolderId }
                onSuccess(userFolders)
            } else {
                task.exception?.let { onFailure(it) }
            }
        }
    }

    /**
   * Converts a DocumentSnapshot to a Folder object.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Folder object.
   */
  fun documentSnapshotToFolder(document: DocumentSnapshot): Folder? {
    return Folder(
       id = document.id,
       name = document.getString("name") ?: return null,
       userId = document.getString("userId") ?: return null
    )
  }
}