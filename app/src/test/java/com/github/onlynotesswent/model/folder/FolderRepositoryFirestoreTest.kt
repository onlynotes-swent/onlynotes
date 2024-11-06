package com.github.onlynotesswent.model.folder

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
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
class FolderRepositoryFirestoreTest {

    @Mock private lateinit var mockFirestore: FirebaseFirestore
    @Mock private lateinit var mockDocumentReference: DocumentReference
    @Mock private lateinit var mockCollectionReference: CollectionReference
    @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
    @Mock private lateinit var mockDocumentSnapshot2: DocumentSnapshot
    @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
    @Mock private lateinit var mockQuerySnapshotTask: Task<QuerySnapshot>

    private lateinit var folderRepositoryFirestore: FolderRepositoryFirestore

    private val testFolder =
        Folder(
            id = "1",
            name = "name",
            userId = "1",
            parentFolderId = null
        )

    private val testSubFolder =
        Folder(
            id = "2",
            name = "subFolder",
            userId = "1",
            parentFolderId = "1"
        )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        }


        folderRepositoryFirestore = FolderRepositoryFirestore(mockFirestore)

        `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
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

        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))

        `when`(mockDocumentSnapshot.id).thenReturn(testFolder.id)
        `when`(mockDocumentSnapshot.getString("name")).thenReturn(testFolder.name)
        `when`(mockDocumentSnapshot.getString("userId")).thenReturn(testFolder.userId)
        `when`(mockDocumentSnapshot.getString("parentFolderId")).thenReturn(testFolder.parentFolderId)

        `when`(mockDocumentSnapshot2.id).thenReturn(testSubFolder.id)
        `when`(mockDocumentSnapshot2.getString("name")).thenReturn(testSubFolder.name)
        `when`(mockDocumentSnapshot2.getString("userId")).thenReturn(testSubFolder.userId)
        `when`(mockDocumentSnapshot2.getString("parentFolderId")).thenReturn(testSubFolder.parentFolderId)

    }

    private fun compareFolders(folder: Folder, folder2: Folder) {
        assert(folder.id == folder2.id)
        assert(folder.name == folder2.name)
        assert(folder.userId == folder2.userId)
        assert(folder.parentFolderId == folder2.parentFolderId)
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
    fun getFoldersFrom_callsDocuments() {
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))
        var receivedFolders: List<Folder>? = null
        folderRepositoryFirestore.getFoldersFrom(testFolder.userId, onSuccess = { receivedFolders = it }, onFailure = {assert(false)})
        assertNotNull(receivedFolders)

        verify(timeout(100)) { (mockQuerySnapshot).documents }
    }

    @Test
    fun getRootFoldersFrom_callsDocuments() {
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot2))
        var receivedFolders: List<Folder>? = null
        folderRepositoryFirestore.getRootFoldersFrom(testFolder.userId, onSuccess = { receivedFolders = it }, onFailure = {assert(false)})
        assertNotNull(receivedFolders)

        verify(timeout(100)) { (mockQuerySnapshot).documents }
    }

    @Test
    fun getFolderById_callsDocument() {
        `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

        folderRepositoryFirestore.getFolderById("1", {}, {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(timeout(100)) { mockDocumentSnapshot.id }
    }

    @Test
    fun addFolder_callsCollection() {
        `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

        folderRepositoryFirestore.addFolder(testFolder, onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(mockDocumentReference).set(any())
    }

    @Test
    fun deleteFolderById_callsDocument() {
        `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

        folderRepositoryFirestore.deleteFolderById("1", onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(mockDocumentReference).delete()
    }

    @Test
    fun updateFolder_callsCollection() {
        `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

        folderRepositoryFirestore.updateFolder(testFolder, onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(mockDocumentReference).set(any())
    }

    @Test
    fun getSubFoldersOf_callsCollection() {
        `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot2))

        folderRepositoryFirestore.getSubFoldersOf("1", {}, {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(timeout(100)) { mockDocumentSnapshot2.id }
    }
}