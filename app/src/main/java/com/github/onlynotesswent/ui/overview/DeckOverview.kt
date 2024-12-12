package com.github.onlynotesswent.ui.overview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.MainActivity
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomSeparatedLazyGrid
import com.github.onlynotesswent.ui.common.FolderDialog
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp

/**
 * Displays the overview screen which contains a list of the user's decks and folders. The user can
 * create new decks and folders from this screen. If there are no decks or folders, it shows a text
 * to the user indicating that there are no decks or folders.
 *
 * @param navigationActions The navigationActions instance used to transition between different
 *   screens.
 * @param deckViewModel The ViewModel that provides the list of decks to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 */
@Composable
fun DeckOverviewScreen(
    navigationActions: NavigationActions,
    deckViewModel: DeckViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel
) {
  val userRootDecks = deckViewModel.userRootDecks.collectAsState()
  userViewModel.currentUser.collectAsState().value?.let {
    deckViewModel.getRootDecksFromUserId(it.uid)
  }

  val userRootFolders = folderViewModel.userRootFolders.collectAsState()
  userViewModel.currentUser.collectAsState().value?.let {
    folderViewModel.getRootFoldersFromUid(it.uid)
  }

  val parentFolderId = folderViewModel.parentFolderId.collectAsState()
  val context = LocalContext.current

  var expanded by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showCreateDeckDialog by remember { mutableStateOf(false) }

  // Handle back press
  BackHandler {
    // Move the app to background
    (context as MainActivity).moveTaskToBack(true)
  }

  Scaffold(
      modifier = Modifier.testTag("overviewScreen"),
      floatingActionButton = {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp), // Space between FABs
            horizontalAlignment = Alignment.End) {
              CreateItemFab(
                  expandedFab = expanded,
                  onExpandedFabChange = { expanded = it },
                  showCreateFolderDialog = { showCreateFolderDialog = it },
                  showCreateItemDialog = { showCreateDeckDialog = it },
                  isDeckView = true)
            }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { paddingValues ->
        DeckOverviewScreenGrid(
            paddingValues = paddingValues,
            userRootDecks = userRootDecks,
            userRootFolders = userRootFolders,
            folderViewModel = folderViewModel,
            deckViewModel = deckViewModel,
            userViewModel = userViewModel,
            navigationActions = navigationActions)

        if (showCreateDeckDialog) {
          Text(stringResource(R.string.create))
        }

        // Logic to show the dialog to create a folder
        if (showCreateFolderDialog) {
          FolderDialog(
              onDismiss = { showCreateFolderDialog = false },
              onConfirm = { newName, visibility ->
                val folderId = folderViewModel.getNewFolderId()
                folderViewModel.addFolder(
                    Folder(
                        id = folderId,
                        name = newName,
                        userId = userViewModel.currentUser.value!!.uid,
                        parentFolderId = parentFolderId.value,
                        visibility = visibility,
                        lastModified = Timestamp.now()))

                showCreateFolderDialog = false
                navigationActions.navigateTo(
                    Screen.FOLDER_CONTENTS.replace(oldValue = "{folderId}", newValue = folderId))
              },
              action = stringResource(R.string.create))
        }
      }
}

/**
 * Displays the overview screen in a grid layout. If there are no decks or folders, it shows a text
 * to the user indicating that there are no decks or folders.
 *
 * @param paddingValues The padding values to apply to the grid layout.
 * @param userRootDecks The list of decks to display.
 * @param userRootFolders The list of folders to display.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 * @param deckViewModel The ViewModel that provides the list of decks to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param navigationActions The navigation view model used to transition between different screens.
 */
@Composable
fun DeckOverviewScreenGrid(
    paddingValues: PaddingValues,
    userRootDecks: State<List<Deck>>,
    userRootFolders: State<List<Folder>>,
    folderViewModel: FolderViewModel,
    deckViewModel: DeckViewModel,
    userViewModel: UserViewModel,
    navigationActions: NavigationActions
) {
  CustomSeparatedLazyGrid(
      modifier = Modifier.fillMaxSize(),
      isDeckView = true,
      decks = userRootDecks,
      folders = userRootFolders,
      gridModifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 20.dp)
              .padding(paddingValues)
              .testTag("deckAndFolderList"),
      folderViewModel = folderViewModel,
      deckViewModel = deckViewModel,
      userViewModel = userViewModel,
      navigationActions = navigationActions,
      paddingValues = paddingValues,
      columnContent = {
        Text(
            modifier = Modifier.testTag("emptyDeckAndFolderPrompt"),
            text = stringResource(R.string.you_have_no_decks_or_folders_yet),
            color = MaterialTheme.colorScheme.onBackground)
      })
}
