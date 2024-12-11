package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions

/**
 * Composable function to display the top bar for the Edit Note complementary screens.
 *
 * @param title The title of the screen.
 * @param titleTestTag The test tag for the title.
 * @param noteViewModel The NoteViewModel to access the selected note.
 * @param navigationActions The NavigationActions object to navigate between screens.
 * @param onClick The action to perform when the back button is clicked.
 */
@Composable
fun EditNoteTopBar(
    title: String,
    titleTestTag: String,
    noteViewModel: NoteViewModel,
    navigationActions: NavigationActions,
    onClick: () -> Unit = {},
) {
  ScreenTopBar(
      title = title,
      titleTestTag = titleTestTag,
      onBackClick = {
        onClick()
        // Unselects the note and navigates back to the previous screen
        navigationActions.goBack()
        noteViewModel.selectedNote(null)
      },
      icon = {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Exit Edit Note",
            tint = MaterialTheme.colorScheme.onSurface)
      },
      iconTestTag = "closeButton")
}
