package com.github.onlynotesswent.ui.overview

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.ConfirmationPopup
import com.github.onlynotesswent.ui.common.CustomDropDownMenu
import com.github.onlynotesswent.ui.common.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.common.EditDeckDialog
import com.github.onlynotesswent.ui.common.FileSystemPopup
import com.github.onlynotesswent.ui.common.FolderDialog
import com.github.onlynotesswent.ui.common.NoteDialog
import com.github.onlynotesswent.ui.common.SavedDocumentButton
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
 * @param isDeckView whether the view is for a deck
 * @param deckViewModel view model for the deck
 */
@Composable
fun FolderContentScreen(
    navigationActions: NavigationActions,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel? = null,
    userViewModel: UserViewModel,
    isDeckView: Boolean = false,
    deckViewModel: DeckViewModel? = null
) {
  val folder = folderViewModel.selectedFolder.collectAsState()
  val currentUser = userViewModel.currentUser.collectAsState()

  val userFolderNotes = noteViewModel?.folderNotes?.collectAsState()
  val userFolderDecks = deckViewModel?.folderDecks?.collectAsState()
  val userFolderSubFolders = folderViewModel.folderSubFolders.collectAsState()

  val context = LocalContext.current

  var expanded by remember { mutableStateOf(false) }
  var showUpdateDialog by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showCreateDialog by remember { mutableStateOf(false) }

  var updatedName by remember { mutableStateOf(folder.value!!.name) }
  var expandedFolder by remember { mutableStateOf(false) }

  // Custom back handler to manage back navigation
  BackHandler {
    folderViewModel.clearSelectedFolder()
    navigationActions.goBackFolderContents(folder.value!!, currentUser.value!!, isDeckView)
  }

  if (currentUser.value == null) {
    UserNotFoundFolderContentScreen()
  } else {
    Scaffold(
        modifier = Modifier.testTag("folderContentScreen"),
        topBar = {
            Column {
                FolderContentTopBar(
                    folder = folder.value,
                    updatedName = updatedName,
                    onUpdateName = { updatedName = it },
                    navigationActions = navigationActions,
                    folderViewModel = folderViewModel,
                    noteViewModel = noteViewModel,
                    userViewModel = userViewModel,
                    currentUser = currentUser,
                    context = context,
                    userFolderSubFolders = userFolderSubFolders,
                    userFolderNotes = userFolderNotes,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    showUpdateDialog = { showUpdateDialog = it },
                    isDeckView = isDeckView,
                    deckViewModel = deckViewModel,
                    userFolderDecks = userFolderDecks)

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 0.5.dp)
            }

        },
        floatingActionButton = {
          if (folder.value!!.isOwner(currentUser.value!!.uid)) {
            CreateItemFab(
                expandedFab = expandedFolder,
                onExpandedFabChange = { expandedFolder = it },
                showCreateFolderDialog = { showCreateFolderDialog = it },
                showCreateItemDialog = { showCreateDialog = it },
                isDeckView = isDeckView)
          }
        }) { paddingValues ->
          Text(
              text = "Selected Folder: ${folderViewModel.selectedFolder.value?.name ?: "None"}",
              style = MaterialTheme.typography.bodySmall)

          if (isDeckView) {
            DeckOverviewScreenGrid(
                paddingValues = paddingValues,
                userDecks = userFolderDecks!!,
                userFolders = userFolderSubFolders,
                folderViewModel = folderViewModel,
                deckViewModel = deckViewModel,
                userViewModel = userViewModel,
                navigationActions = navigationActions)
          } else {
            NoteOverviewScreenGrid(
                paddingValues = paddingValues,
                userNotes = userFolderNotes!!,
                userFolders = userFolderSubFolders,
                folderViewModel = folderViewModel,
                noteViewModel = noteViewModel,
                userViewModel = userViewModel,
                navigationActions = navigationActions)
          }
          // Logic to show the dialog to update a folder

          if (showUpdateDialog) {
            FolderDialog(
                onDismiss = { showUpdateDialog = false },
                onConfirm = { name, vis ->
                  folderViewModel.updateFolder(
                      folder.value!!.copy(
                          name = name, visibility = vis, lastModified = Timestamp.now()))
                  updatedName = name
                  showUpdateDialog = false
                },
                action = stringResource(R.string.update),
                oldName = updatedName,
                oldVisibility = folder.value!!.visibility)
          }
          if (showCreateDialog) {

            if (isDeckView) {
              EditDeckDialog(
                  deckViewModel = deckViewModel!!,
                  userViewModel = userViewModel,
                  onDismissRequest = { showCreateDialog = false },
                  mode = stringResource(R.string.create),
                  onSave = { navigationActions.navigateTo(Screen.DECK_MENU) },
                  folderId = folder.value!!.id)
            } else {
              NoteDialog(
                  onDismiss = { showCreateDialog = false },
                  onConfirm = { newName, visibility ->
                    val note =
                        Note(
                            id = noteViewModel!!.getNewUid(),
                            title = newName,
                            date = Timestamp.now(),
                            lastModified = Timestamp.now(),
                            visibility = visibility,
                            noteCourse = Course.EMPTY,
                            userId = userViewModel.currentUser.value!!.uid,
                            folderId = folder.value!!.id)
                    noteViewModel.addNote(note)
                    noteViewModel.selectedNote(note)
                    showCreateDialog = false
                    navigationActions.navigateTo(Screen.EDIT_NOTE)
                  },
                  action = stringResource(R.string.create))
            }
          }
          // Logic to show the dialog to create a folder
          if (showCreateFolderDialog) {
            FolderDialog(
                onDismiss = { showCreateFolderDialog = false },
                onConfirm = { name, visibility ->
                  val newFolderId = folderViewModel.getNewFolderId()
                  folderViewModel.addFolder(
                      Folder(
                          id = newFolderId,
                          name = name,
                          userId = currentUser.value!!.uid,
                          parentFolderId = folder.value!!.id,
                          visibility = visibility,
                          lastModified = Timestamp.now(),
                          isDeckFolder = isDeckView))
                  navigationActions.navigateTo(
                      Screen.FOLDER_CONTENTS.replace(
                          oldValue = "{folderId}", newValue = newFolderId))
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
      modifier = Modifier.fillMaxSize().testTag("userNotFoundScreen"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.user_not_found))
      }
  Log.e("FolderContentScreen", "User not found")
}

/**
 * Displays a button that adds or removes the folder to the user's saved folders.
 *
 * @param folder The folder to be saved or removed.
 * @param userViewModel The ViewModel that provides the current user.
 * @param folderViewModel The ViewModel that provides the current folder to be edited and handles
 *   folder updates.
 */
@Composable
fun SavedFoldersButton(
    folder: Folder,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel
) {
  val savedFolders by folderViewModel.userSavedFolders.collectAsState()

  val context = LocalContext.current

  SavedDocumentButton(
      isSaved = folder.id in savedFolders.map { it.id },
      onSave = {
        folderViewModel.addCurrentUserSavedFolder(
            folder,
            userViewModel,
            onFailure = {
              Toast.makeText(context, "Failed to save folder", Toast.LENGTH_SHORT).show()
            })
      },
      onDelete = {
        folderViewModel.deleteCurrentUserSavedFolder(
            folder.id,
            userViewModel,
            onFailure = {
              Toast.makeText(
                      context, "Failed to remove folder from saved folders", Toast.LENGTH_SHORT)
                  .show()
            })
      })
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
 * @param userViewModel view model to get the current user
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
    noteViewModel: NoteViewModel?,
    deckViewModel: DeckViewModel?,
    userViewModel: UserViewModel,
    currentUser: State<User?>,
    context: Context,
    userFolderSubFolders: State<List<Folder>>,
    userFolderNotes: State<List<Note>>?,
    userFolderDecks: State<List<Deck>>?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    showUpdateDialog: (Boolean) -> Unit,
    isDeckView: Boolean
) {
  var showDeleteFolderConfirmation by remember { mutableStateOf(false) }
  var showDeleteFolderContentsConfirmation by remember { mutableStateOf(false) }
  var showFileSystemPopup by remember { mutableStateOf(false) }
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
              if (currentUser.value?.uid != folder?.userId) {
                Spacer(modifier = Modifier.weight(1.5f))
              } else {
                Spacer(modifier = Modifier.weight(2f))
              }
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
              navigationActions.goBackFolderContents(
                  folder!!, userViewModel.currentUser.value!!, isDeckView)
              if (folder.parentFolderId == null) {
                folderViewModel.clearSelectedFolder()
              }
            },
            modifier = Modifier.testTag("goBackButton")) {
              Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
      },
      actions = {
        if (folder!!.isOwner(currentUser.value!!.uid)) {
          CustomDropDownMenu(
              modifier = Modifier.testTag("folderSettingsButton"),
              menuItems =
                  listOf(
                      CustomDropDownMenuItem(
                          text = { Text(stringResource(R.string.update_folder)) },
                          icon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "UpdateFolder")
                          },
                          onClick = {
                            onExpandedChange(false)
                            showUpdateDialog(true)
                          },
                          modifier = Modifier.testTag("updateFolderButton")),
                      CustomDropDownMenuItem(
                          text = { Text("Move Folder") },
                          icon = {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "moveFolder")
                          },
                          onClick = {
                            onExpandedChange(false)
                            showFileSystemPopup = true
                          },
                          modifier = Modifier.testTag("moveFolderButton")),
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
        } else if (folder.visibility == Visibility.PUBLIC ||
            (folder.visibility == Visibility.FRIENDS &&
                folder.userId in currentUser.value!!.friends.following)) {
          // Display the saved folders button if the folder is public or the user is following the
          // owner, and the user is not the owner
          SavedFoldersButton(folder, userViewModel, folderViewModel)
        }

        // Popup for delete folder confirmation
        if (showDeleteFolderConfirmation) {
          ConfirmationPopup(
              title = stringResource(R.string.delete_folder),
              text = stringResource(R.string.confirm_delete_folder),
              onConfirm = {
                folderViewModel.deleteFolderById(folder.id, folder.userId)
                folderViewModel.clearSelectedFolder()

                folderViewModel.getRootDeckFoldersFromUserId(currentUser.value!!.uid)
                navigationActions.goBackFolderContents(folder, currentUser.value!!, isDeckView)

                handleSubFoldersAndContent(
                    folder = folder,
                    userFolderSubFolders = userFolderSubFolders.value,
                    userFolderNotes = userFolderNotes?.value,
                    userFolderDecks = userFolderDecks?.value,
                    folderViewModel = folderViewModel,
                    noteViewModel = noteViewModel,
                    deckViewModel = deckViewModel,
                    isDeckView = isDeckView)

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
                if (isDeckView) {
                  deckViewModel!!.deleteDecksFromFolder(folder.id)
                  folderViewModel.deleteFolderContents(folder, deckViewModel)
                } else {
                  noteViewModel!!.deleteNotesFromFolder(folder.id)
                  folderViewModel.deleteFolderContents(folder, noteViewModel)
                }
                showDeleteFolderContentsConfirmation = false
              },
              onDismiss = { showDeleteFolderContentsConfirmation = false })
        }
        // Show the FileSystemPopup if requested
        if (showFileSystemPopup) {
          FileSystemPopup(
              onDismiss = { showFileSystemPopup = false },
              folderViewModel = folderViewModel,
              onMoveHere = { selectedFolder ->
                if (selectedFolder == folder) {
                  return@FileSystemPopup
                }

                folderViewModel.moveFolder(
                    selectedFolder,
                    onSubFolderError = {
                      Toast.makeText(
                              context,
                              context.getString(R.string.folder_cannot_be_moved_to_subfolder),
                              Toast.LENGTH_SHORT)
                          .show()
                    },
                    onSuccess = {
                      if (selectedFolder != null) {
                        navigationActions.navigateTo(
                            Screen.FOLDER_CONTENTS.replace(
                                oldValue = "{folderId}", newValue = selectedFolder.id))
                        return@moveFolder
                      }

                      if (isDeckView) {
                        navigationActions.navigateTo(TopLevelDestinations.DECK_OVERVIEW)
                      } else {
                        navigationActions.navigateTo(TopLevelDestinations.NOTE_OVERVIEW)
                      }
                    })
              })
        }
      })
}

