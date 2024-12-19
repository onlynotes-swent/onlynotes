package com.github.onlynotesswent.ui.deck

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.Deck.SortMode
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomDropDownMenu
import com.github.onlynotesswent.ui.common.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.common.EditDeckDialog
import com.github.onlynotesswent.ui.common.FlashcardDialog
import com.github.onlynotesswent.ui.common.FlashcardViewItem
import com.github.onlynotesswent.ui.common.LoadingIndicator
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.common.ThumbnailPic
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.theme.Typography
import com.github.onlynotesswent.ui.user.switchProfileTo
import com.github.onlynotesswent.utils.PictureTaker

/**
 * Composable function that represents the Deck Screen.
 *
 * @param userViewModel The ViewModel for user-related data.
 * @param deckViewModel The ViewModel for deck-related data.
 * @param flashcardViewModel The ViewModel for flashcard-related data.
 * @param fileViewModel The ViewModel for file-related data.
 * @param pictureTaker The utility for taking pictures.
 * @param navigationActions The actions for navigation.
 */
@Composable
fun DeckScreen(
    userViewModel: UserViewModel,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel,
    pictureTaker: PictureTaker,
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  val selectedDeck = deckViewModel.selectedDeck.collectAsState()
  val deckFlashcards = flashcardViewModel.deckFlashcards.collectAsState()
  val belongsToUser =
      selectedDeck.value?.userId == userViewModel.currentUser.collectAsState().value?.uid
  val userFabDropdownMenuShown = remember { mutableStateOf(false) }
  val author: MutableState<User?> = remember { mutableStateOf(null) }

  val addCardDialogExpanded = remember { mutableStateOf(false) }
  val importDialogExpanded = remember { mutableStateOf(false) }
  val editDialogExpanded = remember { mutableStateOf(false) }

  val publicFabDropdownMenuShown = remember { mutableStateOf(false) }
  // TODO: add mutable states for save to favourites and create local copy dialogs

  val sortOptionsShown = remember { mutableStateOf(false) }
  val sortMode = remember { mutableStateOf(SortMode.ALPHABETICAL) }
  val sortOrder = remember { mutableStateOf(SortMode.Order.HIGH_LOW) }
  val sortedFlashcards = remember {
    derivedStateOf { sortMode.value.sort(deckFlashcards.value, sortOrder.value) }
  }

  val playModesShown = remember { mutableStateOf(false) }

  selectedDeck.value?.let { flashcardViewModel.fetchFlashcardsFromDeck(it) }
  selectedDeck.value?.userId?.let { userId ->
    userViewModel.getUserById(userId, onSuccess = { user -> author.value = user })
  }

  Scaffold(
      topBar = {
        DeckMenuTopAppBar {
          navigationActions.goBack()
          deckViewModel.clearSelectedDeck()
        }
      },
      floatingActionButton = {
        if (belongsToUser) {
          DeckFab(
              expandedDropdownMenu = userFabDropdownMenuShown,
              onAddCardClick = {
                addCardDialogExpanded.value = true
                flashcardViewModel.deselectFlashcard()
              },
              onImportDeckClick = { importDialogExpanded.value = true },
              onEditClick = { editDialogExpanded.value = true })
        } else {
          PublicDeckFab(
              publicFabDropdownMenuShown,
              onSaveClick = {
                Toast.makeText(context, "Not Implemented", Toast.LENGTH_LONG).show()
              },
              onSaveCopyClick = {
                Toast.makeText(context, "Not Implemented", Toast.LENGTH_LONG).show()
              })
        }
      }) { innerPadding ->
        if (selectedDeck.value == null) {
          LoadingIndicator("Loading deck...")
        } else {
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag("deckScreenColumn")
                      .padding(innerPadding)
                      .padding(start = 15.dp, top = 10.dp, end = 10.dp)
                      .verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally) {
                // Dialogs:
                if (addCardDialogExpanded.value) {
                  FlashcardDialog(
                      deckViewModel,
                      flashcardViewModel,
                      pictureTaker,
                      fileViewModel,
                      {
                        addCardDialogExpanded.value = false
                        flashcardViewModel.deselectFlashcard()
                      },
                      mode = stringResource(R.string.create))
                } else if (importDialogExpanded.value) {
                  Dialog(onDismissRequest = { importDialogExpanded.value = false }) {
                    Card {
                      Column(
                          modifier = Modifier.padding(10.dp).testTag("importDeckDialog"),
                          horizontalAlignment = Alignment.CenterHorizontally,
                          verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                stringResource(R.string.import_deck),
                                style = Typography.headlineSmall)
                            // TODO: Import deck functionality
                            Text("Not Implemented yet", style = Typography.bodyMedium)
                            // Import button
                            Button(
                                modifier = Modifier.testTag("importButton"),
                                onClick = { importDialogExpanded.value = false }) {
                                  Text(stringResource(R.string.import_button_text))
                                }
                          }
                    }
                  }
                } else if (editDialogExpanded.value) {
                  EditDeckDialog(deckViewModel, userViewModel, { editDialogExpanded.value = false })
                }

                // Play modes bottom sheet:
                if (playModesShown.value)
                    PlayModesBottomSheet(playModesShown, deckViewModel, navigationActions)

                // Card count and author name
                CardCountAndAuthor(
                    selectedDeck, author, userViewModel, fileViewModel, navigationActions)

                // Deck Title
                Text(
                    selectedDeck.value!!.name,
                    style = Typography.displayMedium,
                    modifier = Modifier.padding(vertical = 10.dp).testTag("deckTitle"))
                // Deck description
                Text(
                    selectedDeck.value!!.description,
                    style = Typography.bodyMedium,
                    modifier = Modifier.padding(10.dp).testTag("deckDescription"))

                // Deck play mode buttons
                Button(
                    onClick = { playModesShown.value = !playModesShown.value },
                    modifier = Modifier.padding(vertical = 15.dp).testTag("deckPlayButton")) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.play_button_text),
                            style = MaterialTheme.typography.headlineMedium)
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "play",
                            modifier = Modifier.padding(start = 5.dp).size(20.dp))
                      }
                    }

                // Deck sort options
                SortOptions(sortOptionsShown, sortMode, sortOrder)

                // Deck cards lazy column
                LazyColumn(
                    modifier =
                        Modifier.fillMaxWidth(0.9f)
                            .testTag("deckFlashcardsList")
                            .heightIn(max = 600.dp)
                            .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      items(sortedFlashcards.value.size) { index ->
                        val flashcardState = remember {
                          derivedStateOf { sortedFlashcards.value[index] }
                        }
                        FlashcardViewItem(
                            flashcard = flashcardState,
                            deckViewModel = deckViewModel,
                            flashcardViewModel = flashcardViewModel,
                            fileViewModel = fileViewModel,
                            pictureTaker = pictureTaker,
                            belongsToUser = belongsToUser)
                      }
                    }
              }
        }
      }
}
////////////////////////////////////////////////////////////////////////////////////////////////////
// Modularised functions for the deck FABs

