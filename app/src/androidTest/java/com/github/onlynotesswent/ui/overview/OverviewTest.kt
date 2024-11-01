package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
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
              type = Note.Type.NORMAL_TEXT,
              title = "Sample Title",
              content = "This is a sample content.",
              date = Timestamp.now(), // Use current timestamp
              visibility = Note.Visibility.DEFAULT,
              userId = "1",
              noteClass = Note.Class("CS-100", "Sample Class", 2024, "path"),
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

    // Mock the addUser method to call the onSuccess callback
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
    `when`(navigationActions.currentRoute()).thenReturn(Screen.OVERVIEW)
    composeTestRule.setContent { OverviewScreen(navigationActions, noteViewModel, userViewModel) }
  }

  @Test
  fun refreshButtonWorks() {
    // Mock the repository to return an empty list of notes, for the refresh button to appear
    `when`(noteRepository.getNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(listOf())
    }
    composeTestRule.onNodeWithTag("emptyNotePrompt").assertIsDisplayed()
    composeTestRule.onNodeWithTag("refreshButton").assertIsDisplayed()

    // Mock the repository to return a list of notes
    `when`(noteRepository.getNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    composeTestRule.onNodeWithTag("refreshButton").performClick()

    // Verify that the repository was called twice, once during the initial load and once during the
    // refresh click
    verify(noteRepository, times(2)).getNotesFrom(eq("1"), any(), any())
    composeTestRule.onNodeWithTag("noteList").assertIsDisplayed()
  }

  @Test
  fun noteListIsDisplayed() {

    `when`(noteRepository.getNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    noteViewModel.getNotesFrom("1")
    composeTestRule.onNodeWithTag("noteList").assertIsDisplayed()
  }

  @Test
  fun editNoteClickCallsNavActions() {
    `when`(noteRepository.getNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    noteViewModel.getNotesFrom("1")
    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noteCard").performClick()
    verify(navigationActions).navigateTo(screen = Screen.EDIT_NOTE)
  }

  @Test
  fun displayTextWhenEmpty() {
    `when`(noteRepository.getNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(listOf())
    }
    noteViewModel.getNotesFrom("1")
    composeTestRule.onNodeWithTag("emptyNotePrompt").assertIsDisplayed()
  }

  @Test
  fun displayTextWhenUserHasNoNotes() {
    `when`(noteRepository.getNotesFrom(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(noteList)
    }
    noteViewModel.getNotesFrom("2") // User 2 has no publicNotes
    composeTestRule.onNodeWithTag("emptyNotePrompt").assertIsDisplayed()
  }

  @Test
  fun displayBaseComponents() {
    composeTestRule.onNodeWithTag("createNote").assertExists()
    composeTestRule.onNodeWithTag("emptyNotePrompt").assertExists()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()
  }

  @Test
  fun createNoteButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNote").performClick()
    verify(navigationActions).navigateTo(screen = Screen.ADD_NOTE)
  }
}
