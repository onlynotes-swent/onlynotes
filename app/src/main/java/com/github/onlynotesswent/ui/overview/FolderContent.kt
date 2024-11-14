package com.github.onlynotesswent.ui.overview

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.CreateFolderDialog
import com.github.onlynotesswent.ui.CustomDropDownMenu
import com.github.onlynotesswent.ui.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.CustomLazyGrid
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations

/**
 * Screen that displays the content of a folder.
 *
 * @param navigationActions actions that can be performed in the app
 * @param folderViewModel view model for the folder
 * @param noteViewModel view model for the note
 * @param userViewModel view model for the user
 */
@Composable
fun FolderContentScreen(
    navigationActions: NavigationActions,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel
) {
  val folder = folderViewModel.selectedFolder.collectAsState()
  val currentUser = userViewModel.currentUser.collectAsState()

  val userFolderNotes = noteViewModel.folderNotes.collectAsState()
  currentUser.let { noteViewModel.getNotesFromFolder(folder.value?.id ?: "") }

  val userFolderSubFolders = folderViewModel.folderSubFolders.collectAsState()
  currentUser.let { folderViewModel.getSubFoldersOf(folder.value?.id ?: "") }

  val parentFolderId = folderViewModel.parentFolderId.collectAsState()

  var expanded by remember { mutableStateOf(false) }
  var showRenameDialog by remember { mutableStateOf(false) }
  var showCreateDialog by remember { mutableStateOf(false) }
  var updatedName by remember { mutableStateOf(folder.value!!.name) }
  var expandedFolder by remember { mutableStateOf(false) }

  if (currentUser.value == null) {
    UserNotFoundFolderContentScreen()
  } else {
    Scaffold(
        modifier = Modifier.testTag("folderContentScreen"),
        topBar = {
          FolderContentTopBar(
              folder = folder.value,
              updatedName = updatedName,
              onUpdateName = { updatedName = it },
              navigationActions = navigationActions,
              folderViewModel = folderViewModel,
              noteViewModel = noteViewModel,
              userFolderSubFolders = userFolderSubFolders,
              userFolderNotes = userFolderNotes,
              expanded = expanded,
              onExpandedChange = { expanded = it },
              showRenameDialog = { showRenameDialog = it })
        },
        floatingActionButton = {
          CreateSubItemFab(
              expandedFolder = expandedFolder,
              onExpandedFolderChange = { expandedFolder = it },
              showCreateDialog = { showCreateDialog = it },
              noteViewModel = noteViewModel,
              folderViewModel = folderViewModel,
              folder = folder.value,
              navigationActions = navigationActions)
        }) { paddingValues ->
          FolderContentScreenGrid(
              paddingValues = paddingValues,
              userFolderNotes = userFolderNotes,
              userFolderSubFolders = userFolderSubFolders,
              folderViewModel = folderViewModel,
              noteViewModel = noteViewModel,
              navigationActions = navigationActions)
          // Logic to show the dialog to rename a folder
          if (showRenameDialog) {
            RenameFolderDialog(
                currentName = updatedName,
                onDismiss = { showRenameDialog = false },
                onConfirm = { newName ->
                  folderViewModel.updateFolder(
                      Folder(
                          id = folder.value!!.id,
                          name = newName,
                          userId = folder.value!!.userId,
                          parentFolderId = folder.value!!.parentFolderId),
                      folder.value!!.userId)
                  updatedName = newName
                  showRenameDialog = false
                })
          }
          // Logic to show the dialog to create a folder
          if (showCreateDialog) {
            CreateFolderDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name ->
                  folderViewModel.addFolder(
                      Folder(
                          id = folderViewModel.getNewFolderId(),
                          name = name,
                          userId = currentUser.value!!.uid,
                          parentFolderId = parentFolderId.value),
                      userViewModel.currentUser.value!!.uid)
                  showCreateDialog = false
                  if (parentFolderId.value != null) {
                    navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
                  } else {
                    navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                  }
                })
          }
        }
  }
}

