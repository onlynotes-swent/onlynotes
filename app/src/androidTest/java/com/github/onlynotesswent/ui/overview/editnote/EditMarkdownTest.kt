package com.github.onlynotesswent.ui.overview.editnote

import android.util.Log
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.file.FileRepository
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepository
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.io.File
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class EditMarkdownTest {
  @Mock private lateinit var noteRepository: NoteRepository
  @Mock private lateinit var fileRepository: FileRepository
  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var navigationActions: NavigationActions
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var fileViewModel: FileViewModel
  private lateinit var userViewModel: UserViewModel
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    MockitoAnnotations.openMocks(this)
    noteViewModel = NoteViewModel(noteRepository)
    fileViewModel = FileViewModel(fileRepository)
    userViewModel = UserViewModel(userRepository)

    val testNote =
        Note(
            "testNoteId",
            "testTitle",
            Timestamp.now(),
            Timestamp.now(),
            Visibility.PUBLIC,
            Course("CS-311", "SwEnt", 2024, "testCoursePath"),
            "1",
            "1")

    noteViewModel.selectedNote(testNote)

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
    `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_NOTE_MARKDOWN)

    `when`(fileRepository.downloadFile(any(), any(), any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(File) -> Unit>(3)
      val testFile = File.createTempFile("test", ".md").apply { writeText("Test content") }
      Log.d("TestDebug", "Mock file created: ${testFile.absolutePath}")
      onSuccess(testFile)
    }
    composeTestRule.setContent {
      EditMarkdownScreen(navigationActions, noteViewModel, fileViewModel, userViewModel)
    }
  }

  @Test
  fun displayBaseComponents() {
    // Top bar buttons
    composeTestRule.onNodeWithTag("closeButton").assertIsDisplayed()

    // Edit markdown components
    composeTestRule.onNodeWithTag("RichTextEditor").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editMarkdownFAB").assertIsDisplayed()

    // Navigation bar
    composeTestRule.onNodeWithTag("Detail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Comments").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PDF").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Content").assertIsDisplayed()
  }

  @Test
  fun clickGoBackButton() {
    composeTestRule.onNodeWithTag("closeButton").performClick()
    Mockito.verify(navigationActions).goBack()
  }

  @Test
  fun clickEditButton() {
    composeTestRule.onNodeWithTag("editMarkdownFAB").performClick()

    // Check that the edit button is not displayed
    composeTestRule.onNodeWithTag("editMarkdownFAB").assertDoesNotExist()

    composeTestRule.onNodeWithTag("RichTextEditor").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BoldControl").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ItalicControl").assertIsDisplayed()
    composeTestRule.onNodeWithTag("UnderlinedControl").assertIsDisplayed()
    composeTestRule.onNodeWithTag("StrikethroughControl").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SaveButton").assertIsDisplayed()
  }

  @Test
  fun enterText() {
    // Mock updateFile to do nothing
    `when`(fileRepository.updateFile(any(), any(), any(), any(), any())).thenAnswer { {} }

    // Should not be able to enter text before clicking the edit button
    assertThrows(AssertionError::class.java) {
      composeTestRule.onNodeWithTag("RichTextEditor").performTextInput("This is a test")
    }

    composeTestRule.onNodeWithTag("RichTextEditor").assertTextEquals("Test content")
    composeTestRule.onNodeWithTag("SaveButton").assertDoesNotExist()

    // Click the edit button
    composeTestRule.onNodeWithTag("editMarkdownFAB").performClick()
    composeTestRule.onNodeWithTag("RichTextEditor").performTextInput("This is a test")
    composeTestRule.onNodeWithTag("RichTextEditor").assertTextEquals("Test contentThis is a test")

    // Click the save button
    composeTestRule.onNodeWithTag("SaveButton").performClick()
    verify(fileRepository).updateFile(any(), any(), any(), any(), any())
  }

  @Test
  fun clickComponents() {
    composeTestRule.onNodeWithTag("editMarkdownFAB").performClick()

    composeTestRule.onNodeWithTag("BoldControl").assertContentDescriptionContains("Unselected")
    composeTestRule.onNodeWithTag("BoldControl").performClick()
    composeTestRule.onNodeWithTag("BoldControl").assertContentDescriptionContains("Selected")
    composeTestRule.onNodeWithTag("BoldControl").performClick()
    composeTestRule.onNodeWithTag("BoldControl").assertContentDescriptionContains("Unselected")

    composeTestRule.onNodeWithTag("ItalicControl").assertContentDescriptionContains("Unselected")
    composeTestRule.onNodeWithTag("ItalicControl").performClick()
    composeTestRule.onNodeWithTag("ItalicControl").assertContentDescriptionContains("Selected")
    composeTestRule.onNodeWithTag("ItalicControl").performClick()
    composeTestRule.onNodeWithTag("ItalicControl").assertContentDescriptionContains("Unselected")

    composeTestRule
        .onNodeWithTag("UnderlinedControl")
        .assertContentDescriptionContains("Unselected")
    composeTestRule.onNodeWithTag("UnderlinedControl").performClick()
    composeTestRule.onNodeWithTag("UnderlinedControl").assertContentDescriptionContains("Selected")
    composeTestRule.onNodeWithTag("UnderlinedControl").performClick()
    composeTestRule
        .onNodeWithTag("UnderlinedControl")
        .assertContentDescriptionContains("Unselected")

    composeTestRule
        .onNodeWithTag("StrikethroughControl")
        .assertContentDescriptionContains("Unselected")
    composeTestRule.onNodeWithTag("StrikethroughControl").performClick()
    composeTestRule
        .onNodeWithTag("StrikethroughControl")
        .assertContentDescriptionContains("Selected")
    composeTestRule.onNodeWithTag("StrikethroughControl").performClick()
    composeTestRule
        .onNodeWithTag("StrikethroughControl")
        .assertContentDescriptionContains("Unselected")
  }
}
