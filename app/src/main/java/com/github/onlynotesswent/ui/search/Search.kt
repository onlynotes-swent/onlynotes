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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
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
    userViewModel: UserViewModel
) {
  var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
  val userChipSelected = remember { mutableStateOf(false) }
  val noteChipSelected = remember { mutableStateOf(false) }

  val notes = noteViewModel.publicNotes.collectAsState()
  val filteredNotes = notes.value.filter { it.title.contains(searchQuery.text, ignoreCase = true) }

  val users = userViewModel.allUsers.collectAsState()
  val filteredUsers =
      users.value.filter {
        it.fullName().contains(searchQuery.text, ignoreCase = true) ||
            it.userHandle().contains(searchQuery.text, ignoreCase = true)
      }

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
              modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp).testTag("searchTextField"))
          Row(
              modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 20.dp)) {
            FilterChip(
                userChipSelected.value,
                label = { Text("Users") },
                onClick = {
                  userChipSelected.value = !userChipSelected.value
                  noteChipSelected.value = false
                },
                leadingIcon = {
                    if (userChipSelected.value)
                  Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Chip Icon",
                      tint = MaterialTheme.colorScheme.onBackground)
                },
                modifier = Modifier.padding(horizontal = 15.dp).testTag("userFilterChip"))
            FilterChip(
                noteChipSelected.value,
                label = { Text("Notes") },
                onClick = {
                  noteChipSelected.value = !noteChipSelected.value
                  userChipSelected.value = false
                },
                leadingIcon = {
                    if (noteChipSelected.value)
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Chip Icon",
                        tint = MaterialTheme.colorScheme.onBackground)
                },
                modifier = Modifier.padding(horizontal = 15.dp).testTag("noteFilterChip"))
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
            searchQuery.text.isNotBlank() && userChipSelected.value && filteredUsers.isNotEmpty()
        val displayNotes: Boolean =
            searchQuery.text.isNotBlank() && noteChipSelected.value && filteredNotes.isNotEmpty()
        val displayLoader: Boolean =
            searchQuery.text.isNotBlank() &&
                ((userChipSelected.value && filteredUsers.isEmpty()) ||
                    (noteChipSelected.value && filteredNotes.isEmpty()))

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

        if (displayLoader) {
          val searchedText = if (userChipSelected.value) "users" else "notes"
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
