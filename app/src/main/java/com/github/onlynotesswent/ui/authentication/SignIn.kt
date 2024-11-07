package com.github.onlynotesswent.ui.authentication

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.authentication.GoogleCredSignIn
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SignInScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    serverClientId: String
) {

  // AUTHENTICATION:
  val context = LocalContext.current as? AppCompatActivity
  if (context == null) {
    // Don not display screen if context isn not an AppCompatActivity
    Log.e("SignInScreen", "Context is not an AppCompatActivity")
    return
  }

  val googleSignIn = GoogleCredSignIn(context, serverClientId)

  // UI:
  Scaffold(modifier = Modifier.fillMaxSize().testTag("loginScreenScaffold")) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).testTag("loginScreenColumn"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      WelcomeText()
      Logo()
      Spacer(modifier = Modifier.height(80.dp))
      SignInButton(
          onClick = {
            googleSignIn.googleLogin {
              // Get the ID token from the credential and sign in to Firebase
              val idToken = idToken
              val credential = GoogleAuthProvider.getCredential(idToken, null)
              signInWithFirebase(credential, navigationActions, userViewModel, context)
            }
          })
    }
  }
}

private fun signInWithFirebase(
    credential: AuthCredential,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    context: Context
) {
  CoroutineScope(Dispatchers.Main).launch {
    try {
      val authResult: AuthResult = Firebase.auth.signInWithCredential(credential).await()
      authSuccessHandler(authResult, navigationActions, userViewModel) { s ->
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
      }
    } catch (e: Exception) {
      Log.e("SignInScreen", "Firebase sign-in failed", e)
      Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
    }
  }
}

internal fun authSuccessHandler(
    result: AuthResult,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    showMessage: (String) -> Unit
) {
  if (result.user == null || result.user?.email == null) {
    showMessage("Login Failed!")
    return
  }
  userViewModel.getCurrentUserByEmail(
      result.user!!.email!!,
      onSuccess = { user ->
        showMessage("Welcome ${user.userName}!")
        navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
      },
      onUserNotFound = {
        showMessage("Welcome to OnlyNotes!")
        navigationActions.navigateTo(Screen.CREATE_USER)
      },
      { showMessage("Error while fetching user: $it") })
}

@Composable
internal fun Logo() {
  Image(
      modifier = Modifier.width(384.dp).height(144.dp).testTag("loginLogo"),
      painter = painterResource(id = R.drawable.only_notes_logo2),
      contentDescription = "image description",
      contentScale = ContentScale.FillBounds)
}

@Composable
internal fun WelcomeText() {
  Text(
      modifier = Modifier.height(65.dp).testTag("loginTitle"),
      text = "Welcome To",
      style =
          TextStyle(
              fontSize = 57.sp,
              lineHeight = 64.sp,
              fontWeight = FontWeight(400),
              color = Color(0xFF191C1E),
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
        text = "Sign in with Google",
        style =
            TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF191C1E),
            ))
  }
}
