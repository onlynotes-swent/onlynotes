package com.github.onlynotesswent.model.folder

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.github.onlynotesswent.model.cache.CacheDatabase
import com.github.onlynotesswent.model.cache.FolderDao
import com.github.onlynotesswent.model.cache.NoteDao
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class FolderRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockFolderDao: FolderDao
  @Mock private lateinit var mockNoteDao: NoteDao
  @Mock private lateinit var mockCacheDatabase: CacheDatabase
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockDocumentSnapshot2: DocumentSnapshot
  @Mock private lateinit var mockDocumentSnapshot3: DocumentSnapshot
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQuerySnapshotTask: Task<QuerySnapshot>

  @Mock private lateinit var mockNoteRepository: NoteRepository
  private lateinit var noteViewModel: NoteViewModel

  private lateinit var folderRepositoryFirestore: FolderRepositoryFirestore

  private val testFolder =
      Folder(
          id = "1",
          name = "name",
          userId = "1",
          parentFolderId = null,
          lastModified = Timestamp.now())

  private val testFolderFriend =
      Folder(
          id = "3",
          name = "name",
          userId = "1",
          parentFolderId = null,
          lastModified = Timestamp.now(),
          visibility = Visibility.FRIENDS)

  private val testSubFolder =
      Folder(
          id = "2",
          name = "subFolder",
          userId = "1",
          parentFolderId = "1",
          lastModified = Timestamp.now())

  private val mockContext = mock(Context::class.java)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    val context = ApplicationProvider.getApplicationContext<Context>()
    mockCacheDatabase = Room.inMemoryDatabaseBuilder(context, CacheDatabase::class.java).build()
    mockNoteDao = mockCacheDatabase.noteDao()
    mockFolderDao = mockCacheDatabase.folderDao()

    folderRepositoryFirestore =
        FolderRepositoryFirestore(mockFirestore, mockCacheDatabase, mockContext)
    noteViewModel = NoteViewModel(mockNoteRepository)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    // Simulate Firestore task completion
    `when`(mockQuerySnapshotTask.isSuccessful).thenReturn(true)
    `when`(mockQuerySnapshotTask.result).thenReturn(mockQuerySnapshot)
    `when`(mockQuerySnapshotTask.isComplete).thenReturn(true)
    `when`(mockQuerySnapshotTask.exception).thenReturn(null)

    // Ensure onCompleteListener is triggered
    doAnswer { invocation ->
          val listener = invocation.getArgument<OnCompleteListener<QuerySnapshot>>(0)
          listener.onComplete(mockQuerySnapshotTask)
          mockQuerySnapshotTask
        }
        .`when`(mockQuerySnapshotTask)
        .addOnCompleteListener(any())
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

    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2, mockDocumentSnapshot3))

    `when`(mockDocumentSnapshot.id).thenReturn(testFolder.id)
    `when`(mockDocumentSnapshot.getString("name")).thenReturn(testFolder.name)
    `when`(mockDocumentSnapshot.getString("userId")).thenReturn(testFolder.userId)
    `when`(mockDocumentSnapshot.getString("parentFolderId")).thenReturn(testFolder.parentFolderId)
    `when`(mockDocumentSnapshot.getString("visibility"))
        .thenReturn(testFolder.visibility.toString())
    `when`(mockDocumentSnapshot.getTimestamp("lastModified")).thenReturn(testFolder.lastModified)

    `when`(mockDocumentSnapshot2.id).thenReturn(testSubFolder.id)
    `when`(mockDocumentSnapshot2.getString("name")).thenReturn(testSubFolder.name)
    `when`(mockDocumentSnapshot2.getString("userId")).thenReturn(testSubFolder.userId)
    `when`(mockDocumentSnapshot2.getString("parentFolderId"))
        .thenReturn(testSubFolder.parentFolderId)
    `when`(mockDocumentSnapshot2.getString("visibility"))
        .thenReturn(testSubFolder.visibility.toString())
    `when`(mockDocumentSnapshot2.getTimestamp("lastModified"))
        .thenReturn(testSubFolder.lastModified)

    `when`(mockDocumentSnapshot3.id).thenReturn(testFolderFriend.id)
    `when`(mockDocumentSnapshot3.getString("name")).thenReturn(testFolderFriend.name)
    `when`(mockDocumentSnapshot3.getString("userId")).thenReturn(testFolderFriend.userId)
    `when`(mockDocumentSnapshot3.getString("parentFolderId"))
        .thenReturn(testFolderFriend.parentFolderId)
    `when`(mockDocumentSnapshot3.getString("visibility"))
        .thenReturn(testFolderFriend.visibility.toString())
    `when`(mockDocumentSnapshot3.getTimestamp("lastModified"))
        .thenReturn(testFolderFriend.lastModified)
  }

  fun mockHasInternetConnection(hasInternetConnection: Boolean) {
    val mockConnectivityManager = mock(ConnectivityManager::class.java)
    `when`(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
        .thenReturn(mockConnectivityManager)
    val mockNetwork = mock(Network::class.java)
    `when`(mockConnectivityManager.activeNetwork).thenReturn(mockNetwork)
    val mockCapabilities = mock(NetworkCapabilities::class.java)
    `when`(mockConnectivityManager.getNetworkCapabilities(mockNetwork)).thenReturn(mockCapabilities)
    `when`(mockCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        .thenReturn(hasInternetConnection)
    `when`(mockCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        .thenReturn(hasInternetConnection)
  }

  private fun compareFolders(testFolder: Folder?, expectedFolder: Folder) {
    assert(testFolder?.id == expectedFolder.id)
    assert(testFolder?.name == expectedFolder.name)
    assert(testFolder?.userId == expectedFolder.userId)
    assert(testFolder?.parentFolderId == expectedFolder.parentFolderId)
    assert(testFolder?.visibility == expectedFolder.visibility)
    assert(testFolder?.lastModified == expectedFolder.lastModified)
  }

  @Test
  fun getNewFolderId() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val folderId = folderRepositoryFirestore.getNewFolderId()
    assert(folderId == "1")
  }

  @Test
  fun documentSnapshotToFolderConvertsSnapshotToFolder() {
    val convertedFolder = folderRepositoryFirestore.documentSnapshotToFolder(mockDocumentSnapshot)
    assertNotNull(convertedFolder)
    compareFolders(convertedFolder!!, testFolder)
  }

  @Test
  fun getFoldersFromUserId_callsDocuments() = runTest {
    mockHasInternetConnection(true)
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))
    var receivedFolders: List<Folder>? = null
    folderRepositoryFirestore.getFoldersFromUserId(
        testFolder.userId,
        onSuccess = { receivedFolders = it },
        onFailure = { assert(false) },
        false)
    assertNotNull(receivedFolders)

    verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun getRootNoteFoldersFromUserId_callsDocuments() = runTest {
    mockHasInternetConnection(true)

    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))
    var receivedFolders: List<Folder>? = null
    folderRepositoryFirestore.getRootNoteFoldersFromUserId(
        testFolder.userId,
        onSuccess = { receivedFolders = it },
        onFailure = { assert(false) },
        false)
    assertNotNull(receivedFolders)

    verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun getRootDeckFoldersFromUserId_callsDocuments() = runTest {
    mockHasInternetConnection(true)
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))
    var receivedFolders: List<Folder>? = null
    folderRepositoryFirestore.getRootDeckFoldersFromUserId(
        testFolder.userId,
        onSuccess = { receivedFolders = it },
        onFailure = { assert(false) },
        false)
    assertNotNull(receivedFolders)

    verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun getDeckFoldersByName_callsDocuments() = runTest {
    mockHasInternetConnection(true)

    // Mock Firestore behavior
    `when`(mockDocumentSnapshot.getBoolean("isDeckFolder")).thenReturn(true)
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))

    // Test logic
    var receivedFolders: List<Folder>? = null
    folderRepositoryFirestore.getDeckFoldersByName(
        testFolder.name,
        testFolder.userId,
        onFolderNotFound = { assert(false) },
        onSuccess = { receivedFolders = it },
        onFailure = { assert(false) },
        useCache = false)

    assertNotNull(receivedFolders)
    verify(mockQuerySnapshot, timeout(100)).documents
  }

  @Test
  fun getDeckFoldersByName_callsOnFolderNotFound() = runTest {
    mockHasInternetConnection(true)
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))
    folderRepositoryFirestore.getDeckFoldersByName(
        testFolder.name,
        testFolder.userId,
        onFolderNotFound = { assert(true) },
        onSuccess = { assert(false) },
        onFailure = { assert(false) },
        false)
  }

  @Test
  fun getDeckFoldersByName_checkCacheReturnsEmptyList() = runTest {
    mockHasInternetConnection(false)
    var onFolderNotFoundCalled = false
    folderRepositoryFirestore.getDeckFoldersByName(
        testFolder.name,
        testFolder.userId,
        onFolderNotFound = { onFolderNotFoundCalled = true },
        onSuccess = { assert(false) },
        onFailure = { assert(false) },
        useCache = true)

    assert(onFolderNotFoundCalled)
  }

  @Test
  fun getFolderById_callsDocument() = runTest {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    folderRepositoryFirestore.getFolderById("1", {}, {}, false)

    shadowOf(Looper.getMainLooper()).idle()

    verify(timeout(100)) { mockDocumentSnapshot.id }
  }

  @Test
  fun addFolder_callsCollection() = runTest {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    folderRepositoryFirestore.addFolder(testFolder, onSuccess = {}, onFailure = {}, false)

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun deleteFolderById_callsDocument() = runTest {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    folderRepositoryFirestore.deleteFolderById("1", onSuccess = {}, onFailure = {}, false)

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
  }

  @Test
  fun deleteAllFoldersFromUserId_callsDocuments() = runTest {
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))

    folderRepositoryFirestore.deleteAllFoldersFromUserId("1", onSuccess = {}, onFailure = {}, false)

    verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun deleteAllFoldersFromUserId_fail() = runTest {
    val errorMessage = "TestError"
    `when`(mockQuerySnapshotTask.isSuccessful).thenReturn(false)
    `when`(mockQuerySnapshotTask.exception).thenReturn(Exception(errorMessage))
    `when`(mockQuerySnapshotTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnCompleteListener<QuerySnapshot>>(0)
      // Simulate a result being passed to the listener
      listener.onComplete(mockQuerySnapshotTask)
      mockQuerySnapshotTask
    }
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))
    var exceptionThrown: Exception? = null
    folderRepositoryFirestore.deleteAllFoldersFromUserId(
        "1", onSuccess = {}, onFailure = { e -> exceptionThrown = e }, false)
    assertNotNull(exceptionThrown)
    assertEquals(errorMessage, exceptionThrown?.message)
  }

  @Test
  fun updateFolder_callsCollection() = runTest {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    folderRepositoryFirestore.updateFolder(testFolder, onSuccess = {}, onFailure = {}, false)

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun getSubFoldersOf_callsCollection() = runTest {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot2))

    folderRepositoryFirestore.getSubFoldersOf("1", null, {}, {}, false)

    shadowOf(Looper.getMainLooper()).idle()

    verify(timeout(100)) { mockDocumentSnapshot2.id }
  }

  @Test
  fun getPublicFolders_callsDocuments() {
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2, mockDocumentSnapshot3))
    var receivedFolders: List<Folder>? = null
    folderRepositoryFirestore.getPublicFolders(
        onSuccess = { receivedFolders = it }, onFailure = { assert(false) })
    assertNotNull(receivedFolders)

    verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun getFoldersFromFollowingList_callsDocuments() {
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2, mockDocumentSnapshot3))
    var receivedFolders: List<Folder>? = null
    folderRepositoryFirestore.getFoldersFromFollowingList(
        listOf("1", "2"), onSuccess = { receivedFolders = it }, onFailure = { assert(false) })
    assertNotNull(receivedFolders)
    assert(receivedFolders!!.size == 1)
    compareFolders(receivedFolders!![0], testFolderFriend)

    verify(timeout(100)) { (mockQuerySnapshot).documents }
  }

  @Test
  fun getFoldersFromFollowingList_callsOnFailure() {
    val errorMessage = "TestError"
    `when`(mockQuerySnapshotTask.isSuccessful).thenReturn(false)
    `when`(mockQuerySnapshotTask.exception).thenReturn(Exception(errorMessage))
    `when`(mockQuerySnapshotTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnCompleteListener<QuerySnapshot>>(0)
      // Simulate a result being passed to the listener
      listener.onComplete(mockQuerySnapshotTask)
      mockQuerySnapshotTask
    }
    var exceptionThrown: Exception? = null
    folderRepositoryFirestore.getFoldersFromFollowingList(
        listOf("1", "2"), onSuccess = {}, onFailure = { e -> exceptionThrown = e })
    assertNotNull(exceptionThrown)
    assertEquals(errorMessage, exceptionThrown?.message)
  }

  @Test
  fun addFolders_callsSuccess() = runTest {
    val mockBatch = mock<com.google.firebase.firestore.WriteBatch>()
    `when`(mockFirestore.batch()).thenReturn(mockBatch)

    val mockTask = mock<Task<Void>>()
    `when`(mockBatch.commit()).thenReturn(mockTask)
    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<Void>
      listener.onComplete(Tasks.forResult(null)) // Simulate successful task completion
      mockTask
    }

    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    val folders = listOf(testFolder, testSubFolder)
    var calledSuccess = false
    folderRepositoryFirestore.addFolders(
        folders, onSuccess = { calledSuccess = true }, onFailure = {}, false)

    shadowOf(Looper.getMainLooper()).idle() // Process pending tasks

    assert(calledSuccess)
  }

  @Test
  fun addFolders_callsFailure() = runTest {
    val mockBatch = mock<com.google.firebase.firestore.WriteBatch>()
    `when`(mockFirestore.batch()).thenReturn(mockBatch)

    val mockTask = mock<Task<Void>>()
    `when`(mockBatch.commit()).thenReturn(mockTask)
    `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnCompleteListener<Void>
      listener.onComplete(Tasks.forException(Exception("Test error"))) // Simulate failed task
      mockTask
    }

    val folders = listOf(testFolder, testSubFolder)
    var calledFailure = false
    folderRepositoryFirestore.addFolders(
        folders, onSuccess = {}, onFailure = { calledFailure = true }, false)

    shadowOf(Looper.getMainLooper()).idle() // Process pending tasks

    assert(calledFailure)
  }

  //  @Test
  //  fun deleteFolderContents_callsDocuments() = runTest {
  //    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))
  //
  //    folderRepositoryFirestore.deleteFolderContents(
  //        testFolder, noteViewModel, onSuccess = {}, onFailure = {}, false)
  //
  //    shadowOf(Looper.getMainLooper()).idle()
  //
  //    verify(mockDocumentReference).delete()
  //  }
  //
  //  @Test
  //  fun deleteFolderContents_fails() = runTest {
  //    val errorMessage = "TestError"
  //    `when`(mockQuerySnapshotTask.isSuccessful).thenReturn(false)
  //    `when`(mockQuerySnapshotTask.exception).thenReturn(Exception(errorMessage))
  //    `when`(mockQuerySnapshotTask.addOnCompleteListener(any())).thenAnswer { invocation ->
  //      val listener = invocation.getArgument<OnCompleteListener<QuerySnapshot>>(0)
  //      // Simulate a result being passed to the listener
  //      listener.onComplete(mockQuerySnapshotTask)
  //      mockQuerySnapshotTask
  //    }
  //    `when`(mockQuerySnapshot.documents)
  //        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))
  //    var exceptionThrown: Exception? = null
  //    folderRepositoryFirestore.deleteFolderContents(
  //        testFolder, noteViewModel, onSuccess = {}, onFailure = { e -> exceptionThrown = e },
  // false)
  //    assertNotNull(exceptionThrown)
  //    assertEquals(errorMessage, exceptionThrown?.message)
  //  }
}
