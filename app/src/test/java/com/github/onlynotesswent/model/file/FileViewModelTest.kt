package com.github.onlynotesswent.model.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import java.io.File
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileViewModelTest {

  @Mock private lateinit var mockFileRepository: FileRepository

  @Mock private lateinit var mockContext: Context

  private lateinit var fileViewModel: FileViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    fileViewModel = FileViewModel(mockFileRepository)
  }

  @Test
  fun uploadFileCallsRepository() {
    val fileUri = Uri.parse("file://dummy")
    val uid = "testUid"
    val fileType = FileType.PROFILE_PIC_JPEG

    fileViewModel.uploadFile(uid, fileUri, fileType)
    verify(mockFileRepository).uploadFile(eq(uid), eq(fileUri), eq(fileType), any(), any())
  }

  @Test
  fun downloadFileCallsRepository() {
    val uid = "testUid"
    val fileType = FileType.PROFILE_PIC_JPEG
    val cacheDir = File("cacheDir")

    `when`(mockContext.cacheDir).thenReturn(cacheDir)

    fileViewModel.downloadFile(uid, fileType, mockContext, {}, {}, {})
    verify(mockFileRepository)
        .downloadFile(eq(uid), eq(fileType), eq(cacheDir), any(), any(), any())
  }

  @Test
  fun deleteFileCallsRepository() {
    val uid = "testUid"
    val fileType = FileType.PROFILE_PIC_JPEG

    fileViewModel.deleteFile(uid, fileType)
    verify(mockFileRepository).deleteFile(eq(uid), eq(fileType), any(), any(), any())
  }

  @Test
  fun updateFileCallsRepository() {
    val fileUri = Uri.parse("file://dummy")
    val uid = "testUid"
    val fileType = FileType.PROFILE_PIC_JPEG

    fileViewModel.updateFile(uid, fileUri, fileType)
    verify(mockFileRepository).updateFile(eq(uid), eq(fileUri), eq(fileType), any(), any())
  }

  @Test
  fun getFileCallsRepository() {
    val uid = "testUid"
    val fileType = FileType.PROFILE_PIC_JPEG

    fileViewModel.getFile(uid, fileType, {}, {}, {})
    verify(mockFileRepository).getFile(eq(uid), eq(fileType), any(), any(), any())
  }

  @Test
  fun openPdfSuccessTest() {
    val uid = "testUid"
    val cacheDir = File("cacheDir")
    val onSuccessCaptor = argumentCaptor<(File) -> Unit>()
    val intentCaptor = argumentCaptor<Intent>()

    val onFileNotFound = {}
    val onFailure = { e: Exception -> }

    `when`(mockContext.cacheDir).thenReturn(cacheDir)

    fileViewModel.openPdf(uid, mockContext, {}, onFileNotFound, onFailure)
    verify(mockFileRepository)
        .downloadFile(
            eq(uid),
            eq(FileType.NOTE_PDF),
            eq(cacheDir),
            onSuccessCaptor.capture(),
            eq(onFileNotFound),
            eq(onFailure))

    val MockContextCompat = mockStatic(ContextCompat::class.java)
    val MockFileProvider = mockStatic(FileProvider::class.java)

    val mockUri = mock(Uri::class.java)
    MockFileProvider.`when`<Uri> { getUriForFile(eq(mockContext), any(), any()) }
        .thenReturn(mockUri)

    onSuccessCaptor.firstValue(File("testFile"))

    MockContextCompat.verify {
      ContextCompat.startActivity(any(), intentCaptor.capture(), isNull())
    }

    val intent = intentCaptor.firstValue
    assertEquals(Intent.ACTION_VIEW, intent.action)
    assertEquals(mockUri, intent.data)
    assertEquals("application/pdf", intent.type)
    assertEquals(
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY, intent.flags)
    MockContextCompat.close()
    MockFileProvider.close()
  }
}
