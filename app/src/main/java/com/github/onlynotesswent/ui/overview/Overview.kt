package com.github.onlynotesswent.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.github.onlynotesswent.utils.Visibility
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays the overview screen which contains a list of publicNotes retrieved from the ViewModel.
 * If there are no publicNotes, it shows a text to the user indicating no publicNotes are available.
 * It also provides a floating action button to add a new note.
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to display.
 */
@Composable
fun OverviewScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel
) {
  val userRootNotes = noteViewModel.userRootNotes.collectAsState()
  userViewModel.currentUser.collectAsState().value?.let { noteViewModel.getRootNotesFrom(it.uid) }

  val userRootFolders = folderViewModel.userRootFolders.collectAsState()
  userViewModel.currentUser.collectAsState().value?.let {
    folderViewModel.getRootFoldersFromUid(it.uid)
  }

  val parentFolderId = folderViewModel.parentFolderId.collectAsState()

  var expanded by remember { mutableStateOf(false) }
  var showCreateDialog by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag("overviewScreen"),
      floatingActionButton = {
        CustomDropDownMenu(
            modifier = Modifier.testTag("createNoteOrFolder"),
            menuItems =
                listOf(
                    CustomDropDownMenuItem(
                        text = { Text("Create note") },
                        onClick = {
                          expanded = false
                          navigationActions.navigateTo(Screen.ADD_NOTE)
                          noteViewModel.selectedFolderId(null)
                        },
                        modifier = Modifier.testTag("createNote")),
                    CustomDropDownMenuItem(
                        text = { Text("Create folder") },
                        onClick = {
                          expanded = false
                          showCreateDialog = true
                          folderViewModel.selectedParentFolderId(null)
                        },
                        modifier = Modifier.testTag("createFolder"))),
            fabIcon = { Icon(imageVector = Icons.Default.Add, contentDescription = "AddNote") },
            expanded = expanded,
            onFabClick = { expanded = true },
            onDismissRequest = { expanded = false })
        // Logic to show the dialog to create a folder
        if (showCreateDialog) {
          FolderDialog(
              onDismiss = { showCreateDialog = false },
              onConfirm = { newName, visibility ->
                folderViewModel.addFolder(
                    Folder(
                        id = folderViewModel.getNewFolderId(),
                        name = newName,
                        userId = userViewModel.currentUser.value!!.uid,
                        parentFolderId = parentFolderId.value,
                        visibility = visibility),
                    userViewModel.currentUser.value!!.uid)
                showCreateDialog = false
                if (parentFolderId.value != null) {
                  navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
                } else {
                  navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                }
              },
              action = "Create")
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { paddingValues ->
        CustomLazyGrid(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            notes = userRootNotes,
            folders = userRootFolders,
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
                  modifier = Modifier.testTag("emptyNoteAndFolderPrompt"),
                  text = "You have no notes or folders yet.",
                  color = MaterialTheme.colorScheme.onBackground)
              Spacer(modifier = Modifier.height(50.dp))
              RefreshButton {
                userViewModel.currentUser.value?.let { noteViewModel.getRootNotesFrom(it.uid) }
                userViewModel.currentUser.value?.let {
                  folderViewModel.getRootFoldersFromUid(it.uid)
                }
              }
              Spacer(modifier = Modifier.height(20.dp))
            })
      }
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
        Text("Refresh", color = MaterialTheme.colorScheme.onSurface)
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = MaterialTheme.colorScheme.onSurface)
      }
}

/**
 * Displays a single note item in a card format. The card contains the note's date, name, and user
 * ID. When clicked, it triggers the provided [onClick] action, which can be used for navigation or
 * other interactions.
 *
 * @param note The note data that will be displayed in this card.
 * @param onClick The lambda function to be invoked when the note card is clicked.
 */
