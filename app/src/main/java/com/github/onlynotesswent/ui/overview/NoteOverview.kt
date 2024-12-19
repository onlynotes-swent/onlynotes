package com.github.onlynotesswent.ui.overview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.OnlyNotes
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.FolderDialog
import com.github.onlynotesswent.ui.common.NoteDialog
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.NotesToFlashcard
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

/**
 * Displays the overview screen which contains a list of the user's notes and folders. The user can
 * create new notes and folders from this screen. If there are no notes or folders, it shows a text
 * to the user indicating that there are no notes or folders.
 *
 * @param navigationActions The navigationActions instance used to transition between different
 *   screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 * @param notesToFlashcard The notes to flashcard object to be passed to the note item.
 */
@Composable
fun NoteOverviewScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel,
    notesToFlashcard: NotesToFlashcard
) {
  val userRootNotes = noteViewModel.userRootNotes.collectAsState()
  val userSavedNotes = noteViewModel.userSavedNotes.collectAsState()
  val userRootFolders = folderViewModel.userRootFolders.collectAsState()
  val userSavedFolders = folderViewModel.userSavedFolders.collectAsState()

  val parentFolderId = folderViewModel.parentFolderId.collectAsState()
  val context = LocalContext.current

  var expanded by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showCreateNoteDialog by remember { mutableStateOf(false) }

  val pageLabels = listOf("Your notes", "Saved")
  val pagerState = rememberPagerState(initialPage = 0) { pageLabels.size }
  val coroutineScope = rememberCoroutineScope()

  // Handle back press
  BackHandler {
    // Move the app to background
    (context as OnlyNotes).moveTaskToBack(true)
  }

  Scaffold(
      modifier = Modifier.testTag("overviewScreen"),
      floatingActionButton = {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp), // Space between FABs
            horizontalAlignment = Alignment.End) {
              CreateItemFab(
                  expandedFab = expanded,
                  onExpandedFabChange = { expanded = it },
                  showCreateFolderDialog = { showCreateFolderDialog = it },
                  showCreateItemDialog = { showCreateNoteDialog = it })
            }
      },
      bottomBar = {
        Column {
          HorizontalDivider(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 0.5.dp)
          BottomNavigationMenu(
              onTabSelect = { route -> navigationActions.navigateTo(route) },
              tabList = LIST_TOP_LEVEL_DESTINATION,
              selectedItem = navigationActions.currentRoute())
        }
        Column {
          HorizontalDivider(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 0.5.dp)
          BottomNavigationMenu(
              onTabSelect = { route -> navigationActions.navigateTo(route) },
              tabList = LIST_TOP_LEVEL_DESTINATION,
              selectedItem = navigationActions.currentRoute())
        }
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          SingleChoiceSegmentedButtonRow(
              modifier =
                  Modifier.fillMaxWidth(fraction = 0.8f).align(Alignment.CenterHorizontally)) {
                pageLabels.forEachIndexed { index, label ->
                  SegmentedButton(
                      selected = pagerState.currentPage == index,
                      shape =
                          SegmentedButtonDefaults.itemShape(
                              index = index,
                              count = pageLabels.size,
                              baseShape = RoundedCornerShape(10)),
                      border = ButtonDefaults.outlinedButtonBorder(false),
                      colors =
                          SegmentedButtonDefaults.colors(
                              activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                              inactiveContainerColor = MaterialTheme.colorScheme.surface),
                      label = { Text(label) },
                      onClick = {
                        // Animate to the selected page when clicked
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                      })
                }
              }
          HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {
            when (it) {
              0 -> {
                NoteOverviewScreenGrid(
                    paddingValues = paddingValues,
                    userNotes = userRootNotes,
                    userFolders = userRootFolders,
                    folderViewModel = folderViewModel,
                    noteViewModel = noteViewModel,
                    userViewModel = userViewModel,
                    navigationActions = navigationActions,
                    notesToFlashcard = notesToFlashcard)

                if (showCreateNoteDialog) {
                  NoteDialog(
                      onDismiss = { showCreateNoteDialog = false },
                      onConfirm = { newName, visibility ->
                        val note =
                            Note(
                                id = noteViewModel.getNewUid(),
                                title = newName,
                                date = Timestamp.now(),
                                lastModified = Timestamp.now(),
                                visibility = visibility,
                                userId = userViewModel.currentUser.value!!.uid,
                                folderId = parentFolderId.value)
                        noteViewModel.addNote(note)
                        noteViewModel.selectedNote(note)
                        showCreateNoteDialog = false
                        navigationActions.navigateTo(Screen.EDIT_NOTE)
                      },
                      action = stringResource(R.string.create))
                }

                // Logic to show the dialog to create a folder
                if (showCreateFolderDialog) {
                  FolderDialog(
                      onDismiss = { showCreateFolderDialog = false },
                      onConfirm = { newName, visibility ->
                        val folderId = folderViewModel.getNewFolderId()
                        folderViewModel.addFolder(
                            Folder(
                                id = folderId,
                                name = newName,
                                userId = userViewModel.currentUser.value!!.uid,
                                parentFolderId = parentFolderId.value,
                                visibility = visibility,
                                lastModified = Timestamp.now()))

                        showCreateFolderDialog = false
                        navigationActions.navigateTo(
                            Screen.FOLDER_CONTENTS.replace(
                                oldValue = "{folderId}", newValue = folderId))
                      },
                      action = stringResource(R.string.create))
                }
              }
              1 -> {
                // Saved documents page
                NoteOverviewScreenGrid(
                    paddingValues = paddingValues,
                    userNotes = userSavedNotes,
                    userFolders = userSavedFolders,
                    folderViewModel = folderViewModel,
                    noteViewModel = noteViewModel,
                    userViewModel = userViewModel,
                    navigationActions = navigationActions)
              }
            }
          }
        }
      }
}
