package com.github.onlynotesswent.model.images

import android.media.Image

interface ImageRepository {



    fun getNewUid(): String

    /**
     * Uploads an image to Firebase Storage.
     *
     * @param image The image to upload.
     * @param onSuccess The function to call when the upload is successful.
     * @param onFailure The function to call when the upload fails.
     */
    fun uploadImage(image: Image, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Downloads an image from Firebase Storage.
     *
     * @param image The image to download.
     * @param onSuccess The function to call when the download is successful.
     * @param onFailure The function to call when the download fails.
     */
    fun downloadImage(image: Image, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Deletes an image from Firebase Storage.
     *
     * @param image The image to delete.
     * @param onSuccess The function to call when the deletion is successful.
     * @param onFailure The function to call when the deletion fails.
     */
    fun deleteImage(image: Image, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Updates an image in Firebase Storage.
     *
     * @param image The image to update.
     * @param onSuccess The function to call when the update is successful.
     * @param onFailure The function to call when the update fails.
     */
    fun updateImage(image: Image, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Gets an image from Firebase Storage.
     *
     * @param image The image to get.
     * @param onSuccess The function to call when the get is successful.
     * @param onFailure The function to call when the get fails.
     */
    fun getImage(image: Image, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Gets all images from Firebase Storage.
     *
     * @param onSuccess The function to call when the get is successful.
     * @param onFailure The function to call when the get fails.
     */
    fun getAllImages(onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Gets all images from Firebase Storage for a specific user.
     *
     * @param userId The user ID to get the images for.
     * @param onSuccess The function to call when the get is successful.
     * @param
     */

}
