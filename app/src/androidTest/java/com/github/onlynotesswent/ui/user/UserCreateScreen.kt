package com.github.onlynotesswent.ui.user

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserRepositoryFirestore
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class UserCreateScreenTest {
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var navigationActions: NavigationActions
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

  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    userRepository = mock(UserRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    userViewModel = UserViewModel(userRepository)

    // Mock the current route to be the user create screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.CREATE_USER)
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { CreateUserScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("loginLogo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addUserScreen").assertExists()
    composeTestRule.onNodeWithTag("goBackButton").assertExists()
    composeTestRule.onNodeWithTag("inputFirstName").assertExists()
    composeTestRule.onNodeWithTag("inputLastName").assertExists()
    composeTestRule.onNodeWithTag("inputUserName").assertExists()
    composeTestRule.onNodeWithTag("createUserButton").assertExists()
  }

  @Test
  fun doesNotSubmitWithoutUser() {
    composeTestRule.setContent { CreateUserScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("createUserButton").performClick()
    verify(userRepository, never()).addUser(any(), any(), any())
  }

  private fun hasError(): SemanticsMatcher {
    return SemanticsMatcher.expectValue(SemanticsProperties.Error, "Invalid input")
  }

  @Test
  fun doesNotSubmitAlreadyExistingUser() {
    `when`(userViewModel.getNewUid()).thenReturn(testUid)

    // Mock the repository to return a result indicating the username already exists
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
      onFailure(UserRepositoryFirestore.UsernameTakenException())
    }

    composeTestRule.setContent { CreateUserScreen(navigationActions, userViewModel) }

    // Act: Enter the existing username and attempt to create a user
    composeTestRule.onNodeWithTag("inputUserName").performTextInput(existingUserName)
    composeTestRule.onNodeWithTag("createUserButton").performClick()

    // Assert: Check that the error state is correctly shown
    composeTestRule
        .onNodeWithTag("inputUserName")
        .assertIsDisplayed() // Check if it's still visible
        .assert(hasError())
  }

  @Test
  fun goesToOverviewScreenOnSuccess() {
    // Mock the getNewUid method to return a consistent UID
    `when`(userViewModel.getNewUid()).thenReturn(testUid)

    // Mock the addUser method to call the onSuccess callback
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    composeTestRule.setContent { CreateUserScreen(navigationActions, userViewModel) }

    // Act: Enter the user data and create the user
    composeTestRule.onNodeWithTag("inputFirstName").performTextInput(testUser.firstName)
    composeTestRule.onNodeWithTag("inputLastName").performTextInput(testUser.lastName)
    composeTestRule.onNodeWithTag("inputUserName").performTextInput(testUser.userName)

    // Assert: Check that the button is enabled
    composeTestRule.onNodeWithTag("createUserButton").assertIsEnabled()

    // Act: Click the button
    composeTestRule.onNodeWithTag("createUserButton").performClick()

    // Assert: Check that the navigation action was called
    verify(navigationActions).navigateTo(Screen.OVERVIEW)
  }
}
