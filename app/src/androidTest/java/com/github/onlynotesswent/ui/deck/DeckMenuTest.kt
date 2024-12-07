package com.github.onlynotesswent.ui.deck

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
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
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.PictureTaker
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@RunWith(AndroidJUnit4::class)
class DeckMenuTest {

  @Mock private lateinit var mockDeckRepository: DeckRepository
  @Mock private lateinit var mockFlashcardRepository: FlashcardRepository
  @Mock private lateinit var mockUserRepository: UserRepository
  @Mock private lateinit var mockFileRepository: FileRepository
  @Mock private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var pictureTaker: PictureTaker

  private lateinit var deckViewModel: DeckViewModel
  private lateinit var flashcardViewModel: FlashcardViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var fileViewModel: FileViewModel

  private val testUser = User("First Name", "Last Name", "Username", "testUserId", "testEmail")

  private val flashcardList =
      listOf(
          Flashcard(
              id = "testFlashcardId1",
              front = "Front 1",
              back = "Back 1",
              userId = testUser.uid,
              folderId = null,
              noteId = null,
              lastReviewed = Timestamp.now(),
          ),
          Flashcard(
              id = "testFlashcardId2",
              front = "Front 2",
              back = "Back 2",
              userId = testUser.uid,
              folderId = null,
              noteId = null,
              lastReviewed = Timestamp.now(),
              fakeBacks = listOf("Fake Back 1", "Fake Back 2"),
          ),
          Flashcard(
              id = "testFlashcardId3",
              front = "Front 3",
              back = "Back 3",
              hasImage = true,
              userId = testUser.uid,
              folderId = null,
              noteId = null,
              lastReviewed = Timestamp.now(),
          ),
      )

  private val testDeck =
      Deck(
          id = "testDeckId",
          name = "Test Deck",
          userId = testUser.uid,
          folderId = null,
          visibility = Visibility.PUBLIC,
          lastModified = Timestamp.now(),
          flashcardIds = flashcardList.map { it.id },
      )

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    deckViewModel = DeckViewModel(mockDeckRepository)
    flashcardViewModel = FlashcardViewModel(mockFlashcardRepository)
    userViewModel = UserViewModel(mockUserRepository)
    fileViewModel = FileViewModel(mockFileRepository)

    // Mock the current route to be the deck screen
    `when`(navigationActions.currentRoute())
        .thenReturn(Screen.DECK_MENU.replace("{deckId}", testDeck.id))

