package com.github.onlynotesswent.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Composable function to display a delete popup dialog.
 *
 * @param title The title of the dialog.
 * @param text The text of the dialog.
 * @param onConfirm The action to perform when the user confirms the deletion.
 * @param onDismiss The action to perform when the user dismisses the dialog.
 */
@Composable
fun DeletePopup(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
      modifier = Modifier.testTag("deletePopup"),
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
            modifier = Modifier.testTag("deleteButton"),
            onClick = {
              // Call the confirm action
              onConfirm()
            }) {
              Text(text = "Delete", color = MaterialTheme.colorScheme.error)
            }
      },
      dismissButton = {
        TextButton(
            modifier = Modifier.testTag("cancelButton"),
            onClick = {
              // Call the dismissal action
              onDismiss()
            }) {
              Text(text = "Cancel")
            }
      })
}
