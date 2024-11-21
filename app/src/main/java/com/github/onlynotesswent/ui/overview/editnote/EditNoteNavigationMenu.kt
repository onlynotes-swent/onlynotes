package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.onlynotesswent.ui.navigation.LIST_EDIT_NOTE_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions

@Composable
fun EditNoteNavigationMenu(
    navigationActions: NavigationActions,
    selectedItem: String,
    onClick: () -> Unit = {},
    isModified: Boolean = false,
) {
  var showDiscardChangesDialog by remember { mutableStateOf(false) }
  var navigateTo by remember { mutableStateOf<String?>(null) }

  NavigationBar(
      modifier = Modifier.testTag("bottomNavigationMenu"),
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.onSurface) {
        LIST_EDIT_NOTE_DESTINATION.forEach { tab ->
          val isSelected = selectedItem == tab.textId
          NavigationBarItem(
              selected = isSelected,
              onClick = {
                if (isModified) {
                  showDiscardChangesDialog = true
                  navigateTo = tab.screen
                } else {
                  tab.screen?.let { navigationActions.navigateTo(it) }
                  onClick()
                }
              },
              modifier = Modifier.testTag(tab.textId),
              icon = {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.textId,
                    tint =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface)
              },
              label = {
                Text(
                    text = tab.textId,
                    color =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface)
              })
        }
      }

  // Discard Changes Dialog
  if (showDiscardChangesDialog) {
    AlertDialog(
        modifier = Modifier.testTag("discardChangesDialog"),
        onDismissRequest = { showDiscardChangesDialog = false },
        title = { Text("Discard Changes?") },
        text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
        confirmButton = {
          TextButton(
              modifier = Modifier.testTag("discardChangesButton"),
              onClick = {
                // Discard changes and navigate away
                showDiscardChangesDialog = false
                navigateTo?.let { navigationActions.navigateTo(it) }
                onClick()
              }) {
                Text("Discard")
              }
        },
        dismissButton = {
          TextButton(
              modifier = Modifier.testTag("cancelDiscardChangesButton"),
              onClick = {
                // Stay on the page
                showDiscardChangesDialog = false
              }) {
                Text("Cancel")
              }
        })
  }
}
