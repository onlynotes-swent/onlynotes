package com.github.onlynotesswent.model.authentication

import android.content.Context
import android.content.Intent
import android.provider.Settings.ACTION_ADD_ACCOUNT
import android.provider.Settings.EXTRA_ACCOUNT_TYPES
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.github.onlynotesswent.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Handles the authentication process for the app, using android's Credential Manager.
 * Authentication is done using Firebase Auth, with the different credentials supported. Currently
 * supported credentials are:
 * - Sign-in with Google.
 *
 * @param ctx The context from the activity that uses the Authenticator
 * @param credentialManager The CredentialManager instance to use
 */
class Authenticator(
    private val ctx: Context,
    private val credentialManager: CredentialManager = CredentialManager.create(ctx)
) {
  // The server client ID for the app
  private val serverClientId = ctx.getString(R.string.default_web_client_id)

  /**
   * Signs in using Google credentials
   *
   * @param onSuccess The function to call on successful sign-in, that handles the AuthResult
   * @param onFailure The function to call on failed sign-in
   * @param coroutineScope The coroutine scope to use for the sign-in request
   */
  fun googleSignIn(
      coroutineScope: CoroutineScope,
      onSuccess: (AuthResult) -> Unit,
      onFailure: (Exception) -> Unit,
  ) {
    val googleIdOption: GetGoogleIdOption =
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Check for all google accounts on device
            .setServerClientId(serverClientId) // Server client ID
            .setNonce(generateNonce()) // For security
            .setAutoSelectEnabled(true) // If only one valid and already used account, auto-select
            .build()

    val request: GetCredentialRequest =
        GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    coroutineScope.launch {
      try {
        // Attempt to get the google credentials from CredentialManager
        val result = credentialManager.getCredential(context = ctx, request = request)
        handleSignIn(result, onSuccess, onFailure)
      } catch (e: NoCredentialException) {
        // If there are no credentials, prompt the user to add a Google account to the phone
        val intent = Intent(ACTION_ADD_ACCOUNT)
        intent.putExtra(EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
        ctx.startActivity(intent)
      } catch (e: GetCredentialException) {
        Log.e(TAG, "Error getting credential", e)
        onFailure(e)
      }
    }
  }

  /**
   * Signs out the user from Firebase and from the Credential Manager
   *
   * @param coroutineScope The coroutine scope to use for the sign-out request
   */
  fun signOut(coroutineScope: CoroutineScope) {
    Firebase.auth.signOut()
    coroutineScope.launch { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
  }

  /**
   * Handles a successful sign-in response
   *
   * @param result The GetCredentialResponse containing the credential
   * @param onSuccess The function to call on successful sign-in, that handles the AuthResult
   * @param onFailure The function to call on failed sign-in
   */
  internal suspend fun handleSignIn(
      result: GetCredentialResponse,
      onSuccess: (AuthResult) -> Unit,
      onFailure: (Exception) -> Unit,
  ) {
    // Retrieve the credential from the response
    when (val credential = result.credential) {
      // Can easily add support for other types of credentials
      is CustomCredential -> {
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          try {
            // Use googleIdTokenCredential and extract the ID to validate and authenticate on the
            // server
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            // Use google credential to sign in to Firebase
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()

            // Verify the user is correctly retrieved
            if (authResult.user != null && authResult.user?.email != null) {
              onSuccess(authResult)
            } else {
              Log.e(TAG, "User incorrectly retrieved from Firebase")
              onFailure(Exception("Login failed"))
            }
          } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Received an invalid google id token response", e)
            onFailure(Exception("Login failed"))
          } catch (e: Exception) {
            Log.e(TAG, "Error signing in with Firebase", e)
            onFailure(Exception("Login failed"))
          }
        } else {
          Log.e(TAG, "Unexpected type of credential: ${credential.type}")
          onFailure(Exception("Login failed"))
        }
      }
      else -> {
        Log.e(TAG, "Unexpected type of credential: ${credential.type}")
        onFailure(Exception("Login failed"))
      }
    }
  }

  companion object {
    private const val TAG = "GoogleCredSignIn"

    /**
     * Generate a nonce for increased security when using credentials
     *
     * @return A hashed nonce
     */
    internal fun generateNonce(): String {
      val ranNonce: String = UUID.randomUUID().toString()
      val bytes: ByteArray = ranNonce.toByteArray()
      val shaDigest: ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)
      val hashedNonce: String = shaDigest.joinToString("") { "%02x".format(it) }
      return hashedNonce
    }
  }
}
