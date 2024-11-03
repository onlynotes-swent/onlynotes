package com.github.onlynotesswent.model.images

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.onlynotesswent.model.note.Type
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.File


/**
 * ViewModel for managing file operations using a FileRepository.
 *
 * @property repository The repository used for file operations.
 */
class FileViewModel(private val repository: FileRepository) : ViewModel() {

    init {
        repository.init {}
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FileViewModel(FileRepositoryFirebaseStorage(Firebase.storage)) as T
                }
            }
    }

    /**
     * Uploads a note file to the repository.
     *
     * @param uid The unique identifier attached to the file, also functions as it's name.
     *      For profile pictures, it's the user's UID
     *      For documents/texts of a note, it's the note's UID.
     * @param fileUri The URI of the file to upload.
     * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or PNG)
     *      or a note file (PDF or MD).
     */
    fun uploadNoteFile(uid: String, fileUri: Uri, fileType: Type) {
        repository.uploadFile(uid, fileUri, fileType, {}, {})
    }

    /**
     * Downloads a file from the repository.
     *
     * @param uid The unique identifier for the file, also functions as it's name.
     *      For profile pictures, it's the user's UID
     *      For documents/texts of a note, it's the note's UID.
     * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or PNG)
     *      or a note file (PDF or MD).
     * @param context The context used to access the cache directory.
     * @param onSuccess The function to call when the download is successful, where the file is downloaded to the File.
     * @param onFailure The function to call when the download fails.
     */
    fun downloadFile(uid: String, fileType: Type, context: Context, onSuccess: (File) -> Unit, onFailure: (Exception) -> Unit) {
        repository.downloadFile(uid, fileType, context.cacheDir, onSuccess, onFailure)
    }

    /**
     * Deletes a file from the repository.
     *
     * @param uid The unique identifier for the file, also functions as it's name.
     *      For profile pictures, it's the user's UID
     *      For documents/texts of a note, it's the note's UID.
     * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or PNG)
     *      or a note file (PDF or MD).
     */
    fun deleteFile(uid: String, fileType: Type) {
        repository.deleteFile(uid, fileType, {}, {})
    }

    /**
     * Updates a file in the repository.
     *
     * @param fileUri The URI of the image to update.
     * @param uid The unique identifier for the file, also functions as it's name.
     *      For profile pictures, it's the user's UID
     *      For documents/texts of a note, it's the note's UID.
     * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or PNG)
     *      or a note file (PDF or MD).
     */
    fun updateFile(uid: String, fileUri: Uri, fileType: Type) {
        repository.updateFile(uid, fileUri, fileType, {}, {})
    }

    /**
     * Retrieves a file from the repository as a byte array.
     *
     * @param uid The unique identifier for the file, also functions as it's name.
     *      For profile pictures, it's the user's UID
     *      For documents/texts of a note, it's the note's UID.
     * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or PNG)
     *      or a note file (PDF or MD).
     * @param onSuccess The function to call when the retrieval is successful, with the file data in the ByteArray.
     * @param onFailure The function to call when the retrieval fails.
     */
    fun getFile(uid: String, fileType: Type, onSuccess: (ByteArray) -> Unit, onFailure: (Exception) -> Unit) {
        repository.getFile(uid, fileType, onSuccess, onFailure)
    }

}