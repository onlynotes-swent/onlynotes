package com.github.onlynotesswent.ui.authentication

import android.content.res.Resources
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.onlynotesswent.OnlyNotes
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.authentication.Authenticator
import com.github.onlynotesswent.model.user.UserRepository
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {
  @get:Rule val activityRule = createAndroidComposeRule<OnlyNotes>()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel
  private lateinit var mockResources: Resources

  private lateinit var mockAuthenticator: Authenticator

  @Before
  fun setUp() {
    // Mock NavigationActions and UserViewModel
    navigationActions = mock(NavigationActions::class.java)
    userViewModel = UserViewModel(mock(UserRepository::class.java))

    // Mock Resources to return a test client ID for getString
    mockResources = mock(Resources::class.java)
    `when`(mockResources.getString(R.string.default_web_client_id)).thenReturn("test-client-id")

    mockAuthenticator = mock(Authenticator::class.java)

    // Mock the current route to ensure it's on the AUTH route
    `when`(navigationActions.currentRoute()).thenReturn(Route.AUTH)

    // Set the SignInScreen content in the OnlyNotes activity
    activityRule.activity.setContent {
      SignInScreen(navigationActions, userViewModel, mockAuthenticator)
    }
  }

  @Test
  fun componentsCorrectlyDisplayed() {
    activityRule.onNodeWithTag("loginScreenScaffold").assertIsDisplayed()
    activityRule.onNodeWithTag("loginScreenColumn").assertIsDisplayed()

    activityRule.onNodeWithTag("loginLogo").assertIsDisplayed()

    activityRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    activityRule.onNodeWithTag("loginTitle").assertTextEquals("Welcome To")

    activityRule.onNodeWithTag("loginButton").assertIsDisplayed()
    activityRule.onNodeWithTag("loginButton").assertHasClickAction()
    activityRule.onNodeWithTag("googleLogo", useUnmergedTree = true).assertIsDisplayed()
    activityRule
        .onNodeWithTag("loginButtonText", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("Sign in with Google")
  }

  @Test
  fun backButtonPressClosesApp() {
    // Simulate back button press
    activityRule.activity.onBackPressedDispatcher.onBackPressed()

    // Verify the expected behavior, e.g., the app should exit
    // This can be done by checking if the activity is finishing
    assert(activityRule.activity.isFinishing)
  }
}
