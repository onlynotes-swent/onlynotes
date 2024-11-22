package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
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
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class CommentsTest {
  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var noteRepository: NoteRepository
  @Mock private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel
  private lateinit var noteViewModel: NoteViewModel
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    // Mock is a way to create a fake object that can be used in place of a real object
    userViewModel = UserViewModel(userRepository)
    noteViewModel = NoteViewModel(noteRepository)

    // Mock the addUser method to call the onSuccess callback
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    val testUser = User("", "", "testUserName", "", "testUID", Timestamp.now(), 0.0)
    userViewModel.addUser(testUser, {}, {})

    // Mock the current route to be the note edit screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_NOTE_COMMENT)
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

    composeTestRule.setContent { CommentsScreen(navigationActions, noteViewModel, userViewModel) }
  }

  @Test
  fun displayBaseComponents() {
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
  fun clickGoBackButton() {
    composeTestRule.onNodeWithTag("closeButton").performClick()

    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
    verify(noteRepository).updateNote(any(), any(), any())
  }

  @Test
  fun clickNavigationDetailButton() {
    composeTestRule.onNodeWithTag("Detail").performClick()
    verify(navigationActions).navigateTo(Screen.EDIT_NOTE)
    verify(noteRepository).updateNote(any(), any(), any())
    verify(noteRepository, times(2)).getNoteById(any(), any(), any())
  }

  @Test
  fun clickNavigationPDFButton() {
    composeTestRule.onNodeWithTag("PDF").performClick()
    verify(navigationActions).navigateTo(Screen.EDIT_NOTE_PDF)
    verify(noteRepository).updateNote(any(), any(), any())
    verify(noteRepository, times(2)).getNoteById(any(), any(), any())
  }

  @Test
  fun clickNavigationContentButton() {
    composeTestRule.onNodeWithTag("Content").performClick()
    verify(navigationActions).navigateTo(Screen.EDIT_NOTE_MARKDOWN)
    verify(noteRepository).updateNote(any(), any(), any())
    verify(noteRepository, times(2)).getNoteById(any(), any(), any())
  }
}
