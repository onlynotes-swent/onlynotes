package com.github.onlynotesswent.model.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.NoCredentialException
import com.github.onlynotesswent.model.authentication.Authenticator.Companion.generateNonce
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class AuthenticatorTest {

  @Mock private lateinit var mockContext: Context
  @Mock private lateinit var mockCredentialManager: CredentialManager
  @Mock private lateinit var mockGetCredentialResponse: GetCredentialResponse
  @Mock private lateinit var mockCustomCredential: CustomCredential
  @Mock private lateinit var mockFirebaseAuth: FirebaseAuth

  private lateinit var authenticator: Authenticator

  private val serverClientId = "mock_client_id"

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    `when`(mockContext.getString(any())).thenReturn(serverClientId)

    authenticator = Authenticator(mockContext, mockCredentialManager)

    `when`(mockGetCredentialResponse.credential).thenReturn(mockCustomCredential)
    `when`(mockCustomCredential.type)
        .thenReturn(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)

    FirebaseApp.initializeApp(org.robolectric.RuntimeEnvironment.getApplication())
  }

  @Test
  fun `googleSignIn should invoke onFailure when no credentials are found`() = runTest {
    doAnswer { throw NoCredentialException() }
        .`when`(mockCredentialManager)
        .getCredential(any(), any<GetCredentialRequest>())

    authenticator.googleSignIn(
        // coroutineScope = this,
        onSuccess = {},
        onFailure = {})
    verify(mockContext).startActivity(any<Intent>())
  }

  @Test
  fun `googleSignIn should invoke onFailure when credential retrieval fails`() = runTest {
    doAnswer { throw GetCredentialUnknownException() }
        .`when`(mockCredentialManager)
        .getCredential(any(), any<GetCredentialRequest>())

    var failureCalled = false

    authenticator.googleSignIn(
        onSuccess = { fail("onSuccess should not be called") },
        onFailure = { exception ->
          failureCalled = true
          assertTrue(exception is GetCredentialException)
        })

    verifyErrorLog("Error getting credential")
    assertTrue(failureCalled)
  }

  @Test
  fun `googleSignIn initializes correct option`() = runTest {
    val getCredentialRequestCaptor = argumentCaptor<GetCredentialRequest>()

    // Mock the getCredential method to throw an exception, so we avoid going into handeSignIn
    doAnswer { throw GetCredentialUnknownException() }
        .`when`(mockCredentialManager)
        .getCredential(any(), any<GetCredentialRequest>())

    authenticator.googleSignIn({}, {})

    // Capture the request to verify the options, and retrieve the getGoogleIdOption used.
    verify(mockCredentialManager).getCredential(any(), getCredentialRequestCaptor.capture())

    val request = getCredentialRequestCaptor.firstValue

    assert(request.credentialOptions.isNotEmpty())
    val googleIdOption = request.credentialOptions[0] as GetGoogleIdOption
    assertFalse(googleIdOption.filterByAuthorizedAccounts)
    assertEquals(serverClientId, googleIdOption.serverClientId)
    assertNotNull(googleIdOption.nonce)
    assert(googleIdOption.autoSelectEnabled)
  }

  @Test
  fun `signOut should clear Firebase session and CredentialManager state`() = runTest {
    // Mock FirebaseAuth, to make signOut do Nothing
    val MockFirebaseAuth = mockStatic(FirebaseAuth::class.java)
    var firebaseSignedOut = false
    MockFirebaseAuth.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
        .thenReturn(mockFirebaseAuth)
    doAnswer { firebaseSignedOut = true }.`when`(mockFirebaseAuth).signOut()

    `when`(mockCredentialManager.clearCredentialState(any())).thenReturn(Unit)

    authenticator.signOut()

    assert(firebaseSignedOut)
    verify(mockCredentialManager).clearCredentialState(any())
    MockFirebaseAuth.close()
  }

  @Test
  fun `handleSignIn should log error when invalid response 2`() {
    // Arrange
    val invalidCredential =
        CustomCredential(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, Bundle())

    val getCredentialResponse = mock<GetCredentialResponse>()
    `when`(getCredentialResponse.credential).thenReturn(invalidCredential)

    runTest {
      var onFailureCalled = false
      authenticator.handleSignIn(getCredentialResponse, {}, { onFailureCalled = true })

      assert(onFailureCalled)

      verifyErrorLog("Received an invalid google id token response")
    }
  }

  @Test
  fun `handleSignIn should log error when invalid custom credential type`() = runTest {
    `when`(mockCustomCredential.type).thenReturn("INVALID_TYPE")
    var onFailureCalled = false
    authenticator.handleSignIn(mockGetCredentialResponse, {}, { onFailureCalled = true })

    assert(onFailureCalled)

    verifyErrorLog("Unexpected type of credential: INVALID_TYPE")
  }

  @Test
  fun `handleSignIn should log error when invalid credential type`() = runTest {
    val mockCredential = mock<Credential>()
    `when`(mockGetCredentialResponse.credential).thenReturn(mockCredential)
    // `when`(mockCredential.javaClass).thenReturn(Credential::class.java)

    `when`(mockCredential.type).thenReturn("INVALID_TYPE")
    var onFailureCalled = false
    authenticator.handleSignIn(mockGetCredentialResponse, {}, { onFailureCalled = true })

    assert(onFailureCalled)

    verifyErrorLog("Unexpected type of credential")
  }

  //  @Test
  //  fun `handleSignIn should log error signing in with Firebase`() = runTest {
  //    //var onFailureCalled = false
  //    //authenticator.handleSignIn(mockGetCredentialResponse, {}, { onFailureCalled = true })
  //
  //    val MockGoogleIdTokenCredential = (GoogleIdTokenCredential::class.java)
  //    val mockGoogleIdTokenCredential = mock<GoogleIdTokenCredential>()
  //    MockGoogleIdTokenCredential.`when`<GoogleIdTokenCredential> {
  // GoogleIdTokenCredential.createFrom(any()) }
  //        .thenReturn(mockGoogleIdTokenCredential)
  //
  ////    val MockGoogleAuthProvider = mockStatic(GoogleIdTokenCredential::class.java)
  ////    val mockGoogleIdTokenCredential = mock<GoogleIdTokenCredential>()
  ////    MockGoogleIdTokenCredential.`when`<GoogleIdTokenCredential> {
  // GoogleIdTokenCredential.createFrom(any()) }
  ////      .thenReturn(mockGoogleIdTokenCredential)
  //
  //    //assert(onFailureCalled)
  //
  //    verifyErrorLog("Received an invalid google id token response")
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

