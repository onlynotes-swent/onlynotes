package com.github.onlynotesswent.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
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
) {
    val folder = folderViewModel.selectedFolder.collectAsState()
    val subFolders = folderViewModel.parentSubFolders.collectAsState()
    val folderNotes = noteViewModel.folderNotes.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var updatedName by remember { mutableStateOf(folder.value!!.name) }
    var expandedFolder by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.testTag("folderContentScreen"),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB3E5FC)),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.weight(2f))
                        Text(updatedName, Modifier.testTag("folderContentTitle"))
                        Spacer(modifier = Modifier.weight(2f))
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.navigateTo(TopLevelDestinations.OVERVIEW) },
                        modifier = Modifier.testTag("clearButton")) {
                             Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                             )
                    }
                },
                actions = {
                    Box {
                        FloatingActionButton(
                            onClick = { expanded = true },
                            modifier = Modifier.testTag("folderSettingsButton")
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "settings")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false}
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename Folder") },
                                onClick = {
                                    expanded = false
                                    showRenameDialog = true
                                },
                                modifier = Modifier.testTag("renameFolder")

                            )
                            DropdownMenuItem(
                                text = { Text("delete Folder") },
                                onClick = {
                                    expanded = false
                                    folderViewModel.deleteFolderById(folder.value!!.id, folder.value!!.userId)
                                    // for now we just delete the folder directly
                                    // later on we need to figure out how to recursively delete all elements of a folder(folders and notes)
                                    navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                                },
                                modifier = Modifier.testTag("deleteFolder")
                            )
                        }
                    }
                })
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { expandedFolder = true },
                    modifier = Modifier.testTag("createSubNoteOrSubFolder")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "AddNote")
                }
                DropdownMenu(
                    expanded = expandedFolder,
                    onDismissRequest = { expandedFolder = false}
                ) {
                    DropdownMenuItem(
                        text = { Text("Create Note") },
                        onClick = {
                            expanded = false
                            noteViewModel.selectedFolderId(folder.value!!.id)
                            navigationActions.navigateTo(Screen.ADD_NOTE)

                        },
                        modifier = Modifier.testTag("createNote")

                    )
                    DropdownMenuItem(
                        text = { Text("Create Folder") },
                        onClick = {
                            expandedFolder = false
                            folderViewModel.selectedParentFolderId(folder.value!!.id)
                            navigationActions.navigateTo(Screen.ADD_FOLDER)

                        },
                        modifier = Modifier.testTag("createFolder")
                    )
                }
            }



        },
        content = { paddingValues ->

            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (folderNotes.value.isNotEmpty() || subFolders.value.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        contentPadding = PaddingValues(vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(paddingValues)
                            .testTag("noteList")
                    ) {

                        items(subFolders.value.size) { index ->
                            FolderItem(folder = subFolders.value[index]) {
                                folderViewModel.selectedFolder(subFolders.value[index])
                                navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
                            }
                        }

                        items(folderNotes.value.size) { index ->
                            NoteItem(note = folderNotes.value[index]) {
                                noteViewModel.selectedNote(folderNotes.value[index])
                                navigationActions.navigateTo(Screen.EDIT_NOTE)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier.testTag("emptyFolderPrompt"),
                            text = "This folder is empty.")
                    }
                }
            }

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
                                parentFolderId = folder.value!!.parentFolderId
                            ),
                            folder.value!!.userId)
                        updatedName = newName
                        showRenameDialog = false
                    }
                )
            }
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
        onDismissRequest = onDismiss,
        title = { Text("Rename Folder") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Folder Name") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(newName) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}
