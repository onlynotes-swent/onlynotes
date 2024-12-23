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
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepository
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.Scanner
import com.github.onlynotesswent.utils.TextExtractor
import com.google.firebase.Timestamp
import java.io.File
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class PdfViewerTest {
  @Mock private lateinit var fileRepository: FileRepository
  @Mock private lateinit var noteRepository: NoteRepository
  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var mockScanner: Scanner
  @Mock private lateinit var mockTextExtractor: TextExtractor
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var fileViewModel: FileViewModel
  private lateinit var userViewModel: UserViewModel
  private val pdfFile: File = File.createTempFile("test", ".pdf")

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() = runTest {
    MockitoAnnotations.openMocks(this)
    fileViewModel = FileViewModel(fileRepository)
    noteViewModel = NoteViewModel(noteRepository)
    userViewModel = UserViewModel(userRepository)

    // Mock the current route to be the note edit screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.EDIT_NOTE_PDF)
    val mockNote =
        Note(
            id = "1",
            title = "Sample Title",
            date = Timestamp.now(),
            lastModified = Timestamp.now(),
            visibility = Visibility.DEFAULT,
            userId = "1",
            noteCourse = Course("CS-100", "Sample Class", 2024, "path"),
            folderId = "1")
    `when`(noteRepository.getNoteById(any(), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(Note) -> Unit>(1)
      onSuccess(mockNote)
    }

    noteViewModel.getNoteById("1")

    // Mock the addUser method to call the onSuccess callback
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    val testUser =
        User(
            firstName = "testFirstName",
            lastName = "testLastName",
            userName = "testUserName",
            email = "testEmail",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0)
    userViewModel.addUser(testUser, {}, {})

    // Create a valid PDF file programmatically
    createSamplePdf(pdfFile)

    // Mock file download
    `when`(fileRepository.downloadFile(any(), any(), any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(File) -> Unit>(3)
      onSuccess(pdfFile)
    }

    composeTestRule.setContent {
      PdfViewerScreen(
          navigationActions,
          noteViewModel,
          fileViewModel,
          userViewModel,
          mockScanner,
          mockTextExtractor)
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
    composeTestRule.onNodeWithTag("moreOptionsPdfButton").assertIsDisplayed()

    // PDF viewer components
    composeTestRule.onNodeWithTag("PDFViewer").assertIsDisplayed()

    // Navigation bar
    composeTestRule.onNodeWithTag("Detail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Comments").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PDF").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Content").assertIsDisplayed()
  }

  @Test
  fun clickMoreOptionsButton() {
    composeTestRule.onNodeWithTag("moreOptionsPdfButton").performClick()
    composeTestRule.onNodeWithTag("pdfOptionsMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("convertPdfToTextMenuItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rescanPdfMenuItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deletePdfMenuItem").assertIsDisplayed()
  }

  @Test
  fun clickGoBackButton() {
    composeTestRule.onNodeWithTag("closeButton").performClick()

    verify(navigationActions).goBack()
  }

  @Test
  fun deleteAndScanPdf() {
    // Mock delete action
    `when`(fileRepository.deleteFile(any(), any(), any(), any(), any())).thenAnswer {}

    // Delete PDF
    composeTestRule.onNodeWithTag("moreOptionsPdfButton").performClick()
    composeTestRule.onNodeWithTag("deletePdfMenuItem").performClick()
    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").performClick()
    verify(fileRepository).deleteFile(any(), any(), any(), any(), any())

    // Ensure components update
    composeTestRule.onNodeWithTag("popup").assertDoesNotExist()
    composeTestRule.onNodeWithTag("PDFViewer").assertDoesNotExist()
    composeTestRule.onNodeWithTag("noPdfFound").assertIsDisplayed()

    // Scan new PDF
    composeTestRule.onNodeWithTag("scanPdfButton").performClick()
  }
}
