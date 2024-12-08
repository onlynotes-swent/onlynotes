package com.github.onlynotesswent.model.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.File

/**
 * Enum class for file types.
 *
 * PROFILE_PIC_JPEG: Profile picture in JPEG format. NOTE_PDF: PDF file for notes. NOTE_TEXT: Text
 * file for notes, in MarkDown format.
 *
 * @property fileExtension The file extension for the file type.
 */
enum class FileType(val fileExtension: String) {
  PROFILE_PIC_JPEG(".jpg"),
  NOTE_PDF(".pdf"),
  NOTE_TEXT(".md"),
  FLASHCARD_IMAGE(".jpg")
}

val fileProviderAuthority = "com.github.onlynotesswent.provider"

/**
 * ViewModel for managing file operations using a FileRepository.
 *
 * @property repository The repository used for file operations.
 */
open class FileViewModel(private val repository: FileRepository) : ViewModel() {

  init {
    repository.init {}
  }

  /** Factory for creating a FileViewModel. */
  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { FileViewModel(FileRepositoryFirebaseStorage(Firebase.storage)) }
    }
  }

  /**
   * Uploads a file to the repository.
   *
   * @param uid The unique identifier for the file, also functions as it's name. For profile
   *   pictures, it's the user's UID; for documents/texts of a note, it's the note's UID; for
   *   flashcards images, it's the flashcard's UID.
   * @param fileUri The URI of the file to upload.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG), a
   *   note file (PDF or MD) or a flashcard image (JPEG).
   * @param onSuccess The function to call when the upload is successful.
   * @param onFailure The function to call when the upload fails.
   */
  fun uploadFile(
      uid: String,
      fileUri: Uri,
      fileType: FileType,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.uploadFile(uid, fileUri, fileType, onSuccess, onFailure)
  }

  /**
   * Downloads a file from the repository.
   *
   * @param uid The unique identifier for the file, also functions as it's name. For profile
   *   pictures, it's the user's UID; for documents/texts of a note, it's the note's UID; for
   *   flashcards images, it's the flashcard's UID.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG), a
   *   note file (PDF or MD) or a flashcard image (JPEG).
   * @param context The context used to access the cache directory.
   * @param onSuccess The function to call when the download is successful, where the file is
   *   downloaded to the File.
   * @param onFileNotFound The function to call when the file is not found.
   * @param onFailure The function to call when the download fails.
   */
  fun downloadFile(
      uid: String,
      fileType: FileType,
      context: Context,
      onSuccess: (File) -> Unit = {},
      onFileNotFound: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.downloadFile(uid, fileType, context.cacheDir, onSuccess, onFileNotFound, onFailure)
  }

  /**
   * Deletes a file from the repository.
   *
   * @param uid The unique identifier for the file, also functions as it's name. For profile
   *   pictures, it's the user's UID; for documents/texts of a note, it's the note's UID; for
   *   flashcards images, it's the flashcard's UID.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG), a
   *   note file (PDF or MD) or a flashcard image (JPEG).
   * @param onSuccess The function to call when the download is successful, where the file is
   *   downloaded to the File.
   * @param onFileNotFound The function to call when the file is not found.
   * @param onFailure The function to call when the delete fails.
   */
  fun deleteFile(
      uid: String,
      fileType: FileType,
      onSuccess: () -> Unit = {},
      onFileNotFound: () -> Unit = {},
      onFailure: (Exception) -> Unit = {},
  ) {
    repository.deleteFile(uid, fileType, onSuccess, onFileNotFound, onFailure)
  }

  /**
   * Updates a file in the repository.
   *
   * @param fileUri The URI of the image to update.
   * @param uid The unique identifier for the file, also functions as it's name. For profile
   *   pictures, it's the user's UID; for documents/texts of a note, it's the note's UID; for
   *   flashcards images, it's the flashcard's UID.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG), a
   *   note file (PDF or MD) or a flashcard image (JPEG).
   * @param onSuccess The function to call when the update is successful.
   * @param onFailure The function to call when the update fails.
   */
  fun updateFile(
      uid: String,
      fileUri: Uri,
      fileType: FileType,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.updateFile(uid, fileUri, fileType, onSuccess, onFailure)
  }

  /**
   * Retrieves a file from the repository as a byte array.
   *
   * @param uid The unique identifier for the file, also functions as it's name. For profile
   *   pictures, it's the user's UID; for documents/texts of a note, it's the note's UID; for
   *   flashcards images, it's the flashcard's UID.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG), a
   *   note file (PDF or MD) or a flashcard image (JPEG).
   * @param onSuccess The function to call when the retrieval is successful, with the file data in
   *   the ByteArray.
   * @param onFileNotFound The function to call when the file is not found.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getFile(
      uid: String,
      fileType: FileType,
      onSuccess: (ByteArray) -> Unit = {},
      onFileNotFound: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getFile(uid, fileType, onSuccess, onFileNotFound, onFailure)
  }

  /**
   * Opens the pdf file attached to a note
   *
   * @param uid The For documents/texts of a note, it's the note's UID.
   * @param context The context used to access the cache directory.
   * @param onSuccess The function to call when the opening is successful
   * @param onFileNotFound The function to call when the pdf is not found.
   * @param onFailure The function to call when the pdf opening fails.
   */
  fun openPdf(
      uid: String,
      context: Context,
      onSuccess: () -> Unit = {},
      onFileNotFound: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    // Download the file, then open it with a 3rd party PDF Viewer
    // TODO: Implement a PDF viewer in the app, possible, though maybe not
    // necessary as our pdfs will be view only,
    //  you can modify the text

    repository.downloadFile(
        uid = uid,
        fileType = FileType.NOTE_PDF,
        cacheDir = context.cacheDir,
        onSuccess = {
          onSuccess()
          // Create an intent to open the pdf with a 3rd party app
          val intent =
              Intent(Intent.ACTION_VIEW).apply {
                val uri = FileProvider.getUriForFile(context, fileProviderAuthority, it)
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
              }
          startActivity(context, intent, null)
        },
        onFileNotFound = onFileNotFound,
        onFailure = onFailure)
  }
}
