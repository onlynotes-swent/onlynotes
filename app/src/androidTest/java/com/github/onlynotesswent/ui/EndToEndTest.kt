package com.github.onlynotesswent.ui

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
import com.github.onlynotesswent.model.file.FileRepository
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.deck.DeckRepository
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
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
import com.github.onlynotesswent.ui.overview.OverviewScreen
import com.github.onlynotesswent.ui.overview.editnote.EditMarkdownScreen
import com.github.onlynotesswent.ui.overview.editnote.EditNoteScreen
import com.github.onlynotesswent.ui.search.SearchScreen
import com.github.onlynotesswent.ui.theme.AppTheme
import com.github.onlynotesswent.ui.user.CreateUserScreen
import com.github.onlynotesswent.ui.user.EditProfileScreen
import com.github.onlynotesswent.ui.user.PublicProfileScreen
import com.github.onlynotesswent.ui.user.UserProfileScreen
import com.github.onlynotesswent.utils.PictureTaker
import com.google.firebase.Timestamp
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

  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var noteRepository: NoteRepository
  @Mock private lateinit var folderRepository: FolderRepository
  @Mock private lateinit var deckRepository: DeckRepository
  @Mock private lateinit var fileRepository: FileRepository
  @Mock private lateinit var pictureTaker: PictureTaker
  private lateinit var userViewModel: UserViewModel
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var folderViewModel: FolderViewModel
  private lateinit var deckViewModel: DeckViewModel
  private lateinit var fileViewModel: FileViewModel
  @Mock private lateinit var mockNotificationRepository: NotificationRepository
  private lateinit var notificationViewModel: NotificationViewModel

  @Mock private lateinit var authenticator: Authenticator

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

  private val newTitle = "New Title"

  // Setup Compose test rule for UI testing
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock objects for dependencies
    MockitoAnnotations.openMocks(this)

    userViewModel = UserViewModel(userRepository, mockNotificationRepository)
    noteViewModel = NoteViewModel(noteRepository)
    folderViewModel = FolderViewModel(folderRepository)
    deckViewModel = DeckViewModel(deckRepository)
    fileViewModel = FileViewModel(fileRepository)
    notificationViewModel = NotificationViewModel(mockNotificationRepository)

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
                    startDestination = Screen.OVERVIEW,
                    route = Route.OVERVIEW,
                ) {
                  composable(Screen.OVERVIEW) {
                    OverviewScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
                  }
                  composable(Screen.EDIT_NOTE) {
                    EditNoteScreen(navigationActions, noteViewModel, userViewModel)
                  }
                  composable(Screen.FOLDER_CONTENTS) {
                    FolderContentScreen(
                        navigationActions, folderViewModel, noteViewModel, userViewModel)
                  }
                  composable(Screen.EDIT_NOTE_MARKDOWN) {
                    EditMarkdownScreen(navigationActions, noteViewModel, fileViewModel)
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
                        fileViewModel)
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
                        fileViewModel,
                        notificationViewModel,
                        authenticator)
                  }
                  composable(Screen.PUBLIC_PROFILE) {
                    PublicProfileScreen(
                        navigationActions,
                        userViewModel,
                        fileViewModel,
                        notificationViewModel,
                        authenticator)
                  }
                  composable(Screen.EDIT_PROFILE) {
                    EditProfileScreen(
                        navigationActions,
                        userViewModel,
                        pictureTaker,
                        fileViewModel,
                        noteViewModel,
                        folderViewModel)
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

    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    `when`(noteRepository.getNewUid()).thenReturn(testNote.id)

    // Mock the note repository update
    `when`(noteRepository.updateNote(any(), any(), any(), any())).thenAnswer {
      testNote = it.arguments[0] as Note
      noteViewModel.selectedNote(testNote)
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Mock get note by id
    `when`(noteRepository.getNoteById(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Note) -> Unit>(1)
      onSuccess(testNote)
    }

    // Mock retrieval of notes
    `when`(noteRepository.getRootNotesFromUid(eq(testUser1.uid), any(), any(), any())).thenAnswer {
        invocation ->
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
    `when`(userRepository.getAllUsers(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<User>) -> Unit>(0)
      onSuccess(testUsers)
    }

    // Mock the user repository to return the specified user
    `when`(userRepository.getUserById(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(User) -> Unit>(1)
      val onNotFound = it.getArgument<() -> Unit>(2)
      val uid = it.arguments[0] as String

      uidToUser(uid)?.let { it1 -> onSuccess(it1) } ?: onNotFound()
    }

    `when`(userRepository.getUsersById(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<User>) -> Unit>(1)
      val userIds = it.getArgument<List<String>>(0)

      onSuccess(userIds.mapNotNull(uidToUser))
    }

    // Mock add user to initialize current user
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }
    // Initialize current user
    userViewModel.addUser(testUser1, {}, {})

    `when`(userRepository.addFollowerTo(any(), any(), any(), any(), any())).thenAnswer {
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

    `when`(userRepository.removeFollowerFrom(any(), any(), any(), any(), any())).thenAnswer {
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

    `when`(userRepository.updateUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Start at overview screen
    composeTestRule.runOnUiThread { navController.navigate(Route.OVERVIEW) }

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
}
