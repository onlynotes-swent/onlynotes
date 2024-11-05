package com.github.onlynotesswent.utils

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.dhaval2404.imagepicker.ImagePicker

/**
 * ProfilePictureTaker class that use the image picker to select a profile picture
 *
 * @param activity the ComponentActivity that will use the image picker
 */
open class ProfilePictureTaker(private val activity: ComponentActivity) {

  private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
  private var onImageSelected: (Uri?) -> Unit = {}

  /**
   * Sets the onImageSelected lambda function to be called when an image is selected
   *
   * @param onImageSelected the lambda function to be called when an image is selected
   */
  fun setOnImageSelected(onImageSelected: (Uri?) -> Unit) {
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
   * Opens the image picker to select a profile picture The image picker is configured to crop the
   * image to a square shape and use only the gallery for selection
   */
  open fun pickImage() {
    ImagePicker.with(activity)
        .cropSquare() // Crop the image to a square shape
        .galleryOnly() // Use only gallery for selection
        .createIntent { intent ->
          try {
            imagePickerLauncher.launch(intent)
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
  }
}
