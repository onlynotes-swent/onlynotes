package com.github.onlynotesswent.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.test.espresso.intent.Intents
import com.github.onlynotesswent.model.file.FileRepository
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.folder.FolderRepository
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.overview.AddNoteScreen
import com.github.onlynotesswent.ui.overview.EditMarkdownScreen
import com.github.onlynotesswent.ui.overview.EditNoteScreen
import com.github.onlynotesswent.ui.overview.FolderContentScreen
import com.github.onlynotesswent.ui.overview.OverviewScreen
import com.github.onlynotesswent.ui.theme.AppTheme
import com.github.onlynotesswent.ui.user.CreateUserScreen
import com.github.onlynotesswent.ui.user.EditProfileScreen
import com.github.onlynotesswent.ui.user.PublicProfileScreen
import com.github.onlynotesswent.ui.user.UserProfileScreen
import com.github.onlynotesswent.utils.Course
import com.github.onlynotesswent.utils.ProfilePictureTaker
import com.github.onlynotesswent.utils.Scanner
import com.github.onlynotesswent.utils.Visibility
import com.google.firebase.Timestamp
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
  private lateinit var navController: NavHostController
  private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  @Mock private lateinit var noteRepository: NoteRepository
  private lateinit var noteViewModel: NoteViewModel
  @Mock private lateinit var folderRepository: FolderRepository
  private lateinit var folderViewModel: FolderViewModel
  @Mock private lateinit var fileRepository: FileRepository
  private lateinit var fileViewModel: FileViewModel

  @Mock private lateinit var profilePictureTaker: ProfilePictureTaker
  @Mock private lateinit var scanner: Scanner

  // Sample user and note data for testing
  private val testUid = "testUid123"
  private val testUser =
      User(
          firstName = "testFirstName",
          lastName = "testLastName",
          userName = "testUserName",
          email = "testEmail",
          uid = testUid,
          dateOfJoining = Timestamp.now(),
          rating = 0.0)

  private val testNote =
      Note(
          id = "1",
          title = "title",
          content = "",
          date = Timestamp.now(),
          userId = testUid,
          visibility = Visibility.DEFAULT,
          noteCourse = Course("courseCode", "courseName", 2024, "publicPath"),
          image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

  // Setup Compose test rule for UI testing
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock objects for dependencies
    MockitoAnnotations.openMocks(this)

    userViewModel = UserViewModel(userRepository)
    noteViewModel = NoteViewModel(noteRepository)
    folderViewModel = FolderViewModel(folderRepository)
    fileViewModel = FileViewModel(fileRepository)

    // Set up mock behavior for user and note repository methods
    `when`(userViewModel.getNewUid()).thenReturn(testUid)

    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    `when`(noteRepository.getNewUid()).thenReturn(testNote.id)

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
                  composable(Screen.ADD_NOTE) {
                    AddNoteScreen(
                        navigationActions, scanner, noteViewModel, userViewModel, fileViewModel)
                  }
                  composable(Screen.EDIT_NOTE) {
                    EditNoteScreen(navigationActions, noteViewModel, userViewModel, fileViewModel)
                  }
                  composable(Screen.FOLDER_CONTENTS) {
                    FolderContentScreen(
                        navigationActions, folderViewModel, noteViewModel, userViewModel)
                  }
                  composable(Screen.EDIT_MARKDOWN) {
                    EditMarkdownScreen(
                        navigationActions, noteViewModel, userViewModel, fileViewModel)
                  }
                }
                navigation(
                    startDestination = Screen.USER_PROFILE,
                    route = Route.PROFILE,
                ) {
                  composable(Screen.USER_PROFILE) {
                    UserProfileScreen(navigationActions, userViewModel, fileViewModel)
                  }
                  composable(Screen.PUBLIC_PROFILE) {
                    PublicProfileScreen(navigationActions, userViewModel, fileViewModel)
                  }
                  composable(Screen.EDIT_PROFILE) {
                    EditProfileScreen(
                        navigationActions, userViewModel, profilePictureTaker, fileViewModel)
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

  // Test the end-to-end flow of creating a user, adding a note, and editing the note
  @Test
  fun testEndToEndFlow1() {
    // Interact with the input fields for creating a user
    composeTestRule.onNodeWithTag("inputFirstName").performTextInput(testUser.firstName)
    composeTestRule.onNodeWithTag("inputLastName").performTextInput(testUser.lastName)
    composeTestRule.onNodeWithTag("inputUserName").performTextInput(testUser.userName)

    // Verify that the "Create User" button is enabled and then click it
    composeTestRule.onNodeWithTag("saveButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("saveButton").performClick()

    // Interact with the note creation flow
    composeTestRule.onNodeWithTag("createNoteOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNoteOrFolder").performClick()
    composeTestRule.onNodeWithTag("createNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createNote").performClick()

    // Verify that the "Create Note" button is initially disabled
    composeTestRule.onNodeWithTag("createNoteButton").assertIsNotEnabled()

    // Input note details and interact with dropdowns
    composeTestRule.onNodeWithTag("inputNoteTitle").performTextInput(testNote.title)

    // Set visibility to "Public"
    composeTestRule.onNodeWithTag("visibilityButton").performClick()
    composeTestRule
        .onNodeWithTag("visibilityMenu")
        .onChildren()
        .filter(hasText("Public"))
        .onFirst()
        .performClick()

    // Set template to "Create Note"
    composeTestRule.onNodeWithTag("templateButton").performClick()
    composeTestRule
        .onNodeWithTag("templateMenu")
        .onChildren()
        .filter(hasText("Create note"))
        .onFirst()
        .performClick()

    // Verify that the "Create Note" button is now enabled and click it
    composeTestRule.onNodeWithTag("createNoteButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("createNoteButton").performClick()

    // Modify the note title and save the changes
    composeTestRule.onNodeWithTag("EditTitle textField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditTitle textField").performTextInput("Updated Title")

    composeTestRule.onNodeWithTag("editNoteColumn").performScrollToNode(hasTestTag("Save button"))
    composeTestRule.onNodeWithTag("Save button").performClick()

    // Mock retrieval of notes
    `when`(noteRepository.getRootNotesFrom(eq(testUser.uid), any(), any())).thenAnswer { invocation
      ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(listOf(testNote))
    }

    // Trigger note retrieval and verify the notes are displayed
    noteViewModel.getRootNotesFrom(testUser.uid)
    composeTestRule.onNodeWithTag("noteAndFolderList").assertIsDisplayed()

    // Verify that the note card is displayed
    composeTestRule.onNodeWithTag("noteCard").assertIsDisplayed()
  }
}
