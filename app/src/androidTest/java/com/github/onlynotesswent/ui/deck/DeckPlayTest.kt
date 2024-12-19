package com.github.onlynotesswent.ui.deck

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
import com.github.onlynotesswent.model.flashcard.UserFlashcard
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.DeckRepository
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepository
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.FlashcardPlayItem
import com.github.onlynotesswent.ui.navigation.NavigationActions
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
  @Mock private lateinit var navigationActions: NavigationActions
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

  private val userFlashcard1 =
      UserFlashcard(
          id = testFlashcard1.id,
          level = 2,
          lastReviewed = Timestamp.now(),
      )
  private val userFlashcard2 =
      UserFlashcard(
          id = testFlashcard2.id,
          level = 4,
          lastReviewed = Timestamp.now(),
      )

  private val testFlashcard3 =
      Flashcard(
          id = "testFlashcard3",
          front = "testFront3",
          back = "testBack3",
          userId = "testUser",
          folderId = null,
          noteId = null)

  private val userFlashcard3 =
      UserFlashcard(
          id = testFlashcard3.id,
          level = 4,
          lastReviewed = Timestamp.now(),
      )

  private val testDeck =
      Deck(
          id = "testDeck",
          name = "testDeckName",
          description = "testDeckDescription",
          userId = "testUser",
          visibility = Visibility.PUBLIC,
          flashcardIds = listOf("testFlashcard1", "testFlashcard2", "testFlashcard3"),
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
              "testFlashcard3" -> testFlashcard3
              else -> null
            }
          })
    }

    `when`(deckRepository.getDeckById(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Deck) -> Unit>(1)
      onSuccess(testDeck)
    }
    `when`(userRepository.updateUserFlashcard(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(2)
      onSuccess()
    }

    `when`(userRepository.getUserFlashcardFromDeck(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Map<String, UserFlashcard>) -> Unit>(2)
      onSuccess(
          mapOf(
              "testFlashcard1" to userFlashcard1,
              "testFlashcard2" to userFlashcard2,
              "testFlashcard3" to userFlashcard3))
    }
    userViewModel.addUser(testUser)
    deckViewModel.playDeckWithMode(testDeck, Deck.PlayMode.REVIEW)
    flashcardViewModel.fetchFlashcardsFromDeck(testDeck)
  }

  @Test
  fun normalFlashcardPlayItem() {
    var onCorrectCalled = false
    var onIncorrectCalled = false

    composeTestRule.setContent {
      FlashcardPlayItem(
          flashcardState = remember { mutableStateOf(testFlashcard1) },
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
          flashcardState = remember { mutableStateOf(testFlashcard2) },
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

  @Test
  fun reviewMode() {
    deckViewModel.playDeckWithMode(testDeck, Deck.PlayMode.REVIEW)

    composeTestRule.setContent {
      DeckPlayScreen(
          userViewModel = userViewModel,
          deckViewModel = deckViewModel,
          flashcardViewModel = flashcardViewModel,
          fileViewModel = fileViewModel,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("DeckPlayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeckPlayScreenTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeckPlayScreenColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ReviewModeColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("flashcardColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("incorrectButton").assertExists()
    composeTestRule.onNodeWithTag("correctButton").assertExists()
    composeTestRule.onNodeWithTag("correctButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("incorrectButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("correctButton").performClick()
    composeTestRule.onNodeWithTag("incorrectButton").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("correctButton").assertIsNotEnabled()
  }

  @Test
  fun testMode() {
    val testDeck =
        Deck(
            id = "testDeck",
            name = "testDeckName",
            description = "testDeckDescription",
            userId = "testUser",
            visibility = Visibility.PUBLIC,
            flashcardIds = listOf("testFlashcard1", "testFlashcard3"),
            lastModified = Timestamp.now(),
            folderId = null)
    deckViewModel.playDeckWithMode(testDeck, Deck.PlayMode.TEST)

    composeTestRule.setContent {
      DeckPlayScreen(
          userViewModel = userViewModel,
          deckViewModel = deckViewModel,
          flashcardViewModel = flashcardViewModel,
          fileViewModel = fileViewModel,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("DeckPlayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeckPlayScreenTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeckPlayScreenColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TestModeColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("flashcardColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("incorrectButton").assertExists()
    composeTestRule.onNodeWithTag("correctButton").assertExists()
    composeTestRule.onNodeWithTag("correctButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("incorrectButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("correctButton").performClick()
    composeTestRule.onNodeWithTag("incorrectButton").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("correctButton").assertIsNotEnabled()
  }

  @Test
  fun testModeMCQ() {
    val testDeck =
        Deck(
            id = "testDeck",
            name = "testDeckName",
            description = "testDeckDescription",
            userId = "testUser",
            visibility = Visibility.PUBLIC,
            flashcardIds = listOf("testFlashcard2"),
            lastModified = Timestamp.now(),
            folderId = null)
    deckViewModel.playDeckWithMode(testDeck, Deck.PlayMode.TEST)

    composeTestRule.setContent {
      DeckPlayScreen(
          userViewModel = userViewModel,
          deckViewModel = deckViewModel,
          flashcardViewModel = flashcardViewModel,
          fileViewModel = fileViewModel,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("DeckPlayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeckPlayScreenTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeckPlayScreenColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TestModeColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("flashcardColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("incorrectButton").assertDoesNotExist()
    composeTestRule.onNodeWithTag("correctButton").assertDoesNotExist()

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
  }

  @Test
  fun testFinishScreen() {

    val testDeck =
        Deck(
            id = "testDeck",
            name = "testDeckName",
            description = "testDeckDescription",
            userId = "testUser",
            visibility = Visibility.PUBLIC,
            flashcardIds = listOf("testFlashcard1"),
            lastModified = Timestamp.now(),
            folderId = null)
    deckViewModel.playDeckWithMode(testDeck, Deck.PlayMode.TEST)

    composeTestRule.setContent {
      DeckPlayScreen(
          userViewModel = userViewModel,
          deckViewModel = deckViewModel,
          flashcardViewModel = flashcardViewModel,
          fileViewModel = fileViewModel,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("DeckPlayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeckPlayScreenTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeckPlayScreenColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TestModeColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("flashcardColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("incorrectButton").assertExists()
    composeTestRule.onNodeWithTag("correctButton").assertExists()
    composeTestRule.onNodeWithTag("correctButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("incorrectButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("correctButton").performClick()
    composeTestRule.onNodeWithTag("submitButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("submitButton").performClick()
    composeTestRule.onNodeWithTag("FinishedScreenColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("retryButton").assertExists()
    composeTestRule.onNodeWithTag("retryButton").performClick()
    composeTestRule.onNodeWithTag("TestModeColumn").assertIsDisplayed()
  }
}