@Composable
fun NoteItem(note: Note, onClick: () -> Unit) {
  Card(
      modifier =
          Modifier.testTag("noteCard")
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable(onClick = onClick),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(note.date.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
              }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
              text = note.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text(
              text = note.noteClass.classCode,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
      }
}

/**
 * Displays a single folder item in a card format. The card contains the folder's name. When
 * clicked, it triggers the provided onClick action, which can be used for navigation or other
 * interactions.
 *
 * @param folder The folder data that will be displayed in this card.
 * @param onClick The lambda function to be invoked when the folder card is clicked.
 */
@Composable
fun FolderItem(folder: Folder, onClick: () -> Unit) {

  Card(
      modifier =
          Modifier.testTag("folderCard").padding(vertical = 4.dp).clickable(onClick = onClick),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
              Image(
                  painter = painterResource(id = R.drawable.folder_icon_big),
                  contentDescription = "Folder Icon",
                  modifier = Modifier.size(80.dp))

              Text(
                  text = folder.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onBackground)
            }
      }
}

/**
 * Dialog that allows the user to create or rename a folder.
 *
 * @param onDismiss callback to be invoked when the dialog is dismissed
 * @param onConfirm callback to be invoked when the user confirms the new name
 * @param action the action to execute on the folder, either "Create" or "Rename"
 * @param oldVis the visibility of the folder
 * @param oldName the name of the folder
 */
@Composable
fun FolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Visibility) -> Unit,
    action: String,
    oldVis: Visibility? = null,
    oldName: String = ""
) {

  var name by remember { mutableStateOf(oldName) }
  var visibility: Visibility? by remember { mutableStateOf(oldVis) }
  var expandedVisibility by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
      Column(
          modifier = Modifier.padding(16.dp).testTag("folderDialog"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(0.92f),
                horizontalArrangement = Arrangement.Start) {
                  Text("$action Folder", style = MaterialTheme.typography.titleLarge)
                }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Folder Name") },
                modifier = Modifier.testTag("inputFolderName"))

            OptionDropDownMenu(
                value = visibility?.toReadableString() ?: "Choose visibility",
                expanded = expandedVisibility,
                buttonTag = "visibilityButton",
                menuTag = "visibilityMenu",
                onExpandedChange = { expandedVisibility = it },
                items = Visibility.READABLE_STRINGS,
                onItemClick = { visibility = Visibility.fromReadableString(it) },
                modifier = Modifier.testTag("visibilityDropDown"),
                widthFactor = 0.94f)

            Row(modifier = Modifier.fillMaxWidth(0.92f), horizontalArrangement = Arrangement.End) {
              Button(onClick = onDismiss, modifier = Modifier.testTag("dismissFolderAction")) {
                Text("Cancel")
              }
              Button(
                  enabled = name.isNotEmpty() && visibility != null,
                  onClick = { onConfirm(name, visibility ?: Visibility.DEFAULT) },
                  modifier = Modifier.testTag("confirmFolderAction")) {
                    Text(action)
                  }
            }
          }
    }
  }
}

/**
 * Custom dropdown menu that displays a floating action button with a dropdown menu. The dropdown
 * menu contains two items, each with its own text and onClick action.
 *
 * @param modifier The modifier for the floating action button.
 * @param fabIcon The icon to be displayed on the floating action button.
 * @param menuItems The list of dropdown menu items to be displayed in the dropdown menu.
 * @param expanded The state of the dropdown menu.
 * @param onFabClick The action to be invoked when the floating action button is clicked.
 * @param onDismissRequest The action to be invoked when the dropdown menu is dismissed.
 */
@Composable
fun CustomDropDownMenu(
    modifier: Modifier,
    menuItems: List<CustomDropDownMenuItem>,
    fabIcon: @Composable () -> Unit,
    expanded: Boolean,
    onFabClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
  Box {
    FloatingActionButton(onClick = onFabClick, modifier = modifier) { fabIcon() }
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
      menuItems.forEach { item ->
        DropdownMenuItem(text = item.text, onClick = item.onClick, modifier = item.modifier)
      }
    }
  }
}

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
                navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
              }
            }
            items(notes.value.size) { index ->
              NoteItem(note = notes.value[index]) {
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

data class CustomDropDownMenuItem(
    val text: @Composable () -> Unit,
    val onClick: () -> Unit,
    val modifier: Modifier = Modifier
)
