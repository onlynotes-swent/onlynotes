package com.github.onlynotesswent.model.ocr

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

/**
 * TextRecognizer class that extracts text from an image URI using Google ML Kit’s Text Recognition
 * v2 API. https://developers.google.com/ml-kit/vision/text-recognition/v2 inspired by:
 * https://github.com/mundodigitalpro/MLVisionKotlin/tree/master
 *
 * @param activity the ComponentActivity that will use the TextRecognizer
 */
class TextRecognizer(private val activity: ComponentActivity) {
  private lateinit var textRecognitionLauncher: ActivityResultLauncher<String>
  private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

  /**
   * Initializes the activity result launcher to handle the image result. To be called in the
   * `onCreate` method of the activity.
   */
  fun init() {
    textRecognitionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
          uri?.let { extractTextFromImage(it) }
              ?: Toast.makeText(activity, "Failed to load image", Toast.LENGTH_LONG).show()
        }
  }

  /** Launches an intent to pick an image from the gallery. */
  fun scanImage() {
    textRecognitionLauncher.launch("image/*")
  }

  /**
   * Extracts text from the given image URI and displays it.
   *
   * @param imageUri the URI of the image from which to extract text
   */
  private fun extractTextFromImage(imageUri: Uri) {
    val inputImage: InputImage
    try {
      inputImage = InputImage.fromFilePath(activity, imageUri)
    } catch (e: IOException) {
      Toast.makeText(activity, "Error reading image: ${e.message}", Toast.LENGTH_LONG).show()
      return
    }

    textRecognizer
        .process(inputImage)
        .addOnSuccessListener { visionText ->
          if (visionText.text.isNotEmpty()) {
              shareText(visionText.text)
          } else {
            Toast.makeText(activity, "No text found in the image", Toast.LENGTH_LONG).show()
          }
        }
        .addOnFailureListener { e ->
          Toast.makeText(activity, "Text recognition failed: ${e.message}", Toast.LENGTH_LONG)
              .show()
        }
  }

  /**
   * Shares the recognized text using an Android sharing intent.
   *
   * @param text the text to share
   */
  private fun shareText(text: String) {
    val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, text)
          putExtra(Intent.EXTRA_TITLE, "Recognized Text")
        }
    activity.startActivity(Intent.createChooser(shareIntent, "Share text"))
  }

  companion object {
    const val TAG = "TextRecognizer"
  }
}