/**
 * Composable function that displays a floating action button (FAB) with a dropdown menu for
 * deck-related actions for public decks. The dropdown menu contains options to save the deck to
 * favorites and create a local copy of the deck.
 *
 * @param expandedDropdownMenu The state for the dropdown menu expansion.
 * @param onSaveClick The callback to be invoked when the "Save to favorites" menu item is clicked.
 * @param onSaveCopyClick The callback to be invoked when the "Create local copy" menu item is
 *   clicked.
 */
@Composable
private fun PublicDeckFab(
    expandedDropdownMenu: MutableState<Boolean>,
    onSaveClick: () -> Unit,
    onSaveCopyClick: () -> Unit
) {
  CustomDropDownMenu(
      modifier = Modifier.testTag("publicDeckFab"),
      menuItems =
          listOf(
              CustomDropDownMenuItem(
                  text = { Text(stringResource(R.string.save_to_favorites)) },
                  icon = {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Save deck")
                  },
                  onClick = onSaveClick,
                  modifier = Modifier.testTag("saveToFavoritesMenuItem")),
              CustomDropDownMenuItem(
                  text = { Text(stringResource(R.string.create_local_copy)) },
                  icon = {
                    Icon(imageVector = Icons.Default.SaveAlt, contentDescription = "Save copy")
                  },
                  onClick = onSaveCopyClick,
                  modifier = Modifier.testTag("createLocalCopyMenuItem")),
          ),
      expanded = expandedDropdownMenu.value,
      onFabClick = { expandedDropdownMenu.value = !expandedDropdownMenu.value },
      onDismissRequest = { expandedDropdownMenu.value = false },
      fabIcon = { Icon(imageVector = Icons.Default.Download, contentDescription = "Save Deck") })
}

