package com.github.onlynotesswent.utils

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

/**
 * TextExtractor class that detects latin-based text from a PDF file. The class uses the Google ML
 * Kit Text Recognition API to extract text from the PDF file.
 *
 * @param activity the ComponentActivity that will use the TextExtractor
 * @param textRecognizer the text recognizer object, initialized by default with the default options
 */
open class TextExtractor(
    private val activity: ComponentActivity,
    private val textRecognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
) {

  fun processPdfFile(pdfFile: File, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    try {
      val bitmaps = convertPdfToBitmap(pdfFile)
      extractTextFromBitmaps(bitmaps, onSuccess, onFailure)
    } catch (e: Exception) {
      Log.e(TAG, "Error while converting PDF to bitmap", e)
      onFailure(e)
    }
  }

  /**
   * Converts the given PDF file to a list of bitmaps.
   *
   * @param pdfFile the PDF file to convert to bitmaps
   * @return a list of bitmaps extracted from the PDF file
   * @throws Exception if the PDF file cannot be converted to bitmaps
   */
  internal fun convertPdfToBitmap(pdfFile: File): List<Bitmap> {
    val bitmaps = mutableListOf<Bitmap>()
    var fileDescriptor: ParcelFileDescriptor? = null
    var pdfRenderer: PdfRenderer? = null

    try {
      fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
      pdfRenderer = PdfRenderer(fileDescriptor)

      for (pageIndex in 0 until pdfRenderer.pageCount) {
        val page = pdfRenderer.openPage(pageIndex)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        bitmaps.add(bitmap)
        page.close()
      }
    } finally {
      pdfRenderer?.close()
      fileDescriptor?.close()
    }

    return bitmaps
  }

  internal fun extractTextFromBitmaps(
      bitmaps: List<Bitmap>,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val textResults = StringBuilder()
    var processedCount = 0
    val totalBitmaps = bitmaps.size
    var hasFailed = false
    val lock = Any()

    bitmaps.forEach { bitmap ->
      val image = InputImage.fromBitmap(bitmap, 0)
      textRecognizer
          .process(image)
          .addOnSuccessListener { visionText ->
            synchronized(lock) {
              if (!hasFailed) {
                val text = visionText.text
                if (text.isNotEmpty()) textResults.append(text).append("\n")
                processedCount++
                if (processedCount == totalBitmaps) onSuccess(textResults.toString())
              }
            }
          }
          .addOnFailureListener { e ->
            synchronized(lock) {
              if (!hasFailed) {
                hasFailed = true
                Log.e(TAG, "Text recognition failed", e)
                onFailure(e)
              }
            }
          }
          .addOnCompleteListener {
            bitmap.recycle() // Recycle the bitmap after processing
          }
    }
  }

  companion object {
    const val TAG = "TextExtractor"
  }
}
