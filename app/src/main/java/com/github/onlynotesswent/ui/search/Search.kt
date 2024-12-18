package com.github.onlynotesswent.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomSeparatedLazyGrid
import com.github.onlynotesswent.ui.common.DeckSearchItem
import com.github.onlynotesswent.ui.common.NoteItem
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.user.UserItem
import com.github.onlynotesswent.ui.user.switchProfileTo
import kotlinx.coroutines.delay

/**
 * Displays the search screen where users can search notes by title.
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to search from.
 * @param userViewModel The ViewModel that provides the list of users to search from.
 * @param folderViewModel The ViewModel that provides the list of folders to search from.
 * @param deckViewModel The ViewModel that provides the list of decks to search from.
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

  val currentUser = userViewModel.currentUser.collectAsState()
  val showAdditionalFilters = remember { mutableStateOf(false) }
  val additionalFilter = remember { mutableStateOf(AdditionalFilterType.PUBLIC) }

  val publicNotes = noteViewModel.publicNotes.collectAsState()
  val friendsNotes = noteViewModel.friendsNotes.collectAsState()

  val filteredPublicNotes = remember { mutableStateOf(publicNotes.value) }
  filteredPublicNotes.value =
      publicNotes.value.filter { textMatchesSearch(it.title, searchWords.value) }
  val filteredFriendsNotes = remember { mutableStateOf(friendsNotes.value) }
  filteredFriendsNotes.value =
      friendsNotes.value.filter { textMatchesSearch(it.title, searchWords.value) }

  val users = userViewModel.allUsers.collectAsState()
  val filteredUsers = remember { mutableStateOf(users.value) }
  filteredUsers.value =
      users.value.filter {
        textMatchesSearch(it.fullName(), searchWords.value) ||
            textMatchesSearch(it.userHandle(), searchWords.value)
      }

  val publicFolders = folderViewModel.publicFolders.collectAsState()
  val friendsFolders = folderViewModel.friendsFolders.collectAsState()

  val filteredPublicFolders = remember { mutableStateOf(publicFolders.value) }
  filteredPublicFolders.value =
      publicFolders.value.filter { textMatchesSearch(it.name, searchWords.value) }
  val filteredFriendsFolders = remember { mutableStateOf(friendsFolders.value) }
  filteredFriendsFolders.value =
      friendsFolders.value.filter { textMatchesSearch(it.name, searchWords.value) }

  val publicDecks = deckViewModel.publicDecks.collectAsState()
  val friendsDecks = deckViewModel.friendsDecks.collectAsState()

  val filteredPublicDecks = remember { mutableStateOf(publicDecks.value) }
  filteredPublicDecks.value =
      publicDecks.value.filter { textMatchesSearch(it.name, searchWords.value) }
  val filteredFriendsDecks = remember { mutableStateOf(friendsDecks.value) }
  filteredFriendsDecks.value =
      friendsDecks.value.filter { textMatchesSearch(it.name, searchWords.value) }

  // Refresh the list of notes, users, folders and decks periodically.
  RefreshPeriodically(
      searchQuery, noteViewModel, userViewModel, folderViewModel, deckViewModel, currentUser)
  // Refresh the list of notes, users, and folders when the search query is empty,
  // typically when reloading the screen.
  if (searchQuery.value.isBlank()) {
    noteViewModel.getPublicNotes()
    noteViewModel.getNotesFromFollowingList(currentUser.value?.friends?.following ?: emptyList())
    userViewModel.getAllUsers()
    folderViewModel.getPublicFolders()
    folderViewModel.getFoldersFromFollowingList(
        currentUser.value?.friends?.following ?: emptyList())
    deckViewModel.getPublicDecks()
    deckViewModel.getDecksFromFollowingList(currentUser.value?.friends?.following ?: emptyList())
  }

  Scaffold(
      modifier = Modifier.testTag("searchScreen"),
      topBar = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
                    showAdditionalFilters.value = false
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
                  modifier = Modifier.padding(horizontal = 5.dp).testTag("userFilterChip"))
            }
            item {
              FilterChip(
                  searchType.value == SearchType.NOTES,
                  label = { Text(stringResource(R.string.notes_maj), maxLines = 1) },
                  onClick = {
                    if (searchType.value == SearchType.NOTES || !showAdditionalFilters.value) {
                      showAdditionalFilters.value = !showAdditionalFilters.value
                    }
                    searchType.value = SearchType.NOTES
                  },
                  leadingIcon = {
                    if (searchType.value == SearchType.NOTES)
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Chip Icon",
                            tint = MaterialTheme.colorScheme.onBackground)
                  },
                  trailingIcon = {
                    if (searchType.value == SearchType.NOTES && showAdditionalFilters.value) {
                      Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    } else {
                      Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                  },
                  modifier = Modifier.padding(horizontal = 5.dp).testTag("noteFilterChip"))
            }
            item {
              FilterChip(
                  searchType.value == SearchType.FOLDERS,
                  label = { Text(stringResource(R.string.folders_maj), maxLines = 1) },
                  onClick = {
                    if (searchType.value == SearchType.FOLDERS || !showAdditionalFilters.value) {
                      showAdditionalFilters.value = !showAdditionalFilters.value
                    }
                    searchType.value = SearchType.FOLDERS
                  },
                  leadingIcon = {
                    if (searchType.value == SearchType.FOLDERS)
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Chip Icon",
                            tint = MaterialTheme.colorScheme.onBackground)
                  },
                  trailingIcon = {
                    if (searchType.value == SearchType.FOLDERS && showAdditionalFilters.value) {
                      Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    } else {
                      Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                  },
                  modifier = Modifier.padding(horizontal = 5.dp).testTag("folderFilterChip"))
            }
            item {
              FilterChip(
                  searchType.value == SearchType.DECKS,
                  label = { Text(stringResource(R.string.decks_maj), maxLines = 1) },
                  onClick = {
                    if (searchType.value == SearchType.DECKS || !showAdditionalFilters.value) {
                      showAdditionalFilters.value = !showAdditionalFilters.value
                    }
                    searchType.value = SearchType.DECKS
                  },
                  leadingIcon = {
                    if (searchType.value == SearchType.DECKS)
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Chip Icon",
                            tint = MaterialTheme.colorScheme.onBackground)
                  },
                  trailingIcon = {
                    if (searchType.value == SearchType.DECKS && showAdditionalFilters.value) {
                      Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    } else {
                      Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                  },
                  modifier = Modifier.padding(horizontal = 5.dp).testTag("deckFilterChip"))
            }
          }

          AdditionalFilters(
              showAdditionalFilters = showAdditionalFilters,
              additionalFilter = additionalFilter,
              itemType = searchType,
              noteViewModel = noteViewModel,
              folderViewModel = folderViewModel,
              deckViewModel = deckViewModel,
              userViewModel = userViewModel)
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
            searchType.value == SearchType.USERS && filteredUsers.value.isNotEmpty()

        val displayPublicNotes: Boolean =
            searchType.value == SearchType.NOTES &&
                additionalFilter.value == AdditionalFilterType.PUBLIC &&
                filteredPublicNotes.value.isNotEmpty()

        val displayFriendsNotes: Boolean =
            searchType.value == SearchType.NOTES &&
                additionalFilter.value == AdditionalFilterType.FRIENDS &&
                filteredFriendsNotes.value.isNotEmpty()

        val displayPublicFolders: Boolean =
            searchType.value == SearchType.FOLDERS &&
                additionalFilter.value == AdditionalFilterType.PUBLIC &&
                filteredPublicFolders.value.isNotEmpty()

        val displayFriendsFolders: Boolean =
            searchType.value == SearchType.FOLDERS &&
                additionalFilter.value == AdditionalFilterType.FRIENDS &&
                filteredFriendsFolders.value.isNotEmpty()

        val displayPublicDecks: Boolean =
            searchType.value == SearchType.DECKS &&
                additionalFilter.value == AdditionalFilterType.PUBLIC &&
                filteredPublicDecks.value.isNotEmpty()

        val displayFriendsDecks: Boolean =
            searchType.value == SearchType.DECKS &&
                additionalFilter.value == AdditionalFilterType.FRIENDS &&
                filteredFriendsDecks.value.isNotEmpty()

        val displayLoader: Boolean =
            searchQuery.value.isNotBlank() &&
                !(displayUsers ||
                    displayPublicNotes ||
                    displayFriendsNotes ||
                    displayPublicFolders ||
                    displayFriendsFolders ||
                    displayPublicDecks ||
                    displayFriendsDecks)

        if (displayPublicNotes) {
          LazyColumn(
              contentPadding = PaddingValues(horizontal = 16.dp),
              modifier =
                  Modifier.fillMaxWidth().padding(padding).testTag("filteredPublicNoteList")) {
                items(filteredPublicNotes.value.size) { index ->
                  NoteItem(
                      note = filteredPublicNotes.value[index],
                      author =
                          users.value
                              .first { it.uid == filteredPublicNotes.value[index].userId }
                              .userHandle(),
                      currentUser = userViewModel.currentUser.collectAsState(),
                      noteViewModel = noteViewModel,
                      folderViewModel = folderViewModel,
                      showDialog = false,
                      navigationActions = navigationActions,
                  ) {
                    noteViewModel.selectedNote(filteredPublicNotes.value[index])
                    navigationActions.navigateTo(Screen.EDIT_NOTE)
                  }
                }
              }
        }

        if (displayFriendsNotes) {
          LazyColumn(
              contentPadding = PaddingValues(horizontal = 16.dp),
              modifier =
                  Modifier.fillMaxWidth().padding(padding).testTag("filteredFriendNoteList")) {
                items(filteredFriendsNotes.value.size) { index ->
                  NoteItem(
                      note = filteredFriendsNotes.value[index],
                      author =
                          users.value
                              .first { it.uid == filteredFriendsNotes.value[index].userId }
                              .userHandle(),
                      currentUser = userViewModel.currentUser.collectAsState(),
                      noteViewModel = noteViewModel,
                      folderViewModel = folderViewModel,
                      showDialog = false,
                      navigationActions = navigationActions,
                  ) {
                    noteViewModel.selectedNote(filteredFriendsNotes.value[index])
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
                  val userState = remember { derivedStateOf { filteredUsers.value[index] } }
                  UserItem(
                      userState,
                      userViewModel,
                      fileViewModel,
                      onClick = {
                        userViewModel.setProfileUser(filteredUsers.value[index])
                        switchProfileTo(
                            filteredUsers.value[index], userViewModel, navigationActions)
                      })
                }
              }
        }

        if (displayPublicFolders) {
          CustomSeparatedLazyGrid(
              modifier = Modifier.fillMaxSize().padding(padding),
              notes = remember { mutableStateOf(emptyList()) },
              folders = filteredPublicFolders,
              gridModifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 16.dp)
                      .testTag("filteredPublicFolderList"),
              folderViewModel = folderViewModel,
              noteViewModel = noteViewModel,
              userViewModel = userViewModel,
              navigationActions = navigationActions,
              paddingValues = padding,
              columnContent = {})
        }

        if (displayFriendsFolders) {
          CustomSeparatedLazyGrid(
              modifier = Modifier.fillMaxSize().padding(padding),
              notes = remember { mutableStateOf(emptyList()) },
              folders = filteredFriendsFolders,
              gridModifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 16.dp)
                      .testTag("filteredFriendFolderList"),
              folderViewModel = folderViewModel,
              noteViewModel = noteViewModel,
              userViewModel = userViewModel,
              navigationActions = navigationActions,
              paddingValues = padding,
              columnContent = {})
        }

        if (displayPublicDecks) {
          LazyColumn(
              contentPadding = PaddingValues(horizontal = 16.dp),
              modifier =
                  Modifier.fillMaxWidth().padding(padding).testTag("filteredPublicDeckList")) {
                items(filteredPublicDecks.value.size) { index ->
                  DeckSearchItem(
                      deck = filteredPublicDecks.value[index],
                      author =
                          users.value
                              .first { it.uid == filteredPublicDecks.value[index].userId }
                              .userHandle(),
                  ) {
                    deckViewModel.selectDeck(filteredPublicDecks.value[index])
                    navigationActions.navigateTo(
                        Screen.DECK_MENU.replace("{deckId}", filteredPublicDecks.value[index].id))
                  }
                }
              }
        }

        if (displayFriendsDecks) {
          LazyColumn(
              contentPadding = PaddingValues(horizontal = 16.dp),
              modifier =
                  Modifier.fillMaxWidth().padding(padding).testTag("filteredFriendDeckList")) {
                items(filteredFriendsDecks.value.size) { index ->
                  DeckSearchItem(
                      deck = filteredFriendsDecks.value[index],
                      author =
                          users.value
                              .first { it.uid == filteredFriendsDecks.value[index].userId }
                              .userHandle(),
                  ) {
                    deckViewModel.selectDeck(filteredFriendsDecks.value[index])
                    navigationActions.navigateTo(
                        Screen.DECK_MENU.replace("{deckId}", filteredFriendsDecks.value[index].id))
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

/**
 * Refreshes the list of notes, users, and folders periodically.
 *
 * @param searchQuery The search query to use.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to search from.
 * @param userViewModel The ViewModel that provides the list of users to search from.
 * @param folderViewModel The ViewModel that provides the list of folders to search from.
 * @param deckViewModel The ViewModel that provides the list of decks to search from.
 * @param currentUser The current user.
 */
