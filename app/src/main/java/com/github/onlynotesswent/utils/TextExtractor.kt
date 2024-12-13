package com.github.onlynotesswent.utils

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
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
class TextExtractor(
    private val activity: ComponentActivity,
    private val textRecognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
) {

  /**
   * Processes the given PDF file to extract text from it.
   *
   * @param pdfFile the PDF file to extract text from.
   * @param onSuccess the lambda function to call with the extracted text.
   */
  fun processPdfFile(pdfFile: File, onSuccess: (String) -> Unit) {
    try {
      val bitmaps = convertPdfToBitmap(pdfFile)
      extractTextFromBitmaps(bitmaps, onSuccess)
    } catch (e: Exception) {
      Log.e(TAG, "Error while converting PDF to bitmap", e)
      Toast.makeText(activity, "Error: text recognition failed", Toast.LENGTH_SHORT).show()
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
        bitmap.recycle()
        page.close()
      }
    } finally {
      pdfRenderer?.close()
      fileDescriptor?.close()
    }

    return bitmaps
  }

  /**
   * Extracts text from the given list of bitmaps.
   *
   * @param bitmaps the list of bitmaps to extract text from.
   * @param onSuccess the lambda function to call with the extracted text.
   */
  internal fun extractTextFromBitmaps(bitmaps: List<Bitmap>, onSuccess: (String) -> Unit) {
    val textResults = StringBuilder()

    bitmaps.forEachIndexed { index, bitmap ->
      val image = InputImage.fromBitmap(bitmap, 0)
      textRecognizer
          .process(image)
          .addOnSuccessListener { visionText ->
            val text = visionText.text
            if (text.isNotEmpty()) textResults.append(text).append("\n")
            if (index == bitmaps.size - 1) onSuccess(textResults.toString())
          }
          .addOnFailureListener { e ->
            Log.e(TAG, "Text recognition failed", e)
            Toast.makeText(activity, "Error: text recognition failed", Toast.LENGTH_SHORT).show()
          }
    }
  }

  companion object {
    const val TAG = "TextExtractor"
  }
}
