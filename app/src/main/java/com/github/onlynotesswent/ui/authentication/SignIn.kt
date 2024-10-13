package com.github.onlynotesswent.ui.authentication

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
import androidx.compose.ui.graphics.Color
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
import com.github.onlynotesswent.R
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SignInScreen(navigationActions: NavigationActions) {
  Scaffold(modifier = Modifier.fillMaxSize().testTag("loginScreenScaffold")) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).testTag("loginScreenColumn"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // AUTHENTICATION:
      val context = LocalContext.current
      val launcher =
          rememberFirebaseAuthLauncher(
              onAuthComplete = { result ->
                authSuccessHandler(result, navigationActions) { s ->
                  Toast.makeText(context, s, Toast.LENGTH_LONG).show()
                }
              },
              onAuthError = { e ->
                Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
                Log.e("SignInScreen", "Failed to sign in: ${e.statusCode}")
              })
      val token = stringResource(R.string.default_web_client_id)

      // UI:
      WelcomeText()
      Logo()
      Spacer(modifier = Modifier.height(80.dp))
      SignInButton(
          onClick = {
            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(token)
                    .requestEmail()
                    .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            launcher.launch(googleSignInClient.signInIntent)
          })
    }
  }
}

internal fun authSuccessHandler(
    result: AuthResult,
    navigationActions: NavigationActions,
    showMessage: (String) -> Unit
) {
  showMessage("Welcome ${result.user?.displayName}")
  navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
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
