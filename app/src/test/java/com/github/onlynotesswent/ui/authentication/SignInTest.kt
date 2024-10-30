package com.github.onlynotesswent.ui.authentication

import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class SignInTest {
  @Test
  fun authSuccessHandler_navigatesToOverview() {
    val navigationActions = mock(NavigationActions::class.java)
    val userViewModel = mock(UserViewModel::class.java)
    val authResult = mock(AuthResult::class.java)
    val firebaseUser = mock(FirebaseUser::class.java)

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

    var messageShown = ""
    // Call the function
    authSuccessHandler(authResult, navigationActions, userViewModel) { message ->
      messageShown = message
    }
    // Verify the message and navigation actions
    assert(messageShown == "Welcome ${user.userName}!")
    verify(navigationActions).navigateTo(TopLevelDestinations.OVERVIEW)
    verify(userViewModel).getCurrentUserByEmail(anyString(), any(), any(), any())
  }

  @Test
  fun authSuccessHandler_navigatesToCreateUserScreen() {
    val navigationActions = mock(NavigationActions::class.java)
    val userViewModel = mock(UserViewModel::class.java)
    val authResult = mock(AuthResult::class.java)
    val firebaseUser = mock(FirebaseUser::class.java)

    val user = User("First name", "Last name", "username", "email", "uid", Timestamp.now(), 0.0)

    `when`(authResult.user).thenReturn(firebaseUser)
    `when`(firebaseUser.email).thenReturn(user.email)

    `when`(userViewModel.getCurrentUserByEmail(anyString(), any(), any(), any())).thenAnswer {
      val onNotFound: () -> Unit = it.arguments[2] as () -> Unit
      onNotFound()
    }

    var messageShown = ""
    // Call the function
    authSuccessHandler(authResult, navigationActions, userViewModel) { message ->
      messageShown = message
    }
    // Verify the message and navigation actions
    assert(messageShown == "Welcome to OnlyNotes!")
    verify(navigationActions).navigateTo(Screen.CREATE_USER)
    verify(userViewModel).getCurrentUserByEmail(anyString(), any(), any(), any())
  }

  @Test
  fun authSuccessHandler_catches_exception() {
    val navigationActions = mock(NavigationActions::class.java)
    val userViewModel = mock(UserViewModel::class.java)
    val authResult = mock(AuthResult::class.java)
    val firebaseUser = mock(FirebaseUser::class.java)

    val user = User("First name", "Last name", "username", "email", "uid", Timestamp.now(), 0.0)

    `when`(authResult.user).thenReturn(firebaseUser)
    `when`(firebaseUser.email).thenReturn(user.email)

    `when`(userViewModel.getCurrentUserByEmail(anyString(), any(), any(), any())).thenAnswer {
      val onFailure: (Exception) -> Unit = it.arguments[3] as (Exception) -> Unit
      onFailure(Exception("TestError"))
    }

    var messageShown = ""
    // Call the function
    authSuccessHandler(authResult, navigationActions, userViewModel) { message ->
      messageShown = message
    }
    // Verify the message
    assertEquals(messageShown, "Error while fetching user: ${Exception("TestError")}")
    verify(userViewModel).getCurrentUserByEmail(anyString(), any(), any(), any())
  }

  @Test
  fun authSuccessHandler_catches_null_exceptions() {
    val navigationActions = mock(NavigationActions::class.java)
    val userViewModel = mock(UserViewModel::class.java)
    val authResult = mock(AuthResult::class.java)
    val firebaseUser = mock(FirebaseUser::class.java)

    // -----------------------------------------
    // Test the case where the email is null
    `when`(authResult.user).thenReturn(firebaseUser)
    `when`(firebaseUser.email).thenReturn(null)

    var messageShown = ""
    // Call the function
    authSuccessHandler(authResult, navigationActions, userViewModel) { message ->
      messageShown = message
    }
    // Verify the message
    assertEquals(messageShown, "Login Failed!")

    // -----------------------------------------
    // Test the case where the user is null
    `when`(authResult.user).thenReturn(null)
    messageShown = ""
    // Call the function
    authSuccessHandler(authResult, navigationActions, userViewModel) { message ->
      messageShown = message
    }
    // Verify the message
    assertEquals(messageShown, "Login Failed!")
  }
}
