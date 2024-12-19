package com.github.onlynotesswent.ui.overview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomSeparatedLazyGrid
import com.github.onlynotesswent.ui.navigation.NavigationActions

/**
 * Displays the overview screen in a grid layout. If there are no decks or folders, it shows a text
 * to the user indicating that there are no decks or folders.
 *
 * @param paddingValues The padding values to apply to the grid layout.
 * @param userDecks The list of decks to display.
 * @param userFolders The list of folders to display.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 * @param deckViewModel The ViewModel that provides the list of decks to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param navigationActions The navigation view model used to transition between different screens.
 */
@Composable
fun DeckOverviewScreenGrid(
    paddingValues: PaddingValues,
    userDecks: State<List<Deck>>,
    userFolders: State<List<Folder>>,
    folderViewModel: FolderViewModel,
    deckViewModel: DeckViewModel,
    userViewModel: UserViewModel,
    navigationActions: NavigationActions
) {
  CustomSeparatedLazyGrid(
      modifier = Modifier.fillMaxSize(),
      isDeckView = true,
      decks = userDecks,
      folders = userFolders,
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
