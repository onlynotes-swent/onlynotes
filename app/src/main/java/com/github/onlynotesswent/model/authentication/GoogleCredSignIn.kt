package com.github.onlynotesswent.model.authentication

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "GoogleCredSignIn"

class GoogleCredSignIn(private val ctx: Context, serverClientId: String) {
  private val credentialManager = CredentialManager.create(ctx)

  // GetGoogleIdOption used to retrieve a user's Google ID Token
  private val request =
      GetGoogleIdOption.Builder()
          .setFilterByAuthorizedAccounts(false)
          .setServerClientId(serverClientId)
          .setAutoSelectEnabled(true)
          .build()
          .let { GetCredentialRequest.Builder().addCredentialOption(it).build() }

  /**
   * Handle the Google login process, and retrieve a credential
   *
   * @param callback The callback to handle the Google ID token credential
   * @throws Exception If the context is not an AppCompatActivity
   */
  fun googleLogin(callback: GoogleIdTokenCredential.() -> Unit) {
    // Ensure the context is an AppCompatActivity to use required activity features
    if (ctx !is AppCompatActivity) {
      throw Exception("Please use Activity Context")
    }

    // Launch coroutine on the IO dispatcher for background processing
    CoroutineScope(Dispatchers.IO).launch {
      try {
        // Attempt to get the Google ID credential
        val result =
            credentialManager.getCredential(
                request = request,
                context = ctx,
            )

        // Pass the result to handleSignIn with the callback
        handleSignIn(callback, result)
      } catch (e: Exception) {
        Log.e(TAG, "Error getting credential", e)
      }
    }
  }

  /**
   * Handle the Google ID token credential from the GetCredentialResponse
   *
   * @param callback The callback to handle the Google ID token credential
   * @param result The GetCredentialResponse containing the credential
   */
  private fun handleSignIn(
      callback: GoogleIdTokenCredential.() -> Unit,
      result: GetCredentialResponse
  ) {
    // Retrieve the credential from the response
    val credential = result.credential

    // Check if it's a Google ID token credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
      try {
        // Parse Google ID token from the credential data
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

        // Invoke the callback with the Google ID token
        callback(googleIdTokenCredential)
      } catch (e: GoogleIdTokenParsingException) {
        Log.e(TAG, "Received an invalid google id token response", e)
      }
    } else {
      // Catch any unrecognized custom credential type
      Log.e(TAG, "Unexpected type of credential")
    }
  }
}
