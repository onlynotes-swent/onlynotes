package com.github.onlynotesswent.utils

/* Using the Google ML Kit Document Scanner API to scan documents:
   https://developers.google.com/ml-kit/vision/doc-scanner/android
  Inspired by: https://github.com/googlesamples/mlkit/tree/master/android/documentscanner
*/

import android.app.Activity
import android.content.ActivityNotFoundException
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.content.FileProvider
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File

/**
 * Scanner class that initializes the scanner and handles the scanning activity
 *
 * @param activity the ComponentActivity that will use the scanner
 * @param scanner the scanner object, initialized by default with the options specified
 *
 * Options for the scanner, in parenthesis are other options that can be set:
 * 1. Scanner mode: Base (Base with filter, Full (ML capabilities))
 * 2. Result format: pdf (jpg, both)
 * 3. Gallery import allowed: true (false)
 * 4. Page limit: none set (> 1, upper limit determined by hardware resources)
 *
 * ToDo, potentially enable each user to choose the options they want
 */
open class Scanner(
    private val activity: ComponentActivity,
    private var scanner: GmsDocumentScanner =
        GmsDocumentScanning.getClient(
            GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .build())
) {

  private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>

  private val fileProviderAuthority = "com.github.onlynotesswent.provider"

  /**
   * Initializes the scanner and the activity result launcher (to obtain the result of the scan in
   * the current activity). To be called in the onCreate method of the activity that will use the
   * scanner
   */
  fun init() {
    scannerLauncher =
        activity.registerForActivityResult(StartIntentSenderForResult()) { result ->
          handleActivityResult(result)
        }
  }

  /** Starts the scanning activity */
  fun scan() {
    scanner
        .getStartScanIntent(activity)
        .addOnSuccessListener { intentSender ->
          try {
            scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
          } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Failed to launch scanner: ${e.message}")
            Toast.makeText(activity, "Failed to launch scanner", Toast.LENGTH_LONG).show()
          }
        }
        .addOnFailureListener { e: Exception ->
          Log.e(TAG, "Failed to scan: ${e.message}")
          Toast.makeText(activity, "Failed to scan: ${e.message}", Toast.LENGTH_LONG).show()
        }
  }

  /**
   * Handles the result of the scanning activity
   *
   * @param activityResult the result of the scanning activity
   *
   * TODO: Once we know what to do with the pdf file, this function will be updated
   */
  private fun handleActivityResult(activityResult: ActivityResult) {
    val resultCode = activityResult.resultCode

    // Get the GmsDocumentScanningResult from the activity result
    // If the result is correctly returned, share the pdf file
    val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
    if (resultCode == Activity.RESULT_OK && result != null) {
      val path = result.pdf?.uri?.path
      if (path != null) {
        try {
          val externalUri = FileProvider.getUriForFile(activity, fileProviderAuthority, File(path))
          // TODO Currently only showing the pdf file returned Uri in toast, to change later
          Toast.makeText(
                  activity,
                  "Scanner success, pdf handling not implemented: $externalUri",
                  Toast.LENGTH_LONG)
              .show()
        } catch (e: IllegalArgumentException) {
          // Shouldn't happen, so no toast is shown
          Log.e(TAG, "$path is outside the paths supported by the File provider.")
        }
      } else {
        Log.e(TAG, "Path to pdf file is null")
        Toast.makeText(activity, "Scanned pdf not found", Toast.LENGTH_SHORT).show()
      }
    } else if (resultCode == Activity.RESULT_CANCELED) {
      Log.d(TAG, "Scanner cancelled")
      Toast.makeText(activity, "Scanner cancelled", Toast.LENGTH_SHORT).show()
    } else {
      Log.e(TAG, "Scanner failed with resultCode: $resultCode")
      Toast.makeText(activity, "Scanner failed", Toast.LENGTH_SHORT).show()
    }
  }

  companion object {
    const val TAG = "Scanner"
  }
}
