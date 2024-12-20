// package com.github.onlynotesswent.model.authentication
//
// import android.content.Context
// import android.content.Intent
// import android.os.Bundle
// import android.util.Log
// import androidx.credentials.Credential
// import androidx.credentials.CredentialManager
// import androidx.credentials.CustomCredential
// import androidx.credentials.GetCredentialRequest
// import androidx.credentials.GetCredentialResponse
// import androidx.credentials.exceptions.GetCredentialCancellationException
// import androidx.credentials.exceptions.GetCredentialException
// import androidx.credentials.exceptions.GetCredentialUnknownException
// import androidx.credentials.exceptions.NoCredentialException
// import com.github.onlynotesswent.model.authentication.Authenticator.Companion.generateNonce
// import com.google.android.libraries.identity.googleid.GetGoogleIdOption
// import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
// import com.google.firebase.FirebaseApp
// import com.google.firebase.auth.FirebaseAuth
// import kotlinx.coroutines.test.runTest
// import org.junit.Assert.assertEquals
// import org.junit.Assert.assertFalse
// import org.junit.Assert.assertNotEquals
// import org.junit.Assert.assertNotNull
// import org.junit.Assert.assertTrue
// import org.junit.Assert.fail
// import org.junit.Before
// import org.junit.Test
// import org.junit.runner.RunWith
// import org.mockito.Mock
// import org.mockito.Mockito.mockStatic
// import org.mockito.Mockito.`when`
// import org.mockito.MockitoAnnotations
// import org.mockito.kotlin.any
// import org.mockito.kotlin.argumentCaptor
// import org.mockito.kotlin.doAnswer
// import org.mockito.kotlin.mock
// import org.mockito.kotlin.verify
// import org.robolectric.RobolectricTestRunner
// import org.robolectric.shadows.ShadowLog
//
// @RunWith(RobolectricTestRunner::class)
// class AuthenticatorTest {
//
//  @Mock private lateinit var mockContext: Context
//  @Mock private lateinit var mockCredentialManager: CredentialManager
//  @Mock private lateinit var mockGetCredentialResponse: GetCredentialResponse
//  @Mock private lateinit var mockCustomCredential: CustomCredential
//  @Mock private lateinit var mockFirebaseAuth: FirebaseAuth
//
//  private lateinit var authenticator: Authenticator
//
//  private val serverClientId = "mock_client_id"
//
//  @Before
//  fun setup() {
//    MockitoAnnotations.openMocks(this)
//
//    `when`(mockContext.getString(any())).thenReturn(serverClientId)
//
//    authenticator = Authenticator(mockContext, mockCredentialManager)
//
//    `when`(mockGetCredentialResponse.credential).thenReturn(mockCustomCredential)
//    `when`(mockCustomCredential.type)
//        .thenReturn(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
//
//    FirebaseApp.initializeApp(org.robolectric.RuntimeEnvironment.getApplication())
//  }
//
//  @Test
//  fun `googleSignIn should invoke onFailure when no credentials are found`() = runTest {
//    doAnswer { throw NoCredentialException() }
//        .`when`(mockCredentialManager)
//        .getCredential(any(), any<GetCredentialRequest>())
//
//    authenticator.googleSignIn(
//        // coroutineScope = this,
//        onSuccess = {},
//        onFailure = {})
//    verify(mockContext).startActivity(any<Intent>())
//  }
//
//  @Test
//  fun `googleSignIn should invoke onFailure when credential retrieval fails`() = runTest {
//    doAnswer { throw GetCredentialUnknownException() }
//        .`when`(mockCredentialManager)
//        .getCredential(any(), any<GetCredentialRequest>())
//
//    var failureCalled = false
//
//    authenticator.googleSignIn(
//        onSuccess = { fail("onSuccess should not be called") },
//        onFailure = { exception ->
//          failureCalled = true
//          assertTrue(exception is GetCredentialException)
//        })
//
//    verifyErrorLog("Error getting credential")
//    assertTrue(failureCalled)
//  }
//
//  @Test
//  fun `googleSignIn logs when credential retrieval cancelled`() = runTest {
//    doAnswer { throw GetCredentialCancellationException() }
//        .`when`(mockCredentialManager)
//        .getCredential(any(), any<GetCredentialRequest>())
//
//    authenticator.googleSignIn(
//        onSuccess = { fail("onSuccess should not be called") },
//        onFailure = { fail("onFailure should not be called") })
//
//    verifyErrorLog("Credential flow cancelled by user")
//  }
//
//  @Test
//  fun `googleSignIn initializes correct option`() = runTest {
//    val getCredentialRequestCaptor = argumentCaptor<GetCredentialRequest>()
//
//    // Mock the getCredential method to throw an exception, so we avoid going into handeSignIn
//    doAnswer { throw GetCredentialUnknownException() }
//        .`when`(mockCredentialManager)
//        .getCredential(any(), any<GetCredentialRequest>())
//
//    authenticator.googleSignIn({}, {})
//
//    // Capture the request to verify the options, and retrieve the getGoogleIdOption used.
//    verify(mockCredentialManager).getCredential(any(), getCredentialRequestCaptor.capture())
//
//    val request = getCredentialRequestCaptor.firstValue
//
//    assert(request.credentialOptions.isNotEmpty())
//    val googleIdOption = request.credentialOptions[0] as GetGoogleIdOption
//    assertFalse(googleIdOption.filterByAuthorizedAccounts)
//    assertEquals(serverClientId, googleIdOption.serverClientId)
//    assertNotNull(googleIdOption.nonce)
//    assert(googleIdOption.autoSelectEnabled)
//  }
//
//  @Test
//  fun `signOut should clear Firebase session and CredentialManager state`() = runTest {
//    // Mock FirebaseAuth, to make signOut do Nothing
//    val MockFirebaseAuth = mockStatic(FirebaseAuth::class.java)
//    var firebaseSignedOut = false
//    MockFirebaseAuth.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
//        .thenReturn(mockFirebaseAuth)
//    doAnswer { firebaseSignedOut = true }.`when`(mockFirebaseAuth).signOut()
//
//    `when`(mockCredentialManager.clearCredentialState(any())).thenReturn(Unit)
//
//    authenticator.signOut()
//
//    assert(firebaseSignedOut)
//    verify(mockCredentialManager).clearCredentialState(any())
//    MockFirebaseAuth.close()
//  }
//
//  @Test
//  fun `handleSignIn should log error when invalid response 2`() {
//    // Arrange
//    val invalidCredential =
//        CustomCredential(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL, Bundle())
//
//    val getCredentialResponse = mock<GetCredentialResponse>()
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
//  @Test
//  fun `handleSignIn should log error when invalid custom credential type`() = runTest {
//    `when`(mockCustomCredential.type).thenReturn("INVALID_TYPE")
//    var onFailureCalled = false
//    authenticator.handleSignIn(mockGetCredentialResponse, {}, { onFailureCalled = true })
//
//    assert(onFailureCalled)
//
//    verifyErrorLog("Unexpected type of credential: INVALID_TYPE")
//  }
//
//  @Test
//  fun `handleSignIn should log error when invalid credential type`() = runTest {
//    val mockCredential = mock<Credential>()
//    `when`(mockGetCredentialResponse.credential).thenReturn(mockCredential)
//    // `when`(mockCredential.javaClass).thenReturn(Credential::class.java)
//
//    `when`(mockCredential.type).thenReturn("INVALID_TYPE")
//    var onFailureCalled = false
//    authenticator.handleSignIn(mockGetCredentialResponse, {}, { onFailureCalled = true })
//
//    assert(onFailureCalled)
//
//    verifyErrorLog("Unexpected type of credential")
//  }
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
// private fun verifyErrorLog(msg: String) {
//  // Get all the logs
//  val logs = ShadowLog.getLogs()
//
//  // Check for the debug log that should be generated
//  val errorLog = logs.find { it.type == Log.ERROR && it.tag == Authenticator.TAG && it.msg == msg
// }
//  assert(errorLog != null) { "Expected error log was not found!" }
// }
