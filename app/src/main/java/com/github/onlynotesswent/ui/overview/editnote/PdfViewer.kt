package com.github.onlynotesswent.ui.overview.editnote

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.ConfirmationPopup
import com.github.onlynotesswent.ui.common.LoadingIndicator
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.github.onlynotesswent.utils.Scanner
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import java.io.File
import kotlinx.coroutines.delay

/**
 * Composable function to display the PDF viewer screen. Allows the user to view, delete, and scan
 *
 * @param noteViewModel The NoteViewModel to access the selected note.
 * @param fileViewModel The FileViewModel to download and delete the PDF file.
 * @param userViewModel The UserViewModel to access the current user.
 * @param scanner The Scanner object to scan the PDF file.
 * @param navigationActions The NavigationActions object to navigate between screens.
 */
@Composable
fun PdfViewerScreen(
    noteViewModel: NoteViewModel,
    fileViewModel: FileViewModel,
    userViewModel: UserViewModel,
    scanner: Scanner,
    navigationActions: NavigationActions
) {

  // State to track the currently selected note
  val note by noteViewModel.selectedNote.collectAsState()
  val currentUser by userViewModel.currentUser.collectAsState()
  val context = LocalContext.current

  // States to manage the PDF existence, file reference, retry logic, and attempts
  var pdfExists by remember { mutableStateOf(false) }
  var pdfFile by remember { mutableStateOf<File?>(null) }
  var retryDownload by remember { mutableStateOf(false) }
  var attempt by remember { mutableIntStateOf(0) }
  var isLoading by remember { mutableStateOf(true) }

  // LaunchedEffect for initial load when entering the screen
  LaunchedEffect(note) {
    if (note != null) {
      Log.d("PdfViewerScreen", "Entering screen: attempting initial PDF download.")
      isLoading = true
      attempt = 0
      try {
        fileViewModel.downloadFile(
            uid = note!!.id,
            fileType = FileType.NOTE_PDF,
            context = context,
            onSuccess = { file ->
              pdfFile = file
              pdfExists = true
              isLoading = false
              retryDownload = false
            },
            onFileNotFound = { isLoading = false },
            onFailure = {})
      } catch (e: Exception) {
        Log.e("PdfViewerScreen", "Error downloading PDF: $e")
      }
    }
  }

  // LaunchedEffect for retry logic
  LaunchedEffect(retryDownload) {
    if (retryDownload) {
      Log.d("PdfViewerScreen", "Retrying PDF download...")
      isLoading = true
      attempt = 0 // Reset attempts
      while (attempt < 5) {
        attempt++
        try {
          fileViewModel.downloadFile(
              uid = note!!.id,
              fileType = FileType.NOTE_PDF,
              context = context,
              onSuccess = { file ->
                pdfFile = file
                pdfExists = true
                isLoading = false
                retryDownload = false
              },
              onFileNotFound = {},
              onFailure = {})
        } catch (e: Exception) {
          Log.e("PdfViewerScreen", "Error downloading PDF: $e")
        }
        delay(1000) // Add delay between retries
      }
      isLoading = false // End loading if retries are exhausted
    }
  }

  Scaffold(
      floatingActionButton = {
        if (note != null && note!!.isOwner(currentUser!!.uid)) {
          // Show a delete button if a PDF exists
          if (pdfExists) {
            var showDeleteConfirmation by remember { mutableStateOf(false) }
            FloatingActionButton(
                modifier = Modifier.testTag("deletePdfButton"),
                onClick = {
                  // Show confirmation dialog when delete button is clicked
                  showDeleteConfirmation = true
                },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete PDF")
            }

            // Confirmation dialog for deletion
            if (showDeleteConfirmation) {
              ConfirmationPopup(
                  title = stringResource(R.string.delete_pdf),
                  text = stringResource(R.string.delete_pdf_text),
                  onConfirm = {
                    // Perform delete action
                    fileViewModel.deleteFile(uid = note!!.id, fileType = FileType.NOTE_PDF)
                    pdfFile = null
                    pdfExists = false
                    showDeleteConfirmation = false
                    isLoading = false
                  },
                  onDismiss = {
                    // Close the dialog without deleting
                    showDeleteConfirmation = false
                  })
            }
          } else {
            // Show a scan button if no PDF exists
            FloatingActionButton(
                onClick = {
                  // Trigger scanning and start the retry logic for downloading
                  scanner.scan {
                    fileViewModel.updateFile(note!!.id, it, FileType.NOTE_PDF)
                    retryDownload = true // Trigger retry logic
                    attempt = 0 // Reset attempts
                  }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("scanPdfButton")) {
                  Icon(
                      imageVector = Icons.Default.UploadFile,
                      contentDescription = "Scan",
                  )
                }
          }
        }
      },
      topBar = {
        EditNoteTopBar(
            title = stringResource(R.string.pdf),
            titleTestTag = "pdfTitle",
            noteViewModel = noteViewModel,
            userViewModel = userViewModel,
            navigationActions = navigationActions,
            onClick = {
              if (!note!!.isOwner(currentUser!!.uid)) {
                navigationActions.navigateTo(TopLevelDestinations.SEARCH)
              }
            })
      },
      bottomBar = {
        // Navigation menu at the bottom
        EditNoteNavigationMenu(
            navigationActions = navigationActions, selectedItem = Screen.EDIT_NOTE_PDF)
      }) { paddingValues ->
        if (isLoading) {
          LoadingIndicator(
              text = stringResource(R.string.loading_pdf),
              modifier = Modifier.fillMaxSize().padding(paddingValues))
        } else if (pdfExists && pdfFile != null) {
          PdfRendererViewCompose(
              file = pdfFile!!,
              lifecycleOwner = LocalLifecycleOwner.current,
              modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("PDFViewer"))
        } else {
          Column(
              modifier = Modifier.fillMaxSize().testTag("noPdfFound"),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.no_pdf_found))
              }
        }
      }
}
