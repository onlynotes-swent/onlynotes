package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
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
          content = "content",
          date = Timestamp.now(),
          visibility = Note.Visibility.PUBLIC,
          userId = "1",
          noteClass = Note.Class("CS-100", "Sample Class", 2024, "path"),
          image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
          comments =
              Note.CommentCollection(
                  listOf(Note.Comment("1", "1", "bob", "1", Timestamp.now(), Timestamp.now()))))
  private val testNotePrivate =
      Note(
          id = "2",
          title = "title",
          content = "content",
          date = Timestamp.now(),
          visibility = Note.Visibility.PRIVATE,
          userId = "1",
          noteClass = Note.Class("CS-100", "Sample Class", 2024, "path"),
          image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
          comments =
              Note.CommentCollection(
                  listOf(Note.Comment("1", "1", "bob", "1", Timestamp.now(), Timestamp.now()))))

  private val testSubNotePublic =
      Note(
          id = "1",
          title = "title",
          content = "content",
          date = Timestamp.now(),
          visibility = Note.Visibility.PUBLIC,
          userId = "1",
          folderId = "1",
          noteClass = Note.Class("CS-100", "Sample Class", 2024, "path"),
          image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

  private val testSubNotePrivate =
      Note(
          id = "1",
          title = "title",
          content = "content",
          date = Timestamp.now(),
          visibility = Note.Visibility.PRIVATE,
          userId = "1",
          folderId = "1",
          noteClass = Note.Class("CS-100", "Sample Class", 2024, "path"),
          image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
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
    `when`(mockDocumentSnapshot.getString("content")).thenReturn(testNotePublic.content)
    `when`(mockDocumentSnapshot.getTimestamp("date")).thenReturn(testNotePublic.date)
    `when`(mockDocumentSnapshot.getString("visibility"))
        .thenReturn(testNotePublic.visibility.toString())
    `when`(mockDocumentSnapshot.getString("classCode"))
        .thenReturn(testNotePublic.noteClass.classCode)
    `when`(mockDocumentSnapshot.getString("className"))
        .thenReturn(testNotePublic.noteClass.className)
    `when`(mockDocumentSnapshot.getLong("classYear"))
        .thenReturn(testNotePublic.noteClass.classYear.toLong())
    `when`(mockDocumentSnapshot.getString("publicPath"))
        .thenReturn(testNotePublic.noteClass.publicPath)
    `when`(mockDocumentSnapshot.getString("userId")).thenReturn(testNotePublic.userId)
    `when`(mockDocumentSnapshot.get("image")).thenReturn(testNotePublic.image)

    `when`(mockDocumentSnapshot2.id).thenReturn(testNotePrivate.id)
    `when`(mockDocumentSnapshot2.getString("title")).thenReturn(testNotePrivate.title)
    `when`(mockDocumentSnapshot2.getString("content")).thenReturn(testNotePrivate.content)
    `when`(mockDocumentSnapshot2.getTimestamp("date")).thenReturn(testNotePrivate.date)
    `when`(mockDocumentSnapshot2.getString("visibility"))
        .thenReturn(testNotePrivate.visibility.toString())
    `when`(mockDocumentSnapshot.getString("classCode"))
        .thenReturn(testNotePrivate.noteClass.classCode)
    `when`(mockDocumentSnapshot.getString("className"))
        .thenReturn(testNotePrivate.noteClass.className)
    `when`(mockDocumentSnapshot.getLong("classYear"))
        .thenReturn(testNotePrivate.noteClass.classYear.toLong())
    `when`(mockDocumentSnapshot.getString("publicPath"))
        .thenReturn(testNotePrivate.noteClass.publicPath)
    `when`(mockDocumentSnapshot2.getString("userId")).thenReturn(testNotePrivate.userId)
    `when`(mockDocumentSnapshot2.get("image")).thenReturn(testNotePrivate.image)
    `when`(mockDocumentSnapshot2.get("commentsList")).thenReturn(testNotePrivate.comments)

    `when`(mockDocumentSnapshot3.id).thenReturn(testSubNotePublic.id)
    `when`(mockDocumentSnapshot3.getString("title")).thenReturn(testSubNotePublic.title)
    `when`(mockDocumentSnapshot3.getString("content")).thenReturn(testSubNotePublic.content)
    `when`(mockDocumentSnapshot3.getTimestamp("date")).thenReturn(testSubNotePublic.date)
    `when`(mockDocumentSnapshot3.getString("visibility"))
        .thenReturn(testSubNotePublic.visibility.toString())
    `when`(mockDocumentSnapshot3.getString("classCode"))
        .thenReturn(testSubNotePublic.noteClass.classCode)
    `when`(mockDocumentSnapshot3.getString("className"))
        .thenReturn(testSubNotePublic.noteClass.className)
    `when`(mockDocumentSnapshot3.getLong("classYear"))
        .thenReturn(testSubNotePublic.noteClass.classYear.toLong())
    `when`(mockDocumentSnapshot3.getString("publicPath"))
        .thenReturn(testSubNotePublic.noteClass.publicPath)
    `when`(mockDocumentSnapshot3.getString("userId")).thenReturn(testSubNotePublic.userId)
    `when`(mockDocumentSnapshot3.getString("folderId")).thenReturn(testSubNotePublic.folderId)
    `when`(mockDocumentSnapshot3.get("image")).thenReturn(testSubNotePublic.image)

    `when`(mockDocumentSnapshot4.id).thenReturn(testSubNotePrivate.id)
    `when`(mockDocumentSnapshot4.getString("title")).thenReturn(testSubNotePrivate.title)
    `when`(mockDocumentSnapshot4.getString("content")).thenReturn(testSubNotePrivate.content)
    `when`(mockDocumentSnapshot4.getTimestamp("date")).thenReturn(testSubNotePrivate.date)
    `when`(mockDocumentSnapshot4.getString("visibility"))
        .thenReturn(testSubNotePrivate.visibility.toString())
    `when`(mockDocumentSnapshot4.getString("classCode"))
        .thenReturn(testSubNotePrivate.noteClass.classCode)
    `when`(mockDocumentSnapshot4.getString("className"))
        .thenReturn(testSubNotePrivate.noteClass.className)
    `when`(mockDocumentSnapshot4.getLong("classYear"))
        .thenReturn(testSubNotePrivate.noteClass.classYear.toLong())
    `when`(mockDocumentSnapshot4.getString("publicPath"))
        .thenReturn(testSubNotePrivate.noteClass.publicPath)
    `when`(mockDocumentSnapshot4.getString("userId")).thenReturn(testSubNotePrivate.userId)
    `when`(mockDocumentSnapshot4.getString("folderId")).thenReturn(testSubNotePrivate.folderId)
    `when`(mockDocumentSnapshot4.get("image")).thenReturn(testSubNotePrivate.image)
  }

  private fun compareNotesButNotImage(note1: Note, note2: Note) {
    assert(note1.id == note2.id)
    assert(note1.title == note2.title)
    assert(note1.content == note2.content)
    assert(note1.date == note2.date)
    assert(note1.visibility == note2.visibility)
    assert(note1.userId == note2.userId)
    assert(note1.folderId == note2.folderId)
    assert(note1.noteClass.classCode == note2.noteClass.classCode)
    assert(note1.noteClass.className == note2.noteClass.className)
    assert(note1.noteClass.classYear == note2.noteClass.classYear)
    assert(note1.noteClass.publicPath == note2.noteClass.publicPath)
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
}
