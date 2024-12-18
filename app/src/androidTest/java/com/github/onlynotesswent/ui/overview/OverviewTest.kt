package com.github.onlynotesswent.ui.overview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
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
import com.google.firebase.Timestamp
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
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
              date = Timestamp.now(), // Use current timestamp
              lastModified = Timestamp.now(),
              visibility = Visibility.DEFAULT,
              userId = "1",
              noteCourse = Course("CS-100", "Sample Course", 2024, "path")))

  private val folderList =
      listOf(
          Folder(
              id = "2",
              name = "name",
              userId = "1",
              parentFolderId = null,
              lastModified = Timestamp.now()),
          Folder(
              id = "3",
              name = "name2",
              userId = "1",
              parentFolderId = null,
              lastModified = Timestamp.now()))

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

    // Mock folder repository to return new folder id
    `when`(folderRepository.getNewFolderId()).thenReturn("2")

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
  fun refreshButtonWorks() = runTest {
    // Mock the repositories to return an empty list of notes and folders, for the refresh button to
    // appear
    `when`(noteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(listOf())
    }
    `when`(folderRepository.getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(listOf())
    }
    composeTestRule.onNodeWithTag("emptyNoteAndFolderPrompt").assertIsDisplayed()
    composeTestRule.onNodeWithTag("refreshButton").assertIsDisplayed()

    // Mock the repositories to return a list of notes and folders
    `when`(noteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    `when`(folderRepository.getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    composeTestRule.onNodeWithTag("refreshButton").performClick()

    // Verify that the repositories were called twice, once during the initial load and once during
    // the
    // refresh click
    verify(noteRepository, times(2)).getRootNotesFromUid(eq("1"), any(), any(), any())
    verify(folderRepository, times(2)).getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())
    composeTestRule.onNodeWithTag("noteAndFolderList").assertIsDisplayed()
  }

  @Test
  fun noteAndFolderListIsDisplayed() = runTest {
    `when`(noteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    `when`(folderRepository.getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    noteViewModel.getRootNotesFromUid("1")
    folderViewModel.getRootFoldersFromUserId("1")
    composeTestRule.onNodeWithTag("noteAndFolderList").assertIsDisplayed()
  }

  @Test
  fun editNoteClickCallsNavActions() = runTest {
    `when`(noteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    noteViewModel.getRootNotesFromUid("1")
    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noteCard").performClick()
    verify(navigationActions).navigateTo(screen = Screen.EDIT_NOTE)
  }

  @Test
  fun displayTextWhenEmpty() = runTest {
    `when`(noteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(listOf())
    }
    noteViewModel.getRootNotesFromUid("1")
    composeTestRule.onNodeWithTag("emptyNoteAndFolderPrompt").assertIsDisplayed()
  }

  @Test
  fun displayTextWhenUserHasNoNotes() = runTest {
    `when`(noteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    noteViewModel.getRootNotesFromUid("2") // User 2 has no publicNotes
    composeTestRule.onNodeWithTag("emptyNoteAndFolderPrompt").assertIsDisplayed()
  }

  @Test
  fun selectFolderCallsNavActions() = runTest {
    `when`(folderRepository.getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    folderViewModel.getRootFoldersFromUserId("1")
    composeTestRule.onAllNodesWithTag("folderCard").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("folderCard").onFirst().performClick()
    val folderContentsScreen =
        Screen.FOLDER_CONTENTS.replace(oldValue = "{folderId}", newValue = folderList[0].id)
    verify(navigationActions).navigateTo(folderContentsScreen)
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
  fun createNoteButtonShowsDialog() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteOrFolder").performClick()
    composeTestRule.onNodeWithTag("createNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNote").performClick()
    composeTestRule.onNodeWithTag("NoteDialog").assertIsDisplayed()
  }

  @Test
  fun createFolderButtonShowsDialog() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteOrFolder").performClick()
    composeTestRule.onNodeWithTag("createFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").performClick()
    composeTestRule.onNodeWithTag("FolderDialog").assertIsDisplayed()
  }

  @Test
  fun createNoteDialogWorks() = runTest {
    composeTestRule.onNodeWithTag("createNoteOrFolder").performClick()
    composeTestRule.onNodeWithTag("createNote").performClick()

    composeTestRule.onNodeWithTag("inputNoteName").performTextInput("Note Name")
    composeTestRule.onNodeWithTag("currentVisibilityOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("previousVisibility").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nextVisibility").performClick()

    composeTestRule.onNodeWithTag("confirmNoteAction").assertIsDisplayed()

    // mock get newUid
    `when`(noteRepository.getNewUid()).thenReturn("2")

    composeTestRule.onNodeWithTag("confirmNoteAction").performClick()

    verify(noteRepository).addNote(any(), any(), any(), any())
    verify(navigationActions).navigateTo(screen = Screen.EDIT_NOTE)
  }

  @Test
  fun createFolderDialogWorks() {
    composeTestRule.onNodeWithTag("createNoteOrFolder").performClick()
    composeTestRule.onNodeWithTag("createFolder").performClick()

    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("inputFolderName").performTextInput("Folder Name")
    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsEnabled().assertIsDisplayed()

    composeTestRule.onNodeWithTag("currentVisibilityOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("previousVisibility").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nextVisibility").performClick()

    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsEnabled().assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").performClick()
  }

  @Test
  fun dragAndDropNoteWorksCorrectly() = runTest {
    `when`(noteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    `when`(folderRepository.getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    noteViewModel.getRootNotesFromUid("1")
    folderViewModel.getRootFoldersFromUserId("1")
    composeTestRule.onNodeWithTag("noteAndFolderList").assertIsDisplayed()

    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("folderCard")
        .filter(hasText("name"))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("noteCard").performTouchInput {
      down(center)
      advanceEventTime(1000)
      moveBy(Offset(-center.x * 9, 0f))
      advanceEventTime(5000)
      up()
    }
  }

  @Test
  fun openFileSystem() = runTest {
    `when`(noteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    `when`(folderRepository.getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    noteViewModel.getRootNotesFromUid("1")
    folderViewModel.getRootFoldersFromUserId("1")
    composeTestRule.onNodeWithTag("noteModalBottomSheet").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").performClick()
    composeTestRule.onNodeWithTag("noteModalBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteNoteBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moveNoteBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moveNoteBottomSheet").performClick()
    composeTestRule.onNodeWithTag("FileSystemPopup").assertIsDisplayed()
  }

  @Test
  fun deleteNote() = runTest {
    `when`(noteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    val userRootFoldersFlow =
        listOf(
            Folder(
                id = "8",
                name = "Root Folder 1",
                userId = "1",
                parentFolderId = "1",
                lastModified = Timestamp.now()),
            Folder(
                id = "9",
                name = "Root Folder 2",
                userId = "1",
                parentFolderId = "1",
                lastModified = Timestamp.now()))

    `when`(folderRepository.getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(userRootFoldersFlow)
    }
    noteViewModel.getRootNotesFromUid("1")
    folderViewModel.getRootNoteFoldersFromUserId("1")

    val subFolderList =
        listOf(
            Folder(
                id = "10",
                name = "SubFolder1",
                userId = "1",
                parentFolderId = "8",
                lastModified = Timestamp.now()),
            Folder(
                id = "11",
                name = "SubFolder2",
                userId = "1",
                parentFolderId = "8",
                lastModified = Timestamp.now()))

    `when`(
            folderRepository.getSubFoldersOf(
                eq("8"), anyOrNull(), any<(List<Folder>) -> Unit>(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
          onSuccess(subFolderList)
        }
    `when`(noteRepository.deleteNoteById(eq("noteId"), any(), any(), eq(true))).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    composeTestRule.onNodeWithTag("noteModalBottomSheet").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").performClick()
    composeTestRule.onNodeWithTag("noteModalBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteNoteBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteNoteBottomSheet").performClick()
    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("popup").performClick()
    composeTestRule.onNodeWithTag("confirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").performClick()
    verify(noteRepository).deleteNoteById(any(), any(), any(), any())
  }

  @Test
  fun navigateFileSystem() = runTest {
    `when`(noteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    val userRootFoldersFlow =
        listOf(
            Folder(
                id = "8",
                name = "Root Folder 1",
                userId = "1",
                parentFolderId = "1",
                lastModified = Timestamp.now()),
            Folder(
                id = "9",
                name = "Root Folder 2",
                userId = "1",
                parentFolderId = "1",
                lastModified = Timestamp.now()))

    `when`(folderRepository.getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(userRootFoldersFlow)
    }
    noteViewModel.getRootNotesFromUid("1")
    folderViewModel.getRootNoteFoldersFromUserId("1")

    val subFolderList =
        listOf(
            Folder(
                id = "10",
                name = "SubFolder1",
                userId = "1",
                parentFolderId = "8",
                lastModified = Timestamp.now()),
            Folder(
                id = "11",
                name = "SubFolder2",
                userId = "1",
                parentFolderId = "8",
                lastModified = Timestamp.now()))

    `when`(
            folderRepository.getSubFoldersOf(
                eq("8"), anyOrNull(), any<(List<Folder>) -> Unit>(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(2)
          onSuccess(subFolderList)
        }
    composeTestRule.onNodeWithTag("noteModalBottomSheet").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").performClick()
    composeTestRule.onNodeWithTag("noteModalBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moveNoteBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moveNoteBottomSheet").performClick()
    composeTestRule.onNodeWithTag("FileSystemPopup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox8").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox9").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox8").performClick()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox8").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox10").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox11").assertIsDisplayed()
  }
}
