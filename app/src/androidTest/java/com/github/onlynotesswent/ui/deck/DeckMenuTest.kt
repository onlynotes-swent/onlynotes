package com.github.onlynotesswent.ui.deck

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import java.lang.Thread.sleep
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

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
              lastReviewed = Timestamp(0, 0),
          ),
          Flashcard(
              id = "testFlashcardId2",
              front = "Front 2",
              back = "Back 2",
              userId = testUser.uid,
              folderId = null,
              noteId = null,
              lastReviewed = Timestamp(1, 0),
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
              lastReviewed = Timestamp(2, 0)),
      )

  private val testDeck =
      Deck(
          id = "testDeckId",
          name = "Test Deck",
          description = "description123",
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
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
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
    composeTestRule.onNodeWithTag("flashcardFront--testFlashcardId1", true).assertExists()
    composeTestRule.onNodeWithTag("flashcardBack--testFlashcardId1", true).assertExists()
    composeTestRule.onNodeWithTag("flashcardFront--testFlashcardId2", true).assertExists()
    composeTestRule.onNodeWithTag("flashcardBack--testFlashcardId2", true).assertExists()
    composeTestRule.onNodeWithTag("flashcardFront--testFlashcardId3", true).assertExists()
    composeTestRule.onNodeWithTag("flashcardBack--testFlashcardId3", true).assertExists()

    // Other flashcard item content (MCQ, Image loader)
    composeTestRule.onNodeWithTag("flashcardMCQ--testFlashcardId2", true).assertExists()
    composeTestRule.onNodeWithTag("flashcardImageLoading--testFlashcardId3", true).assertExists()

    // Flashcard item actions
    composeTestRule.onNodeWithTag("flashcardOptions--testFlashcardId1", true).assertExists()
    composeTestRule.onNodeWithTag("flashcardOptions--testFlashcardId2", true).assertExists()
    composeTestRule.onNodeWithTag("flashcardOptions--testFlashcardId3", true).assertExists()
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
    composeTestRule
        .onNodeWithTag("flashcardFront--testFlashcardId1", true)
        .assertTextEquals("Front 1")
    composeTestRule
        .onNodeWithTag("flashcardBack--testFlashcardId1", true)
        .assertTextEquals("Back 1")
    composeTestRule
        .onNodeWithTag("flashcardFront--testFlashcardId2", true)
        .assertTextEquals("Front 2")
    composeTestRule
        .onNodeWithTag("flashcardBack--testFlashcardId2", true)
        .assertTextEquals("Back 2")
    composeTestRule
        .onNodeWithTag("flashcardFront--testFlashcardId3", true)
        .assertTextEquals("Front 3")
    composeTestRule
        .onNodeWithTag("flashcardBack--testFlashcardId3", true)
        .assertTextEquals("Back 3")
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
    // Add two fake backs
    composeTestRule.onNodeWithTag("FakeBacksBox").performClick()
    composeTestRule
        .onNodeWithTag("addFakeBackButton")
        .assertIsDisplayed()
        .performClick()
        .performClick()
    // Check that the fake back text fields are displayed
    composeTestRule
        .onNodeWithTag("fakeBackTextField--0")
        .assertIsDisplayed()
        .performTextReplacement("New Fake Back 1")
    composeTestRule.onNodeWithTag("fakeBackTextField--1").performTextReplacement("New Fake Back 2")

    // Delete second fake back
    composeTestRule.onNodeWithTag("removeFakeBack--1").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("fakeBackTextField--1").assertDoesNotExist()

    // Check that the save button is displayed
    composeTestRule.onNodeWithTag("saveFlashcardButton").assertIsDisplayed().performClick()

    // Check that the flashcard is updated
    verify(mockFlashcardRepository)
        .updateFlashcard(
            eq(
                Flashcard(
                    id = "testFlashcardId1",
                    front = "New Front 1",
                    back = "New Back 1",
                    fakeBacks = listOf("New Fake Back 1"),
                    userId = testUser.uid,
                    folderId = null,
                    noteId = null,
                    lastReviewed = Timestamp(0, 0),
                )),
            any(),
            any(),
        )
    // Check that the flashcard deck is reloaded
    verify(mockFlashcardRepository, times(2))
        .getFlashcardsById(eq(testDeck.flashcardIds), any(), any())
    // Check that the flashcard dialog is closed
    composeTestRule.onNodeWithTag("flashcardDialog--Edit").assertDoesNotExist()

    // ----------------------------------------------------------------------------------------
    // Delete flashcard

    composeTestRule.onNodeWithTag("flashcardOptions--testFlashcardId1").performClick()
    composeTestRule.onNodeWithTag("deleteFlashcardMenuItem").performClick()

    // Verify that the flashcard is deleted and the updated deck is reloaded
    verify(mockFlashcardRepository).deleteFlashcard(eq(flashcardList[0]), any(), any())

    verify(mockDeckRepository)
        .updateDeck(
            eq(testDeck.copy(flashcardIds = testDeck.flashcardIds - flashcardList[0].id)),
            any(),
            any())
  }

  @Test
  fun deckPlayButtonWorks() {
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

    // Click the play button
    composeTestRule.onNodeWithTag("deckPlayButton").performClick()

    // Check the bottom sheet with play modes is shown
    composeTestRule.onNodeWithTag("playModesBottomSheet").assertIsDisplayed()
    sleep(1000)

    Deck.PlayMode.entries.forEach {
      Log.d("DeckMenuTest", "Checking play mode: ${it.name}, $it")
      composeTestRule.onNodeWithTag("playMode--$it", useUnmergedTree = true).assertIsDisplayed()
    }
    // Click the flashcard play mode
    val playMode = Deck.PlayMode.FLASHCARD
    composeTestRule
        .onNodeWithTag("playMode--${playMode.name}", useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    // Check that the navigation action to the play screen is called, with correct arguments
    verify(navigationActions)
        .navigateTo(
            Screen.DECK_PLAY.replace("{deckId}", testDeck.id).replace("{mode}", playMode.name))
  }

  @Test
  fun sortOptionsButtonWorks() {
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

    // Show the sort options row
    composeTestRule.onNodeWithTag("sortOptionsButton").performClick()

    // Click the sort by front option
    val sortOption = Deck.SortMode.ALPHABETICAL
    composeTestRule
        .onNodeWithTag("sortOptionChip--${sortOption.name}", useUnmergedTree = true)
        .performClick()

    // Check that the flashcards are sorted by front
    composeTestRule.onAllNodesWithTag("flashcardItemColumn").onFirst().assertTextContains("Front 1")

    // Click the sort by last Reviewed option
    val sortOption2 = Deck.SortMode.REVIEW
    composeTestRule
        .onNodeWithTag("sortOptionChip--${sortOption2.name}", useUnmergedTree = true)
        .performClick()

    // Check that the flashcards are sorted by last reviewed
    composeTestRule.onAllNodesWithTag("flashcardItemColumn").onFirst().assertTextContains("Front 3")
  }

  @Test
  fun displaysCorrectDeckInformationAndGoesBack() {
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

    // Check that the deck title and description are displayed
    composeTestRule.onNodeWithTag("deckTitle").assertTextEquals("Test Deck")
    composeTestRule.onNodeWithTag("deckDescription").assertTextEquals("description123")
    // Check that the card count is displayed
    composeTestRule.onNodeWithTag("deckCardCount").assertTextEquals("3 cards")
    // Check that the author name is displayed
    composeTestRule.onNodeWithTag("deckAuthorName", true).assertTextEquals(testUser.userHandle())

    // Click the back button
    composeTestRule.onNodeWithTag("backButton").performClick()

    // Check that the navigation action to the previous screen is called
    verify(navigationActions).goBack()
  }

  @Test
  fun userFabWorks() {
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

    // Click the deck floating action button and then the edit deck option
    composeTestRule.onNodeWithTag("deckFab").performClick()
    composeTestRule.onNodeWithTag("editDeckMenuItem").performClick()

    // Check that the edit deck dialog is displayed
    composeTestRule.onNodeWithTag("editDeckDialog").assertIsDisplayed()
    // Check that the title and description text fields are displayed
    composeTestRule
        .onNodeWithTag("deckTitleTextField")
        .assertIsDisplayed()
        .performTextReplacement("New Deck Title")
    composeTestRule
        .onNodeWithTag("deckDescriptionTextField")
        .assertIsDisplayed()
        .performTextReplacement("New Deck Description")
    // Check that the save button is displayed
    composeTestRule.onNodeWithTag("saveDeckButton").assertIsDisplayed().performClick()

    // Check that the deck is updated
    verify(mockDeckRepository).updateDeck(any(), any(), any())

    // Check that the deck dialog is closed
    composeTestRule.onNodeWithTag("deckDialog--Edit").assertDoesNotExist()
    // Check new title and description are displayed
    composeTestRule.onNodeWithTag("deckTitle").assertTextEquals("New Deck Title")
    composeTestRule.onNodeWithTag("deckDescription").assertTextEquals("New Deck Description")

    // ----------------------------------------------------------------------------------------
    // Click the deck floating action button and then the add card option
    composeTestRule.onNodeWithTag("addCardMenuItem").performClick()

    // Check that the add card dialog is displayed
    composeTestRule.onNodeWithTag("flashcardDialog--Create").assertIsDisplayed()
    // Check that the front and back text fields are displayed
    composeTestRule.onNodeWithTag("frontTextField").performTextInput("New Front 4")
    composeTestRule.onNodeWithTag("backTextField").performTextInput("New Back 4")

    // Check that the save button is displayed
    `when`(mockFlashcardRepository.getNewUid()).thenReturn("newFlashcardId")
    composeTestRule.onNodeWithTag("saveFlashcardButton").assertIsDisplayed().performClick()

    // Check that the flashcard is added (updated)
    verify(mockFlashcardRepository)
        .updateFlashcard(
            eq(
                Flashcard(
                    id = "newFlashcardId",
                    front = "New Front 4",
                    back = "New Back 4",
                    userId = testUser.uid,
                    folderId = null,
                    noteId = null,
                    lastReviewed = null,
                )),
            any(),
            any(),
        )

    // ----------------------------------------------------------------------------------------
    // Click the import card option
    composeTestRule.onNodeWithTag("importDeckMenuItem").performClick()

    // Check that the import dialog is displayed
    composeTestRule.onNodeWithTag("importDeckDialog").assertIsDisplayed()
    // Check that the import button is displayed
    composeTestRule.onNodeWithTag("importButton").assertIsDisplayed().performClick()
    // Check dialog is closed
    composeTestRule.onNodeWithTag("importDeckDialog").assertDoesNotExist()
  }

  @Test
  fun publicFabWorks() {
    // Log in another user
    val otherUser = User("First Name", "Last Name", "Username", "otherUserId", "otherEmail")
    userViewModel.addUser(otherUser)

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

    // Click the public floating action button and then both save options
    composeTestRule.onNodeWithTag("publicDeckFab").performClick()
    composeTestRule.onNodeWithTag("saveToFavoritesMenuItem").assertExists()
    composeTestRule.onNodeWithTag("createLocalCopyMenuItem").assertExists().performClick()
  }
}