/**
 * Handle sub folders and content when a folder is deleted.
 *
 * @param folder the folder that is deleted
 * @param userFolderSubFolders the sub folders of the user
 * @param userFolderNotes the notes of the user
 * @param folderViewModel the view model for the folder
 * @param noteViewModel the view model for the note
 * @param deckViewModel the view model for the deck
 * @param isDeckView whether the view is for a deck
 */
fun handleSubFoldersAndContent(
    folder: Folder,
    userFolderSubFolders: List<Folder>,
    userFolderNotes: List<Note>?,
    userFolderDecks: List<Deck>?,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel?,
    deckViewModel: DeckViewModel?,
    isDeckView: Boolean
) {
  // If folder is subfolder, set parent Id and folder Id of sub
  // elements to parent folder id
  userFolderSubFolders.forEach { subFolder ->
    folderViewModel.updateFolder(subFolder.copy(parentFolderId = folder.parentFolderId))
  }
  if (isDeckView) {
    userFolderDecks!!.forEach { deck ->
      deckViewModel!!.updateDeck(deck.copy(folderId = folder.parentFolderId))
    }
  } else {
    userFolderNotes!!.forEach { note ->
      noteViewModel!!.updateNote(note.copy(folderId = folder.parentFolderId))
    }
  }
}
