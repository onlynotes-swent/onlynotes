package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
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
  @Mock private lateinit var mockToDoQuerySnapshot: QuerySnapshot

  private lateinit var noteRepositoryFirestore: NoteRepositoryFirestore

  private val note =
      Note(
          id = "1",
          type = Type.NORMAL_TEXT,
          name = "name",
          title = "title",
          content = "content",
          date = Timestamp.now(),
          userId = "1",
          image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

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
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = noteRepositoryFirestore.getNewUid()
    assert(uid == "1")
  }

  @Test
  fun documentSnapshotToNoteConvertsSnapshotToNote() {
    val currentTime = Timestamp.now()

    `when`(mockDocumentSnapshot.id).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("type")).thenReturn(Type.NORMAL_TEXT.name)
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("name")
    `when`(mockDocumentSnapshot.getString("title")).thenReturn("title")
    `when`(mockDocumentSnapshot.getString("content")).thenReturn("content")
    `when`(mockDocumentSnapshot.getTimestamp("date")).thenReturn(currentTime)
    `when`(mockDocumentSnapshot.getString("userId")).thenReturn("1")
    val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    `when`(mockDocumentSnapshot.get("image")).thenReturn(bitmap)

    val note = noteRepositoryFirestore.documentSnapshotToNote(mockDocumentSnapshot)

    assertNotNull(note)
    assert(note?.id == "1")
    assert(note?.type == Type.NORMAL_TEXT)
    assert(note?.name == "name")
    assert(note?.title == "title")
    assert(note?.content == "content")
    assert(note?.date == currentTime)
    assert(note?.userId == "1")
    note?.image?.let { assert(it.sameAs(bitmap)) }
  }

  @Test
  fun getNotes_callsDocuments() {
    // Ensure that mockToDoQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockToDoQuerySnapshot))

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockToDoQuerySnapshot.documents).thenReturn(listOf())

    noteRepositoryFirestore.getNotes("1", {}, {})

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockToDoQuerySnapshot).documents }
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

    // This test verifies that when we add a new ToDo, the Firestore `collection()` method is
    // called.
    noteRepositoryFirestore.addNote(note, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    // Ensure Firestore collection method was called to reference the notes collection
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
  fun updateNote_callsCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    noteRepositoryFirestore.updateNote(note, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }
}
