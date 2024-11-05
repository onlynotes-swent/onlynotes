package com.github.onlynotesswent.utils

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.dhaval2404.imagepicker.ImagePicker

open class ProfilePictureTaker(private val activity: ComponentActivity) {

  private val imagePickerLauncher: ActivityResultLauncher<Intent>
  private var onImageSelected: (Uri?) -> Unit = {}

  fun setOnImageSelected(onImageSelected: (Uri?) -> Unit) {
    this.onImageSelected = onImageSelected
  }

  init {
    imagePickerLauncher =
        (activity).registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
          val uri = result.data?.data
          onImageSelected(uri)
        }
  }

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
