package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.onlynotesswent.ui.navigation.LIST_EDIT_NOTE_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions

@Composable
fun EditNoteNavigationMenu(navigationActions: NavigationActions, selectedItem: String) {
  NavigationBar(
      modifier = Modifier.testTag("bottomNavigationMenu"),
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.onSurface) {
        LIST_EDIT_NOTE_DESTINATION.forEach { tab ->
          NavigationBarItem(
              selected = selectedItem == tab.textId,
              onClick = { navigationActions.navigateTo(tab.screen) },
              icon = {},
              modifier = Modifier.testTag(tab.textId),
              label = { Text(tab.textId) })
        }
      }
}
