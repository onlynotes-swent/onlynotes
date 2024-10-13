package com.github.onlynotesswent.ui.authentication

import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SignInTest {
  @Test
  fun authSuccessHandler_navigatesToOverview() {
    val navigationActions = mock(NavigationActions::class.java)
    val authResult = mock(AuthResult::class.java)
    val user = mock(FirebaseUser::class.java)

    `when`(authResult.user).thenReturn(user)
    `when`(user.displayName).thenReturn("Test User")

    var messageShown = ""
    // Call the function
    authSuccessHandler(authResult, navigationActions) { message -> messageShown = message }
    // Verify the message and navigation actions
    assert(messageShown == "Welcome Test User")
  }
}