/**
 * Composable function that displays a floating action button (FAB) with a dropdown menu for
 * deck-related actions for owners of the deck. The dropdown menu contains options to add a new
 * card, import a deck, and edit the deck.
 *
 * @param expandedDropdownMenu The state for the dropdown menu expansion.
 * @param onAddCardClick The callback to be invoked when the "Add new card" menu item is clicked.
 * @param onImportDeckClick The callback to be invoked when the "Import deck" menu item is clicked.
 * @param onEditClick The callback to be invoked when the "Edit Deck" menu item is clicked.
 */
@Composable
private fun DeckFab(
    expandedDropdownMenu: MutableState<Boolean>,
    onAddCardClick: () -> Unit,
    onImportDeckClick: () -> Unit,
    onEditClick: () -> Unit
) {
  CustomDropDownMenu(
      modifier = Modifier.testTag("deckFab"),
      menuItems =
          listOf(
              CustomDropDownMenuItem(
                  text = { Text(stringResource(R.string.add_new_card)) },
                  icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add card") },
                  onClick = onAddCardClick,
                  modifier = Modifier.testTag("addCardMenuItem")),
              CustomDropDownMenuItem(
                  text = { Text(stringResource(R.string.import_deck)) },
                  icon = {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "Import deck")
                  },
                  onClick = onImportDeckClick,
                  modifier = Modifier.testTag("importDeckMenuItem")),
              CustomDropDownMenuItem(
                  text = { Text(stringResource(R.string.edit_deck)) },
                  icon = {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit deck")
                  },
                  onClick = onEditClick,
                  modifier = Modifier.testTag("editDeckMenuItem"))),
      expanded = expandedDropdownMenu.value,
      onFabClick = { expandedDropdownMenu.value = !expandedDropdownMenu.value },
      onDismissRequest = { expandedDropdownMenu.value = false },
      fabIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Deck") })
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Modularised functions for the top app bar, deck info, sort options and play modes

/**
 * Composable function that represents the top app bar for the Deck Menu screen.
 *
 * @param onBackButtonClick The callback to be invoked when the back button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckMenuTopAppBar(onBackButtonClick: () -> Unit) {
  ScreenTopBar(
      title = stringResource(R.string.deck_maj),
      titleTestTag = "deckTopBarTitle",
      onBackClick = onBackButtonClick,
      icon = {
        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
      },
      iconTestTag = "backButton")
}

/**
 * Composable function that displays the card count and author information for a deck.
 *
 * @param deck The state containing the deck information.
 * @param author The state containing the author information.
 * @param userViewModel The ViewModel for user-related data.
 * @param fileViewModel The ViewModel for file-related data.
 * @param navigationActions The actions for navigation.
 */
@Composable
private fun CardCountAndAuthor(
    deck: State<Deck?>,
    author: State<User?>,
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel,
    navigationActions: NavigationActions
) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        deck.value?.let {
          Text(
              text =
                  it.flashcardIds.size.let { count ->
                    if (count == 1) "1 " + stringResource(R.string.card_min)
                    else "$count " + stringResource(R.string.cards_min)
                  },
              modifier = Modifier.testTag("deckCardCount"),
              style = Typography.bodyLarge,
              fontStyle = FontStyle.Italic)
        }
        VerticalDivider(modifier = Modifier.height(25.dp))
        author.value?.let { user ->
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
              modifier =
                  Modifier.clickable { switchProfileTo(user, userViewModel, navigationActions) }) {
                ThumbnailPic(user, fileViewModel, size = 25)
                Text(
                    text = user.userHandle(),
                    style = Typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.testTag("deckAuthorName"))
              }
        }
      }
}

