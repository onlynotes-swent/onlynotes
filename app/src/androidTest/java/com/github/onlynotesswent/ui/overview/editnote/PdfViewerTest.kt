package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.file.FileRepository
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.Scanner
import com.google.firebase.Timestamp
import java.io.File
import java.io.IOException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class PdfViewerTest {
  @Mock private lateinit var fileRepository: FileRepository
  @Mock private lateinit var noteRepository: NoteRepository
  @Mock private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var mockScanner: Scanner
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var fileViewModel: FileViewModel
  private val pdfFile: File = File.createTempFile("test", ".pdf")

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    fileViewModel = FileViewModel(fileRepository)
    noteViewModel = NoteViewModel(noteRepository)

    // Mock the current route to be the note edit screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_NOTE_PDF)
    val mockNote =
        Note(
            id = "1",
            title = "Sample Title",
            date = Timestamp.now(),
            visibility = Visibility.DEFAULT,
            userId = "1",
            noteCourse = Course("CS-100", "Sample Class", 2024, "path"),
        )
    `when`(noteRepository.getNoteById(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(Note) -> Unit>(1)
      onSuccess(mockNote)
    }

    noteViewModel.getNoteById("mockNoteId")

    // Create a valid PDF file programmatically
    createSamplePdf(pdfFile)

    // Mock file download
    `when`(fileRepository.downloadFile(any(), any(), any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(File) -> Unit>(3)
      onSuccess(pdfFile)
    }

    composeTestRule.setContent {
      PdfViewerScreen(noteViewModel, fileViewModel, mockScanner, navigationActions)
    }
  }

  /**
   * Creates a sample PDF file programmatically.
   *
   * @param file The file where the PDF content will be written.
   */
  private fun createSamplePdf(file: File) {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(300, 300, 1).create()
    val page = pdfDocument.startPage(pageInfo)

    val canvas = page.canvas
    val paint = android.graphics.Paint()
    paint.textSize = 14f
    canvas.drawText("This is a sample PDF content.", 50f, 50f, paint)

    pdfDocument.finishPage(page)

    try {
      pdfDocument.writeTo(file.outputStream())
    } catch (e: IOException) {
      throw RuntimeException("Error writing PDF file: ${e.message}", e)
    } finally {
      pdfDocument.close()
    }
  }

  @Test
  fun displayBaseComponents() {
    // Top bar buttons
    composeTestRule.onNodeWithTag("closeButton").assertIsDisplayed()

    // PDF viewer components
    composeTestRule.onNodeWithTag("PDFViewer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deletePdfButton").assertIsDisplayed()

    // Navigation bar
    composeTestRule.onNodeWithTag("Detail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Comments").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PDF").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Content").assertIsDisplayed()
  }

  @Test
  fun deleteAndScanPdf() {
    // Mock delete action
    `when`(fileRepository.deleteFile(any(), any(), any(), any(), any())).thenAnswer {}

    // Delete PDF
    composeTestRule.onNodeWithTag("deletePdfButton").performClick()
    composeTestRule.onNodeWithTag("deletePopup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteButton").performClick()
    verify(fileRepository).deleteFile(any(), any(), any(), any(), any())

    // Ensure components update
    composeTestRule.onNodeWithTag("deletePopup").assertDoesNotExist()
    composeTestRule.onNodeWithTag("PDFViewer").assertDoesNotExist()
    composeTestRule.onNodeWithTag("noPdfFound").assertIsDisplayed()

    // Scan new PDF
    composeTestRule.onNodeWithTag("scanPdfButton").performClick()
  }
}
