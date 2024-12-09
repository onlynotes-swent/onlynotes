package com.github.onlynotesswent.model.note

import android.content.Context
import android.util.Log
import com.github.onlynotesswent.model.cache.CacheDatabase
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
    private val cache: CacheDatabase,
    private val context: Context
) : NoteRepository {

  private val collectionPath = "notes"
  private val noteDao = cache.noteDao()

  companion object {
    private const val TAG = "NoteRepositoryFirestore"
  }

  private data class FirebaseNote(
      val id: String,
      val title: String,
      val date: Timestamp,
      val lastModified: Timestamp,
      val visibility: Visibility,
      val courseCode: String,
      val courseName: String,
      val courseYear: Int?,
      val userId: String,
      val publicPath: String,
      val folderId: String?,
      val commentsList: List<String>
  )

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

  override suspend fun getNotesFromUid(
      userId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedNotes: List<Note> =
          if (useCache) withContext(Dispatchers.IO) { noteDao.getNotesFromUid() } else emptyList()

      // If device is offline, fetch from from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedNotes)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreNotes =
          withContext(Dispatchers.IO) {
            db.collection(collectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { documentSnapshotToNote(it) }
                .filter { it.userId == userId }
          }

      // Sync Firestore with cache
      val updatedNotes =
          if (useCache) syncNotesFirestoreWithCache(firestoreNotes, cachedNotes) else firestoreNotes

      onSuccess(updatedNotes)
    } catch (e: Exception) {
      Log.e(TAG, "Error getting user documents", e)
      onFailure(e)
    }
  }

  override suspend fun getRootNotesFromUid(
      userId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    try {
      val cachedNotes: List<Note> =
          if (useCache) withContext(Dispatchers.IO) { noteDao.getRootNotesFromUid() }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedNotes)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreNotes =
          withContext(Dispatchers.IO) {
            db.collection(collectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { documentSnapshotToNote(it) }
                .filter { it.userId == userId && it.folderId == null }
          }

      // Sync Firestore with cache
      val updatedNotes =
          if (useCache) syncNotesFirestoreWithCache(firestoreNotes, cachedNotes) else firestoreNotes

      onSuccess(updatedNotes)
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
      val cachedNote: Note? =
          if (useCache) withContext(Dispatchers.IO) { noteDao.getNoteById(id) } else null

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        if (cachedNote != null) {
          onSuccess(cachedNote)
          return
        }
        throw Exception("Note not found")
      }

      // If device is online, fetch from Firestore
      val firestoreNote =
          withContext(Dispatchers.IO) {
            db.collection(collectionPath).document(id).get().await().let {
              documentSnapshotToNote(it)
            }
          } ?: throw Exception("Note not found")

      // Sync Firestore with cache
      val updatedNote =
          if (useCache) syncNoteFirestoreWithCache(firestoreNote, cachedNote) else firestoreNote

      onSuccess(updatedNote)
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
      withContext(Dispatchers.IO) { noteDao.addNote(note) }
    }

    performFirestoreOperation(
        db.collection(collectionPath).document(note.id).set(convertNotes(note)),
        onSuccess,
        onFailure)
  }

  override suspend fun addNotes(
      notes: List<Note>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(Dispatchers.IO) { noteDao.addNotes(notes) }
    }

    val batch = db.batch()
    notes.forEach { note ->
      batch.set(db.collection(collectionPath).document(note.id), convertNotes(note))
    }

    performFirestoreOperation(batch.commit(), onSuccess, onFailure)
  }

  override suspend fun updateNote(
      note: Note,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(Dispatchers.IO) { noteDao.addNote(note) }
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
    // Update the cache if needed
    if (useCache) {
      withContext(Dispatchers.IO) { noteDao.deleteNoteById(id) }
    }

    performFirestoreOperation(
        db.collection(collectionPath).document(id).delete(), onSuccess, onFailure)
  }

  override suspend fun deleteNotesFromUid(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(Dispatchers.IO) { noteDao.deleteNotesFromUid() }
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
    try {
      val cachedNotes: List<Note> =
          if (useCache) withContext(Dispatchers.IO) { noteDao.getNotesFromFolder(folderId) }
          else emptyList()

      // If device is offline, fetch from local database
      if (!NetworkUtils.isInternetAvailable(context)) {
        onSuccess(cachedNotes)
        return
      }

      // If device is online, fetch from Firestore
      val firestoreNotes =
          withContext(Dispatchers.IO) {
            db.collection(collectionPath)
                .get()
                .await()
                .documents
                .mapNotNull { documentSnapshotToNote(it) }
                .filter { it.folderId == folderId }
          }

      // Sync Firestore with cache
      val updatedNotes =
          if (useCache) syncNotesFirestoreWithCache(firestoreNotes, cachedNotes) else firestoreNotes

      onSuccess(updatedNotes)
    } catch (e: Exception) {
      Log.e(TAG, "Error getting folder notes", e)
      onFailure(e)
    }
  }

  override suspend fun deleteNotesFromFolder(
      folderId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  ) {
    // Update the cache if needed
    if (useCache) {
      withContext(Dispatchers.IO) { noteDao.deleteNotesFromFolder(folderId) }
    }

    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val folderNotes =
            task.result.documents
                .mapNotNull { document -> documentSnapshotToNote(document) }
                .filter { it.folderId == folderId }
        folderNotes.forEach { note -> db.collection(collectionPath).document(note.id).delete() }
        // Throw onSuccess only after deleting all notes
        onSuccess()
      } else {
        task.exception?.let { e ->
          Log.e(TAG, "Error deleting folder notes", e)
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
   * Synchronizes the Firestore database with the local cache based on 'lastModified' field. Should
   * be called only if the userId of the note is the current user.
   *
   * @param firestoreNote The note from Firestore.
   * @param cachedNote The note from the local cache.
   * @return Latest version of the note.
   */
  private suspend fun syncNoteFirestoreWithCache(firestoreNote: Note, cachedNote: Note?): Note {
    val updatedNote =
        if (cachedNote == null || firestoreNote.lastModified > cachedNote.lastModified)
            firestoreNote // Firestore has newest data
        else cachedNote // Local database has newest data

    // Update firestore and cache with newest data
    addNote(updatedNote, {}, {}, true)

    return updatedNote
  }

  /**
   * Synchronizes the Firestore database with the local cache based on 'lastModified' field. Should
   * be called only if the userId of the notes is the current user.
   *
   * @param firestoreNotes The list of notes from Firestore.
   * @param cachedNotes The list of notes from the local cache.
   * @return Latest version of the list of notes.
   */
  private suspend fun syncNotesFirestoreWithCache(
      firestoreNotes: List<Note>,
      cachedNotes: List<Note>
  ): List<Note> {
    val updatedNotes =
        (firestoreNotes + cachedNotes)
            .groupBy { it.id }
            .map { (_, note) -> note.maxByOrNull { it.lastModified }!! }

    // Update firestore and cache with newest data
    addNotes(updatedNotes, {}, {}, true)

    return updatedNotes
  }

  /**
   * Converts a note into a FirebaseNote (a note that is compatible with Firebase).
   *
   * @param note The note to convert.
   * @return The converted FirebaseNote object.
   */
  private fun convertNotes(note: Note): FirebaseNote {
    val course = note.noteCourse ?: Course.EMPTY
    return FirebaseNote(
        note.id,
        note.title,
        note.date,
        note.lastModified,
        note.visibility,
        course.courseCode,
        course.courseName,
        course.courseYear,
        note.userId,
        course.publicPath,
        note.folderId,
        convertCommentsList(note.comments.commentsList))
  }

  /**
   * Converts a Firestore DocumentSnapshot to a Note object.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Note object. Returns null if the conversion fails.
   */
  fun documentSnapshotToNote(document: DocumentSnapshot): Note? {
    return try {
      val id = document.id
      val title = document.getString("title")!!
      val date = document.getTimestamp("date")!!
      val lastModified = document.getTimestamp("lastModified")!!
      val visibility = Visibility.fromString(document.getString("visibility")!!)
      val courseCode = document.getString("courseCode") ?: ""
      val courseName = document.getString("courseName") ?: ""
      val courseYear = document.getLong("courseYear")?.toInt()
      val publicPath = document.getString("publicPath") ?: ""
      val userId = document.getString("userId")!!
      val folderId = document.getString("folderId")
      val comments = commentStringToCommentClass(document.get("commentsList") as List<String>)

      val course = Course(courseCode, courseName, courseYear, publicPath)

      Note(
          id = id,
          title = title,
          date = date,
          lastModified = lastModified,
          visibility = visibility,
          noteCourse = if (course == Course.EMPTY) null else course,
          userId = userId,
          folderId = folderId,
          comments = Note.CommentCollection(comments))
    } catch (e: Exception) {
      Log.e(TAG, "Error converting document to Note", e)
      null
    }
  }

  private val commentDelimiter: String = '\u001F'.toString()

  /**
   * Converts a single Comment object into a formatted string for Firestore storage.
   *
   * @param comment The Comment object to convert.
   * @return A string representing the Comment, formatted as
   *   "commentId<delimiter>userId<delimiter>userName<delimiter>content<delimiter>creationDate<delimiter>editedDate".
   *
   * Each field is separated by the `commentDelimiter` for easy parsing during retrieval.
   */
  internal fun convertCommentToString(comment: Note.Comment): String {
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
  internal fun convertCommentsList(commentsList: List<Note.Comment>): List<String> {
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
}
