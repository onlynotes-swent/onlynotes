package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
import com.google.firebase.Timestamp
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@RunWith(MockitoJUnitRunner::class)
class CommentsTest {
  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var noteRepository: NoteRepository
  @Mock private lateinit var folderRepository: FolderRepository
  @Mock private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var folderViewModel: FolderViewModel
  @get:Rule val composeTestRule = createComposeRule()

  private val testFolder =
      Folder(id = "1", name = "Test Folder", userId = "1", lastModified = Timestamp.now())

  @Before
  fun setUp() = runTest {
    MockitoAnnotations.openMocks(this)
    // Mock is a way to create a fake object that can be used in place of a real object
    userViewModel = UserViewModel(userRepository)
    noteViewModel = NoteViewModel(noteRepository)
    folderViewModel = FolderViewModel(folderRepository)

    // Mock the addUser method to call the onSuccess callback
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    val testUser = User("", "", "testUserName", "", "1", Timestamp.now(), 0.0)
    userViewModel.addUser(testUser, {}, {})

    // Mock the current route to be the note edit screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_NOTE_COMMENT)
    val mockNote1 =
        Note(
            id = "1",
            title = "Sample Title",
            date = Timestamp.now(), // Use current timestamp
            lastModified = Timestamp.now(), // Use current timestamp
            visibility = Visibility.DEFAULT,
            userId = "1",
            noteCourse = Course("CS-100", "Sample Class", 2024, "path"),
        )

    val mockNote2 =
        Note(
            id = "2",
            title = "Sample Title2",
            date = Timestamp.now(), // Use current timestamp
            lastModified = Timestamp.now(),
            visibility = Visibility.DEFAULT,
            userId = "1",
            folderId = "1",
            noteCourse = Course("CS-100", "Sample Class", 2024, "path"),
        )

    `when`(noteRepository.getNoteById(eq("1"), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(Note) -> Unit>(1)
      onSuccess(mockNote1)
    }

    `when`(noteRepository.getNoteById(eq("2"), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(Note) -> Unit>(1)
      onSuccess(mockNote2)
    }

    `when`(folderRepository.getFolderById(eq("1"), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(Folder) -> Unit>(1)
      onSuccess(testFolder)
    }
  }

  private fun init(noteId: String) = runTest {
    noteViewModel.getNoteById(noteId)
    composeTestRule.setContent { CommentsScreen(navigationActions, noteViewModel, userViewModel) }
  }

  @Test
  fun displayBaseComponents() {
    init("1")
    // Top bar buttons
    composeTestRule.onNodeWithTag("closeButton").assertIsDisplayed()

    // Add comment button and no comments
    composeTestRule.onNodeWithTag("addCommentButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noCommentsText").assertIsDisplayed()

    // Navigation bar
    composeTestRule.onNodeWithTag("Detail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Comments").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PDF").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Content").assertIsDisplayed()
  }

  @Test
  fun addAndDeleteComment() {
    init("1")
    // Add a comment
    composeTestRule.onNodeWithTag("addCommentButton").performClick()
    composeTestRule.onNodeWithTag("EditCommentTextField").performTextInput("New comment")
    composeTestRule.onNodeWithTag("DeleteCommentButton").assertIsDisplayed()

    // Delete the comment
    composeTestRule.onNodeWithTag("DeleteCommentButton").performClick()
    composeTestRule.onNodeWithTag("DeleteCommentButton").assertDoesNotExist()
    composeTestRule.onNodeWithTag("EditCommentTextField").assertDoesNotExist()
  }

  @Test
  fun clickGoBackButton() = runTest {
    init("1")
    composeTestRule.onNodeWithTag("closeButton").performClick()

    verify(navigationActions).goBack()
    verify(noteRepository).updateNote(any(), any(), any(), any())
  }

  @Test
  fun clickGoBackButtonInsideFolder() = runTest {
    init("2")
    folderViewModel.getFolderById(testFolder.id)
    composeTestRule.onNodeWithTag("closeButton").performClick()

    verify(navigationActions).goBack()
    verify(noteRepository).updateNote(any(), any(), any(), any())
  }

  @Test
  fun clickNavigationDetailButton() = runTest {
    init("1")
    composeTestRule.onNodeWithTag("Detail").performClick()
    verify(navigationActions).navigateToAndPop(Screen.EDIT_NOTE)
    verify(noteRepository).updateNote(any(), any(), any(), any())
    verify(noteRepository, times(2)).getNoteById(any(), any(), any(), any())
  }

  @Test
  fun clickNavigationPDFButton() = runTest {
    init("1")
    composeTestRule.onNodeWithTag("PDF").performClick()
    verify(navigationActions).navigateToAndPop(Screen.EDIT_NOTE_PDF)
    verify(noteRepository).updateNote(any(), any(), any(), any())
    verify(noteRepository, times(2)).getNoteById(any(), any(), any(), any())
  }

  @Test
  fun clickNavigationContentButton() = runTest {
    init("1")
    composeTestRule.onNodeWithTag("Content").performClick()
    verify(navigationActions).navigateToAndPop(Screen.EDIT_NOTE_MARKDOWN)
    verify(noteRepository).updateNote(any(), any(), any(), any())
    verify(noteRepository, times(2)).getNoteById(any(), any(), any(), any())
  }
}
