package com.github.onlynotesswent.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.fromActivityResultIntent
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

/**
 * Unit tests for the `Scanner` class which interacts with Google ML Kit's Document Scanner API.
 * These tests cover scenarios like initialization, success, and failure during scanning, as well as
 * verifying if `Toast` messages and log outputs are correctly generated during the scanning
 * process.
 *
 * Tools used:
 * - **Mockito**: For mocking components, including static methods like `Toast.makeText()`.
 * - **Robolectric**: For testing Android-specific components like logs.
 * - **JUnit**: The testing framework for writing and running the tests.
 *
 * The tests ensure that the scanner behaves correctly in both success and failure cases, and that
 * logs and UI feedback (like Toast messages) are triggered as expected.
 */
@RunWith(RobolectricTestRunner::class)
class ScannerTest {

  @Mock private lateinit var mockOnlyNotes: ComponentActivity
  @Mock private lateinit var mockDocScanner: GmsDocumentScanner
  @Mock private lateinit var mockTaskIntentSender: Task<IntentSender>
  @Mock private lateinit var mockIntentSender: IntentSender
  @Mock private lateinit var mockActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

  private lateinit var scanner: Scanner

  private fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

  @Captor private lateinit var intentSenderRequestCaptor: ArgumentCaptor<IntentSenderRequest>

  @Before
  fun setUp() {

    // Initializes the mocks and sets up the scanner with the mock activity and document scanner
    MockitoAnnotations.openMocks(this)

    // Mock the registration of activity result launcher in the activity
    `when`(
            mockOnlyNotes.registerForActivityResult(
                any<ActivityResultContract<IntentSenderRequest, ActivityResult>>(),
                any<ActivityResultCallback<ActivityResult>>()))
        .thenReturn(mockActivityResultLauncher)

    // Initialize the scanner with the mocked components
    scanner = Scanner(mockOnlyNotes, mockDocScanner)
  }

  /**
   * Test that verifies if the scanner is correctly initialized by registering the activity result
   * launcher.
   */
  @Test
  fun initTest() {

    // Call a scan without initializing first
    scanner.scan {}

    verifyErrorLog("Error: scannerLauncher is not initialized")

    // Initialize the scanner
    scanner.init()

    // Verify that the activity result launcher is registered
    verify(mockOnlyNotes)
        .registerForActivityResult(
            any<ActivityResultContract<IntentSenderRequest, ActivityResult>>(),
            any<ActivityResultCallback<ActivityResult>>())
  }

  /**
   * Test that simulates a scan already in progress and ensures that the appropriate log message is
   * generated.
   */
  @Test
  fun scanInProgressTest() {

    // Simulate a successful scan
    `when`(mockDocScanner.getStartScanIntent(mockOnlyNotes)).thenReturn(mockTaskIntentSender)
    `when`(mockTaskIntentSender.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<IntentSender>
      listener.onSuccess(mockIntentSender)
      mockTaskIntentSender
    }

    Mockito.mockStatic(Toast::class.java).use { toastMock ->
      // Create a mock Toast object
      val mockToast = mock(Toast::class.java)

      // Stub the static makeText method to return the mock Toast object
      toastMock
          .`when`<Toast> { makeText(any<Context>(), any<String>(), any()) }
          .thenReturn(mockToast)

      // Trigger the scan method that will lead to failure and show the Toast
      scanner.init()
      scanner.scan {}
      scanner.scan {}

      // Verify that Toast.makeText() was called with the appropriate arguments
      toastMock.verify { makeText(any<Context>(), any<String>(), any()) }

      // Verify that Toast.show() was called on the returned Toast object
      verify(mockToast).show()
    }

    verifyErrorLog("Error: scan already in progress")
  }

