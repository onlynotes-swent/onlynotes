package com.github.onlynotesswent.ui.authentication

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SignInScreen(navigationActions: NavigationActions) {
    Column(
        modifier = Modifier.padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // AUTHENTICATION:
        var user by remember { mutableStateOf(Firebase.auth.currentUser) }

        val context = LocalContext.current
        val launcher =
            rememberFirebaseAuthLauncher(
                onAuthComplete = { result ->
                    Toast.makeText(context, "Welcome ${result.user?.displayName}", Toast.LENGTH_LONG)
                        .show()
                    user = result.user

                    //TODO: add user profile fetching using
                    // profile = userViewModel.fetchUserProfile(user.mail)

                    //TODO: change later to navigate to profile creation screen
                    navigationActions.navigateTo("overview")
                },
                onAuthError = { user = null })
        val token = stringResource(R.string.default_web_client_id)

        // UI pre login:
        WelcomeText()
        Logo()
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

@Composable
private fun Logo() {
    Image(
        modifier = Modifier.width(384.dp).height(144.dp),
        painter = painterResource(id = R.drawable.only_notes_logo2),
        contentDescription = "image description",
        contentScale = ContentScale.FillBounds)
}

@Composable
private fun WelcomeText() {
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
private fun SignInButton(
    onClick: () -> Unit,
    buttonText: String = "Sign in with Google",
    testLabel: String = "loginButton"
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp).testTag(testLabel),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Image(
            modifier = Modifier.width(24.dp).height(24.dp),
            painter = painterResource(id = R.drawable.google_logo),
            contentDescription = "google logo",
            contentScale = ContentScale.FillBounds)
        Text(
            modifier = Modifier.padding(6.dp),
            text = buttonText,
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
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (Exception) -> Unit // Catch all types of exceptions
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let {
                val credential = GoogleAuthProvider.getCredential(it.idToken!!, null)
                scope.launch {
                    try {
                        val authResult = Firebase.auth.signInWithCredential(credential).await()
                        onAuthComplete(authResult)
                    } catch (e: Exception) {
                        onAuthError(e)
                    }
                }
            }
        } catch (e: ApiException) {
            onAuthError(e)
        }
    }
}
