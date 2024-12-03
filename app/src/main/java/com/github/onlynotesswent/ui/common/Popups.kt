package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.folder.Folder

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
