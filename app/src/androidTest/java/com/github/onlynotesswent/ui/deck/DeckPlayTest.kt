package com.github.onlynotesswent.ui.deck

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.file.FileRepository
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardRepository
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckRepository
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepository
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.FlashcardPlayItem
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

@RunWith(AndroidJUnit4::class)
class DeckPlayTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Mock private lateinit var deckRepository: DeckRepository
  @Mock private lateinit var flashcardRepository: FlashcardRepository
  @Mock private lateinit var fileRepository: FileRepository
  @Mock private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var deckViewModel: DeckViewModel
  private lateinit var flashcardViewModel: FlashcardViewModel
  private lateinit var fileViewModel: FileViewModel

  private val testUser =
      User(
          uid = "testUser",
          email = "testEmail",
          userName = "testUsername",
          firstName = "testFirstName",
          lastName = "testLastName",
      )

  private val testFlashcard1 =
      Flashcard(
          id = "testFlashcard1",
          front = "testFront1",
          back = "testBack1",
          userId = "testUser",
          folderId = null,
          noteId = null)

  private val testFlashcard2 =
      Flashcard(
          id = "testFlashcard2",
          front = "testFront2",
          back = "testBack2",
          fakeBacks = listOf("fakeBack1", "fakeBack2"),
          userId = "testUser",
          folderId = null,
          noteId = null)

  private val testDeck =
      Deck(
          id = "testDeck",
          name = "testDeckName",
          description = "testDeckDescription",
          userId = "testUser",
          visibility = Visibility.PUBLIC,
          flashcardIds = listOf("testFlashcard1", "testFlashcard2"),
          lastModified = Timestamp.now(),
          folderId = null)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(userRepository)
    deckViewModel = DeckViewModel(deckRepository)
    flashcardViewModel = FlashcardViewModel(flashcardRepository)
    fileViewModel = FileViewModel(fileRepository)

    `when`(userRepository.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }

    `when`(flashcardRepository.getFlashcardsById(any(), any(), any())).thenAnswer {
      val flashcardIds = it.getArgument<List<String>>(0)
      val onSuccess = it.getArgument<(List<Flashcard>) -> Unit>(1)
      onSuccess(
          flashcardIds.mapNotNull { id ->
            when (id) {
              "testFlashcard1" -> testFlashcard1
              "testFlashcard2" -> testFlashcard2
              else -> null
            }
          })
    }

    `when`(deckRepository.getDeckById(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Deck) -> Unit>(1)
      onSuccess(testDeck)
    }
    userViewModel.addUser(testUser)
    deckViewModel.selectDeck(testDeck)
    flashcardViewModel.fetchFlashcardsFromDeck(testDeck)
  }

  @Test
  fun normalFlashcardPlayItem() {
    var onCorrectCalled = false
    var onIncorrectCalled = false

    composeTestRule.setContent {
      FlashcardPlayItem(
          flashcard = remember { mutableStateOf(testFlashcard1) },
          fileViewModel = fileViewModel,
          onCorrect = { onCorrectCalled = true },
          onIncorrect = { onIncorrectCalled = true })
    }

    // Test that the front of the flashcard is displayed
    composeTestRule.onNodeWithTag("flashcardFront", true).assertTextEquals("testFront1")
    // Flip the flashcard
    composeTestRule.onNodeWithTag("flashcard").performClick()
    // Test that the back of the flashcard is displayed
    composeTestRule.onNodeWithTag("flashcardBack", true).assertTextEquals("testBack1")
  }

  @Test
  fun mcqFlashcardPlayItem() {
    var onCorrectCalled = false
    var onIncorrectCalled = false

    composeTestRule.setContent {
      FlashcardPlayItem(
          flashcard = remember { mutableStateOf(testFlashcard2) },
          fileViewModel = fileViewModel,
          onCorrect = { onCorrectCalled = true },
          onIncorrect = { onIncorrectCalled = true })
    }

    // Test that the front of the flashcard is displayed
    composeTestRule.onNodeWithTag("flashcardFront", true).assertTextEquals("testFront2")
    // Flip the flashcard
    composeTestRule.onNodeWithTag("flashcard").performClick()
    // Test that the fake backs are displayed
    composeTestRule.onAllNodesWithTag("flashcardChoice").assertCountEquals(3)
    composeTestRule.onAllNodesWithTag("flashcardChoiceIcon", true).assertCountEquals(3)
    composeTestRule.onNodeWithTag("flashcardChoice--0", true).assert(hasText("testBack2"))
    composeTestRule.onNodeWithTag("flashcardChoice--1", true).assert(hasText("fakeBack1"))
    composeTestRule.onNodeWithTag("flashcardChoice--2", true).assert(hasText("fakeBack2"))

    // Select the correct answer
    composeTestRule
        .onNodeWithTag("flashcardChoice--0", true)
        .assert(hasText("testBack2"))
        .performClick()

    // Test that the onCorrect callback is called
    assert(onCorrectCalled)

    // Test icons are displayed:
    composeTestRule.onNodeWithTag("flashcardCheckIcon", true).assertExists()
    composeTestRule.onAllNodesWithTag("flashcardWrongIcon", true).assertCountEquals(2)
    composeTestRule.onNodeWithTag("flashcardChoiceIcon", true).assertDoesNotExist()
  }
}
