package com.github.onlynotesswent.model.user

import com.google.android.gms.tasks.Task
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class UserRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuery: Query
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQueryTask: Task<QuerySnapshot>
  @Mock private lateinit var mockResolveTask: Task<Void>
  @Mock private lateinit var mockDocumentTask: Task<DocumentSnapshot>

  private lateinit var userRepositoryFirestore: UserRepositoryFirestore

  private val defaultTimestamp = Timestamp.now()

  private val user =
      User(
          firstName = "User",
          lastName = "Name",
          userName = "username",
          email = "example@gmail.com",
          uid = "1",
          dateOfJoining = defaultTimestamp,
          rating = 0.0,
          friends = Friends(following = listOf("2"), followers = listOf("3")))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    userRepositoryFirestore = UserRepositoryFirestore(mockFirestore)

    // Mock the behavior of the Firestore database
    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.whereEqualTo("userName", user.userName)).thenReturn(mockQuery)
    `when`(mockCollectionReference.whereEqualTo("email", user.email)).thenReturn(mockQuery)
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

    // Mock the behavior of the DocumentSnapshot
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockDocumentSnapshot.getString("firstName")).thenReturn(user.firstName)
    `when`(mockDocumentSnapshot.getString("lastName")).thenReturn(user.lastName)
    `when`(mockDocumentSnapshot.getString("userName")).thenReturn(user.userName)
    `when`(mockDocumentSnapshot.getString("email")).thenReturn(user.email)
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn(user.uid)
    `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(user.dateOfJoining)
    `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(user.rating)
    `when`(mockDocumentSnapshot.get("friends.following")).thenReturn(user.friends.following)
    `when`(mockDocumentSnapshot.get("friends.followers")).thenReturn(user.friends.followers)
  }

  @Test
  fun `getNewUid should return new UID`() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = userRepositoryFirestore.getNewUid()
    assertEquals("1", uid)
  }

  @Test
  fun `documentSnapshotToUser should convert DocumentSnapshot to User`() {
    // Test with valid user
    val userTest = userRepositoryFirestore.documentSnapshotToUser(mockDocumentSnapshot)
    assertEquals(user, userTest)

    // Test with empty user
    `when`(mockDocumentSnapshot.getString("firstName")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("lastName")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("userName")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("email")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn(null)
    `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(null)
    `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(null)
    `when`(mockDocumentSnapshot.get("friends.following")).thenReturn(null)
    `when`(mockDocumentSnapshot.get("friends.followers")).thenReturn(null)

    val userTestEmpty = userRepositoryFirestore.documentSnapshotToUser(mockDocumentSnapshot)

    assertEquals("", userTestEmpty.firstName)
    assertEquals("", userTestEmpty.lastName)
    assertEquals("", userTestEmpty.userName)
    assertEquals("", userTestEmpty.email)
    assertEquals("", userTestEmpty.uid)
    assertNotNull(userTestEmpty.dateOfJoining)
    assertEquals(0.0, userTestEmpty.rating)
    assert(userTestEmpty.friends.following.isEmpty())
    assert(userTestEmpty.friends.followers.isEmpty())
  }

  @Test
  fun `updateUser should call Firestore collection`() {
    `when`(mockQuery.whereNotEqualTo("uid", user.uid)).thenReturn(mockQuery)
    `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

    // Call updateUser method
    var onSuccessCalled = false
    userRepositoryFirestore.updateUser(user, { onSuccessCalled = true }, { assert(false) })

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).document(user.uid)
    assert(onSuccessCalled)
  }

  @Test
  fun `deleteUserById should call Firestore collection`() {
    `when`(mockDocumentReference.delete()).thenReturn(mockResolveTask)

    // Call deleteUserById method
    var onSuccessCalled = false
    userRepositoryFirestore.deleteUserById(user.uid, { onSuccessCalled = true }, { assert(false) })

    // Verify if Firestore collection was called multiple times
    verify(mockCollectionReference, atLeastOnce()).document(any())

    assert(onSuccessCalled)
  }

  @Test
  fun `getUserById should call Firestore collection`() {
    // Call getUserById method
    var userTest: User? = null
    userRepositoryFirestore.getUserById(
        user.uid, { usr -> userTest = usr }, { assert(false) }, { assert(false) })

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).document(user.uid)
    assertNotNull(userTest)
    assertEquals(user, userTest!!)
  }

  @Test
  fun `getUserById handles empty document`() {
    // Mock a document that does not exist
    `when`(mockDocumentSnapshot.exists()).thenReturn(false)

    // Call getUserById method
    var onNotFoundCalled = false
    userRepositoryFirestore.getUserById(
        "2", { assert(false) }, { onNotFoundCalled = true }, { assert(false) })

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).document("2")
    assert(onNotFoundCalled)
  }

  @Test
  fun `getUserById handles failure`() {
    // Mock exception occurring on document retrieval
    `when`(mockDocumentTask.addOnSuccessListener(any())).thenReturn(mockDocumentTask)
    `when`(mockDocumentTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      listener.onFailure(Exception("Document retrieval failed"))
      mockDocumentTask
    }

    // Call getUserById
    var failureCalled = false
    userRepositoryFirestore.getUserById("2", { assert(false) }, { assert(false) }) { exception ->
      failureCalled = true
      assertEquals("Document retrieval failed", exception.message)
    }

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).document("2")
    assert(failureCalled)
  }

  @Test
  fun `getUserByEmail should call Firestore collection`() {
    // Mock a successful QuerySnapshot task
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    // Call getUserByEmail method
    var userTest: User? = null
    userRepositoryFirestore.getUserByEmail(
        user.email, { usr -> userTest = usr }, { assert(false) }, { assert(false) })

    // Verify that Firestore collection was called correctly
    verify(mockCollectionReference, timeout(1000)).whereEqualTo("email", user.email)

    // Assertions to verify that the correct user was returned
    assertNotNull(userTest)
    assertEquals(user, userTest!!)
  }

  @Test
  fun `getUserByEmail handles empty document`() {
    // Mock an empty QuerySnapshot task
    `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

    // Call getUserByEmail method
    var onNotFoundCalled = false
    userRepositoryFirestore.getUserByEmail(
        user.email, { assert(false) }, { onNotFoundCalled = true }, { assert(false) })

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).whereEqualTo("email", user.email)
    assert(onNotFoundCalled)
  }

  @Test
  fun `getUserByEmail handles failure`() {
    // Mock exception occurring on query
    `when`(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask)
    `when`(mockQueryTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      listener.onFailure(Exception("Query failed"))
      mockQueryTask
    }
    // Call getUserByEmail
    var failureCalled = false
    userRepositoryFirestore.getUserByEmail(user.email, { assert(false) }, { assert(false) }) {
        exception ->
      failureCalled = true
      assertEquals("Query failed", exception.message)
    }
    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).whereEqualTo("email", user.email)
    assert(failureCalled)
  }

  @Test
  fun `addUser should call Firestore collection`() {
    // Mock an empty QuerySnapshot task, signifying no duplicate usernames
    `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

    // Call addUser method
    var onSuccessCalled = false
    userRepositoryFirestore.addUser(user, { onSuccessCalled = true }, { assert(false) })

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).document(user.uid)
    assert(onSuccessCalled)
  }

  @Test
  fun `addUser fails when adding already existing username`() {
    // Mock a non-empty QuerySnapshot task, signifying a duplicate username was found
    `when`(mockQuerySnapshot.isEmpty).thenReturn(false)

    // Call addUser method
    var exception: Exception? = null
    userRepositoryFirestore.addUser(user, { assert(false) }, { exception = it })

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).whereEqualTo("userName", user.userName)
    assert(exception is UserRepositoryFirestore.UsernameTakenException)
  }

  @Test
  fun `init should call FirebaseAuth currentUser`() {
    val mockAuth = mock(FirebaseAuth::class.java)
    val mockUser = mock(FirebaseUser::class.java)
    `when`(mockAuth.currentUser).thenReturn(mockUser)

    var onSuccessCalled = false
    userRepositoryFirestore.init(mockAuth) { onSuccessCalled = true }
    verify(mockAuth).currentUser
    assert(onSuccessCalled)

    // Mock failure:
    `when`(mockAuth.currentUser).thenReturn(null)
    userRepositoryFirestore.init(mockAuth) { assert(false) }
  }

  @Test
  fun `getAllUsers calls Firestore collection`() {
    // Mock the behavior of the QuerySnapshot task
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    // Call getAllUsers method
    var users: List<User>? = null
    userRepositoryFirestore.getAllUsers({ users = it }, { assert(false) })
    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).get()
    assertNotNull(users)
    assertEquals(1, users!!.size)
    assertEquals(user, users!![0])
  }

  @Test
  fun `getAllUsers handles failure`() {
    // Mock exception occurring on query
    `when`(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask)
    `when`(mockQueryTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      listener.onFailure(Exception("Query failed"))
      mockQueryTask
    }
    // Call getAllUsers
    var failureCalled = false
    userRepositoryFirestore.getAllUsers({ assert(false) }) { failureCalled = true }
    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).get()
    assert(failureCalled)
  }

  @Test
  fun `addFollowerTo should call Firestore collection`() {
    // Mock the behavior of the DocumentReference update operation
    `when`(mockDocumentReference.update(eq("friends.followers"), any())).thenReturn(mockResolveTask)
    `when`(mockDocumentReference.update(eq("friends.following"), any())).thenReturn(mockResolveTask)

    // Call addFollowerTo method
    var onSuccessCalled = false
    userRepositoryFirestore.addFollowerTo(
        user = user.uid, follower = "4", false, { onSuccessCalled = true }, { assert(false) })

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).document(user.uid)
    assert(onSuccessCalled)
  }

  @Test
  fun `removeFollowerFrom should call Firestore collection`() {
    // Mock the behavior of the DocumentReference update operation
    `when`(mockDocumentReference.update(eq("friends.followers"), any())).thenReturn(mockResolveTask)
    `when`(mockDocumentReference.update(eq("friends.following"), any())).thenReturn(mockResolveTask)

    // Call addFollowerTo method
    var onSuccessCalled = false
    userRepositoryFirestore.removeFollowerFrom(
        user = user.uid, follower = "4", false, { onSuccessCalled = true }, { assert(false) })

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).document(user.uid)
    assert(onSuccessCalled)
  }

  @Test
  fun `getUsersById should call Firestore collection`() {
    // Mock whereIn method and QuerySnapshot task
    `when`(mockCollectionReference.whereIn("uid", listOf(user.uid))).thenReturn(mockQuery)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    // Call getUsersById method
    var users: List<User>? = null
    userRepositoryFirestore.getUsersById(listOf(user.uid), { users = it }, { assert(false) })

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).whereIn("uid", listOf(user.uid))

    // Assertions to verify that the correct user was returned
    assertNotNull(users)
    assertEquals(1, users!!.size)
    assertEquals(user, users!![0])
  }
}
