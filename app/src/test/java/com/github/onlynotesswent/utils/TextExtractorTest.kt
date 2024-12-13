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
@Suppress("UNCHECKED_CAST")
class TextExtractorTest {

  @Mock private lateinit var mockTextRecognizer: TextRecognizer
  @Mock private lateinit var mockPdfFile: File
  @Mock private lateinit var mockBitmap: Bitmap
  //  @Mock private lateinit var mockPdfRenderer: PdfRenderer
  //  @Mock private lateinit var mockParcelFileDescriptor: ParcelFileDescriptor

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

  //  @Test
  //  fun processPdfFileCatchesExceptionTest() {
  //    `when`(textExtractor.convertPdfToBitmap(mockPdfFile)).thenThrow(Exception("Test Exception"))
  //    val onSuccess: (String) -> Unit = mock()
  //
  //    mockStatic(Toast::class.java).use { toastMock ->
  //      val mockToast = mock(Toast::class.java)
  //      toastMock.`when`<Toast> { Toast.makeText(any<Context>(), any<String>(), any()) }
  //        .thenReturn(mockToast)
  //
  //      textExtractor.processPdfFile(mockPdfFile, onSuccess)
  //
  //      // log shows
  //      val logs = ShadowLog.getLogs()
  //      println(logs)
  //      val errorLog =
  //        logs.find {
  //          it.type == Log.ERROR &&
  //                  it.tag == TextExtractor.TAG &&
  //                  it.msg == "Error while converting PDF to bitmap"
  //        }
  //      assert(errorLog != null) { "Expected error log was not found!" }
  //
  //      // toast shows
  //      toastMock.verify {
  //        Toast.makeText(
  //          eq(mockActivity), eq("Error: text recognition failed"), eq(Toast.LENGTH_SHORT)
  //        )
  //      }
  //      verify(mockToast).show()
  //    }
  //  }
  //
  //  @Test
  //  fun processPdfFileCallsCorrectFunctionsTest() {
  //    `when`(textExtractor.convertPdfToBitmap(mockPdfFile)).thenReturn(listOf(mockBitmap))
  //    val onSuccess: (String) -> Unit = mock()
  //
  //    textExtractor.processPdfFile(mockPdfFile, onSuccess)
  //
  //    // Verify the methods were called
  //    verify(textExtractor).convertPdfToBitmap(mockPdfFile)
  //    verify(textExtractor).extractTextFromBitmaps(any(), any())
  //  }

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

  //  @Test
  //  fun convertPdfToBitmapSuccessTest() {
  //    val mockPage = mock(PdfRenderer.Page::class.java)
  //    val fakeBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
  //
  //    `when`(mockPdfFile.canRead()).thenReturn(true)
  //    mockStatic(ParcelFileDescriptor::class.java).use { pfdMock ->
  //      pfdMock.`when`<ParcelFileDescriptor> {
  //        ParcelFileDescriptor.open(mockPdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
  //      }.thenReturn(mockParcelFileDescriptor) }
  //    `when`(PdfRenderer(mockParcelFileDescriptor)).thenReturn(mockPdfRenderer)
  //    `when`(mockPdfRenderer.pageCount).thenReturn(1)
  //    `when`(mockPdfRenderer.openPage(0)).thenReturn(mockPage)
  //    `when`(mockPage.width).thenReturn(100)
  //    `when`(mockPage.height).thenReturn(100)
  //    `when`(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)).thenReturn(fakeBitmap)
  //
  //    val bitmaps = textExtractor.convertPdfToBitmap(mockPdfFile)
  //
  //    assert(bitmaps.size == 1) { "Expected one bitmap" }
  //    assert(bitmaps[0] == fakeBitmap) { "Expected the bitmap to be the same" }
  //  }

  @Test
  fun extractTextFromBitmapsSuccessTest() {
    val onSuccess: (String) -> Unit = mock()
    val text = mock(Text::class.java)
    `when`(text.text).thenReturn("Hello world")

    val successfulTask = mock(Task::class.java) as Task<Text>
    `when`(successfulTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<Text>
      listener.onSuccess(text)
      successfulTask
    }
    `when`(mockTextRecognizer.process(any(InputImage::class.java))).thenReturn(successfulTask)

    textExtractor.extractTextFromBitmaps(listOf(mockBitmap), onSuccess)

    verify(onSuccess).invoke("Hello world\n")
  }

  @Test
  fun extractTextFromBitmapsSuccessNoTextTest() {
    val onSuccess: (String) -> Unit = mock()
    val text = mock(Text::class.java)
    `when`(text.text).thenReturn("")

    val successfulTask = mock(Task::class.java) as Task<Text>
    `when`(successfulTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<Text>
      listener.onSuccess(text)
      successfulTask
    }
    `when`(mockTextRecognizer.process(any(InputImage::class.java))).thenReturn(successfulTask)

    textExtractor.extractTextFromBitmaps(listOf(mockBitmap), onSuccess)

    verify(onSuccess).invoke("")
  }

  @Test
  fun extractTextFromBitmapsFails() {
    val onSuccess: (String) -> Unit = mock()

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

      textExtractor.extractTextFromBitmaps(listOf(mockBitmap), onSuccess)

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

      // toast shows
      toastMock.verify {
        Toast.makeText(
            eq(mockActivity), eq("Error: text recognition failed"), eq(Toast.LENGTH_SHORT))
      }
      verify(mockToast).show()
    }
  }
}
