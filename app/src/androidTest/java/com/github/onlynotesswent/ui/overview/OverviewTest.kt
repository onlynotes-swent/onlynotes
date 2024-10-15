package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.note.Type
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class OverviewTest {
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var noteRepository: NoteRepository
  private val noteList =
      listOf(
          Note(
              id = "1",
              type = Type.NORMAL_TEXT,
              name = "Sample Note",
              title = "Sample Title",
              content = "This is a sample content.",
              date = Timestamp.now(), // Use current timestamp
              userId = "1",
              image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Placeholder Bitmap
              ))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    userRepository = mock(UserRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    userViewModel = UserViewModel(userRepository)
    noteRepository = mock(NoteRepository::class.java)
    noteViewModel = NoteViewModel(noteRepository)

    // Mock the current route to be the user create screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.OVERVIEW)
    composeTestRule.setContent { OverviewScreen(navigationActions, noteViewModel) }
  }

  @Test
  fun noteListIsDisplayed() {

    `when`(noteRepository.getNotes(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    noteViewModel.getNotes("1")
    composeTestRule.onNodeWithTag("noteList").assertIsDisplayed()
  }

  @Test
  fun editNoteClickCallsNavActions() {
    `when`(noteRepository.getNotes(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    noteViewModel.getNotes("1")
    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noteCard").performClick()
    verify(navigationActions).navigateTo(screen = Screen.EDIT_NOTE)
  }

  @Test
  fun displayTextWhenEmpty() {
    `when`(noteRepository.getNotes(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(listOf())
    }
    noteViewModel.getNotes("1")

    composeTestRule.onNodeWithTag("emptyNotePrompt").assertIsDisplayed()
  }

  @Test
  fun displayBaseComponents() {
    composeTestRule.onNodeWithTag("createNote").assertExists()
    composeTestRule.onNodeWithTag("emptyNotePrompt").assertExists()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()
    composeTestRule.onNodeWithTag("RefreshButton").assertExists()
  }

  @Test
  fun createNoteButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNote").performClick()
    verify(navigationActions).navigateTo(screen = Screen.ADD_NOTE)
  }
}
