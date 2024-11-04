package com.github.onlynotesswent.model.folder

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class FolderViewModelTest {

    private lateinit var folderRepository: FolderRepository
    private lateinit var folderViewModel: FolderViewModel

    private val testFolder =
        Folder(
            id = "1",
            name = "name",
            userId = "1",
            parentFolderId = null
        )

    @Before
    fun setUp() {
        folderRepository = mock(FolderRepository::class.java)
        folderViewModel = FolderViewModel(folderRepository)
    }

    @Test
    fun getNewFolderId() {
        `when`(folderRepository.getNewFolderId()).thenReturn("fid")
        assertThat(folderViewModel.getNewFolderId(), `is`("fid"))
    }

    @Test
    fun initCallsRepository() {
        verify(folderRepository).init(any())
    }

    @Test
    fun getFoldersFromCallsRepository() {
        folderViewModel.getFoldersFrom("1")
        verify(folderRepository).getFoldersFrom(eq("1"), any(), any())
    }

    @Test
    fun getFolderByIdCallsRepository() {
        folderViewModel.getFolderById("1")
        verify(folderRepository).getFolderById(eq("1"), any(), any())
    }

    @Test
    fun addFolderCallsRepository() {
        folderViewModel.addFolder(testFolder, "1")
        verify(folderRepository).addFolder(eq(testFolder), any(), any())
    }

    @Test
    fun updateFolderCallsRepository() {
        folderViewModel.updateFolder(testFolder, "1")
        verify(folderRepository).updateFolder(eq(testFolder), any(), any())
    }

    @Test
    fun deleteFolderByIdCallsRepository() {
        folderViewModel.deleteFolderById("1", "1")
        verify(folderRepository).deleteFolderById(eq("1"), any(), any())
    }

    @Test
    fun getFoldersByParentFolderIdCallsRepository() {
        folderViewModel.getFoldersByParentFolderId("pid")
        verify(folderRepository).getFoldersByParentFolderId(eq("pid"), any(), any())
    }
}