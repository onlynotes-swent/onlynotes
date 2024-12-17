package com.github.onlynotesswent.ui.authentication

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.github.onlynotesswent.OnlyNotes
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.authentication.Authenticator
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    authenticator: Authenticator
) {
  val context = LocalContext.current

  // AUTHENTICATION:
  val scope = rememberCoroutineScope()

  val onClickSignIn: () -> Unit = {
    scope.launch {
      authenticator.googleSignIn(
          onSuccess = { authSuccessHandler(it, navigationActions, userViewModel, context) },
          onFailure = { Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show() })
    }
  }

  // Handle back button press
  BackHandler {
    // Exit the app
    ActivityCompat.finishAffinity(context as OnlyNotes)
  }

  // UI:
  Scaffold(modifier = Modifier.fillMaxSize().testTag("loginScreenScaffold")) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).testTag("loginScreenColumn"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      WelcomeText()
      Logo()
      Spacer(modifier = Modifier.height(120.dp))
      SignInButton(onClick = { onClickSignIn() })
    }
  }
}

/**
 * Handles the success of the authentication process.
 *
 * @param result The AuthResult object returned by the authentication process.
 * @param navigationActions The NavigationActions object to use for navigation.
 * @param userViewModel The UserViewModel object to use for fetching the user.
 * @param context The context to use for displaying toasts.
 */
internal fun authSuccessHandler(
    result: AuthResult,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    context: Context
) {
  userViewModel.getCurrentUserByEmail(
      email = result.user!!.email!!,
      onSuccess = { navigationActions.navigateTo(TopLevelDestinations.OVERVIEW) },
      onUserNotFound = { navigationActions.navigateTo(Screen.CREATE_USER) },
      onFailure = { Toast.makeText(context, "Error fetching user", Toast.LENGTH_LONG).show() })
}

// UI:
@Composable
internal fun Logo() {
  Image(
      modifier = Modifier.width(384.dp).height(144.dp).testTag("loginLogo"),
      painter = painterResource(id = R.drawable.only_notes_logo),
      contentDescription = "image description",
      contentScale = ContentScale.FillBounds)
}

@Composable
internal fun WelcomeText() {
  Text(
      modifier = Modifier.height(65.dp).testTag("loginTitle"),
      text = stringResource(R.string.welcome_to),
      style =
          TextStyle(
              fontSize = 57.sp,
              lineHeight = 64.sp,
              fontWeight = FontWeight(400),
              color = MaterialTheme.colorScheme.onBackground,
              textAlign = TextAlign.Center,
          ))
}

@Composable
internal fun SignInButton(onClick: () -> Unit) {
  OutlinedButton(
      onClick = onClick,
      shape = RoundedCornerShape(50),
      modifier = Modifier.padding(start = 16.dp, end = 16.dp).testTag("loginButton"),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Image(
        modifier = Modifier.width(24.dp).height(24.dp).testTag("googleLogo"),
        painter = painterResource(id = R.drawable.google_logo),
        contentDescription = "google logo",
        contentScale = ContentScale.FillBounds)
    Text(
        modifier = Modifier.padding(6.dp).testTag("loginButtonText"),
        text = stringResource(R.string.sign_in_with_google),
        style =
            TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight(400),
                color = MaterialTheme.colorScheme.onSurface))
  }
}
