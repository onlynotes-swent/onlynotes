package com.github.onlynotesswent.ui.overview

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
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
import com.github.onlynotesswent.utils.Scanner
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class AddNoteTest {
  @Mock private lateinit var mockUserRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockNoteRepository: NoteRepository
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var scanner: Scanner

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(mockUserRepository)
    noteViewModel = NoteViewModel(mockNoteRepository)
    scanner = Scanner(mock(ComponentActivity::class.java))

    // Mock the current route to be the add note screen
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.ADD_NOTE)
    // Mock the addUser method to call the onSuccess callback
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      AddNoteScreen(mockNavigationActions, scanner, noteViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("addNoteScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addNoteTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputNoteTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("visibilityButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("templateButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ClassNameTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ClassCodeTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ClassYearTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("visibilityMenu").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("templateMenu").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("visibilityButton").performClick()
    composeTestRule.onNodeWithTag("visibilityMenu").assertIsDisplayed()

    composeTestRule.onNodeWithTag("templateButton").performClick()
    composeTestRule.onNodeWithTag("templateMenu").assertIsDisplayed()
  }

  @Test
  fun clickGoBackButton() {
    composeTestRule.setContent {
      AddNoteScreen(mockNavigationActions, scanner, noteViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(mockNavigationActions).goBack()
    verify(mockNavigationActions, never()).navigateTo(Screen.OVERVIEW)
  }

  @Test
  fun doesNotSubmitWithoutTitleAndOptionsSelected() {
    composeTestRule.setContent {
      AddNoteScreen(mockNavigationActions, scanner, noteViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("createNoteButton").assertIsNotEnabled()

    composeTestRule.onNodeWithTag("inputNoteTitle").performTextInput("sample title")

    // Set the visibility dropdown
    composeTestRule.onNodeWithTag("visibilityButton").performClick()
    composeTestRule
        .onNodeWithTag("visibilityMenu")
        .onChildren()
        .filter(hasText("Public"))
        .onFirst()
        .performClick()
    // Set the template dropdown
    composeTestRule.onNodeWithTag("templateButton").performClick()
    composeTestRule
        .onNodeWithTag("templateMenu")
        .onChildren()
        .filter(hasText("Scan note"))
        .onFirst()
        .performClick()

    // Now the button should be enabled
    composeTestRule.onNodeWithTag("createNoteButton").assertIsEnabled()
  }

  @Test
  fun createNoteButtonTextChangesWhenScanNoteSelected() {
    composeTestRule.setContent {
      AddNoteScreen(mockNavigationActions, scanner, noteViewModel, userViewModel)
    }

    // Initially, the button text should be "Create Note"
    composeTestRule.onNodeWithTag("createNoteButton").assertTextEquals("Choose mode")

    // Set the template dropdown to "Scan Image"
    composeTestRule.onNodeWithTag("templateButton").performClick()
    composeTestRule
        .onNodeWithTag("templateMenu")
        .onChildren()
        .filter(hasText("Scan note"))
        .onFirst()
        .performClick()

    // Now the button text should be "Take Picture"
    composeTestRule.onNodeWithTag("createNoteButton").assertTextEquals("Scan note")
  }

  @Test
  fun createNoteButtonTextChangesWhenCreateNoteSelected() {
    composeTestRule.setContent {
      AddNoteScreen(mockNavigationActions, scanner, noteViewModel, userViewModel)
    }

    // Initially, the button text should be "Create Note"
    composeTestRule.onNodeWithTag("createNoteButton").assertTextEquals("Choose mode")

    // Set the template dropdown to "Create Note From Scratch"
    composeTestRule.onNodeWithTag("templateButton").performClick()
    composeTestRule
        .onNodeWithTag("templateMenu")
        .onChildren()
        .filter(hasText("Create note"))
        .onFirst()
        .performClick()

    // Now the button text should be "Create Note"
    composeTestRule.onNodeWithTag("createNoteButton").assertTextEquals("Create note")
  }

  @Test
  fun createNoteFromScratchButtonCorrect() {
    composeTestRule.setContent {
      AddNoteScreen(mockNavigationActions, scanner, noteViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("inputNoteTitle").performTextInput("test")

    // Set the template dropdown to "Private"
    composeTestRule.onNodeWithTag("visibilityButton").performClick()
    composeTestRule
        .onNodeWithTag("visibilityMenu")
        .onChildren()
        .filter(hasText("Private"))
        .onFirst()
        .performClick()

    // Set the template dropdown to "Create Note From Scratch"
    composeTestRule.onNodeWithTag("templateButton").performClick()
    composeTestRule
        .onNodeWithTag("templateMenu")
        .onChildren()
        .filter(hasText("Create note"))
        .onFirst()
        .performClick()

    // Now the button text should be "Create Note"
    composeTestRule.onNodeWithTag("createNoteButton").assertTextEquals("Create note")

    `when`(mockNoteRepository.getNewUid()).thenReturn("1")
    val testUser = User("", "", "username", "", "userID", Timestamp.now(), 0.0)

    userViewModel.addUser(testUser, {}, {})

    composeTestRule.onNodeWithTag("createNoteButton").performClick()
    verify(mockNoteRepository).addNote(any(), any<() -> Unit>(), any<(Exception) -> Unit>())
    verify(mockNavigationActions).navigateTo(Screen.EDIT_NOTE)
    verify(mockNoteRepository).getNewUid()
  }
}
