package com.github.onlynotesswent.ui.deck

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomDropDownMenu
import com.github.onlynotesswent.ui.common.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.common.LoadingIndicator
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.common.ThumbnailPic
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.theme.Typography
import com.github.onlynotesswent.ui.user.switchProfileTo
import com.github.onlynotesswent.utils.PictureTaker

@Composable
fun DeckScreen(
    userViewModel: UserViewModel,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel,
    pictureTaker: PictureTaker,
    navigationActions: NavigationActions
) {
  val selectedDeck = deckViewModel.selectedDeck.collectAsState()
  val deckFlashcards = flashcardViewModel.deckFlashcards.collectAsState()
  val belongsToUser =
      selectedDeck.value?.userId == userViewModel.currentUser.collectAsState().value?.uid
  val fabDropdownMenuShown = remember { mutableStateOf(false) }
  val author: MutableState<User?> = remember { mutableStateOf(null) }

  val addCardDialogExpanded = remember { mutableStateOf(false) }
  val importDialogExpanded = remember { mutableStateOf(false) }

  selectedDeck.value?.let { flashcardViewModel.fetchFlashcardsFromDeck(it) }
  selectedDeck.value?.userId?.let { userId ->
    userViewModel.getUserById(userId, onSuccess = { user -> author.value = user })
  }

  Scaffold(
      topBar = { DeckMenuTopAppBar { navigationActions.goBack() } },
      floatingActionButton = {
        if (belongsToUser) {
          DeckFab(
              expandedDropdownMenu = fabDropdownMenuShown,
              onAddCardClick = {
                addCardDialogExpanded.value = true
                flashcardViewModel.deselectFlashcard()
              },
              onImportDeckClick = { importDialogExpanded.value = true })
        }
      }) { innerPadding ->
        if (selectedDeck.value == null) {
          LoadingIndicator("Loading deck...")
        } else {
          Column(
              modifier = Modifier.fillMaxWidth().padding(innerPadding).padding(10.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                }
                if (importDialogExpanded.value) {
                  Dialog(onDismissRequest = { importDialogExpanded.value = false }) {
                    Card {
                      Column(
                          modifier = Modifier.padding(10.dp),
                          horizontalAlignment = Alignment.CenterHorizontally,
                          verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Import Deck", style = Typography.headlineSmall)
                            // TODO: Import deck functionality
                            Text("Not Implemented yet", style = Typography.bodyMedium)
                            // Import button
                            Button(onClick = { importDialogExpanded.value = false }) {
                              Text("Import")
                            }
                          }
                    }
                  }
                }

                // Card count and author name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                      Text(
                          text =
                              selectedDeck.value!!.flashcardIds.size.let { count ->
                                if (count == 1) "1 card" else "$count cards"
                              },
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
                                  fontStyle = FontStyle.Italic)
                            }
                      }
                    }
                // Deck Title
                Text(selectedDeck.value!!.name, style = Typography.displayMedium)
                // Deck description
                Text(
                    selectedDeck.value!!.description,
                    style = Typography.bodyMedium,
                    modifier = Modifier.padding(10.dp))
                // Deck play mode buttons
                Box {
                  Deck.PlayMode.entries.forEach {
                    PlayButton(it, selectedDeck, deckViewModel, navigationActions)
                  }
                }

                // Deck cards
                LazyColumn(
                    reverseLayout = true,
                    modifier =
                        Modifier.fillMaxWidth(0.9f)
                            .padding(top = 15.dp, start = 10.dp, end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      items(deckFlashcards.value.size) { index ->
                        FlashcardViewItem(
                            flashcard = deckFlashcards.value[index],
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

@Composable
fun PlayButton(
    playMode: Deck.PlayMode,
    selectedDeck: State<Deck?>,
    deckViewModel: DeckViewModel,
    navigationActions: NavigationActions,
) {
  val label: String =
      when (playMode) {
        Deck.PlayMode.FLASHCARD -> stringResource(R.string.play_mode_flashcards)
        Deck.PlayMode.MATCH -> stringResource(R.string.play_mode_match_cards)
        Deck.PlayMode.MCQ -> stringResource(R.string.play_mode_mcq)
        Deck.PlayMode.ALL -> stringResource(R.string.play_mode_all_combined)
      }
  Button(
      onClick = {
        deckViewModel.selectDeck(selectedDeck.value!!)
        navigationActions.navigateTo(
            Screen.DECK_PLAY.replace("{deckId}", selectedDeck.value!!.id)
                .replace("{mode}", playMode.toString()))
      }) {
        Text(label)
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

  Card(modifier = Modifier.testTag("flashcardItem").fillMaxWidth()) {
    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(10.dp)) {
      if (flashcard.isMCQ()) {
        Text(
            "MCQ",
            style = Typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.TopStart))
      }
      // Show front and options icon
      Box(modifier = Modifier.align(Alignment.TopEnd)) {
        Icon(
            modifier =
                Modifier.testTag("flashcardOptions").clickable(enabled = belongsToUser) {
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
      ) {
        Text(flashcard.front, style = Typography.bodyMedium)
        if (flashcard.hasImage) {
          // Show image
          val imageUri: MutableState<String?> = remember { mutableStateOf(null) }
          if (imageUri.value == null) {
            LoadingIndicator(
                "Image is being downloaded...",
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
                modifier = Modifier.height(100.dp))
          }
        }
        HorizontalDivider(modifier = Modifier.height(5.dp).padding(5.dp))
        // Show back
        Text(flashcard.back, style = Typography.bodyMedium)
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
private fun DeckFab(
    expandedDropdownMenu: MutableState<Boolean>,
    onAddCardClick: () -> Unit,
    onImportDeckClick: () -> Unit
) {
  CustomDropDownMenu(
      modifier = Modifier.testTag("deckFab"),
      menuItems =
          listOf(
              CustomDropDownMenuItem(
                  text = { Text("Add card") },
                  icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add card") },
                  onClick = onAddCardClick),
              CustomDropDownMenuItem(
                  text = { Text("Import deck") },
                  icon = {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "Import deck")
                  },
                  onClick = onImportDeckClick)),
      expanded = expandedDropdownMenu.value,
      onFabClick = { expandedDropdownMenu.value = !expandedDropdownMenu.value },
      onDismissRequest = { expandedDropdownMenu.value = false },
      fabIcon = {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add card or import deck")
      })
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
    Card {
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
                  modifier = Modifier.fillMaxWidth(),
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
                                Modifier.fillMaxWidth(),
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
                  modifier = Modifier.fillMaxWidth(),
                  value = back.value,
                  onValueChange = { back.value = it },
                  label = { Text("Back") })

              // Fake backs
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag("FakeBacks textField")
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
                                          Modifier.weight(1f)
                                              .testTag("EditFakeBack${index + 1} textField"))
                                  IconButton(
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
                                  IconButton(onClick = { fakeBacks.value = fakeBacks.value + "" }) {
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
                                      .copy(flashcardIds = deck.flashcardIds + newFlashcard.id)
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
            })
        DropdownMenuItem(
            text = @Composable { Text("Delete") },
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