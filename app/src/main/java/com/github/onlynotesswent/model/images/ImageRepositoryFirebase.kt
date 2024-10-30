package com.github.onlynotesswent.model.images

import android.media.Image
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ImageRepositoryFirebase(private val db: FirebaseStorage) : ImageRepository {

        private val collectionPath = "images"

        override fun getNewUid(): String {
            TODO("Not yet implemented")
        }

        override fun uploadImage(image: Image, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

        }

    override fun downloadImage(
        image: Image,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteImage(image: Image, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun updateImage(image: Image, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getImage(image: Image, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getAllImages(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        TODO("Not yet implemented")
    }

}
