package com.github.onlynotesswent.ui.deck

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.Deck.SortMode
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomDropDownMenu
import com.github.onlynotesswent.ui.common.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.common.LoadingIndicator
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.common.SelectVisibility
import com.github.onlynotesswent.ui.common.ThumbnailPic
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.theme.Typography
import com.github.onlynotesswent.ui.user.switchProfileTo
import com.github.onlynotesswent.utils.PictureTaker
import com.google.firebase.Timestamp

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
      topBar = { DeckMenuTopAppBar { navigationActions.goBack() } },
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
                      mode = "Create")
                } else if (importDialogExpanded.value) {
                  Dialog(onDismissRequest = { importDialogExpanded.value = false }) {
                    Card {
                      Column(
                          modifier = Modifier.padding(10.dp).testTag("importDeckDialog"),
                          horizontalAlignment = Alignment.CenterHorizontally,
                          verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Import Deck", style = Typography.headlineSmall)
                            // TODO: Import deck functionality
                            Text("Not Implemented yet", style = Typography.bodyMedium)
                            // Import button
                            Button(
                                modifier = Modifier.testTag("importButton"),
                                onClick = { importDialogExpanded.value = false }) {
                                  Text("Import")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                      Text(
                          text =
                              selectedDeck.value!!.flashcardIds.size.let { count ->
                                if (count == 1) "1 card" else "$count cards"
                              },
                          modifier = Modifier.testTag("deckCardCount"),
                          style = Typography.bodyLarge,
                          fontStyle = FontStyle.Italic)
                      VerticalDivider(modifier = Modifier.height(25.dp))
                      author.value?.let { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier =
                                Modifier.clickable {
                                  switchProfileTo(user, userViewModel, navigationActions)
                                }) {
                              ThumbnailPic(user, fileViewModel, size = 25)
                              Text(
                                  text = user.userHandle(),
                                  style = Typography.bodyLarge,
                                  fontStyle = FontStyle.Italic,
                                  modifier = Modifier.testTag("deckAuthorName"))
                            }
                      }
                    }
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
                FilledTonalButton(
                    onClick = { playModesShown.value = !playModesShown.value },
                    modifier = Modifier.padding(vertical = 15.dp).testTag("deckPlayButton")) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Play", style = MaterialTheme.typography.headlineMedium)
                        Icon(Icons.Default.PlayArrow, contentDescription = "play")
                      }
                    }

                // Deck sort options
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
                                modifier =
                                    Modifier.testTag("sortOptionChip--${SortMode.entries[index]}"),
                                selected = sortMode.value == SortMode.entries[index],
                                onClick = {
                                  sortMode.value = SortMode.entries[index]
                                  sortOrder.value = sortOrder.value.next()
                                },
                                label = {
                                  Text(SortMode.entries[index].toReadableString(), maxLines = 1)
                                },
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
                        FlashcardViewItem(
                            flashcard = sortedFlashcards.value[index],
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayModesBottomSheet(
    playModesShown: MutableState<Boolean>,
    deckViewModel: DeckViewModel,
    navigationActions: NavigationActions,
) {
  ModalBottomSheet(onDismissRequest = { playModesShown.value = false }) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp).testTag("playModesBottomSheet"),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Choose your play mode:", style = MaterialTheme.typography.headlineMedium)
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
                                    Deck.PlayMode.FLASHCARD ->
                                        stringResource(R.string.play_mode_flashcards)
                                    Deck.PlayMode.MATCH ->
                                        stringResource(R.string.play_mode_match_cards)
                                    Deck.PlayMode.MCQ -> stringResource(R.string.play_mode_mcq)
                                    Deck.PlayMode.ALL ->
                                        stringResource(R.string.play_mode_all_combined)
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

@Composable
fun FlashcardViewItem(
    flashcard: Flashcard,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel,
    pictureTaker: PictureTaker,
    belongsToUser: Boolean = false
) {
  val dropdownMenuExpanded = remember { mutableStateOf(false) }
  val editDialogExpanded = remember { mutableStateOf(false) }

  if (editDialogExpanded.value) {
    FlashcardDialog(
        deckViewModel,
        flashcardViewModel,
        pictureTaker,
        fileViewModel,
        {
          editDialogExpanded.value = false
          flashcardViewModel.deselectFlashcard()
        },
        mode = "Edit")
  }

  Card(modifier = Modifier.testTag("flashcardItem--${flashcard.id}").fillMaxWidth()) {
    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(10.dp)) {
      if (flashcard.isMCQ()) {
        Text(
            "MCQ",
            style = Typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.TopStart).testTag("flashcardMCQ--${flashcard.id}"))
      }
      // Show front and options icon
      Box(modifier = Modifier.align(Alignment.TopEnd)) {
        Icon(
            modifier =
                Modifier.testTag("flashcardOptions--${flashcard.id}").clickable(
                    enabled = belongsToUser) {
                      dropdownMenuExpanded.value = true
                    },
            imageVector = Icons.Filled.MoreVert,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer)
        if (dropdownMenuExpanded.value) {
          FlashcardItemDropdownMenu(
              flashcard,
              deckViewModel,
              flashcardViewModel,
              dropdownMenuExpanded,
              editDialogExpanded)
        }
      }
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier =
              Modifier.testTag("flashcardItemColumn")
                  .semantics(mergeDescendants = true, properties = {})) {
            Text(
                flashcard.front,
                style = Typography.bodyMedium,
                modifier = Modifier.testTag("flashcardFront--${flashcard.id}"))
            if (flashcard.hasImage) {
              // Show image
              val imageUri: MutableState<String?> = remember { mutableStateOf(null) }
              if (imageUri.value == null) {
                LoadingIndicator(
                    "Image is being downloaded...",
                    modifier =
                        Modifier.fillMaxWidth().testTag("flashcardImageLoading--${flashcard.id}"),
                    loadingIndicatorSize = 24.dp,
                    spacerHeight = 5.dp,
                    style = MaterialTheme.typography.bodySmall)
                fileViewModel.downloadFile(
                    flashcard.id,
                    FileType.FLASHCARD_IMAGE,
                    context = LocalContext.current,
                    onSuccess = { file -> imageUri.value = file.absolutePath })
              }
              imageUri.value?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Flashcard image",
                    modifier = Modifier.height(100.dp).testTag("flashcardImage--${flashcard.id}"))
              }
            }
            HorizontalDivider(modifier = Modifier.height(5.dp).padding(5.dp))
            // Show back
            Text(
                flashcard.back,
                style = Typography.bodyMedium,
                modifier = Modifier.testTag("flashcardBack--${flashcard.id}"))
          }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckMenuTopAppBar(onBackButtonClick: () -> Unit) {
  ScreenTopBar(
      title = "Deck",
      titleTestTag = "deckTopBarTitle",
      onBackClick = onBackButtonClick,
      icon = {
        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
      },
      iconTestTag = "backButton")
}

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
                  text = { Text("Save to favorites") },
                  icon = {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Save deck")
                  },
                  onClick = onSaveClick,
                  modifier = Modifier.testTag("saveToFavoritesMenuItem")),
              CustomDropDownMenuItem(
                  text = { Text("Create local copy") },
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
                  text = { Text("Add new card") },
                  icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add card") },
                  onClick = onAddCardClick,
                  modifier = Modifier.testTag("addCardMenuItem")),
              CustomDropDownMenuItem(
                  text = { Text("Import deck") },
                  icon = {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "Import deck")
                  },
                  onClick = onImportDeckClick,
                  modifier = Modifier.testTag("importDeckMenuItem")),
              CustomDropDownMenuItem(
                  text = { Text("Edit Deck") },
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

@Composable
fun FlashcardDialog(
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    pictureTaker: PictureTaker,
    fileViewModel: FileViewModel,
    onDismissRequest: () -> Unit,
    mode: String = "Create"
) {
  val flashcard = flashcardViewModel.selectedFlashcard.collectAsState()
  val front = remember { mutableStateOf(flashcard.value?.front ?: "") }
  val back = remember { mutableStateOf(flashcard.value?.back ?: "") }
  val fakeBacks = remember { mutableStateOf(flashcard.value?.fakeBacks ?: listOf()) }
  var showFakeBacksDetails = remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismissRequest) {
    Card(modifier = Modifier.testTag("flashcardDialog--$mode").padding(5.dp)) {
      if (flashcard.value == null && mode == "Edit") {
        LoadingIndicator("Loading flashcard...")
      } else {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text("$mode Flashcard", style = Typography.headlineSmall)
              // Front
              OutlinedTextField(
                  modifier = Modifier.fillMaxWidth().testTag("frontTextField"),
                  value = front.value,
                  onValueChange = { front.value = it },
                  label = { Text("Front") })

              // Image
              val imageUri: MutableState<String?> = remember { mutableStateOf(null) }
              val hasImageBeenChanged = remember { mutableStateOf(false) }

              // Remove image block when fake backs drop down is open to save space on screen
              AnimatedVisibility(
                  visible = !showFakeBacksDetails.value,
                  enter = expandVertically(),
                  exit = shrinkVertically()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()) {
                          // Image control buttons (delete and add)
                          Row(
                              horizontalArrangement = Arrangement.spacedBy(10.dp),
                              verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    modifier = Modifier.testTag("addImageIconButton"),
                                    onClick = {
                                      pictureTaker.setOnImageSelected { uri ->
                                        if (uri != null) {
                                          imageUri.value = uri.toString()
                                          hasImageBeenChanged.value = true
                                        }
                                      }
                                      pictureTaker.pickImage()
                                    }) {
                                      Icon(
                                          imageVector = Icons.Default.ImageSearch,
                                          contentDescription = "Add image")
                                    }
                                if (imageUri.value != null) {
                                  Spacer(modifier = Modifier.width(0.dp))
                                  IconButton(
                                      modifier = Modifier.testTag("removeImageIconButton"),
                                      onClick = {
                                        imageUri.value = null
                                        hasImageBeenChanged.value = true
                                      }) {
                                        Icon(
                                            imageVector = Icons.Default.HideImage,
                                            contentDescription = "Remove image")
                                      }
                                }
                              }
                          // Image loading indicator
                          if (flashcard.value?.hasImage == true &&
                              imageUri.value == null &&
                              !hasImageBeenChanged.value) {
                            fileViewModel.downloadFile(
                                flashcard.value!!.id,
                                FileType.FLASHCARD_IMAGE,
                                context = LocalContext.current,
                                onSuccess = { file -> imageUri.value = file.absolutePath })
                            LoadingIndicator(
                                "Image is being downloaded...",
                                Modifier.fillMaxWidth().testTag("imageLoadingIndicator"),
                                loadingIndicatorSize = 24.dp,
                                spacerHeight = 5.dp)
                          }
                          // Image placeholder
                          if (flashcard.value?.hasImage != true &&
                              imageUri.value == null &&
                              !hasImageBeenChanged.value) {
                            Text("Add an image", style = Typography.bodyMedium)
                          }
                          // Image
                          imageUri.value?.let {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = "Flashcard image",
                                modifier = Modifier.height(100.dp))
                          }
                        }
                  }

              HorizontalDivider(modifier = Modifier.height(5.dp).padding(top = 10.dp))
              // Back
              OutlinedTextField(
                  modifier = Modifier.fillMaxWidth().testTag("backTextField"),
                  value = back.value,
                  onValueChange = { back.value = it },
                  label = { Text("Back") })

              // Fake backs
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag("FakeBacksBox")
                          .clickable { showFakeBacksDetails.value = !showFakeBacksDetails.value }
                          .border(
                              1.dp,
                              OutlinedTextFieldDefaults.colors().unfocusedPlaceholderColor,
                              OutlinedTextFieldDefaults.shape)) {
                    val fakeBacksText =
                        if (fakeBacks.value.isEmpty()) "Add fake backs for MCQ"
                        else "Fake backs for MCQ"
                    Text(
                        text = fakeBacksText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp))
                    Icon(
                        imageVector =
                            if (showFakeBacksDetails.value) Icons.Default.ArrowDropUp
                            else Icons.Default.ArrowDropDown,
                        contentDescription = "Show fake backs",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterEnd))
                  }

              AnimatedVisibility(
                  modifier = Modifier.fillMaxWidth(),
                  visible = showFakeBacksDetails.value,
                  enter = expandVertically(),
                  exit = shrinkVertically()) {
                    LazyColumn(
                        state =
                            rememberLazyListState(
                                initialFirstVisibleItemIndex = fakeBacks.value.size),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                          items(fakeBacks.value.size) { index ->
                            Row(
                                modifier = Modifier.fillMaxWidth().animateItem(),
                                verticalAlignment = Alignment.CenterVertically) {
                                  OutlinedTextField(
                                      value = fakeBacks.value[index],
                                      onValueChange = { newValue ->
                                        fakeBacks.value =
                                            fakeBacks.value.toMutableList().apply {
                                              set(index, newValue)
                                            }
                                      },
                                      label = { Text("Fake Back ${index + 1}") },
                                      placeholder = { Text("Enter fake back text") },
                                      modifier =
                                          Modifier.weight(1f).testTag("fakeBackTextField--$index"))
                                  IconButton(
                                      modifier = Modifier.testTag("removeFakeBack--$index"),
                                      onClick = {
                                        fakeBacks.value =
                                            fakeBacks.value.toMutableList().apply {
                                              removeAt(index)
                                            }
                                      }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            tint = Color.Red,
                                            contentDescription = "Remove fake back")
                                      }
                                }
                          }
                          item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                  IconButton(
                                      modifier = Modifier.testTag("addFakeBackButton"),
                                      onClick = { fakeBacks.value = fakeBacks.value + "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add fake back")
                                      }
                                  Text(
                                      "Add fake back",
                                      style = Typography.bodyLarge,
                                      fontStyle = FontStyle.Italic)
                                }
                          }
                        }
                  }
              // Save button
              Button(
                  modifier = Modifier.testTag("saveFlashcardButton"),
                  enabled = front.value.isNotBlank() && back.value.isNotBlank(),
                  onClick = {
                    val newHasImage: Boolean =
                        if (hasImageBeenChanged.value) imageUri.value != null
                        else flashcard.value?.hasImage == true

                    val newFlashcard: Flashcard =
                        flashcard.value?.copy(
                            front = front.value,
                            back = back.value,
                            hasImage = newHasImage,
                            fakeBacks = fakeBacks.value)
                            ?: Flashcard(
                                id = flashcardViewModel.getNewUid(),
                                front = front.value,
                                back = back.value,
                                hasImage = newHasImage,
                                fakeBacks = fakeBacks.value,
                                lastReviewed = null,
                                userId = deckViewModel.selectedDeck.value!!.userId,
                                folderId = null,
                                noteId = null)

                    // Update elements asynchronously but in the correct order:
                    //   flashcard -> deck -> fetch flashcards -> image (if needed)
                    flashcardViewModel.updateFlashcard(
                        newFlashcard,
                        onSuccess = {
                          val newDeck: Deck =
                              deckViewModel.selectedDeck.value!!.let { deck ->
                                if (mode == "Create") {
                                  deck
                                      .copy(
                                          flashcardIds = // prepend new flashcard
                                          listOf(newFlashcard.id) + deck.flashcardIds)
                                      .let {
                                        deckViewModel.updateDeck(
                                            it, onSuccess = { deckViewModel.selectDeck(it) })
                                        it
                                      }
                                } else deck
                              }
                          flashcardViewModel.fetchFlashcardsFromDeck(newDeck)
                          onDismissRequest()
                        })

                    if (hasImageBeenChanged.value && imageUri.value != null) {
                      fileViewModel.uploadFile(
                          newFlashcard.id,
                          imageUri.value!!.toUri(),
                          FileType.FLASHCARD_IMAGE,
                          onSuccess = { hasImageBeenChanged.value = false },
                          onFailure = {})
                    } else if (flashcard.value != null &&
                        hasImageBeenChanged.value &&
                        imageUri.value == null) {
                      // No need to delete the image if it's a new flashcard, it will not find it
                      fileViewModel.deleteFile(
                          flashcard.value!!.id,
                          FileType.FLASHCARD_IMAGE,
                          onSuccess = { hasImageBeenChanged.value = false },
                          onFileNotFound = {},
                          onFailure = {})
                    }
                  }) {
                    Text("Save")
                  }
            }
      }
    }
  }
}

