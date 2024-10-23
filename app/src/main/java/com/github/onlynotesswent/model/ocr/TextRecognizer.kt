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
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

/* Using Google ML Kit’s Text Recognition v2 API to extract text from an image:
  https://developers.google.com/ml-kit/vision/text-recognition/v2
 Inspired by: https://github.com/mundodigitalpro/MLVisionKotlin/tree/master
*/

/**
 * TextRecognizer class that extracts text from an image URI
 *
 * @param activity the ComponentActivity that will use the TextRecognizer
 * @param textRecognizer the TextRecognizer object, initialized with the default options
 */
class TextRecognizer(
    private val activity: ComponentActivity,
    private val textRecognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
) {

  private lateinit var textRecognitionLauncher: ActivityResultLauncher<String>

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
      Log.e(TAG, "Error reading image", e)
      return
    }

    textRecognizer
        .process(inputImage)
        .addOnSuccessListener { visionText ->
          if (visionText.text.isNotEmpty()) {
            shareText(visionText.text)
          } else {
            Toast.makeText(activity, "No text found in the image", Toast.LENGTH_LONG).show()
            Log.d(TAG, "No text found in the image")
          }
        }
        .addOnFailureListener { e ->
          Toast.makeText(activity, "Text recognition failed: ${e.message}", Toast.LENGTH_LONG)
              .show()
          Log.e(TAG, "Text recognition failed", e)
        }
  }

  /**
   * Shares the recognized text using an Android sharing intent.
   *
   * @param text the text to share
   *
   * TODO: Update function when we know what to do with the recognized text
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
