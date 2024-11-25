package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
              Text(text = "Yes", color = MaterialTheme.colorScheme.error)
            }
      },
      dismissButton = {
        TextButton(
            modifier = Modifier.testTag("cancelButton"),
            onClick = {
              // Call the dismissal action
              onDismiss()
            }) {
              Text(text = "No")
            }
      })
}

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
  var expandedVisibility by remember { mutableStateOf(false) }

  androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
      Column(
          modifier = Modifier.padding(16.dp).testTag("${type}Dialog"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(0.92f),
                horizontalArrangement = Arrangement.Start) {
                  Text("$action Note", style = MaterialTheme.typography.titleLarge)
                }
            OutlinedTextField(
                value = name,
                onValueChange = { name = Folder.formatName(it) },
                label = { Text("${type} Name") },
                modifier = Modifier.testTag("input${type}Name"))
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
              Button(onClick = onDismiss, modifier = Modifier.testTag("dismiss${type}Action")) {
                Text("Cancel")
              }
              Button(
                  enabled = name.isNotEmpty() && visibility != null,
                  onClick = { onConfirm(name, visibility ?: Visibility.DEFAULT) },
                  modifier = Modifier.testTag("confirm${type}Action")) {
                    Text(action)
                  }
            }
          }
    }
  }
}
