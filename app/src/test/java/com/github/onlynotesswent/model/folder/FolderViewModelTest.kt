package com.github.onlynotesswent.model.folder

import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
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
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FolderViewModelTest {

  @Mock private lateinit var mockFolderRepository: FolderRepository
  private lateinit var folderViewModel: FolderViewModel
  @Mock private lateinit var mockNoteRepository: NoteRepository
  private lateinit var noteViewModel: NoteViewModel

  private val testFolder =
      Folder(
          id = "1",
          name = "name",
          userId = "1",
          parentFolderId = "pid",
          lastModified = Timestamp.now())

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
    `when`(mockFolderRepository.getFoldersFromUid(any(), any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getFoldersFromUid(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.userFolders.value, listOf(testFolder))
  }

  @Test
  fun getRootFoldersFromCallsRepository() = runTest {
    `when`(mockFolderRepository.getRootFoldersFromUid(any(), any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getRootFoldersFromUid(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.userRootFolders.value, listOf(testFolder))
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
    folderViewModel.addFolder(testFolder, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun updateFolderCallsRepository() = runTest {
    `when`(mockFolderRepository.updateFolder(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.updateFolder(testFolder, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun deleteFolderByIdCallsRepository() = runTest {
    `when`(mockFolderRepository.deleteFolderById(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.deleteFolderById(testFolder.id, testFolder.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun deleteFoldersFromUidCallsRepository() = runTest {
    `when`(mockFolderRepository.deleteFoldersFromUid(any(), any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.deleteFoldersFromUid(testFolder.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun getSubFoldersOfCallsRepository() = runTest {
    `when`(mockFolderRepository.getSubFoldersOf(any(), any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getSubFoldersOf(testFolder.parentFolderId!!, { assert(true) })
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
    folderViewModel.updateFolder(testFolder)

    verify(mockFolderRepository).updateFolder(eq(testFolder), any(), any(), any())
    verify(mockFolderRepository).getRootFoldersFromUid(eq("1"), any(), any(), any())
    verify(mockFolderRepository).getSubFoldersOf(eq("pid"), any(), any(), any())
  }
}
