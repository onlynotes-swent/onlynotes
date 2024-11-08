package com.github.onlynotesswent.ui.overview

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations

/**
 * Screen that displays the content of a folder.
 *
 * @param navigationActions actions that can be performed in the app
 * @param folderViewModel view model for the folder
 * @param noteViewModel view model for the note
 */
@OptIn(ExperimentalMaterial3Api::class)
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

  val userFolderSubFolders = folderViewModel.userSubFolders.collectAsState()
  currentUser.let { folderViewModel.getSubFoldersOf(folder.value?.id ?: "") }

  val parentFolderId = folderViewModel.parentFolderId.collectAsState()

  var expanded by remember { mutableStateOf(false) }
  var showRenameDialog by remember { mutableStateOf(false) }
  var showCreateDialog by remember { mutableStateOf(false) }
  var updatedName by remember { mutableStateOf(folder.value!!.name) }
  var expandedFolder by remember { mutableStateOf(false) }

  if (currentUser.value == null) {
    // If the user is null, display an error message
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text("User  not found ...")
        }
    Log.e("FolderContentScreen", "User not found")
  } else {
    Scaffold(
        modifier = Modifier.testTag("folderContentScreen"),
        topBar = {
          TopAppBar(
              colors =
              TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.surface),
              title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      Spacer(modifier = Modifier.weight(2f))
                      Text(updatedName,
                          color = MaterialTheme.colorScheme.onSurface,
                          modifier = Modifier.testTag("folderContentTitle"))
                      Spacer(modifier = Modifier.weight(2f))
                    }
              },
              navigationIcon = {
                IconButton(
                    onClick = {
                      navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                    }, // For now we always return to overview screen, go back does not work
                    // properly
                    modifier = Modifier.testTag("clearButton")) {
                      Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
              },
              actions = {
                CustomDropDownMenu(
                    modifier = Modifier.testTag("folderSettingsButton"),
                    menuItems =
                        listOf(
                            CustomDropDownMenuItem(
                                text = { Text("Rename folder") },
                                onClick = {
                                  expanded = false
                                  showRenameDialog = true
                                },
                                modifier = Modifier.testTag("renameFolderButton")),
                            CustomDropDownMenuItem(
                                text = { Text("Delete folder") },
                                onClick = {
                                  expanded = false
                                  folderViewModel.deleteFolderById(
                                      folder.value!!.id, folder.value!!.userId)
                                  // TODO for now we just delete the folder directly
                                  // later on we need to figure out how to recursively delete all
                                  // elements of a
                                  // folder(folders and notes)
                                  navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                                },
                                modifier = Modifier.testTag("deleteFolderButton"))),
                    fabIcon = {
                      Icon(imageVector = Icons.Default.MoreVert, contentDescription = "settings")
                    },
                    expanded = expanded,
                    onFabClick = { expanded = true },
                    onDismissRequest = { expanded = false })
              })
        },
        floatingActionButton = {
          CustomDropDownMenu(
              modifier = Modifier.testTag("createSubNoteOrSubFolder"),
              menuItems =
                  listOf(
                      CustomDropDownMenuItem(
                          text = { Text("Create note") },
                          onClick = {
                            expandedFolder = false
                            noteViewModel.selectedFolderId(folder.value!!.id)
                            navigationActions.navigateTo(Screen.ADD_NOTE)
                          },
                          modifier = Modifier.testTag("createNote")),
                      CustomDropDownMenuItem(
                          text = { Text("Create folder") },
                          onClick = {
                            expandedFolder = false
                            showCreateDialog = true
                            folderViewModel.selectedParentFolderId(folder.value!!.id)
                          },
                          modifier = Modifier.testTag("createFolder"))),
              fabIcon = { Icon(imageVector = Icons.Default.Add, contentDescription = "AddNote") },
              expanded = expandedFolder,
              onFabClick = { expandedFolder = true },
              onDismissRequest = { expandedFolder = false })
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
        }) { paddingValues ->
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
                Text(
                    modifier = Modifier.testTag("emptyFolderPrompt"),
                    text = "This folder is empty.")
              })
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
        }
  }
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
      title = { Text("Rename folder") },
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
