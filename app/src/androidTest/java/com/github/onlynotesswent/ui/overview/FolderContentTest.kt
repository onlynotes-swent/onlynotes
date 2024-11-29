package com.github.onlynotesswent.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderRepository
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepository
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class FolderContentTest {

  @Mock private lateinit var mockUserRepository: UserRepository
  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockNoteRepository: NoteRepository
  @Mock private lateinit var mockFolderRepository: FolderRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var folderViewModel: FolderViewModel

  private val noteList =
      listOf(
          Note(
              id = "4",
              title = "Sample Title",
              date = Timestamp.now(),
              visibility = Visibility.DEFAULT,
              userId = "1",
              noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
          ))

  private val subNoteList1 =
      listOf(
          Note(
              id = "5",
              title = "Sample Sub Note",
              date = Timestamp.now(),
              visibility = Visibility.DEFAULT,
              userId = "2",
              folderId = "2",
              noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
          ))

  private val subNoteList2 =
      listOf(
          Note(
              id = "6",
              title = "Sample Sub Note3",
              date = Timestamp.now(),
              visibility = Visibility.DEFAULT,
              userId = "1",
              folderId = "1",
              noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
          ))

  private val subNoteList3 =
      listOf(
          Note(
              id = "7",
              title = "Sample Sub Note4",
              date = Timestamp.now(),
              visibility = Visibility.DEFAULT,
              userId = "1",
              folderId = "4",
              noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
          ))

  private val folderList =
      listOf(Folder(id = "1", name = "name", userId = "1", parentFolderId = null))

  private val subFolderListSameUser =
      listOf(Folder(id = "4", name = "name", userId = "1", parentFolderId = "1"))

  private val folderListDifferentUser =
      listOf(Folder(id = "2", name = "name", userId = "2", parentFolderId = "1"))

  private val subfolder = Folder("3", "sub name", "1", "1")
  private val folder = Folder("1", "1", "1")
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(mockUserRepository)
    noteViewModel = NoteViewModel(mockNoteRepository)
    folderViewModel = FolderViewModel(mockFolderRepository)

    // Mock the addUser method to call the onSuccess callback
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    val testUser =
        User(
            firstName = "testFirstName",
            lastName = "testLastName",
            userName = "testUserName",
            email = "testEmail",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0)

    userViewModel.addUser(testUser, {}, {})

    // Mock the current route to be the user create screen
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.FOLDER_CONTENTS)
    `when`(mockNoteRepository.getRootNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    `when`(mockFolderRepository.getRootFoldersFromUid(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    `when`(mockFolderRepository.getNewFolderId()).thenAnswer { _ -> "mockFolderId" }

    noteViewModel.getRootNotesFrom("1")
    folderViewModel.getRootFoldersFromUid("1")
  }

  private fun init(selectedFolder: Folder) {
    folderViewModel.selectedFolder(selectedFolder)
    composeTestRule.setContent {
      FolderContentScreen(mockNavigationActions, folderViewModel, noteViewModel, userViewModel)
    }
  }

  @Test
  fun displayBaseComponents() {
    init(folder)
    composeTestRule.onNodeWithTag("folderContentScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folderContentTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folderSettingsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createSubNoteOrSubFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emptyFolderPrompt").assertIsDisplayed()
  }

  @Test
  fun createFolderAndNoteFabWorks() {
    init(folder)
    composeTestRule.onNodeWithTag("createSubNoteOrSubFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createSubNoteOrSubFolder").performClick()
    composeTestRule.onNodeWithTag("createNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").performClick()
    composeTestRule.onNodeWithTag("inputFolderName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dismissFolderAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFolderName").performTextInput("Sample Folder Name")
    composeTestRule.onNodeWithTag("confirmFolderAction").performClick()
  }

  @Test
  fun createFolder() {
    init(folder)

    `when`(mockFolderRepository.addFolder(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    `when`(mockFolderRepository.getNewFolderId()).thenReturn("3")
    composeTestRule.onNodeWithTag("createSubNoteOrSubFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createSubNoteOrSubFolder").performClick()
    composeTestRule.onNodeWithTag("createFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").performClick()
    composeTestRule.onNodeWithTag("FolderDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFolderName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dismissFolderAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFolderName").performTextInput("sub name")

    composeTestRule.onNodeWithTag("visibilityDropDown").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("visibilityButton").assertIsDisplayed().performClick()

    composeTestRule.onNodeWithTag("item--Public").assertIsDisplayed()
    composeTestRule.onNodeWithTag("item--Friends Only").assertIsDisplayed()
    composeTestRule.onNodeWithTag("item--Private").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsEnabled().assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").performClick()

    verify(mockFolderRepository).addFolder(any(), any(), any())
    verify(mockNavigationActions).navigateTo(Screen.FOLDER_CONTENTS.replace("{folderId}", "3"))
  }

  @Test
  fun changeFolderName() {
    init(folder)
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("renameFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("renameFolderButton").performClick()
    composeTestRule.onNodeWithTag("FolderDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").performClick()
  }

  @Test
  fun deleteFolderButtonIsDisplayed() {
    init(folder)
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("renameFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").assertIsDisplayed()
  }

  @Test
  fun deleteFolderContents() {
    init(folder)
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("deleteFolderContentsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderContentsButton").performClick()
    composeTestRule.onNodeWithTag("emptyFolderPrompt").assertIsDisplayed()
  }

  @Test
  fun deleteRootFolder() {
    init(folder)
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("deleteFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").performClick()

    verify(mockNavigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
  }

  @Test
  fun deleteSubFolder() {
    init(subfolder)

    `when`(mockFolderRepository.getSubFoldersOf(eq("3"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(emptyList())
    }

    folderViewModel.getSubFoldersOf(subfolder.id)

    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("deleteFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").performClick()

    verify(mockNavigationActions).navigateTo(Screen.FOLDER_CONTENTS.replace("{folderId}", "1"))
  }

  @Test
  fun moveOutDifferentUserDoesNotMoveNote() {
    init(folder)

    `when`(mockNoteRepository.getNotesFromFolder(eq("2"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(subNoteList1)
    }

    noteViewModel.getNotesFromFolder("2")

    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutButton").performClick()
    composeTestRule.onNodeWithTag("MoveOutDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutConfirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutConfirmButton").performClick()
    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
  }

  @Test
  fun moveOutSameUserDoesMoveNote() {
    init(folder)

    `when`(mockNoteRepository.getNotesFromFolder(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(subNoteList2)
    }

    noteViewModel.getNotesFromFolder("1")

    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutButton").performClick()
    composeTestRule.onNodeWithTag("MoveOutDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutConfirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutConfirmButton").performClick()
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText("Sample Sub Note3"))
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun moveOutMovesNoteToParentFolder() {
    init(folder)

    `when`(mockFolderRepository.getSubFoldersOf(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(subFolderListSameUser)
    }

    folderViewModel.getSubFoldersOf("1")

    composeTestRule.onNodeWithTag("folderCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folderCard").performClick()

    `when`(mockNoteRepository.getNotesFromFolder(eq("4"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(subNoteList3)
    }
    noteViewModel.getNotesFromFolder("4")

    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutButton").performClick()
    composeTestRule.onNodeWithTag("MoveOutDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutConfirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveOutConfirmButton").performClick()
    verify(mockNavigationActions).navigateTo(Screen.FOLDER_CONTENTS.replace("{folderId}", "1"))
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText("Sample Sub Note4"))
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun deleteFolderContentsDifferentUserDoesNotDelete() {
    init(folder)

    `when`(mockFolderRepository.getSubFoldersOf(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderListDifferentUser)
    }

    `when`(mockNoteRepository.getNotesFromFolder(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(subNoteList1)
    }
    folderViewModel.getSubFoldersOf("1")
    noteViewModel.getNotesFromFolder("1")

    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folderCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folderCard").performClick()
    val folderContentsScreen =
        Screen.FOLDER_CONTENTS.replace(
            oldValue = "{folderId}", newValue = folderListDifferentUser[0].id)
    verify(mockNavigationActions).navigateTo(folderContentsScreen)
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("deleteFolderContentsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderContentsButton").performClick()
    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
  }
}
