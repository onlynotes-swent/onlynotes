package com.github.onlynotesswent.utils

import android.graphics.Bitmap
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardRepository
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.note.Note
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotesToFlashcardTest {

  // Wrapper functions for Mockito functions to allow to return null for non-null types
  private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
  private fun <T> any(): T = Mockito.any<T>()
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
          content = "content",
          date = Timestamp.now(),
          visibility = Note.Visibility.DEFAULT,
          userId = "1",
          folderId = "1",
          noteClass = Note.Class("CS-100", "Sample Class", 2024, "path"),
          image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

  private lateinit var notesToFlashcard: NotesToFlashcard

  @Mock private lateinit var mockFlashcardRepository: FlashcardRepository

  @Mock private lateinit var mockFlashcardViewModel: FlashcardViewModel

  @Mock private lateinit var mockFileViewModel: FileViewModel

  @Mock private lateinit var mockOpenAI: OpenAI

  @Captor private lateinit var flashcardCaptor: ArgumentCaptor<Flashcard>

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    // Initialize FirebaseApp with Robolectric context
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    FirebaseApp.initializeApp(context)

    // Mock FlashcardRepository and set up FlashcardViewModel with it
    mockFlashcardRepository = mock(FlashcardRepository::class.java)
    mockFlashcardViewModel = FlashcardViewModel(mockFlashcardRepository)
    notesToFlashcard = NotesToFlashcard(mockFlashcardViewModel, mockFileViewModel, mockOpenAI)

    // Mock the return value for getNewUid
    `when`(mockFlashcardRepository.getNewUid()).thenReturn("test")
  }

  @Test
  fun `convertNoteToFlashcards should parse JSON and create flashcards`() {
    // Mocking OpenAI's sendRequest to trigger onSuccess
    doAnswer { invocation ->
          val callback = invocation.getArgument<OpenAICallback>(2)
          callback.onSuccess(jsonResponse)
        }
        .`when`(mockOpenAI)
        .sendRequest(anyString(), anyString(), any(OpenAICallback::class.java))

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
      }
    }

    // Execute the method
    notesToFlashcard.convertNoteToFlashcards(
        note = testNote,
        onSuccess = onSuccess,
        onFailure = { fail("Expected successful conversion") })

    // Verify that addFlashcard was called exactly three times with the correct flashcards
    verify(mockFlashcardRepository, times(3)).addFlashcard(capture(flashcardCaptor), any(), any())

    // Retrieve captured flashcards and validate them
    val capturedFlashcards = flashcardCaptor.allValues
    assertEquals(3, capturedFlashcards.size)
  }
}
