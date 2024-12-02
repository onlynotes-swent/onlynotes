package com.github.onlynotesswent.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomLazyGrid
import com.github.onlynotesswent.ui.common.NoteItem
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.user.UserItem
import com.github.onlynotesswent.ui.user.switchProfileTo
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Displays the search screen where users can search notes by title.
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to search from.
 * @param userViewModel The ViewModel that provides the list of users to search from.
 * @param folderViewModel The ViewModel that provides the list of folders to search from.
 * @param fileViewModel The ViewModel used to download large files.
 */
@Composable
fun SearchScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel,
    deckViewModel: DeckViewModel,
    fileViewModel: FileViewModel
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

  val decks = deckViewModel.publicDecks.collectAsState()
  val filteredDecks = remember { mutableStateOf(decks.value) }
  filteredDecks.value = decks.value.filter { textMatchesSearch(it.name, searchWords.value) }

  val context = LocalContext.current

  // Refresh the list of notes, users, and folders periodically.
  RefreshPeriodically(searchQuery, noteViewModel, userViewModel, folderViewModel, deckViewModel)
  // Refresh the list of notes, users, and folders when the search query is empty,
  // typically when reloading the screen.
  if (searchQuery.value.isBlank()) {
    noteViewModel.getPublicNotes()
    userViewModel.getAllUsers()
    folderViewModel.getPublicFolders()
    deckViewModel.getPublicDecks()
  }

  Scaffold(
      modifier = Modifier.testTag("searchScreen"),
      topBar = {
        Column {
          OutlinedTextField(
              value = searchQuery.value,
              onValueChange = { searchQuery.value = it },
              placeholder = { Text(stringResource(R.string.search)) },
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
          LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 20.dp)) {
            item {
              FilterChip(
                  searchType.value == SearchType.USERS,
                  label = { Text(stringResource(R.string.users_maj), maxLines = 1) },
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
            }
            item {
              FilterChip(
                  searchType.value == SearchType.NOTES,
                  label = { Text(stringResource(R.string.notes_maj), maxLines = 1) },
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
            }
            item {
              FilterChip(
                  searchType.value == SearchType.FOLDERS,
                  label = { Text(stringResource(R.string.folders_maj), maxLines = 1) },
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
            item {
              FilterChip(
                  searchType.value == SearchType.DECKS,
                  label = { Text(stringResource(R.string.decks_maj), maxLines = 1) },
                  onClick = {
                    searchType.value = SearchType.DECKS
                    deckViewModel.getPublicDecks()
                  },
                  leadingIcon = {
                    if (searchType.value == SearchType.DECKS)
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Chip Icon",
                            tint = MaterialTheme.colorScheme.onBackground)
                  },
                  modifier = Modifier.padding(horizontal = 5.dp).testTag("deckFilterChip"))
            }
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

        val displayDecks: Boolean =
            searchQuery.value.isNotBlank() &&
                searchType.value == SearchType.DECKS &&
                filteredDecks.value.isNotEmpty()

        val displayLoader: Boolean =
            searchQuery.value.isNotBlank() &&
                ((searchType.value == SearchType.USERS && filteredUsers.value.isEmpty()) ||
                    (searchType.value == SearchType.NOTES && filteredNotes.value.isEmpty()) ||
                    (searchType.value == SearchType.FOLDERS && filteredFolders.value.isEmpty()) ||
                    (searchType.value == SearchType.DECKS && filteredDecks.value.isEmpty()))

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
                              .userHandle(),
                      currentUser = userViewModel.currentUser.collectAsState(),
                      context = context,
                      noteViewModel = noteViewModel,
                      folderViewModel = folderViewModel,
                      showDialog = false,
                      navigationActions = navigationActions,
                  ) {
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
                  UserItem(filteredUsers.value[index], userViewModel, fileViewModel) {
                    userViewModel.setProfileUser(filteredUsers.value[index])
                    switchProfileTo(filteredUsers.value[index], userViewModel, navigationActions)
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
              userViewModel = userViewModel,
              context = context,
              navigationActions = navigationActions,
              paddingValues = padding,
              columnContent = {})
        }

        if (displayDecks) {
          LazyColumn(
              contentPadding = PaddingValues(horizontal = 16.dp),
              modifier = Modifier.fillMaxWidth().padding(padding).testTag("filteredDeckList")) {
                items(filteredDecks.value.size) { index ->
                  DeckSearchItem(
                      deck = filteredDecks.value[index],
                      author =
                          users.value
                              .first { it.uid == filteredDecks.value[index].userId }
                              .userHandle(),
                  ) {
                    deckViewModel.selectDeck(filteredDecks.value[index])
                    // navigationActions.navigateTo(Screen.DECK_MENU)
                  }
                }
              }
        }

        if (displayLoader) {
          val searchedText =
              when (searchType.value) {
                SearchType.USERS -> stringResource(R.string.users_min)
                SearchType.NOTES -> stringResource(R.string.notes_min)
                SearchType.FOLDERS -> stringResource(R.string.folders_min)
                SearchType.DECKS -> stringResource(R.string.decks_min)
              }
          Text(
              modifier =
                  Modifier.padding(padding)
                      .fillMaxWidth()
                      .padding(24.dp)
                      .testTag("noSearchResults"),
              text = stringResource(R.string.not_found_search, searchedText),
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onBackground)
        }
      }
}

@Composable
fun DeckSearchItem(deck: Deck, author: String, onClick: () -> Unit) {
  Card(
      Modifier.testTag("deckCard")
          .padding(4.dp)
          .semantics(mergeDescendants = true, properties = {})
          .fillMaxWidth()
          .clickable(onClick = onClick)) {
        Column(
            Modifier.testTag("deckColumn").padding(10.dp).fillMaxWidth(),
        ) {
          Row {
            Text(
                text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(deck.lastModified.toDate()),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 10.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${deck.flashcardIds.size} cards",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 10.dp))
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
              text = deck.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(0.dp))
          Text(
              text = author,
              style = MaterialTheme.typography.bodySmall,
              fontStyle = FontStyle.Italic,
              modifier = Modifier.padding(0.dp))
        }
      }
}

@Composable
private fun RefreshPeriodically(
    searchQuery: MutableState<String>,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel,
    deckViewModel: DeckViewModel
) {
  LaunchedEffect(Unit) {
    while (searchQuery.value.isNotBlank()) {
      delay(3000)
      noteViewModel.getPublicNotes()
      userViewModel.getAllUsers()
      folderViewModel.getPublicFolders()
      deckViewModel.getPublicDecks()
    }
  }
}

enum class SearchType {
  USERS,
  NOTES,
  FOLDERS,
  DECKS
}

fun textMatchesSearch(text: String, searchWords: List<String>): Boolean {
  return searchWords.all { text.contains(it, ignoreCase = true) }
}
