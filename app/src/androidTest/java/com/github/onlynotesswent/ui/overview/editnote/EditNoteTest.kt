package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.file.FileRepository
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.Scanner
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
import org.mockito.kotlin.never

class EditNoteTest {
  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var noteRepository: NoteRepository
  @Mock private lateinit var fileRepository: FileRepository
  @Mock private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var scanner: Scanner
  private lateinit var userViewModel: UserViewModel
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var fileViewModel: FileViewModel
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    // Mock is a way to create a fake object that can be used in place of a real object
    userViewModel = UserViewModel(userRepository)
    noteViewModel = NoteViewModel(noteRepository)
    fileViewModel = FileViewModel(fileRepository)

    // Mock the addUser method to call the onSuccess callback
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    val testUser = User("", "", "testUserName", "", "testUID", Timestamp.now(), 0.0)
    userViewModel.addUser(testUser, {}, {})

    // Mock the current route to be the note edit screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_NOTE)
    val mockNote =
        Note(
            id = "1",
            title = "Sample Title",
            date = Timestamp.now(), // Use current timestamp
            visibility = Visibility.DEFAULT,
            userId = "1",
            noteCourse = Course("CS-100", "Sample Class", 2024, "path"),
        )

    `when`(noteRepository.getNoteById(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(Note) -> Unit>(1)
      onSuccess(mockNote)
    }

    noteViewModel.getNoteById("mockNoteId")
    `when`(fileRepository.downloadFile(any(), any(), any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(5)
      onFailure(Exception("Simulated failure"))
    }
    `when`(fileRepository.uploadFile(any(), any(), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(3)
      onSuccess()
    }
    composeTestRule.setContent { EditNoteScreen(navigationActions, noteViewModel, userViewModel) }
  }

  @Test
  fun displayBaseComponents() {
    composeTestRule.onNodeWithTag("EditTitle textField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editNoteTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Save button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Edit Markdown button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("Delete button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("Add Comment Button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("NoCommentsText").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("visibilityEditButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("visibilityEditMenu").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("visibilityEditButton").performClick()
    composeTestRule.onNodeWithTag("visibilityEditMenu").assertIsDisplayed()

    composeTestRule.onNodeWithTag("EditPdfCard").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditPdfViewButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditPdfScanButton").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditPdfDeleteButton").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun clickGoBackButton() {
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navigationActions).goBack()
    verify(navigationActions, never()).navigateTo(Screen.OVERVIEW)
  }

  @Test
  fun modifyMarkdownButton() {
    composeTestRule.onNodeWithTag("Edit Markdown button").performScrollTo().performClick()
    verify(navigationActions).navigateTo(Screen.EDIT_NOTE_MARKDOWN)
  }

  @Test
  fun addCommentAndDeleteComment() {
    composeTestRule.onNodeWithTag("Add Comment Button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("Add Comment Button").performClick()
    composeTestRule.onNodeWithTag("EditCommentTextField").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeleteCommentButton").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("Save button").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun addCommentAndUpdateNote() {
    composeTestRule.onNodeWithTag("Add Comment Button").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("EditCommentTextField").performScrollTo().assertIsDisplayed()
    val updatedCommentText = "Edited comment content"
    composeTestRule.onNodeWithTag("EditCommentTextField").performTextInput(updatedCommentText)
    val expectedLabelText = "edited: "
    composeTestRule.onNode(hasText(expectedLabelText, substring = true)).assertIsDisplayed()
    composeTestRule.onNodeWithTag("Save button").performScrollTo().performClick()
  }

  @Test
  fun addComment() {
    composeTestRule.onNodeWithTag("Add Comment Button").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("EditCommentTextField").performScrollTo().assertIsDisplayed()
    val updatedCommentText = "Edited comment content"
    composeTestRule.onNodeWithTag("EditCommentTextField").performTextInput(updatedCommentText)
    val expectedLabelText = "edited: "
    composeTestRule.onNode(hasText(expectedLabelText, substring = true)).assertIsDisplayed()
  }

  @Test
  fun deleteClickCallsNavActions() {
    composeTestRule.onNodeWithTag("Delete button").performScrollTo().performClick()
    verify(navigationActions).navigateTo(Screen.OVERVIEW)
  }

  @Test
  fun pdfOperationsCallsCorrect() {
    // Can't directly test calls to fileViewModel, as it is not mockable. Thus verify calls to
    // fileRepository
    composeTestRule.onNodeWithTag("EditPdfViewButton").performScrollTo().performClick()
    verify(fileRepository).downloadFile(any(), eq(FileType.NOTE_PDF), any(), any(), any(), any())

    // Todo: Currently fails, as says parameter is null. Need to fix
    // composeTestRule.onNodeWithTag("EditPdfScanButton").performScrollTo().performClick()
    // verify(scanner).scan(any())

    composeTestRule.onNodeWithTag("EditPdfDeleteButton").performScrollTo().performClick()
    verify(fileRepository).deleteFile(any(), any(), any(), any(), any())
  }
}
