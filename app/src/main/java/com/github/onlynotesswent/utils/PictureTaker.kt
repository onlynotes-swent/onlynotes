package com.github.onlynotesswent.utils

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.dhaval2404.imagepicker.ImagePicker

/**
 * PictureTaker class that use the image picker to select a picture
 *
 * @param activity the ComponentActivity that will use the image picker
 */
open class PictureTaker(private val activity: ComponentActivity) {

  private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
  private var onImageSelected: (Uri?) -> Unit = {}

  /**
   * Sets the onImageSelected lambda function to be called when an image is selected
   *
   * @param onImageSelected the lambda function to be called when an image is selected
   */
  open fun setOnImageSelected(onImageSelected: (Uri?) -> Unit) {
    this.onImageSelected = onImageSelected
  }

  /** Initializes the imagePickerLauncher to obtain the result of the image selection. */
  fun init() {
    imagePickerLauncher =
        (activity).registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
          val uri = result.data?.data
          onImageSelected(uri)
        }
  }

  /**
   * Opens the image picker to select a picture. The image picker is configured to crop the
   * image to a square shape and use only the gallery for selection
   */
  open fun pickImage(fromGalleryOnly: Boolean = true, cropToSquare: Boolean = true) {
    ImagePicker.with(activity)
        .let {
          if (cropToSquare) it.cropSquare() // Crop the image to a square shape
          else it
        }
        .let {
          if (fromGalleryOnly) it.galleryOnly() // Pick only from the gallery
          else it
        }
        .createIntent { intent ->
          try {
            imagePickerLauncher.launch(intent)
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
  }
}
