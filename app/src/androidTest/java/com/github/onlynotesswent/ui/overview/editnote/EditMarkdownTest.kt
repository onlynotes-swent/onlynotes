package com.github.onlynotesswent.ui.overview.editnote

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
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.never

class EditMarkdownTest {
  @Mock private lateinit var noteRepository: NoteRepository
  @Mock private lateinit var fileRepository: FileRepository
  @Mock private lateinit var navigationActions: NavigationActions
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var fileViewModel: FileViewModel
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    MockitoAnnotations.openMocks(this)
    noteViewModel = NoteViewModel(noteRepository)
    fileViewModel = FileViewModel(fileRepository)

    val testNote =
        Note(
            "testNoteId",
            "testTitle",
            Timestamp.now(),
            Visibility.PUBLIC,
            Course("CS-311", "SwEnt", 2024, "testCoursePath"),
            "testUserId")

    noteViewModel.selectedNote(testNote)

    // Mock the current route to be the user create screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_NOTE)
    composeTestRule.setContent {
      EditMarkdownScreen(navigationActions, noteViewModel, fileViewModel)
    }
  }

  @Test
  fun displayBaseComponents() {
    composeTestRule.onNodeWithTag("EditorControl").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Save button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("RichTextEditor").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BoldControl").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ItalicControl").assertIsDisplayed()
    composeTestRule.onNodeWithTag("UnderlinedControl").assertIsDisplayed()
    composeTestRule.onNodeWithTag("StrikethroughControl").assertIsDisplayed()
  }

  @Test
  fun clickGoBackButton() {
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    org.mockito.kotlin.verify(navigationActions).goBack()
    org.mockito.kotlin.verify(navigationActions, never()).navigateTo(Screen.OVERVIEW)
  }

  @Test
  fun enterText() {
    composeTestRule.onNodeWithTag("RichTextEditor").performTextInput("example text")
    composeTestRule.onNodeWithTag("RichTextEditor").assertTextEquals("example text")
  }

  @Test
  fun clickComponents() {
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
