package com.github.onlynotesswent.ui.authentication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.authentication.GoogleCredSignIn
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
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
  val context = LocalContext.current
  val credentialManager = CredentialManager.create(context)

  // Launcher for fallback method
  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthComplete = { result ->
            authSuccessHandler(result, navigationActions, userViewModel, context)
          },
          onAuthError = { e ->
            Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
            Log.e("SignInScreen", "Failed to sign in: ${e.statusCode}")
          })

  val googleSignIn = GoogleCredSignIn(context, credentialManager, serverClientId, launcher)

  val onClickSignIn: () -> Unit = {
    googleSignIn.googleLogin { googleIdToken ->
      // Sign in to Firebase with the Google ID token
      val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
      signInWithFirebase(firebaseCredential, navigationActions, userViewModel, context)
    }
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

// AUTHENTICATION:
internal fun signInWithFirebase(
    credential: AuthCredential,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    context: Context
) {
  CoroutineScope(Dispatchers.Main).launch {
    try {
      val authResult: AuthResult = Firebase.auth.signInWithCredential(credential).await()
      authSuccessHandler(authResult, navigationActions, userViewModel, context)
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
    context: Context
) {
  if (result.user == null || result.user?.email == null) {
    Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
    return
  }
  userViewModel.getCurrentUserByEmail(
      email = result.user!!.email!!,
      onSuccess = { user ->
        Toast.makeText(context, "Welcome ${user.userName}!", Toast.LENGTH_LONG).show()
        navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
      },
      onUserNotFound = {
        Toast.makeText(context, "Welcome to OnlyNotes!", Toast.LENGTH_LONG).show()
        navigationActions.navigateTo(Screen.CREATE_USER)
      },
      onFailure = {
        Toast.makeText(context, "Error while fetching user: $it", Toast.LENGTH_LONG).show()
      })
}

@Composable
internal fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
  val scope = rememberCoroutineScope()
  return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      result ->
    activityResultHandler(result, scope, onAuthComplete, onAuthError)
  }
}

internal fun activityResultHandler(
    result: ActivityResult,
    scope: CoroutineScope,
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
) {
  val task = result.data?.let { GoogleSignIn.getSignedInAccountFromIntent(it) }
  try {
    val account = task?.getResult(ApiException::class.java)!!
    val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
    scope.launch {
      val authResult = Firebase.auth.signInWithCredential(credential).await()
      onAuthComplete(authResult)
    }
  } catch (e: ApiException) {
    onAuthError(e)
  }
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
      text = "Welcome To",
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
        text = "Sign in with Google",
        style =
            TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight(400),
                color = MaterialTheme.colorScheme.onSurface))
  }
}
