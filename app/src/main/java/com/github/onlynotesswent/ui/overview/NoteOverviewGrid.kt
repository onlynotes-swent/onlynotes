package com.github.onlynotesswent.ui.overview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomSeparatedLazyGrid
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.utils.NotesToFlashcard

/**
 * Displays the overview screen in a grid layout. If there are no notes or folders, it shows a text
 * to the user indicating that there are no notes or folders. It also provides a button to refresh
 * the list of notes and folders.
 *
 * @param paddingValues The padding values to apply to the grid layout.
 * @param userNotes The list of notes to display.
 * @param userFolders The list of folders to display.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param notesToFlashcard The notes to flashcard object to be passed to the note item.
 */
@Composable
fun NoteOverviewScreenGrid(
    paddingValues: PaddingValues,
    userNotes: State<List<Note>>,
    userFolders: State<List<Folder>>,
    folderViewModel: FolderViewModel,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    navigationActions: NavigationActions,
    notesToFlashcard: NotesToFlashcard,
) {
  CustomSeparatedLazyGrid(
      modifier = Modifier.fillMaxSize(),
      notes = userNotes,
      folders = userFolders,
      gridModifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 20.dp)
              .padding(paddingValues)
              .testTag("noteAndFolderList"),
      folderViewModel = folderViewModel,
      noteViewModel = noteViewModel,
      userViewModel = userViewModel,
      navigationActions = navigationActions,
      paddingValues = paddingValues,
      notesToFlashcard = notesToFlashcard,
      columnContent = {
        Text(
            modifier = Modifier.testTag("emptyNoteAndFolderPrompt"),
            text = stringResource(R.string.you_have_no_notes_or_folders_yet),
            color = MaterialTheme.colorScheme.onBackground)
      },
      notesToFlashcard = notesToFlashcard)
}
