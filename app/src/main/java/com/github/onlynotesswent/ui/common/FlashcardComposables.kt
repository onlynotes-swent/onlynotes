package com.github.onlynotesswent.ui.common

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.theme.Typography
import com.github.onlynotesswent.utils.PictureTaker

/**
 * Composable function that displays a flashcard item. The flashcard item includes the front and
 * back of the flashcard, and optionally an image. It also provides options to edit or delete the
 * flashcard if it belongs to the user.
 *
 * @param flashcard The flashcard to be displayed.
 * @param deckViewModel The ViewModel for deck-related data.
 * @param flashcardViewModel The ViewModel for flashcard-related data.
 * @param fileViewModel The ViewModel for file-related data.
 * @param pictureTaker The utility for taking pictures.
 * @param belongsToUser Indicates whether the flashcard belongs to the current user.
 */
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
            stringResource(R.string.mcq),
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
                    stringResource(R.string.image_is_being_downloaded),
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

/**
 * Composable function that displays a dialog for creating or editing a flashcard. The dialog
 * contains fields for the front and back of the flashcard, as well as an image field. The dialog
 * also allows the user to add fake backs for multiple choice questions.
 *
 * @param deckViewModel The ViewModel for deck-related data.
 * @param flashcardViewModel The ViewModel for flashcard-related data.
 * @param pictureTaker The utility for taking pictures.
 * @param fileViewModel The ViewModel for file-related data.
 * @param onDismissRequest The callback to be invoked when the dialog is dismissed.
 * @param mode The mode of the dialog, default is "Create".
 */
@Composable
fun FlashcardDialog(
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    pictureTaker: PictureTaker,
    fileViewModel: FileViewModel,
    onDismissRequest: () -> Unit,
    mode: String = stringResource(R.string.create)
) {
  val flashcard = flashcardViewModel.selectedFlashcard.collectAsState()
  val front = remember { mutableStateOf(flashcard.value?.front ?: "") }
  val back = remember { mutableStateOf(flashcard.value?.back ?: "") }
  val fakeBacks = remember { mutableStateOf(flashcard.value?.fakeBacks ?: listOf()) }
  var showFakeBacksDetails = remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismissRequest) {
    Card(modifier = Modifier.testTag("flashcardDialog--$mode").padding(5.dp)) {
      if (flashcard.value == null && mode == stringResource(R.string.edit_maj)) {
        LoadingIndicator(stringResource(R.string.loading_flashcard))
      } else {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                  "$mode " + stringResource(R.string.flashcard_maj),
                  style = Typography.headlineSmall)
              // Front
              OutlinedTextField(
                  modifier = Modifier.fillMaxWidth().testTag("frontTextField"),
                  value = front.value,
                  onValueChange = { front.value = it },
                  label = { Text(stringResource(R.string.front)) })

              // Image block:
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
                                // Add image button
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
                                // Remove image button
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
                                stringResource(R.string.image_is_being_downloaded),
                                Modifier.fillMaxWidth().testTag("imageLoadingIndicator"),
                                loadingIndicatorSize = 24.dp,
                                spacerHeight = 5.dp)
                          }
                          // Image placeholder
                          if (flashcard.value?.hasImage != true &&
                              imageUri.value == null &&
                              !hasImageBeenChanged.value) {
                            Text(
                                stringResource(R.string.add_an_image),
                                style = Typography.bodyMedium)
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
                  label = { Text(stringResource(R.string.back)) })

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
                        if (fakeBacks.value.isEmpty())
                            stringResource(R.string.add_fake_options_for_mcq)
                        else stringResource(R.string.fake_options_for_mcq)
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
              // Fake backs list (only shown when the box is clicked)
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
                                      label = {
                                        Text(stringResource(R.string.fake_option_i, index + 1))
                                      },
                                      placeholder = {
                                        Text(stringResource(R.string.enter_fake_option_text))
                                      },
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
                                            contentDescription = "Remove fake option")
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
                                      stringResource(R.string.add_fake_option),
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
                    Text(stringResource(R.string.save))
                  }
            }
      }
    }
  }
}

