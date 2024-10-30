package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class NoteRepositoryFirestore(private val db: FirebaseFirestore) : NoteRepository {

  private data class FirebaseNote(
      val id: String,
      val type: Type,
      val title: String,
      val content: String,
      val date: Timestamp,
      val public: Boolean,
      val userId: String,
      val classCode: String,
      val className: String,
      val classYear: Int,
      val publicPath: String,
      val image: String
  )

  /**
   * Converts a note into a FirebaseNote (a note that is compatible with Firebase).
   *
   * @param note The note to convert.
   * @return The converted FirebaseNote object.
   */
  private fun convertNotes(note: Note): FirebaseNote {
    return FirebaseNote(
        note.id,
        note.type,
        note.title,
        note.content,
        note.date,
        note.public,
        note.userId,
        note.noteClass.classCode,
        note.noteClass.className,
        note.noteClass.classYear,
        note.noteClass.publicPath,
        "null")
  }

  private val collectionPath = "notes"

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getNotes(
      userId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userNotes =
            task.result
                ?.mapNotNull { document -> documentSnapshotToNote(document) }
                ?.filter { it.userId == userId } ?: emptyList()
        onSuccess(userNotes)
      } else {
        task.exception?.let { e ->
          Log.e("NoteRepositoryFirestore", "Error getting user documents", e)
          onFailure(e)
        }
      }
    }
  }

  override fun getNoteById(id: String, onSuccess: (Note) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath).document(id).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val note = task.result?.let { documentSnapshotToNote(it) }
        if (note != null) {
          onSuccess(note)
        } else {
          onFailure(Exception("Note not found"))
        }
      } else {
        task.exception?.let { e ->
          Log.e("NoteRepositoryFirestore", "Error getting document", e)
          onFailure(e)
        }
      }
    }
  }

  override fun addNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    performFirestoreOperation(
        db.collection(collectionPath).document(note.id).set(convertNotes(note)),
        onSuccess,
        onFailure)
  }

  override fun updateNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    performFirestoreOperation(
        db.collection(collectionPath).document(note.id).set(convertNotes(note)),
        onSuccess,
        onFailure)
  }

  override fun deleteNoteById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    performFirestoreOperation(
        db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
  }

  /**
   * Performs a Firestore operation and calls the appropriate callback based on the result.
   *
   * @param task The Firestore task to perform.
   * @param onSuccess The callback to call if the operation is successful.
   * @param onFailure The callback to call if the operation fails.
   */
  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener { result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e ->
          Log.e("NoteRepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Converts a Firestore DocumentSnapshot to a Note object.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Note object.
   */
  fun documentSnapshotToNote(document: DocumentSnapshot): Note? {
    return try {
      val id = document.id
      val type = Type.valueOf(document.getString("type") ?: return null)
      val title = document.getString("title") ?: return null
      val content = document.getString("content") ?: return null
      val date = document.getTimestamp("date") ?: return null
      val public = document.getBoolean("public") ?: true
      val userId = document.getString("userId") ?: return null
      val classCode = document.getString("classCode") ?: return null
      val className = document.getString("className") ?: return null
      val classYear = document.getLong("classYear")?.toInt() ?: return null
      val classPath = document.getString("publicPath") ?: return null
      val image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
      // Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) is the default bitMap, to be changed
      // when we implement images by URL

      Note(
          id = id,
          type = type,
          title = title,
          content = content,
          date = date,
          public = public,
          userId = userId,
          noteClass = Class(classCode, className, classYear, classPath),
          image = image)
    } catch (e: Exception) {
      Log.e("NoteRepositoryFirestore", "Error converting document to Note", e)
      null
    }
  }
}