/** Screen that displays when a user is not found. */
@Composable
fun UserNotFoundFolderContentScreen() {
  Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text("User not found ...")
      }
  Log.e("FolderContentScreen", "User not found")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderContentTopBar(
    folder: Folder?,
    updatedName: String,
    onUpdateName: (String) -> Unit,
    navigationActions: NavigationActions,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel,
    userFolderSubFolders: State<List<Folder>>,
    userFolderNotes: State<List<Note>>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    showRenameDialog: (Boolean) -> Unit,
) {
  TopAppBar(
      colors =
          TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
      title = {
        Row(
            modifier = Modifier.fillMaxWidth().testTag("folderContentTitle"),
            verticalAlignment = Alignment.CenterVertically) {
              // Update the updatedName state whenever the folder state changes to display it in the
              // title
              LaunchedEffect(folder?.name) { onUpdateName(folder?.name ?: "Folder name not found") }
              Spacer(modifier = Modifier.weight(2f))
              Icon(
                  painter = painterResource(id = R.drawable.open_folder_icon),
                  contentDescription = "Folder icon")
              Spacer(modifier = Modifier.weight(0.25f))
              Text(updatedName, color = MaterialTheme.colorScheme.onSurface)
              Spacer(modifier = Modifier.weight(2f))
            }
      },
      navigationIcon = {
        IconButton(
            onClick = {
              var previousFolderId = navigationActions.popFromScreenNavigationStack()
              // If we pop from the stack the current folder, we call pop twice to get the
              // previous folder
              if (previousFolderId != null && previousFolderId == folder?.id) {
                // Set the selected folder state to the previous folder
                previousFolderId = navigationActions.popFromScreenNavigationStack()
                if (previousFolderId != null) {
                  folderViewModel.getFolderById(previousFolderId)
                } else {
                  navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                }
              } else if (previousFolderId != null) {
                folderViewModel.getFolderById(previousFolderId)
              } else {
                navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
              }
            },
            modifier = Modifier.testTag("goBackButton")) {
              Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
      },
      actions = {
        CustomDropDownMenu(
            modifier = Modifier.testTag("folderSettingsButton"),
            menuItems =
                listOf(
                    CustomDropDownMenuItem(
                        text = { Text("Rename folder") },
                        icon = {
                          Icon(
                              imageVector = Icons.Default.Edit, contentDescription = "RenameFolder")
                        },
                        onClick = {
                          onExpandedChange(false)
                          showRenameDialog(true)
                        },
                        modifier = Modifier.testTag("renameFolderButton")),
                    CustomDropDownMenuItem(
                        text = { Text("Delete folder") },
                        icon = {
                          Icon(
                              painter = painterResource(id = R.drawable.folder_delete_icon),
                              contentDescription = "DeleteFolder")
                        },
                        onClick = {
                          onExpandedChange(false)
                          // Clear the folder navigation stack as we go back to overview
                          // screen
                          navigationActions.clearScreenNavigationStack()
                          folderViewModel.deleteFolderById(folder!!.id, folder.userId)

                          handleSubFoldersAndNotes(
                              folder = folder,
                              userFolderSubFolders = userFolderSubFolders.value,
                              userFolderNotes = userFolderNotes.value,
                              folderViewModel = folderViewModel,
                              noteViewModel = noteViewModel)
                          navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                          // TODO for now we just delete the folder directly and set the
                          // folderId field of sub elements to null, later on we will
                          // implement a recursive delete to delete all elements of a folder
                          // (folders and notes)
                        },
                        modifier = Modifier.testTag("deleteFolderButton"))),
            fabIcon = {
              Icon(imageVector = Icons.Default.MoreVert, contentDescription = "settings")
            },
            expanded = expanded,
            onFabClick = { onExpandedChange(true) },
            onDismissRequest = { onExpandedChange(false) })
      })
}

/**
 * Handle sub folders and notes when a folder is deleted.
 *
 * @param folder the folder that is deleted
 * @param userFolderSubFolders the sub folders of the user
 * @param userFolderNotes the notes of the user
 * @param folderViewModel the view model for the folder
 * @param noteViewModel the view model for the note
 */
fun handleSubFoldersAndNotes(
    folder: Folder,
    userFolderSubFolders: List<Folder>,
    userFolderNotes: List<Note>,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel
) {
  // If folder is subfolder, set parent Id and folder Id of sub
  // elements to parent folder id
  if (folder.parentFolderId != null) {
    userFolderSubFolders.forEach { subFolder ->
      folderViewModel.updateFolder(
          subFolder.copy(parentFolderId = folder.parentFolderId), subFolder.userId)
    }
    userFolderNotes.forEach { note ->
      noteViewModel.updateNote(note.copy(folderId = folder.parentFolderId), note.userId)
    }
  } else {
    // folder is root folder
    userFolderSubFolders.forEach { subFolder ->
      folderViewModel.updateFolder(subFolder.copy(parentFolderId = null), subFolder.userId)
    }
    userFolderNotes.forEach { note ->
      noteViewModel.updateNote(note.copy(folderId = null), note.userId)
    }
  }
}

/**
 * Create a FAB to create a sub item (note or folder) in the current folder.
 *
 * @param expandedFolder whether the folder is expanded
 * @param onExpandedFolderChange function to change the expanded state
 * @param showCreateDialog function to show the create dialog
 * @param noteViewModel the view model for the note
 * @param folderViewModel the view model for the folder
 * @param folder the current folder
 * @param navigationActions actions that can be performed in the app
 */
@Composable
fun CreateSubItemFab(
    expandedFolder: Boolean,
    onExpandedFolderChange: (Boolean) -> Unit,
    showCreateDialog: (Boolean) -> Unit,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
    folder: Folder?,
    navigationActions: NavigationActions
) {
  CustomDropDownMenu(
      modifier = Modifier.testTag("createSubNoteOrSubFolder"),
      menuItems =
          listOf(
              CustomDropDownMenuItem(
                  text = { Text("Create note") },
                  icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.add_note_icon),
                        contentDescription = "AddNote")
                  },
                  onClick = {
                    onExpandedFolderChange(false)
                    noteViewModel.selectedFolderId(folder!!.id)
                    navigationActions.navigateTo(Screen.ADD_NOTE)
                  },
                  modifier = Modifier.testTag("createNote")),
              CustomDropDownMenuItem(
                  text = { Text("Create folder") },
                  icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.folder_create_icon),
                        contentDescription = "createFolder")
                  },
                  onClick = {
                    onExpandedFolderChange(false)
                    showCreateDialog(true)
                    folderViewModel.selectedParentFolderId(folder!!.id)
                  },
                  modifier = Modifier.testTag("createFolder"))),
      fabIcon = { Icon(imageVector = Icons.Default.Add, contentDescription = "AddNote") },
      expanded = expandedFolder,
      onFabClick = { onExpandedFolderChange(true) },
      onDismissRequest = { onExpandedFolderChange(false) })
}

