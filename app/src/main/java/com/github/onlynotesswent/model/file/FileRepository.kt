package com.github.onlynotesswent.model.file

import android.net.Uri
import com.github.onlynotesswent.model.note.Type
import java.io.File

interface FileRepository {



    /**
     * Initializes the repository.
     *
     * @param onSuccess The function to call when the initialization is successful.
     */
    fun init(onSuccess: () -> Unit)

    /**
     * Uploads a file to Firebase Storage.
     *
     * @param uid The unique identifier for the file, also functions as it's name.
     *      For profile pictures, it's the user's UID
     *      For documents/texts of a note, it's the note's UID.
     * @param fileUri The URI of the file to upload.
     * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or PNG)
     *      or a note file (PDF or MD).
     * @param onSuccess The function to call when the upload is successful.
     * @param onFailure The function to call when the upload fails.
     */
    fun uploadFile(uid: String, fileUri: Uri, fileType: Type, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Downloads a file from Firebase Storage.
     *
     * @param uid The unique identifier for the file, also functions as it's name.
     *      For profile pictures, it's the user's UID
     *      For documents/texts of a note, it's the note's UID.
     * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or PNG)
     *      or a note file (PDF or MD).
     * @param cacheDir The directory to cache the downloaded file.
     * @param onSuccess The function to call when the download is successful.
     * @param onFailure The function to call when the download fails.
     */
    fun downloadFile(uid: String, fileType: Type, cacheDir: File, onSuccess: (File) -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Deletes a file from Firebase Storage.
     *
     * @param uid The unique identifier for the file, also functions as it's name.
     *      For profile pictures, it's the user's UID
     *      For documents/texts of a note, it's the note's UID.
     * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or PNG)
     *      or a note file (PDF or MD).
     * @param onSuccess The function to call when the deletion is successful.
     * @param onFailure The function to call when the deletion fails.
     */
    fun deleteFile(uid: String, fileType: Type, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Updates a file in Firebase Storage.
     *
     * @param uid The unique identifier for the file, also functions as it's name.
     *      For profile pictures, it's the user's UID
     *      For documents/texts of a note, it's the note's UID.
     * @param fileUri The URI of the file to update.
     * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or PNG)
     *      or a note file (PDF or MD).
     * @param onSuccess The function to call when the update is successful.
     * @param onFailure The function to call when the update fails.
     */
    fun updateFile(uid: String, fileUri: Uri,  fileType: Type, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Retrieves a file from Firebase Storage as a byte array.
     *
     * @param uid The unique identifier for the file, also functions as it's name.
     *      For profile pictures, it's the user's UID
     *      For documents/texts of a note, it's the note's UID.
     * @param fileType The type of the file. This type determines if it is a profile picture (JPEG or PNG)
     *      or a note file (PDF or MD).
     * @param onSuccess The function to call when the retrieval is successful.
     * @param onFailure The function to call when the retrieval fails.
     */
    fun getFile(uid: String, fileType: Type, onSuccess: (ByteArray) -> Unit, onFailure: (Exception) -> Unit)

}
