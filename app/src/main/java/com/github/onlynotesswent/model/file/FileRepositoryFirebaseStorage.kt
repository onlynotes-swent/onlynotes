package com.github.onlynotesswent.model.file

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import java.io.File

/** Maximum file size in bytes for downloading directly to memory: 50 MB */
const val MAX_FILE_SIZE: Long = 50 * 1024 * 1024

class FileRepositoryFirebaseStorage(private val db: FirebaseStorage) : FileRepository {

  private val noteFileName = "note"

  private val profilePicFolderRef = db.reference.child("profile_pics")
  private val notesFilesFolderRef = db.reference.child("notes")

  override fun init(onSuccess: () -> Unit) {
    onSuccess()
  }

  override fun uploadFile(
      uid: String,
      fileUri: Uri,
      fileType: FileType,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {

    // Possibility of adding support for lifecycle changes, complex
    getFileRef(uid, fileType)
        .putFile(fileUri)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener {
          Log.e(TAG, "Error uploading file.", it)
          onFailure(it)
        }
  }

  override fun downloadFile(
      uid: String,
      fileType: FileType,
      cacheDir: File,
      onSuccess: (File) -> Unit,
      onFileNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {

    // Naming convention for the local file:
    // uid.fileExtension (e.g. 2RkHrdA7zZrd4os3685f.jpg, 2RkHrdA7zZrd4os3685f.pdf)
    // for profile pictures, uid is the user's uid,
    // for notes, uid is the note's uid

    val localFile = File.createTempFile(uid, fileType.fileExtension, cacheDir)

    getFileRef(uid, fileType)
        .getFile(localFile)
        .addOnSuccessListener { onSuccess(localFile) }
        .addOnFailureListener { handleException("downloading", it, onFileNotFound, onFailure) }
  }

  override fun deleteFile(
      uid: String,
      fileType: FileType,
      onSuccess: () -> Unit,
      onFileNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getFileRef(uid, fileType)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { handleException("deleting", it, onFileNotFound, onFailure) }
  }

  // Unnecessary as uploading with the same name will overwrite the image, but we can add it for
  // clarity and possible future additions/implementations
  override fun updateFile(
      uid: String,
      fileUri: Uri,
      fileType: FileType,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    uploadFile(uid, fileUri, fileType, onSuccess, onFailure)
  }

  override fun getFile(
      uid: String,
      fileType: FileType,
      onSuccess: (ByteArray) -> Unit,
      onFileNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getFileRef(uid, fileType)
        .getBytes(MAX_FILE_SIZE)
        .addOnSuccessListener { onSuccess(it) }
        .addOnFailureListener { error ->
          if (error is IndexOutOfBoundsException) {
            Log.e(TAG, "File too large, max size is $MAX_FILE_SIZE", error)
            onFailure(error)
          } else {
            handleException("getting", error, onFileNotFound, onFailure)
          }
        }
  }

  /**
   * Returns the file reference based on the UID and file type, according to our storage structure:
   * - profile_pics
   *     - userUid.jpg or userUid.png (which stores that user's profile picture)
   *     - ...
   * - notes
   *     - noteUid
   *         - note.md (Markdown text file associated to this note)
   *         - note.pdf (Pdf file associated to this note)
   *     - ...
   *
   * @param uid The unique identifier for the file, also functions as it's name. For profile
   *   pictures, it's the user's UID For documents/texts of a note, it's the note's UID.
   * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or
   *   PNG) or a note file (PDF or MD).
   * @return The file reference.
   */
  private fun getFileRef(uid: String, fileType: FileType): StorageReference {
    return when (fileType) {
      FileType.PROFILE_PIC_JPEG -> profilePicFolderRef.child(uid + fileType.fileExtension)
      FileType.NOTE_PDF,
      FileType.NOTE_TEXT ->
          notesFilesFolderRef.child(uid).child(noteFileName + fileType.fileExtension)
    }
  }

  /**
   * Handles exceptions (which should be `StorageException`) that occur when performing an action on
   * a file that could not . Handles the case where the file is not found.
   *
   * @param failedActionString The string representation of the failed action. For example, if
   *   downloading the fail failed, this would be "downloading".
   * @param exception The exception that occurred.
   * @param onFileNotFound The function to call when the file is not found.
   * @param onFailure The function to call when the action fails.
   */
  private fun handleException(
      failedActionString: String,
      exception: Exception,
      onFileNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val errorCode = (exception as StorageException).errorCode
    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
      // Check should be external, since notes have a hasPdf and hasText field
      Log.e(TAG, "File not found.", exception)
      onFileNotFound()
    } else {
      Log.e(TAG, "Error $failedActionString file.", exception)
      onFailure(exception)
    }
  }

  companion object {
    const val TAG = "FileRepositoryFirebaseStorage"
  }
}