  /**
   * Test that simulates a successful document scan and ensures that the scan process is launched
   * correctly.
   */
  @Test
  fun scanSuccessTest() {
    val captor = ArgumentCaptor.forClass(IntentSenderRequest::class.java)

    // Simulate a successful scan
    `when`(mockDocScanner.getStartScanIntent(mockOnlyNotes)).thenReturn(mockTaskIntentSender)
    `when`(mockTaskIntentSender.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<IntentSender>
      listener.onSuccess(mockIntentSender)
      mockTaskIntentSender
    }

    // Trigger the scan
    scanner.init()
    scanner.scan {}

    // Verify that the scanning intent was retrieved and launched
    verify(mockDocScanner).getStartScanIntent(mockOnlyNotes)
    verify(mockTaskIntentSender).addOnSuccessListener(any())
    verify(mockActivityResultLauncher).launch(capture(intentSenderRequestCaptor))
    assertEquals(mockIntentSender, intentSenderRequestCaptor.value.intentSender)
  }

  @Test
  fun scanLaunchThrowsExceptionTest() {
    // Simulate a successful call, but an exception thrown by the scanner launcher
    `when`(mockActivityResultLauncher.launch(any())).thenThrow(ActivityNotFoundException("test"))
    `when`(mockDocScanner.getStartScanIntent(mockOnlyNotes)).thenReturn(mockTaskIntentSender)
    `when`(mockTaskIntentSender.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<IntentSender>
      listener.onSuccess(mockIntentSender)
      mockTaskIntentSender
    }

    Mockito.mockStatic(Toast::class.java).use { toastMock ->
      // Create a mock Toast object
      val mockToast = mock(Toast::class.java)

      // Stub the static makeText method to return the mock Toast object
      toastMock
          .`when`<Toast> { makeText(any<Context>(), any<String>(), any()) }
          .thenReturn(mockToast)

      // Trigger the scan method that will lead to failure and show the Toast
      scanner.init()
      scanner.scan {}

      verifyErrorLog("Failed to launch scanner: test")

      // Verify that Toast.makeText() was called with the appropriate arguments
      toastMock.verify { makeText(any<Context>(), any<String>(), any()) }

      // Verify that Toast.show() was called on the returned Toast object
      verify(mockToast).show()
    }

    // Verify that the scanning intent was retrieved and launched
    verify(mockDocScanner).getStartScanIntent(mockOnlyNotes)
    verify(mockTaskIntentSender).addOnSuccessListener(any())
    verify(mockActivityResultLauncher).launch(capture(intentSenderRequestCaptor))
    assertEquals(mockIntentSender, intentSenderRequestCaptor.value.intentSender)
  }

  /**
   * Test that simulates a scan failure and verifies that a `Toast` message is shown with the
   * appropriate error message.
   */
  @Test
  fun scanFailTest() {
    // Simulate a failure
    `when`(mockDocScanner.getStartScanIntent(mockOnlyNotes)).thenReturn(mockTaskIntentSender)
    `when`(mockTaskIntentSender.addOnSuccessListener(any())).thenReturn(mockTaskIntentSender)
    `when`(mockTaskIntentSender.addOnFailureListener(any())).thenAnswer {
      (it.arguments[0] as OnFailureListener).onFailure(Exception("Failed to scan"))
      mockTaskIntentSender
    }

    // Mock the Toast object to verify that it is shown
    Mockito.mockStatic(Toast::class.java).use { toastMock ->
      // Create a mock Toast object
      val mockToast = mock(Toast::class.java)

      // Stub the static makeText method to return the mock Toast object
      toastMock
          .`when`<Toast> { makeText(any<Context>(), any<String>(), any()) }
          .thenReturn(mockToast)

      // Trigger the scan method that will lead to failure and show the Toast
      scanner.init()
      scanner.scan {}

      // Verify that Toast.makeText() was called with the appropriate arguments
      toastMock.verify { makeText(any<Context>(), any<String>(), any()) }

      // Verify that Toast.show() was called on the returned Toast object
      verify(mockToast).show()
    }
  }

