package com.github.onlynotesswent.model.file

import android.content.Context
import android.net.Uri
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
  NOTE_TEXT(".md")
}

/**
 * ViewModel for managing file operations using a FileRepository.
 *
 * @property repository The repository used for file operations.
 */
class FileViewModel(private val repository: FileRepository) : ViewModel() {

  init {
    repository.init {}
  }

    /**
     * Factory for creating a FileViewModel.
     */
  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { FileViewModel(FileRepositoryFirebaseStorage(Firebase.storage)) }
    }
  }

  /**
   * Uploads a file to the repository.
   *
   * @param uid The unique identifier attached to the file, also functions as it's name. For profile
   *   pictures, it's the user's UID For documents/texts of a note, it's the note's UID.
   * @param fileUri The URI of the file to upload.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or
   *   PNG) or a note file (PDF or MD).
   */
  fun uploadFile(uid: String, fileUri: Uri, fileType: FileType) {
    repository.uploadFile(uid, fileUri, fileType, {}, {})
  }

  /**
   * Downloads a file from the repository.
   *
   * @param uid The unique identifier for the file, also functions as it's name. For profile
   *   pictures, it's the user's UID For documents/texts of a note, it's the note's UID.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or
   *   PNG) or a note file (PDF or MD).
   * @param context The context used to access the cache directory.
   * @param onSuccess The function to call when the download is successful, where the file is
   *   downloaded to the File.
   * @param onFailure The function to call when the download fails.
   */
  fun downloadFile(
      uid: String,
      fileType: FileType,
      context: Context,
      onSuccess: (File) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.downloadFile(uid, fileType, context.cacheDir, onSuccess, onFailure)
  }

  /**
   * Deletes a file from the repository.
   *
   * @param uid The unique identifier for the file, also functions as it's name. For profile
   *   pictures, it's the user's UID For documents/texts of a note, it's the note's UID.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or
   *   PNG) or a note file (PDF or MD).
   */
  fun deleteFile(uid: String, fileType: FileType) {
    repository.deleteFile(uid, fileType, {}, {})
  }

  /**
   * Updates a file in the repository.
   *
   * @param fileUri The URI of the image to update.
   * @param uid The unique identifier for the file, also functions as it's name. For profile
   *   pictures, it's the user's UID For documents/texts of a note, it's the note's UID.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or
   *   PNG) or a note file (PDF or MD).
   */
  fun updateFile(uid: String, fileUri: Uri, fileType: FileType) {
    repository.updateFile(uid, fileUri, fileType, {}, {})
  }

  /**
   * Retrieves a file from the repository as a byte array.
   *
   * @param uid The unique identifier for the file, also functions as it's name. For profile
   *   pictures, it's the user's UID For documents/texts of a note, it's the note's UID.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or
   *   PNG) or a note file (PDF or MD).
   * @param onSuccess The function to call when the retrieval is successful, with the file data in
   *   the ByteArray.
   * @param onFailure The function to call when the retrieval fails.
   */
  fun getFile(
      uid: String,
      fileType: FileType,
      onSuccess: (ByteArray) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.getFile(uid, fileType, onSuccess, onFailure)
  }
}
