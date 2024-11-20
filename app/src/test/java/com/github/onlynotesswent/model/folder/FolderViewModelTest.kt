package com.github.onlynotesswent.model.folder

import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
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
    folderViewModel.getFoldersFromUid("1")
    verify(mockFolderRepository).getFoldersFromUid(eq("1"), any(), any())
  }

  @Test
  fun getRootFoldersFromCallsRepository() {
    folderViewModel.getRootFoldersFromUid("1")
    verify(mockFolderRepository).getRootFoldersFromUid(eq("1"), any(), any())
  }

  @Test
  fun getFolderByIdCallsRepository() {
    folderViewModel.getFolderById("1")
    verify(mockFolderRepository).getFolderById(eq("1"), any(), any())
  }

  @Test
  fun addFolderCallsRepository() {
    folderViewModel.addFolder(testFolder, "1")
    verify(mockFolderRepository).addFolder(eq(testFolder), any(), any())
  }

  @Test
  fun updateFolderCallsRepository() {
    folderViewModel.updateFolder(testFolder, "1")
    verify(mockFolderRepository).updateFolder(eq(testFolder), any(), any())
  }

  @Test
  fun deleteFolderByIdCallsRepository() {
    folderViewModel.deleteFolderById("1", "1")
    verify(mockFolderRepository).deleteFolderById(eq("1"), any(), any())
  }

  @Test
  fun deleteFoldersByUserIdCallsRepository() {
    folderViewModel.deleteFoldersByUserId("1")
    verify(mockFolderRepository).deleteFoldersByUserId(eq("1"), any(), any())
  }

  @Test
  fun getSubFoldersOfCallsRepository() {
    folderViewModel.getSubFoldersOf("pid")
    verify(mockFolderRepository).getSubFoldersOf(eq("pid"), any(), any())
  }

  @Test
  fun getPublicFoldersCallsRepository() {
    folderViewModel.getPublicFolders()
    verify(mockFolderRepository).getPublicFolders(any(), any())
  }

  @Test
  fun deleteFolderContentsCallsRepository() {
    folderViewModel.deleteFolderContents(testFolder, noteViewModel)
    verify(mockFolderRepository)
        .deleteFolderContents(eq(testFolder), eq(noteViewModel), any(), any())
  }
}