  /**
   * Test to simulate a failed scan result and verify that a log message is generated and the
   * appropriate Toast is shown to the user.
   */
  @Test
  fun scanResultFailTest() {
    scanner.init()
    // Capture the ActivityResultCallback, to be able to test the private function
    // handleActivityResult
    val captor =
        ArgumentCaptor.forClass(ActivityResultCallback::class.java)
            as ArgumentCaptor<ActivityResultCallback<ActivityResult>>
    verify(mockOnlyNotes)
        .registerForActivityResult(
            any<ActivityResultContract<IntentSenderRequest, ActivityResult>>(), capture(captor))
    val handleActivityResult = captor.value

    Mockito.mockStatic(Toast::class.java).use { ToastMock ->
      // Create a mock Toast object
      val mockToast = mock(Toast::class.java)

      // Stub the static makeText method to return the mock Toast object
      ToastMock.`when`<Toast> { makeText(any<Context>(), any<String>(), any()) }
          .thenReturn(mockToast)

      // Call the captured handleActivityResult, that will fail
      handleActivityResult.onActivityResult(ActivityResult(Activity.RESULT_OK, null))

      verifyErrorLog("Scanner failed with resultCode: ${Activity.RESULT_OK}")

      // Verify that Toast.makeText() was called with the appropriate arguments
      ToastMock.verify { makeText(eq(mockOnlyNotes), eq("Scanner failed"), any()) }

      // Verify that Toast.show() was called on the returned Toast object
      verify(mockToast).show()
    }
  }

  /**
   * Test to simulate the user canceling the scan and ensure that a log message is generated and the
   * appropriate Toast is shown.
   */
  @Test
  fun scanResultCancelTest() {
    // Call the init method
    scanner.init()

    // Capture the ActivityResultCallback, to be able to test the private function
    // handleActivityResult
    val captor =
        ArgumentCaptor.forClass(ActivityResultCallback::class.java)
            as ArgumentCaptor<ActivityResultCallback<ActivityResult>>
    verify(mockOnlyNotes)
        .registerForActivityResult(
            any<ActivityResultContract<IntentSenderRequest, ActivityResult>>(), capture(captor))
    val handleActivityResult = captor.value

    Mockito.mockStatic(Toast::class.java).use { ToastMock ->
      // Create a mock Toast object
      val mockToast = mock(Toast::class.java)

      // Stub the static makeText method to return the mock Toast object
      ToastMock.`when`<Toast> { makeText(any<Context>(), any<String>(), any()) }
          .thenReturn(mockToast)

      handleActivityResult.onActivityResult(ActivityResult(Activity.RESULT_CANCELED, null))

      verifyErrorLog("Scanner cancelled")

      // Verify that Toast.makeText() was called with the appropriate arguments
      ToastMock.verify { makeText(eq(mockOnlyNotes), eq("Scanner cancelled"), any()) }

      // Verify that Toast.show() was called on the returned Toast object
      verify(mockToast).show()
    }
  }