/**
 * Composable function that displays a dropdown menu of actions for a flashcard item. The menu
 * contains options to edit and delete the flashcard.
 *
 * @param flashcard The flashcard to display the dropdown menu for.
 * @param deckViewModel The ViewModel for deck-related data.
 * @param flashcardViewModel The ViewModel for flashcard-related data.
 * @param dropdownMenuExpanded The state for the dropdown menu expansion.
 * @param editDialogExpanded The state for the edit dialog expansion.
 */
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
            text = @Composable { Text(stringResource(R.string.edit_maj)) },
            onClick = {
              flashcardViewModel.selectFlashcard(flashcard)
              editDialogExpanded.value = true
              dropdownMenuExpanded.value = false
            },
            modifier = Modifier.testTag("editFlashcardMenuItem"))
        DropdownMenuItem(
            text = @Composable { Text(stringResource(R.string.delete)) },
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
fun FlashcardPlayItem(
    flashcard: Flashcard,
    userViewModel: UserViewModel,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel,
    onCorrect: () -> Unit = {},
    onIncorrect: () -> Unit = {},
){
    when {
        flashcard.isMCQ() -> {
            McqPlayItem(flashcard, userViewModel, deckViewModel, flashcardViewModel, fileViewModel, onCorrect, onIncorrect)
        }
        else -> {
            NormalFlashcardPlayItem(flashcard,userViewModel, deckViewModel, flashcardViewModel, fileViewModel)
        }
    }

}

@Composable
fun NormalFlashcardPlayItem(
    flashcard: Flashcard,
    userViewModel: UserViewModel,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel,
){
    val frontShown = remember { mutableStateOf(true) }
    val front = flashcard.front
    val back = flashcard.back
    val imageUri: MutableState<String?> = remember { mutableStateOf(null) }
    if (imageUri.value == null) {
        fileViewModel.downloadFile(
            flashcard.id,
            FileType.FLASHCARD_IMAGE,
            context = LocalContext.current,
            onSuccess = { file -> imageUri.value = file.absolutePath })
    }
    AnimatedContent(targetState = frontShown.value, label = "") {
        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(5.dp))
        {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical=30.dp)
                    .heightIn(min = 200.dp, max = 400.dp)
                    .clickable(onClick = { frontShown.value = !frontShown.value })
            ) {
                Text(
                    if (it) front else back,
                    style = Typography.bodyMedium,
                    modifier = Modifier.testTag("flashcardFront--${flashcard.id}")
                )
                if (it) {
                    if (imageUri.value != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri.value!!),
                            contentDescription = "Flashcard image",
                            modifier = Modifier.height(100.dp)
                                .testTag("flashcardImage--${flashcard.id}")
                        )
                    }
                    else {
                        LoadingIndicator(
                            stringResource(R.string.image_is_being_downloaded),
                            modifier =
                            Modifier.fillMaxWidth().testTag("flashcardImageLoading--${flashcard.id}"),
                            loadingIndicatorSize = 24.dp,
                            spacerHeight = 5.dp,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}


@Composable
fun McqPlayItem(
    flashcard: Flashcard,
    userViewModel: UserViewModel,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
){
    var choice: MutableState<Int?> = remember { mutableStateOf(null) }
    val backs = listOf(flashcard.back) + flashcard.fakeBacks.filter { it != flashcard.back && it.isNotBlank() }
    val shuffledIndexes = backs.indices.shuffled()

    ElevatedCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp).heightIn(min = 200.dp, max = 600.dp)
        ) {
            Text(
                flashcard.front,
                style = Typography.bodyMedium,
                modifier = Modifier.testTag("flashcardFront--${flashcard.id}")
            )
            HorizontalDivider(modifier = Modifier.height(5.dp))
            shuffledIndexes.forEach{ index ->
                val color = remember { mutableStateOf(Color.Gray) }
                if (choice.value != null) {
                    if (index == 0) {
                        color.value = Color.Green.copy(alpha = 0.9f, green = 0.8f, blue = 0.2f)
                    } else {
                        color.value = if (choice.value == index) Color.Red.copy(alpha = 0.9f, red = 0.8f, green = 0.2f) else MaterialTheme.colorScheme.onSurface
                    }
                } else {
                    color.value = MaterialTheme.colorScheme.onSurface
                }

                ListItem(
                    { Text(backs[index]) },
                    modifier = Modifier.clickable {
                        if (choice.value == null) {
                            choice.value = index
                        }
                        if (index == 0) {
                            onCorrect()
                        } else {
                            onIncorrect()
                        }
                    }.fillMaxWidth(0.8f).padding(5.dp),
                    colors = ListItemDefaults.colors(
                        headlineColor = color.value,
                    )
                )
            }
        }
    }

}