/**
 * Display the content of a folder using the custom lazy grid.
 *
 * @param paddingValues the padding values
 * @param userFolderNotes the notes in the folder
 * @param userFolderSubFolders the sub folders in the folder
 * @param folderViewModel the view model for the folder
 * @param noteViewModel the view model for the note
 * @param navigationActions actions that can be performed in the app
 */
@Composable
fun FolderContentScreenGrid(
    paddingValues: PaddingValues,
    userFolderNotes: State<List<Note>>,
    userFolderSubFolders: State<List<Folder>>,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel,
    navigationActions: NavigationActions
) {
  CustomLazyGrid(
      modifier = Modifier.fillMaxSize().padding(paddingValues),
      notes = userFolderNotes,
      folders = userFolderSubFolders,
      gridModifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp)
              .padding(paddingValues)
              .testTag("noteAndFolderList"),
      folderViewModel = folderViewModel,
      noteViewModel = noteViewModel,
      navigationActions = navigationActions,
      paddingValues = paddingValues,
      columnContent = {
        Text(modifier = Modifier.testTag("emptyFolderPrompt"), text = "This folder is empty.")
      })
}

/**
 * Dialog that allows the user to rename a folder.
 *
 * @param currentName the current name of the folder
 * @param onDismiss callback to be invoked when the dialog is dismissed
 * @param onConfirm callback to be invoked when the user confirms the new name
 */
@Composable
fun RenameFolderDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {

  var newName by remember { mutableStateOf(currentName) }

  AlertDialog(
      modifier = Modifier.testTag("renameFolderDialog"),
      onDismissRequest = onDismiss,
      title = {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Text("Rename folder")
        }
      },
      text = {
        OutlinedTextField(
            value = newName, onValueChange = { newName = it }, label = { Text("New Folder Name") })
      },
      confirmButton = {
        Button(
            enabled = newName.isNotEmpty(),
            onClick = { onConfirm(newName) },
            modifier = Modifier.testTag("confirmRenameButton")) {
              Text("Confirm")
            }
      },
      dismissButton = {
        Button(onClick = onDismiss, modifier = Modifier.testTag("dismissRenameButton")) {
          Text("Cancel")
        }
      })
}
