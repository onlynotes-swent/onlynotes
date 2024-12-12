package com.github.onlynotesswent.ui.overview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.github.onlynotesswent.R
import com.github.onlynotesswent.ui.common.CustomDropDownMenu
import com.github.onlynotesswent.ui.common.CustomDropDownMenuItem

/**
 * Displays a floating action button that expands to show options to create a note or a folder.
 *
 * @param expandedFab The state of the floating action button. True if the button is expanded, false
 *   otherwise.
 * @param onExpandedFabChange The callback to change the state of the floating action button.
 * @param showCreateFolderDialog The callback to show the dialog to create a folder.
 * @param showCreateItemDialog The callback to show the dialog to create a note or a deck.
 * @param isDeckView True if the item to be created is a deck, false otherwise.
 */
@Composable
fun CreateItemFab(
    expandedFab: Boolean,
    onExpandedFabChange: (Boolean) -> Unit,
    showCreateFolderDialog: (Boolean) -> Unit,
    showCreateItemDialog: (Boolean) -> Unit,
    isDeckView: Boolean = false,
) {
  CustomDropDownMenu(
      modifier = Modifier.testTag("createNoteOrFolder"),
      menuItems =
          listOf(
              CustomDropDownMenuItem(
                  text = {
                    if (!isDeckView) {
                      Text(stringResource(R.string.create_note))
                    } else {
                      Text(stringResource(R.string.create_deck))
                    }
                  },
                  icon = {
                    if (!isDeckView) {
                      Icon(
                          painter = painterResource(id = R.drawable.add_note_icon),
                          contentDescription = "AddNote")
                    } else {
                      Icon(
                          imageVector = Icons.Default.LibraryAdd, contentDescription = "createDeck")
                    }
                  },
                  onClick = {
                    onExpandedFabChange(false)
                    showCreateItemDialog(true)
                  },
                  modifier = Modifier.testTag("createNote")),
              CustomDropDownMenuItem(
                  text = { Text(stringResource(R.string.create_folder)) },
                  icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.folder_create_icon),
                        contentDescription = "createFolder")
                  },
                  onClick = {
                    onExpandedFabChange(false)
                    showCreateFolderDialog(true)
                  },
                  modifier = Modifier.testTag("createFolder"))),
      fabIcon = { Icon(imageVector = Icons.Default.Add, contentDescription = "AddNote") },
      expanded = expandedFab,
      onFabClick = { onExpandedFabChange(true) },
      onDismissRequest = { onExpandedFabChange(false) })
}
