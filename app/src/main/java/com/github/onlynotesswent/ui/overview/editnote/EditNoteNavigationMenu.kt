package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.github.onlynotesswent.R
import com.github.onlynotesswent.ui.common.ConfirmationPopup
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
    ConfirmationPopup(
        title = stringResource(R.string.discard_changes),
        text = stringResource(R.string.discard_changes_text),
        onConfirm = {
          // Discard changes and navigate away
          showDiscardChangesDialog = false
          navigateTo?.let { navigationActions.navigateTo(it) }
          onClick()
        },
        onDismiss = {
          // Stay on the page
          showDiscardChangesDialog = false
        })
  }
}
