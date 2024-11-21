package com.github.onlynotesswent.model.folder

import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FolderViewModelTest {

  private lateinit var folderRepository: FolderRepository
  private lateinit var folderViewModel: FolderViewModel

  private val testFolder = Folder(id = "1", name = "name", userId = "1", parentFolderId = "pid")

  @Before
  fun setUp() {
    folderRepository = mock(FolderRepository::class.java)
    folderViewModel = FolderViewModel(folderRepository)
  }

  @Test
  fun getNewFolderId() {
    `when`(folderRepository.getNewFolderId()).thenReturn("1")
    assertThat(folderViewModel.getNewFolderId(), `is`("1"))
  }

  @Test
  fun initCallsRepository() {
    verify(folderRepository).init(any())
  }

  @Test
  fun getFoldersFromCallsRepository() {
    `when`(folderRepository.getFoldersFromUid(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getFoldersFromUid(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.userFolders.value, listOf(testFolder))
  }

  @Test
  fun getRootFoldersFromCallsRepository() {
    `when`(folderRepository.getRootFoldersFromUid(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getRootFoldersFromUid(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.userRootFolders.value, listOf(testFolder))
  }

  @Test
  fun getFolderByIdCallsRepository() {
    `when`(folderRepository.getFolderById(any(), any(), any())).thenAnswer {
      val onSuccess: (Folder) -> Unit = it.getArgument(1)
      onSuccess(testFolder)
    }
    folderViewModel.getFolderById(testFolder.id, { assert(true) })
    assertEquals(folderViewModel.selectedFolder.value, testFolder)
  }

  @Test
  fun addFolderCallsRepository() {
    `when`(folderRepository.addFolder(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.addFolder(testFolder, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun updateFolderCallsRepository() {
    `when`(folderRepository.updateFolder(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.updateFolder(testFolder, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun deleteFolderByIdCallsRepository() {
    `when`(folderRepository.deleteFolderById(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.deleteFolderById(testFolder.id, testFolder.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun deleteFoldersByUserIdCallsRepository() {
    `when`(folderRepository.deleteFoldersByUserId(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }

    var onSuccessCalled = false
    folderViewModel.deleteFoldersByUserId(testFolder.userId, { onSuccessCalled = true })
    assert(onSuccessCalled)
  }

  @Test
  fun getSubFoldersOfCallsRepository() {
    `when`(folderRepository.getSubFoldersOf(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getSubFoldersOf(testFolder.parentFolderId!!, { assert(true) })
    assertEquals(folderViewModel.folderSubFolders.value, listOf(testFolder))
  }

  @Test
  fun getPublicFoldersCallsRepository() {
    `when`(folderRepository.getPublicFolders(any(), any())).thenAnswer {
      val onSuccess: (List<Folder>) -> Unit = it.getArgument(0)
      onSuccess(listOf(testFolder))
    }
    folderViewModel.getPublicFolders { assert(true) }
    assertEquals(folderViewModel.publicFolders.value, listOf(testFolder))
  }
}
