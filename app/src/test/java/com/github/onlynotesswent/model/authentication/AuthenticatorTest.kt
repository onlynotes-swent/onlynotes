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
 import androidx.credentials.exceptions.GetCredentialException
 import androidx.credentials.exceptions.NoCredentialException
 import com.github.onlynotesswent.model.authentication.Authenticator.Companion.generateNonce
 import com.github.onlynotesswent.utils.Scanner
 import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
 import com.google.firebase.auth.AuthCredential
 import com.google.firebase.auth.AuthResult
 import com.google.firebase.auth.FirebaseAuth
 import com.google.firebase.auth.GoogleAuthProvider
 import kotlinx.coroutines.test.*
 import org.junit.Assert.*
 import org.junit.Before
 import org.junit.Test
 import org.junit.runner.RunWith
 import org.mockito.Mock
 import org.mockito.Mockito
 import org.mockito.Mockito.mock
 import org.mockito.Mockito.spy
 import org.mockito.Mockito.`when`
 import org.mockito.MockitoAnnotations
 import org.mockito.kotlin.any
 import org.mockito.kotlin.anyOrNull
 import org.mockito.kotlin.doThrow
 import org.mockito.kotlin.eq
 import org.mockito.kotlin.verify
 import org.robolectric.RobolectricTestRunner
 import org.robolectric.shadows.ShadowLog
 import kotlin.contracts.contract

 @RunWith(RobolectricTestRunner::class)
 class AuthenticatorTest {
  @Mock private lateinit var mockContext: Context
  @Mock private lateinit var mockCredentialManager: CredentialManager

  private val serverClientId = "test-server-client-id"
  private lateinit var authenticator: Authenticator

  @Before
  fun setup() {
      MockitoAnnotations.openMocks(this)
        `when`(mockContext.getString(any())).thenReturn(serverClientId)
    authenticator = Authenticator(mockContext, mockCredentialManager)
  }

//  @Test
//  fun `handleSignIn should log error when credential is invalid`() {
//      runTest {
//
////          doThrow(NoCredentialException())
////              .`when`(mockCredentialManager).getCredential(eq(mockContext), any<GetCredentialRequest>())
//          `when`(mockCredentialManager.getCredential(eq(mockContext), any<GetCredentialRequest>()))
//              .thenReturn({ GetCredentialException::class.java})
//            assertNull(authenticator.googleSignIn(this, {}, {}))
////          authenticator.googleSignIn(this, {}, {})
//            verify(mockContext).startActivity(any<Intent>())
//      }
//  }

//     @Test
//     fun `handleSignIn correct for Google Sign in`() {
//         // Arrange
//         val spyCredential =
//             CustomCredential(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, Bundle())
//
//         val getCredentialResponse = mock(GetCredentialResponse::class.java)
//         `when`(getCredentialResponse.credential).thenReturn(spyCredential)
//
//         runTest {
//             val GoogleIdTokenCredentialMock = Mockito.mockStatic(GoogleIdTokenCredential::class.java)
//             val mockGoogleIdTokenCredential = mock(GoogleIdTokenCredential::class.java)
//
//             GoogleIdTokenCredentialMock.`when`<GoogleIdTokenCredential> {
//                 GoogleIdTokenCredential.createFrom(any())
//             }.thenReturn(mockGoogleIdTokenCredential)
//
//             val GoogleAuthProviderMock = Mockito.mockStatic(GoogleAuthProvider::class.java)
//             val mockAuthCredential = mock(AuthCredential::class.java)
//
//             GoogleAuthProviderMock.`when`<AuthCredential> {
//                 GoogleAuthProvider.getCredential(any(), anyOrNull())
//             }.thenReturn(mockAuthCredential)
//
////             val FirebaseAuthMock = Mockito.mockStatic(FirebaseAuth::class.java)
////             val mockAuthResult = mock(AuthResult::class.java)
////
////             FirebaseAuthMock.`when`<AuthResult> {
////                 FirebaseAuth.(any(), anyOrNull())
////             }.thenReturn(mockAuthResult)
//
//
//             authenticator.handleSignIn(getCredentialResponse, {}, {})
//
//             verify(mockGoogleIdTokenCredential).idToken
//
//             verifyErrorLog("Received an invalid google id token response")
//
//             GoogleAuthProviderMock.close()
//             GoogleIdTokenCredentialMock.close()
//         }
//     }

  @Test
  fun `handleSignIn should log error when invalid response`() {
    // Arrange
    val invalidCredential =
        CustomCredential(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, Bundle())

    val getCredentialResponse = mock(GetCredentialResponse::class.java)
    `when`(getCredentialResponse.credential).thenReturn(invalidCredential)

      runTest {
          var onFailureCalled = false
          authenticator.handleSignIn(getCredentialResponse, {}, { onFailureCalled = true })

          assert(onFailureCalled)

          verifyErrorLog("Received an invalid google id token response")
      }
  }

//  @Test
//  fun `handleSignIn should log error when unexpected type`() {
//    runTest {
//      val mockGetCredentialResponse = mock(GetCredentialResponse::class.java)
//
//      `when`(mockCredentialManager.getCredential(any(), any<GetCredentialRequest>()))
//          .thenReturn(mockGetCredentialResponse)
//
//        runTest {
//            var onFailureCalled = false
//            authenticator.handleSignIn(this, {}, {onFailureCalled = true})
//
//            assert(onFailureCalled)
//        }
//    }
//  }

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

 private fun verifyErrorLog(msg: String) {
     // Get all the logs
     val logs = ShadowLog.getLogs()

     // Check for the debug log that should be generated
     val errorLog = logs.find { it.type == Log.ERROR && it.tag == Authenticator.TAG && it.msg == msg }
     assert(errorLog != null) { "Expected error log was not found!" }
 }
