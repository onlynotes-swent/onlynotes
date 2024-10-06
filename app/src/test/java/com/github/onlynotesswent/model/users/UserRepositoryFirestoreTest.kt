package com.github.onlynotesswent.model.users

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
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

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirestoreTest {

    @Mock private lateinit var mockFirestore: FirebaseFirestore
    @Mock private lateinit var mockDocumentReference: DocumentReference
    @Mock private lateinit var mockCollectionReference: CollectionReference
    @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
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
    fun getNewUid() {
        `when`(mockDocumentReference.id).thenReturn("1")
        val uid = userRepositoryFirestore.getNewUid()
        assert(uid == "1")
    }

    @Test
    fun documentSnapshotToUser() {
        `when`(mockDocumentSnapshot.getString("name")).thenReturn(user.name)
        `when`(mockDocumentSnapshot.getString("email")).thenReturn(user.email)
        `when`(mockDocumentSnapshot.getString("uid")).thenReturn(user.uid)
        `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(user.dateOfJoining)
        `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(user.rating)

        val userTest = userRepositoryFirestore.documentSnapshotToUser(mockDocumentSnapshot)

        assert(userTest.name == user.name)
        assert(userTest.email == user.email)
        assert(userTest.uid == user.uid)
        assert(userTest.dateOfJoining == user.dateOfJoining)
        assert(userTest.rating == user.rating)

        // empty user:
        `when`(mockDocumentSnapshot.getString("name")).thenReturn(null)
        `when`(mockDocumentSnapshot.getString("email")).thenReturn(null)
        `when`(mockDocumentSnapshot.getString("uid")).thenReturn(null)
        `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(null)
        `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(null)

        val userTestEmpty = userRepositoryFirestore.documentSnapshotToUser(mockDocumentSnapshot)

        assert(userTestEmpty.name == "")
        assert(userTestEmpty.email == "")
        assert(userTestEmpty.uid == "")
        assert(userTestEmpty.dateOfJoining == Timestamp.now())
        assert(userTestEmpty.rating == 0.0)
    }

    @Test
    fun updateUser_shouldCallFirestoreCollection() {
        userRepositoryFirestore.updateUser(user, {}, {})
        verify(mockCollectionReference, timeout(1000)).document(user.uid)
    }

    @Test
    fun deleteUserById_shouldCallFirestoreCollection() {
        userRepositoryFirestore.deleteUserById(user.uid, {}, {})
        verify(mockCollectionReference, timeout(1000)).document(user.uid)
    }

    @Test
    fun getUserById_shouldCallFirestoreCollection() {
        `when`(mockDocumentSnapshot.getString("name")).thenReturn(user.name)
        `when`(mockDocumentSnapshot.getString("email")).thenReturn(user.email)
        `when`(mockDocumentSnapshot.getString("uid")).thenReturn(user.uid)
        `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(user.dateOfJoining)
        `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(user.rating)

        var userTest:User? = null
        userRepositoryFirestore.getUserById(user.uid, {usr -> userTest=usr }, {})
        verify(mockCollectionReference, timeout(1000)).document(user.uid)
        assert(userTest != null)
        assert(userTest!!.name == user.name)
        assert(userTest!!.email == user.email)
        assert(userTest!!.uid == user.uid)
        assert(userTest!!.dateOfJoining == user.dateOfJoining)
        assert(userTest!!.rating == user.rating)
    }

    @Test
    fun addUser_shouldCallFirestoreCollection() {
        userRepositoryFirestore.addUser(user, {}, {})
        verify(mockCollectionReference, timeout(1000)).document(user.uid)
    }

    @Test
    fun init() {
        userRepositoryFirestore.init {}
    }





}