package com.github.onlynotesswent.model.note

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.github.onlynotesswent.utils.Course
import com.github.onlynotesswent.utils.Visibility
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class NoteRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockDocumentSnapshot2: DocumentSnapshot
  @Mock private lateinit var mockDocumentSnapshot3: DocumentSnapshot
  @Mock private lateinit var mockDocumentSnapshot4: DocumentSnapshot
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQuerySnapshotTask: Task<QuerySnapshot>

  private lateinit var noteRepositoryFirestore: NoteRepositoryFirestore

  private val testNotePublic =
      Note(
          id = "1",
          title = "title",
          date = Timestamp.now(),
          visibility = Visibility.PUBLIC,
          userId = "1",
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
          comments =
              Note.CommentCollection(
                  listOf(Note.Comment("1", "1", "bob", "1", Timestamp.now(), Timestamp.now()))))
  private val testNotePrivate =
      Note(
          id = "2",
          title = "title",
          date = Timestamp.now(),
          visibility = Visibility.PRIVATE,
          userId = "1",
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
          comments =
              Note.CommentCollection(
                  listOf(Note.Comment("1", "1", "bob", "1", Timestamp.now(), Timestamp.now()))))

  private val testSubNotePublic =
      Note(
          id = "1",
          title = "title",
          date = Timestamp.now(),
          visibility = Visibility.PUBLIC,
          userId = "1",
          folderId = "1",
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
      )

  private val testSubNotePrivate =
      Note(
          id = "1",
          title = "title",
          date = Timestamp.now(),
          visibility = Visibility.PRIVATE,
          userId = "1",
          folderId = "1",
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
          comments =
              Note.CommentCollection(
                  listOf(Note.Comment("1", "1", "bob", "1", Timestamp.now(), Timestamp.now()))))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    noteRepositoryFirestore = NoteRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)

    `when`(mockCollectionReference.get()).thenReturn(mockQuerySnapshotTask)
    `when`(mockQuerySnapshotTask.result).thenReturn(mockQuerySnapshot)
    `when`(mockQuerySnapshotTask.isSuccessful).thenReturn(true)
    `when`(mockQuerySnapshotTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnCompleteListener<QuerySnapshot>>(0)
      // Simulate a result being passed to the listener
      listener.onComplete(mockQuerySnapshotTask)
      mockQuerySnapshotTask
    }

    // Ensure the documents field is properly initialized
    `when`(mockQuerySnapshot.documents)
        .thenReturn(
            listOf(
                mockDocumentSnapshot,
                mockDocumentSnapshot2,
                mockDocumentSnapshot3,
                mockDocumentSnapshot4))

    `when`(mockDocumentSnapshot.id).thenReturn(testNotePublic.id)
    `when`(mockDocumentSnapshot.getString("title")).thenReturn(testNotePublic.title)
    `when`(mockDocumentSnapshot.getTimestamp("date")).thenReturn(testNotePublic.date)
    `when`(mockDocumentSnapshot.getString("visibility"))
        .thenReturn(testNotePublic.visibility.toString())
    `when`(mockDocumentSnapshot.getString("courseCode"))
        .thenReturn(testNotePublic.noteCourse.courseCode)
    `when`(mockDocumentSnapshot.getString("courseName"))
        .thenReturn(testNotePublic.noteCourse.courseName)
    `when`(mockDocumentSnapshot.getLong("courseYear"))
        .thenReturn(testNotePublic.noteCourse.courseYear.toLong())
    `when`(mockDocumentSnapshot.getString("publicPath"))
        .thenReturn(testNotePublic.noteCourse.publicPath)
    `when`(mockDocumentSnapshot.getString("userId")).thenReturn(testNotePublic.userId)

    `when`(mockDocumentSnapshot2.id).thenReturn(testNotePrivate.id)
    `when`(mockDocumentSnapshot2.getString("title")).thenReturn(testNotePrivate.title)
    `when`(mockDocumentSnapshot2.getTimestamp("date")).thenReturn(testNotePrivate.date)
    `when`(mockDocumentSnapshot2.getString("visibility"))
        .thenReturn(testNotePrivate.visibility.toString())
    `when`(mockDocumentSnapshot.getString("courseCode"))
        .thenReturn(testNotePrivate.noteCourse.courseCode)
    `when`(mockDocumentSnapshot.getString("courseName"))
        .thenReturn(testNotePrivate.noteCourse.courseName)
    `when`(mockDocumentSnapshot.getLong("courseYear"))
        .thenReturn(testNotePrivate.noteCourse.courseYear.toLong())
    `when`(mockDocumentSnapshot.getString("publicPath"))
        .thenReturn(testNotePrivate.noteCourse.publicPath)
    `when`(mockDocumentSnapshot2.getString("userId")).thenReturn(testNotePrivate.userId)
    `when`(mockDocumentSnapshot2.get("commentsList")).thenReturn(testNotePrivate.comments)

    `when`(mockDocumentSnapshot3.id).thenReturn(testSubNotePublic.id)
    `when`(mockDocumentSnapshot3.getString("title")).thenReturn(testSubNotePublic.title)
    `when`(mockDocumentSnapshot3.getTimestamp("date")).thenReturn(testSubNotePublic.date)
    `when`(mockDocumentSnapshot3.getString("visibility"))
        .thenReturn(testSubNotePublic.visibility.toString())
    `when`(mockDocumentSnapshot3.getString("courseCode"))
        .thenReturn(testSubNotePublic.noteCourse.courseCode)
    `when`(mockDocumentSnapshot3.getString("courseName"))
        .thenReturn(testSubNotePublic.noteCourse.courseName)
    `when`(mockDocumentSnapshot3.getLong("courseYear"))
        .thenReturn(testSubNotePublic.noteCourse.courseYear.toLong())
    `when`(mockDocumentSnapshot3.getString("publicPath"))
        .thenReturn(testSubNotePublic.noteCourse.publicPath)
    `when`(mockDocumentSnapshot3.getString("userId")).thenReturn(testSubNotePublic.userId)
    `when`(mockDocumentSnapshot3.getString("folderId")).thenReturn(testSubNotePublic.folderId)

    `when`(mockDocumentSnapshot4.id).thenReturn(testSubNotePrivate.id)
    `when`(mockDocumentSnapshot4.getString("title")).thenReturn(testSubNotePrivate.title)
    `when`(mockDocumentSnapshot4.getTimestamp("date")).thenReturn(testSubNotePrivate.date)
    `when`(mockDocumentSnapshot4.getString("visibility"))
        .thenReturn(testSubNotePrivate.visibility.toString())
    `when`(mockDocumentSnapshot4.getString("courseCode"))
        .thenReturn(testSubNotePrivate.noteCourse.courseCode)
    `when`(mockDocumentSnapshot4.getString("courseName"))
        .thenReturn(testSubNotePrivate.noteCourse.courseName)
    `when`(mockDocumentSnapshot4.getLong("courseYear"))
        .thenReturn(testSubNotePrivate.noteCourse.courseYear.toLong())
    `when`(mockDocumentSnapshot4.getString("publicPath"))
        .thenReturn(testSubNotePrivate.noteCourse.publicPath)
    `when`(mockDocumentSnapshot4.getString("userId")).thenReturn(testSubNotePrivate.userId)
    `when`(mockDocumentSnapshot4.getString("folderId")).thenReturn(testSubNotePrivate.folderId)
  }

  private fun compareNotesButNotImage(note1: Note, note2: Note) {
    assert(note1.id == note2.id)
    assert(note1.title == note2.title)
    assert(note1.date == note2.date)
    assert(note1.visibility == note2.visibility)
    assert(note1.userId == note2.userId)
    assert(note1.folderId == note2.folderId)
    assert(note1.noteCourse.courseCode == note2.noteCourse.courseCode)
    assert(note1.noteCourse.courseName == note2.noteCourse.courseName)
    assert(note1.noteCourse.courseYear == note2.noteCourse.courseYear)
    assert(note1.noteCourse.publicPath == note2.noteCourse.publicPath)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = noteRepositoryFirestore.getNewUid()
    assert(uid == "1")
  }

  @Test
  fun documentSnapshotToNoteConvertsSnapshotToNote() {

    val resultingNote = noteRepositoryFirestore.documentSnapshotToNote(mockDocumentSnapshot)

    assertNotNull(resultingNote)
    compareNotesButNotImage(resultingNote!!, testNotePublic)
  }

  @Test
  fun getPublicNotes_callsDocuments() {

    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))

    var receivedNotes: List<Note>? = null
    noteRepositoryFirestore.getPublicNotes({ receivedNotes = it }, { assert(false) })

    assertNotNull(receivedNotes)
    assert(receivedNotes!!.size == 1)
    compareNotesButNotImage(receivedNotes?.get(0)!!, testNotePublic)
    verify(timeout(100)) { mockQuerySnapshot.documents }
  }

  @Test
  fun getNotesFrom_callsDocuments() {
    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))

    var receivedNotes: List<Note>? = null
    noteRepositoryFirestore.getNotesFrom(
        testNotePublic.userId, { receivedNotes = it }, { assert(false) })
    assertNotNull(receivedNotes)

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun getRootNotesFrom_callsDocuments() {

    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))

    var receivedNotes: List<Note>? = null
    noteRepositoryFirestore.getRootNotesFrom(
        testNotePublic.userId, { receivedNotes = it }, { assert(false) })
    assertNotNull(receivedNotes)

    verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun getNoteById_callsDocument() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    noteRepositoryFirestore.getNoteById("1", {}, {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(timeout(100)) { mockDocumentSnapshot.id }
  }

  @Test
  fun addNote_callsCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    // This test verifies that when we add a new note, the Firestore `collection()` method is
    // called.
    noteRepositoryFirestore.addNote(testNotePublic, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    // Ensure Firestore collection method was called to reference the publicNotes collection
    verify(mockDocumentReference).set(any())
  }

  @Test
  fun deleteNoteById_callsDocument() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    noteRepositoryFirestore.deleteNoteById("1", onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
  }

  @Test
  fun deleteNotesByUserId_callsDocuments() {
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot3, mockDocumentSnapshot4))

    noteRepositoryFirestore.deleteNotesByUserId("1", onSuccess = {}, onFailure = {})

    verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun deleteNotesByUserId_fail() {
    val errorMessage = "TestError"
    `when`(mockQuerySnapshotTask.isSuccessful).thenReturn(false)
    `when`(mockQuerySnapshotTask.exception).thenReturn(Exception(errorMessage))
    `when`(mockQuerySnapshotTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnCompleteListener<QuerySnapshot>>(0)
      // Simulate a result being passed to the listener
      listener.onComplete(mockQuerySnapshotTask)
      mockQuerySnapshotTask
    }
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot3, mockDocumentSnapshot4))
    var exceptionThrown: Exception? = null
    noteRepositoryFirestore.deleteNotesByUserId(
        "1", onSuccess = {}, onFailure = { e -> exceptionThrown = e })
    assertNotNull(exceptionThrown)
    assertEquals(errorMessage, exceptionThrown?.message)
  }

  @Test
  fun updateNote_callsCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    noteRepositoryFirestore.updateNote(testNotePublic, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun getNotesFromFolder_callsDocuments() {
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot3, mockDocumentSnapshot4))

    var receivedNotes: List<Note>? = null
    noteRepositoryFirestore.getNotesFromFolder(
        testSubNotePublic.folderId!!, { receivedNotes = it }, { assert(false) })
    assertNotNull(receivedNotes)

    verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun deleteNotesFromFolder_callsDocuments() {
      `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

      noteRepositoryFirestore.deleteNotesFromFolder("1", onSuccess = {}, onFailure = {})

      shadowOf(Looper.getMainLooper()).idle()

      // Ensure the delete method was called twice (once for each sub note)
      verify(mockDocumentReference, times(2)).delete()
  }
}
