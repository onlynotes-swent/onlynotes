package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderRepository
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class FolderContentTest {

  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var noteRepository: NoteRepository
  private lateinit var folderViewModel: FolderViewModel
  private lateinit var folderRepository: FolderRepository

  private val noteList =
      listOf(
          Note(
              id = "1",
              type = Note.Type.NORMAL_TEXT,
              title = "Sample Title",
              content = "This is a sample content.",
              date = Timestamp.now(),
              visibility = Note.Visibility.DEFAULT,
              userId = "1",
              noteClass = Note.Class("CS-100", "Sample Class", 2024, "path"),
              image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)))

  private val folderList =
      listOf(Folder(id = "1", name = "name", userId = "1", parentFolderId = null))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {

    userRepository = mock(UserRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    userViewModel = UserViewModel(userRepository)
    noteRepository = mock(NoteRepository::class.java)
    noteViewModel = NoteViewModel(noteRepository)
    folderRepository = mock(FolderRepository::class.java)
    folderViewModel = FolderViewModel(folderRepository)

    // Mock the addUser method to call the onSuccess callback
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
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
    `when`(navigationActions.currentRoute()).thenReturn(Screen.FOLDER_CONTENTS)
    `when`(noteRepository.getRootNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    `when`(folderRepository.getRootFoldersFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    `when`(folderRepository.getNewFolderId()).thenAnswer { _ -> "mockFolderId" }

    noteViewModel.getRootNotesFrom("1")
    folderViewModel.getRootFoldersFrom("1")
    val folder = Folder("1", "1", "1")
    folderViewModel.addFolder(folder, "1")
    folderViewModel.selectedFolder(folder)
    composeTestRule.setContent {
      FolderContentScreen(navigationActions, folderViewModel, noteViewModel, userViewModel)
    }
  }

  @Test
  fun displayBaseComponents() {
    composeTestRule.onNodeWithTag("folderContentScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folderContentTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("clearButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folderSettingsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createSubNoteOrSubFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emptyFolderPrompt").assertIsDisplayed()
  }

  @Test
  fun addFolder() {
    composeTestRule.onNodeWithTag("createSubNoteOrSubFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createSubNoteOrSubFolder").performClick()
    composeTestRule.onNodeWithTag("createNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").performClick()
    composeTestRule.onNodeWithTag("createFolderDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFolderName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderCreation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dismissFolderCreation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFolderName").performTextInput("Sample Folder Name")
    composeTestRule.onNodeWithTag("confirmFolderCreation").performClick()
  }

  @Test
  fun changeFolderName() {
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("renameFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("renameFolderButton").performClick()
    composeTestRule.onNodeWithTag("renameFolderDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dismissRenameButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmRenameButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmRenameButton").performClick()
  }

  @Test
  fun deleteFolder() {
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("renameFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").performClick()
    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
  }

  @Test
  fun createNoteButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("createSubNoteOrSubFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createSubNoteOrSubFolder").performClick()
    composeTestRule.onNodeWithTag("createNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNote").performClick()
    verify(navigationActions).navigateTo(Screen.ADD_NOTE)
  }
}
