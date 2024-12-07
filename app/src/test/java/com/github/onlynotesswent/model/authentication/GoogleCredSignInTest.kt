package com.github.onlynotesswent.model.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class GoogleCredSignInTest {
  private val context = mock(Context::class.java)
  private val credentialManager = mock(CredentialManager::class.java)
  private val launcher =
      mock(ManagedActivityResultLauncher::class.java)
          as ManagedActivityResultLauncher<Intent, ActivityResult>
  private val serverClientId = "test-server-client-id"
  private lateinit var googleCredSignIn: GoogleCredSignIn

  @Before
  fun setup() {
    googleCredSignIn = GoogleCredSignIn(context, credentialManager, serverClientId, launcher)
  }

  @Test
  fun `handleSignIn should log error when credential is invalid`() {
    // Arrange
    val invalidCredential = CustomCredential("invalid_type", Bundle())

    val getCredentialResponse = mock(GetCredentialResponse::class.java)
    `when`(getCredentialResponse.credential).thenReturn(invalidCredential)

    googleCredSignIn.handleSignIn({}, getCredentialResponse)

    // Get all the logs
    val logs = ShadowLog.getLogs()

    // Check for the debug log that should be generated
    val errorLog =
        logs.find {
          it.type == Log.ERROR &&
              it.tag == "GoogleCredSignIn" &&
              it.msg == "Unexpected type of credential: invalid_type"
        }
    assert(errorLog != null) { "Expected error log was not found!" }
  }

  @Test
  fun `handleSignIn should log error when invalid response`() {
    // Arrange
    val invalidCredential =
        CustomCredential(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, Bundle())

    val getCredentialResponse = mock(GetCredentialResponse::class.java)
    `when`(getCredentialResponse.credential).thenReturn(invalidCredential)

    googleCredSignIn.handleSignIn({}, getCredentialResponse)

    // Get all the logs
    val logs = ShadowLog.getLogs()

    // Check for the debug log that should be generated
    val errorLog =
        logs.find {
          it.type == Log.ERROR &&
              it.tag == "GoogleCredSignIn" &&
              it.msg == "Received an invalid google id token response"
        }
    assert(errorLog != null) { "Expected error log was not found!" }
  }

  @Test
  fun `handleSignIn should log error when unexpected type`() {
    runTest {
      val mockGetCredentialResponse = mock(GetCredentialResponse::class.java)

      `when`(credentialManager.getCredential(any(), any<GetCredentialRequest>()))
          .thenReturn(mockGetCredentialResponse)

      googleCredSignIn.googleLogin {}
    }
  }

  @Test
  fun `generateNonce should generate a hashed nonce`() {
    // Act
    val nonce1 = generateNonce()
    val nonce2 = generateNonce()

    // Assert
    assertNotNull(nonce1)
    assertNotNull(nonce2)
    assertNotEquals(nonce1, nonce2)
    assertEquals(64, nonce1.length) // SHA-256 hash length in hex is 64 characters
    assertEquals(64, nonce2.length)
  }
}
