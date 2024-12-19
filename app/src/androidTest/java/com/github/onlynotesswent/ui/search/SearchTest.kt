package com.github.onlynotesswent.ui.search

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.file.FileRepository
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.DeckRepository
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderRepository
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepository
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
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
  @Mock private lateinit var deckRepository: DeckRepository
  @Mock private lateinit var fileRepository: FileRepository
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var folderViewModel: FolderViewModel
  private lateinit var deckViewModel: DeckViewModel
  private lateinit var fileViewModel: FileViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private val testNote1 =
      Note(
          id = "",
          title = "Note 1",
          date = Timestamp.now(),
          lastModified = Timestamp.now(),
          visibility = Visibility.PUBLIC,
          userId = "1",
          noteCourse = Course("CS-100", "Sample Course 1", 2024, "path"),
      )
  private val testNote2 =
      Note(
          id = "1",
          title = "Note 2",
          date = Timestamp.now(),
          lastModified = Timestamp.now(),
          visibility = Visibility.PUBLIC,
          userId = "2",
          noteCourse = Course("CS-200", "Sample Course 2", 2024, "path"),
      )

  private val testNotes = listOf(testNote1, testNote2)

  private val testFriendNote1 =
      Note(
          id = "10",
          title = "Note 1",
          date = Timestamp.now(),
          lastModified = Timestamp.now(),
          visibility = Visibility.FRIENDS,
          userId = "1",
          noteCourse = Course("CS-100", "Sample Course 1", 2024, "path"),
      )
  private val testFriendNote2 =
      Note(
          id = "11",
          title = "Note 2",
          date = Timestamp.now(),
          lastModified = Timestamp.now(),
          visibility = Visibility.FRIENDS,
          userId = "2",
          noteCourse = Course("CS-200", "Sample Course 2", 2024, "path"),
      )

  private val testFriendsNotes = listOf(testFriendNote1, testFriendNote2)

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
          lastModified = Timestamp.now(),
      )

  private val testFolder2 =
      Folder(
          id = "2",
          name = "Folder 2",
          parentFolderId = null,
          userId = "2",
          lastModified = Timestamp.now(),
      )

  private val testFolders = listOf(testFolder1, testFolder2)

  private val testFriendFolder1 =
      Folder(
          id = "21",
          name = "Folder 1",
          parentFolderId = null,
          userId = "1",
          lastModified = Timestamp.now(),
          visibility = Visibility.FRIENDS,
      )

  private val testFriendFolder2 =
      Folder(
          id = "22",
          name = "Folder 2",
          parentFolderId = null,
          userId = "2",
          lastModified = Timestamp.now(),
          visibility = Visibility.FRIENDS,
      )

  private val testFriendsFolders = listOf(testFriendFolder1, testFriendFolder2)

  private val testDeck1 =
      Deck(
          id = "1",
          name = "Deck 1",
          userId = "1",
          folderId = "1",
          visibility = Visibility.PUBLIC,
          lastModified = Timestamp.now(),
          flashcardIds = listOf("1", "2"))
  private val testDeck2 =
      Deck(
          id = "2",
          name = "Deck 2",
          userId = "2",
          folderId = "2",
          visibility = Visibility.PUBLIC,
          lastModified = Timestamp.now(),
          flashcardIds = listOf("3", "4"))

  private val testDecks = listOf(testDeck1, testDeck2)

  private val testFriendDeck1 =
      Deck(
          id = "31",
          name = "Deck 1",
          userId = "1",
          folderId = "1",
          visibility = Visibility.FRIENDS,
          lastModified = Timestamp.now(),
          flashcardIds = listOf("1", "2"))

  private val testFriendDeck2 =
      Deck(
          id = "32",
          name = "Deck 2",
          userId = "2",
          folderId = "2",
          visibility = Visibility.FRIENDS,
          lastModified = Timestamp.now(),
          flashcardIds = listOf("3", "4"))

  private val testFriendsDecks = listOf(testFriendDeck1, testFriendDeck2)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SEARCH)
    `when`(noteRepository.getPublicNotes(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(0)
      onSuccess(testNotes)
    }
    `when`(noteRepository.getNotesFromFollowingList(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Note>) -> Unit>(1)
      onSuccess(testFriendsNotes)
    }

    `when`(userRepository.getAllUsers(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<User>) -> Unit>(0)
      onSuccess(testUsers)
    }
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    `when`(folderRepository.getPublicFolders(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(0)
      onSuccess(testFolders)
    }
    `when`(folderRepository.getFoldersFromFollowingList(any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(testFriendsFolders)
    }
    `when`(deckRepository.getPublicDecks(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Deck>) -> Unit>(0)
      onSuccess(testDecks)
    }
    `when`(deckRepository.getDecksFromFollowingList(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Deck>) -> Unit>(1)
      onSuccess(testFriendsDecks)
    }

    userViewModel = UserViewModel(userRepository)
    noteViewModel = NoteViewModel(noteRepository)
    folderViewModel = FolderViewModel(folderRepository)
    deckViewModel = DeckViewModel(deckRepository)
    fileViewModel = FileViewModel(fileRepository)

    userViewModel.addUser(testUser1, {}, {})

    composeTestRule.setContent {
      SearchScreen(
          navigationActions,
          noteViewModel,
          userViewModel,
          folderViewModel,
          deckViewModel,
          fileViewModel)
    }
  }

  @Test
  fun testSearchFieldVisibility() {
    composeTestRule.onNodeWithTag("searchScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
  }

  @Test
  fun testEmptySearchQuery() {
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("noteFilterChip").performClick()
    composeTestRule.onNodeWithTag("additionalFilterChip--PUBLIC").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--PUBLIC").performClick()
    composeTestRule.onNodeWithTag("filteredPublicNoteList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").performClick()
    composeTestRule.onNodeWithTag("filteredFriendNoteList").assertIsDisplayed()

    composeTestRule.onNodeWithTag("userFilterChip").performClick()
    composeTestRule.onNodeWithTag("filteredUserList").assertIsDisplayed()

    composeTestRule.onNodeWithTag("folderFilterChip").performClick()
    composeTestRule.onNodeWithTag("additionalFilterChip--PUBLIC").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--PUBLIC").performClick()
    composeTestRule.onNodeWithTag("filteredPublicFolderList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").performClick()
    composeTestRule.onNodeWithTag("filteredFriendFolderList").assertIsDisplayed()

    composeTestRule.onNodeWithTag("deckFilterChip").performClick()
    composeTestRule.onNodeWithTag("additionalFilterChip--PUBLIC").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--PUBLIC").performClick()
    composeTestRule.onNodeWithTag("filteredPublicDeckList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").performClick()
    composeTestRule.onNodeWithTag("filteredFriendDeckList").assertIsDisplayed()
  }

  @Test
  fun testValidFriendSearchQueryShowsOneResult() {
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testFriendNote1.title)
    composeTestRule.onNodeWithTag("noteFilterChip").performClick()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").performClick()

    composeTestRule.onNodeWithTag("filteredFriendNoteList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("noteCard")
        .filter(hasText(testFriendNote1.title))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("noteCard").assertCountEquals(1)

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement(testFriendFolder1.name)
    composeTestRule.onNodeWithTag("folderFilterChip").performClick()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").performClick()

    composeTestRule.onNodeWithTag("filteredFriendFolderList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("folderCard")
        .filter(hasText(testFriendFolder1.name))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("folderCard").assertCountEquals(1)

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement(testFriendDeck1.name)
    composeTestRule.onNodeWithTag("deckFilterChip").performClick()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").performClick()

    composeTestRule.onNodeWithTag("filteredFriendDeckList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("deckCard")
        .filter(hasText(testFriendDeck1.name))
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun testValidSearchQueryShowsOneResult() {
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testNote1.title)
    composeTestRule.onNodeWithTag("noteFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredPublicNoteList").assertIsDisplayed()
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
        .onAllNodesWithTag("userItem")
        .filter(hasText(testUser1.fullName()))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("userItem").assertCountEquals(1)

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement(testFolder1.name)
    composeTestRule.onNodeWithTag("folderFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredPublicFolderList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("folderCard")
        .filter(hasText(testFolder1.name))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("folderCard").assertCountEquals(1)

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement(testDeck1.name)
    composeTestRule.onNodeWithTag("deckFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredPublicDeckList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("deckCard")
        .filter(hasText(testDeck1.name))
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun testValidSearchQueryShowsMultipleResults() {
    // View 2 notes:
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Note")
    composeTestRule.onNodeWithTag("noteFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredPublicNoteList").assertIsDisplayed()
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

    // View 2 users:
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

    // View 2 folders:
    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement("Folder")
    composeTestRule.onNodeWithTag("folderFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredPublicFolderList").assertIsDisplayed()
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

    // View 2 decks:
    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement("Deck")
    composeTestRule.onNodeWithTag("deckFilterChip").performClick()

    composeTestRule.onNodeWithTag("filteredPublicDeckList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("deckCard")
        .filter(hasText(testDeck1.name))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("deckCard")
        .filter(hasText(testDeck2.name))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("deckCard").assertCountEquals(2)
  }

  @Test
  fun testNoSearchResultsMessage() {
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Non-existent Note")
    composeTestRule.onNodeWithTag("noteFilterChip").performClick()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No notes found matching your search.").assertIsDisplayed()
    // Check friend only notes behavior
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").performClick()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No notes found matching your search.").assertIsDisplayed()

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement("Non-existent User")
    composeTestRule.onNodeWithTag("userFilterChip").performClick()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No users found matching your search.").assertIsDisplayed()

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement("Non-existent Folder")
    composeTestRule.onNodeWithTag("folderFilterChip").performClick()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No folders found matching your search.").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").performClick()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No folders found matching your search.").assertIsDisplayed()

    composeTestRule.onNodeWithTag("searchTextField").performTextReplacement("Non-existent Deck")
    composeTestRule.onNodeWithTag("deckFilterChip").performClick()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No decks found matching your search.").assertIsDisplayed()
    composeTestRule.onNodeWithTag("additionalFilterChip--FRIENDS").performClick()
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()
    composeTestRule.onNodeWithText("No decks found matching your search.").assertIsDisplayed()
  }

  @Test
  fun testNoteSelectionNavigatesToEditScreen() {
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testNote1.title)
    composeTestRule.onNodeWithTag("noteFilterChip").performClick()
    composeTestRule.onNodeWithTag("filteredPublicNoteList").onChildren().onFirst().performClick()

    verify(navigationActions).navigateTo(Screen.EDIT_NOTE)
  }

  @Test
  fun testUserSelectionNavigatesToUserProfileScreen() {
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testUser2.userName)
    composeTestRule.onNodeWithTag("userFilterChip").performClick()
    composeTestRule
        .onNodeWithTag("filteredUserList")
        .onChildren()
        .filter(hasText(testUser2.fullName()))
        .onFirst()
        .performClick()

    val publicProfileScreen = Screen.PUBLIC_PROFILE.replace("{userId}", testUser2.uid)
    verify(navigationActions).navigateTo(publicProfileScreen)
  }

  @Test
  fun testFolderSelectionNavigatesToFolderScreen() {
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testFolder1.name)
    composeTestRule.onNodeWithTag("folderFilterChip").performClick()
    composeTestRule.onNodeWithTag("filteredPublicFolderList").onChildren().onFirst().performClick()

    val folderContentsScreen = Screen.FOLDER_CONTENTS.replace("{folderId}", testFolder1.id)
    verify(navigationActions).navigateTo(folderContentsScreen)
  }

  @Test
  fun testDeckSelectionNavigatesToDeckMenuScreen() {
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(testDeck2.name)
    composeTestRule.onNodeWithTag("deckFilterChip").performClick()
    composeTestRule.onNodeWithTag("filteredPublicDeckList").onChildren().onFirst().performClick()

    val deckMenuScreen = Screen.DECK_MENU.replace("{deckId}", testDeck2.id)
    verify(navigationActions).navigateTo(deckMenuScreen)
  }
}
