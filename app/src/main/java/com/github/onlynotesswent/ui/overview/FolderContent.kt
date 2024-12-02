package com.github.onlynotesswent.ui.overview

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.ConfirmationPopup
import com.github.onlynotesswent.ui.common.CustomDropDownMenu
import com.github.onlynotesswent.ui.common.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.common.CustomLazyGrid
import com.github.onlynotesswent.ui.common.CustomSeparatedLazyGrid
import com.github.onlynotesswent.ui.common.FolderDialog
import com.github.onlynotesswent.ui.common.NoteDialog
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp

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
  var showUpdateDialog by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showCreateNoteDialog by remember { mutableStateOf(false) }

  var updatedName by remember { mutableStateOf(folder.value!!.name) }
  var expandedFolder by remember { mutableStateOf(false) }

  // Custom back handler to manage back navigation
  BackHandler {
    if (folder.value!!.parentFolderId != null) {
      navigationActions.navigateTo(
          Screen.FOLDER_CONTENTS.replace(
              oldValue = "{folderId}", newValue = folder.value!!.parentFolderId!!))
    } else {
      navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
    }
  }

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
              showUpdateDialog = { showUpdateDialog = it })
        },
        floatingActionButton = {
          CreateSubItemFab(
              expandedFolder = expandedFolder,
              onExpandedFolderChange = { expandedFolder = it },
              showCreateFolderDialog = { showCreateFolderDialog = it },
              showCreateNoteDialog = { showCreateNoteDialog = it },
              noteViewModel = noteViewModel,
              folderViewModel = folderViewModel,
              currentUser = currentUser,
              context = context,
              folder = folder.value)
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
          // Logic to show the dialog to update a folder
          if (showUpdateDialog) {
            FolderDialog(
                onDismiss = { showUpdateDialog = false },
                onConfirm = { name, vis ->
                  if (currentUser.value!!.uid == folder.value?.userId) {
                    folderViewModel.updateFolder(
                        Folder(
                            id = folder.value!!.id,
                            name = name,
                            userId = folder.value!!.userId,
                            parentFolderId = folder.value!!.parentFolderId,
                            visibility = vis))
                    updatedName = name
                  } else {
                    Toast.makeText(
                            context, "You are not the owner of this folder", Toast.LENGTH_SHORT)
                        .show()
                  }
                  showUpdateDialog = false
                },
                action = stringResource(R.string.update),
                oldName = updatedName,
                oldVisibility = folder.value!!.visibility)
          }
          if (showCreateNoteDialog) {
            NoteDialog(
                onDismiss = { showCreateNoteDialog = false },
                onConfirm = { newName, visibility ->
                  val note =
                      Note(
                          id = noteViewModel.getNewUid(),
                          title = newName,
                          date = Timestamp.now(),
                          visibility = visibility,
                          noteCourse = Course.EMPTY,
                          userId = userViewModel.currentUser.value!!.uid,
                          folderId = folder.value!!.id)
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
                onConfirm = { name, visibility ->
                  if (currentUser.value!!.uid == folder.value?.userId) {
                    val folderId = folderViewModel.getNewFolderId()
                    folderViewModel.addFolder(
                        Folder(
                            id = folderId,
                            name = name,
                            userId = currentUser.value!!.uid,
                            parentFolderId = parentFolderId.value,
                            visibility = visibility))
                    if (parentFolderId.value != null) {
                      navigationActions.navigateTo(
                          Screen.FOLDER_CONTENTS.replace(
                              oldValue = "{folderId}", newValue = folderId))
                    } else {
                      navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                    }
                  } else {
                    Toast.makeText(
                            context, "You are not the owner of this folder", Toast.LENGTH_SHORT)
                        .show()
                  }
                  showCreateFolderDialog = false
                },
                action = stringResource(R.string.create))
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
        Text(stringResource(R.string.user_not_found))
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
 * @param showUpdateDialog function to show the update dialog
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
    showUpdateDialog: (Boolean) -> Unit,
) {
  var showDeleteFolderConfirmation by remember { mutableStateOf(false) }
  var showDeleteFolderContentsConfirmation by remember { mutableStateOf(false) }
  TopAppBar(
      colors =
          TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
      title = {
        Row(
            modifier = Modifier.fillMaxWidth().testTag("folderContentTitle"),
            verticalAlignment = Alignment.CenterVertically) {
              // Update the updatedName state whenever the folder state changes to display it in the
              // title
              LaunchedEffect(folder?.name) {
                onUpdateName(folder?.name ?: context.getString(R.string.folder_name_not_found))
              }
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
            onClick = { navigationActions.goBackFolderContents(folder?.parentFolderId) },
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
                        text = { Text(stringResource(R.string.update_folder)) },
                        icon = {
                          Icon(
                              imageVector = Icons.Default.Edit, contentDescription = "UpdateFolder")
                        },
                        onClick = {
                          onExpandedChange(false)
                          showUpdateDialog(true)
                        },
                        modifier = Modifier.testTag("updateFolderButton")),
                    CustomDropDownMenuItem(
                        text = { Text(stringResource(R.string.delete_folder)) },
                        icon = {
                          Icon(
                              painter = painterResource(id = R.drawable.folder_delete_icon),
                              contentDescription = "DeleteFolder")
                        },
                        onClick = {
                          onExpandedChange(false)
                          showDeleteFolderConfirmation = true
                        },
                        modifier = Modifier.testTag("deleteFolderButton")),
                    CustomDropDownMenuItem(
                        text = { Text(stringResource(R.string.delete_folder_contents)) },
                        icon = {
                          Icon(
                              painter = painterResource(id = R.drawable.delete_folder_contents),
                              contentDescription = "DeleteFolderContents")
                        },
                        onClick = {
                          onExpandedChange(false)
                          showDeleteFolderContentsConfirmation = true
                        },
                        modifier = Modifier.testTag("deleteFolderContentsButton"))),
            fabIcon = {
              Icon(imageVector = Icons.Default.MoreVert, contentDescription = "settings")
            },
            expanded = expanded,
            onFabClick = { onExpandedChange(true) },
            onDismissRequest = { onExpandedChange(false) })

        // Popup for delete folder confirmation
        if (showDeleteFolderConfirmation) {
          ConfirmationPopup(
              title = stringResource(R.string.delete_folder),
              text = stringResource(R.string.confirm_delete_folder),
              onConfirm = {
                if (currentUser.value!!.uid == folder?.userId) {
                  // Retrieve parent folder id to navigate to the parent folder
                  val parentFolderId = folder.parentFolderId
                  folderViewModel.deleteFolderById(folder.id, folder.userId)

                  if (parentFolderId != null) {
                    navigationActions.navigateTo(
                        Screen.FOLDER_CONTENTS.replace(
                            oldValue = "{folderId}", newValue = parentFolderId))
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
                          context, "You are not the owner of this folder", Toast.LENGTH_SHORT)
                      .show()
                }
                showDeleteFolderConfirmation = false
              },
              onDismiss = { showDeleteFolderConfirmation = false })
        }

        // Popup for delete folder contents confirmation
        if (showDeleteFolderContentsConfirmation) {
          ConfirmationPopup(
              title = stringResource(R.string.delete_folder_contents),
              text = stringResource(R.string.confirm_delete_folder_contents),
              onConfirm = {
                if (currentUser.value!!.uid == folder?.userId) {
                  noteViewModel.deleteNotesFromFolder(folder.id)
                  folderViewModel.deleteFolderContents(folder, noteViewModel)
                } else {
                  Toast.makeText(
                          context, "You are not the owner of this folder", Toast.LENGTH_SHORT)
                      .show()
                }
                showDeleteFolderContentsConfirmation = false
              },
              onDismiss = { showDeleteFolderContentsConfirmation = false })
        }
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
      folderViewModel.updateFolder(subFolder.copy(parentFolderId = folder.parentFolderId))
    }
    userFolderNotes.forEach { note ->
      noteViewModel.updateNote(note.copy(folderId = folder.parentFolderId))
    }
  } else {
    // Folder is root folder
    userFolderSubFolders.forEach { subFolder ->
      folderViewModel.updateFolder(subFolder.copy(parentFolderId = null))
    }
    userFolderNotes.forEach { note -> noteViewModel.updateNote(note.copy(folderId = null)) }
  }
}

