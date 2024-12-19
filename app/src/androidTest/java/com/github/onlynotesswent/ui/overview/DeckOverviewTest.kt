package com.github.onlynotesswent.ui.overview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.DeckRepository
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderRepository
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepository
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class DeckOverviewTest {
  @Mock private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  @Mock private lateinit var navigationActions: NavigationActions
  private lateinit var deckViewModel: DeckViewModel
  @Mock private lateinit var deckRepository: DeckRepository
  private lateinit var folderViewModel: FolderViewModel
  @Mock private lateinit var folderRepository: FolderRepository

  private val deckList =
      listOf(
          Deck(
              id = "1",
              name = "name",
              userId = "1",
              folderId = null,
              visibility = Visibility.DEFAULT,
              lastModified = Timestamp.now(),
              description = "description"))

  private val folderList =
      listOf(
          Folder(
              id = "2",
              isDeckFolder = true,
              name = "name",
              userId = "1",
              parentFolderId = null,
              lastModified = Timestamp.now()),
          Folder(
              id = "3",
              isDeckFolder = true,
              name = "name2",
              userId = "1",
              parentFolderId = null,
              lastModified = Timestamp.now()))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(userRepository)
    deckViewModel = DeckViewModel(deckRepository)
    folderViewModel = FolderViewModel(folderRepository)

    // Mock folder repository to return new folder id
    `when`(folderRepository.getNewFolderId()).thenReturn("2")

    // Mock the addUser method to call the onSuccess callback
    `when`(userRepository.addUser(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }

    val testUser =
        User(
            firstName = "testFirstName",
            lastName = "testLastName",
            userName = "testUserName",
            email = "testEmail",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0)
    userViewModel.addUser(testUser, {}, {})

    // Mock the current route to be the user create screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.DECK_OVERVIEW)
    composeTestRule.setContent {
      DeckOverviewScreen(navigationActions, deckViewModel, userViewModel, folderViewModel)
    }
  }

  private fun mockGetRootDecksFromUserId() {
    `when`(deckRepository.getRootDecksFromUserId(eq("1"), any(), any())).then { invocation ->
      val onSuccess = invocation.getArgument<(List<Deck>) -> Unit>(1)
      onSuccess(deckList)
    }
    deckViewModel.getRootDecksFromUserId("1")
  }

  @Test
  fun deckAndFolderListIsDisplayed() = runTest {
    mockGetRootDecksFromUserId()
    `when`(folderRepository.getRootDeckFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    folderViewModel.getRootFoldersFromUserId("1", isDeckView = true)
    composeTestRule.onNodeWithTag("deckAndFolderList").assertIsDisplayed()
  }

  @Test
  fun editDeckClickCallsNavActions() = runTest {
    mockGetRootDecksFromUserId()
    composeTestRule.onNodeWithTag("deckCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deckCard").performClick()
    verify(navigationActions).navigateTo(screen = Screen.DECK_MENU)
  }

  @Test
  fun displayTextWhenEmpty() = runTest {
    composeTestRule.onNodeWithTag("emptyDeckAndFolderPrompt").assertIsDisplayed()
  }

  @Test
  fun displayTextWhenUserHasNoDecks() = runTest {
    deckViewModel.getRootDecksFromUserId("2") // User 2 has no publicDecks
    composeTestRule.onNodeWithTag("emptyDeckAndFolderPrompt").assertIsDisplayed()
  }

  @Test
  fun selectFolderCallsNavActions() = runTest {
    mockGetRootDecksFromUserId()
    `when`(folderRepository.getRootDeckFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    folderViewModel.getRootFoldersFromUserId("1", isDeckView = true)
    composeTestRule.onAllNodesWithTag("folderCard").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("folderCard").onFirst().performClick()
    val folderContentsScreen =
        Screen.FOLDER_CONTENTS.replace(oldValue = "{folderId}", newValue = folderList[0].id)
    verify(navigationActions).navigateTo(folderContentsScreen)
  }

  @Test
  fun displayBaseComponents() {
    composeTestRule.onNodeWithTag("createObjectOrFolder").assertExists()
    composeTestRule.onNodeWithTag("emptyDeckAndFolderPrompt").assertExists()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    composeTestRule.onNodeWithTag("createObjectOrFolder").performClick()
    composeTestRule.onNodeWithTag("createDeckOrNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").assertIsDisplayed()
  }

  @Test
  fun createDeckButtonShowsDialog() {
    mockGetRootDecksFromUserId()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createObjectOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createObjectOrFolder").performClick()
    composeTestRule.onNodeWithTag("createDeckOrNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createDeckOrNote").performClick()
    composeTestRule.onNodeWithTag("DeckDialog").assertIsDisplayed()
  }

  @Test
  fun createFolderButtonShowsDialog() {
    mockGetRootDecksFromUserId()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createObjectOrFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createObjectOrFolder").performClick()
    composeTestRule.onNodeWithTag("createFolder").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createFolder").performClick()
    composeTestRule.onNodeWithTag("FolderDialog").assertIsDisplayed()
  }

  @Test
  fun createDeckDialogWorks() = runTest {
    mockGetRootDecksFromUserId()
    composeTestRule.onNodeWithTag("createObjectOrFolder").performClick()
    composeTestRule.onNodeWithTag("createDeckOrNote").performClick()

    composeTestRule.onNodeWithTag("deckTitleTextField").performTextInput("Deck Name")
    composeTestRule.onNodeWithTag("currentVisibilityOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("previousVisibility").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nextVisibility").performClick()
    composeTestRule.onNodeWithTag("deckDescriptionTextField").performTextInput("Deck Description")

    composeTestRule.onNodeWithTag("saveDeckButton").assertIsDisplayed()

    // mock get newUid
    `when`(deckRepository.getNewUid()).thenReturn("2")

    composeTestRule.onNodeWithTag("saveDeckButton").performClick()

    verify(deckRepository).updateDeck(any(), any(), any())
    verify(navigationActions).navigateTo(screen = Screen.DECK_MENU)
  }

  @Test
  fun createFolderDialogWorks() {
    mockGetRootDecksFromUserId()
    composeTestRule.onNodeWithTag("createObjectOrFolder").performClick()
    composeTestRule.onNodeWithTag("createFolder").performClick()

    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("inputFolderName").performTextInput("Folder Name")
    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsEnabled().assertIsDisplayed()

    composeTestRule.onNodeWithTag("currentVisibilityOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("previousVisibility").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nextVisibility").performClick()

    composeTestRule.onNodeWithTag("confirmFolderAction").assertIsEnabled().assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmFolderAction").performClick()
  }

  @Test
  fun dragAndDropDeckWorksCorrectly() = runTest {
    mockGetRootDecksFromUserId()
    `when`(folderRepository.getRootDeckFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    folderViewModel.getRootFoldersFromUserId("1", isDeckView = true)
    composeTestRule.onNodeWithTag("deckAndFolderList").assertIsDisplayed()

    composeTestRule.onNodeWithTag("deckCard").assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("folderCard")
        .filter(hasText("name"))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("deckCard").performTouchInput {
      down(center)
      advanceEventTime(1000)
      moveBy(Offset(-center.x * 9, 0f))
      advanceEventTime(5000)
      up()
    }
  }

  @Test
  fun openFileSystem() = runTest {
    mockGetRootDecksFromUserId()
    `when`(folderRepository.getRootDeckFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(folderList)
    }
    folderViewModel.getRootFoldersFromUserId("1", isDeckView = true)
    composeTestRule.onNodeWithTag("deckModalBottomSheet").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").performClick()
    composeTestRule.onNodeWithTag("deckModalBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteDeckBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moveDeckBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moveDeckBottomSheet").performClick()
    composeTestRule.onNodeWithTag("FileSystemPopup").assertIsDisplayed()
  }

  @Test
  fun deleteDeck() = runTest {
    mockGetRootDecksFromUserId()
    val userRootFoldersFlow =
        listOf(
            Folder(
                id = "8",
                isDeckFolder = true,
                name = "Root Folder 1",
                userId = "1",
                parentFolderId = "1",
                lastModified = Timestamp.now()),
            Folder(
                id = "9",
                isDeckFolder = true,
                name = "Root Folder 2",
                userId = "1",
                parentFolderId = "1",
                lastModified = Timestamp.now()))

    `when`(folderRepository.getRootDeckFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(userRootFoldersFlow)
    }
    folderViewModel.getRootDeckFoldersFromUserId("1")

    val subFolderList =
        listOf(
            Folder(
                id = "10",
                isDeckFolder = true,
                name = "SubFolder1",
                userId = "1",
                parentFolderId = "8",
                lastModified = Timestamp.now()),
            Folder(
                id = "11",
                isDeckFolder = true,
                name = "SubFolder2",
                userId = "1",
                parentFolderId = "8",
                lastModified = Timestamp.now()))

    `when`(folderRepository.getSubFoldersOf(eq("8"), any<(List<Folder>) -> Unit>(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
          onSuccess(subFolderList)
        }
    `when`(deckRepository.deleteDeck(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(1)
      onSuccess()
    }
    composeTestRule.onNodeWithTag("deckModalBottomSheet").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").performClick()
    composeTestRule.onNodeWithTag("deckModalBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteDeckBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteDeckBottomSheet").performClick()
    composeTestRule.onNodeWithTag("popup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("popup").performClick()
    composeTestRule.onNodeWithTag("confirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").performClick()
    verify(deckRepository).deleteDeck(any(), any(), any())
  }

  @Test
  fun navigateFileSystem() = runTest {
    mockGetRootDecksFromUserId()
    val userRootFoldersFlow =
        listOf(
            Folder(
                id = "8",
                isDeckFolder = true,
                name = "Root Folder 1",
                userId = "1",
                parentFolderId = "1",
                lastModified = Timestamp.now()),
            Folder(
                id = "9",
                isDeckFolder = true,
                name = "Root Folder 2",
                userId = "1",
                parentFolderId = "1",
                lastModified = Timestamp.now()))

    `when`(folderRepository.getRootDeckFoldersFromUserId(eq("1"), any(), any(), any())).then {
        invocation ->
      val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
      onSuccess(userRootFoldersFlow)
    }
    folderViewModel.getRootDeckFoldersFromUserId("1")

    val subFolderList =
        listOf(
            Folder(
                id = "10",
                isDeckFolder = true,
                name = "SubFolder1",
                userId = "1",
                parentFolderId = "8",
                lastModified = Timestamp.now()),
            Folder(
                id = "11",
                isDeckFolder = true,
                name = "SubFolder2",
                userId = "1",
                parentFolderId = "8",
                lastModified = Timestamp.now()))

    `when`(folderRepository.getSubFoldersOf(eq("8"), any<(List<Folder>) -> Unit>(), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Folder>) -> Unit>(1)
          onSuccess(subFolderList)
        }
    composeTestRule.onNodeWithTag("deckModalBottomSheet").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("showBottomSheetButton").performClick()
    composeTestRule.onNodeWithTag("deckModalBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moveDeckBottomSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moveDeckBottomSheet").performClick()
    composeTestRule.onNodeWithTag("FileSystemPopup").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox8").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox9").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox8").performClick()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox8").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox10").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FileSystemPopupFolderChoiceBox11").assertIsDisplayed()
  }
}
