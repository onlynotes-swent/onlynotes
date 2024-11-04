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
  private val commentDelimiter: String = '\u001F'.toString()

  private data class FirebaseNote(
      val id: String,
      val type: Note.Type,
      val title: String,
      val content: String,
      val date: Timestamp,
      val visibility: Note.Visibility,
      val userId: String,
      val classCode: String,
      val className: String,
      val classYear: Int,
      val publicPath: String,
      val image: String,
      val commentsList: List<String>
  )
  /**
   * Converts a list of Comment objects to a list of snapshot strings for Firestore storage.
   *
   * @param commentsList The list of Comment objects to be converted.
   * @return A list of snapshot strings where each string represents a Comment, formatted as
   *   "commentId<delimiter>userId<delimiter>content".
   */
  private fun convertCommentsList(commentsList: List<Note.Comment>): List<String> {
    return commentsList.map {
      it.commentId + commentDelimiter + it.userId + commentDelimiter + it.content
    }
  }
  /**
   * Converts a list of snapshot strings to a list of Comment objects.
   *
   * @param snapshotList The list of snapshot strings, where each string represents a Comment in the
   *   format "commentId<delimiter>userId<delimiter>content".
   * @return A list of Comment objects created from the parsed snapshot strings.
   */
  private fun commentStringToCommentClass(commentSnapshotList: List<String>): List<Note.Comment> {
    return commentSnapshotList.map {
      val commentValues = it.split(commentDelimiter)
      Note.Comment(commentValues[0], userId = commentValues[1], content = commentValues[2])
    }
  }

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
        note.visibility,
        note.userId,
        note.noteClass.classCode,
        note.noteClass.className,
        note.noteClass.classYear,
        note.noteClass.publicPath,
        "null",
        convertCommentsList(note.comments.commentsList))
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

  /**
   * Fetches all public notes from the Firestore database.
   *
   * @param onSuccess A callback function that is called with the list of public notes if the
   *   operation is successful.
   * @param onFailure A callback function that is called with an exception if the operation fails.
   */
  override fun getPublicNotes(onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val publicNotes =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToNote(document) }
                .filter { it.visibility == Note.Visibility.PUBLIC }
        onSuccess(publicNotes)
      } else {
        task.exception?.let { e ->
          Log.e("NoteRepositoryFirestore", "Error getting visibility documents", e)
          onFailure(e)
        }
      }
    }
  }

  override fun getNotesFrom(
      userId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userNotes =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToNote(document) }
                .filter { it.userId == userId }
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
      val type = Note.Type.valueOf(document.getString("type") ?: return null)
      val title = document.getString("title") ?: return null
      val content = document.getString("content") ?: return null
      val date = document.getTimestamp("date") ?: return null
      val visibility =
          Note.Visibility.fromString(
              document.getString("visibility") ?: Note.Visibility.DEFAULT.toString())
      val userId = document.getString("userId") ?: return null
      val classCode = document.getString("classCode") ?: return null
      val className = document.getString("className") ?: return null
      val classYear = document.getLong("classYear")?.toInt() ?: return null
      val classPath = document.getString("publicPath") ?: return null
      val image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
      val comments =
          commentStringToCommentClass(document.get("commentsList") as? List<String> ?: emptyList())
      // Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) is the default bitMap, to be changed
      // when we implement images by URL

      Note(
          id = id,
          type = type,
          title = title,
          content = content,
          date = date,
          visibility = visibility,
          userId = userId,
          noteClass = Note.Class(classCode, className, classYear, classPath),
          image = image,
          comments = Note.CommentCollection(comments))
    } catch (e: Exception) {
      Log.e("NoteRepositoryFirestore", "Error converting document to Note", e)
      null
    }
  }
}
