package com.github.onlynotesswent.ui.overview

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomDropDownMenu
import com.github.onlynotesswent.ui.common.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.common.CustomLazyGrid
import com.github.onlynotesswent.ui.common.FolderDialog
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
  val context = LocalContext.current

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
              currentUser = currentUser,
              context = context,
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
              currentUser = currentUser,
              context = context,
              folder = folder.value,
              navigationActions = navigationActions)
        }) { paddingValues ->
          FolderContentScreenGrid(
              paddingValues = paddingValues,
              userFolderNotes = userFolderNotes,
              userFolderSubFolders = userFolderSubFolders,
              folderViewModel = folderViewModel,
              noteViewModel = noteViewModel,
              userViewModel = userViewModel,
              context = context,
              navigationActions = navigationActions)
          // Logic to show the dialog to rename a folder
          if (showRenameDialog) {
            FolderDialog(
                onDismiss = { showRenameDialog = false },
                onConfirm = { name, vis ->
                  if (currentUser.value!!.uid == folder.value?.userId) {
                    folderViewModel.updateFolder(
                        Folder(
                            id = folder.value!!.id,
                            name = name,
                            userId = folder.value!!.userId,
                            parentFolderId = folder.value!!.parentFolderId,
                            visibility = vis),
                        folder.value!!.userId)
                    updatedName = name
                  } else {
                    Toast.makeText(
                            context, "You are not the owner of this folder", Toast.LENGTH_SHORT)
                        .show()
                  }
                  showRenameDialog = false
                },
                action = "Rename",
                oldName = updatedName,
                oldVis = folder.value!!.visibility)
          }
          // Logic to show the dialog to create a folder
          if (showCreateDialog) {
            FolderDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, visibility ->
                  if (currentUser.value!!.uid == folder.value?.userId) {
                    val folderId = folderViewModel.getNewFolderId()
                    folderViewModel.addFolder(
                        Folder(
                            id = folderId,
                            name = name,
                            userId = currentUser.value!!.uid,
                            parentFolderId = parentFolderId.value,
                            visibility = visibility),
                        userViewModel.currentUser.value!!.uid)
                    if (parentFolderId.value != null) {
                      val folderContentsScreen =
                          Screen.FOLDER_CONTENTS.replace(
                              oldValue = "{folderId}", newValue = folderId)
                      navigationActions.navigateTo(folderContentsScreen)
                    } else {
                      navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                    }
                  } else {
                    Toast.makeText(
                            context, "You are not the owner of this folder", Toast.LENGTH_SHORT)
                        .show()
                  }
                  showCreateDialog = false
                },
                action = "Create")
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

/**
 * Display the top bar of the folder content screen.
 *
 * @param folder the folder to display
 * @param updatedName the updated name of the folder
 * @param onUpdateName function to update the name of the folder
 * @param navigationActions actions that can be performed in the app
 * @param folderViewModel view model for the folder
 * @param noteViewModel view model for the note
 * @param currentUser the current user
 * @param context the context of the app
 * @param userFolderSubFolders the sub folders of the user
 * @param userFolderNotes the notes of the user
 * @param expanded whether the dropdown menu is expanded
 * @param onExpandedChange function to change the expanded state
 * @param showRenameDialog function to show the rename dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderContentTopBar(
    folder: Folder?,
    updatedName: String,
    onUpdateName: (String) -> Unit,
    navigationActions: NavigationActions,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel,
    currentUser: State<User?>,
    context: Context,
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
            onClick = { navigationActions.goBack() }, modifier = Modifier.testTag("goBackButton")) {
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

                          if (currentUser.value!!.uid == folder?.userId) {
                            // Retrieve parent folder id to navigate to the parent folder
                            val parentFolderId = folder.parentFolderId
                            folderViewModel.deleteFolderById(folder.id, folder.userId)

                            if (parentFolderId != null) {
                              val folderContentsScreen = Screen.FOLDER_CONTENTS.replace(
                                  oldValue = "{folderId}",
                                  newValue = parentFolderId)
                              navigationActions.navigateTo(folderContentsScreen)
                              //folderViewModel.getFolderById(parentFolderId)
                            } else {
                              navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                            }

                            handleSubFoldersAndNotes(
                                folder = folder,
                                userFolderSubFolders = userFolderSubFolders.value,
                                userFolderNotes = userFolderNotes.value,
                                folderViewModel = folderViewModel,
                                noteViewModel = noteViewModel)
                          } else {
                            Toast.makeText(
                                    context,
                                    "You are not the owner of this folder",
                                    Toast.LENGTH_SHORT)
                                .show()
                          }
                        },
                        modifier = Modifier.testTag("deleteFolderButton")),
                    CustomDropDownMenuItem(
                        text = { Text("Delete folder contents") },
                        icon = {
                          Icon(
                              painter = painterResource(id = R.drawable.delete_folder_contents),
                              contentDescription = "DeleteFolderContents")
                        },
                        onClick = {
                          onExpandedChange(false)
                          if (currentUser.value!!.uid == folder?.userId) {
                            // Delete all notes from the folder and call delete folder contents to
                            // delete everything except the folder itself
                            noteViewModel.deleteNotesFromFolder(folder.id)
                            folderViewModel.deleteFolderContents(folder, noteViewModel)
                          } else {
                            Toast.makeText(
                                    context,
                                    "You are not the owner of this folder",
                                    Toast.LENGTH_SHORT)
                                .show()
                          }
                        },
                        modifier = Modifier.testTag("deleteFolderContentsButton"))),
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
    // Folder is root folder
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
    currentUser: State<User?>,
    context: Context,
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
                    if (currentUser.value!!.uid == folder?.userId) {
                      noteViewModel.selectedFolderId(folder.id)
                      navigationActions.navigateTo(Screen.ADD_NOTE)
                    } else {
                      Toast.makeText(
                              context, "You are not the owner of this folder", Toast.LENGTH_SHORT)
                          .show()
                    }
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
                    if (currentUser.value!!.uid == folder?.userId) {
                      folderViewModel.selectedParentFolderId(folder.id)
                    }
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
 * @param userViewModel the view model for the user
 * @param context the context of the app
 * @param navigationActions actions that can be performed in the app
 */
@Composable
fun FolderContentScreenGrid(
    paddingValues: PaddingValues,
    userFolderNotes: State<List<Note>>,
    userFolderSubFolders: State<List<Folder>>,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    context: Context,
    navigationActions: NavigationActions
) {
  CustomLazyGrid(
      modifier = Modifier.fillMaxSize().padding(paddingValues),
      notes = userFolderNotes,
      folders = userFolderSubFolders,
      gridModifier =
          Modifier.fillMaxWidth().padding(horizontal = 20.dp).testTag("noteAndFolderList"),
      folderViewModel = folderViewModel,
      noteViewModel = noteViewModel,
      userViewModel = userViewModel,
      context = context,
      navigationActions = navigationActions,
      paddingValues = paddingValues,
      columnContent = {
        Text(modifier = Modifier.testTag("emptyFolderPrompt"), text = "This folder is empty.")
      })
}
