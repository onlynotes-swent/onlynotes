package com.github.onlynotesswent.model.users

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
  @Mock private lateinit var mockResolveTask: Task<Void>
  @Mock private lateinit var mockWhereEqualTo: Query
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
    `when`(mockDocumentSnapshot.getString("firstName")).thenReturn(user.firstName)
    `when`(mockDocumentSnapshot.getString("lastName")).thenReturn(user.lastName)
    `when`(mockDocumentSnapshot.getString("userName")).thenReturn(user.userName)
    `when`(mockDocumentSnapshot.getString("email")).thenReturn(user.email)
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn(user.uid)
    `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(user.dateOfJoining)
    `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(user.rating)

    val userTest = userRepositoryFirestore.documentSnapshotToUser(mockDocumentSnapshot)

    assertEquals(user.firstName, userTest.firstName)
    assertEquals(user.lastName, userTest.lastName)
    assertEquals(user.userName, userTest.userName)
    assertEquals(user.email, userTest.email)
    assertEquals(user.uid, userTest.uid)
    assertEquals(user.dateOfJoining, userTest.dateOfJoining)
    assertEquals(user.rating, userTest.rating)

    // Test with empty user
    `when`(mockDocumentSnapshot.getString("firstName")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("lastName")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("userName")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("email")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn(null)
    `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(null)
    `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(null)

    val userTestEmpty = userRepositoryFirestore.documentSnapshotToUser(mockDocumentSnapshot)

    assertEquals("", userTestEmpty.firstName)
    assertEquals("", userTestEmpty.lastName)
    assertEquals("", userTestEmpty.userName)
    assertEquals("", userTestEmpty.email)
    assertEquals("", userTestEmpty.uid)
    assertNotNull(userTestEmpty.dateOfJoining)
    assertEquals(0.0, userTestEmpty.rating)
  }

  @Test
  fun `updateUser should call Firestore collection`() {
    `when`(mockDocumentReference.set(any())).thenReturn(mockResolveTask)
    `when`(mockDocumentReference.delete()).thenReturn(mockResolveTask)
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
      val listener =
          invocation.arguments[0]
              as com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockDocumentTask
    }
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockDocumentSnapshot.getString("firstName")).thenReturn(user.firstName)
    `when`(mockDocumentSnapshot.getString("lastName")).thenReturn(user.lastName)
    `when`(mockDocumentSnapshot.getString("userName")).thenReturn(user.userName)
    `when`(mockDocumentSnapshot.getString("email")).thenReturn(user.email)
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn(user.uid)
    `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(user.dateOfJoining)
    `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(user.rating)

    var userTest: User? = null
    userRepositoryFirestore.getUserById(
        user.uid, { usr -> userTest = usr }, { assert(false) }, { assert(false) })
    verify(mockCollectionReference, timeout(1000)).document(user.uid)
    assertNotNull(userTest)
    assertEquals(user.firstName, userTest!!.firstName)
    assertEquals(user.lastName, userTest!!.lastName)
    assertEquals(user.userName, userTest!!.userName)
    assertEquals(user.email, userTest!!.email)
    assertEquals(user.uid, userTest!!.uid)
    assertEquals(user.dateOfJoining, userTest!!.dateOfJoining)
    assertEquals(user.rating, userTest!!.rating)
  }

  @Test
  fun `getUserById handles empty document`() {
    `when`(mockCollectionReference.document("2")).thenReturn(mockDocumentReference)
    val mockEmptyDocumentTask = mock(Task::class.java) as Task<DocumentSnapshot>
    `when`(mockDocumentReference.get()).thenReturn(mockEmptyDocumentTask)
    `when`(mockEmptyDocumentTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0]
              as com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockEmptyDocumentTask
    }
    `when`(mockDocumentSnapshot.exists()).thenReturn(false)

    userRepositoryFirestore.getUserById("2", { assert(false) }, { assert(true) }, { assert(false) })
    verify(mockCollectionReference, timeout(1000)).document("2")
  }

  @Test
  fun `getUserById handles failure`() {
    `when`(mockCollectionReference.document("2")).thenReturn(mockDocumentReference)
    val mockDocumentTask = mock(Task::class.java) as Task<DocumentSnapshot>
    `when`(mockDocumentReference.get()).thenReturn(mockDocumentTask)
    `when`(mockDocumentTask.addOnSuccessListener(any())).thenReturn(mockDocumentTask)
    `when`(mockDocumentTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      listener.onFailure(Exception("Document retrieval failed"))
      mockDocumentTask
    }

    var failureCalled = false
    userRepositoryFirestore.getUserById("2", { assert(false) }, { assert(false) }) { exception ->
      failureCalled = true
      assertEquals("Document retrieval failed", exception.message)
    }

    verify(mockCollectionReference, timeout(1000)).document("2")
    assert(failureCalled)
  }

  @Test
  fun `getUserByEmail should call Firestore collection`() {
    val mockQueryTask = mock<Task<QuerySnapshot>>()
    val mockQuery = mock<Query>()
    val mockQuerySnapshot = mock<QuerySnapshot>()

    `when`(mockCollectionReference.whereEqualTo("email", user.email)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)

    // Mock the behavior of the QuerySnapshot task
    `when`(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>
      // Simulate a QuerySnapshot result
      `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }

    `when`(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Mock the behavior of the DocumentSnapshot
    `when`(mockDocumentSnapshot.getString("firstName")).thenReturn(user.firstName)
    `when`(mockDocumentSnapshot.getString("lastName")).thenReturn(user.lastName)
    `when`(mockDocumentSnapshot.getString("userName")).thenReturn(user.userName)
    `when`(mockDocumentSnapshot.getString("email")).thenReturn(user.email)
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn(user.uid)
    `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(user.dateOfJoining)
    `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(user.rating)

    var userTest: User? = null
    userRepositoryFirestore.getUserByEmail(
        user.email, { usr -> userTest = usr }, { assert(false) }, { assert(false) })

    // Verify that Firestore collection was called correctly
    verify(mockCollectionReference, timeout(1000)).whereEqualTo("email", user.email)

    // Assertions to verify that the correct user was returned
    assertNotNull(userTest)
    assertEquals(user.firstName, userTest!!.firstName)
    assertEquals(user.lastName, userTest!!.lastName)
    assertEquals(user.userName, userTest!!.userName)
    assertEquals(user.email, userTest!!.email)
    assertEquals(user.uid, userTest!!.uid)
    assertEquals(user.dateOfJoining, userTest!!.dateOfJoining)
    assertEquals(user.rating, userTest!!.rating)
  }

  @Test
  fun `getUserByEmail handles empty document`() {

    val mockEmptyQueryTask = mock(Task::class.java) as Task<QuerySnapshot>
    val mockQuerySnapshot = mock(QuerySnapshot::class.java)

    `when`(mockCollectionReference.whereEqualTo("email", user.email)).thenReturn(mockWhereEqualTo)
    `when`(mockWhereEqualTo.get()).thenReturn(mockEmptyQueryTask)
    `when`(mockEmptyQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>
      listener.onSuccess(mockQuerySnapshot)
      mockEmptyQueryTask
    }
    `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

    userRepositoryFirestore.getUserByEmail(
        user.email, { assert(false) }, { assert(true) }, { assert(false) })
    verify(mockCollectionReference, timeout(1000)).whereEqualTo("email", user.email)
  }

  @Test
  fun `getUserByEmail handles failure`() {
    val mockQueryTask = mock(Task::class.java) as Task<QuerySnapshot>
    `when`(mockCollectionReference.whereEqualTo("email", user.email)).thenReturn(mockWhereEqualTo)
    `when`(mockWhereEqualTo.get()).thenReturn(mockQueryTask)
    `when`(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask)
    `when`(mockQueryTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as com.google.android.gms.tasks.OnFailureListener
      listener.onFailure(Exception("Query failed"))
      mockQueryTask
    }

    var failureCalled = false
    userRepositoryFirestore.getUserByEmail(user.email, { assert(false) }, { assert(false) }) {
        exception ->
      failureCalled = true
      assertEquals("Query failed", exception.message)
    }

    verify(mockCollectionReference, timeout(1000)).whereEqualTo("email", user.email)
    assert(failureCalled)
  }

  @Test
  fun `addUser should call Firestore collection`() {
    val mockQueryTask = mock<Task<QuerySnapshot>>()
    val mockQuery = mock<Query>()
    val mockQuerySnapshot = mock<QuerySnapshot>()

    `when`(mockCollectionReference.whereEqualTo("userName", user.userName)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)

    // Mock the behavior of the QuerySnapshot task
    `when`(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>
      // Simulate a result being passed to the listener
      `when`(mockQuerySnapshot.isEmpty).thenReturn(true)
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

    // Call addUser method
    userRepositoryFirestore.addUser(user, {}, {})

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).document(user.uid)
  }

  @Test
  fun `addUser fails when adding already existing username`() {
    val mockQueryTask = mock<Task<QuerySnapshot>>()
    val mockQuery = mock<Query>()
    val mockQuerySnapshot = mock<QuerySnapshot>()

    `when`(mockCollectionReference.whereEqualTo("userName", user.userName)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)

    // Mock the behavior of the QuerySnapshot task
    `when`(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>
      // Simulate a result being passed to the listener
      `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }

    `when`(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Call addUser method
    var exceptionThrown = false
    var exception: Exception? = null
    userRepositoryFirestore.addUser(
        user,
        {},
        { e ->
          exception = e
          exceptionThrown = true
        })
    assert(exceptionThrown)
    assert(exception is UserRepositoryFirestore.UsernameTakenException)

    // Verify if Firestore collection was called
    verify(mockCollectionReference, timeout(1000)).whereEqualTo("userName", user.userName)
  }

  @Test
  fun `init should call FirebaseAuth currentUser`() {
    val mockAuth = mock(FirebaseAuth::class.java)
    val mockUser = mock(FirebaseUser::class.java)
    `when`(mockAuth.currentUser).thenReturn(mockUser)

    userRepositoryFirestore.init(mockAuth) {}
    verify(mockAuth, timeout(1000)).currentUser
  }

  @Test
  fun `getUsers calls Firestore collection`() {
    val mockQueryTask = mock<Task<QuerySnapshot>>()
    val mockQuerySnapshot = mock<QuerySnapshot>()
    `when`(mockCollectionReference.get()).thenReturn(mockQueryTask as Task<QuerySnapshot>)
    `when`(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }

    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.getString("firstName")).thenReturn(user.firstName)
    `when`(mockDocumentSnapshot.getString("lastName")).thenReturn(user.lastName)
    `when`(mockDocumentSnapshot.getString("userName")).thenReturn(user.userName)
    `when`(mockDocumentSnapshot.getString("email")).thenReturn(user.email)
    `when`(mockDocumentSnapshot.getString("uid")).thenReturn(user.uid)
    `when`(mockDocumentSnapshot.getTimestamp("dateOfJoining")).thenReturn(user.dateOfJoining)
    `when`(mockDocumentSnapshot.getDouble("rating")).thenReturn(user.rating)

    var users: List<User>? = null
    userRepositoryFirestore.getUsers({ users = it }, { assert(false) })
    verify(mockCollectionReference, timeout(1000)).get()
    assertNotNull(users)
    assertEquals(1, users!!.size)
    assertEquals(user, users!![0])
  }
}
