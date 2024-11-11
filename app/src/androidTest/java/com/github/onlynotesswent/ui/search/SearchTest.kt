package com.github.onlynotesswent.ui.search

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class SearchScreenTest {
  @Mock private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var noteRepository: NoteRepository
  @Mock private lateinit var userRepository: UserRepository
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private val testNote1 =
      Note(
          id = "",
          title = "Note 1",
          content = "",
          date = Timestamp.now(),
          visibility = Note.Visibility.PUBLIC,
          userId = "test",
          noteClass = Note.Class("CS-100", "Sample Class 1", 2024, "path"),
          image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
  private val testNote2 =
      Note(
          id = "1",
          title = "Note 2",
          content = "",
          date = Timestamp.now(),
          visibility = Note.Visibility.PUBLIC,
          userId = "test",
          noteClass = Note.Class("CS-200", "Sample Class 2", 2024, "path"),
          image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

  private val mockNotes = listOf(testNote1, testNote2)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(userRepository)
    noteViewModel = NoteViewModel(noteRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SEARCH_NOTE)
    `when`(noteRepository.getPublicNotes(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(0)
      onSuccess(mockNotes)
    }

    noteViewModel.getPublicNotes()
  }

  @Test
  fun testSearchFieldVisibility() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel, userViewModel) }

    composeTestRule.onNodeWithTag("searchScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
  }

  @Test
  fun testEmptySearchQuery() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel, userViewModel) }

    composeTestRule.onNodeWithTag("filteredNoteList").assertDoesNotExist()
  }

  @Test
  fun testValidSearchQueryShowsOneResult() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel, userViewModel) }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testNote1.title)

    composeTestRule.onNodeWithTag("filteredNoteList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText(testNote1.title))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("noteCard").assertCountEquals(1)
  }

  @Test
  fun testValidSearchQueryShowsMultipleResults() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel, userViewModel) }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Note")

    composeTestRule.onNodeWithTag("filteredNoteList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText("Note 1"))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText("Note 2"))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("noteCard").assertCountEquals(2)
  }

  @Test
  fun testNoSearchResultsMessage() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel, userViewModel) }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Non-existent Note")

    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No notes found matching your search.").assertIsDisplayed()
  }

  @Test
  fun testNoteSelectionNavigatesToEditScreen() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel, userViewModel) }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testNote1.title)
    composeTestRule.onNodeWithTag("filteredNoteList").onChildren().onFirst().performClick()

    verify(navigationActions).navigateTo(Screen.EDIT_NOTE)
  }
}
