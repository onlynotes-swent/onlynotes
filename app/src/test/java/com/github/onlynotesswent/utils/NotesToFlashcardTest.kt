package com.github.onlynotesswent.utils

import android.content.Context
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardRepository
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.note.Note
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotesToFlashcardTest {
  // Helper function to capture arguments in Mockito tests, bypassing Kotlin's null-safety checks
  private fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

  private val jsonResponse =
      """
    {
        "id": "chatcmpl-ASApJxQH9B975zpiNEukghJeWKMsd",
        "object": "chat.completion",
        "created": 1731278165,
        "model": "gpt-3.5-turbo-0125",
        "choices": [
            {
                "index": 0,
                "message": {
                    "role": "assistant",
                    "content": "[{\"question\": \"What is cryptocurrency?\",\"answer\": \"Cryptocurrency is a digital payment system...\"},{\"question\": \"How does cryptocurrency work?\",\"answer\": \"Cryptocurrencies run on a distributed public ledger...\"},{\"question\": \"Cryptocurrency examples\",\"answer\": \"There are thousands of cryptocurrencies. Some of the best known include: Bitcoin, Ethereum...\"}]"
                },
                "logprobs": null,
                "finish_reason": "stop"
            }
        ]
    }
    """
          .trimIndent()

  private val testNote =
      Note(
          id = "1",
          title = "title",
          date = Timestamp.now(),
          visibility = Visibility.DEFAULT,
          userId = "1",
          folderId = "1",
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
      )

  private lateinit var notesToFlashcard: NotesToFlashcard

  private lateinit var flashcardViewModel: FlashcardViewModel

  // Mock dependencies
  @Mock private lateinit var mockFlashcardRepository: FlashcardRepository

  @Mock private lateinit var mockFileViewModel: FileViewModel

  @Mock private lateinit var mockOpenAI: OpenAI

  @Mock private lateinit var mockContext: Context

  // Argument captor for Flashcard objects
  @Captor private lateinit var flashcardCaptor: ArgumentCaptor<Flashcard>

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    // Initialize FirebaseApp with Robolectric context
    FirebaseApp.initializeApp(org.robolectric.RuntimeEnvironment.getApplication())

    // Mock FlashcardRepository and set up FlashcardViewModel with it
    flashcardViewModel = FlashcardViewModel(mockFlashcardRepository)
    notesToFlashcard =
        NotesToFlashcard(flashcardViewModel, mockFileViewModel, mockOpenAI, mockContext)

    val testFile = File.createTempFile("test", ".md")
    testFile.deleteOnExit()
    `when`(
            mockFileViewModel.downloadFile(
                any<String>(), eq(FileType.NOTE_TEXT), eq(mockContext), any(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(File) -> Unit>(3)
          onSuccess(testFile)
        }

    // Mock the return value for getNewUid
    `when`(mockFlashcardRepository.getNewUid()).thenReturn("test")
  }

  @Test
  fun `convertNoteToFlashcards should parse JSON and create flashcards`() {

    // Mocking OpenAI's sendRequest to trigger onSuccess
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(String) -> Unit>(1)
          onSuccess(jsonResponse)
          null
        }
        .`when`(mockOpenAI)
        .sendRequest(anyString(), any(), any(), anyString())

    // Capture the success callback's flashcards list
    val onSuccess: (List<Flashcard>) -> Unit = { flashcards ->
      assertEquals(3, flashcards.size)

      // Verify each flashcard's content
      assertEquals("What is cryptocurrency?", flashcards[0].front)
      assertEquals("Cryptocurrency is a digital payment system...", flashcards[0].back)

      assertEquals("How does cryptocurrency work?", flashcards[1].front)
      assertEquals("Cryptocurrencies run on a distributed public ledger...", flashcards[1].back)

      assertEquals("Cryptocurrency examples", flashcards[2].front)
      assertEquals(
          "There are thousands of cryptocurrencies. Some of the best known include: Bitcoin, Ethereum...",
          flashcards[2].back)

      // Verify for each flashcard
      for (flashcard in flashcards) {
        assertEquals(testNote.userId, flashcard.userId)
        assertEquals(testNote.id, flashcard.noteId)
        assertEquals(testNote.folderId, flashcard.folderId)
      }
    }

    // Execute the method
    notesToFlashcard.convertNoteToFlashcards(
        note = testNote,
        onSuccess = onSuccess,
        onFileNotFoundException = { fail("Expected successful conversion") },
        onFailure = { fail("Expected successful conversion") })

    // Verify that addFlashcard was called exactly three times with the correct flashcards
    verify(mockFlashcardRepository, times(3)).addFlashcard(capture(flashcardCaptor), any(), any())

    // Retrieve captured flashcards and validate them
    val capturedFlashcards = flashcardCaptor.allValues
    assertEquals(3, capturedFlashcards.size)
  }

  @Test
  fun `convertNoteToFlashcards failure`() {
    // Mocking OpenAI's sendRequest to trigger onFailure
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(IOException) -> Unit>(2)
          onFailure(IOException("Mocked IOException"))
          null
        }
        .`when`(mockOpenAI)
        .sendRequest(anyString(), anyOrNull(), anyOrNull(), anyString())

    // Set up a flag to ensure the failure callback was called
    var failureCallbackCalled = false

    // Execute the method
    notesToFlashcard.convertNoteToFlashcards(
        note = testNote,
        onSuccess = { fail("Expected failure but got success") },
        onFileNotFoundException = { fail("Expected failure but got not found") },
        onFailure = { error ->
          failureCallbackCalled = true
          assert(error is IOException)
          assertEquals("Mocked IOException", error.message)
        })

    // Verify that failure callback was indeed called
    assert(failureCallbackCalled)
  }

  @Test
  fun `convertNoteToFlashcards invalid JSON`() {
    // Mocking OpenAI's sendRequest to trigger onSuccess
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(String) -> Unit>(1)
          onSuccess("invalid JSON")
          null
        }
        .`when`(mockOpenAI)
        .sendRequest(anyString(), anyOrNull(), anyOrNull(), anyString())

    // Set up a flag to ensure the failure callback was called
    var failureCallbackCalled = false
    notesToFlashcard.convertNoteToFlashcards(
        note = testNote,
        onSuccess = { fail("Expected failure but got success") },
        onFileNotFoundException = { fail("Expected failure but got not found") },
        onFailure = { error ->
          failureCallbackCalled = true
          assertEquals(JsonSyntaxException::class.java, error::class.java)
        })

    // Verify that failure callback was indeed called
    assert(failureCallbackCalled)
  }
}