  /**
   * Test to simulate the path to the pdf file being null and ensure that a log message is
   * generated.
   */
  @Test
  fun scanResultPathNotFoundTest() {

    // Call the init method
    scanner.init()

    // Capture the ActivityResultCallback, to be able to test the private function
    // handleActivityResult
    val captor =
        ArgumentCaptor.forClass(ActivityResultCallback::class.java)
            as ArgumentCaptor<ActivityResultCallback<ActivityResult>>
    verify(mockOnlyNotes)
        .registerForActivityResult(
            any<ActivityResultContract<IntentSenderRequest, ActivityResult>>(), capture(captor))
    val handleActivityResult = captor.value

    Mockito.mockStatic(Toast::class.java).use { ToastMock ->
      // Create a mock Toast object
      val mockToast = mock(Toast::class.java)

      // Stub the static makeText method to return the mock Toast object
      ToastMock.`when`<Toast> { makeText(any<Context>(), any<String>(), any()) }
          .thenReturn(mockToast)

      Mockito.mockStatic(GmsDocumentScanningResult::class.java).use { ScanningResultMock ->
        // Create a mock GmsDocumentScanningResult object
        val mockResult = mock(GmsDocumentScanningResult::class.java)

        `when`(fromActivityResultIntent(any())).thenReturn(mockResult)

        // Stub the static fromActivityResultIntent method to return the mock
        // GmsDocumentScanningResult object
        ScanningResultMock.`when`<GmsDocumentScanningResult> { fromActivityResultIntent(any()) }
            .thenReturn(mockResult)

        // Simulate the returning of a null path
        `when`(mockResult.pdf?.uri?.path).thenReturn(null)

        handleActivityResult.onActivityResult(ActivityResult(Activity.RESULT_OK, Intent()))

        verifyErrorLog("Path to pdf file is null")

        // Verify that Toast.makeText() was called with the appropriate arguments
        ToastMock.verify { makeText(eq(mockOnlyNotes), any<String>(), any()) }

        // Verify that Toast.show() was called on the returned Toast object
        verify(mockToast).show()
      }
    }
  }

  /**
   * Test to simulate a successful scan result and ensure that the path to the pdf file is correctly
   * handled. ToDo To be improved upon when we know what to do with the pdf file.
   */
  @Test
  fun scanResultCorrectTest() {
    val testPath = "test_path.pdf"

    // Simulate a successful scan
    `when`(mockDocScanner.getStartScanIntent(mockOnlyNotes)).thenReturn(mockTaskIntentSender)
    `when`(mockTaskIntentSender.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<IntentSender>
      listener.onSuccess(mockIntentSender)
      mockTaskIntentSender
    }

    // Initialize the scanner and trigger the scan
    scanner.init()
    scanner.scan { uri -> assertEquals(testPath, uri.path) }

    // Capture the ActivityResultCallback, to be able to test the private function
    // handleActivityResult
    val captor =
        ArgumentCaptor.forClass(ActivityResultCallback::class.java)
            as ArgumentCaptor<ActivityResultCallback<ActivityResult>>
    verify(mockOnlyNotes)
        .registerForActivityResult(
            any<ActivityResultContract<IntentSenderRequest, ActivityResult>>(), capture(captor))
    val handleActivityResult = captor.value

    // Mock the static GmsDocumentScanningResult.fromActivityResultIntent method and return
    val ScanningResultMock = Mockito.mockStatic(GmsDocumentScanningResult::class.java)
    val mockResult = mock(GmsDocumentScanningResult::class.java)
    ScanningResultMock.`when`<GmsDocumentScanningResult> { fromActivityResultIntent(any()) }
        .thenReturn(mockResult)

    val mockPdf = mock(GmsDocumentScanningResult.Pdf::class.java)
    val mockUri = mock(Uri::class.java)

    // Simulate the returning of a valid path

    `when`(mockResult.pdf).thenReturn(mockPdf)
    `when`(mockPdf.uri).thenReturn(mockUri)
    `when`(mockUri.path).thenReturn(testPath)

    val FileProviderMock = Mockito.mockStatic(FileProvider::class.java)
    // Stub the static getUriForFile method to return the mock Uri object
    // (no separate mock created for an external URI as it would be unnecessary)
    FileProviderMock.`when`<Uri> { getUriForFile(eq(mockOnlyNotes), any(), any()) }
        .thenReturn(mockUri)

    handleActivityResult.onActivityResult(ActivityResult(Activity.RESULT_OK, Intent()))

    ScanningResultMock.close()
    FileProviderMock.close()
  }

  private fun verifyErrorLog(msg: String) {
    // Get all the logs
    val logs = ShadowLog.getLogs()

    // Check for the debug log that should be generated
    val errorLog = logs.find { it.type == Log.ERROR && it.tag == Scanner.TAG && it.msg == msg }
    assert(errorLog != null) { "Expected error log was not found!" }
  }
}
