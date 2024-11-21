package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations

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
        if (noteViewModel.selectedNote.value?.folderId != null) {
          navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
        } else {
          navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
        }
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
