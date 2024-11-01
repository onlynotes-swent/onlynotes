package com.github.onlynotesswent.utils

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.github.dhaval2404.imagepicker.ImagePicker

open class ProfilePictureTaker(
    private val activity: ComponentActivity,
    var onImageSelected: (Uri?) -> Unit
) {

  private val imagePickerLauncher =
      (activity).registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
          result ->
        val uri = result.data?.data
        onImageSelected(uri)
      }

  fun pickImage() {
    ImagePicker.with(activity)
        .cropSquare() // Crop the image to a square shape
        .galleryOnly() // Use only gallery for selection
        .createIntent { intent -> imagePickerLauncher.launch(intent) }
  }
}
