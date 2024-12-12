package com.github.onlynotesswent.ui.overview.editnote

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.github.onlynotesswent.utils.Scanner
import com.github.onlynotesswent.utils.TextExtractor
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import java.io.File
import kotlinx.coroutines.delay

/**
 * Composable function to display the PDF viewer screen. Allows the user to view, delete, and scan
 *
 * @param navigationActions The NavigationActions object to navigate between screens.
 * @param noteViewModel The NoteViewModel to access the selected note.
 * @param fileViewModel The FileViewModel to download and delete the PDF file.
 * @param userViewModel The UserViewModel to access the current user.
 * @param scanner The Scanner object to scan the PDF file.
 * @param textExtractor The TextExtractor object to extract text from the PDF file.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    fileViewModel: FileViewModel,
    userViewModel: UserViewModel,
    scanner: Scanner,
    textExtractor: TextExtractor
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
  var expandedMenu by remember { mutableStateOf(false) }
  var showDeleteConfirmation by remember { mutableStateOf(false) }

  // LaunchedEffect for initial load when entering the screen
  LaunchedEffect(note) {
    if (note != null) {
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

  /** Row item button to convert PDF to text */
  @Composable
  fun rowItemConvertToText() {
    RowItemPdfViewer(
        buttonText = "Convert to text",
        testTag = "convertPdfToTextMenuItem",
        onClick = {
          textExtractor.processPdfFile(
              pdfFile = pdfFile!!,
              onSuccess = { text ->
                if (text.isEmpty()) {
                  Toast.makeText(context, "No text found", Toast.LENGTH_LONG).show()
                } else {
                  updateMdFile(text, noteViewModel, fileViewModel, navigationActions, context)
                }
              })
          expandedMenu = false
        },
        icon = {
          Icon(
              painter = painterResource(R.drawable.outline_convert_to_text),
              contentDescription = "Convert to text",
              modifier = Modifier.size(24.dp))
        })
  }

  /** Row item button to re-scan PDF */
  @Composable
  fun rowItemRescanPdf() {
    RowItemPdfViewer(
        buttonText = "Re-scan PDF",
        testTag = "rescanPdfMenuItem",
        onClick = {
          scanner.scan {
            fileViewModel.deleteFile(note!!.id, FileType.NOTE_PDF)
            pdfFile = null
            pdfExists = false
            fileViewModel.updateFile(note!!.id, it, FileType.NOTE_PDF)
            retryDownload = true // Trigger retry logic
            attempt = 0 // Reset attempts
          }
          expandedMenu = false
        },
        icon = {
          Icon(
              imageVector = Icons.Outlined.Sync,
              contentDescription = "Re-scan PDF",
              modifier = Modifier.size(24.dp))
        })
  }

  /** Row item button to delete PDF */
  @Composable
  fun rowItemDeletePdf() {
    RowItemPdfViewer(
        buttonText = "Delete PDF",
        textColor = MaterialTheme.colorScheme.error,
        testTag = "deletePdfMenuItem",
        onClick = {
          showDeleteConfirmation = true
          expandedMenu = false
        },
        icon = {
          Icon(
              imageVector = Icons.Outlined.Delete,
              contentDescription = "Delete PDF",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(24.dp))
        })
  }

  /** Confirmation popup when deleting a PDF */
  @Composable
  fun confirmationPopupDeletePdf() {
    ConfirmationPopup(
        title = stringResource(R.string.delete_pdf),
        text = stringResource(R.string.delete_pdf_text),
        onConfirm = {
          fileViewModel.deleteFile(note!!.id, FileType.NOTE_PDF)
          pdfFile = null
          pdfExists = false
          isLoading = false
          showDeleteConfirmation = false
        },
        onDismiss = { showDeleteConfirmation = false })
  }

  /** Bottom sheet menu with options for PDF */
  @Composable
  fun bottomSheetMenu() {
    ModalBottomSheet(
        modifier = Modifier.testTag("pdfOptionsMenu"),
        onDismissRequest = { expandedMenu = false },
        content = {
          Column(
              modifier = Modifier.fillMaxWidth().padding(20.dp),
              content = {
                rowItemConvertToText() // Item 1: Convert to text
                rowItemRescanPdf() // Item 2: Re-scan PDF
                HorizontalDivider(Modifier.padding(vertical = 10.dp), 1.dp)
                rowItemDeletePdf() // Item 3: Delete PDF
              })
        })
  }

  /** Floating action button to scan or add PDF. Shown when no PDF available */
  @Composable
  fun floatingActionButton() {
    FloatingActionButton(
        onClick = {
          scanner.scan {
            fileViewModel.updateFile(note!!.id, it, FileType.NOTE_PDF)
            retryDownload = true // Trigger retry logic
            attempt = 0 // Reset attempts
          }
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("scanPdfButton"),
        content = { Icon(Icons.Default.UploadFile, "Scan") })
  }

  /** More options button to display the bottom sheet. Shown when PDF available */
  @Composable
  fun moreOptionsIconButton() {
    IconButton(
        modifier = Modifier.testTag("moreOptionsPdfButton"),
        content = { Icon(Icons.Default.MoreVert, contentDescription = "More options") },
        onClick = { expandedMenu = true })
  }

  // ----------------------------- UI -----------------------------
  Scaffold(
      floatingActionButton = {
        // Show a scan button if no PDF exists
        if (note != null && note!!.isOwner(currentUser!!.uid) && !pdfExists) {
          floatingActionButton()
        }
      },
      topBar = {
        CenterAlignedTopAppBar(
            modifier = Modifier.testTag("pdfTopBar"),
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface),
            title = { TitlePdfTopBar() },
            navigationIcon = { NavigationIconPdfTopBar(navigationActions, noteViewModel) },
            actions = {
              // Show "more options" button if PDF exists
              if (note != null && note!!.isOwner(currentUser!!.uid) && pdfExists) {
                moreOptionsIconButton()
                if (expandedMenu) {
                  bottomSheetMenu()
                }
                if (showDeleteConfirmation) {
                  confirmationPopupDeletePdf()
                }
              }
            })
      },
      bottomBar = {
        EditNoteNavigationMenu(
            navigationActions = navigationActions, selectedItem = Screen.EDIT_NOTE_PDF)
      },
      content = { paddingValues ->
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
              horizontalAlignment = Alignment.CenterHorizontally,
              content = { Text(stringResource(R.string.no_pdf_found)) })
        }
      })
}

