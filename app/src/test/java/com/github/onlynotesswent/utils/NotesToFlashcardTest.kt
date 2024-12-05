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
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

  private val testFolder =
      Folder(
          id = "1",
          name = "folder1",
          userId = "1",
          visibility = Visibility.DEFAULT,
      )

  private val testSubfolder =
      Folder(
          id = "2",
          name = "folder2",
          userId = "1",
          parentFolderId = "1",
          visibility = Visibility.DEFAULT,
      )

  private val testNote1 =
      Note(
          id = "1",
          title = "title",
          date = Timestamp.now(),
          visibility = Visibility.DEFAULT,
          userId = "1",
          folderId = testFolder.id,
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
      )

  private val testNote2 =
      Note(
          id = "2",
          title = "title",
          date = Timestamp.now(),
          visibility = Visibility.DEFAULT,
          userId = "1",
          folderId = testFolder.id,
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
      )

  private val testNote3 =
      Note(
          id = "3",
          title = "title",
          date = Timestamp.now(),
          visibility = Visibility.DEFAULT,
          userId = "1",
          folderId = testSubfolder.id,
          noteCourse = Course("CS-100", "Sample Course", 2024, "path"),
      )

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

  private val savedFlashcards = mutableListOf<Flashcard>()

  private val savedDecks = mutableListOf<Deck>()

  private val flashcardId = AtomicInteger(0)

  private val folderId = AtomicInteger(3)

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

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
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(String) -> Unit>(1)
          onSuccess(jsonResponse)
          null
        }
        .`when`(mockOpenAI)
        .sendRequest(anyString(), any(), any(), anyString())

    // Mock the repositories
    `when`(mockFlashcardRepository.getNewUid()).thenReturn(flashcardId.getAndIncrement().toString())
    `when`(mockDeckRepository.getNewUid()).thenReturn("test")
    `when`(mockFlashcardRepository.addFlashcard(any(), any(), any())).thenAnswer { invocation ->
      savedFlashcards.add(invocation.getArgument(0))
    }
    `when`(mockDeckRepository.updateDeck(any(), any(), any())).thenAnswer { invocation ->
      savedDecks.add(invocation.getArgument(0))
    }
  }

  @Test
  fun convertFolderToDecks() {
    // Initialize the view models, repositories and saved objects
    savedFlashcards.clear()
    savedDecks.clear()
    folderViewModel.selectedFolder(testFolder)

    `when`(mockFolderRepository.getSubFoldersOf(any(), any(), any())).thenAnswer { invocation ->
      val parentFolderId = invocation.getArgument<String>(0)
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      if (parentFolderId == testFolder.id) {
        onSuccess(listOf(testSubfolder))
      } else {
        onSuccess(emptyList())
      }
    }
    folderViewModel.getSubFoldersOf(testFolder.id)

    `when`(mockNoteRepository.getNotesFromFolder(any(), any(), any())).thenAnswer { invocation ->
      val folderId = invocation.getArgument<String>(0)
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
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
    noteViewModel.getNotesFromFolder(testFolder.id)

    `when`(mockFolderRepository.getNewFolderId()).thenAnswer {
      folderId.getAndIncrement().toString()
    }

    val savedFolders = mutableListOf<Folder>()
    `when`(mockFolderRepository.addFolder(any(), any(), any())).thenAnswer { invocation ->
      savedFolders.add(invocation.getArgument(0))
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    `when`(mockFolderRepository.getDeckFoldersByName(any(), any(), any(), any(), any()))
        .thenAnswer { invocation ->
          val onFolderNotFound = invocation.getArgument<() -> Unit>(2)
          onFolderNotFound()
        }

    notesToFlashcard.convertFolderToDecks(
        onSuccess = {}, onFailure = { fail("Conversion failed with exception: $it") })

    // Assert
    // Check 4 decks were created
    assertEquals(4, savedDecks.size)

    // Check 16 flashcards were created
    assertEquals(12, savedFlashcards.size)

    // Check 2 folders were created
    assertEquals(2, savedFolders.size)

    // Check the hierarchy of deck folders
    val parentFolder = savedFolders.first { it.id == "3" }
    val subFolder = savedFolders.first { it.parentFolderId == parentFolder.id }
    assertNotNull(subFolder)

    // Check the parent folder has 3 decks
    val parentDecks = savedDecks.filter { it.folderId == parentFolder.id }
    assertEquals(3, parentDecks.size)
    assertTrue(parentDecks.any { it.name == testFolder.name })
    assertTrue(parentDecks.any { it.name == testNote1.title })
    assertTrue(parentDecks.any { it.name == testNote2.title })

    // Check the subfolder has 1 deck
    val subFolderDecks = savedDecks.filter { it.folderId == subFolder.id }
    assertEquals(1, subFolderDecks.size)
    assertEquals(testNote3.title, subFolderDecks.first().name)
  }

  @Test
  fun `convertNoteToFlashcards should parse JSON and create flashcards`() {
    // Initialize the view models, repositories and saved objects
    savedFlashcards.clear()

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
        note = testNote1,
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
        note = testNote1,
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
