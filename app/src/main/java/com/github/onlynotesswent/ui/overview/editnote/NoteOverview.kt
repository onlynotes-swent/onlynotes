package com.github.onlynotesswent.ui.overview.editnote

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomSeparatedLazyGrid
import com.github.onlynotesswent.ui.navigation.NavigationActions

@Composable
fun NoteOverviewScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel,
    paddingValues: PaddingValues
) {

  val currentUser = userViewModel.currentUser.collectAsState()

  val userRootNotes = noteViewModel.userRootNotes.collectAsState()
  currentUser.value?.let { noteViewModel.getRootNotesFrom(it.uid) }

  val userRootFolders = folderViewModel.userRootFolders.collectAsState()
  userViewModel.currentUser.collectAsState().value?.let {
    folderViewModel.getRootFoldersFromUid(it.uid)
  }

  val context = LocalContext.current
  NoteOverviewScreenGrid(
      paddingValues = paddingValues,
      userRootNotes = userRootNotes,
      userRootFolders = userRootFolders,
      folderViewModel = folderViewModel,
      noteViewModel = noteViewModel,
      userViewModel = userViewModel,
      context = context,
      navigationActions = navigationActions)
}

/**
 * Displays the overview screen in a grid layout. If there are no notes or folders, it shows a text
 * to the user indicating that there are no notes or folders. It also provides a button to refresh
 * the list of notes and folders.
 *
 * @param paddingValues The padding values to apply to the grid layout.
 * @param userRootNotes The list of notes to display.
 * @param userRootFolders The list of folders to display.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param context The context of the app.
 * @param navigationActions The navigation view model used to transition between different screens.
 */
@Composable
fun NoteOverviewScreenGrid(
    paddingValues: PaddingValues,
    userRootNotes: State<List<Note>>,
    userRootFolders: State<List<Folder>>,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    context: Context,
    navigationActions: NavigationActions
) {
  CustomSeparatedLazyGrid(
      modifier = Modifier.fillMaxSize(),
      notes = userRootNotes,
      folders = userRootFolders,
      gridModifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 20.dp)
              .padding(bottom = 5.dp)
              .testTag("noteAndFolderList"),
      folderViewModel = folderViewModel,
      noteViewModel = noteViewModel,
      userViewModel = userViewModel,
      context = context,
      navigationActions = navigationActions,
      paddingValues = paddingValues,
      columnContent = {
        Text(
            modifier = Modifier.testTag("emptyNoteAndFolderPrompt"),
            text = stringResource(R.string.you_have_no_notes_or_folders_yet),
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(50.dp))
        RefreshButton {
          userViewModel.currentUser.value?.let { noteViewModel.getRootNotesFrom(it.uid) }
          userViewModel.currentUser.value?.let { folderViewModel.getRootFoldersFromUid(it.uid) }
        }
        Spacer(modifier = Modifier.height(20.dp))
      })
}

/**
 * A composable function that displays a refresh button.
 *
 * @param onClick A lambda function to be invoked when the button is clicked.
 */
@Composable
fun RefreshButton(onClick: () -> Unit) {
  ElevatedButton(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.testTag("refreshButton")) {
        Text(stringResource(R.string.refresh), color = MaterialTheme.colorScheme.onSurface)
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = MaterialTheme.colorScheme.onSurface)
      }
}
