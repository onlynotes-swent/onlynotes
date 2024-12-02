package com.github.onlynotesswent.model.notification

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuery: Query
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQuerySnapshotTask: Task<QuerySnapshot>
  @Mock private lateinit var mockResolveTask: Task<Void>
  @Mock private lateinit var mockDocumentTask: Task<DocumentSnapshot>

  private lateinit var notificationRepositoryFirestore: NotificationRepositoryFirestore

  private val testNotification =
      Notification(
          id = "1", senderId = "1", receiverId = "2", timestamp = Timestamp.now(), read = false)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    notificationRepositoryFirestore = NotificationRepositoryFirestore(mockFirestore)

    // Mock the behavior of the Firestore database
    `when`(mockFirestore.collection("notifications")).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.whereEqualTo("receiverId", "1")).thenReturn(mockQuery)
    `when`(mockCollectionReference.get()).thenReturn(mockQuerySnapshotTask)

    // Mock the behavior of the QuerySnapshot task
    `when`(mockQuery.get()).thenReturn(mockQuerySnapshotTask)
    `when`(mockQuerySnapshotTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0] as com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>
      // Simulate a result being passed to the listener
      listener.onSuccess(mockQuerySnapshot)
      mockQuerySnapshotTask
    }
    `when`(mockQuerySnapshotTask.addOnFailureListener(any())).thenReturn(mockQuerySnapshotTask)
    `when`(mockQuerySnapshot.documents)
        .thenReturn(
            listOf(
                mockDocumentSnapshot,
            ))

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
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockDocumentTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0]
              as com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockDocumentTask
    }
    `when`(mockDocumentTask.addOnFailureListener(any())).thenReturn(mockDocumentTask)

    // Mock the behavior of the DocumentReference delete operation
    `when`(mockDocumentReference.delete()).thenReturn(mockResolveTask)
    // ------------------------------------------------------------------------------------------

    `when`(mockDocumentSnapshot.getString("id")).thenReturn(testNotification.id)
    `when`(mockDocumentSnapshot.getString("senderId")).thenReturn(testNotification.senderId)
    `when`(mockDocumentSnapshot.getString("receiverId")).thenReturn(testNotification.receiverId)
    `when`(mockDocumentSnapshot.getTimestamp("timestamp")).thenReturn(testNotification.timestamp)
    `when`(mockDocumentSnapshot.getBoolean("read")).thenReturn(testNotification.read)
    `when`(mockDocumentSnapshot.getString("type")).thenReturn(testNotification.type.toString())
  }

  @Test
  fun `getNotificationById should return notification`() {
    var notification: Notification? = null
    notificationRepositoryFirestore.getNotificationById(
        id = "1",
        onSuccess = { notification = it },
        onNotificationNotFound = { assert(false) },
        onFailure = { assert(false) })
    assertNotNull(notification)
    assertEquals(testNotification, notification)
  }

  @Test
  fun `getNotificationByReceiverId should return list of notifications`() {

    val notifications = mutableListOf<Notification>()
    notificationRepositoryFirestore.getNotificationByReceiverId(
        receiverId = "1", onSuccess = { notifications.addAll(it) }, onFailure = { fail() })

    assertEquals(listOf(testNotification), notifications)
  }

  @Test
  fun `addNotification should add notification`() {
    var wasCalled = false
    notificationRepositoryFirestore.addNotification(
        notification = testNotification, onSuccess = { wasCalled = true }, onFailure = { fail() })
    assert(wasCalled)
  }

  @Test
  fun `updateNotification should update notification`() {
    var wasCalled = false
    notificationRepositoryFirestore.updateNotification(
        notification = testNotification, onSuccess = { wasCalled = true }, onFailure = { fail() })
    assert(wasCalled)
  }

  @Test
  fun `deleteNotification should delete notification`() {
    var wasCalled = false
    notificationRepositoryFirestore.deleteNotification(
        id = "1", onSuccess = { wasCalled = true }, onFailure = { fail() })
    assert(wasCalled)
  }
}
