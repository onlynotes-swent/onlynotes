package com.github.onlynotesswent.utils

import android.content.Context
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.DeckRepository
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardRepository
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderRepository
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
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

  private val testFolder =
      Folder(
          id = "testFolder",
          name = "folder1",
          userId = "1",
          visibility = Visibility.DEFAULT,
          lastModified = Timestamp.now(),
      )

  private val testSubfolder =
      Folder(
          id = "2",
          name = "testSubfolder",
          userId = "1",
          parentFolderId = "1",
          visibility = Visibility.DEFAULT,
          lastModified = Timestamp.now(),
      )

  private val deckFolder =
      Folder(
          id = "3",
          name = "deckFolder",
          userId = "1",
          visibility = Visibility.DEFAULT,
          lastModified = Timestamp.now(),
          isDeckFolder = true,
      )

  private val deckSubfolder =
      Folder(
          id = "4",
          name = "deckSubfolder",
          userId = "1",
          parentFolderId = "3",
          visibility = Visibility.DEFAULT,
          lastModified = Timestamp.now(),
          isDeckFolder = true,
      )

  private val testNote1 =
      Note(
          id = "1",
          title = "testNote1",
          date = Timestamp.now(),
          lastModified = Timestamp.now(),
          visibility = Visibility.DEFAULT,
          userId = "1",
          folderId = testFolder.id,
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
      )

  private val testNote2 =
      Note(
          id = "2",
          title = "testNote2",
          date = Timestamp.now(),
          visibility = Visibility.DEFAULT,
          userId = "1",
          folderId = testFolder.id,
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
          lastModified = Timestamp.now())

  private val testNote3 =
      Note(
          id = "3",
          title = "testNote3",
          date = Timestamp.now(),
          visibility = Visibility.DEFAULT,
          userId = "1",
          folderId = testSubfolder.id,
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
          lastModified = Timestamp.now())

  private lateinit var notesToFlashcard: NotesToFlashcard

  private lateinit var flashcardViewModel: FlashcardViewModel

  private lateinit var deckViewModel: DeckViewModel

  private lateinit var folderViewModel: FolderViewModel

  private lateinit var noteViewModel: NoteViewModel

  // Mock dependencies
  @Mock private lateinit var mockFlashcardRepository: FlashcardRepository

  @Mock private lateinit var mockFileViewModel: FileViewModel

  @Mock private lateinit var mockDeckRepository: DeckRepository

  @Mock private lateinit var mockFolderRepository: FolderRepository

  @Mock private lateinit var mockNoteRepository: NoteRepository

  @Mock private lateinit var mockOpenAI: OpenAI

  @Mock private lateinit var mockContext: Context

  // Saved objects, use synchronizedList to avoid concurrent modification exceptions
  private val savedFlashcards = Collections.synchronizedList(mutableListOf<Flashcard>())

  private val savedDecks = Collections.synchronizedList(mutableListOf<Deck>())

  private val savedFolders = Collections.synchronizedList(mutableListOf<Folder>())

  private val flashcardId = AtomicInteger(0)

  private val folderId = AtomicInteger(3)

  private val testDispatcher = StandardTestDispatcher()

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)

    // Initialize FirebaseApp with Robolectric context
    FirebaseApp.initializeApp(org.robolectric.RuntimeEnvironment.getApplication())

    // Setup view models
    flashcardViewModel = FlashcardViewModel(mockFlashcardRepository)
    deckViewModel = DeckViewModel(mockDeckRepository)
    folderViewModel = FolderViewModel(mockFolderRepository)
    noteViewModel = NoteViewModel(mockNoteRepository)

    notesToFlashcard =
        NotesToFlashcard(
            flashcardViewModel,
            mockFileViewModel,
            deckViewModel,
            noteViewModel,
            folderViewModel,
            mockOpenAI,
            mockContext)

    folderViewModel.selectedFolder(testFolder)

    val testFile = File.createTempFile("test", ".md")
    testFile.deleteOnExit()
    `when`(
            mockFileViewModel.downloadFile(
                any<String>(), eq(FileType.NOTE_TEXT), eq(mockContext), any(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(File) -> Unit>(3)
          onSuccess(testFile)
        }

    // Mocking OpenAI's sendRequest to trigger onSuccess
    runBlocking {
      `when`(mockOpenAI.sendRequestSuspend(anyString(), anyString())).thenReturn(jsonResponse)
    }

    // Mock the repositories
    `when`(mockFlashcardRepository.getNewUid()).thenReturn(flashcardId.getAndIncrement().toString())
    `when`(mockDeckRepository.getNewUid()).thenReturn("test")
    `when`(mockFlashcardRepository.addFlashcard(any(), any(), any())).thenAnswer { invocation ->
      savedFlashcards.add(invocation.getArgument(0))
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    `when`(mockDeckRepository.updateDeck(any(), any(), any())).thenAnswer { invocation ->
      savedDecks.add(invocation.getArgument(0))
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun resetDispatcher() {
    savedFlashcards.clear()
    savedDecks.clear()
    savedFolders.clear()
    flashcardId.set(0)
    folderId.set(3)

    Dispatchers.resetMain()
  }

  private fun convertFolderToDecksChecks(expectedAddedFolderSize: Int) {
    // Check 4 decks were created
    assertEquals(4, savedDecks.size)

    // Check 16 flashcards were created
    assertEquals(12, savedFlashcards.size)

    // Check 2 folders were created
    assertEquals(expectedAddedFolderSize, savedFolders.size)

    // Check the parent folder has 3 decks
    val parentDecks = savedDecks.filter { it.folderId == deckFolder.id }
    assertEquals(3, parentDecks.size)
    assertTrue(parentDecks.any { it.name == testFolder.name })
    assertTrue(parentDecks.any { it.name == testNote1.title })
    assertTrue(parentDecks.any { it.name == testNote2.title })

    // Check the subfolder has 1 deck
    val subFolderDecks = savedDecks.filter { it.folderId == deckSubfolder.id }
    assertEquals(1, subFolderDecks.size)
    assertEquals(testNote3.title, subFolderDecks.first().name)
  }

  private fun convertFolderToDecksCommonMocks() = runTest {
    // Initialize the view models, repositories
    `when`(mockFolderRepository.addFolder(any(), any(), any(), any())).thenAnswer { invocation ->
      savedFolders.add(invocation.getArgument(0))
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    `when`(mockFolderRepository.getSubFoldersOf(any(), anyOrNull(), any(), any(), any()))
        .thenAnswer { invocation ->
          val parentFolderId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(2)
          if (parentFolderId == testFolder.id) {
            onSuccess(listOf(testSubfolder))
          } else {
            onSuccess(emptyList())
          }
        }
    folderViewModel.getSubFoldersOf(testFolder.id, null)

    `when`(mockNoteRepository.getNotesFromFolder(any(), anyOrNull(), any(), any(), any()))
        .thenAnswer { invocation ->
          val folderId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(2)
          when (folderId) {
            testFolder.id -> {
              onSuccess(listOf(testNote1, testNote2))
            }
            testSubfolder.id -> {
              onSuccess(listOf(testNote3))
            }
            else -> {
              onSuccess(emptyList())
            }
          }
        }
    noteViewModel.getNotesFromFolder(testFolder.id, null)

    `when`(mockFolderRepository.getNewFolderId()).thenAnswer {
      folderId.getAndIncrement().toString()
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `convertFolderToDecks with existing deck folders`() = runTest {
    convertFolderToDecksCommonMocks()

    `when`(mockFolderRepository.getDeckFoldersByName(any(), any(), any(), any(), any(), any()))
        .thenAnswer { invocation ->
          val name = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(3)
          when (name) {
            testFolder.name -> {
              onSuccess(listOf(deckFolder))
            }
            testSubfolder.name -> {
              onSuccess(listOf(deckSubfolder))
            }
            else -> {
              onSuccess(emptyList())
            }
          }
        }

    notesToFlashcard.convertFolderToDecks(
        onProgress = { _, _, _ -> },
        onSuccess = {},
        onFailure = { fail("Conversion failed with exception: $it") })

    // Wait for the coroutine to finish
    advanceUntilIdle()

    convertFolderToDecksChecks(0)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `convertFolderToDecks with no existing deck folders`() = runTest {
    convertFolderToDecksCommonMocks()

    `when`(mockFolderRepository.getDeckFoldersByName(any(), any(), any(), any(), any(), any()))
        .thenAnswer { invocation ->
          val onFolderNotFound = invocation.getArgument<() -> Unit>(2)
          onFolderNotFound()
        }

    notesToFlashcard.convertFolderToDecks(
        onProgress = { _, _, _ -> },
        onSuccess = {},
        onFailure = { fail("Conversion failed with exception: $it") })

    // Wait for the coroutine to finish
    advanceUntilIdle()

    convertFolderToDecksChecks(2)
  }

  @Test
  fun `convertNoteToFlashcards should parse JSON and create flashcards`() = runTest {
    // Initialize the view models, repositories and saved objects
    savedFlashcards.clear()

    // Capture the success callback's flashcards list
    val onSuccess: (Deck?) -> Unit = { deck ->
      if (deck == null) {
        fail("Expected a deck but got null")
      }
      // Verify the number of flashcards
      val flashcardUid = deck!!.flashcardIds

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
        assertEquals(testNote1.userId, flashcard.userId)
        assertEquals(testNote1.id, flashcard.noteId)
        assertNull(flashcard.folderId)
      }
    }

    // Execute the method
    notesToFlashcard.convertNoteToDeck(
        note = testNote1,
        onSuccess = onSuccess,
        onFileNotFoundException = { fail("Expected successful conversion") },
        onFailure = { fail("Expected successful conversion") })
  }

  @Test
  fun `convertNoteToDeck failure`() = runTest {
    // Mocking OpenAI's sendRequestSuspend to simulate failure
    `when`(mockOpenAI.sendRequestSuspend(anyString(), anyString())).thenAnswer {
      throw IOException("Mocked IOException")
    }

    // Define failure callback
    val onFailure: (Exception) -> Unit = { exception ->
      assertTrue(exception is IOException)
      assertEquals("Mocked IOException", exception.message)
    }

    // Execute the method
    notesToFlashcard.convertNoteToDeck(
        note = testNote1,
        onSuccess = { fail("Expected failure but got success") },
        onFileNotFoundException = { fail("Expected failure but got not found") },
        onFailure = onFailure)
  }

  @Test
  fun `convertNoteToDeck invalid JSON`() {
    // Mocking OpenAI's sendRequest to trigger onSuccess
    runBlocking {
      `when`(mockOpenAI.sendRequestSuspend(anyString(), anyString())).thenReturn("invalid JSON")
    }

    val onFailure: (Exception) -> Unit = { exception ->
      assertTrue(exception is JsonSyntaxException)
    }
    notesToFlashcard.convertNoteToDeck(
        note = testNote1,
        onSuccess = { fail("Expected failure but got success") },
        onFileNotFoundException = { fail("Expected failure but got not found") },
        onFailure = onFailure)
  }
}