@Composable
private fun RefreshPeriodically(
    searchQuery: MutableState<String>,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    folderViewModel: FolderViewModel,
    deckViewModel: DeckViewModel,
    currentUser: State<User?>
) {
  LaunchedEffect(Unit) {
    while (searchQuery.value.isNotBlank()) {
      delay(3000)
      noteViewModel.getPublicNotes()
      noteViewModel.getNotesFromFollowingList(currentUser.value?.friends?.following ?: emptyList())
      userViewModel.getAllUsers()
      folderViewModel.getPublicFolders()
      folderViewModel.getFoldersFromFollowingList(
          currentUser.value?.friends?.following ?: emptyList())
      deckViewModel.getPublicDecks()
      deckViewModel.getDecksFromFollowingList(currentUser.value?.friends?.following ?: emptyList())
    }
  }
}

enum class SearchType {
  USERS,
  NOTES,
  FOLDERS,
  DECKS
}

enum class AdditionalFilterType {
  PUBLIC,
  FRIENDS;

  /**
   * Converts the filter type to a readable string.
   *
   * @return The readable string.
   */
  fun toReadableString(): String {
    return when (this) {
      PUBLIC -> "Public"
      FRIENDS -> "Friends"
    }
  }
}

/**
 * Checks if the text contains all the search words.
 *
 * @param text The text to search.
 * @param searchWords The search words.
 * @return True if the text contains all the search words, false otherwise.
 */
