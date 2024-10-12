package com.github.onlynotesswent.model.scanner

/* Using the Google ML Kit Document Scanner API to scan documents:
   https://developers.google.com/ml-kit/vision/doc-scanner/android
  Inspired by: https://github.com/googlesamples/mlkit/tree/master/android/documentscanner
*/

import android.app.Activity
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
class Scanner(
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
          scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        }
        .addOnFailureListener() { e: Exception ->
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
        val externalUri = FileProvider.getUriForFile(activity, fileProviderAuthority, File(path))

        // TODO Currently only sharing the pdf file returned (using android send intent),
        // to be determined what to do with the pdf file later, accessible through externalUri
        /*val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
              putExtra(Intent.EXTRA_STREAM, externalUri)
              type = "application/pdf"
              addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        activity.startActivity(Intent.createChooser(shareIntent, "Share pdf"))*/
      } else {
        Log.e(TAG, "Path to pdf file is null")
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
    const val TAG = "MainActivity"
  }
}
