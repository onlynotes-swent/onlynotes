package com.github.onlynotesswent.model.users

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class UserRepositoryFirestoreTest {

    @Mock private lateinit var mockFirestore: FirebaseFirestore
    @Mock private lateinit var mockDocumentReference: DocumentReference
    @Mock private lateinit var mockCollectionReference: CollectionReference
    @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
    @Mock private lateinit var mockResolveTask: com.google.android.gms.tasks.Task<Void>
    private lateinit var userRepositoryFirestore: UserRepositoryFirestore

    private val defaultTimestamp = Timestamp.now()

    private val user = User(
        name = "User",
        email = "example@gmail.com",
        uid = "1",
        dateOfJoining = defaultTimestamp,
        rating = 0.0)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        userRepositoryFirestore = UserRepositoryFirestore(mockFirestore)

        `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
        `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
        `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    }

    @Test
    fun `getNewUid should return new UID`() {
        `when`(mockDocumentReference.id).thenReturn("1")
        val uid = userRepositoryFirestore.getNewUid()
        assertEquals("1", uid)
    }

    @Test
    fun `documentSnapshotToUser should convert DocumentSnapshot to User`() {
        `when`(mockDocumentSnapshot.getString("name")).thenReturn(user.name)
        `when`(mockDocumentSnapshot.getString("email")).thenReturn(user.email)
        `when`(mockDocumentSnapshot.getString("uid")).thenReturn(user.uid)
        `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(user.dateOfJoining)
        `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(user.rating)

        val userTest = userRepositoryFirestore.documentSnapshotToUser(mockDocumentSnapshot)

        assertEquals(user.name, userTest.name)
        assertEquals(user.email, userTest.email)
        assertEquals(user.uid, userTest.uid)
        assertEquals(user.dateOfJoining, userTest.dateOfJoining)
        assertEquals(user.rating, userTest.rating)

        // Test with empty user
        `when`(mockDocumentSnapshot.getString("name")).thenReturn(null)
        `when`(mockDocumentSnapshot.getString("email")).thenReturn(null)
        `when`(mockDocumentSnapshot.getString("uid")).thenReturn(null)
        `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(null)
        `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(null)

        val userTestEmpty = userRepositoryFirestore.documentSnapshotToUser(mockDocumentSnapshot)

        assertEquals("", userTestEmpty.name)
        assertEquals("", userTestEmpty.email)
        assertEquals("", userTestEmpty.uid)
        assertNotNull(userTestEmpty.dateOfJoining)
        assertEquals(0.0, userTestEmpty.rating)
    }

    @Test
    fun `updateUser should call Firestore collection`() {
        `when`(mockDocumentReference.set(any())).thenReturn(mockResolveTask)
        `when`(mockResolveTask.addOnSuccessListener(any())).thenReturn(mockResolveTask)

        userRepositoryFirestore.updateUser(user, {}, {})
        verify(mockCollectionReference, timeout(1000)).document(user.uid)
    }

    @Test
    fun `deleteUserById should call Firestore collection`() {
        `when`(mockDocumentReference.delete()).thenReturn(mockResolveTask)
        `when`(mockResolveTask.addOnSuccessListener(any())).thenReturn(mockResolveTask)

        userRepositoryFirestore.deleteUserById(user.uid, {}, {})
        verify(mockCollectionReference, timeout(1000)).document(user.uid)
    }

    @Test
    fun `getUserById should call Firestore collection`() {
        val mockDocumentTask = mock(Task::class.java) as Task<DocumentSnapshot>
        `when`(mockDocumentReference.get()).thenReturn(mockDocumentTask)
        `when`(mockDocumentTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>
            listener.onSuccess(mockDocumentSnapshot)
            mockDocumentTask
        }

        `when`(mockDocumentSnapshot.getString("name")).thenReturn(user.name)
        `when`(mockDocumentSnapshot.getString("email")).thenReturn(user.email)
        `when`(mockDocumentSnapshot.getString("uid")).thenReturn(user.uid)
        `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(user.dateOfJoining)
        `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(user.rating)

        var userTest: User? = null
        userRepositoryFirestore.getUserById(user.uid, { usr -> userTest = usr }, {})
        verify(mockCollectionReference, timeout(1000)).document(user.uid)
        assertNotNull(userTest)
        assertEquals(user.name, userTest!!.name)
        assertEquals(user.email, userTest!!.email)
        assertEquals(user.uid, userTest!!.uid)
        assertEquals(user.dateOfJoining, userTest!!.dateOfJoining)
        assertEquals(user.rating, userTest!!.rating)
    }

    @Test
    fun `addUser should call Firestore collection`() {
        `when`(mockDocumentReference.set(any())).thenReturn(mockResolveTask)
        `when`(mockResolveTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<Void>
            listener.onSuccess(null)
            mockResolveTask
        }

        userRepositoryFirestore.addUser(user, {}, {})
        verify(mockCollectionReference, timeout(1000)).document(user.uid)
    }

    @Test
    fun `init should call FirebaseAuth currentUser`() {
        val mockAuth = mock(FirebaseAuth::class.java)
        val mockUser = mock(FirebaseUser::class.java)
        `when`(mockAuth.currentUser).thenReturn(mockUser)

        userRepositoryFirestore.init(mockAuth) {}
        verify(mockAuth, timeout(1000)).currentUser
    }
}