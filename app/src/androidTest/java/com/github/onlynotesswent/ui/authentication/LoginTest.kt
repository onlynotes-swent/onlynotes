package com.github.onlynotesswent.ui.authentication

// ***************************************************************************** //
// ***                                                                       *** //
// *** THIS FILE WILL BE OVERWRITTEN DURING GRADING. IT SHOULD BE LOCATED IN *** //
// *** `app/src/androidTest/java/com/github/se/bootcamp/ui/authentication/`.    *** //
// *** DO **NOT** IMPLEMENT YOUR OWN TESTS IN THIS FILE                      *** //
// ***                                                                       *** //
// ***************************************************************************** //

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.onlynotesswent.MainActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest : TestCase() {
    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    // The IntentsTestRule is not reliable.

    @Before
    fun setUp() {
        Intents.init()
    }

    // Release Intents after each test
    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun titleAndButtonAreCorrectlyDisplayed() {
        composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("Welcome To")

        composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
    }

    @Test
    fun googleSignInReturnsValidActivityResult() {
        composeTestRule.onNodeWithTag("loginButton").performClick()
        composeTestRule.waitForIdle()
        // assert that an Intent resolving to Google Mobile Services has been sent (for sign-in)
        intended(toPackage("com.google.android.gms"))
    }
}
