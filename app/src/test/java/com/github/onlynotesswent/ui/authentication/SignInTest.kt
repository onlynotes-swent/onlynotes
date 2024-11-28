package com.github.onlynotesswent.ui.authentication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowToast

@Suppress("UNCHECKED_CAST")
@RunWith(RobolectricTestRunner::class)
class SignInTest {
  @Mock private lateinit var context: Context

  private val navigationActions = mock(NavigationActions::class.java)
  private val userViewModel = mock(UserViewModel::class.java)
  private val authResult = mock(AuthResult::class.java)
  private val firebaseUser = mock(FirebaseUser::class.java)

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun authSuccessHandler_navigatesToOverview() {
    val user = User("First name", "Last name", "username", "email", "uid", Timestamp.now(), 0.0)

    `when`(authResult.user).thenReturn(firebaseUser)
    `when`(firebaseUser.email).thenReturn(user.email)

    `when`(userViewModel.getCurrentUserByEmail(anyString(), any(), any(), any())).thenAnswer {
      val email = it.arguments[0] as String
      val onSuccess: (User) -> Unit = it.arguments[1] as (User) -> Unit
      val onNotFound: () -> Unit = it.arguments[2] as () -> Unit
      if (email == "email") {
        onSuccess(user)
      } else {
        onNotFound()
      }
    }

    // Call the function
    authSuccessHandler(authResult, navigationActions, userViewModel, context)

    // Verify the message and navigation actions
    assertEquals("Welcome ${user.userName}!", ShadowToast.getTextOfLatestToast())
    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
    verify(userViewModel).getCurrentUserByEmail(anyString(), any(), any(), any())
  }

  @Test
  fun authSuccessHandler_navigatesToCreateUserScreen() {
    val user = User("First name", "Last name", "username", "email", "uid", Timestamp.now(), 0.0)

    `when`(authResult.user).thenReturn(firebaseUser)
    `when`(firebaseUser.email).thenReturn(user.email)

    `when`(userViewModel.getCurrentUserByEmail(anyString(), any(), any(), any())).thenAnswer {
      val onNotFound: () -> Unit = it.arguments[2] as () -> Unit
      onNotFound()
    }

    // Call the function
    authSuccessHandler(authResult, navigationActions, userViewModel, context)

    // Verify the message and navigation actions
    assertEquals("Welcome to OnlyNotes!", ShadowToast.getTextOfLatestToast())
    verify(navigationActions).navigateTo(Screen.CREATE_USER)
    verify(userViewModel).getCurrentUserByEmail(anyString(), any(), any(), any())
  }

  @Test
  fun authSuccessHandler_catches_exception() {
    val user = User("First name", "Last name", "username", "email", "uid", Timestamp.now(), 0.0)

    `when`(authResult.user).thenReturn(firebaseUser)
    `when`(firebaseUser.email).thenReturn(user.email)

    `when`(userViewModel.getCurrentUserByEmail(anyString(), any(), any(), any())).thenAnswer {
      val onFailure: (Exception) -> Unit = it.arguments[3] as (Exception) -> Unit
      onFailure(Exception("TestError"))
    }

    // Call the function
    authSuccessHandler(authResult, navigationActions, userViewModel, context)

    // Verify the message
    assertEquals(
        "Error while fetching user: ${Exception("TestError")}", ShadowToast.getTextOfLatestToast())
    verify(userViewModel).getCurrentUserByEmail(anyString(), any(), any(), any())
  }

  @Test
  fun authSuccessHandler_catches_null_exceptions() {
    // -----------------------------------------
    // Test the case where the email is null
    `when`(authResult.user).thenReturn(firebaseUser)
    `when`(firebaseUser.email).thenReturn(null)

    // Call the function
    authSuccessHandler(authResult, navigationActions, userViewModel, context)

    // Verify the message
    assertEquals("Login Failed!", ShadowToast.getTextOfLatestToast())

    // -----------------------------------------
    // Test the case where the user is null
    `when`(authResult.user).thenReturn(null)

    // Call the function
    authSuccessHandler(authResult, navigationActions, userViewModel, context)

    // Verify the message
    assertEquals("Login Failed!", ShadowToast.getTextOfLatestToast())
  }
}