fun textMatchesSearch(text: String, searchWords: List<String>): Boolean {
  return searchWords.all { text.contains(it, ignoreCase = true) } || searchWords.isEmpty()
}

// Animated visibility to show additional filters
@Composable
private fun AdditionalFilters(
    showAdditionalFilters: MutableState<Boolean>,
    additionalFilter: MutableState<AdditionalFilterType>,
    itemType: MutableState<SearchType>,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
    deckViewModel: DeckViewModel,
    userViewModel: UserViewModel
) {
  Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(vertical = 5.dp)) {
        AnimatedVisibility(visible = showAdditionalFilters.value) {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(AdditionalFilterType.entries.size) { index ->
              FilterChip(
                  modifier =
                      Modifier.testTag(
                          "additionalFilterChip--${AdditionalFilterType.entries[index]}"),
                  selected = additionalFilter.value == AdditionalFilterType.entries[index],
                  onClick = {
                    additionalFilter.value = AdditionalFilterType.entries[index]
                    applyFilter(
                        noteViewModel,
                        folderViewModel,
                        deckViewModel,
                        userViewModel,
                        additionalFilter.value,
                        itemType.value)
                  },
                  label = {
                    Text(AdditionalFilterType.entries[index].toReadableString(), maxLines = 1)
                  },
                  leadingIcon = {
                    if (additionalFilter.value == AdditionalFilterType.entries[index])
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Chip Icon",
                            tint = MaterialTheme.colorScheme.onBackground)
                  })
            }
          }
        }
      }
}

// Helper function to apply the filter (public or friend) based on the filter and item type.
private fun applyFilter(
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
    deckViewModel: DeckViewModel,
    userViewModel: UserViewModel,
    filterType: AdditionalFilterType,
    itemType: SearchType
) {
  when (filterType) {
    AdditionalFilterType.PUBLIC -> {
      when (itemType) {
        SearchType.NOTES -> {
          noteViewModel.getPublicNotes()
        }
        SearchType.FOLDERS -> {
          folderViewModel.getPublicFolders()
        }
        else -> {
          deckViewModel.getPublicDecks()
        }
      }
    }
    AdditionalFilterType.FRIENDS -> {
      when (itemType) {
        SearchType.NOTES -> {
          noteViewModel.getNotesFromFollowingList(
              userViewModel.currentUser.value?.friends?.following ?: emptyList())
        }
        SearchType.FOLDERS -> {
          folderViewModel.getFoldersFromFollowingList(
              userViewModel.currentUser.value?.friends?.following ?: emptyList())
        }
        else -> {
          deckViewModel.getDecksFromFollowingList(
              userViewModel.currentUser.value?.friends?.following ?: emptyList())
        }
      }
    }
  }
}
