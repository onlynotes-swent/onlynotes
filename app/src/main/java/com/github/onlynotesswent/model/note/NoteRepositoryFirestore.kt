package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import android.util.Log
import com.github.onlynotesswent.utils.Course
import com.github.onlynotesswent.utils.Visibility
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
      val title: String,
      val content: String,
      val date: Timestamp,
      val visibility: Visibility,
      val userId: String,
      val courseCode: String,
      val courseName: String,
      val courseYear: Int,
      val publicPath: String,
      val folderId: String?,
      val image: String,
      val commentsList: List<String>
  )
  /**
   * Converts a single Comment object into a formatted string for Firestore storage.
   *
   * @param comment The Comment object to convert.
   * @return A string representing the Comment, formatted as
   *   "commentId<delimiter>userId<delimiter>userName<delimiter>content<delimiter>creationDate<delimiter>editedDate".
   *
   * Each field is separated by the `commentDelimiter` for easy parsing during retrieval.
   */
  private fun convertCommentToString(comment: Note.Comment): String {
    return comment.commentId +
        commentDelimiter +
        comment.userId +
        commentDelimiter +
        comment.userName +
        commentDelimiter +
        comment.content +
        commentDelimiter +
        comment.creationDate.seconds.toString() +
        commentDelimiter +
        comment.editedDate.seconds.toString()
  }
  /**
   * Converts a formatted string snapshot of a comment back into a Comment object.
   *
   * @param commentSnapshot The string representing the comment, formatted as
   *   "commentId<delimiter>userId<delimiter>userName<delimiter>content<delimiter>creationDate<delimiter>editedDate".
   * @return A Comment object created from the parsed string values.
   * @throws IndexOutOfBoundsException if the comment snapshot is improperly formatted and does not
   *   contain the expected number of fields.
   */
  private fun convertCommentStringToComment(commentSnapshot: String): Note.Comment {
    val commentValues = commentSnapshot.split(commentDelimiter)
    return Note.Comment(
        commentValues[0],
        userId = commentValues[1],
        userName = commentValues[2],
        content = commentValues[3],
        creationDate = Timestamp(commentValues[4].toLong(), 0),
        editedDate = Timestamp(commentValues[5].toLong(), 0))
  }
  /**
   * Converts a list of Comment objects to a list of snapshot strings for Firestore storage.
   *
   * @param commentsList The list of Comment objects to be converted.
   * @return A list of snapshot strings where each string represents a Comment, formatted as
   *   "commentId<delimiter>userId<delimiter>content".
   */
  private fun convertCommentsList(commentsList: List<Note.Comment>): List<String> {
    return commentsList.map { convertCommentToString(it) }
  }
  /**
   * Converts a list of snapshot strings to a list of Comment objects.
   *
   * @param commentSnapshotList The list of snapshot strings, where each string represents a Comment
   *   in the format "commentId<delimiter>userId<delimiter>content".
   * @return A list of Comment objects created from the parsed snapshot strings.
   */
  private fun commentStringToCommentClass(commentSnapshotList: List<String>): List<Note.Comment> {
    return commentSnapshotList.map { convertCommentStringToComment(it) }
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
        note.title,
        note.content,
        note.date,
        note.visibility,
        note.userId,
        note.noteCourse.courseCode,
        note.noteCourse.courseName,
        note.noteCourse.courseYear,
        note.noteCourse.publicPath,
        note.folderId,
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
                .filter { it.visibility == Visibility.PUBLIC }
        onSuccess(publicNotes)
      } else {
        task.exception?.let { e ->
          Log.e(TAG, "Error getting visibility documents", e)
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
          Log.e(TAG, "Error getting user documents", e)
          onFailure(e)
        }
      }
    }
  }

  override fun getRootNotesFrom(
      userId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userRootNotes =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToNote(document) }
                .filter {
                  it.userId == userId && it.folderId == null
                } // filter out notes that are in folders
        onSuccess(userRootNotes)
      } else {
        task.exception?.let { e ->
          Log.e(TAG, "Error getting user documents", e)
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
          Log.e(TAG, "Error getting document", e)
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
    val firebaseNote = convertNotes(note)
    Log.d("NoteRepositoryFirestore", "updateNote: $firebaseNote")
    performFirestoreOperation(
        db.collection(collectionPath).document(note.id).set(firebaseNote), onSuccess, onFailure)
  }

  override fun deleteNoteById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    performFirestoreOperation(
        db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
  }

  override fun deleteNotesByUserId(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userNotes =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToNote(document) }
                .filter { it.userId == userId }
        userNotes.forEach { note -> db.collection(collectionPath).document(note.id).delete() }
        onSuccess()
      } else {
        task.exception?.let { e ->
          Log.e(TAG, "Error getting user documents", e)
          onFailure(e)
        }
      }
    }
  }

  override fun getNotesFromFolder(
      folderId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val folderNotes =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToNote(document) }
                .filter { it.folderId == folderId }
        onSuccess(folderNotes)
      } else {
        task.exception?.let { e ->
          Log.e(TAG, "Error getting user documents", e)
          onFailure(e)
        }
      }
    }
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
          Log.e(TAG, "Error performing Firestore operation", e)
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
      val title = document.getString("title") ?: ""
      val content = document.getString("content") ?: ""
      val date = document.getTimestamp("date") ?: Timestamp.now()
      val visibility =
          Visibility.fromString(document.getString("visibility") ?: Visibility.DEFAULT.toString())
      val userId = document.getString("userId") ?: return null
      val courseCode = document.getString("courseCode") ?: ""
      val courseName = document.getString("courseName") ?: ""
      val courseYear = document.getLong("courseYear")?.toInt() ?: 0
      val publicPath = document.getString("publicPath") ?: ""
      val folderId = document.getString("folderId")
      val image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
      val comments =
          commentStringToCommentClass(document.get("commentsList") as? List<String> ?: emptyList())
      // Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) is the default bitMap, to be changed
      // when we implement images by URL

      val course =
          if (courseYear == 0 ||
              courseCode.isEmpty() ||
              courseName.isEmpty() ||
              publicPath.isEmpty()) {
            Course.DEFAULT
          } else {
            Course(courseCode, courseName, courseYear, publicPath)
          }

      Note(
          id = id,
          title = title,
          content = content,
          date = date,
          visibility = visibility,
          userId = userId,
          noteCourse = course,
          folderId = folderId,
          image = image,
          comments = Note.CommentCollection(comments))
    } catch (e: Exception) {
      Log.e(TAG, "Error converting document to Note", e)
      null
    }
  }

  companion object {
    private const val TAG = "NoteRepositoryFirestore"
  }
}
