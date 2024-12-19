package com.github.onlynotesswent.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKit.initialize
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import java.io.File
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class TextExtractorTest {

  @Mock private lateinit var mockTextRecognizer: TextRecognizer
  @Mock private lateinit var mockPdfFile: File
  @Mock private lateinit var mockBitmap: Bitmap

  private lateinit var textExtractor: TextExtractor
  private lateinit var mockActivity: ComponentActivity

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    mockActivity = Robolectric.buildActivity(ComponentActivity::class.java).create().get()
    try {
      initialize(mockActivity)
    } catch (e: Exception) {
      // do nothing
    }

    textExtractor = TextExtractor(mockActivity, mockTextRecognizer)
  }

  @Test
  fun convertPdfToBitmapThrowsException1Test() {
    `when`(mockPdfFile.canRead()).thenReturn(false)

    assertThrows(Exception::class.java) { textExtractor.convertPdfToBitmap(mockPdfFile) }
  }

  @Test
  fun convertPdfToBitmapThrowsException2Test() {
    mockStatic(Bitmap::class.java).use { mockedBitmap ->
      mockedBitmap
          .`when`<Bitmap> { Bitmap.createBitmap(anyInt(), anyInt(), any()) }
          .thenThrow(RuntimeException("Test Exception"))
    }

    assertThrows(RuntimeException::class.java) { textExtractor.convertPdfToBitmap(mockPdfFile) }
  }

  @Test
  fun extractTextFromBitmapsSuccessTest() {
    val onSuccess: (String) -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()
    val text = mock(Text::class.java)
    `when`(text.text).thenReturn("Hello world")

    val successfulTask = mock(Task::class.java) as Task<Text>
    `when`(successfulTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<Text>
      listener.onSuccess(text)
      successfulTask
    }
    `when`(successfulTask.addOnFailureListener(any()))
        .thenReturn(successfulTask) // no failure listener
    `when`(mockTextRecognizer.process(any(InputImage::class.java))).thenReturn(successfulTask)

    textExtractor.extractTextFromBitmaps(listOf(mockBitmap), onSuccess, onFailure)

    verify(onSuccess).invoke("Hello world\n")
  }

  @Test
  fun extractTextFromBitmapsSuccessNoTextTest() {
    val onSuccess: (String) -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()
    val text = mock(Text::class.java)
    `when`(text.text).thenReturn("")

    val successfulTask = mock(Task::class.java) as Task<Text>
    `when`(successfulTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<Text>
      listener.onSuccess(text)
      successfulTask
    }
    `when`(successfulTask.addOnFailureListener(any()))
        .thenReturn(successfulTask) // no failure listener
    `when`(mockTextRecognizer.process(any(InputImage::class.java))).thenReturn(successfulTask)

    textExtractor.extractTextFromBitmaps(listOf(mockBitmap), onSuccess, onFailure)

    verify(onSuccess).invoke("")
  }

  @Test
  fun extractTextFromBitmapsFails() {
    val onSuccess: (String) -> Unit = mock()
    val onFailure: (Exception) -> Unit = mock()

    val failedTask = mock(Task::class.java) as Task<Text>
    `when`(failedTask.addOnSuccessListener(any())).thenReturn(failedTask) // No success listener
    `when`(failedTask.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(Exception("Recognition failed"))
      failedTask
    }
    `when`(mockTextRecognizer.process(any(InputImage::class.java))).thenReturn(failedTask)

    mockStatic(Toast::class.java).use { toastMock ->
      val mockToast = mock(Toast::class.java)
      toastMock
          .`when`<Toast> { Toast.makeText(any<Context>(), any<String>(), any()) }
          .thenReturn(mockToast)

      textExtractor.extractTextFromBitmaps(listOf(mockBitmap), onSuccess, onFailure)

      // log shows
      val logs = ShadowLog.getLogs()
      val errorLog =
          logs.find {
            it.type == Log.ERROR &&
                it.tag == TextExtractor.TAG &&
                it.msg == "Text recognition failed" &&
                it.throwable.message == "Recognition failed"
          }
      assert(errorLog != null) { "Expected error log was not found!" }
    }
  }
}
