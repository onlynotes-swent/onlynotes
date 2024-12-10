package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Custom dropdown menu that displays a floating action button with a dropdown menu. The dropdown
 * menu contains two items, each with its own text and onClick action.
 *
 * @param modifier The modifier for the floating action button.
 * @param menuItems The list of dropdown menu items to be displayed in the dropdown menu.
 * @param fabIcon The icon to be displayed on the floating action button.
 * @param expanded The state of the dropdown menu.
 * @param onFabClick The action to be invoked when the floating action button is clicked.
 * @param onDismissRequest The action to be invoked when the dropdown menu is dismissed.
 */
@Composable
fun CustomDropDownMenu(
    modifier: Modifier,
    menuItems: List<CustomDropDownMenuItem>,
    fabIcon: @Composable () -> Unit,
    expanded: Boolean,
    onFabClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
  Box {
    FloatingActionButton(onClick = onFabClick, modifier = modifier) { fabIcon() }
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
      menuItems.forEach { item ->
        DropdownMenuItem(
            text = item.text,
            leadingIcon = item.icon,
            onClick = item.onClick,
            modifier = item.modifier)
      }
    }
  }
}

/**
 * Custom dropdown menu item that contains a text and an onClick action.
 *
 * @param text The text to be displayed in the dropdown menu item.
 * @param icon The icon to be displayed in the dropdown menu item.
 * @param onClick The action to be invoked when the dropdown menu item is clicked.
 * @param modifier The modifier for the dropdown menu item.
 */
data class CustomDropDownMenuItem(
    val text: @Composable () -> Unit,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit,
    val modifier: Modifier = Modifier
)

/**
 * A composable function that displays an `OutlinedTextField` with a dropdown menu.
 *
 * @param expanded A boolean indicating whether the dropdown menu is expanded.
 * @param value The current value of the text field.
 * @param buttonTag The test tag for the button.
 * @param menuTag The test tag for the dropdown menu.
 * @param onExpandedChange A callback to be invoked when the expanded state of the dropdown menu
 *   changes.
 * @param items A list of strings representing the items to be displayed in the dropdown menu.
 * @param onItemClick A callback to be invoked when an item in the dropdown menu is clicked.
 * @param modifier The modifier to be applied to the `OutlinedTextField`.
 * @param widthFactor The width factor of the `BoxWithConstraints`.
 */
@Composable
fun OptionDropDownMenu(
    expanded: Boolean,
    value: String,
    buttonTag: String,
    menuTag: String,
    onExpandedChange: (Boolean) -> Unit,
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    widthFactor: Float = 0.8f
) {
  BoxWithConstraints(modifier = Modifier.fillMaxWidth(widthFactor)) {
    Button(
        onClick = { onExpandedChange(!expanded) },
        modifier = Modifier.width(this.maxWidth).testTag(buttonTag)) {
          Text(text = value)
          Icon(Icons.Outlined.ArrowDropDown, "Dropdown icon")
        }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        modifier = modifier.width(this.maxWidth).testTag(menuTag)) {
          items.forEach { item ->
            DropdownMenuItem(
                modifier = Modifier.testTag("item--$item"),
                text = { Text(item) },
                onClick = {
                  onItemClick(item)
                  onExpandedChange(false)
                })
          }
        }
  }
}
