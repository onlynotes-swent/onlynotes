package com.github.onlynotesswent.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.github.onlynotesswent.ui.overview.CustomLazyGrid
import com.github.onlynotesswent.ui.overview.NoteItem
import kotlinx.coroutines.delay

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
  val searchQuery = remember { mutableStateOf("") }
  val searchType = remember { mutableStateOf(SearchType.NOTES) }
  val searchWords = remember { mutableStateOf(emptyList<String>()) }
  searchWords.value = searchQuery.value.split("\\s+".toRegex())

  val notes = noteViewModel.publicNotes.collectAsState()
  val filteredNotes = remember { mutableStateOf(notes.value) }
  filteredNotes.value = notes.value.filter { textMatchesSearch(it.title, searchWords.value) }

  val users = userViewModel.allUsers.collectAsState()
  val filteredUsers = remember { mutableStateOf(users.value) }
  filteredUsers.value =
      users.value.filter {
        textMatchesSearch(it.fullName(), searchWords.value) ||
            textMatchesSearch(it.userHandle(), searchWords.value)
      }

  val folders = folderViewModel.publicFolders.collectAsState()
  val filteredFolders = remember { mutableStateOf(folders.value) }
  filteredFolders.value = folders.value.filter { textMatchesSearch(it.name, searchWords.value) }

  // Refresh the list of notes, users, and folders periodically.
  RefreshPeriodically(searchQuery, noteViewModel, userViewModel, folderViewModel)
  // Refresh the list of notes, users, and folders when the search query is empty,
  // typically when reloading the screen.
  if (searchQuery.value.isBlank()) {
    noteViewModel.getPublicNotes()
    userViewModel.getAllUsers()
    folderViewModel.getPublicFolders()
  }

  Scaffold(
      modifier = Modifier.testTag("searchScreen"),
      topBar = {
        Column {
          OutlinedTextField(
              value = searchQuery.value,
              onValueChange = { searchQuery.value = it },
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
          Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp, start = 30.dp)) {
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
            searchQuery.value.isNotBlank() &&
                searchType.value == SearchType.USERS &&
                filteredUsers.value.isNotEmpty()
        val displayNotes: Boolean =
            searchQuery.value.isNotBlank() &&
                searchType.value == SearchType.NOTES &&
                filteredNotes.value.isNotEmpty()

        val displayFolders: Boolean =
            searchQuery.value.isNotBlank() &&
                searchType.value == SearchType.FOLDERS &&
                filteredFolders.value.isNotEmpty()

        val displayLoader: Boolean =
            searchQuery.value.isNotBlank() &&
                ((searchType.value == SearchType.USERS && filteredUsers.value.isEmpty()) ||
                    (searchType.value == SearchType.NOTES && filteredNotes.value.isEmpty()) ||
                    (searchType.value == SearchType.FOLDERS && filteredFolders.value.isEmpty()))

        if (displayNotes) {
          LazyColumn(
              contentPadding = PaddingValues(horizontal = 16.dp),
              modifier = Modifier.fillMaxWidth().padding(padding).testTag("filteredNoteList")) {
                items(filteredNotes.value.size) { index ->
                  NoteItem(
                      note = filteredNotes.value[index],
                      author =
                          users.value
                              .first { it.uid == filteredNotes.value[index].userId }
                              .userHandle()) {
                        noteViewModel.selectedNote(filteredNotes.value[index])
                        navigationActions.navigateTo(Screen.EDIT_NOTE)
                      }
                }
              }
        }
        if (displayUsers) {
          LazyColumn(
              contentPadding = PaddingValues(horizontal = 16.dp),
              modifier = Modifier.fillMaxWidth().padding(padding).testTag("filteredUserList")) {
                items(filteredUsers.value.size) { index ->
                  UserItem(filteredUsers.value[index]) {
                    userViewModel.setProfileUser(filteredUsers.value[index])
                    navigationActions.navigateTo(Screen.PUBLIC_PROFILE)
                  }
                }
              }
        }

        if (displayFolders) {
          CustomLazyGrid(
              modifier = Modifier.fillMaxSize().padding(padding),
              notes = remember { mutableStateOf(emptyList()) },
              folders = filteredFolders,
              gridModifier =
                  Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("filteredFolderList"),
              folderViewModel = folderViewModel,
              noteViewModel = noteViewModel,
              navigationActions = navigationActions,
              paddingValues = padding,
              columnContent = {})
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
private fun RefreshPeriodically(
    searchQuery: MutableState<String>,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel
) {
  LaunchedEffect(Unit) {
    while (searchQuery.value.isNotBlank()) {
      delay(3000)
      noteViewModel.getPublicNotes()
      userViewModel.getAllUsers()
      folderViewModel.getPublicFolders()
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

fun textMatchesSearch(text: String, searchWords: List<String>): Boolean {
  return searchWords.all { text.contains(it, ignoreCase = true) }
}
