package com.github.onlynotesswent.ui

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.test.espresso.intent.Intents
import com.github.onlynotesswent.model.authentication.Authenticator
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.DeckRepository
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardRepository
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.folder.FolderRepository
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.notification.NotificationRepository
import com.github.onlynotesswent.model.notification.NotificationViewModel
import com.github.onlynotesswent.model.user.Friends
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepository
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.overview.FolderContentScreen
import com.github.onlynotesswent.ui.overview.NoteOverviewScreen
import com.github.onlynotesswent.ui.overview.editnote.EditMarkdownScreen
import com.github.onlynotesswent.ui.overview.editnote.EditNoteScreen
import com.github.onlynotesswent.ui.search.SearchScreen
import com.github.onlynotesswent.ui.theme.AppTheme
import com.github.onlynotesswent.ui.user.CreateUserScreen
import com.github.onlynotesswent.ui.user.EditProfileScreen
import com.github.onlynotesswent.ui.user.PublicProfileScreen
import com.github.onlynotesswent.ui.user.UserProfileScreen
import com.github.onlynotesswent.utils.NotesToFlashcard
import com.github.onlynotesswent.utils.OpenAI
import com.github.onlynotesswent.utils.PictureTaker
import com.google.firebase.Timestamp
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class EndToEndTest {

  // Mock repositories, view models, and other dependencies

  @Mock private lateinit var mockUserRepository: UserRepository
  @Mock private lateinit var mockNoteRepository: NoteRepository
  @Mock private lateinit var mockFolderRepository: FolderRepository
  @Mock private lateinit var mockDeckRepository: DeckRepository
  @Mock private lateinit var mockNotificationRepository: NotificationRepository
  @Mock private lateinit var mockPictureTaker: PictureTaker
  @Mock private lateinit var mockFileViewModel: FileViewModel

  private lateinit var userViewModel: UserViewModel
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var folderViewModel: FolderViewModel
  private lateinit var deckViewModel: DeckViewModel
  private lateinit var fileViewModel: FileViewModel

  private lateinit var notificationViewModel: NotificationViewModel

  @Mock private lateinit var mockAuthenticator: Authenticator

  @Mock private lateinit var mockContext: Context
  @Mock private lateinit var mockOpenAI: OpenAI
  @Mock private lateinit var mockFlashcardRepository: FlashcardRepository
  private lateinit var notesToFlashcard: NotesToFlashcard
  private lateinit var flashcardViewModel: FlashcardViewModel

  private lateinit var navController: NavHostController
  private lateinit var navigationActions: NavigationActions

  // Sample user and note data for testing
  private val testUid = "testUid123"
  private var testUser1 =
      User(
          firstName = "testFirstName1",
          lastName = "testLastName1",
          userName = "testUserName1",
          email = "testEmail",
          uid = testUid,
          dateOfJoining = Timestamp.now(),
          rating = 0.0)

  private var testUser2 =
      User(
          firstName = "testFirstName2",
          lastName = "testLastName2",
          userName = "testUserName2",
          email = "testEmail",
          uid = "testUid2",
          dateOfJoining = Timestamp.now(),
          rating = 0.0)

  private val testUsers = listOf(testUser1, testUser2)

  private val uidToUser = { s: String ->
    when (s) {
      testUser1.uid -> testUser1
      testUser2.uid -> testUser2
      else -> null
    }
  }

  private var testNote =
      Note(
          id = "1",
          title = "title",
          date = Timestamp.now(),
          userId = testUid,
          lastModified = Timestamp.now())

  private var testNoteUser2 =
      Note(
          id = "2",
          title = "sampleTitle",
          date = Timestamp.now(),
          userId = testUser2.uid,
          lastModified = Timestamp.now(),
          visibility = Visibility.PUBLIC)

  private val newTitle = "New Title"

  private val testFlashcard =
      Flashcard(
          id = "testFlashcardId",
          front = "front",
          back = "back",
          userId = testUid,
          folderId = null,
          noteId = testNote.id)

  private val testDeck =
      Deck(
          id = "1",
          name = "deckName",
          userId = testUid,
          folderId = null,
          visibility = Visibility.PRIVATE,
          lastModified = Timestamp.now(),
          description = "deckDescription",
          flashcardIds = listOf(testFlashcard.id))

  // Setup Compose test rule for UI testing
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock objects for dependencies
    MockitoAnnotations.openMocks(this)

    userViewModel = UserViewModel(mockUserRepository, mockNotificationRepository)
    noteViewModel = NoteViewModel(mockNoteRepository)
    folderViewModel = FolderViewModel(mockFolderRepository)
    deckViewModel = DeckViewModel(mockDeckRepository)
    flashcardViewModel = FlashcardViewModel(mockFlashcardRepository)
    notificationViewModel = NotificationViewModel(mockNotificationRepository)

    notesToFlashcard =
        NotesToFlashcard(
            flashcardViewModel,
            mockFileViewModel,
            deckViewModel,
            noteViewModel,
            folderViewModel,
            mockOpenAI,
            mockContext)

    // Initialize Intents for handling navigation intents in the test
    Intents.init()

    val createUserRoute = "Create User"
    // Set up the Compose content for the test, that doesn't include authentication
    composeTestRule.setContent {
      AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          navController = rememberNavController()
          navigationActions = NavigationActions(navController)
          NavHost(
              navController = navController,
              startDestination = createUserRoute // Start at the "Create User" screen
              ) {
                navigation(
                    startDestination = Screen.CREATE_USER,
                    route = createUserRoute,
                ) {
                  composable(Screen.CREATE_USER) {
                    CreateUserScreen(navigationActions, userViewModel)
                  }
                }

                navigation(
                    startDestination = Screen.NOTE_OVERVIEW,
                    route = Route.NOTE_OVERVIEW,
                ) {
                  composable(Screen.NOTE_OVERVIEW) {
                    NoteOverviewScreen(
                        navigationActions, noteViewModel, userViewModel, folderViewModel)
                  }
                  composable(Screen.EDIT_NOTE) {
                    EditNoteScreen(navigationActions, noteViewModel, userViewModel)
                  }
                  composable(Screen.FOLDER_CONTENTS) {
                    FolderContentScreen(
                        navigationActions, folderViewModel, noteViewModel, userViewModel)
                  }
                  composable(Screen.EDIT_NOTE_MARKDOWN) {
                    EditMarkdownScreen(
                        navigationActions, noteViewModel, mockFileViewModel, userViewModel)
                  }
                }
                navigation(
                    startDestination = Screen.SEARCH,
                    route = Route.SEARCH,
                ) {
                  composable(Screen.SEARCH) {
                    SearchScreen(
                        navigationActions,
                        noteViewModel,
                        userViewModel,
                        folderViewModel,
                        deckViewModel,
                        mockFileViewModel)
                  }
                }
                navigation(
                    startDestination = Screen.USER_PROFILE,
                    route = Route.PROFILE,
                ) {
                  composable(Screen.USER_PROFILE) {
                    UserProfileScreen(
                        navigationActions,
                        userViewModel,
                        mockFileViewModel,
                        notificationViewModel,
                        mockAuthenticator)
                  }
                  composable(Screen.PUBLIC_PROFILE) {
                    PublicProfileScreen(
                        navigationActions,
                        userViewModel,
                        mockFileViewModel,
                        notificationViewModel,
                        mockAuthenticator)
                  }
                  composable(Screen.EDIT_PROFILE) {
                    EditProfileScreen(
                        navigationActions,
                        userViewModel,
                        mockPictureTaker,
                        mockFileViewModel,
                        noteViewModel,
                        folderViewModel,
                        deckViewModel,
                        flashcardViewModel,
                        notificationViewModel)
                  }
                }
              }
        }
      }
    }
  }

  @After
  fun tearDown() {
    // Release Intents after the test to clean up resources
    Intents.release()
  }

  // Creates the mock behavior needed for the end-to-end flow of creating a user, adding a note, and
  // editing the note
  private fun testEndToEndFlow1_init() = runTest {
    // Set up mock behavior for user and note repository methods
    `when`(userViewModel.getNewUid()).thenReturn(testUid)

    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    `when`(mockNoteRepository.getNewUid()).thenReturn(testNote.id)

    // Mock the note repository update
    `when`(mockNoteRepository.updateNote(any(), any(), any(), any())).thenAnswer {
      testNote = it.arguments[0] as Note
      noteViewModel.selectedNote(testNote)
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Mock get note by id
    `when`(mockNoteRepository.getNoteById(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Note) -> Unit>(1)
      onSuccess(testNote)
    }

    // Mock retrieval of notes
    `when`(mockNoteRepository.getRootNotesFromUid(eq(testUser1.uid), any(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
          onSuccess(listOf(testNote))
        }
  }

  // Test the end-to-end flow of creating a user, adding a note, and editing the note
  @Test
  fun testEndToEndFlow1() {
    testEndToEndFlow1_init()

    // Interact with the input fields for creating a user
    composeTestRule.onNodeWithTag("inputFirstName").performTextInput(testUser1.firstName)
    composeTestRule.onNodeWithTag("inputLastName").performTextInput(testUser1.lastName)
    composeTestRule.onNodeWithTag("inputUserName").performTextInput(testUser1.userName)

    // Verify that the "Create User" button is enabled and then click it
    composeTestRule.onNodeWithTag("saveButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("saveButton").performClick()

    // Interact with the note creation flow
    composeTestRule.onNodeWithTag("createObjectOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createObjectOrFolder").performClick()
    composeTestRule.onNodeWithTag("createDeckOrNote").performClick()
    composeTestRule.onNodeWithTag("confirmNoteAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputNoteName").performTextInput(testNote.title)
    composeTestRule.onNodeWithTag("currentVisibilityOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("previousVisibility").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nextVisibility").performClick()
    composeTestRule.onNodeWithTag("confirmNoteAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmNoteAction").performClick()

    // Modify the note title and save the changes
    composeTestRule.onNodeWithTag("EditTitle textField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditTitle textField").performTextClearance()

    composeTestRule.onNodeWithTag("EditTitle textField").performTextInput(newTitle)
    composeTestRule.onNodeWithTag("saveNoteButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("saveNoteButton").performClick()

    // Exit the note edit screen
    composeTestRule.onNodeWithTag("closeButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("closeButton").performClick()

    // Verify that the note title has been properly saved
    composeTestRule.onNodeWithTag("popup").assertIsNotDisplayed()

    // Verify the notes are displayed
    composeTestRule.onNodeWithTag("noteAndFolderList").assertIsDisplayed()

    // Verify that the note card is displayed
    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
  }

  // Creates the mock behavior needed for the end-to-end flow of searching for testUser2 and viewing
  // their profile and following them and then going to profile screen to unfollow and modify
  // profile
  private fun testEndToEndFlow2_init() {
    `when`(mockUserRepository.getAllUsers(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<User>) -> Unit>(0)
      onSuccess(testUsers)
    }

    // Mock the user repository to return the specified user
    `when`(mockUserRepository.getUserById(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(User) -> Unit>(1)
      val onNotFound = it.getArgument<() -> Unit>(2)
      val uid = it.arguments[0] as String

      uidToUser(uid)?.let { it1 -> onSuccess(it1) } ?: onNotFound()
    }

    `when`(mockUserRepository.getUsersById(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<User>) -> Unit>(1)
      val userIds = it.getArgument<List<String>>(0)

      onSuccess(userIds.mapNotNull(uidToUser))
    }

    // Mock add user to initialize current user
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }
    // Initialize current user
    userViewModel.addUser(testUser1, {}, {})

    `when`(mockUserRepository.addFollowerTo(any(), any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(3)
      val userId = it.arguments[0] as String // testUser2
      val followerId = it.arguments[1] as String // testUser
      testUser2 =
          testUser2.copy(
              friends =
                  Friends(
                      testUser2.friends.following, testUser2.friends.followers.plus(followerId)))
      testUser1 =
          testUser1.copy(
              friends =
                  Friends(testUser1.friends.following.plus(userId), testUser1.friends.followers))

      onSuccess()
    }

    `when`(mockUserRepository.removeFollowerFrom(any(), any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(3)
      val userId = it.arguments[0] as String // testUser2
      val followerId = it.arguments[1] as String // testUser
      testUser2 =
          testUser2.copy(
              friends =
                  Friends(
                      testUser2.friends.following, testUser2.friends.followers.minus(followerId)))
      testUser1 =
          testUser1.copy(
              friends =
                  Friends(testUser1.friends.following.minus(userId), testUser1.friends.followers))
      onSuccess()
    }

    `when`(mockUserRepository.updateUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Start at overview screen
    composeTestRule.runOnUiThread { navController.navigate(Route.NOTE_OVERVIEW) }

    `when`(mockNotificationRepository.getNewUid()).thenReturn(testUid)

    `when`(mockNotificationRepository.addNotification(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }
  }

  // Test the end-to-end flow of searching for testUser2 and viewing their profile and following
  // them and then going to profile screen to unfollow and modify profile
  @Test
  fun testEndToEndFlow2() {
    testEndToEndFlow2_init()

    // Go to search screen
    composeTestRule.onNodeWithTag("Search").performClick()

    // Search for testUser2
    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement("User")
    composeTestRule.onNodeWithTag("userFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredUserList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()

    composeTestRule
        .onAllNodesWithTag("userItem")
        .filter(hasText(testUser1.fullName()))
        .onFirst()
        .assertIsDisplayed()

    composeTestRule
        .onAllNodesWithTag("userItem")
        .filter(hasText(testUser2.fullName()))
        .onFirst()
        .assertIsDisplayed()

    composeTestRule.onAllNodesWithTag("userItem").assertCountEquals(2)

    // Click on testUser2
    composeTestRule
        .onAllNodesWithTag("userItem")
        .filter(hasText(testUser2.fullName()))
        .onFirst()
        .assertIsDisplayed()
        .performClick()

    // Verify that the user profile screen is displayed and you can follow the user
    composeTestRule.onNodeWithTag("followUnfollowButton--${testUser2.uid}").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("followUnfollowButtonText--${testUser2.uid}", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("Follow")
        .performClick()
        .assertTextContains("Unfollow")

    // Go to profile screen
    composeTestRule.onNodeWithTag("Profile").performClick()

    // Verify that the following button is displayed and the person the user is following is
    // displayed
    composeTestRule.onNodeWithTag("followingButton").assertIsDisplayed().performClick()
    composeTestRule
        .onAllNodesWithTag("userItem")
        .filter(hasText(testUser2.fullName()))
        .onFirst()
        .assertIsDisplayed()

    // Verify that the unfollow button is displayed and the person the user is following is
    // displayed
    composeTestRule.onNodeWithTag("followUnfollowButton--${testUser2.uid}").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("followUnfollowButtonText--${testUser2.uid}", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("Unfollow")
        .performClick()
        .assertTextContains("Follow")

    // Go to profile screen
    composeTestRule.onNodeWithTag("Profile").performClick()

    // Verify that the following button is displayed and no one is displayed in the following list
    composeTestRule.onNodeWithTag("followingButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("followingAbsent").assertIsDisplayed()

    // Go to edit profile screen
    composeTestRule.onNodeWithTag("editProfileButton").assertIsDisplayed().performClick()

    composeTestRule.onNodeWithTag("inputUserName").performTextClearance()
    composeTestRule.onNodeWithTag("inputUserName").performTextInput("newUserName")
    assert(userViewModel.currentUser.value?.userName == testUser1.userName)
    composeTestRule.onNodeWithTag("saveButton").performClick()
    assert(userViewModel.currentUser.value?.userName == "newUserName")

    // Go to edit profile screen
    composeTestRule.onNodeWithTag("editProfileButton").assertIsDisplayed().performClick()

    composeTestRule.onNodeWithTag("inputFirstName").performTextClearance()
    composeTestRule.onNodeWithTag("inputFirstName").performTextInput("New First Name")
    assert(userViewModel.currentUser.value?.firstName == testUser1.firstName)
    composeTestRule.onNodeWithTag("saveButton").performClick()
    assert(userViewModel.currentUser.value?.firstName == "New First Name")

    // Go to edit profile screen
    composeTestRule.onNodeWithTag("editProfileButton").assertIsDisplayed().performClick()

    composeTestRule.onNodeWithTag("inputLastName").performTextClearance()
    composeTestRule.onNodeWithTag("inputLastName").performTextInput("New Last Name")
    assert(userViewModel.currentUser.value?.lastName == testUser1.lastName)
    composeTestRule.onNodeWithTag("saveButton").performClick()
    assert(userViewModel.currentUser.value?.lastName == "New Last Name")
  }

  private fun testEndToEndFlow3_init() = runTest {
    `when`(mockUserRepository.getAllUsers(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<User>) -> Unit>(0)
      onSuccess(testUsers)
    }

    // Mock the user repository to return the specified user
    `when`(mockUserRepository.getUserById(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(User) -> Unit>(1)
      val onNotFound = it.getArgument<() -> Unit>(2)
      val uid = it.arguments[0] as String

      uidToUser(uid)?.let { it1 -> onSuccess(it1) } ?: onNotFound()
    }

    `when`(mockUserRepository.getUsersById(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<User>) -> Unit>(1)
      val userIds = it.getArgument<List<String>>(0)

      onSuccess(userIds.mapNotNull(uidToUser))
    }

    // Mock add user to initialize current user
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }
    // Initialize current user
    userViewModel.addUser(testUser1, {}, {})

    `when`(mockNoteRepository.getNewUid()).thenReturn(testNote.id)

    // Mock the note repository update
    `when`(mockNoteRepository.updateNote(any(), any(), any(), any())).thenAnswer {
      testNote = it.arguments[0] as Note
      noteViewModel.selectedNote(testNote)
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Mock get note by id
    `when`(mockNoteRepository.getNoteById(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Note) -> Unit>(1)
      onSuccess(testNote)
    }

    // Mock retrieval of notes
    `when`(mockNoteRepository.getRootNotesFromUid(eq(testUser1.uid), any(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
          onSuccess(listOf(testNote))
        }

    // Start at overview screen
    composeTestRule.runOnUiThread { navController.navigate(Route.NOTE_OVERVIEW) }
  }

  private fun noteToFlashcardsMock() {
    val jsonResponse =
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
    //      runBlocking {
    //          `when`(mockOpenAI.sendRequestSuspend(anyString(),
    // anyString())).thenReturn(jsonResponse)
    //      }

    `when`(mockFlashcardRepository.getNewUid()).thenReturn("testFlashcardId")
    `when`(mockDeckRepository.getNewUid()).thenReturn("testDeckId")
    `when`(mockFlashcardRepository.addFlashcard(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    `when`(mockDeckRepository.updateDeck(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
  }

  // Test the end-to-end flow of creating a note, converting it into a flashcard and playing its
  // decks
  @Test
  fun testEndToEndFlow3() = runTest {
    testEndToEndFlow3_init()
    noteToFlashcardsMock()

    // Create a root note
    composeTestRule.onNodeWithTag("createNoteOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteOrFolder").performClick()
    composeTestRule.onNodeWithTag("createNote").performClick()
    composeTestRule.onNodeWithTag("confirmNoteAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputNoteName").performTextInput(testNote.title)
    composeTestRule.onNodeWithTag("currentVisibilityOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("previousVisibility").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nextVisibility").performClick()
    composeTestRule.onNodeWithTag("confirmNoteAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmNoteAction").performClick()
    composeTestRule.onNodeWithTag("saveNoteButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("saveNoteButton").performClick()
    composeTestRule.onNodeWithTag("closeButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("closeButton").performClick()

    // Verify the notes are displayed
    composeTestRule.onNodeWithTag("noteAndFolderList").assertIsDisplayed()

    // Verify that the note card is displayed
    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()

    // Display the note bottom sheet and convert to flashcard
    composeTestRule.onNodeWithTag("showBottomSheetButton").performClick()
    composeTestRule.onNodeWithTag("noteModalBottomSheet").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("convertToFlashcardButton")
        .assertIsDisplayed() // change node name to the correct one
    composeTestRule.onNodeWithTag("convertToFlashcardButton").performClick()

    // (Imagining it goes directly to deck play menu)
    // Show deck play bottom sheet
    composeTestRule.onNodeWithTag("deckPlayButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deckPlayButton").performClick()
    composeTestRule.onNodeWithTag("playModesBottomSheet").assertIsDisplayed()

    // Play the deck
    composeTestRule.onNodeWithTag("playMode--TEST").assertIsDisplayed()
    composeTestRule.onNodeWithTag("playMode--TEST").performClick()

    // Flip card, select got it right and submit
    composeTestRule.onNodeWithTag("flashcardColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("flashcard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("flashcardFront").assertIsDisplayed()
    composeTestRule.onNodeWithTag("flashcard").performClick()
    composeTestRule.onNodeWithTag("flashcardBack").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gotItRightButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gotItRightButton").performClick()

    composeTestRule.onNodeWithTag("submitButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("submitButton").performClick()
    composeTestRule.onNodeWithTag("FinishedScreenColumn").assertIsDisplayed()
  }

  private fun testEndToEndFlow4_init() {

    `when`(mockUserRepository.getAllUsers(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<User>) -> Unit>(0)
      onSuccess(testUsers)
    }

    // Mock the user repository to return the specified user
    `when`(mockUserRepository.getUserById(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(User) -> Unit>(1)
      val onNotFound = it.getArgument<() -> Unit>(2)
      val uid = it.arguments[0] as String

      uidToUser(uid)?.let { it1 -> onSuccess(it1) } ?: onNotFound()
    }

    `when`(mockUserRepository.getUsersById(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<User>) -> Unit>(1)
      val userIds = it.getArgument<List<String>>(0)

      onSuccess(userIds.mapNotNull(uidToUser))
    }

    // Mock add user to initialize current user
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Initialize current user
    userViewModel.addUser(testUser1, {}, {})

    `when`(mockNoteRepository.getPublicNotes(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(listOf(testNoteUser2))
    }

    // mock the note repository method called when saving and unsaving a note

    // Start at overview screen
    composeTestRule.runOnUiThread { navController.navigate(Route.NOTE_OVERVIEW) }
  }

  // Test the end-to-end flow of saving a note and viewing it in the current user saved overview
  // screen,
  // checking the note is there and cannot be modified, and unsaving it and checking it is no longer
  // there
  @Test
  fun testEndToEndFlow4() {
    testEndToEndFlow4_init()

    // Go to search screen
    composeTestRule.onNodeWithTag("Search").performClick()

    // Search for a note from testUser2 to save it
    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement("sampleTitle")
    composeTestRule.onNodeWithTag("filteredNoteList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()

    // Verify the note is displayed
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText(testNoteUser2.title))
        .onFirst()
        .assertIsDisplayed()

    // Go to the note
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText(testNoteUser2.title))
        .onFirst()
        .assertIsDisplayed()
        .performClick()

    // verify the bookmark button is displayed and click it
    composeTestRule.onNodeWithTag("saveNoteButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("saveNoteButton").performClick()
    composeTestRule.onNodeWithTag("closeButton").performClick()

    // Go to saved overview screen
    composeTestRule.onNodeWithTag("Note Overview").performClick()
    composeTestRule.onNodeWithTag("segmentedButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("segmentedButtonSavedOption").performClick()

    // Verify the saved note is there
    composeTestRule.onNodeWithTag("noteAndFolderListSavedScreen").assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText(testNoteUser2.title))
        .onFirst()
        .assertIsDisplayed()

    // Go to the note and unsave it
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText(testNoteUser2.title))
        .onFirst()
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithTag("removeSavedDocumentButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("removeSavedDocumentButton").performClick()
    composeTestRule.onNodeWithTag("closeButton").performClick()

    // Verify the note is not displayed anymore
    composeTestRule.onNodeWithTag("noteAndFolderListSavedScreen").assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText(testNoteUser2.title))
        .assertCountEquals(0)
  }
}
