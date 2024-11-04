package com.github.onlynotesswent.model.file

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.UploadTask.TaskSnapshot
import java.io.File
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class FileRepositoryFirebaseStorageTest {

  @Mock private lateinit var mockFirebaseStorage: FirebaseStorage
  @Mock private lateinit var mockStorageReference: StorageReference
  @Mock private lateinit var mockProfilePicReference: StorageReference
  @Mock private lateinit var mockNoteFilesReference: StorageReference
  @Mock private lateinit var mockFileReference: StorageReference
  @Mock private lateinit var mockUploadTask: UploadTask
  @Mock private lateinit var mockFileDownloadTask: FileDownloadTask
  @Mock private lateinit var mockDeleteTask: Task<Void>
  @Mock private lateinit var mockGetFileTask: Task<ByteArray>

  private lateinit var fileRepository: FileRepositoryFirebaseStorage

  private val uid = "testUid"

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    `when`(mockFirebaseStorage.reference).thenReturn(mockStorageReference)
    `when`(mockStorageReference.child(eq("profile_pics"))).thenReturn(mockProfilePicReference)
    `when`(mockStorageReference.child(eq("notes"))).thenReturn(mockNoteFilesReference)

    // When a file is attempted to be accessed, return mockFileReference (two levels deep for note
    // files,
    // one level deep for profile pictures)
    `when`(mockProfilePicReference.child(any())).thenReturn(mockFileReference)
    `when`(mockNoteFilesReference.child(any())).thenReturn(mockFileReference)
    `when`(mockFileReference.child(any())).thenReturn(mockFileReference)

    fileRepository = FileRepositoryFirebaseStorage(mockFirebaseStorage)
  }

  @Test
  fun uploadFileSuccess() {
    val fileUri = Uri.parse("content://dummy")

    `when`(mockFileReference.putFile(eq(fileUri))).thenReturn(mockUploadTask)

    `when`(mockUploadTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<TaskSnapshot>
      listener.onSuccess(null)
      mockUploadTask
    }

    fileRepository.uploadFile(
        uid,
        fileUri,
        FileType.PROFILE_PIC_JPEG,
        onSuccess = {
          // Success callback
          assert(true)
        },
        onFailure = {
          // Failure callback
          assert(false)
        })

    fileRepository.uploadFile(
        uid,
        fileUri,
        FileType.NOTE_PDF,
        onSuccess = {
          // Success callback
          assert(true)
        },
        onFailure = {
          // Failure callback
          assert(false)
        })
  }

  @Test
  fun uploadFileFailure() {
    val fileUri = Uri.parse("content://dummy")
    val exceptionError = "Error"
    val exception = Exception(exceptionError)

    `when`(mockFileReference.putFile(eq(fileUri))).thenReturn(mockUploadTask)

    `when`(mockUploadTask.addOnSuccessListener(any())).thenReturn(mockUploadTask)
    `when`(mockUploadTask.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockUploadTask
    }

    fileRepository.uploadFile(
        uid,
        fileUri,
        FileType.PROFILE_PIC_JPEG,
        onSuccess = {
          // Success callback
          assert(false)
        },
        onFailure = {
          // Verify correct exception is returned
          assertEquals(exception, it)
        })

    fileRepository.uploadFile(
        uid,
        fileUri,
        FileType.NOTE_PDF,
        onSuccess = {
          // Success callback
          assert(false)
        },
        onFailure = {
          // Verify correct exception is returned
          assertEquals(exception, it)
        })

    // Get all the logs
    val logs = ShadowLog.getLogs()

    // Check for the debug log that should be generated
    val errorLog =
        logs.find {
          it.type == Log.ERROR &&
              it.tag == FileRepositoryFirebaseStorage.TAG &&
              it.msg == "Error uploading image: " + exceptionError
        }
    assert(errorLog != null) { "Expected error log was not found!" }
  }

  @Test
  fun downloadFileSuccess() {
    `when`(mockFileReference.getFile(any<File>())).thenReturn(mockFileDownloadTask)

    `when`(mockFileDownloadTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<File>
      listener.onSuccess(null)
      mockFileDownloadTask
    }

    mockStatic(File::class.java).use { FileMock ->
      // Create a mock File object
      val mockCacheDir = mock(File::class.java)
      val mockFile = mock(File::class.java)
      FileMock.`when`<File> { File.createTempFile(any(), any(), any()) }.thenReturn(mockFile)

      fileRepository.downloadFile(
          uid,
          FileType.PROFILE_PIC_JPEG,
          mockCacheDir,
          onSuccess = {
            // Verify that the onSuccess callback returns the mock File object
            assertEquals(mockFile, it)
          },
          onFailure = {
            // Failure callback
            assert(false)
          })

      fileRepository.downloadFile(
          uid,
          FileType.NOTE_PDF,
          mockCacheDir,
          onSuccess = {
            // Verify that the onSuccess callback returns the mock File object
            assertEquals(mockFile, it)
          },
          onFailure = {
            // Failure callback
            assert(false)
          })
    }
  }

  @Test
  fun downloadFileFailure() {
    val exceptionError = "Error"
    val exception = Exception(exceptionError)

    `when`(mockFileReference.getFile(any<File>())).thenReturn(mockFileDownloadTask)

    `when`(mockFileDownloadTask.addOnSuccessListener(any())).thenReturn(mockFileDownloadTask)
    `when`(mockFileDownloadTask.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockFileDownloadTask
    }

    mockStatic(File::class.java).use { FileMock ->
      // Create a mock File object
      val mockCacheDir = mock(File::class.java)
      val mockFile = mock(File::class.java)
      FileMock.`when`<File> { File.createTempFile(any(), any(), any()) }.thenReturn(mockFile)

      fileRepository.downloadFile(
          uid,
          FileType.PROFILE_PIC_JPEG,
          mockCacheDir,
          onSuccess = {
            // Success callback
            assert(false)
          },
          onFailure = {
            // Verify correct exception is returned
            assertEquals(exception, it)
          })

      fileRepository.downloadFile(
          uid,
          FileType.NOTE_PDF,
          mockCacheDir,
          onSuccess = {
            // Success callback
            assert(false)
          },
          onFailure = {
            // Verify correct exception is returned
            assertEquals(exception, it)
          })
    }

    // Get all the logs
    val logs = ShadowLog.getLogs()

    // Check for the debug log that should be generated
    val errorLog =
        logs.find {
          it.type == Log.ERROR &&
              it.tag == FileRepositoryFirebaseStorage.TAG &&
              it.msg == "Error downloading image: " + exceptionError
        }
    assert(errorLog != null) { "Expected error log was not found!" }
  }

  @Test
  fun deleteFileSuccess() {
    `when`(mockFileReference.delete()).thenReturn(mockDeleteTask)

    `when`(mockDeleteTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<TaskSnapshot>
      listener.onSuccess(null)
      mockDeleteTask
    }

    fileRepository.deleteFile(
        uid,
        FileType.PROFILE_PIC_JPEG,
        onSuccess = {
          // Success callback
          assert(true)
        },
        onFailure = {
          // Failure callback
          assert(false)
        })

    fileRepository.deleteFile(
        uid,
        FileType.NOTE_PDF,
        onSuccess = {
          // Success callback
          assert(true)
        },
        onFailure = {
          // Failure callback
          assert(false)
        })
  }

  @Test
  fun deleteFileFailure() {
    val exceptionError = "Error"
    val exception = Exception(exceptionError)

    `when`(mockFileReference.delete()).thenReturn(mockDeleteTask)

    `when`(mockDeleteTask.addOnSuccessListener(any())).thenReturn(mockDeleteTask)
    `when`(mockDeleteTask.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockDeleteTask
    }

    fileRepository.deleteFile(
        uid,
        FileType.PROFILE_PIC_JPEG,
        onSuccess = {
          // Success callback
          assert(false)
        },
        onFailure = {
          // Failure callback
          assertEquals(exception, it)
        })

    fileRepository.deleteFile(
        uid,
        FileType.NOTE_PDF,
        onSuccess = {
          // Success callback
          assert(false)
        },
        onFailure = {
          // Failure callback
          assertEquals(exception, it)
        })

    // Get all the logs
    val logs = ShadowLog.getLogs()

    // Check for the debug log that should be generated
    val errorLog =
        logs.find {
          it.type == Log.ERROR &&
              it.tag == FileRepositoryFirebaseStorage.TAG &&
              it.msg == "Error deleting image: " + exceptionError
        }
    assert(errorLog != null) { "Expected error log was not found!" }
  }

  @Test
  fun getFileSuccess() {
    `when`(mockFileReference.getBytes(any())).thenReturn(mockGetFileTask)

    `when`(mockGetFileTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<ByteArray>
      listener.onSuccess(ByteArray(10))
      mockGetFileTask
    }

    fileRepository.getFile(
        uid,
        FileType.PROFILE_PIC_JPEG,
        onSuccess = {
          // Verify that the onSuccess callback returns the mock File object
          assert(true)
        },
        onFailure = {
          // Failure callback
          assert(false)
        })

    fileRepository.getFile(
        uid,
        FileType.NOTE_PDF,
        onSuccess = {
          // Verify that the onSuccess callback returns the mock File object
          assert(true)
        },
        onFailure = {
          // Failure callback
          assert(false)
        })
  }

  @Test
  fun getFileFailure() {
    val exceptionError = "Error"
    val exception = Exception(exceptionError)

    `when`(mockFileReference.getBytes(any())).thenReturn(mockGetFileTask)

    `when`(mockGetFileTask.addOnSuccessListener(any())).thenReturn(mockGetFileTask)
    `when`(mockGetFileTask.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockGetFileTask
    }

    fileRepository.getFile(
        uid,
        FileType.PROFILE_PIC_JPEG,
        onSuccess = {
          // Success callback
          assert(false)
        },
        onFailure = {
          // Verify correct exception is returned
          assertEquals(exception, it)
        })

    fileRepository.getFile(
        uid,
        FileType.NOTE_PDF,
        onSuccess = {
          // Success callback
          assert(false)
        },
        onFailure = {
          // Verify correct exception is returned
          assertEquals(exception, it)
        })

    // Get all the logs
    val logs = ShadowLog.getLogs()

    // Check for the debug log that should be generated
    val errorLog =
        logs.find {
          it.type == Log.ERROR &&
              it.tag == FileRepositoryFirebaseStorage.TAG &&
              it.msg == "Error getting image: " + exceptionError
        }
    assert(errorLog != null) { "Expected error log was not found!" }
  }

  @Test
  fun getFileTooLargeFailure() {
    val exception = IndexOutOfBoundsException()

    `when`(mockFileReference.getBytes(any())).thenReturn(mockGetFileTask)

    `when`(mockGetFileTask.addOnSuccessListener(any())).thenReturn(mockGetFileTask)
    `when`(mockGetFileTask.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockGetFileTask
    }

    fileRepository.getFile(
        uid,
        FileType.PROFILE_PIC_JPEG,
        onSuccess = {
          // Success callback
          assert(false)
        },
        onFailure = {
          // Verify correct exception is returned
          assertEquals(exception, it)
        })

    fileRepository.getFile(
        uid,
        FileType.NOTE_PDF,
        onSuccess = {
          // Success callback
          assert(false)
        },
        onFailure = {
          // Verify correct exception is returned
          assertEquals(exception, it)
        })

    // Get all the logs
    val logs = ShadowLog.getLogs()

    // Check for the debug log that should be generated
    val errorLog =
        logs.find {
          it.type == Log.ERROR &&
              it.tag == FileRepositoryFirebaseStorage.TAG &&
              it.msg == "File too large, max size is ${MAX_FILE_SIZE}"
        }
    assert(errorLog != null) { "Expected error log was not found!" }
  }
}
