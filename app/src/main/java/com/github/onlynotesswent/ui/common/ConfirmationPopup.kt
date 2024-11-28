package com.github.onlynotesswent.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.github.onlynotesswent.R

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
