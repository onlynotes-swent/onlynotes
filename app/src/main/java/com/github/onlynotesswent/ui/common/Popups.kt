package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel

/**
 * Composable function to display a popup dialog with a title, text, and confirm and dismiss
 * buttons.
 *
 * @param title The title of the dialog.
 * @param text The text of the dialog.
 * @param onConfirm The action to perform when the user confirms the action.
 * @param onDismiss The action to perform when the user dismisses the dialog.
 */
@Composable
fun ConfirmationPopup(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
      modifier = Modifier.testTag("popup"),
      onDismissRequest = {
        // Call the dismissal action when the dialog is dismissed
        onDismiss()
      },
      title = {
        Text(
            text = title,
            style = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface))
      },
      text = {
        Text(
            text = text,
            style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
      },
      confirmButton = {
        TextButton(
            modifier = Modifier.testTag("confirmButton"),
            onClick = {
              // Call the confirm action
              onConfirm()
            }) {
              Text(text = stringResource(R.string.yes), color = MaterialTheme.colorScheme.error)
            }
      },
      dismissButton = {
        TextButton(
            modifier = Modifier.testTag("cancelButton"),
            onClick = {
              // Call the dismissal action
              onDismiss()
            }) {
              Text(text = stringResource(R.string.no))
            }
      })
}

/**
 * Generic dialog for creating or renaming an item, such as a folder or a note.
 *
 * @param onDismiss callback to be invoked when the dialog is dismissed
 * @param onConfirm callback to be invoked when the user confirms the new name and visibility
 * @param action the action to be performed (e.g., "Create" or "Update")
 * @param oldVisibility the previous visibility of the item (if renaming), defaults to
 *   [Visibility.PRIVATE]
 * @param oldName the previous name of the item (if renaming), defaults to an empty string
 * @param type the type of item (e.g., "Folder" or "Note") displayed in the dialog
 * @param currentUserId the Id of the current user
 * @param noteUserId the Id of the user who created the note
 */
@Composable
fun CreationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Visibility) -> Unit,
    action: String,
    oldVisibility: Visibility? = Visibility.PRIVATE,
    oldName: String = "",
    type: String,
    currentUserId: String = "",
    noteUserId: String = ""
) {
  var name by remember { mutableStateOf(oldName) }
  var visibility: Visibility? by remember { mutableStateOf(oldVisibility) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("$action $type") },
      text = {
        Column(
            modifier = Modifier.padding(16.dp).testTag("${type}Dialog"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              OutlinedTextField(
                  value = name,
                  onValueChange = { name = Folder.formatName(it) },
                  label = { Text("$type Name") },
                  modifier = Modifier.testTag("input${type}Name"))

              // Spacing
              Spacer(modifier = Modifier.height(8.dp))
              SelectVisibility(visibility, currentUserId, noteUserId) { visibility = it }
            }
      },
      confirmButton = {
        Button(
            enabled = name.isNotEmpty() && visibility != null,
            onClick = { onConfirm(name, visibility ?: Visibility.DEFAULT) },
            modifier = Modifier.testTag("confirm${type}Action")) {
              Text(action)
            }
      },
      dismissButton = {
        OutlinedButton(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            onClick = onDismiss,
            modifier = Modifier.testTag("dismiss${type}Action")) {
              Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.error)
            }
      })
}

/**
 * A composable function that displays a popup dialog for moving a specific item in the file system.
 *
 * @param onDismiss A callback triggered to dismiss the popup dialog.
 * @param folderViewModel The ViewModel managing folder-related data and operations.
 * @param onMoveHere A callback triggered when the "Move Here" button is clicked, designed to move
 *   the selected item in a specific folder.
 */
