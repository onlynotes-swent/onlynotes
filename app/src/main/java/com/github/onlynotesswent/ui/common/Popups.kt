package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
 */
@Composable
fun CreationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Visibility) -> Unit,
    action: String,
    oldVisibility: Visibility? = Visibility.PRIVATE,
    oldName: String = "",
    type: String
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
              SelectVisibility(visibility) { visibility = it }
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


@Composable
fun FileSystemPopup(
    onDismiss: () -> Unit,
    folderViewModel: FolderViewModel
) {
    var selectedFolder by remember { mutableStateOf<Folder?>(folderViewModel.selectedFolder.value) }
    val folderSubFolders = folderViewModel.folderSubFolders.collectAsState()
    val userRootFolders = folderViewModel.userRootFolders.collectAsState()
    val initialFolder = remember { folderViewModel.selectedFolder.value }


        Dialog(onDismissRequest = {
            onDismiss()}) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Adjust the popup width
                    .fillMaxHeight(0.5f)
                    .padding(16.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedFolder == null) {
                        Text(
                            text = "Contents of root folder",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        userRootFolders.value.forEach() { folder ->
                            Text(
                                text = folder.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        // Set the clicked folder as the selected folder
                                        selectedFolder = folder
                                        folderViewModel.getSubFoldersOf(folder.id)
                                    }
                            )
                        }
                    } else {
                        // Display all folders in the selected folder
                        Text(
                            text = "Move selected item into: ${selectedFolder!!.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        folderSubFolders.value.forEach { subFolder ->
                            Text(
                                text = subFolder.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        // Navigate into the sub-folder
                                        selectedFolder = subFolder
                                        folderViewModel.getSubFoldersOf(subFolder.id)
                                    }
                            )
                        }

                        // Add a button to go back to the parent folder or root
                        Spacer(modifier = Modifier.height(16.dp))
                        ElevatedButton(
                            onClick = {
                                // Reset to the root folder list
                                selectedFolder = null
                            }
                        ) {
                            Text("Back to Root Folders")
                        }
                    }
                }
            }
        }

    }