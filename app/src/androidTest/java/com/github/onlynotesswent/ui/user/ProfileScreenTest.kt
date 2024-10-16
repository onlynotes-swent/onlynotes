package com.github.onlynotesswent.ui.user

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class ProfileScreenTest {
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
    `when`(navigationActions.currentRoute()).thenReturn(Screen.PROFILE)

    `when`(userRepository.updateUser(any(), any(), any())).thenAnswer {
      val user = it.arguments[0] as User
      val onSuccess = it.arguments[1] as () -> Unit
      val onFailure = it.arguments[2] as (Exception) -> Unit

      if (user.userName == existingUserName) {
        onFailure(UserRepositoryFirestore.UsernameTakenException())
      } else {
        onSuccess()
      }
    }

    // Mock the current user to be the test user
    userViewModel.setCurrentUser(testUser)
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { ProfileScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("ProfileScreen").assertExists()
    composeTestRule.onNodeWithTag("goBackButton").assertExists()
    composeTestRule.onNodeWithTag("inputFirstName").assertExists()
    composeTestRule.onNodeWithTag("inputLastName").assertExists()
    composeTestRule.onNodeWithTag("inputUserName").assertExists()
    composeTestRule.onNodeWithTag("modifyUserButton").assertExists()
  }

  private fun hasError(): SemanticsMatcher {
    return SemanticsMatcher.expectValue(SemanticsProperties.Error, "Invalid input")
  }

  // test that submit does navigate to the overview screen
  @Test
  fun submitNavigatesToOverview() {
    composeTestRule.setContent { ProfileScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("modifyUserButton").performClick()
    verify(navigationActions).navigateTo(Screen.OVERVIEW)
  }
}
