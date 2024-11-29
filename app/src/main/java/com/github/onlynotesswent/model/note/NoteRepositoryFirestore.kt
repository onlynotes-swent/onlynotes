package com.github.onlynotesswent.model.note

import android.content.Context
import android.util.Log
import com.github.onlynotesswent.model.cache.NoteDatabase
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.utils.NetworkUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NoteRepositoryFirestore(
    private val db: FirebaseFirestore,
    cache: NoteDatabase,
    private val context: Context
) : NoteRepository {
  private val commentDelimiter: String = '\u001F'.toString()

  private data class FirebaseNote(
      val id: String,
      val title: String,
      val date: Timestamp,
      val visibility: Visibility,
      val userId: String,
      val courseCode: String,
      val courseName: String,
      val courseYear: Int,
      val publicPath: String,
      val folderId: String?,
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
        note.date,
        note.visibility,
        note.userId,
        note.noteCourse.courseCode,
        note.noteCourse.courseName,
        note.noteCourse.courseYear,
        note.noteCourse.publicPath,
        note.folderId,
        convertCommentsList(note.comments.commentsList))
  }

  private val collectionPath = "notes"
  private val noteDao = cache.noteDao()

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

  override suspend fun getNotesFrom(
      userId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      // If device is offline, fetch from cache
      if (!NetworkUtils.isInternetAvailable(context)) {
        val cachedNotes = withContext(Dispatchers.IO) { noteDao.getNotes() }
        onSuccess(cachedNotes)
        return
      }

      // If device is online, fetch from Firestore
      val userNotes =
          withContext(Dispatchers.IO) {
            db.collection(collectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { document -> documentSnapshotToNote(document) }
                .filter { it.userId == userId }
          }

      // Update cache if necessary
      if (useCache) {
        withContext(Dispatchers.IO) { noteDao.insertNotes(userNotes) }
      }

      onSuccess(userNotes)
    } catch (e: Exception) {
      Log.e(TAG, "Error getting user documents", e)
      onFailure(e)
    }
  }

  override suspend fun getRootNotesFrom(
      userId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      // If device is offline, fetch from cache
      if (!NetworkUtils.isInternetAvailable(context)) {
        val cachedNotes = withContext(Dispatchers.IO) { noteDao.getRootNotes() }
        onSuccess(cachedNotes)
        return
      }

      // If device is online, fetch from Firestore
      val userRootNotes =
          withContext(Dispatchers.IO) {
            db.collection(collectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { document -> documentSnapshotToNote(document) }
                .filter { it.userId == userId && it.folderId == null }
          }

      // Update cache if necessary
      if (useCache) {
        withContext(Dispatchers.IO) { noteDao.insertNotes(userRootNotes) }
      }

      onSuccess(userRootNotes)
    } catch (e: Exception) {
      Log.e(TAG, "Error getting user documents", e)
      onFailure(e)
    }
  }

  override suspend fun getNoteById(
      id: String,
      onSuccess: (Note) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      // If device is offline, fetch from cache
      if (!NetworkUtils.isInternetAvailable(context)) {
        val cachedData = withContext(Dispatchers.IO) { noteDao.getNoteById(id) }
        if (cachedData != null) {
          onSuccess(cachedData)
          return
        } else {
          onFailure(Exception("Note not found"))
        }
      }

      // If device is online, fetch from Firestore
      val note =
          withContext(Dispatchers.IO) {
            db.collection(collectionPath).document(id).get().await().let {
              documentSnapshotToNote(it)
            }
          }

      if (note == null) {
        throw Exception("Note not found")
      }

      // Update cache if necessary
      if (useCache) {
        withContext(Dispatchers.IO) { noteDao.insertNote(note) }
      }

      onSuccess(note)
    } catch (e: Exception) {
      Log.e(TAG, "Error getting document", e)
      onFailure(e)
    }
  }

  override suspend fun addNote(
      note: Note,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(Dispatchers.IO) { noteDao.insertNote(note) }
      onSuccess()
      return
    }

    // If device is online, add the note to Firestore
    performFirestoreOperation(
        db.collection(collectionPath).document(note.id).set(convertNotes(note)),
        onSuccess,
        onFailure)
  }

  // TODO: modify below
  override suspend fun updateNote(
      note: Note,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      noteDao.updateNote(note)
    }
    performFirestoreOperation(
        db.collection(collectionPath).document(note.id).set(convertNotes(note)),
        onSuccess,
        onFailure)
  }

  override suspend fun deleteNoteById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      noteDao.deleteNoteById(id)
    }
    performFirestoreOperation(
        db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
  }

  override suspend fun deleteNotesByUserId(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      noteDao.deleteNotes()
    }
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

  override suspend fun getNotesFromFolder(
      folderId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    if (useCache) {
      val cachedData = noteDao.getNotesFromFolder(folderId)
      if (cachedData.isNotEmpty()) {
        onSuccess(cachedData)
        return
      }
    }
    // If cache is not used or cache is empty, fetch data from Firestore
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val folderNotes =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToNote(document) }
                .filter { it.folderId == folderId }
        if (useCache) {
          noteDao.insertNotes(folderNotes)
        }
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
      val date = document.getTimestamp("date") ?: Timestamp.now()
      val visibility =
          Visibility.fromString(document.getString("visibility") ?: Visibility.DEFAULT.toString())
      val userId = document.getString("userId") ?: return null
      val courseCode = document.getString("courseCode") ?: ""
      val courseName = document.getString("courseName") ?: ""
      val courseYear = document.getLong("courseYear")?.toInt() ?: 0
      val publicPath = document.getString("publicPath") ?: ""
      val folderId = document.getString("folderId")
      val comments =
          commentStringToCommentClass(document.get("commentsList") as? List<String> ?: emptyList())

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
          date = date,
          visibility = visibility,
          userId = userId,
          noteCourse = course,
          folderId = folderId,
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