@Composable
fun FileSystemPopup(
    onDismiss: () -> Unit,
    folderViewModel: FolderViewModel,
    onMoveHere: (Folder?) -> Unit = {}
) {
  var selectedFolder by remember { mutableStateOf<Folder?>(folderViewModel.selectedFolder.value) }
  var folderSubFolders by remember { mutableStateOf<List<Folder>>(emptyList()) }
  val userRootFolders = folderViewModel.userRootFolders.collectAsState()

  // Modify the subfolder when selected Folder changes, best way I found how to do it as when done
  // sequentially it takes a bit of time for the selected Folder to change which causes a bug
  // where the subfolders don't update. Could fix this problem using a wait but that would depend
  // on the internet speed of the user.
  LaunchedEffect(selectedFolder) {
    if (selectedFolder != null) {
      folderViewModel.getSubFoldersOfNoStateUpdate(
          selectedFolder!!.id, onSuccess = { subFolders -> folderSubFolders = subFolders })
    }
  }
  Dialog(onDismissRequest = { onDismiss() }) {
    Box(
        modifier =
            Modifier.testTag("FileSystemPopup")
                .fillMaxWidth(0.95f) // Adjust the popup width
                .fillMaxHeight(0.7f)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(12.dp))) {
          Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 12.dp, horizontal = 16.dp)) {
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            modifier = Modifier.testTag("goBackFileSystemPopup"),
                            onClick = {
                              if (selectedFolder != null) {
                                if (selectedFolder!!.parentFolderId != null) {
                                  folderViewModel.getFolderByIdNoStateUpdate(
                                      selectedFolder!!.parentFolderId!!,
                                      onSuccess = { parentFolder -> selectedFolder = parentFolder })
                                } else {
                                  selectedFolder = null
                                }
                              }
                            }) {
                              Icon(
                                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                  contentDescription = "Back")
                            }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text =
                                if (selectedFolder == null)
                                    stringResource(R.string.file_system_folders_in_root)
                                else "Folders in: ${selectedFolder!!.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary)

                        Spacer(modifier = Modifier.weight(1f))

                        // Home button at the top right
                        IconButton(
                            modifier = Modifier.testTag("goToOverviewFileSystemPopup"),
                            onClick = { selectedFolder = null }) {
                              Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                            }
                      }
                }

            Column(
                modifier =
                    Modifier.weight(1f)
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  if (selectedFolder == null) {
                    userRootFolders.value.forEach { folder ->
                      Box(
                          modifier =
                              Modifier.testTag("FileSystemPopupFolderChoiceBox" + folder.id)
                                  .fillMaxWidth()
                                  .background(
                                      color = MaterialTheme.colorScheme.surface,
                                      shape = RoundedCornerShape(8.dp))
                                  .clickable {
                                    folderViewModel.getSubFoldersOfNoStateUpdate(
                                        folder.id,
                                        onSuccess = { subFolders -> folderSubFolders = subFolders })
                                    selectedFolder = folder
                                  }
                                  .padding(12.dp)) {
                            Text(
                                modifier = Modifier.testTag("FileSystemPopupFolderChoiceText"),
                                text = folder.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                          }
                    }
                  } else {
                    folderSubFolders.forEach { subFolder ->
                      Box(
                          modifier =
                              Modifier.testTag("FileSystemPopupFolderChoiceBox" + subFolder.id)
                                  .fillMaxWidth()
                                  .background(
                                      color = MaterialTheme.colorScheme.surface,
                                      shape = RoundedCornerShape(8.dp))
                                  .clickable {
                                    folderViewModel.getSubFoldersOfNoStateUpdate(
                                        subFolder.id,
                                        onSuccess = { subFolders -> folderSubFolders = subFolders })
                                    selectedFolder = subFolder
                                  }
                                  .padding(12.dp)) {
                            Text(
                                text = subFolder.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                          }
                    }
                  }
                }
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center) {
                  Button(
                      onClick = {
                        onMoveHere(selectedFolder)
                        onDismiss()
                      },
                      modifier = Modifier.testTag("MoveHereButton")) {
                        Text(text = stringResource(R.string.move_here))
                      }
                }
          }
        }
  }
}

/**
 * Generic dialog for entering text.
 *
 * @param onDismiss callback to be invoked when the dialog is dismissed
 * @param onConfirm callback to be invoked when the user confirms the new text
 * @param formatter function to format the text
 * @param action the action to be performed (eg. "Send")
 * @param type the type of item displayed in the dialog (eg. "Message")
 */
@Composable
fun EnterTextPopup(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    formatter: (String) -> String,
    action: String,
    type: String
) {

  var text by remember { mutableStateOf("") }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("$action $type") },
      text = {
        Column(
            modifier = Modifier.padding(16.dp).testTag("${type}Dialog"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              OutlinedTextField(
                  value = text,
                  onValueChange = { text = formatter(it) },
                  label = { Text(type) },
                  modifier = Modifier.testTag("input${type}"))

              // Spacing
              Spacer(modifier = Modifier.height(8.dp))
            }
      },
      confirmButton = {
        Button(
            enabled = text.isNotEmpty(),
            onClick = { onConfirm(text) },
            modifier = Modifier.testTag("confirm${type}Action")) {
              Text(action)
            }
      },
      dismissButton = {
        OutlinedButton(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            onClick = onDismiss,
            modifier = Modifier.testTag("dismiss${type}Action")) {
              Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.error)
            }
      })
}
