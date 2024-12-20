package com.github.onlynotesswent.model.folder

import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.Friends
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepositoryFirestore.SavedDocumentType.FOLDER
import com.github.onlynotesswent.model.user.UserViewModel
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FolderViewModelTest {

  @Mock private lateinit var mockFolderRepository: FolderRepository
  private lateinit var folderViewModel: FolderViewModel
  @Mock private lateinit var mockNoteRepository: NoteRepository
  private lateinit var noteViewModel: NoteViewModel
  // @Mock private lateinit var mockUserRepositoryFirestore: UserRepository
  @Mock private lateinit var mockUserViewModel: UserViewModel

  private val testFolder =
      Folder(
          id = "1",
          name = "name",
          userId = "1",
          parentFolderId = "pid",
          lastModified = Timestamp.now())

  private val testFolderFriend =
      Folder(
          id = "2",
          name = "name",
          userId = "1",
          parentFolderId = "pid",
          lastModified = Timestamp.now(),
          visibility = Visibility.FRIENDS)

  private val user =
      User(
          firstName = "User",
          lastName = "Name",
          userName = "username",
          email = "example@gmail.com",
          uid = "1",
          dateOfJoining = Timestamp.now(),
          rating = 0.0,
          friends = Friends(following = listOf("3"), followers = listOf("3")))

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    folderViewModel = FolderViewModel(mockFolderRepository)
    noteViewModel = NoteViewModel(mockNoteRepository)
  }

  @Test
  fun getNewFolderId() {
    `when`(mockFolderRepository.getNewFolderId()).thenReturn("1")
    assertThat(folderViewModel.getNewFolderId(), `is`("1"))
  }

  @Test
  fun initCallsRepository() {
    verify(mockFolderRepository).init(any())
  }

  @Test
  fun getFoldersFromCallsRepository() = runTest {
    `when`(mockFolderRepository.getFoldersFromUserId(any(), any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getFoldersFromUserId(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.userFolders.value, listOf(testFolder))
  }

  @Test
  fun getFolderFromFollowingListCallsRepository() {
    `when`(mockFolderRepository.getFoldersFromFollowingList(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolderFriend))
    }
    folderViewModel.getFoldersFromFollowingList(listOf("1"), { assert(true) })
    assertEquals(folderViewModel.friendsFolders.value, listOf(testFolderFriend))
  }

  @Test
  fun getRootFoldersFromCallsNoteRepository() = runTest {
    `when`(mockFolderRepository.getRootNoteFoldersFromUserId(any(), any(), any(), any()))
        .thenAnswer {
          val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
          onSuccess(listOf(testFolder))
        }
    folderViewModel.getRootFoldersFromUserId(testFolder.id, false, { assert(true) })
    assertEquals(folderViewModel.userRootFolders.value, listOf(testFolder))
  }

  @Test
  fun getRootFoldersFromCallsDeckRepository() = runTest {
    `when`(mockFolderRepository.getRootDeckFoldersFromUserId(any(), any(), any(), any()))
        .thenAnswer {
          val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
          onSuccess(listOf(testFolder))
        }
    folderViewModel.getRootFoldersFromUserId(testFolder.id, true, { assert(true) })
    assertEquals(folderViewModel.userRootFolders.value, listOf(testFolder))
  }

  @Test
  fun getRootNoteFoldersFromCallsRepository() = runTest {
    `when`(mockFolderRepository.getRootNoteFoldersFromUserId(any(), any(), any(), any()))
        .thenAnswer {
          val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
          onSuccess(listOf(testFolder))
        }
    folderViewModel.getRootNoteFoldersFromUserId(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.userRootFolders.value, listOf(testFolder))
  }

  @Test
  fun getRootDeckFoldersFromCallsRepository() = runTest {
    `when`(mockFolderRepository.getRootDeckFoldersFromUserId(any(), any(), any(), any()))
        .thenAnswer {
          val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
          onSuccess(listOf(testFolder))
        }
    folderViewModel.getRootDeckFoldersFromUserId(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.userRootFolders.value, listOf(testFolder))
  }

  @Test
  fun getDeckFoldersByNameCallsRepository() = runTest {
    `when`(mockFolderRepository.getDeckFoldersByName(any(), any(), any(), any(), any(), any()))
        .thenAnswer {
          val onSuccess: (List<Folder>) -> Unit = it.getArgument(3)
          onSuccess(listOf(testFolder))
        }
    var deckFolders: List<Folder> = listOf()
    folderViewModel.getDeckFoldersByName(
        testFolder.name,
        testFolder.userId,
        { assert(false) },
        { deckFolders = it },
        { assert(false) })
    assertEquals(deckFolders, listOf(testFolder))
  }

  @Test
  fun getFolderByIdCallsRepository() = runTest {
    `when`(mockFolderRepository.getFolderById(any(), any(), any(), any())).thenAnswer {
      val onSuccess: (Folder) -> Unit = it.getArgument(1)
      onSuccess(testFolder)
    }
    folderViewModel.getFolderById(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.selectedFolder.value, testFolder)
  }

  @Test
  fun addFolderCallsRepository() = runTest {
    `when`(mockFolderRepository.addFolder(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.addFolder(testFolder, { onSuccessCalled = true }, isDeckView = false)
    assert(onSuccessCalled)
  }

  @Test
  fun updateFolderCallsRepository() = runTest {
    `when`(mockFolderRepository.updateFolder(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.updateFolder(testFolder, { onSuccessCalled = true }, isDeckView = false)
    assert(onSuccessCalled)
  }

  @Test
  fun deleteFolderByIdCallsRepository() = runTest {
    `when`(mockFolderRepository.deleteFolderById(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.deleteFolderById(
        testFolder.id, testFolder.userId, { onSuccessCalled = true }, isDeckView = false)
    assert(onSuccessCalled)
  }

  @Test
  fun deleteFoldersFromUidCallsRepository() = runTest {
    `when`(mockFolderRepository.deleteAllFoldersFromUserId(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.deleteAllFoldersFromUserId(testFolder.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun getSubFoldersOfCallsRepository() = runTest {
    `when`(mockFolderRepository.getSubFoldersOf(any(), anyOrNull(), any(), any(), any()))
        .thenAnswer {
          val onSuccess: (List<Folder>) -> Unit = it.getArgument(2)
          onSuccess(listOf(testFolder))
        }
    folderViewModel.getSubFoldersOf(testFolder.parentFolderId!!, null, { assert(true) })
    assertEquals(folderViewModel.folderSubFolders.value, listOf(testFolder))
  }

  @Test
  fun getPublicFoldersCallsRepository() {
    `when`(mockFolderRepository.getPublicFolders(any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(0)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getPublicFolders { assert(true) }
    assertEquals(folderViewModel.publicFolders.value, listOf(testFolder))
  }

  @Test
  fun deleteFolderContentsCallsRepository() = runTest {
    folderViewModel.deleteFolderContents(testFolder, noteViewModel)
    verify(mockFolderRepository)
        .deleteFolderContents(eq(testFolder), eq(noteViewModel), any(), any(), any())
  }

  @Test
  fun updateFolderUpdatesStatesWhenSuccess() = runTest {
    `when`(mockFolderRepository.updateFolder(eq(testFolder), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    folderViewModel.updateFolder(testFolder, isDeckView = false)

    verify(mockFolderRepository).updateFolder(eq(testFolder), any(), any(), any())
    verify(mockFolderRepository).getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())
    verify(mockFolderRepository).getSubFoldersOf(eq("pid"), anyOrNull(), any(), any(), any())
  }

  @Test
  fun addCurrentUserSavedFolderCallsRepositoryIfEmpty() = runTest {
    val testList = listOf(testFolder)

    `when`(mockUserViewModel.setSavedDocumentIdsOfType(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(2)
      onSuccess()
    }

    `when`(mockUserViewModel.getSavedDocumentIdsOfType(any(), any(), any())).thenAnswer { invocation
      ->
      folderViewModel.setCurrentUserSavedFolders(mockUserViewModel, testList)
    }

    folderViewModel.addCurrentUserSavedFolder(testFolderFriend, mockUserViewModel)

    verify(mockUserViewModel).getSavedDocumentIdsOfType(any(), any(), any())
  }

  @Test
  fun addCurrentUserSavedFolderCallsRepositoryIfNonEmpty() = runTest {
    val testList = listOf(testFolder)
    `when`(mockUserViewModel.setSavedDocumentIdsOfType(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(2)
      onSuccess()
    }

    var onSuccesCalled = false

    folderViewModel.setCurrentUserSavedFolders(
        mockUserViewModel,
        testList,
        {
          folderViewModel.addCurrentUserSavedFolder(
              testFolderFriend,
              mockUserViewModel,
              {
                assert(folderViewModel.userSavedFolders.value == testList + testFolderFriend)
                onSuccesCalled = true
              },
              { fail() })
        })

    assert(onSuccesCalled)
  }

  @Test
  fun deleteCurrentUserSavedFolderCallsRepositoryIfEmpty() = runTest {
    val testList = listOf(testFolder)

    `when`(mockUserViewModel.setSavedDocumentIdsOfType(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(2)
      onSuccess()
    }

    `when`(mockUserViewModel.getSavedDocumentIdsOfType(any(), any(), any())).thenAnswer { invocation
      ->
      folderViewModel.setCurrentUserSavedFolders(mockUserViewModel, testList)
    }

    folderViewModel.deleteCurrentUserSavedFolder(testFolder.id, mockUserViewModel)

    verify(mockUserViewModel).getSavedDocumentIdsOfType(any(), any(), any())
  }

  @Test
  fun deleteCurrentUserSavedFolderCallsRepositoryIfNonEmpty() = runTest {
    val testList = listOf(testFolder)
    `when`(mockUserViewModel.setSavedDocumentIdsOfType(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(2)
      onSuccess()
    }

    var onSuccesCalled = false

    folderViewModel.setCurrentUserSavedFolders(
        mockUserViewModel,
        testList,
        {
          folderViewModel.deleteCurrentUserSavedFolder(
              testFolder.id,
              mockUserViewModel,
              {
                assert(folderViewModel.userSavedFolders.value == emptyList<Folder>())
                onSuccesCalled = true
              },
              { fail() })
        })

    assert(onSuccesCalled)
  }

  @Test
  fun getCurrentUserSavedFoldersCallsRepositoryOnSuccess() = runTest {
    val documentIds = listOf("1", "2")
    val savedFolders = listOf(testFolder, testFolderFriend)
    val nonSaveableFoldersIds = listOf<String>("test")

    `when`(mockUserViewModel.getSavedDocumentIdsOfType(any(), any(), any())).thenAnswer {
      val onSuccess: (List<String>) -> Unit = it.getArgument(1)
      onSuccess(documentIds)
    }

    `when`(mockFolderRepository.getSavedFoldersByIds(any(), any(), any(), any(), any()))
        .thenAnswer {
          val onSuccess: (List<Folder>, List<String>) -> Unit = it.getArgument(2)
          onSuccess(savedFolders, nonSaveableFoldersIds)
        }

    `when`(mockUserViewModel.currentUser).thenReturn(MutableStateFlow(user))

    `when`(mockUserViewModel.deleteSavedDocumentIdOfType(eq("test"), eq(FOLDER), any(), any()))
        .thenAnswer {
          val onSuccess: () -> Unit = it.getArgument(2)
          onSuccess()
        }

    var onSuccessCalled = false
    folderViewModel.getCurrentUserSavedFolders(mockUserViewModel, { onSuccessCalled = true })
    assert(onSuccessCalled)
    assertEquals(folderViewModel.userSavedFolders.value, savedFolders)
  }

  @Test
  fun getCurrentUserSavedFoldersCallsRepositoryOnFailure() = runTest {
    val exception = Exception("Failed to retrieve saved folders")

    `when`(mockUserViewModel.getSavedDocumentIdsOfType(any(), any(), any())).thenAnswer {
      val onFailure: (Exception) -> Unit = it.getArgument(2)
      onFailure(exception)
    }

    var onFailureCalled = false
    folderViewModel.getCurrentUserSavedFolders(
        mockUserViewModel, onFailure = { onFailureCalled = true })
    assert(onFailureCalled)
  }
}
