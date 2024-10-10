package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ImplementationNoteRepository(private val db: FirebaseFirestore) : NoteRepository {


  private data class firebaseNote(
    val id: String,
    val type: Type,
    val name: String,
    val title: String,
    val content: String,
    val date: Timestamp,
    val userId: String,
    val image: String
  )

  //to convert a Note into a firebaseNote
  /**
   * Converts a note into a firebaseNote (a note that is compatible with firerbase).
   *
   * @param note The note to convert.
   * @return The converted firebaseNote object.
   */
  private fun convertNotes(note: Note): firebaseNote{
    return firebaseNote(note.id, note.type, note.name, note.title, note.content, note.date, note.userId, "null") }

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
          Log.e("ImplementationNoteRepository", "Error getting user documents", e)
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
          Log.e("ImplementationNoteRepository", "Error getting document", e)
          onFailure(e)
        }
      }
    }
  }

  override fun insertNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    performFirestoreOperation(
        db.collection(collectionPath).document(note.id).set(convertNotes(note)), onSuccess, onFailure)
  }

  override fun updateNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    performFirestoreOperation(
        db.collection(collectionPath).document(note.id).set(convertNotes(note)), onSuccess, onFailure)
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
          Log.e("ImplementationNoteRepository", "Error performing Firestore operation", e)
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
      val name = document.getString("name") ?: return null
      val title = document.getString("title") ?: return null
      val content = document.getString("content") ?: return null
      val date = document.getTimestamp("date") ?: return null
      val userId = document.getString("userId") ?: return null
      val image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
      //Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) is the default bitMap, to be changed when
      // we implement images by URL

      Note(
          id = id,
          type = type,
          name = name,
          title = title,
          content = content,
          date = date,
          userId = userId,
          image = image)
    } catch (e: Exception) {
      Log.e("ImplementationNoteRepository", "Error converting document to Note", e)
      null
    }
  }
}