    // Mock the deck repository to return the test deck
    `when`(mockDeckRepository.getDeckById(eq(testDeck.id), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(Deck) -> Unit>(1)
      onSuccess(testDeck)
    }

    // Mock the flashcard repository to return the test flashcards
    `when`(mockFlashcardRepository.getFlashcardsById(eq(testDeck.flashcardIds), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Flashcard>) -> Unit>(1)
          onSuccess(flashcardList)
        }

    // Mock the user repository to add the test user
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Mock the user repository to get the test user
    `when`(mockUserRepository.getUserById(eq(testUser.uid), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<(User) -> Unit>(1)
      onSuccess(testUser)
    }

    // Mock the file repository to not download images
    `when`(mockFileRepository.downloadFile(any(), any(), any(), any(), any(), any())).thenAnswer {}

    // Mock update flashcard
    `when`(mockFlashcardRepository.updateFlashcard(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Mock update deck
    `when`(mockDeckRepository.updateDeck(any(), any(), any())).thenAnswer { invocation ->
      val deck = invocation.getArgument<Deck>(0)
      val onSuccess = invocation.getArgument<(Deck) -> Unit>(2)
      onSuccess(deck)
    }

    // Log in the test user
    userViewModel.addUser(testUser)
    // Select deck
    deckViewModel.selectDeck(testDeck)
  }

  @Test
  fun displaysAllComponents() {
    // When the DeckMenu screen is displayed
    composeTestRule.setContent {
      DeckScreen(
          deckViewModel = deckViewModel,
          flashcardViewModel = flashcardViewModel,
          userViewModel = userViewModel,
          fileViewModel = fileViewModel,
          navigationActions = navigationActions,
          pictureTaker = pictureTaker,
      )
    }

    // Top app bar
    composeTestRule.onNodeWithTag("deckTopBarTitle").assertExists()
    composeTestRule.onNodeWithTag("backButton").assertExists()

    // Card count and Author information
    composeTestRule.onNodeWithTag("deckCardCount").assertExists()
    composeTestRule.onNodeWithTag("deckAuthorName", useUnmergedTree = true).assertExists()

    // Deck information
    composeTestRule.onNodeWithTag("deckTitle").assertExists()
    composeTestRule.onNodeWithTag("deckDescription").assertExists()

    // Deck actions
    composeTestRule.onNodeWithTag("deckPlayButton").assertExists()
    composeTestRule.onNodeWithTag("sortOptionsButton").assertExists()

    // Flashcard list
    composeTestRule.onNodeWithTag("deckFlashcardsList").assertExists()

    // Flashcard item
    composeTestRule.onNodeWithTag("flashcardItem--testFlashcardId1").assertExists()
    composeTestRule.onNodeWithTag("flashcardItem--testFlashcardId2").assertExists()
    composeTestRule.onNodeWithTag("flashcardItem--testFlashcardId3").assertExists()

    // Flashcard item content
    composeTestRule.onNodeWithTag("flashcardFront--testFlashcardId1").assertExists()
    composeTestRule.onNodeWithTag("flashcardBack--testFlashcardId1").assertExists()
    composeTestRule.onNodeWithTag("flashcardFront--testFlashcardId2").assertExists()
    composeTestRule.onNodeWithTag("flashcardBack--testFlashcardId2").assertExists()
    composeTestRule.onNodeWithTag("flashcardFront--testFlashcardId3").assertExists()
    composeTestRule.onNodeWithTag("flashcardBack--testFlashcardId3").assertExists()

    // Other flashcard item content (MCQ, Image loader)
    composeTestRule.onNodeWithTag("flashcardMCQ--testFlashcardId2").assertExists()
    composeTestRule.onNodeWithTag("flashcardImageLoading--testFlashcardId3").assertExists()

    // Flashcard item actions
    composeTestRule.onNodeWithTag("flashcardOptions--testFlashcardId1").assertExists()
    composeTestRule.onNodeWithTag("flashcardOptions--testFlashcardId2").assertExists()
    composeTestRule.onNodeWithTag("flashcardOptions--testFlashcardId3").assertExists()
  }

  @Test
  fun displaysCorrectFlashcardContent() {
    // When the DeckMenu screen is displayed
    composeTestRule.setContent {
      DeckScreen(
          deckViewModel = deckViewModel,
          flashcardViewModel = flashcardViewModel,
          userViewModel = userViewModel,
          fileViewModel = fileViewModel,
          navigationActions = navigationActions,
          pictureTaker = pictureTaker,
      )
    }

    // Flashcard item content
    composeTestRule.onNodeWithTag("flashcardFront--testFlashcardId1").assertTextEquals("Front 1")
    composeTestRule.onNodeWithTag("flashcardBack--testFlashcardId1").assertTextEquals("Back 1")
    composeTestRule.onNodeWithTag("flashcardFront--testFlashcardId2").assertTextEquals("Front 2")
    composeTestRule.onNodeWithTag("flashcardBack--testFlashcardId2").assertTextEquals("Back 2")
    composeTestRule.onNodeWithTag("flashcardFront--testFlashcardId3").assertTextEquals("Front 3")
    composeTestRule.onNodeWithTag("flashcardBack--testFlashcardId3").assertTextEquals("Back 3")
  }

  @Test
  fun flashcardOptionsWork() {
    // When the DeckMenu screen is displayed
    composeTestRule.setContent {
      DeckScreen(
          deckViewModel = deckViewModel,
          flashcardViewModel = flashcardViewModel,
          userViewModel = userViewModel,
          fileViewModel = fileViewModel,
          navigationActions = navigationActions,
          pictureTaker = pictureTaker,
      )
    }

    // Flashcard item actions
    composeTestRule
        .onNodeWithTag("flashcardOptions--testFlashcardId1")
        .assertIsDisplayed()
        .performClick()
    for (i in 1..1000000) println("Test")
    composeTestRule
        .onNodeWithTag("flashcardOptionsMenu", useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteFlashcardMenuItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editFlashcardMenuItem").assertIsDisplayed().performClick()

    // Edit flashcard
    // Check that the edit flashcard dialog is displayed
    composeTestRule.onNodeWithTag("flashcardDialog--Edit").assertIsDisplayed()
    // Check that the front and back text fields are displayed
    composeTestRule
        .onNodeWithTag("frontTextField")
        .assertIsDisplayed()
        .performTextReplacement("New Front 1")
    composeTestRule
        .onNodeWithTag("backTextField")
        .assertIsDisplayed()
        .performTextReplacement("New Back 1")
    // Check that the save button is displayed
    composeTestRule.onNodeWithTag("saveFlashcardButton").assertIsDisplayed().performClick()
  }
}