/**
 * Create a FAB to create a sub item (note or folder) in the current folder.
 *
 * @param expandedFolder whether the folder is expanded
 * @param onExpandedFolderChange function to change the expanded state
 * @param showCreateFolderDialog function to show the create dialog
 * @param noteViewModel the view model for the note
 * @param folderViewModel the view model for the folder
 * @param folder the current folder
 */
@Composable
fun CreateSubItemFab(
    expandedFolder: Boolean,
    onExpandedFolderChange: (Boolean) -> Unit,
    showCreateFolderDialog: (Boolean) -> Unit,
    showCreateNoteDialog: (Boolean) -> Unit,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
    currentUser: State<User?>,
    context: Context,
    folder: Folder?,
) {
  CustomDropDownMenu(
      modifier = Modifier.testTag("createSubNoteOrSubFolder"),
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
                    onExpandedFolderChange(false)
                    if (currentUser.value!!.uid == folder?.userId) {
                      showCreateNoteDialog(true)
                      noteViewModel.selectedFolderId(folder.id)
                    } else {
                      Toast.makeText(
                              context, "You are not the owner of this folder", Toast.LENGTH_SHORT)
                          .show()
                    }
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
                    onExpandedFolderChange(false)
                    showCreateFolderDialog(true)
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
  CustomSeparatedLazyGrid(
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
        Text(
            modifier = Modifier.testTag("emptyFolderPrompt"),
            text = stringResource(R.string.this_folder_is_empty))
      })
}
