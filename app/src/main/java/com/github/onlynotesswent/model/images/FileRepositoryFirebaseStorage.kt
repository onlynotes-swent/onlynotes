package com.github.onlynotesswent.model.images

import android.net.Uri
import android.util.Log
import com.github.onlynotesswent.model.note.Type
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.component1
import com.google.firebase.storage.component2
import java.io.File

class FileRepositoryFirebaseStorage(private val db: FirebaseStorage) : FileRepository {

    private val noteFileName = "note"

    private val profilePicFolderRef = db.reference.child("profile_pics")
    private val notesFilesFolderRef = db.reference.child("notes")

    private val MAX_FILE_SIZE : Long = 50 * 1024 * 1024 //50 MB

    override fun init(onSuccess: () -> Unit) {
        onSuccess()
    }

    override fun uploadFile(uid: String, fileUri: Uri, fileType: Type, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

        //Possibility of adding support for lifecycle changes, complex
        getFileRef(uid, fileType).putFile(fileUri)
            .addOnProgressListener { (bytesTransferred, totalByteCount) ->
                // TODO: Remove this log, or add progress bar
                val progress = (100.0 * bytesTransferred) / totalByteCount
                Log.d(TAG, "Upload is $progress% done")
            }.addOnPausedListener {
                Log.d(TAG, "Upload is paused")
            }.addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override fun downloadFile(uid: String, fileType: Type, cacheDir: File, onSuccess: (File) -> Unit, onFailure: (Exception) -> Unit) {

        // Naming convention for the local file:
        // uid.fileExtension (e.g. 2RkHrdA7zZrd4os3685f.jpg, 2RkHrdA7zZrd4os3685f.pdf)
        // for profile pictures, uid is the user's uid,
        // for notes, uid is the note's uid

        val localFile = File.createTempFile(uid, fileType.fileExtension, cacheDir)

        getFileRef(uid, fileType).getFile(localFile)
            .addOnSuccessListener {
                onSuccess(localFile)
            }
            .addOnFailureListener {
                Log.e(TAG, "Error downloading image", it)
                onFailure(it)
            }
    }

    override fun deleteFile(uid: String, fileType: Type, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        getFileRef(uid, fileType).delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                Log.e(TAG, "Error downloading image", it)
                onFailure(it)
            }
    }

    // Unnecessary as uploading with the same name will overwrite the image, but we can add it for clarity
    // and possible future additions/implementations
    override fun updateFile(uid: String, fileUri: Uri, fileType: Type, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        uploadFile(uid, fileUri, fileType, onSuccess, onFailure)
    }

    override fun getFile(uid: String, fileType: Type, onSuccess: (ByteArray) -> Unit, onFailure: (Exception) -> Unit) {
        getFileRef(uid, fileType).getBytes(MAX_FILE_SIZE)
            .addOnSuccessListener {
                onSuccess(it)
            }
            .addOnFailureListener {
                if (it is IndexOutOfBoundsException) {
                    Log.e(TAG, "File too large, max size is $MAX_FILE_SIZE", it)
                } else{
                    Log.e(TAG, "Error getting image", it)
                }
                onFailure(it)
            }
    }

    private fun getFileRef(uid: String, fileType: Type): StorageReference {
        return when (fileType) {
            Type.PNG, Type.JPEG -> profilePicFolderRef.child(uid + fileType.fileExtension)
            Type.PDF, Type.TEXT -> notesFilesFolderRef.child(uid).child(noteFileName +  fileType.fileExtension)
        }
    }


    companion object {
        const val TAG = "FileRepositoryFirebaseStorage"
    }
}