@Composable
fun FlashcardItemDropdownMenu(
    flashcard: Flashcard,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    dropdownMenuExpanded: MutableState<Boolean>,
    editDialogExpanded: MutableState<Boolean>
) {
  DropdownMenu(
      modifier = Modifier.testTag("flashcardOptionsMenu"),
      expanded = dropdownMenuExpanded.value,
      onDismissRequest = { dropdownMenuExpanded.value = false }) {
        DropdownMenuItem(
            text = @Composable { Text("Edit") },
            onClick = {
              flashcardViewModel.selectFlashcard(flashcard)
              editDialogExpanded.value = true
              dropdownMenuExpanded.value = false
            },
            modifier = Modifier.testTag("editFlashcardMenuItem"))
        DropdownMenuItem(
            text = @Composable { Text("Delete") },
            modifier = Modifier.testTag("deleteFlashcardMenuItem"),
            onClick = {
              val newDeck =
                  deckViewModel.selectedDeck.value?.copy(
                      flashcardIds =
                          deckViewModel.selectedDeck.value!!.flashcardIds.filter {
                            it != flashcard.id
                          })
              newDeck?.let {
                deckViewModel.updateDeck(it)
                deckViewModel.selectDeck(it)
              }
              flashcardViewModel.deleteFlashcard(flashcard)
              dropdownMenuExpanded.value = false
            })
      }
}

