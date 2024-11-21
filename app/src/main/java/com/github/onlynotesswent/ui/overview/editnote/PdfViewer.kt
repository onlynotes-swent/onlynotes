package com.github.onlynotesswent.ui.overview.editnote

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.Scanner
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import java.io.File

/**
 * Composable function to display the PDF viewer screen. Allows the user to view, delete, and scan
 *
 * @param noteViewModel The NoteViewModel to access the selected note.
 * @param fileViewModel The FileViewModel to download and delete the PDF file.
 * @param scanner The Scanner object to scan the PDF file.
 * @param navigationActions The NavigationActions object to navigate between screens.
 */
@Composable
fun PdfViewerScreen(
    noteViewModel: NoteViewModel,
    fileViewModel: FileViewModel,
    scanner: Scanner,
    navigationActions: NavigationActions
) {

  // State to track the currently selected note
  val note by noteViewModel.selectedNote.collectAsState()
  val context = LocalContext.current

  // States to manage the PDF existence, file reference, retry logic, and attempts
  var pdfExists by remember { mutableStateOf(false) }
  var pdfFile by remember { mutableStateOf<File?>(null) }
  var retryDownload by remember { mutableStateOf(false) }
  var attempt by remember { mutableIntStateOf(0) }
  val maxAttempts = 5

  // Retry logic for downloading the file
  LaunchedEffect(retryDownload) {
    if (retryDownload && note != null) {
      while (attempt < maxAttempts) {
        attempt++
        fileViewModel.downloadFile(
            uid = note!!.id,
            fileType = FileType.NOTE_PDF,
            context = context,
            onSuccess = { file ->
              // If the download is successful, update the state and stop retrying
              pdfFile = file
              pdfExists = true
              Toast.makeText(context, "PDF updated successfully", Toast.LENGTH_SHORT).show()
              retryDownload = false
            },
            onFileNotFound = {
              // If the file is not found, check if max attempts have been reached
              pdfExists = false
              if (attempt == maxAttempts) {
                Toast.makeText(context, "PDF not found after multiple attempts", Toast.LENGTH_SHORT)
                    .show()
                retryDownload = false
              }
            },
            onFailure = {
              // If the download fails, check if max attempts have been reached
              pdfExists = false
              if (attempt == maxAttempts) {
                Toast.makeText(
                        context,
                        "Failed to download PDF after multiple attempts",
                        Toast.LENGTH_SHORT)
                    .show()
                retryDownload = false
              }
            })
        // Add a delay between retries to avoid rapid consecutive attempts
        kotlinx.coroutines.delay(1000)
      }
    }
  }

  Scaffold(
      floatingActionButton = {
        // Show a delete button if a PDF exists
        if (pdfExists) {
          FloatingActionButton(
              onClick = {
                // Delete the file and update the state
                fileViewModel.deleteFile(uid = note!!.id, fileType = FileType.NOTE_PDF)
                pdfFile = null
                pdfExists = false
                Toast.makeText(context, "PDF deleted successfully", Toast.LENGTH_SHORT).show()
              },
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete PDF")
              }
        }
      },
      topBar = {
        // Top bar with navigation back button
        ScreenTopBar(
            title = "View PDF",
            titleTestTag = "pdfViewerTitle",
            onBackClick = { navigationActions.goBack() },
            icon = {
              Icon(
                  imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                  contentDescription = "Back",
                  tint = MaterialTheme.colorScheme.onSurface)
            },
            iconTestTag = "goBackButton")
      },
      bottomBar = {
        // Navigation menu at the bottom
        EditNoteNavigationMenu(
            navigationActions = navigationActions, selectedItem = Screen.EDIT_NOTE_PDF)
      }) { paddingValues ->
        if (pdfExists && pdfFile != null) {
          // Display the PDF if it exists
          PdfRendererViewCompose(
              file = pdfFile!!,
              lifecycleOwner = LocalLifecycleOwner.current,
              modifier = Modifier.fillMaxSize().padding(paddingValues))
        } else {
          // Display a fallback UI if no PDF is found
          Column(
              modifier = Modifier.fillMaxSize().padding(paddingValues),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No PDF found.")
                IconButton(
                    onClick = {
                      // Trigger scanning and start the retry logic for downloading
                      scanner.scan {
                        fileViewModel.updateFile(note!!.id, it, FileType.NOTE_PDF)
                        retryDownload = true // Trigger retry logic
                        attempt = 0 // Reset attempts
                      }
                    },
                    modifier = Modifier.testTag("scanPdfButton")) {
                      Icon(
                          imageVector = Icons.Default.UploadFile,
                          contentDescription = "Scan and replace PDF",
                      )
                    }
              }
        }
      }
}
