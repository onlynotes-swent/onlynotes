package com.github.onlynotesswent.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.never

class EditNoteTest {
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var noteRepository: NoteRepository
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

    val testUser = User("", "", "testUserName", "", "testUID", Timestamp.now(), 0.0)
    userViewModel.addUser(testUser, {}, {})

    // Mock the current route to be the user create screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_NOTE)
    composeTestRule.setContent { EditNoteScreen(navigationActions, noteViewModel, userViewModel) }
  }

  @Test
  fun displayBaseComponents() {
    composeTestRule.onNodeWithTag("EditTitle textField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editNoteTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Save button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Delete button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Add Comment Button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("NoCommentsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("visibilityEditButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("visibilityEditMenu").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("visibilityEditButton").performClick()
    composeTestRule.onNodeWithTag("visibilityEditMenu").assertIsDisplayed()
  }

  @Test
  fun clickGoBackButton() {
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    org.mockito.kotlin.verify(navigationActions).goBack()
    org.mockito.kotlin.verify(navigationActions, never()).navigateTo(Screen.OVERVIEW)
  }
  /*
  @Test
  fun clickAddCommentButton() {
    composeTestRule.onNodeWithTag("Add Comment Button").performClick()
    composeTestRule.onNodeWithTag("EditCommentTextField_0").assertIsDisplayed()
  }*/

  @Test
  fun saveClickCallsNavActions() {
    // Ensure the button is enabled
    composeTestRule.onNodeWithTag("EditTitle textField").performTextInput("title")

    composeTestRule.onNodeWithTag("Save button").performClick()
    verify(navigationActions).navigateTo(Screen.OVERVIEW)
  }

  @Test
  fun deleteClickCallsNavActions() {
    composeTestRule.onNodeWithTag("Delete button").performClick()
    verify(navigationActions).navigateTo(Screen.OVERVIEW)
  }
}
