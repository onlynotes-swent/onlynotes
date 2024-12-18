package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen

/**
 * Custom lazy grid that displays a list of notes and folders in a separate manner. If there are no
 * notes or folders, it displays a message to the user. The grid is scrollable.
 *
 * @param modifier The modifier for the grid.
 * @param isDeckView True if the view is for a deck, false otherwise.
 * @param notes The list of notes to be displayed.
 * @param decks The list of decks to be displayed.
 * @param folders The list of folders to be displayed.
 * @param gridModifier The modifier for the grid.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 * @param noteViewModel The ViewModel that provides the list of notes to display.
 * @param deckViewModel The ViewModel that provides the list of decks to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param paddingValues The padding values for the grid.
 * @param columnContent The content to be displayed in the column when there are no notes or
 *   folders.
 */
@Composable
fun CustomSeparatedLazyGrid(
    modifier: Modifier,
    isDeckView: Boolean = false,
    notes: State<List<Note>>? = null,
    decks: State<List<Deck>>? = null,
    folders: State<List<Folder>>,
    gridModifier: Modifier,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel? = null,
    deckViewModel: DeckViewModel? = null,
    userViewModel: UserViewModel,
    navigationActions: NavigationActions,
    paddingValues: PaddingValues,
    columnContent: @Composable (ColumnScope.() -> Unit)
) {
  val sortedFolders = remember(folders.value) { folders.value.sortedBy { it.name } }
  val sortedNotes = remember(notes?.value) { notes?.value?.sortedBy { it.title } ?: emptyList() }
  val sortedDecks = remember(decks?.value) { decks?.value?.sortedBy { it.name } ?: emptyList() }

  Box(modifier = modifier) {
    if ((sortedNotes.isNotEmpty() && !isDeckView) ||
        (sortedDecks.isNotEmpty() && isDeckView) ||
        sortedFolders.isNotEmpty()) {
      LazyVerticalGrid(
          columns = GridCells.Fixed(6),
          contentPadding = PaddingValues(vertical = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = gridModifier) {
            // If the current user is not the owner of the folders (except for search screen),
            // only display the notes
            if ((sortedFolders.isNotEmpty() &&
                sortedFolders[0].isOwner(userViewModel.currentUser.value!!.uid)) ||
                navigationActions.currentRoute() == Screen.SEARCH) {
              items(sortedFolders, key = { it.id }, span = { GridItemSpan(2) }) { folder ->
                FolderItem(
                    folder = folder,
                    navigationActions = navigationActions,
                    isDeckView = isDeckView,
                    noteViewModel = noteViewModel,
                    deckViewModel = deckViewModel,
                    folderViewModel = folderViewModel) {
                      folderViewModel.selectedParentFolderId(folder.parentFolderId)
                      navigationActions.navigateTo(
                          Screen.FOLDER_CONTENTS.replace(
                              oldValue = "{folderId}", newValue = folder.id))
                    }
              }
              // Spacer item to create space between folders and notes
              item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(50.dp))
              }
            }

            if (isDeckView) {
              items(sortedDecks, key = { it.id }, span = { GridItemSpan(3) }) { deck ->
                DeckItem(deck = deck, deckViewModel = deckViewModel!!) {
                  deckViewModel.selectDeck(deck)
                  navigationActions.navigateTo(Screen.DECK_MENU)
                }
              }
            } else {
              items(sortedNotes, key = { it.id }, span = { GridItemSpan(3) }) { note ->
                NoteItem(
                    note = note,
                    currentUser = userViewModel.currentUser.collectAsState(),
                    noteViewModel = noteViewModel!!,
                    folderViewModel = folderViewModel,
                    navigationActions = navigationActions) {
                      noteViewModel.selectedNote(note)
                      navigationActions.navigateTo(Screen.EDIT_NOTE)
                    }
              }
            }
          }
    } else {
      Column(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally) {
            columnContent()
          }
    }
  }
}
