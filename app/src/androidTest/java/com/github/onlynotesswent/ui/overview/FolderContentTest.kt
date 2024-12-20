package com.github.onlynotesswent.ui.overview

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.deck.DeckRepository
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.FlashcardRepository
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
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
import com.github.onlynotesswent.utils.NotesToFlashcard
import com.github.onlynotesswent.utils.OpenAI
import com.google.firebase.Timestamp
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq

@RunWith(MockitoJUnitRunner::class)
class FolderContentTest {

  @Mock private lateinit var mockUserRepository: UserRepository
  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockNoteRepository: NoteRepository
  @Mock private lateinit var mockFolderRepository: FolderRepository
  @Mock private lateinit var mockOpenAI: OpenAI
  @Mock private lateinit var mockContext: Context
  private lateinit var userViewModel: UserViewModel
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var folderViewModel: FolderViewModel
  private lateinit var notesToFlashcard: NotesToFlashcard

  private val noteList =
      listOf(
          Note(
              id = "4",
              title = "Sample Title",
              date = Timestamp.now(),
              lastModified = Timestamp.now(),
              visibility = Visibility.DEFAULT,
              userId = "1",
              noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
          ))

  private val folderList =
      listOf(
          Folder(
              id = "1",
              name = "name",
              userId = "1",
              parentFolderId = null,
              lastModified = Timestamp.now()))

  private val subfolder = Folder("3", "sub name", "1", "1", lastModified = Timestamp.now())
  private val folder = Folder("1", "1", "1", lastModified = Timestamp.now())

