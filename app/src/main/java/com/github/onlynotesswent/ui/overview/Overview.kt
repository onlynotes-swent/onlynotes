package com.github.onlynotesswent.ui.overview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.MainActivity
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomDropDownMenu
import com.github.onlynotesswent.ui.common.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.common.FolderDialog
import com.github.onlynotesswent.ui.common.NoteDialog
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.overview.editnote.NoteOverviewScreen
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

/**
 * A [NestedScrollConnection] that allows the [HorizontalPager] to be nested in a vertical scroll.
 */
class PagerNestedScrollConnection : NestedScrollConnection

/**
 * Displays the overview screen which contains a list of publicNotes retrieved from the ViewModel.
 * If there are no publicNotes, it shows a text to the user indicating no publicNotes are available.
 * It also provides a floating action button to add a new note.
 *
 * @param navigationActions The navigationActions instance used to transition between different
 *   screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to display.
 * @param userViewModel The ViewModel that provides the current user.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 */
@Composable
fun OverviewScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel
) {
  val context = LocalContext.current
  // Handle back press
  BackHandler {
    // Move the app to background
    (context as MainActivity).moveTaskToBack(true)
  }

  var expanded by remember { mutableStateOf(false) }
  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showCreateDialog by remember { mutableStateOf(false) }

  val parentFolderId = folderViewModel.parentFolderId.collectAsState()

  val pagerState = rememberPagerState(initialPage = 0) { 2 }
  val coroutineScope = rememberCoroutineScope()
  Scaffold(
      modifier = Modifier.testTag("overviewScreen"),
      floatingActionButton = {
        CreateItemFab(
            expandedFab = expanded,
            onExpandedFabChange = { expanded = it },
            showCreateFolderDialog = { showCreateFolderDialog = it },
            showCreateDialog = { showCreateDialog = it },
            pagerState = pagerState)
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageNestedScrollConnection = PagerNestedScrollConnection()) { page ->
                  // Our page content
                  if (page == 0) {
                    NoteOverviewScreen(
                        navigationActions = navigationActions,
                        noteViewModel = noteViewModel,
                        userViewModel = userViewModel,
                        folderViewModel = folderViewModel,
                        paddingValues = paddingValues)

                    if (showCreateDialog) {
                      NoteDialog(
                          onDismiss = { showCreateDialog = false },
                          onConfirm = { newName, visibility ->
                            val note =
                                Note(
                                    id = noteViewModel.getNewUid(),
                                    title = newName,
                                    date = Timestamp.now(),
                                    visibility = visibility,
                                    userId = userViewModel.currentUser.value!!.uid,
                                    folderId = parentFolderId.value)
                            noteViewModel.addNote(note)
                            noteViewModel.selectedNote(note)
                            showCreateDialog = false
                            navigationActions.navigateTo(Screen.EDIT_NOTE)
                          },
                          action = stringResource(R.string.create))
                    }
                  } else {
                    Text("Page 2")
                    if (showCreateDialog) {
                      Text("Deck dialog")
                    }
                  }
                }
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
                      ))
                  showCreateFolderDialog = false
                  navigationActions.navigateTo(
                      Screen.FOLDER_CONTENTS.replace(oldValue = "{folderId}", newValue = folderId))
                },
                action = stringResource(R.string.create))
          }
          Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
              horizontalArrangement = Arrangement.Center,
          ) {
            repeat(pagerState.pageCount) { iteration ->
              val color =
                  if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
              Box(
                  modifier =
                      Modifier.padding(2.dp)
                          .clip(CircleShape)
                          .background(color)
                          .size(10.dp)
                          .clickable {
                            // Animate to the selected page when clicked
                            coroutineScope.launch { pagerState.animateScrollToPage(iteration) }
                          })
            }
          }
        }
      }
}

/**
 * Displays a floating action button that expands to show options to create a note or a folder.
 *
 * @param expandedFab The state of the floating action button. True if the button is expanded, false
 *   otherwise.
 * @param onExpandedFabChange The callback to change the state of the floating action button.
 * @param showCreateFolderDialog The callback to show the dialog to create a folder.
 */
@Composable
fun CreateItemFab(
    expandedFab: Boolean,
    onExpandedFabChange: (Boolean) -> Unit,
    showCreateFolderDialog: (Boolean) -> Unit,
    showCreateDialog: (Boolean) -> Unit,
    pagerState: PagerState
) {
  CustomDropDownMenu(
      modifier = Modifier.testTag("createNoteOrFolder"),
      menuItems =
          listOf(
              CustomDropDownMenuItem(
                  text = {
                    if (pagerState.currentPage == 0) {
                      Text(stringResource(R.string.create_note))
                    } else {
                      Text(stringResource(R.string.create_deck))
                    }
                  },
                  icon = {
                    if (pagerState.currentPage == 0) {
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
                    showCreateDialog(true)
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
