package com.github.onlynotesswent.utils

import android.content.Context
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardRepository
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckRepository
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.note.Note
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
                    "content": "[
                    {\"question\": \"What is cryptocurrency?\",\"answer\": \"Cryptocurrency is a digital payment system...\"}
                    ,{\"question\": \"How does cryptocurrency work?\",\"answer\": \"Cryptocurrencies run on a distributed public ledger...\"}
                    ,{\"question\": \"Cryptocurrency examples\",\"answer\": \"There are thousands of cryptocurrencies. Some of the best known include: Bitcoin, Ethereum...\"}
                    ,{\"question\": \"Which one of the following is a cryptocurrency?\",\"answer\": \"Bitcoin\",\"fakeBacks\": [\"PayPal\",\"Visa\",\"Mastercard\"]}
                    ]"
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

  private lateinit var deckViewModel: DeckViewModel

  // Mock dependencies
  @Mock private lateinit var mockFlashcardRepository: FlashcardRepository

  @Mock private lateinit var mockFileViewModel: FileViewModel

  @Mock private lateinit var mockDeckRepository: DeckRepository

  @Mock private lateinit var mockOpenAI: OpenAI

  @Mock private lateinit var mockContext: Context

  private var savedFlashcards = mutableListOf<Flashcard>()

  private val counter = AtomicInteger(0)

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    // Initialize FirebaseApp with Robolectric context
    FirebaseApp.initializeApp(org.robolectric.RuntimeEnvironment.getApplication())

    // Setup view models
    flashcardViewModel = FlashcardViewModel(mockFlashcardRepository)
    deckViewModel = DeckViewModel(mockDeckRepository)

    notesToFlashcard =
        NotesToFlashcard(
            flashcardViewModel, mockFileViewModel, deckViewModel, mockOpenAI, mockContext)

    val testFile = File.createTempFile("test", ".md")
    testFile.deleteOnExit()
    `when`(
            mockFileViewModel.downloadFile(
                any<String>(), eq(FileType.NOTE_TEXT), eq(mockContext), any(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(File) -> Unit>(3)
          onSuccess(testFile)
        }
  }

  @Test
  fun `convertNoteToFlashcards should parse JSON and create flashcards`() {
    // Mock the repositories
    `when`(mockFlashcardRepository.getNewUid()).thenReturn(counter.getAndIncrement().toString())
    `when`(mockDeckRepository.getNewUid()).thenReturn("test")
    `when`(mockFlashcardRepository.addFlashcard(any(), any(), any())).thenAnswer { invocation ->
      savedFlashcards.add(invocation.getArgument(0))
    }
    `when`(mockDeckRepository.updateDeck(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Mocking OpenAI's sendRequest to trigger onSuccess
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(String) -> Unit>(1)
          onSuccess(jsonResponse)
          null
        }
        .`when`(mockOpenAI)
        .sendRequest(anyString(), any(), any(), anyString())

    // Capture the success callback's flashcards list
    val onSuccess: (Deck) -> Unit = { deck ->
      // Verify the number of flashcards
      val flashcardUid = deck.flashcardIds

      // Check the number of flashcards
      assertEquals(4, deck.flashcardIds.size)
      assertEquals(4, savedFlashcards.size)

      // Check the flashcard IDs match
      assertEquals(flashcardUid[0], savedFlashcards[0].id)
      assertEquals(flashcardUid[1], savedFlashcards[1].id)
      assertEquals(flashcardUid[2], savedFlashcards[2].id)
      assertEquals(flashcardUid[3], savedFlashcards[3].id)

      // Verify each flashcard's content
      assertEquals("What is cryptocurrency?", savedFlashcards[0].front)
      assertEquals("Cryptocurrency is a digital payment system...", savedFlashcards[0].back)

      assertEquals("How does cryptocurrency work?", savedFlashcards[1].front)
      assertEquals(
          "Cryptocurrencies run on a distributed public ledger...", savedFlashcards[1].back)

      assertEquals("Cryptocurrency examples", savedFlashcards[2].front)
      assertEquals(
          "There are thousands of cryptocurrencies. Some of the best known include: Bitcoin, Ethereum...",
          savedFlashcards[2].back)

      assert(savedFlashcards[3].isMCQ())
      assertEquals("Which one of the following is a cryptocurrency?", savedFlashcards[3].front)
      assertEquals("Bitcoin", savedFlashcards[3].back)
      assertEquals(listOf("PayPal", "Visa", "Mastercard"), savedFlashcards[3].fakeBacks)

      // Verify for each flashcard
      for (flashcard in savedFlashcards) {
        assertEquals(testNote.userId, flashcard.userId)
        assertEquals(testNote.id, flashcard.noteId)
        assertEquals(testNote.folderId, flashcard.folderId)
      }
    }

    // Execute the method
    notesToFlashcard.convertNoteToDeck(
        note = testNote,
        onSuccess = onSuccess,
        onFileNotFoundException = { fail("Expected successful conversion") },
        onFailure = { fail("Expected successful conversion") })

    // Verify that addFlashcard was called exactly three times with the correct flashcards
    verify(mockFlashcardRepository, times(4)).addFlashcard(any(), any(), any())
  }

  @Test
  fun `convertNoteToDeck failure`() {
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
    notesToFlashcard.convertNoteToDeck(
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
  fun `convertNoteToDeck invalid JSON`() {
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
    notesToFlashcard.convertNoteToDeck(
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
