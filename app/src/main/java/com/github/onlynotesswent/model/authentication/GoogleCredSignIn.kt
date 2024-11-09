package com.github.onlynotesswent.model.authentication

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "GoogleCredSignIn"

/**
 * A class to handle Google sign-in and retrieve a Google ID token credential
 *
 * @param ctx The context to use for the sign-in request
 * @param credentialManager The CredentialManager instance to use
 * @param serverClientId The server client ID to use for the sign-in request
 */
class GoogleCredSignIn(
    private val ctx: Context,
    private val credentialManager: CredentialManager,
    private val serverClientId: String
) {
  // Instantiate a Google sign-in request
  private val googleIdOption: GetGoogleIdOption =
      GetGoogleIdOption.Builder()
          .setFilterByAuthorizedAccounts(false)
          .setServerClientId(serverClientId)
          .setAutoSelectEnabled(true) // enable automatic sign-in for returning users
          .setNonce(generateNonce()) // add a nonce for security
          .build()

  // Retrieve the credentials
  val request: GetCredentialRequest =
      GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

  /**
   * Handle the Google login process, and retrieve a credential
   *
   * @param callback The callback to handle the Google ID token credential
   * @throws Exception If the device has never had a Google account before
   */
  fun googleLogin(callback: (String) -> Unit) {
    // Retrieve user's available credentials
    CoroutineScope(Dispatchers.IO).launch {
      try {
        // Attempt to get the Google ID credential
        val result = credentialManager.getCredential(context = ctx, request = request)

        // Pass the result to handleSignIn with the callback
        handleSignIn(callback, result)
      } catch (e: Exception) {
        Log.e(TAG, "Error getting credential", e)
        // Exception can happen when the device has never had a Google account before
        // In this case, reattempt the sign-in using traditional Google sign-in
        throw (Exception("Error getting credential"))
      }
    }
  }

  /**
   * Handles a successful Google sign-in response
   *
   * @param callback The callback to handle the Google ID token credential
   * @param result The GetCredentialResponse containing the credential
   */
  private fun handleSignIn(callback: (String) -> Unit, result: GetCredentialResponse) {
    // Retrieve the credential from the response
    val credential = result.credential

    // Check if it's a Google ID token credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
      try {
        // Use googleIdTokenCredential and extract the ID to validate and authenticate on the server
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val googleIdToken = googleIdTokenCredential.idToken

        // Invoke the callback with the Google ID token
        callback(googleIdToken)
      } catch (e: GoogleIdTokenParsingException) {
        Log.e(TAG, "Received an invalid google id token response", e)
      }
    } else {
      // Catch any unrecognized custom credential type
      Log.e(TAG, "Unexpected type of credential")
    }
  }
}

/**
 * Generate a nonce for the Google sign-in request
 *
 * @return A hashed nonce
 */
fun generateNonce(): String {
  val ranNonce: String = UUID.randomUUID().toString()
  val bytes: ByteArray = ranNonce.toByteArray()
  val md: MessageDigest = MessageDigest.getInstance("SHA-256")
  val digest: ByteArray = md.digest(bytes)
  val hashedNonce: String = digest.fold("") { str, it -> str + "%02x".format(it) }
  return hashedNonce
}