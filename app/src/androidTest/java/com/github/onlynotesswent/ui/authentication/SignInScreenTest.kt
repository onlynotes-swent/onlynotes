package com.github.onlynotesswent.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class SignInScreenTest : TestCase() {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    Intents.init()
    navigationActions = mock(NavigationActions::class.java)
    userViewModel = UserViewModel(mock(UserRepository::class.java))

    `when`(navigationActions.currentRoute()).thenReturn(Route.AUTH)
  }

  // Release Intents after each test
  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun componentsCorrectlyDisplayed() {
    composeTestRule.setContent { SignInScreen(navigationActions, userViewModel) }
    composeTestRule.onNodeWithTag("loginScreenScaffold").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginScreenColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginLogo").assertIsDisplayed()

    composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("Welcome To")

    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("googleLogo", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("loginButtonText", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("Sign in with Google")
  }

  @Test
  fun googleSignInReturnsValidActivityResult() {
    composeTestRule.setContent { SignInScreen(navigationActions, userViewModel) }
    composeTestRule.onNodeWithTag("loginButton").performClick()
    // assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
    intended(toPackage("com.google.android.gms"))
  }
}