// package com.github.onlynotesswent.model.authentication
//
// import android.content.Context
// import android.content.Intent
// import android.os.Bundle
// import android.util.Log
// import androidx.credentials.CredentialManager
// import androidx.credentials.CustomCredential
// import androidx.credentials.GetCredentialRequest
// import androidx.credentials.GetCredentialResponse
// import androidx.credentials.exceptions.GetCredentialException
// import com.github.onlynotesswent.model.authentication.Authenticator.Companion.generateNonce
// import com.google.android.libraries.identity.googleid.GetGoogleIdOption
// import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
// import kotlinx.coroutines.test.*
// import org.junit.Assert.*
// import org.junit.Before
// import org.junit.Test
// import org.junit.runner.RunWith
// import org.mockito.Mock
// import org.mockito.Mockito.mock
// import org.mockito.Mockito.mockStatic
// import org.mockito.Mockito.spy
// import org.mockito.Mockito.`when`
// import org.mockito.MockitoAnnotations
// import org.mockito.kotlin.any
// import org.mockito.kotlin.eq
// import org.mockito.kotlin.verify
// import org.robolectric.RobolectricTestRunner
// import org.robolectric.shadows.ShadowLog
//
// @RunWith(RobolectricTestRunner::class)
// class AuthenticatorTest {
//  @Mock private lateinit var mockContext: Context
//  @Mock private lateinit var mockCredentialManager: CredentialManager
//
//  private val serverClientId = "test-server-client-id"
//  private lateinit var authenticator: Authenticator
//
//  @Before
//  fun setup() {
//    MockitoAnnotations.openMocks(this)
//    `when`(mockContext.getString(any())).thenReturn(serverClientId)
//    authenticator = Authenticator(mockContext, mockCredentialManager)
//  }
//
////  @Test
////    fun `googleSignIn initializes correct option`() {
////
////    val googleIdOption: GetGoogleIdOption =
////      GetGoogleIdOption.Builder()
////        .setFilterByAuthorizedAccounts(false) // Check for all google accounts on device
////        .setServerClientId(serverClientId) // Server client ID
////        .setNonce(generateNonce()) // For security
////        .setAutoSelectEnabled(true) // If only one valid and already used account, auto-select
////        .build()
////
////      val mockGetGoogleIdOption = spy(GetGoogleIdOption::class.java)
////      val GetGoogleIdOptionMock = mockStatic(GetGoogleIdOption::class.java)
////      val mockBuilder = spy(GetGoogleIdOption.Builder::class.java)
////
////      GetGoogleIdOptionMock.`when`<GetGoogleIdOption.Builder> { GetGoogleIdOption.Builder() }
////        .thenReturn(mockBuilder)
//////      `when`(mockBuilder.setFilterByAuthorizedAccounts(any())).thenReturn(mockBuilder)
//////        `when`(mockBuilder.setServerClientId(any())).thenReturn(mockBuilder)
//////        `when`(mockBuilder.setNonce(any())).thenReturn(mockBuilder)
//////        `when`(mockBuilder.setAutoSelectEnabled(any())).thenReturn(mockBuilder)
//////        `when`(mockBuilder.build()).thenReturn(mockGetGoogleIdOption)
////
////    authenticator.googleSignIn(TestScope(), {}, {})
////
////    verify(mockBuilder).setFilterByAuthorizedAccounts(false)
////    verify(mockBuilder).setServerClientId(serverClientId)
////    verify(mockBuilder).setNonce(any())
////    verify(mockBuilder).setAutoSelectEnabled(true)
////    verify(mockBuilder).build()
////
////      //val request = authenticator.googleSignIn(mockContext, mockCredentialManager)
////
////    }
//
//
////    @Test
////    fun `handleSignIn should log error when credential is invalid`() {
////        runTest {
////
////  //          doThrow(NoCredentialException())
////  //              .`when`(mockCredentialManager).getCredential(eq(mockContext),
////   any<GetCredentialRequest>())
////            `when`(mockCredentialManager.getCredential(eq(mockContext),
////   any<GetCredentialRequest>()))
////                .thenReturn({ GetCredentialException::class.java})
////              assertNull(authenticator.googleSignIn(this, {}, {}))
////  //          authenticator.googleSignIn(this, {}, {})
////              verify(mockContext).startActivity(any<Intent>())
////        }
////    }
//
//  //     @Test
//  //     fun `handleSignIn correct for Google Sign in`() {
//  //         // Arrange
//  //         val spyCredential =
//  //             CustomCredential(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
// Bundle())
//  //
//  //         val getCredentialResponse = mock(GetCredentialResponse::class.java)
//  //         `when`(getCredentialResponse.credential).thenReturn(spyCredential)
//  //
//  //         runTest {
//  //             val GoogleIdTokenCredentialMock =
//  // Mockito.mockStatic(GoogleIdTokenCredential::class.java)
//  //             val mockGoogleIdTokenCredential = mock(GoogleIdTokenCredential::class.java)
//  //
//  //             GoogleIdTokenCredentialMock.`when`<GoogleIdTokenCredential> {
//  //                 GoogleIdTokenCredential.createFrom(any())
//  //             }.thenReturn(mockGoogleIdTokenCredential)
//  //
//  //             val GoogleAuthProviderMock = Mockito.mockStatic(GoogleAuthProvider::class.java)
//  //             val mockAuthCredential = mock(AuthCredential::class.java)
//  //
//  //             GoogleAuthProviderMock.`when`<AuthCredential> {
//  //                 GoogleAuthProvider.getCredential(any(), anyOrNull())
//  //             }.thenReturn(mockAuthCredential)
//  //
//  ////             val FirebaseAuthMock = Mockito.mockStatic(FirebaseAuth::class.java)
//  ////             val mockAuthResult = mock(AuthResult::class.java)
//  ////
//  ////             FirebaseAuthMock.`when`<AuthResult> {
//  ////                 FirebaseAuth.(any(), anyOrNull())
//  ////             }.thenReturn(mockAuthResult)
//  //
//  //
//  //             authenticator.handleSignIn(getCredentialResponse, {}, {})
//  //
//  //             verify(mockGoogleIdTokenCredential).idToken
//  //
//  //             verifyErrorLog("Received an invalid google id token response")
//  //
//  //             GoogleAuthProviderMock.close()
//  //             GoogleIdTokenCredentialMock.close()
//  //         }
//  //     }
//
//  @Test
//  fun `handleSignIn should log error when invalid response`() {
//    // Arrange
//    val invalidCredential =
//        CustomCredential(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, Bundle())
//
//    val getCredentialResponse = mock(GetCredentialResponse::class.java)
//    `when`(getCredentialResponse.credential).thenReturn(invalidCredential)
//
//    runTest {
//      var onFailureCalled = false
//      authenticator.handleSignIn(getCredentialResponse, {}, { onFailureCalled = true })
//
//      assert(onFailureCalled)
//
//      verifyErrorLog("Received an invalid google id token response")
//    }
//  }
//
//  //  @Test
//  //  fun `handleSignIn should log error when unexpected type`() {
//  //    runTest {
//  //      val mockGetCredentialResponse = mock(GetCredentialResponse::class.java)
//  //
//  //      `when`(mockCredentialManager.getCredential(any(), any<GetCredentialRequest>()))
//  //          .thenReturn(mockGetCredentialResponse)
//  //
//  //        runTest {
//  //            var onFailureCalled = false
//  //            authenticator.handleSignIn(this, {}, {onFailureCalled = true})
//  //
//  //            assert(onFailureCalled)
//  //        }
//  //    }
//  //  }
//
//  @Test
//  fun `generateNonce should generate a hashed nonce`() {
//    // Act
//    val nonce1 = generateNonce()
//    val nonce2 = generateNonce()
//
//    // Assert
//    assertNotNull(nonce1)
//    assertNotNull(nonce2)
//    assertNotEquals(nonce1, nonce2)
//    assertEquals(64, nonce1.length) // SHA-256 hash length in hex is 64 characters
//    assertEquals(64, nonce2.length)
//  }
// }
//
