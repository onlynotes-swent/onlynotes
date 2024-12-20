package com.github.onlynotesswent.ui.overview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.FolderDialog
import com.github.onlynotesswent.ui.common.NoteDialog
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.NotesToFlashcard
import com.google.firebase.Timestamp

/**
 * Displays the overview screen which contains a list of the user's notes and folders. The user can
 * create new notes and folders from this screen. If there are no notes or folders, it shows a text
 * to the user indicating that there are no notes or folders.
 *
 * @param navigationActions The navigationActions instance used to transition between different
 *   screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 */
@Composable
fun NoteOverviewScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel,
    notesToFlashcard: NotesToFlashcard
) {
  val userRootNotes = noteViewModel.userRootNotes.collectAsState()
  val userRootFolders = folderViewModel.userRootFolders.collectAsState()

  val parentFolderId = folderViewModel.parentFolderId.collectAsState()
  val context = LocalContext.current

  var expanded by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showCreateNoteDialog by remember { mutableStateOf(false) }

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
                  showCreateItemDialog = { showCreateNoteDialog = it })
            }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { paddingValues ->
        NoteOverviewScreenGrid(
            paddingValues = paddingValues,
            userNotes = userRootNotes,
            userFolders = userRootFolders,
            folderViewModel = folderViewModel,
            noteViewModel = noteViewModel,
            userViewModel = userViewModel,
            navigationActions = navigationActions,
            notesToFlashcard = notesToFlashcard)

        if (showCreateNoteDialog) {
          NoteDialog(
              onDismiss = { showCreateNoteDialog = false },
              onConfirm = { newName, visibility ->
                val note =
                    Note(
                        id = noteViewModel.getNewUid(),
                        title = newName,
                        date = Timestamp.now(),
                        lastModified = Timestamp.now(),
                        visibility = visibility,
                        userId = userViewModel.currentUser.value!!.uid,
                        folderId = parentFolderId.value)
                noteViewModel.addNote(note)
                noteViewModel.selectedNote(note)
                showCreateNoteDialog = false
                navigationActions.navigateTo(Screen.EDIT_NOTE)
              },
              action = stringResource(R.string.create))
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
