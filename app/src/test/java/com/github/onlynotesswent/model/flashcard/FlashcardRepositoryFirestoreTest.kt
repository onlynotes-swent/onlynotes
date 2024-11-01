package com.github.onlynotesswent.model.flashcard

import android.os.Looper
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
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
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockToDoQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQuery: Query

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

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockToDoQuerySnapshot))
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

    assert(convertedFlashcard?.id == flashcard.id)
    assert(convertedFlashcard?.front == flashcard.front)
    assert(convertedFlashcard?.back == flashcard.back)
    assert(convertedFlashcard?.nextReview == flashcard.nextReview)
    assert(convertedFlashcard?.userId == flashcard.userId)
    assert(convertedFlashcard?.folderId == flashcard.folderId)
  }

  @Test
  fun getNewUid() {
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.id).thenReturn(flashcard.id)
    val uid = flashcardRepositoryFirestore.getNewUid()
    assert(uid == flashcard.id)
  }

  @Test
  fun getFlashcards_callsDocuments() {
    // Ensure that mockToDoQuerySnapshot is properly initialized and mocked
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockToDoQuerySnapshot))

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockToDoQuerySnapshot.documents).thenReturn(listOf())

    // Call the method under test
    flashcardRepositoryFirestore.getFlashcards(
        flashcard.userId,
        onSuccess = {

          // Do nothing; we just want to verify that the 'documents' field was accessed
        },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'documents' field was accessed
    verify(timeout(100)) { (mockToDoQuerySnapshot).documents }
  }

  @Test
  fun getFlashcards_success() {
    // Create mock QuerySnapshot with mock documents
    `when`(mockToDoQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.getString("userId")).thenReturn(flashcard.userId)

    // Mock the successful task result with the mock QuerySnapshot
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockToDoQuerySnapshot)
    `when`(mockCollectionReference.get()).thenReturn(mockTask)

    flashcardRepositoryFirestore.getFlashcards(
        flashcard.userId,
        onSuccess = { flashcards ->
          assert(flashcards.isNotEmpty())
          assert(flashcards[0].userId == flashcard.userId)
        },
        onFailure = { fail("Failure callback should not be called") })
  }

  @Test
  fun getFlashcards_failure() {
    // Create a failed task with an exception
    val exception = Exception("Test exception")
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)
    `when`(mockCollectionReference.get()).thenReturn(mockTask)

    flashcardRepositoryFirestore.getFlashcards(
        flashcard.userId,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { error -> assert(error.message == "Test exception") })
  }

  @Test
  fun getFlashcardById_callsDocument() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    // Call the method under test
    flashcardRepositoryFirestore.getFlashcardById(
        flashcard.id,
        onSuccess = {

          // Do nothing; we just want to verify that the 'get()' method was called
        },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'get()' method was called
    verify(timeout(100)) { mockDocumentReference.get() }
  }

  @Test
  fun getFlashcardsByFolder_callsDocuments() {
    // mock whereEqualTo and get() method to return a mock Task<QuerySnapshot>
    `when`(mockCollectionReference.whereEqualTo(anyString(), any()))
        .thenReturn(mockCollectionReference) // return the mock collection reference itself

    // Create a mock Task that would represent the get() operation
    val mockQueryTask: Task<QuerySnapshot> = Tasks.forResult(mockToDoQuerySnapshot)

    // Set up the get() method to return the mocked Task
    `when`(mockCollectionReference.get()).thenReturn(mockQueryTask)

    // Mock the query snapshot to return an empty list for documents
    `when`(mockToDoQuerySnapshot.documents).thenReturn(listOf())

    // Call the method under test
    flashcardRepositoryFirestore.getFlashcardsByFolder(
        flashcard.folderId,
        onSuccess = {
          // Do nothing; we just want to verify that the 'documents' field was accessed
        },
        onFailure = { fail("Failure callback should not be called") })

    // Verify that the 'get()' method was called
    verify(timeout(100)) { mockDocumentReference.get() }
  }

  @Test
  fun getFlashcardsByFolder_success() {
    // Create a mock Query to represent the result of whereEqualTo
    val mockQuery: Query = mock(Query::class.java)

    // Set up whereEqualTo to return the mock Query
    `when`(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery)

    // Create a mock Task that returns a successful QuerySnapshot
    val mockQueryTask: Task<QuerySnapshot> = Tasks.forResult(mockToDoQuerySnapshot)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)

    // Mock the documents in the QuerySnapshot
    `when`(mockToDoQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.getString("folderId")).thenReturn(flashcard.folderId)

    flashcardRepositoryFirestore.getFlashcardsByFolder(
        flashcard.folderId,
        onSuccess = { flashcards ->
          assert(flashcards.isNotEmpty())
          assert(flashcards[0].folderId == flashcard.folderId)
        },
        onFailure = { fail("Failure callback should not be called") })
  }

  @Test
  fun getFlashcardsByFolder_failure() {
    // Create a mock Query to represent the result of whereEqualTo
    val mockQuery: Query = mock(Query::class.java)

    // Set up whereEqualTo to return the mock Query
    `when`(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery)

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
