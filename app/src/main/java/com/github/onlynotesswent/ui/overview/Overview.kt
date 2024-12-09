package com.github.onlynotesswent.ui.overview

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.MainActivity
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomDropDownMenu
import com.github.onlynotesswent.ui.common.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.common.CustomSeparatedLazyGrid
import com.github.onlynotesswent.ui.common.FileSystemPopup
import com.github.onlynotesswent.ui.common.FolderDialog
import com.github.onlynotesswent.ui.common.NoteDialog
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp

/**
 * Displays the overview screen which contains a list of publicNotes retrieved from the ViewModel.
 * If there are no publicNotes, it shows a text to the user indicating no publicNotes are available.
 * It also provides a floating action button to add a new note.
 *
 * @param navigationActions The navigationActions instance used to transition between different
 *   screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 */
@Composable
fun OverviewScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel
) {
  val userRootNotes = noteViewModel.userRootNotes.collectAsState()
  userViewModel.currentUser.collectAsState().value?.let {
    noteViewModel.getRootNotesFromUid(it.uid)
  }

  val userRootFolders = folderViewModel.userRootFolders.collectAsState()
  userViewModel.currentUser.collectAsState().value?.let {
    folderViewModel.getRootFoldersFromUserId(it.uid)
  }

  val parentFolderId = folderViewModel.parentFolderId.collectAsState()
  val context = LocalContext.current

  var expanded by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showCreateNoteDialog by remember { mutableStateOf(false) }
  var showFilePopup by remember { mutableStateOf(false) } // State to control the popup visibility

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
                  showCreateNoteDialog = { showCreateNoteDialog = it },
                  noteViewModel = noteViewModel,
                  folderViewModel = folderViewModel)
              FloatingActionButton(
                  modifier = Modifier.testTag("showFileSystemButton"),
                  onClick = { showFilePopup = true },
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary) {
                    Icon(
                        painter = painterResource(id = R.drawable.open_folder_icon),
                        contentDescription = "Open File Manager")
                  }
            }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { paddingValues ->
        OverviewScreenGrid(
            paddingValues = paddingValues,
            userRootNotes = userRootNotes,
            userRootFolders = userRootFolders,
            folderViewModel = folderViewModel,
            noteViewModel = noteViewModel,
            userViewModel = userViewModel,
            context = context,
            navigationActions = navigationActions)

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

        if (showFilePopup) {
          FileSystemPopup(onDismiss = { showFilePopup = false }, folderViewModel = folderViewModel)
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
 * Displays a floating action button that expands to show options to create a note or a folder.
 *
 * @param expandedFab The state of the floating action button. True if the button is expanded, false
 *   otherwise.
 * @param onExpandedFabChange The callback to change the state of the floating action button.
 * @param showCreateFolderDialog The callback to show the dialog to create a folder.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to display.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 */
@Composable
fun CreateItemFab(
    expandedFab: Boolean,
    onExpandedFabChange: (Boolean) -> Unit,
    showCreateFolderDialog: (Boolean) -> Unit,
    showCreateNoteDialog: (Boolean) -> Unit,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
) {
  CustomDropDownMenu(
      modifier = Modifier.testTag("createNoteOrFolder"),
      menuItems =
          listOf(
              CustomDropDownMenuItem(
                  text = { Text(stringResource(R.string.create_note)) },
                  icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.add_note_icon),
                        contentDescription = "AddNote")
                  },
                  onClick = {
                    onExpandedFabChange(false)
                    showCreateNoteDialog(true)
                    noteViewModel.selectedFolderId(null)
                  },
                  modifier = Modifier.testTag("createNote")),
              CustomDropDownMenuItem(
                  text = { Text(stringResource(R.string.create_folder)) },
                  icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.folder_create_icon),
                        contentDescription = "createFolder")
                  },
                  onClick = {
                    onExpandedFabChange(false)
                    showCreateFolderDialog(true)
                    folderViewModel.selectedParentFolderId(null)
                  },
                  modifier = Modifier.testTag("createFolder"))),
      fabIcon = { Icon(imageVector = Icons.Default.Add, contentDescription = "AddNote") },
      expanded = expandedFab,
      onFabClick = { onExpandedFabChange(true) },
      onDismissRequest = { onExpandedFabChange(false) })
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
fun OverviewScreenGrid(
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
              .padding(paddingValues)
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
          userViewModel.currentUser.value?.let { noteViewModel.getRootNotesFromUid(it.uid) }
          userViewModel.currentUser.value?.let {
            folderViewModel.getRootNoteFoldersFromUserId(it.uid)
          }
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
