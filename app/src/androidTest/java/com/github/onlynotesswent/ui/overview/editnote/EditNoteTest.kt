package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
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
import org.mockito.kotlin.never

class EditNoteTest {
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
    `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_NOTE)
    val mockNote1 =
        Note(
            id = "1",
            title = "Sample Title",
            date = Timestamp.now(), // Use current timestamp
            visibility = Visibility.DEFAULT,
            userId = "1",
            noteCourse = Course("CS-100", "Sample Class", 2024, "path"),
        )

    val mockNote2 =
        Note(
            id = "2",
            title = "Sample Title2",
            date = Timestamp.now(), // Use current timestamp
            visibility = Visibility.DEFAULT,
            userId = "1",
            folderId = "1",
            noteCourse = Course("CS-100", "Sample Class", 2024, "path"),
        )

    `when`(noteRepository.getNoteById(eq("1"), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(Note) -> Unit>(1)
      onSuccess(mockNote1)
    }

    `when`(noteRepository.getNoteById(eq("2"), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(Note) -> Unit>(1)
      onSuccess(mockNote2)
    }
  }

  private fun init(noteId: String) {
    noteViewModel.getNoteById(noteId)
    composeTestRule.setContent { EditNoteScreen(navigationActions, noteViewModel, userViewModel) }
  }

  @Test
  fun displayBaseComponents() {
    init("1")
    // Top bar buttons
    composeTestRule.onNodeWithTag("closeButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("saveNoteButton").assertIsDisplayed()

    // Edit note components
    composeTestRule.onNodeWithTag("EditTitle textField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("VisibilityEditMenuPublic").assertIsDisplayed()
    composeTestRule.onNodeWithTag("VisibilityEditMenuFriends Only").assertIsDisplayed()
    composeTestRule.onNodeWithTag("VisibilityEditMenuPrivate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("CourseFullName textField").assertIsDisplayed()

    // Delete button
    composeTestRule.onNodeWithTag("deleteNoteButton").assertIsDisplayed()

    // Navigation bar
    composeTestRule.onNodeWithTag("Detail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Comments").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PDF").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Content").assertIsDisplayed()
  }

  @Test
  fun clickGoBackButton() {
    init("1")

    composeTestRule.onNodeWithTag("closeButton").performClick()

    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
  }

  @Test
  fun clickGoBackButtonInsideFolder() {
    init("2")

    composeTestRule.onNodeWithTag("closeButton").performClick()

    verify(navigationActions).navigateTo(Screen.FOLDER_CONTENTS.replace("{folderId}", "1"))
  }

  @Test
  fun clickSaveButton() {
    init("1")

    composeTestRule.onNodeWithTag("saveNoteButton").performClick()

    verify(noteRepository).updateNote(any(), any(), any())
  }

  @Test
  fun clickDeleteButton() {
    init("1")

    composeTestRule.onNodeWithTag("deleteNoteButton").performClick()

    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").performClick()
    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
    verify(noteRepository).deleteNoteById(any(), any(), any())
  }

  @Test
  fun clickDeleteButtonAndCancel() {
    init("1")

    composeTestRule.onNodeWithTag("deleteNoteButton").performClick()

    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").performClick()
    composeTestRule.onNodeWithTag("popup").assertIsNotDisplayed()
    verify(noteRepository, never()).deleteNoteById(any(), any(), any())
    verify(navigationActions, never()).navigateTo(TopLevelDestinations.OVERVIEW)
  }

  @Test
  fun clickDetailButton() {
    init("1")

    composeTestRule.onNodeWithTag("Detail").performClick()

    verify(navigationActions).navigateTo(Screen.EDIT_NOTE)
  }

  @Test
  fun clickCommentsButton() {
    init("1")

    composeTestRule.onNodeWithTag("Comments").performClick()

    verify(navigationActions).navigateTo(Screen.EDIT_NOTE_COMMENT)
  }

  @Test
  fun pdfButtonNavigatesToPdf() {
    init("1")

    composeTestRule.onNodeWithTag("PDF").performClick()

    verify(navigationActions).navigateTo(Screen.EDIT_NOTE_PDF)
  }

  @Test
  fun contentButtonNavigatesToContent() {
    init("1")

    composeTestRule.onNodeWithTag("Content").performClick()

    verify(navigationActions).navigateTo(Screen.EDIT_NOTE_MARKDOWN)
  }

  @Test
  fun modifyTitleAndExitingShowsPopup() {
    init("1")

    val newTitle = "New Title"
    composeTestRule.onNodeWithTag("EditTitle textField").performTextInput(newTitle)
    composeTestRule.onNodeWithTag("closeButton").performClick()

    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("confirmButton").performClick()

    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
    verify(noteRepository, never()).updateNote(any(), any(), any())
  }

  @Test
  fun clickDiscardChangesPopUpInsideFolderNavigatesToFolderContents() {
    init("2")

    val newTitle = "New Title"
    composeTestRule.onNodeWithTag("EditTitle textField").performTextInput(newTitle)
    composeTestRule.onNodeWithTag("closeButton").performClick()

    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("confirmButton").performClick()
    verify(navigationActions).navigateTo(Screen.FOLDER_CONTENTS.replace("{folderId}", "1"))
  }

  @Test
  fun modifyTitleAndGoingToCommentsShowsPopup() {
    init("1")

    val newTitle = "New Title"
    composeTestRule.onNodeWithTag("EditTitle textField").performTextInput(newTitle)
    composeTestRule.onNodeWithTag("Comments").performClick()

    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("confirmButton").performClick()

    verify(navigationActions).navigateTo(Screen.EDIT_NOTE_COMMENT)
    verify(noteRepository, never()).updateNote(any(), any(), any())
  }

  @Test
  fun clickingOnCourseNameShowsCourseDetails() {
    init("1")

    composeTestRule.onNodeWithTag("CourseFullName textField").performClick()
    composeTestRule.onNodeWithTag("CourseFullName textField").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("EditCourseCode textField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditCourseName textField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditCourseYear textField").assertIsDisplayed()
  }
}
