package com.github.onlynotesswent.ui.search

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderRepository
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.Course
import com.github.onlynotesswent.utils.Visibility
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class SearchScreenTest {
  @Mock private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var noteRepository: NoteRepository
  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var folderRepository: FolderRepository
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var folderViewModel: FolderViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private val testNote1 =
      Note(
          id = "",
          title = "Note 1",
          date = Timestamp.now(),
          visibility = Visibility.PUBLIC,
          userId = "1",
          noteCourse = Course("CS-100", "Sample Course 1", 2024, "path"),
      )
  private val testNote2 =
      Note(
          id = "1",
          title = "Note 2",
          date = Timestamp.now(),
          visibility = Visibility.PUBLIC,
          userId = "2",
          noteCourse = Course("CS-200", "Sample Course 2", 2024, "path"),
      )

  private val testNotes = listOf(testNote1, testNote2)

  private val testUser1 =
      User(
          firstName = "User One",
          lastName = "Name One",
          userName = "username1",
          email = "example@gmail.com",
          uid = "1")

  private val testUser2 =
      User(
          firstName = "User Two",
          lastName = "Name Two",
          userName = "username2",
          email = "example2@gmail.com",
          uid = "2")

  private val testUsers = listOf(testUser1, testUser2)

  private val testFolder1 =
      Folder(
          id = "1",
          name = "Folder 1",
          parentFolderId = null,
          userId = "1",
      )

  private val testFolder2 =
      Folder(
          id = "2",
          name = "Folder 2",
          parentFolderId = null,
          userId = "2",
      )

  private val testFolders = listOf(testFolder1, testFolder2)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SEARCH)
    `when`(noteRepository.getPublicNotes(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(0)
      onSuccess(testNotes)
    }
    `when`(userRepository.getAllUsers(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<User>) -> Unit>(0)
      onSuccess(testUsers)
    }
    `when`(folderRepository.getPublicFolders(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(0)
      onSuccess(testFolders)
    }

    userViewModel = UserViewModel(userRepository)
    noteViewModel = NoteViewModel(noteRepository)
    folderViewModel = FolderViewModel(folderRepository)
  }

  @Test
  fun testSearchFieldVisibility() {
    composeTestRule.setContent {
      SearchScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
    }

    composeTestRule.onNodeWithTag("searchScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
  }

  @Test
  fun testEmptySearchQuery() {
    composeTestRule.setContent {
      SearchScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
    }

    composeTestRule.onNodeWithTag("filteredNoteList").assertDoesNotExist()
    composeTestRule.onNodeWithTag("filteredUserList").assertDoesNotExist()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
  }

  @Test
  fun testValidSearchQueryShowsOneResult() {
    composeTestRule.setContent {
      SearchScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
    }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testNote1.title)
    composeTestRule.onNodeWithTag("noteFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredNoteList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText(testNote1.title))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("noteCard").assertCountEquals(1)

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement(testUser1.firstName)
    composeTestRule.onNodeWithTag("userFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredUserList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("userCard")
        .filter(hasText(testUser1.fullName()))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("userCard").assertCountEquals(1)

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement(testFolder1.name)
    composeTestRule.onNodeWithTag("folderFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredFolderList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("folderCard")
        .filter(hasText(testFolder1.name))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("folderCard").assertCountEquals(1)
  }

  @Test
  fun testValidSearchQueryShowsMultipleResults() {
    composeTestRule.setContent {
      SearchScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
    }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Note")
    composeTestRule.onNodeWithTag("noteFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredNoteList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText("Note 1"))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText("Note 2"))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("noteCard").assertCountEquals(2)

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement("User")
    composeTestRule.onNodeWithTag("userFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredUserList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("userCard")
        .filter(hasText(testUser1.fullName()))
        .onFirst()
        .assertIsDisplayed()

    composeTestRule
        .onAllNodesWithTag("userCard")
        .filter(hasText(testUser2.fullName()))
        .onFirst()
        .assertIsDisplayed()

    composeTestRule.onAllNodesWithTag("userCard").assertCountEquals(2)

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement("Folder")
    composeTestRule.onNodeWithTag("folderFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredFolderList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("folderCard")
        .filter(hasText(testFolder1.name))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("folderCard")
        .filter(hasText(testFolder2.name))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("folderCard").assertCountEquals(2)
  }

  @Test
  fun testNoSearchResultsMessage() {
    composeTestRule.setContent {
      SearchScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
    }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Non-existent Note")
    composeTestRule.onNodeWithTag("noteFilterChip").performClick()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No notes found matching your search.").assertIsDisplayed()

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Non-existent User")
    composeTestRule.onNodeWithTag("userFilterChip").performClick()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No users found matching your search.").assertIsDisplayed()

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Non-existent Folder")
    composeTestRule.onNodeWithTag("folderFilterChip").performClick()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No folders found matching your search.").assertIsDisplayed()
  }

  @Test
  fun testNoteSelectionNavigatesToEditScreen() {
    composeTestRule.setContent {
      SearchScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
    }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testNote1.title)
    composeTestRule.onNodeWithTag("noteFilterChip").performClick()
    composeTestRule.onNodeWithTag("filteredNoteList").onChildren().onFirst().performClick()

    verify(navigationActions).navigateTo(Screen.EDIT_NOTE)
  }

  @Test
  fun testUserSelectionNavigatesToUserProfileScreen() {
    composeTestRule.setContent {
      SearchScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
    }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testUser1.userName)
    composeTestRule.onNodeWithTag("userFilterChip").performClick()
    composeTestRule.onNodeWithTag("filteredUserList").onChildren().onFirst().performClick()

    verify(navigationActions).navigateTo(Screen.PUBLIC_PROFILE)
  }

  @Test
  fun testFolderSelectionNavigatesToFolderScreen() {
    composeTestRule.setContent {
      SearchScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
    }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testFolder1.name)
    composeTestRule.onNodeWithTag("folderFilterChip").performClick()
    composeTestRule.onNodeWithTag("filteredFolderList").onChildren().onFirst().performClick()

    verify(navigationActions).navigateTo(Screen.FOLDER_CONTENTS)
  }
}
