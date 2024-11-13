package com.github.onlynotesswent.ui.overview

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.CreateFolderDialog
import com.github.onlynotesswent.ui.CustomDropDownMenu
import com.github.onlynotesswent.ui.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.CustomLazyGrid
import com.github.onlynotesswent.ui.RefreshButton
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations

/**
 * Displays the overview screen which contains a list of publicNotes retrieved from the ViewModel.
 * If there are no publicNotes, it shows a text to the user indicating no publicNotes are available.
 * It also provides a floating action button to add a new note.
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to display.
 */
@Composable
fun OverviewScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel
) {
  val userRootNotes = noteViewModel.userRootNotes.collectAsState()
  userViewModel.currentUser.collectAsState().value?.let { noteViewModel.getRootNotesFrom(it.uid) }

  val userRootFolders = folderViewModel.userRootFolders.collectAsState()
  userViewModel.currentUser.collectAsState().value?.let {
    folderViewModel.getRootFoldersFromUid(it.uid)
  }

  val parentFolderId = folderViewModel.parentFolderId.collectAsState()

  var expanded by remember { mutableStateOf(false) }
  var showCreateDialog by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag("overviewScreen"),
      floatingActionButton = {
        CustomDropDownMenu(
            modifier = Modifier.testTag("createNoteOrFolder"),
            menuItems =
                listOf(
                    CustomDropDownMenuItem(
                        text = { Text("Create note") },
                        icon = {
                          Icon(
                              painter = painterResource(id = R.drawable.add_note_icon),
                              contentDescription = "AddNote")
                        },
                        onClick = {
                          expanded = false
                          navigationActions.navigateTo(Screen.ADD_NOTE)
                          noteViewModel.selectedFolderId(null)
                        },
                        modifier = Modifier.testTag("createNote")),
                    CustomDropDownMenuItem(
                        text = { Text("Create folder") },
                        icon = {
                          Icon(
                              painter = painterResource(id = R.drawable.folder_create_icon),
                              contentDescription = "createFolder")
                        },
                        onClick = {
                          expanded = false
                          showCreateDialog = true
                          folderViewModel.selectedParentFolderId(null)
                        },
                        modifier = Modifier.testTag("createFolder"))),
            fabIcon = { Icon(imageVector = Icons.Default.Add, contentDescription = "AddNote") },
            expanded = expanded,
            onFabClick = { expanded = true },
            onDismissRequest = { expanded = false })
        // Logic to show the dialog to create a folder
        if (showCreateDialog) {
          CreateFolderDialog(
              onDismiss = { showCreateDialog = false },
              onConfirm = { newName ->
                folderViewModel.addFolder(
                    Folder(
                        id = folderViewModel.getNewFolderId(),
                        name = newName,
                        userId = userViewModel.currentUser.value!!.uid,
                        parentFolderId = parentFolderId.value),
                    userViewModel.currentUser.value!!.uid)
                showCreateDialog = false
                if (parentFolderId.value != null) {
                  navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
                } else {
                  navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                }
              })
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { paddingValues ->
        CustomLazyGrid(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            notes = userRootNotes,
            folders = userRootFolders,
            gridModifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(paddingValues)
                    .testTag("noteAndFolderList"),
            folderViewModel = folderViewModel,
            noteViewModel = noteViewModel,
            navigationActions = navigationActions,
            paddingValues = paddingValues,
            columnContent = {
              Text(
                  modifier = Modifier.testTag("emptyNoteAndFolderPrompt"),
                  text = "You have no notes or folders yet.",
                  color = MaterialTheme.colorScheme.onBackground)
              Spacer(modifier = Modifier.height(50.dp))
              RefreshButton {
                userViewModel.currentUser.value?.let { noteViewModel.getRootNotesFrom(it.uid) }
                userViewModel.currentUser.value?.let {
                  folderViewModel.getRootFoldersFromUid(it.uid)
                }
              }
              Spacer(modifier = Modifier.height(20.dp))
            })
      }
}
