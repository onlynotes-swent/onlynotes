package com.github.onlynotesswent.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.onlynotesswent.model.file.FileRepository
import com.github.onlynotesswent.model.file.FileViewModel
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

class EditMarkdownTest {
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var noteRepository: NoteRepository
  private lateinit var fileRepository: FileRepository
  private lateinit var fileViewModel: FileViewModel
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    userRepository = mock(UserRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    userViewModel = UserViewModel(userRepository)
    noteRepository = mock(NoteRepository::class.java)
    noteViewModel = NoteViewModel(noteRepository)
    fileRepository = mock(FileRepository::class.java)
    fileViewModel = FileViewModel(fileRepository)

    // Mock the addUser method to call the onSuccess callback
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    val testUser = User("", "", "testUserName", "", "testUID", Timestamp.now(), 0.0)
    userViewModel.addUser(testUser, {}, {})

    // Mock the current route to be the user create screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_NOTE)
    composeTestRule.setContent {
      EditMarkdownScreen(navigationActions, noteViewModel, userViewModel, fileViewModel)
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
  fun clickComponents() {
    composeTestRule.onNodeWithTag("BoldControl").performClick()
    composeTestRule.onNodeWithTag("ItalicControl").performClick()
    composeTestRule.onNodeWithTag("UnderlinedControl").performClick()
    composeTestRule.onNodeWithTag("StrikethroughControl").performClick()
  }
}
