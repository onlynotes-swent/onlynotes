package com.github.onlynotesswent.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import java.io.IOException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Suppress("UNCHECKED_CAST")
class TextExtractorTest {

  @Mock private lateinit var mockActivity: ComponentActivity
  @Mock private lateinit var mockTextRecognizer: TextRecognizer
  @Mock private lateinit var mockActivityResultLauncher: ActivityResultLauncher<String>

  private lateinit var textExtractor: TextExtractor

  private fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Mock the registration of activity result launcher in the activity
    `when`(
            mockActivity.registerForActivityResult(
                any<ActivityResultContract<String, ActivityResult>>(),
                any<ActivityResultCallback<ActivityResult>>()))
        .thenReturn(mockActivityResultLauncher)

    textExtractor = TextExtractor(mockActivity, mockTextRecognizer)
  }

  @Test
  fun initRegistersActivityResultLauncherTest() {
    textExtractor.init()

    // Verify that the activity result launcher is registered
    verify(mockActivity)
        .registerForActivityResult(
            any<ActivityResultContract<String, ActivityResult>>(),
            any<ActivityResultCallback<ActivityResult>>())
  }

  @Test
  fun scanImageLogsErrorWhenNotInitializedTest() {
    textExtractor.scanImage()

    // Get all the logs
    val logs = ShadowLog.getLogs()

    // Check for the error log that should be generated
    val errorLog =
        logs.find {
          it.type == Log.ERROR &&
              it.tag == TextExtractor.TAG &&
              it.msg == "Error: textRecognitionLauncher is not initialized"
        }
    assert(errorLog != null) { "Expected error log was not found!" }
  }

  @Test
  fun scanImageLaunchesImagePickerIntentTest() {
    textExtractor.init()
    textExtractor.scanImage()

    // Verify that the activity result launcher was launched with the correct MIME type
    verify(mockActivityResultLauncher).launch("image/*")
  }

  @Test
  fun scanImageHandlesActivityNotFoundExceptionTest() {
    // Simulate ActivityNotFoundException when launching the image picker
    `when`(mockActivityResultLauncher.launch("image/*"))
        .thenThrow(ActivityNotFoundException::class.java)

    mockStatic(Toast::class.java).use { toastMock ->
      val mockToast = mock(Toast::class.java)
      toastMock
          .`when`<Toast> { Toast.makeText(any<Context>(), any<String>(), any()) }
          .thenReturn(mockToast)

      // Start the activity
      textExtractor.init()
      textExtractor.scanImage()

      // Get all the logs
      val logs = ShadowLog.getLogs()

      // Check for the error log that should be generated
      val errorLog =
          logs.find {
            it.type == Log.ERROR &&
                it.tag == TextExtractor.TAG &&
                it.msg == "Failed to launch gallery"
          }
      assert(errorLog != null) { "Expected error log was not found!" }

      // Verify that Toast.makeText() was called with the appropriate arguments
      toastMock.verify {
        Toast.makeText(eq(mockActivity), eq("Failed to launch gallery"), eq(Toast.LENGTH_LONG))
      }

      // Verify that Toast.show() was called on the returned Toast object
      verify(mockToast).show()
    }
  }

  @Test
  fun extractTextFromImageSuccessTest() {
    val mockUri = mock(Uri::class.java)
    val mockInputImage = mock(InputImage::class.java)
    val mockText = mock(Text::class.java)
    `when`(mockText.text).thenReturn("Test recognized text")

    // Create a task that simulates a successful recognition
    val successfulTask = mock(Task::class.java) as Task<Text>
    `when`(successfulTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<Text>
      listener.onSuccess(mockText)
      successfulTask
    }

    // Mock the text recognizer's processing result
    `when`(mockTextRecognizer.process(mockInputImage)).thenReturn(successfulTask)

    // Mock the InputImage creation
    mockStatic(InputImage::class.java).use { inputImageMock ->
      inputImageMock
          .`when`<InputImage> { InputImage.fromFilePath(any(), eq(mockUri)) }
          .thenReturn(mockInputImage)

      // Trigger the text extraction process
      textExtractor.init()
      textExtractor.scanImage()

      // Simulate the image selection callback
      val captor =
          ArgumentCaptor.forClass(ActivityResultCallback::class.java)
              as ArgumentCaptor<ActivityResultCallback<Uri?>>
      verify(mockActivity)
          .registerForActivityResult(any<ActivityResultContracts.GetContent>(), capture(captor))
      captor.value.onActivityResult(mockUri)

      // Verify that the text processing was triggered
      verify(mockTextRecognizer).process(mockInputImage)
    }
  }

  @Test
  fun extractTextFromImageIOExceptionTest() {
    val mockUri = mock(Uri::class.java)

    // Mock the InputImage creation to throw an IOException
    mockStatic(InputImage::class.java).use { inputImageMock ->
      inputImageMock
          .`when`<InputImage> { InputImage.fromFilePath(any(), eq(mockUri)) }
          .thenThrow(IOException("Test IO Exception"))

      mockStatic(Toast::class.java).use { toastMock ->
        val mockToast = mock(Toast::class.java)
        toastMock
            .`when`<Toast> { Toast.makeText(any<Context>(), any<String>(), any()) }
            .thenReturn(mockToast)

        // Trigger the text extraction process
        textExtractor.init()
        textExtractor.scanImage()

        // Simulate the image selection callback
        val captor =
            ArgumentCaptor.forClass(ActivityResultCallback::class.java)
                as ArgumentCaptor<ActivityResultCallback<Uri?>>
        verify(mockActivity)
            .registerForActivityResult(any<ActivityResultContracts.GetContent>(), capture(captor))
        captor.value.onActivityResult(mockUri)

        // Get all the logs
        val logs = ShadowLog.getLogs()

        // Check for the error log that should be generated
        val errorLog =
            logs.find {
              it.type == Log.ERROR && it.tag == TextExtractor.TAG && it.msg == "Error reading image"
            }
        assert(errorLog != null) { "Expected error log was not found!" }

        // Verify that Toast.makeText() was called with the appropriate arguments
        toastMock.verify {
          Toast.makeText(
              eq(mockActivity), eq("Error reading image: Test IO Exception"), eq(Toast.LENGTH_LONG))
        }

        // Verify that Toast.show() was called on the returned Toast object
        verify(mockToast).show()
      }
    }
  }

  @Test
  fun extractTextFromImageNoTextFoundTest() {
    val mockUri = mock(Uri::class.java)
    val mockInputImage = mock(InputImage::class.java)
    val mockText = mock(Text::class.java)
    `when`(mockText.text).thenReturn("")

    // Create a task that simulates a successful recognition
    val successfulTask = mock(Task::class.java) as Task<Text>
    `when`(successfulTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<Text>
      listener.onSuccess(mockText)
      successfulTask
    }

    // Mock the text recognizer's processing result
    `when`(mockTextRecognizer.process(mockInputImage)).thenReturn(successfulTask)

    // Mock the InputImage creation
    mockStatic(InputImage::class.java).use { inputImageMock ->
      inputImageMock
          .`when`<InputImage> { InputImage.fromFilePath(any(), eq(mockUri)) }
          .thenReturn(mockInputImage)

      mockStatic(Toast::class.java).use { toastMock ->
        val mockToast = mock(Toast::class.java)
        toastMock
            .`when`<Toast> { Toast.makeText(any<Context>(), any<String>(), any()) }
            .thenReturn(mockToast)

        // Trigger the text extraction process
        textExtractor.init()
        textExtractor.scanImage()

        // Simulate the image selection callback
        val captor =
            ArgumentCaptor.forClass(ActivityResultCallback::class.java)
                as ArgumentCaptor<ActivityResultCallback<Uri?>>
        verify(mockActivity)
            .registerForActivityResult(any<ActivityResultContracts.GetContent>(), capture(captor))
        captor.value.onActivityResult(mockUri)

        // Get all the logs
        val logs = ShadowLog.getLogs()

        // Check for the debug log that should be generated
        val debugLog =
            logs.find {
              it.type == Log.DEBUG &&
                  it.tag == TextExtractor.TAG &&
                  it.msg == "No text found in the image"
            }
        assert(debugLog != null) { "Expected warning log was not found!" }

        // Verify that Toast.makeText() was called with the appropriate arguments
        toastMock.verify {
          Toast.makeText(eq(mockActivity), eq("No text found in the image"), eq(Toast.LENGTH_LONG))
        }

        // Verify that Toast.show() was called on the returned Toast object
        verify(mockToast).show()
      }
    }
  }

  @Test
  fun extractTextFromImageFailureRecognitionTest() {
    val mockUri = mock(Uri::class.java)
    val mockInputImage = mock(InputImage::class.java)

    // Create a task that simulates a failed recognition
    val failedTask = mock(Task::class.java) as Task<Text>
    `when`(failedTask.addOnSuccessListener(any())).thenReturn(failedTask) // No success listener
    `when`(failedTask.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(Exception("Recognition failed"))
      failedTask
    }

    // Mock the text recognizer's processing result
    `when`(mockTextRecognizer.process(mockInputImage)).thenReturn(failedTask)

    // Mock the InputImage creation
    mockStatic(InputImage::class.java).use { inputImageMock ->
      inputImageMock
          .`when`<InputImage> { InputImage.fromFilePath(any(), eq(mockUri)) }
          .thenReturn(mockInputImage)

      mockStatic(Toast::class.java).use { toastMock ->
        val mockToast = mock(Toast::class.java)

        toastMock
            .`when`<Toast> { Toast.makeText(any<Context>(), any<String>(), any()) }
            .thenReturn(mockToast)

        // Trigger the text extraction process
        textExtractor.init()
        textExtractor.scanImage()

        // Simulate the image selection callback
        val captor =
            ArgumentCaptor.forClass(ActivityResultCallback::class.java)
                as ArgumentCaptor<ActivityResultCallback<Uri?>>
        verify(mockActivity)
            .registerForActivityResult(any<ActivityResultContracts.GetContent>(), capture(captor))
        captor.value.onActivityResult(mockUri)

        // Get all the logs
        val logs = ShadowLog.getLogs()

        // Check for the error log that should be generated
        val errorLog =
            logs.find {
              it.type == Log.ERROR &&
                  it.tag == TextExtractor.TAG &&
                  it.msg == "Text recognition failed"
            }
        assert(errorLog != null) { "Expected error log was not found!" }

        // Verify that Toast.makeText() was called with the appropriate arguments
        toastMock.verify {
          Toast.makeText(
              eq(mockActivity),
              eq("Text recognition failed: Recognition failed"),
              eq(Toast.LENGTH_LONG))
        }

        // Verify that Toast.show() was called on the returned Toast object
        verify(mockToast).show()
      }
    }
  }
}
