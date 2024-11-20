package com.github.onlynotesswent.utils

import android.content.ClipData
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.platform.LocalContext
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
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays a single note item in a card format. The card contains the note's date, name, and user
 * ID. When clicked, it triggers the provided [onClick] action, which can be used for navigation or
 * other interactions.
 *
 * @param note The note data that will be displayed in this card.
 * @param author The author of the note.
 * @param noteViewModel The ViewModel that provides the list of notes to display.
 * @param showDialog A boolean indicating whether the move out dialog should be displayed.
 * @param navigationActions The navigation instance used to transition between different screens.
 * @param onClick The lambda function to be invoked when the note card is clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: Note,
    author: String? = null,
    currentUser: State<User?>,
    context: Context,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
    showDialog: Boolean,
    navigationActions: NavigationActions,
    onClick: () -> Unit
) {
  // Mutable state to show the move out dialog
  var showMoveOutDialog by remember { mutableStateOf(showDialog) }

  if (showMoveOutDialog && note.folderId != null) {
    AlertDialog(
        onDismissRequest = { showMoveOutDialog = false },
        title = {
          Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Move note out of folder")
          }
        },
        confirmButton = {
          Button(
              onClick = {
                if (currentUser.value!!.uid == note.userId) {
                  // Move out will move the given note to the parent folder
                  val parentFolderId = navigationActions.popFromScreenNavigationStack()
                  if (parentFolderId != null) {
                    noteViewModel.updateNote(note.copy(folderId = parentFolderId), note.userId)
                    folderViewModel.getFolderById(parentFolderId)
                  } else {
                    noteViewModel.updateNote(note.copy(folderId = null), note.userId)
                    navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                  }
                  // Clear the screen navigation stack as we navigate to the overview screen

                  // navigationActions.clearScreenNavigationStack()
                  // navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                } else {
                  Toast.makeText(
                          context,
                          "You can't move out a note that you didn't create",
                          Toast.LENGTH_SHORT)
                      .show()
                }
                showMoveOutDialog = false
              }) {
                Text("Move")
              }
        },
        dismissButton = { Button(onClick = { showMoveOutDialog = false }) { Text("Cancel") } })
  }
  Card(
      modifier =
          Modifier.testTag("noteCard")
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              // Enable drag and drop for the note card (as a source)
              .dragAndDropSource {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                      noteViewModel.draggedNote(note)
                      // noteViewModel.selectedNote(note)
                      // Start a drag-and-drop operation to transfer the data which is being dragged
                      startTransfer(
                          // Transfer the note Id as a ClipData object
                          DragAndDropTransferData(ClipData.newPlainText("Note", note.id)))
                    },
                )
              },
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
                      // Show move out menu when clicking on the Icon
                      modifier =
                          Modifier.clickable(
                              enabled =
                                  note.folderId != null &&
                                      navigationActions.currentRoute() == Screen.FOLDER_CONTENTS) {
                                showMoveOutDialog = true
                              },
                      imageVector = Icons.Filled.MoreVert,
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
          if (author != null) {
            Text(
                text = author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
          }
          Text(
              text = note.noteCourse.fullName(),
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderItem(
    folder: Folder,
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
    onClick: () -> Unit
) {

  val context = LocalContext.current
  val dropSuccess = remember { mutableStateOf(false) }

  Card(
      modifier =
          Modifier.testTag("folderCard")
              .padding(vertical = 4.dp)
              .dragAndDropSource {
                detectTapGestures(
                    // When tapping on a folder, perform onCLick
                    onTap = { onClick() },
                    onLongPress = {
                      folderViewModel.draggedFolder(folder)
                      // Start a drag-and-drop operation to transfer the data which is being dragged
                      startTransfer(
                          DragAndDropTransferData(
                              // Transfer the folder Id as a ClipData object
                              ClipData.newPlainText("Folder", folder.id)))
                    })
              } // Enable drag-and-drop for the folder (as a target)
              .dragAndDropTarget(
                  // Accept any drag-and-drop event (either folder or note in this case)
                  shouldStartDragAndDrop = { true },
                  // Handle the drop event
                  target =
                      remember {
                        object : DragAndDropTarget {
                          override fun onDrop(event: DragAndDropEvent): Boolean {
                            // Set the target folder as the selected folder to navigate to it when
                            // dropping on it
                            // folderViewModel.selectedFolder(folder)
                            Log.e(
                                "FolderItem",
                                "onDrop selected folder: ${folder.name} vs real selected folder: ${folderViewModel.selectedFolder.value?.name}")
                            // Get the dragged object Id
                            val draggedObjectId =
                                event.toAndroidDragEvent().clipData.getItemAt(0).text.toString()
                            val draggedNote = noteViewModel.draggedNote.value
                            Log.e(
                                "FolderItem",
                                "selectedNoteId: ${draggedNote?.id} and draggedObjectId: $draggedObjectId")
                            if (draggedNote != null && draggedNote.id == draggedObjectId) {
                              // Update the selected note (dragged) with the new folder Id
                              noteViewModel.updateNote(
                                  draggedNote.copy(folderId = folder.id), draggedNote.userId)

                              Log.e("FolderItem", "returning true")
                              // Toast:
                              Toast.makeText(
                                      context, "Note moved to ${folder.name}", Toast.LENGTH_SHORT)
                                  .show()
                              noteViewModel.draggedNote(null)
                              dropSuccess.value = true
                              return true
                            }
                            // Get the dragged folder in case a folder is being dragged
                            val draggedFolder = folderViewModel.draggedFolder.value
                            val selectedFolder = folderViewModel.selectedFolder.value
                            Log.e(
                                "FolderItem",
                                "draggedFolderId: ${draggedFolder?.id} and draggedObjectId: $draggedObjectId and selectedFolderId: ${selectedFolder?.id} and folderId: ${folder.id}")
                            if (draggedFolder != null &&
                                draggedFolder.id == draggedObjectId &&
                                draggedFolder.id != folder.id &&
                                draggedFolder.parentFolderId ==
                                    folder.parentFolderId // If dragged and folder dont have same
                                // parent id dont allow drop
                                &&
                                selectedFolder?.id !=
                                    folder.id) { // Also check the current selected folder and the
                              // target folder are not the same
                              // Update the dragged folder with the new parent folder Id. Folder
                              // here represents the target folder, so the future parent of the
                              // dragged folder
                              folderViewModel.updateFolder(
                                  draggedFolder.copy(parentFolderId = folder.id), folder.userId)
                              // Toast:
                              Toast.makeText(
                                      context, "Folder moved to ${folder.name}", Toast.LENGTH_SHORT)
                                  .show()
                              folderViewModel.draggedFolder(null)
                              // Allows calling the LaunchedEffect after returning true
                              dropSuccess.value = true
                              return true
                            }
                            dropSuccess.value = false
                            return false
                          }

                          override fun onEnded(event: DragAndDropEvent) {
                            if (dropSuccess.value) {
                              // Drop was successful // TODO try calling collect as state inside
                              // folder content
                              // if (folder.parentFolderId == null) {
                              folderViewModel.selectedFolder(folder)
                              navigateToFolderContents(folder, navigationActions)
                              // } else {
                              // To refresh folder contents
                              // navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                              // }
                            }
                            // Reset dropSuccess value
                            dropSuccess.value = false
                          }
                        }
                      }),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
              Image(
                  painter = painterResource(id = R.drawable.folder_icon),
                  contentDescription = "Folder Icon",
                  modifier = Modifier.size(80.dp))

              Text(
                  modifier = Modifier.align(Alignment.CenterHorizontally),
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
 * @param action the action to be performed (create or rename)
 * @param oldVis the old visibility of the folder (if renaming)
 * @param oldName the old name of the folder (if renaming)
 */
@Composable
fun FolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Visibility) -> Unit,
    action: String,
    oldVis: Visibility? = null,
    oldName: String = ""
) {

  var name by remember { mutableStateOf("") }
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
                onValueChange = { name = Folder.formatName(it) },
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
        DropdownMenuItem(
            text = item.text,
            leadingIcon = item.icon,
            onClick = item.onClick,
            modifier = item.modifier)
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
    userViewModel: UserViewModel,
    context: Context,
    navigationActions: NavigationActions,
    paddingValues: PaddingValues,
    columnContent: @Composable (ColumnScope.() -> Unit)
) {
  val sortedFolders = folders.value.sortedBy { it.name }
  val sortedNotes = notes.value.sortedBy { it.title }

  Box(modifier = modifier) {
    if (sortedNotes.isNotEmpty() || sortedFolders.isNotEmpty()) {
      LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 100.dp),
          contentPadding = PaddingValues(vertical = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = gridModifier) {
            items(sortedFolders.size) { index ->
              FolderItem(
                  folder = sortedFolders[index],
                  navigationActions = navigationActions,
                  noteViewModel = noteViewModel,
                  folderViewModel = folderViewModel) {
                    folderViewModel.selectedFolder(sortedFolders[index])
                    navigateToFolderContents(sortedFolders[index], navigationActions)
                  }
            }
            items(sortedNotes.size) { index ->
              NoteItem(
                  note = sortedNotes[index],
                  currentUser = userViewModel.currentUser.collectAsState(),
                  context = context,
                  noteViewModel = noteViewModel,
                  folderViewModel = folderViewModel,
                  showDialog = false,
                  navigationActions = navigationActions) {
                    noteViewModel.selectedNote(sortedNotes[index])
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

/**
 * Custom dropdown menu item that contains a text and an onClick action.
 *
 * @param text The text to be displayed in the dropdown menu item.
 * @param icon The icon to be displayed in the dropdown menu item.
 * @param onClick The action to be invoked when the dropdown menu item is clicked.
 * @param modifier The modifier for the dropdown menu item.
 */
data class CustomDropDownMenuItem(
    val text: @Composable () -> Unit,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit,
    val modifier: Modifier = Modifier
)

/**
 * A composable function that displays an `OutlinedTextField` with an optional trailing icon.
 *
 * @param value The current value of the text field.
 * @param onValueChange The callback that is triggered when the value changes.
 * @param label The label to be displayed inside the text field container.
 * @param placeholder The placeholder to be displayed when the text field is in focus and the input
 *   text is empty.
 * @param modifier The modifier to be applied to the `OutlinedTextField`.
 * @param trailingIcon An optional trailing icon displayed at the end of the text field container.
 */
@Composable
fun NoteDataTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      modifier = modifier,
      trailingIcon = trailingIcon,
      colors =
          TextFieldDefaults.colors(
              focusedIndicatorColor = MaterialTheme.colorScheme.primary,
              unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
              focusedContainerColor = MaterialTheme.colorScheme.background,
              unfocusedContainerColor = MaterialTheme.colorScheme.background))
}

/**
 * A composable function that displays the top app bar for the screen. It is composed of an icon
 * button and a title.
 *
 * @param title The title to be displayed in the top app bar.
 * @param titleTestTag The test tag for the title.
 * @param onBackClick The callback to be invoked when the back button is clicked.
 * @param icon The icon to be displayed in the top app bar.
 * @param iconTestTag The test tag for the icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTopBar(
    title: String,
    titleTestTag: String,
    onBackClick: () -> Unit,
    icon: @Composable () -> Unit,
    iconTestTag: String
) {
  TopAppBar(
      colors =
          TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
      title = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
              Spacer(modifier = Modifier.weight(1.4f))

              Text(
                  title,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.testTag(titleTestTag))

              Spacer(modifier = Modifier.weight(2f))
            }
      },
      navigationIcon = {
        IconButton(onClick = onBackClick, Modifier.testTag(iconTestTag), content = icon)
      })
}

/**
 * A composable function that displays an `OutlinedTextField` with a dropdown menu.
 *
 * @param value The current value of the text field.
 * @param expanded A boolean indicating whether the dropdown menu is expanded.
 * @param onExpandedChange A callback to be invoked when the expanded state of the dropdown menu
 *   changes.
 * @param items A list of strings representing the items to be displayed in the dropdown menu.
 * @param onItemClick A callback to be invoked when an item in the dropdown menu is clicked.
 * @param modifier The modifier to be applied to the `OutlinedTextField`.
 */
@Composable
fun OptionDropDownMenu(
    expanded: Boolean,
    value: String,
    buttonTag: String,
    menuTag: String,
    onExpandedChange: (Boolean) -> Unit,
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    widthFactor: Float = 0.8f
) {
  BoxWithConstraints(modifier = Modifier.fillMaxWidth(widthFactor)) {
    Button(
        onClick = { onExpandedChange(!expanded) },
        modifier = Modifier.width(maxWidth).testTag(buttonTag)) {
          Text(text = value)
          Icon(Icons.Outlined.ArrowDropDown, "Dropdown icon")
        }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        modifier = modifier.width(maxWidth).testTag(menuTag)) {
          items.forEach { item ->
            DropdownMenuItem(
                modifier = Modifier.testTag("item--$item"),
                text = { Text(item) },
                onClick = {
                  onItemClick(item)
                  onExpandedChange(false)
                })
          }
        }
  }
}

/**
 * A function that handles the navigation to the folder content screen by using the screen
 * navigation stack.
 *
 * @param folder The folder to navigate to. If it is not a root folder, its parent id will be pushed
 *   to the screen navigation stack to properly go back.
 * @param navigationActions The navigation instance used to navigate between different screens.
 */
fun navigateToFolderContents(folder: Folder, navigationActions: NavigationActions) {
  if (folder.parentFolderId == null) {
    // Don't add to the screen navigation stack as we are at the root folder
    navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
  } else {
    val poppedId = navigationActions.popFromScreenNavigationStack()
    if (poppedId == Screen.SEARCH) {
      // If we come from search, don't push the folderId to the stack
      navigationActions.pushToScreenNavigationStack(poppedId)
    } else {
      if (poppedId != null) {
        navigationActions.pushToScreenNavigationStack(poppedId)
      }
      // Add the previously visited folder Id (parent) to the screen navigation stack
      navigationActions.pushToScreenNavigationStack(folder.parentFolderId)
    }
    navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
  }
}
