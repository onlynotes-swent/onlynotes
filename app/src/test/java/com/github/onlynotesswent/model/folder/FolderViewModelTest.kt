package com.github.onlynotesswent.model.folder

import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import junit.framework.TestCase.assertEquals
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

  private val testFolder = Folder(id = "1", name = "name", userId = "1", parentFolderId = "pid")

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
  fun getFoldersFromCallsRepository() {
    `when`(mockFolderRepository.getFoldersFromUserId(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getFoldersFromUserId(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.userFolders.value, listOf(testFolder))
  }

  @Test
  fun getRootFoldersFromCallsNoteRepository() {
    `when`(mockFolderRepository.getRootNoteFoldersFromUserId(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getRootFoldersFromUserId(testFolder.id, false, { assert(true) })
    assertEquals(folderViewModel.userRootFolders.value, listOf(testFolder))
  }

  @Test
  fun getRootFoldersFromCallsDeckRepository() {
    `when`(mockFolderRepository.getRootDeckFoldersFromUserId(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getRootFoldersFromUserId(testFolder.id, true, { assert(true) })
    assertEquals(folderViewModel.userRootFolders.value, listOf(testFolder))
  }

  @Test
  fun getRootNoteFoldersFromCallsRepository() {
    `when`(mockFolderRepository.getRootNoteFoldersFromUserId(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getRootNoteFoldersFromUserId(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.userRootFolders.value, listOf(testFolder))
  }

  @Test
  fun getRootDeckFoldersFromCallsRepository() {
    `when`(mockFolderRepository.getRootDeckFoldersFromUserId(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getRootDeckFoldersFromUserId(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.userRootFolders.value, listOf(testFolder))
  }

  @Test
  fun getFolderByIdCallsRepository() {
    `when`(mockFolderRepository.getFolderById(any(), any(), any())).thenAnswer {
      val onSuccess: (Folder) -> Unit = it.getArgument(1)
      onSuccess(testFolder)
    }
    folderViewModel.getFolderById(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.selectedFolder.value, testFolder)
  }

  @Test
  fun addFolderCallsRepository() {
    `when`(mockFolderRepository.addFolder(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.addFolder(testFolder, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun updateFolderCallsRepository() {
    `when`(mockFolderRepository.updateFolder(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.updateFolder(testFolder, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun deleteFolderByIdCallsRepository() {
    `when`(mockFolderRepository.deleteFolderById(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.deleteFolderById(testFolder.id, testFolder.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun deleteFoldersByUserIdCallsRepository() {
    `when`(mockFolderRepository.deleteFoldersByUserId(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.deleteFoldersByUserId(testFolder.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun getSubFoldersOfCallsRepository() {
    `when`(mockFolderRepository.getSubFoldersOf(any(), any(), any())).thenAnswer {
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
  fun deleteFolderContentsCallsRepository() {
    folderViewModel.deleteFolderContents(testFolder, noteViewModel)
    verify(mockFolderRepository)
        .deleteFolderContents(eq(testFolder), eq(noteViewModel), any(), any())
  }

  @Test
  fun updateFolderUpdatesStatesWhenSuccess() {
    `when`(mockFolderRepository.updateFolder(eq(testFolder), any(), any())).thenAnswer { invocation
      ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    folderViewModel.updateFolder(testFolder)

    verify(mockFolderRepository).updateFolder(eq(testFolder), any(), any())
    verify(mockFolderRepository).getRootNoteFoldersFromUserId(eq("1"), any(), any())
    verify(mockFolderRepository).getSubFoldersOf(eq("pid"), any(), any())
  }

  @Test
  fun draggedFolderUpdatesCorrectly() {
    folderViewModel.draggedFolder(testFolder)
    assertThat(folderViewModel.draggedFolder.value, `is`(testFolder))
  }
}