  private val testUser =
      User(
          firstName = "testFirstName",
          lastName = "testLastName",
          userName = "testUserName",
          email = "testEmail",
          uid = "1",
          dateOfJoining = Timestamp.now(),
          rating = 0.0)

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() = runTest {
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(mockUserRepository)
    noteViewModel = NoteViewModel(mockNoteRepository)
    folderViewModel = FolderViewModel(mockFolderRepository)
    notesToFlashcard =
        NotesToFlashcard(
            flashcardViewModel = FlashcardViewModel(mock(FlashcardRepository::class.java)),
            fileViewModel = mock(FileViewModel::class.java),
            deckViewModel = DeckViewModel(mock(DeckRepository::class.java)),
            noteViewModel = noteViewModel,
            folderViewModel = folderViewModel,
            openAIClient = mockOpenAI,
            context = mockContext)

    // Mock the current route to be the user create screen
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.FOLDER_CONTENTS)
    `when`(mockNoteRepository.getRootNotesFromUid(eq("1"), any(), any(), any())).then { invocation
      ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    `when`(mockFolderRepository.getRootNoteFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    `when`(mockFolderRepository.getNewFolderId()).thenAnswer { _ -> "mockFolderId" }
  }

  private fun init(selectedFolder: Folder) = runTest {
    // Mock the addUser method to call the onSuccess callback
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    userViewModel.addUser(testUser, {}, {})

    noteViewModel.getRootNotesFromUid("1")
    folderViewModel.getRootFoldersFromUserId("1", isDeckView = false)

    folderViewModel.selectedFolder(selectedFolder)
    composeTestRule.setContent {
      FolderContentScreen(
          mockNavigationActions, folderViewModel, noteViewModel, userViewModel, notesToFlashcard)
    }
  }

  private fun initWithoutUser(selectedFolder: Folder) {
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
    composeTestRule.onNodeWithTag("createObjectOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emptyNoteAndFolderPrompt").assertIsDisplayed()
  }

  @Test
  fun createFolderAndNoteFabWorks() {
    init(folder)
    composeTestRule.onNodeWithTag("createObjectOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createObjectOrFolder").performClick()
    composeTestRule.onNodeWithTag("createDeckOrNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").performClick()
    composeTestRule.onNodeWithTag("inputFolderName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dismissFolderAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFolderName").performTextInput("Sample Folder Name")
    composeTestRule.onNodeWithTag("confirmFolderAction").performClick()
  }

  @Test
  fun createFolder() = runTest {
    init(folder)

    `when`(mockFolderRepository.addFolder(any(), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    `when`(mockFolderRepository.getNewFolderId()).thenReturn("3")
    composeTestRule.onNodeWithTag("createObjectOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createObjectOrFolder").performClick()
    composeTestRule.onNodeWithTag("createFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").performClick()
    composeTestRule.onNodeWithTag("FolderDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFolderName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dismissFolderAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputFolderName").performTextInput("sub name")

    composeTestRule.onNodeWithTag("visibilityDropDown").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("currentVisibilityOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("previousVisibility").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nextVisibility").performClick()

    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsEnabled().assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").performClick()

    verify(mockFolderRepository).addFolder(any(), any(), any(), any())
    verify(mockNavigationActions).navigateTo(Screen.FOLDER_CONTENTS.replace("{folderId}", "3"))
  }

  @Test
  fun changeFolderName() {
    init(folder)
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("updateFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("updateFolderButton").performClick()
    composeTestRule.onNodeWithTag("FolderDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").performClick()
  }

  @Test
  fun deleteFolderButtonIsDisplayed() {
    init(folder)
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("updateFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").assertIsDisplayed()
  }

  @Test
  fun deleteFolderContents() = runTest {
    init(folder)
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("deleteFolderContentsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderContentsButton").performClick()
    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").performClick()

    verify(mockNoteRepository).deleteNotesFromFolder(eq("1"), any(), any(), any())
    verify(mockFolderRepository)
        .deleteFolderContents(any(), any<NoteViewModel>(), any(), any(), any())
  }

  @Test
  fun deleteRootFolder() {
    init(folder)
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("deleteFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").performClick()

    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").performClick()

    verify(mockNavigationActions).goBackFolderContents(any(), any(), any())
  }

  @Test
  fun deleteSubFolder() = runTest {
    init(subfolder)

    `when`(mockFolderRepository.getSubFoldersOf(eq("3"), anyOrNull(), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(2)
      onSuccess(emptyList())
    }

    folderViewModel.getSubFoldersOf(subfolder.id, null)

    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("deleteFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFolderButton").performClick()

    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").performClick()

    verify(mockNavigationActions).goBackFolderContents(any(), any(), any())
  }

  @Test
  fun moveFolder() = runTest {
    init(subfolder)
    `when`(mockFolderRepository.getSubFoldersOf(eq("3"), anyOrNull(), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(2)
      onSuccess(emptyList())
    }
    composeTestRule.onNodeWithTag("folderSettingsButton").performClick()
    composeTestRule.onNodeWithTag("moveFolderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moveFolderButton").performClick()
    composeTestRule.onNodeWithTag("FileSystemPopup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackFileSystemPopup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackFileSystemPopup").performClick()
    composeTestRule.onNodeWithTag("MoveHereButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoveHereButton").performClick()
    assert(folderViewModel.parentFolderId.value == null) {
      "Expected parentFolderId to be overview so null"
    }
  }

  @Test
  fun noteDialogDisplaysCorrectly() = runTest {
    init(folder)

    `when`(mockNoteRepository.addNote(any(), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    `when`(mockNoteRepository.getNewUid()).thenReturn("4")
    composeTestRule.onNodeWithTag("createObjectOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createObjectOrFolder").performClick()
    composeTestRule.onNodeWithTag("createDeckOrNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createDeckOrNote").performClick()
    composeTestRule.onNodeWithTag("NoteDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputNoteName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputNoteName").performTextInput("test name")

    composeTestRule.onNodeWithTag("confirmNoteAction").assertIsEnabled().assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmNoteAction").performClick()

    verify(mockNoteRepository).addNote(any(), any(), any(), any())
    verify(mockNavigationActions).navigateTo(Screen.EDIT_NOTE)
  }

  @Test
  fun userNotFoundScreenDisplaysCorrectly() {
    initWithoutUser(folder)
    composeTestRule.onNodeWithTag("userNotFoundScreen").assertIsDisplayed()
  }
}
