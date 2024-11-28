package com.github.onlynotesswent.model.flashcard.deck

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class DeckRepositoryFirestoreTest {
    @Mock private lateinit var mockFirestore: FirebaseFirestore
    @Mock private lateinit var mockCollectionReference: CollectionReference
    @Mock private lateinit var mockDocumentReference: DocumentReference
    @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
    @Mock private lateinit var mockQuery: Query
    @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
    @Mock private lateinit var mockQueryTask: Task<QuerySnapshot>
    @Mock private lateinit var mockResolveTask: Task<Void>
    @Mock private lateinit var mockDocumentTask: Task<DocumentSnapshot>

    private lateinit var deckRepository: DeckRepositoryFirestore
    private val testException = Exception("Test exception")
    private val testFlashcardId = "6"
    private val testFlashcardIds = listOf("7", "8")

    private val testDeck = Deck(
        id = "1",
        name = "Deck",
        userId = "2",
        folderId = "3",
        flashcardIds = listOf("4", "5")
    )

    private fun verifyErrorLog(msg: String) {
        // Get all the logs
        val logs = ShadowLog.getLogs()

        // Check for the debug log that should be generated
        val errorLog =
            logs.find { it.type == Log.ERROR && it.tag == DeckRepositoryFirestore.TAG && it.msg == msg }
        assert(errorLog != null) { "Expected error log was not found!" }
    }

    private fun mockFailure(){
        // Mock query task to fail
        `when`(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask)
        `when`(mockQueryTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener =
                invocation.arguments[0] as com.google.android.gms.tasks.OnFailureListener
            // Simulate a failure being passed to the listener
            listener.onFailure(testException)
            mockQueryTask
        }
        // Mock document task to fail
        `when`(mockDocumentTask.addOnSuccessListener(any())).thenReturn(mockDocumentTask)
        `when`(mockDocumentTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener =
                invocation.arguments[0] as com.google.android.gms.tasks.OnFailureListener
            // Simulate a failure being passed to the listener
            listener.onFailure(testException)
            mockDocumentTask
        }
        // Mock resolve task to fail
        `when`(mockResolveTask.addOnSuccessListener(any())).thenReturn(mockResolveTask)
        `when`(mockResolveTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener =
                invocation.arguments[0] as com.google.android.gms.tasks.OnFailureListener
            // Simulate a failure being passed to the listener
            listener.onFailure(testException)
            mockResolveTask
        }
    }

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        deckRepository = DeckRepositoryFirestore(mockFirestore)

        val context = org.robolectric.RuntimeEnvironment.getApplication()
        FirebaseApp.initializeApp(context)

        // Mock the behavior of the Firestore database
        `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
        `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
        `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
        `when`(mockCollectionReference.whereEqualTo("folderId", testDeck.folderId)).thenReturn(
            mockQuery
        )
        `when`(
            mockCollectionReference.whereEqualTo(
                "userId",
                testDeck.userId
            )
        ).thenReturn(mockQuery)
        `when`(mockCollectionReference.get()).thenReturn(mockQueryTask)

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
            val listener =
                invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<Void>
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

        // Mock the behavior of the DocumentReference update operation
        `when`(mockDocumentReference.update(eq("flashcardIds"), any())).thenReturn(mockResolveTask)

        // Mock the behavior of the DocumentReference delete operation
        `when`(mockDocumentReference.delete()).thenReturn(mockResolveTask)


        // Mock the behavior of the QuerySnapshot
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
        // Mock the behavior of the DocumentSnapshot
        `when`(mockDocumentSnapshot.id).thenReturn(testDeck.id)
        `when`(mockDocumentSnapshot.getString("name")).thenReturn(testDeck.name)
        `when`(mockDocumentSnapshot.getString("userId")).thenReturn(testDeck.userId)
        `when`(mockDocumentSnapshot.getString("folderId")).thenReturn(testDeck.folderId)
        `when`(mockDocumentSnapshot.get("flashcardIds")).thenReturn(testDeck.flashcardIds)
    }


    @Test
    fun testDocumentSnapshotToDeck() {
        // Test with a valid document
        val deck = deckRepository.documentSnapshotToDeck(mockDocumentSnapshot)
        assertNotNull(deck)
        assertEquals(testDeck, deck)

        // Test with an invalid document
        val mockDocumentSnapshotEmpty = mock(DocumentSnapshot::class.java)
        `when`(mockDocumentSnapshotEmpty.id).thenReturn("1")
        `when`(mockDocumentSnapshotEmpty.getString("name")).thenReturn(null)
        `when`(mockDocumentSnapshotEmpty.getString("userId")).thenReturn(null)
        `when`(mockDocumentSnapshotEmpty.getString("folderId")).thenReturn(null)
        `when`(mockDocumentSnapshotEmpty.get("flashcardIds")).thenReturn(null)
        val deckEmpty = deckRepository.documentSnapshotToDeck(mockDocumentSnapshotEmpty)
        assertEquals(null, deckEmpty)
    }

    @Test
    fun testGetNewUid() {
        `when`(mockDocumentReference.id).thenReturn("1")
        val newUid = deckRepository.getNewUid()
        assertEquals("1", newUid)
    }

    @Test
    fun testGetDecksFrom() {
        var decks = emptyList<Deck>()
        deckRepository.getDecksFrom("2", {decks=it}, {fail("Should not fail")})
        verify(mockCollectionReference).whereEqualTo("userId", "2")
        assertEquals(listOf(testDeck), decks)
    }

    @Test
    fun testGetDecksFromFailure() {
        `when`(mockCollectionReference.whereEqualTo("userId","3")).thenReturn(mockQuery)
        mockFailure()

        var exception: Exception? = null
        deckRepository.getDecksFrom("3", {fail("Should not succeed")}, {exception=it})
        verify(mockCollectionReference).whereEqualTo("userId", "3")
        verifyErrorLog("Error getting decks from user")
        assertNotNull(exception)
    }

    @Test
    fun testGetDeckById() {
        var deck: Deck? = null
        deckRepository.getDeckById("1", {deck=it}, {fail("Should not fail")})
        verify(mockCollectionReference).document("1")
        assertEquals(testDeck, deck)
    }

    @Test
    fun testGetDeckByIdFailure() {
        mockFailure()
        var exception: Exception? = null
        deckRepository.getDeckById("2", {fail("Should not succeed")}, {exception=it})
        verify(mockCollectionReference).document("2")
        verifyErrorLog("Error getting deck by ID")
        assertNotNull(exception)
    }

    @Test
    fun testUpdateDeck() {
        var wasCalled = false
        deckRepository.updateDeck(testDeck, {wasCalled = true}, {fail("Should not fail")})
        verify(mockDocumentReference).set(testDeck)
        assert(wasCalled)
    }

    @Test
    fun testUpdateDeckFailure() {
        mockFailure()
        var exception: Exception? = null
        deckRepository.updateDeck(testDeck, {fail("Should not succeed")}, {exception=it})
        verify(mockDocumentReference).set(testDeck)
        verifyErrorLog("Error updating deck")
        assertNotNull(exception)
    }

    @Test
    fun testGetDecksByFolder() {
        var decks = emptyList<Deck>()
        deckRepository.getDecksByFolder("3", {decks=it}, {fail("Should not fail")})
        verify(mockCollectionReference).whereEqualTo("folderId", "3")
        assertEquals(listOf(testDeck), decks)
    }

    @Test
    fun testGetDecksByFolderFailure() {
        `when`(mockCollectionReference.whereEqualTo("folderId","4")).thenReturn(mockQuery)
        mockFailure()

        var exception: Exception? = null
        deckRepository.getDecksByFolder("4", {fail("Should not succeed")}, {exception=it})
        verify(mockCollectionReference).whereEqualTo("folderId", "4")
        verifyErrorLog("Error getting decks by folder")
        assertNotNull(exception)
    }

    @Test
    fun testAddFlashcardId() {
        var wasCalled = false
        deckRepository.addFlashcardToDeck(testDeck.id, testFlashcardId, {wasCalled = true}, {fail("Should not fail")})
        verify(mockDocumentReference).update(eq("flashcardIds"), any())
        assert(wasCalled)
    }

    @Test
    fun testAddFlashcardIdFailure() {
        mockFailure()
        var exception: Exception? = null
        deckRepository.addFlashcardToDeck(testDeck.id, testFlashcardId, {fail("Should not succeed")}, {exception=it})
        verify(mockDocumentReference).update(eq("flashcardIds"), any())
        verifyErrorLog("Error adding flashcard to deck")
        assertNotNull(exception)
    }

    @Test
    fun testAddFlashcardIds() {
        var wasCalled = false
        deckRepository.addFlashcardsToDeck(testDeck.id, testFlashcardIds, {wasCalled = true}, {fail("Should not fail")})
        verify(mockDocumentReference).update(eq("flashcardIds"), any())
        assert(wasCalled)
    }

    @Test
    fun testAddFlashcardIdsFailure() {
        mockFailure()
        var exception: Exception? = null
        deckRepository.addFlashcardsToDeck(testDeck.id, testFlashcardIds, {fail("Should not succeed")}, {exception=it})
        verify(mockDocumentReference).update(eq("flashcardIds"), any())
        verifyErrorLog("Error adding flashcards to deck")
        assertNotNull(exception)
    }


    @Test
    fun testRemoveFlashcard() {
        var wasCalled = false
        deckRepository.removeFlashcardFromDeck(testDeck.id, testFlashcardId, {wasCalled = true}, {fail("Should not fail")})
        verify(mockDocumentReference).update(eq("flashcardIds"), any())
        assert(wasCalled)
    }

    @Test
    fun testRemoveFlashcardFailure() {
        mockFailure()
        var exception: Exception? = null
        deckRepository.removeFlashcardFromDeck(testDeck.id, testFlashcardId, {fail("Should not succeed")}, {exception=it})
        verify(mockDocumentReference).update(eq("flashcardIds"), any())
        verifyErrorLog("Error removing flashcard from deck")
        assertNotNull(exception)
    }

    @Test
    fun testDeleteDeck() {
        var wasCalled = false
        deckRepository.deleteDeck(testDeck, {wasCalled = true}, {fail("Should not fail")})
        verify(mockDocumentReference).delete()
        assert(wasCalled)
    }

    @Test
    fun testDeleteDeckFailure() {
        mockFailure()
        var exception: Exception? = null
        deckRepository.deleteDeck(testDeck, {fail("Should not succeed")}, {exception=it})
        verify(mockDocumentReference).delete()
        verifyErrorLog("Error deleting deck")
        assertNotNull(exception)
    }
}