@Composable
fun EditDeckDialog(
    deckViewModel: DeckViewModel,
    userViewModel: UserViewModel,
    onDismissRequest: () -> Unit,
    mode: String = "Edit",
) {
  val deck: State<Deck?> = deckViewModel.selectedDeck.collectAsState()
  val deckTitle = remember { mutableStateOf(deck.value?.name ?: "") }
  val deckDescription = remember { mutableStateOf(deck.value?.description ?: "") }
  val deckVisibility = remember { mutableStateOf(deck.value?.visibility ?: Visibility.DEFAULT) }

  Dialog(onDismissRequest = onDismissRequest) {
    Card(modifier = Modifier.testTag("editDeckDialog").padding(5.dp)) {
      Column(
          modifier = Modifier.padding(10.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("$mode Deck", style = Typography.headlineSmall)
            OutlinedTextField(
                value = deckTitle.value,
                onValueChange = { deckTitle.value = Deck.formatTitle(it) },
                maxLines = 1,
                modifier = Modifier.testTag("deckTitleTextField"),
            )
            SelectVisibility(deckVisibility.value, { deckVisibility.value = it })
            OutlinedTextField(
                value = deckDescription.value,
                onValueChange = { deckDescription.value = Deck.formatDescription(it) },
                minLines = 2,
                maxLines = 5,
                modifier = Modifier.testTag("deckDescriptionTextField"))
            // Save button
            Button(
                modifier = Modifier.testTag("saveDeckButton"),
                onClick = {
                  val newDeck =
                      deck.value?.copy(
                          name = deckTitle.value,
                          description = deckDescription.value,
                          visibility = deckVisibility.value,
                          lastModified = Timestamp.now())
                          ?: Deck(
                              id = deckViewModel.getNewUid(),
                              name = deckTitle.value,
                              userId = userViewModel.currentUser.value!!.uid,
                              folderId = null,
                              visibility = deckVisibility.value,
                              description = deckDescription.value,
                              lastModified = Timestamp.now())
                  deckViewModel.updateDeck(newDeck, { deckViewModel.selectDeck(newDeck) })
                  onDismissRequest()
                }) {
                  Text("Save")
                }
          }
    }
  }
}