@Composable
private fun SortOptions(
    sortOptionsShown: MutableState<Boolean>,
    sortMode: MutableState<SortMode>,
    sortOrder: MutableState<SortMode.Order>,
) {
  Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(vertical = 5.dp)) {
        IconButton(
            onClick = { sortOptionsShown.value = !sortOptionsShown.value },
            modifier = Modifier.testTag("sortOptionsButton")) {
              Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
            }
        AnimatedVisibility(visible = sortOptionsShown.value) {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(SortMode.entries.size) { index ->
              FilterChip(
                  modifier = Modifier.testTag("sortOptionChip--${SortMode.entries[index]}"),
                  selected = sortMode.value == SortMode.entries[index],
                  onClick = {
                    sortMode.value = SortMode.entries[index]
                    sortOrder.value = sortOrder.value.next()
                  },
                  label = { Text(SortMode.entries[index].toReadableString(), maxLines = 1) },
                  leadingIcon = {
                    if (sortMode.value == SortMode.entries[index] &&
                        sortOrder.value == SortMode.Order.HIGH_LOW)
                        Icon(Icons.Default.ArrowUpward, contentDescription = null)
                    else if (sortMode.value == SortMode.entries[index] &&
                        sortOrder.value == SortMode.Order.LOW_HIGH)
                        Icon(Icons.Default.ArrowDownward, contentDescription = null)
                  })
            }
          }
        }
      }
}

/**
 * Composable function that displays a bottom sheet for selecting play modes. The bottom sheet
 * contains a list of available play modes for the deck, which include flashcards, matching cards,
 * multiple choice questions, and all combined. Upon selecting a play mode, the user is navigated to
 * the deck play screen with the selected play mode.
 *
 * @param playModesShown The state for the visibility of the bottom sheet.
 * @param deckViewModel The ViewModel for deck-related data.
 * @param navigationActions The actions for navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayModesBottomSheet(
    playModesShown: MutableState<Boolean>,
    deckViewModel: DeckViewModel,
    navigationActions: NavigationActions,
) {
  ModalBottomSheet(onDismissRequest = { playModesShown.value = false }) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp).testTag("playModesBottomSheet"),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              stringResource(R.string.choose_your_play_mode),
              style = MaterialTheme.typography.headlineMedium)
          Spacer(modifier = Modifier.height(15.dp))
          Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally // Center-align items horizontally
              ) {
                Deck.PlayMode.entries.forEach { playMode ->
                  Card(
                      shape = RoundedCornerShape(16.dp), // Rounded corners
                      colors =
                          CardColors(
                              containerColor = MaterialTheme.colorScheme.surface,
                              contentColor = MaterialTheme.colorScheme.onSurface,
                              disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                              disabledContentColor =
                                  MaterialTheme.colorScheme.onSurfaceVariant), // Custom colors
                      modifier =
                          Modifier.fillMaxWidth(0.8f).testTag("playMode--$playMode").clickable {
                            playModesShown.value = false
                            navigationActions.navigateTo(
                                Screen.DECK_PLAY.replace(
                                        "{deckId}", deckViewModel.selectedDeck.value!!.id)
                                    .replace("{mode}", playMode.name))
                          }) {
                        ListItem(
                            modifier = Modifier.padding(1.dp),
                            headlineContent = {
                              Text(
                                  when (playMode) {
                                    Deck.PlayMode.REVIEW ->
                                        stringResource(R.string.review_the_flashcards)
                                    Deck.PlayMode.TEST ->
                                        stringResource(R.string.test_your_knowledge)
                                  },
                                  style = MaterialTheme.typography.bodyLarge)
                            },
                            trailingContent = {
                              Icon(Icons.Default.PlayArrow, contentDescription = null)
                            })
                      }
                }
              }
        }
  }
}
