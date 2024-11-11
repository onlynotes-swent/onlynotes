package com.github.onlynotesswent.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.overview.FolderItem
import com.github.onlynotesswent.ui.overview.NoteItem

/**
 * Displays the search screen where users can search notes by title.
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to search from.
 */
@Composable
fun SearchScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel
) {
  var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
  val searchType = remember { mutableStateOf(SearchType.NOTES) }

  val notes = noteViewModel.publicNotes.collectAsState()
  val filteredNotes = notes.value.filter { it.title.contains(searchQuery.text, ignoreCase = true) }

  val users = userViewModel.allUsers.collectAsState()
  val filteredUsers =
      users.value.filter {
        it.fullName().contains(searchQuery.text, ignoreCase = true) ||
            it.userHandle().contains(searchQuery.text, ignoreCase = true)
      }

  val folders = folderViewModel.publicFolders.collectAsState()
  val filteredFolders =
      folders.value.filter { it.name.contains(searchQuery.text, ignoreCase = true) }

  Scaffold(
      modifier = Modifier.testTag("searchScreen"),
      topBar = {
        Column {
          OutlinedTextField(
              value = searchQuery,
              onValueChange = {
                searchQuery = it
                noteViewModel.getPublicNotes()
                userViewModel.getAllUsers()
              },
              placeholder = { Text("Search ...") },
              leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = MaterialTheme.colorScheme.onBackground)
              },
              shape = RoundedCornerShape(50),
              singleLine = true,
              colors =
                  OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                      unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                      focusedTextColor = MaterialTheme.colorScheme.onBackground,
                      unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                      .testTag("searchTextField"))
          Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 20.dp)) {
            FilterChip(
                searchType.value == SearchType.USERS,
                label = { Text("Users") },
                onClick = {
                  searchType.value = SearchType.USERS
                  userViewModel.getAllUsers()
                },
                leadingIcon = {
                  if (searchType.value == SearchType.USERS)
                      Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = "Chip Icon",
                          tint = MaterialTheme.colorScheme.onBackground)
                },
                modifier = Modifier.padding(horizontal = 10.dp).testTag("userFilterChip"))
            FilterChip(
                searchType.value == SearchType.NOTES,
                label = { Text("Notes") },
                onClick = {
                  searchType.value = SearchType.NOTES
                  noteViewModel.getPublicNotes()
                },
                leadingIcon = {
                  if (searchType.value == SearchType.NOTES)
                      Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = "Chip Icon",
                          tint = MaterialTheme.colorScheme.onBackground)
                },
                modifier = Modifier.padding(horizontal = 5.dp).testTag("noteFilterChip"))
            FilterChip(
                searchType.value == SearchType.FOLDERS,
                label = { Text("Folders") },
                onClick = {
                  searchType.value = SearchType.FOLDERS
                  folderViewModel.getPublicFolders()
                },
                leadingIcon = {
                  if (searchType.value == SearchType.FOLDERS)
                      Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = "Chip Icon",
                          tint = MaterialTheme.colorScheme.onBackground)
                },
                modifier = Modifier.padding(horizontal = 10.dp).testTag("folderFilterChip"))
          }
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { padding ->

        // To skip large nested if-else blocks, we can use a boolean to determine which list to
        // display.
        val displayUsers: Boolean =
            searchQuery.text.isNotBlank() &&
                searchType.value == SearchType.USERS &&
                filteredUsers.isNotEmpty()
        val displayNotes: Boolean =
            searchQuery.text.isNotBlank() &&
                searchType.value == SearchType.NOTES &&
                filteredNotes.isNotEmpty()

        val displayFolders: Boolean =
            searchQuery.text.isNotBlank() &&
                searchType.value == SearchType.FOLDERS &&
                filteredFolders.isNotEmpty()

        val displayLoader: Boolean =
            searchQuery.text.isNotBlank() &&
                ((searchType.value == SearchType.USERS && filteredUsers.isEmpty()) ||
                    (searchType.value == SearchType.NOTES && filteredNotes.isEmpty()) ||
                    (searchType.value == SearchType.FOLDERS && filteredFolders.isEmpty()))

        if (displayNotes) {
          LazyColumn(
              contentPadding = PaddingValues(vertical = 40.dp),
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 16.dp)
                      .padding(padding)
                      .testTag("filteredNoteList")) {
                items(filteredNotes.size) { index ->
                  NoteItem(note = filteredNotes[index]) {
                    noteViewModel.selectedNote(filteredNotes[index])
                    navigationActions.navigateTo(Screen.EDIT_NOTE)
                  }
                }
              }
        }
        if (displayUsers) {
          LazyColumn(
              contentPadding = PaddingValues(vertical = 40.dp),
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 16.dp)
                      .padding(padding)
                      .testTag("filteredUserList")) {
                items(filteredUsers.size) { index ->
                  UserItem(filteredUsers[index]) {
                    userViewModel.setProfileUser(filteredUsers[index])
                    navigationActions.navigateTo(Screen.PUBLIC_PROFILE)
                  }
                }
              }
        }

        if (displayFolders) {
          LazyColumn(
              contentPadding = PaddingValues(vertical = 40.dp),
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 16.dp)
                      .padding(padding)
                      .testTag("filteredFolderList")) {
                items(filteredFolders.size) { index ->
                  FolderItem(filteredFolders[index]) {
                    folderViewModel.selectedFolder(filteredFolders[index])
                    navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
                  }
                }
              }
        }

        if (displayLoader) {
          val searchedText =
              when (searchType.value) {
                SearchType.USERS -> "users"
                SearchType.NOTES -> "notes"
                SearchType.FOLDERS -> "folders"
              }
          Text(
              modifier =
                  Modifier.padding(padding)
                      .fillMaxWidth()
                      .padding(24.dp)
                      .testTag("noSearchResults"),
              text = "No $searchedText found matching your search.",
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onBackground)
        }
      }
}

@Composable
fun UserItem(user: User, onClick: () -> Unit) {
  Card(
      modifier =
          Modifier.testTag("userCard")
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable(onClick = onClick),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Member since ${user.dateToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
              }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
              text = user.fullName(),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text(
              text = user.userHandle(),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
      }
}

enum class SearchType {
  USERS,
  NOTES,
  FOLDERS
}
