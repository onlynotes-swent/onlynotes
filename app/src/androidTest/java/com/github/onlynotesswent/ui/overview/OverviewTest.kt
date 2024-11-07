package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class OverviewTest {
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
              title = "Sample Title",
              content = "This is a sample content.",
              date = Timestamp.now(), // Use current timestamp
              visibility = Note.Visibility.DEFAULT,
              userId = "1",
              noteClass = Note.Class("CS-100", "Sample Class", 2024, "path"),
              image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Placeholder Bitmap
              ))

  private val folderList =
      listOf(Folder(id = "1", name = "name", userId = "1", parentFolderId = null))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
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
    `when`(navigationActions.currentRoute()).thenReturn(Screen.OVERVIEW)
    composeTestRule.setContent {
      OverviewScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
    }
  }

  @Test
  fun refreshButtonWorks() {
    // Mock the repositories to return an empty list of notes and folders, for the refresh button to
    // appear
    `when`(noteRepository.getRootNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(listOf())
    }
    `when`(folderRepository.getRootFoldersFromUid(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(listOf())
    }
    composeTestRule.onNodeWithTag("emptyNoteAndFolderPrompt").assertIsDisplayed()
    composeTestRule.onNodeWithTag("refreshButton").assertIsDisplayed()

    // Mock the repositories to return a list of notes and folders
    `when`(noteRepository.getRootNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    `when`(folderRepository.getRootFoldersFromUid(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    composeTestRule.onNodeWithTag("refreshButton").performClick()

    // Verify that the repositories were called twice, once during the initial load and once during
    // the
    // refresh click
    verify(noteRepository, times(2)).getRootNotesFrom(eq("1"), any(), any())
    verify(folderRepository, times(2)).getRootFoldersFromUid(eq("1"), any(), any())
    composeTestRule.onNodeWithTag("noteAndFolderList").assertIsDisplayed()
  }

  @Test
  fun noteAndFolderListIsDisplayed() {
    `when`(noteRepository.getRootNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    `when`(folderRepository.getRootFoldersFromUid(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    noteViewModel.getRootNotesFrom("1")
    folderViewModel.getRootFoldersFromUid("1")
    composeTestRule.onNodeWithTag("noteAndFolderList").assertIsDisplayed()
  }

  @Test
  fun editNoteClickCallsNavActions() {
    `when`(noteRepository.getRootNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    noteViewModel.getRootNotesFrom("1")
    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noteCard").performClick()
    verify(navigationActions).navigateTo(screen = Screen.EDIT_NOTE)
  }

  @Test
  fun displayTextWhenEmpty() {
    `when`(noteRepository.getRootNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(listOf())
    }
    noteViewModel.getRootNotesFrom("1")
    composeTestRule.onNodeWithTag("emptyNoteAndFolderPrompt").assertIsDisplayed()
  }

  @Test
  fun displayTextWhenUserHasNoNotes() {
    `when`(noteRepository.getRootNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    noteViewModel.getRootNotesFrom("2") // User 2 has no publicNotes
    composeTestRule.onNodeWithTag("emptyNoteAndFolderPrompt").assertIsDisplayed()
  }

  @Test
  fun selectFolderCallsNavActions() {
    `when`(folderRepository.getRootFoldersFromUid(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    folderViewModel.getRootFoldersFromUid("1")
    composeTestRule.onNodeWithTag("folderCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("folderCard").performClick()
    verify(navigationActions).navigateTo(screen = Screen.FOLDER_CONTENTS)
  }

  @Test
  fun displayBaseComponents() {
    composeTestRule.onNodeWithTag("createNoteOrFolder").assertExists()
    composeTestRule.onNodeWithTag("emptyNoteAndFolderPrompt").assertExists()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    composeTestRule.onNodeWithTag("createNoteOrFolder").performClick()
    composeTestRule.onNodeWithTag("createNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").assertIsDisplayed()
  }

  @Test
  fun createNoteButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteOrFolder").performClick()
    composeTestRule.onNodeWithTag("createNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNote").performClick()
    verify(navigationActions).navigateTo(screen = Screen.ADD_NOTE)
  }

  @Test
  fun createFolderButtonShowsDialog() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteOrFolder").performClick()
    composeTestRule.onNodeWithTag("createFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").performClick()
    composeTestRule.onNodeWithTag("createFolderDialog").assertIsDisplayed()
  }
}
