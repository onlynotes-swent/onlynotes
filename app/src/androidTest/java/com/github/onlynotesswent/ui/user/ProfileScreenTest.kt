package com.github.onlynotesswent.ui.user

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserRepositoryFirestore
import com.github.onlynotesswent.model.users.UserViewModel
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

class ProfileScreenTest {
  @Mock private lateinit var mockUserRepository: UserRepository
  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockNoteRepository: NoteRepository
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var userViewModel: UserViewModel
  private val testUid = "testUid123"
  private val testUser =
      User(
          firstName = "testFirstName",
          lastName = "testLastName",
          userName = "testUserName",
          email = "testEmail",
          uid = testUid,
          dateOfJoining = Timestamp.now(),
          rating = 0.0)
  private val existingUserName = "alreadyTakenUsername"

  @get:Rule val composeTestRule = createComposeRule()

  @Suppress("UNCHECKED_CAST")
  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(mockUserRepository)
    noteViewModel = NoteViewModel(mockNoteRepository)

    // Mock the current route to be the user create screen
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.PROFILE)

    // Mock add user to initialize current user
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as () -> Unit
      onSuccess()
    }
    // Initialize current user
    userViewModel.addUser(testUser, {}, {})

    // Mock update user to throw error when applicable, or update user
    `when`(mockUserRepository.updateUser(any(), any(), any())).thenAnswer {
      val user = it.arguments[0] as User
      val onSuccess = it.arguments[1] as () -> Unit
      val onFailure = it.arguments[2] as (Exception) -> Unit

      if (user.userName == existingUserName) {
        onFailure(UserRepositoryFirestore.UsernameTakenException())
      } else {
        onSuccess()
      }
    }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      ProfileScreen(mockNavigationActions, userViewModel)
    }

    composeTestRule.onNodeWithTag("ProfileScreen").assertExists()
    composeTestRule.onNodeWithTag("goBackButton").assertExists()
    composeTestRule.onNodeWithTag("inputFirstName").assertExists()
    composeTestRule.onNodeWithTag("inputLastName").assertExists()
    composeTestRule.onNodeWithTag("inputUserName").assertExists()
    composeTestRule.onNodeWithTag("saveButton").assertExists()
  }

  private fun hasError(): SemanticsMatcher {
    return SemanticsMatcher.expectValue(SemanticsProperties.Error, "Invalid input")
  }

  @Test
  fun submitNavigatesToOverview() {
    composeTestRule.setContent {
      ProfileScreen(mockNavigationActions, userViewModel)
    }

    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(mockNavigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
  }

  @Test
  fun modifyProfile() {
    composeTestRule.setContent {
      ProfileScreen(mockNavigationActions, userViewModel)
    }

    composeTestRule.onNodeWithTag("inputUserName").performTextClearance()
    composeTestRule.onNodeWithTag("inputUserName").performTextInput("newUserName")
    assert(userViewModel.currentUser.value?.userName == "testUserName")
    composeTestRule.onNodeWithTag("saveButton").performClick()
    assert(userViewModel.currentUser.value?.userName == "newUserName")

    composeTestRule.onNodeWithTag("inputFirstName").performTextClearance()
    composeTestRule.onNodeWithTag("inputFirstName").performTextInput("newFirstName")
    assert(userViewModel.currentUser.value?.firstName == "testFirstName")
    composeTestRule.onNodeWithTag("saveButton").performClick()
    assert(userViewModel.currentUser.value?.firstName == "newFirstName")

    composeTestRule.onNodeWithTag("inputLastName").performTextClearance()
    composeTestRule.onNodeWithTag("inputLastName").performTextInput("newLastName")
    assert(userViewModel.currentUser.value?.lastName == "testLastName")
    composeTestRule.onNodeWithTag("saveButton").performClick()
    assert(userViewModel.currentUser.value?.lastName == "newLastName")
  }

  @Test
  fun userNameFieldDisplaysError() {
    composeTestRule.setContent { ProfileScreen(mockNavigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("inputFirstName").performTextClearance()
    composeTestRule.onNodeWithTag("inputFirstName").performTextInput("newFirstName")

    composeTestRule.onNodeWithTag("inputUserName").performTextClearance()
    composeTestRule.onNodeWithTag("inputUserName").performTextInput(existingUserName)

    composeTestRule.onNodeWithTag("saveButton").performClick() // error occurs
    composeTestRule.onNodeWithTag("inputUserName").assert(hasError()) // error shown

    assert(userViewModel.currentUser.value?.userName == "testUserName") // did not change
    assert(userViewModel.currentUser.value?.firstName == "testFirstName") // did not change
  }

  @Test
  fun saveButtonDisabledWhenUserNameIsEmpty() {
    composeTestRule.setContent { ProfileScreen(mockNavigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("saveButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("inputUserName").performTextClearance()
    composeTestRule.onNodeWithTag("saveButton").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("inputUserName").performTextInput("test")
    composeTestRule.onNodeWithTag("saveButton").assertIsEnabled()
  }
}
