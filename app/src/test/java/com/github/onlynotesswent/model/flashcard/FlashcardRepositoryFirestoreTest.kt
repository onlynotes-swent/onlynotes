package com.github.onlynotesswent.model.flashcard

import android.os.Looper
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class FlashcardRepositoryFirestoreTest {
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuery: Query
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQueryTask: Task<QuerySnapshot>
  @Mock private lateinit var mockResolveTask: Task<Void>
  @Mock private lateinit var mockDocumentTask: Task<DocumentSnapshot>

  private lateinit var flashcardRepositoryFirestore: FlashcardRepositoryFirestore

  private val flashcard =
      Flashcard(
          id = "1",
          front = "front",
          back = "back",
          nextReview = Timestamp.now(),
          userId = "2",
          folderId = "3",
          noteId = "4")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    flashcardRepositoryFirestore = FlashcardRepositoryFirestore(mockFirestore)

    // Mock the behavior of the Firestore database
    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    // Set up whereEqualTo to return the mock Query
    `when`(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery)
    `when`(mockCollectionReference.get()).thenReturn(mockQueryTask)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    // Mock the behavior of the QuerySnapshot task
    `when`(mockQuery.get()).thenReturn(mockQueryTask)
    `when`(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>
      // Simulate a result being passed to the listener
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }
    `when`(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Mock the behavior of the DocumentReference set operation
    `when`(mockDocumentReference.set(any())).thenReturn(mockResolveTask)
    `when`(mockResolveTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<Void>
      listener.onSuccess(null)
      mockResolveTask
    }
    `when`(mockResolveTask.addOnFailureListener(any())).thenReturn(mockResolveTask)

    // Mock the behavior of the DocumentReference get operation
    `when`(mockDocumentReference.get()).thenReturn(mockDocumentTask)
    `when`(mockDocumentTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0]
              as com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockDocumentTask
    }
    `when`(mockDocumentTask.addOnFailureListener(any())).thenReturn(mockDocumentTask)

    // Mock the DocumentSnapshot to return expected fields for a Flashcard
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockDocumentSnapshot.id).thenReturn(flashcard.id)
    `when`(mockDocumentSnapshot.getString("front")).thenReturn(flashcard.front)
    `when`(mockDocumentSnapshot.getString("back")).thenReturn(flashcard.back)
    `when`(mockDocumentSnapshot.getTimestamp("nextReview")).thenReturn(flashcard.nextReview)
    `when`(mockDocumentSnapshot.getString("userId")).thenReturn(flashcard.userId)
    `when`(mockDocumentSnapshot.getString("folderId")).thenReturn(flashcard.folderId)
    `when`(mockDocumentSnapshot.getString("noteId")).thenReturn(flashcard.noteId)
  }

  @Test
  fun init_callsAuthStateListener() {
    val mockAuth = mock(FirebaseAuth::class.java)
    val mockUser = mock(FirebaseUser::class.java)
    `when`(mockAuth.currentUser).thenReturn(mockUser)

    var onSuccessCalled = false
    flashcardRepositoryFirestore.init(mockAuth) { onSuccessCalled = true }
    assert(onSuccessCalled)
  }

  @Test
  fun documentSnapshotToFlashcardConvertsToFlashcard() {
    `when`(mockDocumentSnapshot.id).thenReturn(flashcard.id)
    `when`(mockDocumentSnapshot.getString("front")).thenReturn(flashcard.front)
    `when`(mockDocumentSnapshot.getString("back")).thenReturn(flashcard.back)
    `when`(mockDocumentSnapshot.getTimestamp("nextReview")).thenReturn(flashcard.nextReview)
    `when`(mockDocumentSnapshot.getString("userId")).thenReturn(flashcard.userId)
    `when`(mockDocumentSnapshot.getString("folderId")).thenReturn(flashcard.folderId)

    val convertedFlashcard =
        flashcardRepositoryFirestore.documentSnapshotToFlashcard(mockDocumentSnapshot)

    assertEquals(flashcard, convertedFlashcard)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn(flashcard.id)
    val uid = flashcardRepositoryFirestore.getNewUid()
    assert(uid == flashcard.id)
  }

  @Test
  fun getFlashcards_success() {
    // Ensure that mockQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

    var flashcardTest: Flashcard? = null

    // Call the method under test
    flashcardRepositoryFirestore.getFlashcardsFrom(
        flashcard.userId,
        onSuccess = { flashcard -> flashcardTest = flashcard.firstOrNull() },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockQuerySnapshot).documents }

    // Assertions to verify that the correct flashcard is returned
    assertNotNull(flashcardTest)
    assertEquals(flashcard, flashcardTest)
  }

  @Test
  fun getFlashcards_failure() {
    // Mock exception occurring on query
    `when`(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask)
    `when`(mockQueryTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      listener.onFailure(Exception("Query failed"))
      mockQueryTask
    }
    // Call getUserByEmail
    var failureCalled = false
    flashcardRepositoryFirestore.getFlashcardsFrom(
        flashcard.userId,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { failureCalled = true })
    assert(failureCalled)
  }

  @Test
  fun getFlashcardById_success() {
    var flashcardTest: Flashcard? = null

    // Call the method under test
    flashcardRepositoryFirestore.getFlashcardById(
        flashcard.id,
        onSuccess = { flashcard -> flashcardTest = flashcard },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'get()' method was called
    verify(timeout(100)) { mockDocumentReference.get() }

    // Assertions to verify that the correct flashcard is returned
    assertNotNull(flashcardTest)
    assertEquals(flashcard, flashcardTest)
  }

  @Test
  fun getFlashcardById_failure() {
    // Mock exception occurring on query
    `when`(mockDocumentTask.addOnSuccessListener(any())).thenReturn(mockDocumentTask)
    `when`(mockDocumentTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      listener.onFailure(Exception("Query failed"))
      mockDocumentTask
    }
    // Call getUserByEmail
    var failureCalled = false
    flashcardRepositoryFirestore.getFlashcardById(
        flashcard.id,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { failureCalled = true })
    assert(failureCalled)
  }

  @Test
  fun getFlashcardsByFolder_success() {
    // mock whereEqualTo and get() method to return a mock Task<QuerySnapshot>
    `when`(mockCollectionReference.whereEqualTo(anyString(), any()))
        .thenReturn(mockCollectionReference) // return the mock collection reference itself

    var flashcardTest: Flashcard? = null

    // Call the method under test
    flashcardRepositoryFirestore.getFlashcardsByFolder(
        flashcard.folderId,
        onSuccess = { flashcards -> flashcardTest = flashcards.firstOrNull() },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'get()' method was called
    verify(timeout(100)) { mockDocumentReference.get() }

    // Assertions to verify that the correct flashcard is returned
    assertNotNull(flashcardTest)
    assertEquals(flashcard, flashcardTest)
  }

  @Test
  fun getFlashcardsByFolder_failure() {
    // Create a mock Task that returns an exception
    val exception = Exception("Test exception")
    val mockQueryTask: Task<QuerySnapshot> = Tasks.forException(exception)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)

    flashcardRepositoryFirestore.getFlashcardsByFolder(
        flashcard.folderId,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { error -> assert(error.message == "Test exception") })
  }

  @Test
  fun getFlashcardsByNote_success() {
    // mock whereEqualTo and get() method to return a mock Task<QuerySnapshot>
    `when`(mockCollectionReference.whereEqualTo(anyString(), any()))
        .thenReturn(mockCollectionReference) // return the mock collection reference itself

    var flashcardTest: Flashcard? = null

    // Call the method under test
    flashcardRepositoryFirestore.getFlashcardsByNote(
        flashcard.noteId,
        onSuccess = { flashcard -> flashcardTest = flashcard.firstOrNull() },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockQuerySnapshot).documents }

    // Assertions to verify that the correct flashcard is returned
    assertNotNull(flashcardTest)
    assertEquals(flashcard, flashcardTest)
  }

  @Test
  fun getFlashcardsByNote_failure() {
    // Create a mock Task that returns an exception
    val exception = Exception("Test exception")
    val mockQueryTask: Task<QuerySnapshot> = Tasks.forException(exception)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)

    flashcardRepositoryFirestore.getFlashcardsByNote(
        flashcard.noteId,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { error -> assert(error.message == "Test exception") })
  }

  @Test
  fun addFlashcard_callsCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

    // This test verifies that when we add a new flashcard, the Firestore `collection()` method is
    // called.
    flashcardRepositoryFirestore.addFlashcard(flashcard, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    // Ensure Firestore collection method was called to reference the "flashcards" collection
    verify(mockDocumentReference).set(any())
  }

  @Test
  fun updateFlashcard_callsCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

    // This test verifies that when we update a flashcard, the Firestore `collection()` method is
    // called.
    flashcardRepositoryFirestore.updateFlashcard(flashcard, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    // Ensure Firestore collection method was called to reference the "flashcards" collection
    verify(mockDocumentReference).set(any())
  }

  @Test
  fun deleteFlashcard_callsDocument() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null)) // Simulate success

    flashcardRepositoryFirestore.deleteFlashcard(flashcard, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
  }
}
