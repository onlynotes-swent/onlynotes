package com.github.onlynotesswent.ui.common

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen

/**
 * Custom lazy grid that displays a list of notes and folders. If there are no notes or folders, it
 * displays a message to the user. The grid is scrollable.
 *
 * @param modifier The modifier for the grid.
 * @param notes The list of notes to be displayed.
 * @param folders The list of folders to be displayed.
 * @param gridModifier The modifier for the grid.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 * @param noteViewModel The ViewModel that provides the list of notes to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param context The context used to display the dialog.
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param paddingValues The padding values for the grid.
 * @param columnContent The content to be displayed in the column when there are no notes or
 *   folders.
 */
@Composable
fun CustomLazyGrid(
    modifier: Modifier,
    notes: State<List<Note>>,
    folders: State<List<Folder>>,
    gridModifier: Modifier,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    context: Context,
    navigationActions: NavigationActions,
    paddingValues: PaddingValues,
    columnContent: @Composable (ColumnScope.() -> Unit)
) {
  Box(modifier = modifier) {
    if (notes.value.isNotEmpty() || folders.value.isNotEmpty()) {
      LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 100.dp),
          contentPadding = PaddingValues(vertical = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = gridModifier) {
            items(folders.value.size) { index ->
              FolderItem(folder = folders.value[index]) {
                folderViewModel.selectedFolder(folders.value[index])

                if (folders.value[index].parentFolderId == null) {
                  // Don't add to the screen navigation stack as we are at the root folder
                  navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
                } else {
                  val poppedId = navigationActions.popFromScreenNavigationStack()
                  if (poppedId == Screen.SEARCH) {
                    // If we come from search, don't push the folderId to the stack
                    navigationActions.pushToScreenNavigationStack(poppedId)
                  } else {
                    if (poppedId != null) {
                      navigationActions.pushToScreenNavigationStack(poppedId)
                    }
                    // Add the previously visited folder Id (parent) to the screen navigation stack
                    navigationActions.pushToScreenNavigationStack(
                        folders.value[index].parentFolderId!!)
                  }
                  navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
                }
              }
            }
            items(notes.value.size) { index ->
              NoteItem(
                  note = notes.value[index],
                  currentUser = userViewModel.currentUser.collectAsState(),
                  context = context,
                  noteViewModel = noteViewModel,
                  showDialog = false,
                  navigationActions = navigationActions) {
                    noteViewModel.selectedNote(notes.value[index])
                    navigationActions.navigateTo(Screen.EDIT_NOTE)
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