/** Composable function to display the title of the PDF viewer screen. */
@Composable
fun TitlePdfTopBar() {
  Text(
      text = stringResource(R.string.pdf),
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.testTag("pdfTitle"))
}

/**
 * Composable function to display the navigation icon in the PDF viewer screen.
 *
 * @param navigationActions The NavigationActions object to navigate between screens.
 * @param noteViewModel The NoteViewModel to access the selected note.
 */
@Composable
fun NavigationIconPdfTopBar(navigationActions: NavigationActions, noteViewModel: NoteViewModel) {
  IconButton(
      modifier = Modifier.testTag("closeButton"),
      content = {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Exit Edit Note",
            tint = MaterialTheme.colorScheme.onSurface)
      },
      onClick = {
        // Unselects the note and navigates back to the previous screen
        navigationActions.goBack()
        noteViewModel.selectedNote(null)
      })
}

/**
 * Composable function that creates a row item for the PDF viewer screen.
 *
 * @param buttonText The text to display in the row item.
 * @param textColor The color of the text.
 * @param testTag The test tag for the row item.
 * @param onClick The lambda function to call when the row item is clicked.
 * @param icon The icon to display in the row item.
 */
@Composable
fun RowItemPdfViewer(
    buttonText: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    testTag: String,
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit
) {
  Row(
      modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(10.dp).testTag(testTag),
      content = {
        icon()
        Spacer(Modifier.padding(10.dp))
        Text(text = buttonText, color = textColor, style = MaterialTheme.typography.titleMedium)
      })
}

/**
 * Function to update (append) the MD file with the extracted text from the PDF file. If the MD file
 * does not exist, it creates a new one. After updating the MD file, it navigates to the Edit
 * Markdown screen.
 *
 * @param text The extracted text from the PDF file.
 * @param noteViewModel The NoteViewModel to access the selected note.
 * @param fileViewModel The FileViewModel to update the MD file.
 * @param navigationActions The NavigationActions object to navigate between screens.
 * @param context The context used to access the cache directory.
 */
fun updateMdFile(
    text: String,
    noteViewModel: NoteViewModel,
    fileViewModel: FileViewModel,
    navigationActions: NavigationActions,
    context: Context
) {
  val note = noteViewModel.selectedNote.value

  fileViewModel.downloadFile(
      uid = note!!.id,
      fileType = FileType.NOTE_TEXT,
      context = context,
      onSuccess = { mdFile ->
        mdFile.appendText("\n" + text)
        fileViewModel.updateFile(
            uid = note.id,
            fileUri = Uri.fromFile(mdFile),
            fileType = FileType.NOTE_TEXT,
            onSuccess = { navigationActions.navigateToAndPop(Screen.EDIT_NOTE_MARKDOWN) })
      },
      onFileNotFound = {
        val mdFile =
            File(context.cacheDir, "${note.id}.md").apply { if (!exists()) createNewFile() }
        mdFile.appendText(text)
        fileViewModel.updateFile(
            uid = note.id,
            fileUri = Uri.fromFile(mdFile),
            fileType = FileType.NOTE_TEXT,
            onSuccess = { navigationActions.navigateToAndPop(Screen.EDIT_NOTE_MARKDOWN) })
      },
      onFailure = {
        Log.e("PdfViewerScreen", "Error downloading MD file: $it")
        Toast.makeText(context, "Error: markdown file could not be updated", Toast.LENGTH_LONG)
            .show()
      })
}
