package com.github.onlynotesswent.ui

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
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
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
 * @param noteViewModel The ViewModel that provides the list of notes to display.
 * @param showDialog A boolean indicating whether the move out dialog should be displayed.
 * @param navigationActions The navigation instance used to transition between different screens.
 * @param onClick The lambda function to be invoked when the note card is clicked.
 */
@Composable
fun NoteItem(
    note: Note,
    noteViewModel: NoteViewModel,
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
                // Move out will move the given note to the overview menu
                noteViewModel.updateNote(note.copy(folderId = null), note.userId)
                showMoveOutDialog = false
                // Clear the screen navigation stack as we navigate to the overview screen
                navigationActions.clearScreenNavigationStack()
                navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
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
 * Dialog that allows the user to create a folder.
 *
 * @param onDismiss callback to be invoked when the dialog is dismissed
 * @param onConfirm callback to be invoked when the user confirms the new name
 */
@Composable
fun CreateFolderDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {

  var name by remember { mutableStateOf("") }

  AlertDialog(
      modifier = Modifier.testTag("createFolderDialog"),
      onDismissRequest = onDismiss,
      title = {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Text("Create folder")
        }
      },
      text = {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Folder Name") },
            modifier = Modifier.testTag("inputFolderName"))
      },
      confirmButton = {
        Button(
            enabled = name.isNotEmpty(),
            onClick = { onConfirm(name) },
            modifier = Modifier.testTag("confirmFolderCreation")) {
              Text("Create")
            }
      },
      dismissButton = {
        Button(onClick = onDismiss, modifier = Modifier.testTag("dismissFolderCreation")) {
          Text("Cancel")
        }
      })
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
                // Add the folder ID to the screen navigation stack
                navigationActions.pushToScreenNavigationStack(folders.value[index].id)
                navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
              }
            }
            items(notes.value.size) { index ->
              NoteItem(
                  note = notes.value[index],
                  noteViewModel = noteViewModel,
                  showDialog = false,
                  navigationActions = navigationActions) {
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
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
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
) {
  Box(modifier = Modifier.fillMaxWidth()) {
    Button(
        onClick = { onExpandedChange(!expanded) },
        modifier = Modifier.fillMaxWidth().testTag(buttonTag)) {
          Text(text = value)
          Icon(Icons.Outlined.ArrowDropDown, "Dropdown icon")
        }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        modifier = modifier.fillMaxWidth().testTag(menuTag)) {
          items.forEach { item ->
            DropdownMenuItem(
                text = { Text(item) },
                onClick = {
                  onItemClick(item)
                  onExpandedChange(false)
                })
          }
        }
  }
}
