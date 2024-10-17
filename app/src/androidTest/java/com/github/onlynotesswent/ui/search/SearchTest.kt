package com.github.onlynotesswent.ui.search

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.note.Type
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class SearchScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var noteRepository: NoteRepository

  @get:Rule val composeTestRule = createComposeRule()

  private val mockNotes =
      listOf(
          Note(
              id = "",
              type = Type.NORMAL_TEXT,
              title = "Note 1",
              name = "",
              content = "",
              date = Timestamp.now(),
              userId = "test",
              image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)),
          Note(
              id = "1",
              type = Type.NORMAL_TEXT,
              title = "Note 2",
              name = "",
              content = "",
              date = Timestamp.now(),
              userId = "test",
              image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)),
      )

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    noteRepository = mock(NoteRepository::class.java)
    noteViewModel = NoteViewModel(noteRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SEARCH_NOTE)
    `when`(noteRepository.getNotes(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(mockNotes)
    }

    noteViewModel.getNotes("test")
  }

  @Test
  fun testSearchFieldVisibility() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel) }

    composeTestRule.onNodeWithTag("searchScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
  }

  @Test
  fun testEmptySearchQuery() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel) }

    composeTestRule.onNodeWithTag("filteredNoteList").assertDoesNotExist()
  }

  @Test
  fun testValidSearchQueryShowsOneResult() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel) }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Note 1")

    composeTestRule.onNodeWithTag("filteredNoteList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText("Note 1"))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("noteCard").assertCountEquals(1)
  }

  @Test
  fun testValidSearchQueryShowsMultipleResults() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel) }

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
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel) }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Non-existent Note")

    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No notes found matching your search.").assertIsDisplayed()
  }

  @Test
  fun testNoteSelectionNavigatesToEditScreen() {
    composeTestRule.setContent { SearchScreen(navigationActions, noteViewModel) }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Note 1")
    composeTestRule.onNodeWithTag("filteredNoteList").onChildren().onFirst().performClick()

    verify(navigationActions).navigateTo(Screen.EDIT_NOTE)
  }
}
