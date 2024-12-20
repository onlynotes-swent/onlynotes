package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.overview.editnote.EditNoteNavigationMenu

/**
 * Composable function to display the bottom navigation bar with a divider.
 *
 * @param navigationActions The NavigationActions object to navigate between screens.
 */
@Composable
fun BottomNavigationBarWithDivider(navigationActions: NavigationActions) {
  Column {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 0.5.dp)

    BottomNavigationMenu(
        onTabSelect = { route -> navigationActions.navigateTo(route) },
        tabList = LIST_TOP_LEVEL_DESTINATION,
        selectedItem = navigationActions.currentRoute())
  }
}

/**
 * Composable function to display the bottom navigation bar with a divider for the edit note screen.
 *
 * @param navigationActions The NavigationActions object to navigate between screens.
 * @param selectedItem The selected item in the navigation bar.
 * @param isModified The flag to indicate if the note is modified.
 * @param onClick The callback to be invoked when the navigation item is clicked.
 */
@Composable
fun BottomEditNoteNavigationBarWithDivider(
    navigationActions: NavigationActions,
    selectedItem: String,
    isModified: Boolean = false,
    onClick: () -> Unit = {}
) {
  Column {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 0.5.dp)

    EditNoteNavigationMenu(
        navigationActions = navigationActions,
        selectedItem = selectedItem,
        onClick = onClick,
        isModified = isModified)
  }
}
