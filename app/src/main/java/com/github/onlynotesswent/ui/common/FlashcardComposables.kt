package com.github.onlynotesswent.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.absoluteOffset
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
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
    flashcard: State<Flashcard>,
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

  ElevatedCard(modifier = Modifier.testTag("flashcardItem--${flashcard.value.id}").fillMaxWidth()) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp).fillMaxWidth()) {
      if (flashcard.value.isMCQ()) {
        Text(
            stringResource(R.string.mcq),
            style = Typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            modifier =
                Modifier.align(Alignment.TopStart).testTag("flashcardMCQ--${flashcard.value.id}"))
      }
      // Show front and options icon
      Column(modifier = Modifier.align(Alignment.TopEnd)) {
        Icon(
            modifier =
                Modifier.testTag("flashcardOptions--${flashcard.value.id}").clickable(
                    enabled = belongsToUser) {
                      dropdownMenuExpanded.value = true
                    },
            imageVector = Icons.Filled.MoreVert,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer)
        AnimatedVisibility(
            dropdownMenuExpanded.value,
            enter = expandVertically(tween(700)),
            exit = shrinkVertically(tween(700))) {
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
          verticalArrangement = Arrangement.SpaceAround,
          modifier =
              Modifier.testTag("flashcardItemColumn")
                  .fillMaxWidth()
                  .heightIn(min = 160.dp)
                  .absoluteOffset(y = 5.dp)
                  .semantics(mergeDescendants = true, properties = {})) {
            Text(
                flashcard.value.front,
                style = Typography.bodyMedium,
                modifier = Modifier.testTag("flashcardFront--${flashcard.value.id}").padding(10.dp))
            FlashcardImage(flashcard, fileViewModel)
            HorizontalDivider(modifier = Modifier.height(5.dp).padding(5.dp))
            // Show back
            Text(
                flashcard.value.back,
                style = Typography.bodyMedium,
                modifier = Modifier.testTag("flashcardBack--${flashcard.value.id}").padding(20.dp))
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
  val showFakeBacksDetails = remember { mutableStateOf(false) }

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
                                      pictureTaker.pickImage(cropToSquare = false)
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

                    // Remove empty fake backs
                    fakeBacks.value = fakeBacks.value.filter { it.isNotBlank() }

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
    flashcard: State<Flashcard>,
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
              flashcardViewModel.selectFlashcard(flashcard.value)
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
                            it != flashcard.value.id
                          })
              newDeck?.let {
                deckViewModel.updateDeck(it)
                deckViewModel.selectDeck(it)
              }
              flashcardViewModel.deleteFlashcard(flashcard.value)
              dropdownMenuExpanded.value = false
            })
      }
}

@Composable
fun FlashcardPlayItem(
    flashcardState: State<Flashcard?>,
    fileViewModel: FileViewModel,
    onCorrect: () -> Unit = {},
    onIncorrect: () -> Unit = {},
    choice: MutableState<Int?> = remember { mutableStateOf(null) },
    isReview: Boolean = false
) {
  AnimatedContent(flashcardState.value == null, label = "") { displayLoader ->
    if (displayLoader) {
      LoadingIndicator(
          stringResource(R.string.loading_flashcard), Modifier.fillMaxWidth().height(200.dp))
    } else {
      val flashcard = remember { derivedStateOf { flashcardState.value!! } }
      if (flashcard.value.isMCQ() && !isReview) {
        McqPlayItem(flashcard, fileViewModel, onCorrect, onIncorrect, choice)
      } else {
        NormalFlashcardPlayItem(flashcard, fileViewModel)
      }
    }
  }
}

/**
 * Enum class representing the flip state of a flashcard.
 *
 * @property angle The angle of rotation for the flip state.
 */
enum class FlipState(val angle: Float) {
  FRONT(0f),
  BACK(180f);
  /** Gets the next flip state. */
  val next: FlipState
    get() =
        when (this) {
          FRONT -> BACK
          BACK -> FRONT
        }
}

/**
 * Composable function that displays a normal flashcard item for playing. The flashcard can be
 * flipped to show the front and back sides.
 *
 * @param flashcard The flashcard to be displayed.
 * @param fileViewModel The ViewModel for file-related data.
 */
@Composable
fun NormalFlashcardPlayItem(
    flashcard: State<Flashcard>,
    fileViewModel: FileViewModel,
) {
  val flipState = remember { mutableStateOf(FlipState.FRONT) }
  val rotation =
      animateFloatAsState(
              targetValue = flipState.value.angle,
              animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
              label = "rotationFloatState")
          .value

  ElevatedCard(
      modifier =
          Modifier.fillMaxWidth(0.95f)
              .testTag("flashcard")
              .padding(5.dp)
              .graphicsLayer {
                rotationY =
                    when (flipState.value) {
                      FlipState.FRONT -> 360f - rotation // same rotation for both sides
                      FlipState.BACK -> rotation
                    }
                cameraDistance = 15 * density
              }
              .clickable { flipState.value = flipState.value.next }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(vertical = 30.dp)
                    .heightIn(min = 200.dp, max = 400.dp)) {
              if (rotation <= 90f || rotation >= 270f) {
                Text(
                    flashcard.value.front,
                    style = Typography.bodyLarge,
                    fontWeight = FontWeight(450),
                    modifier = Modifier.testTag("flashcardFront"))
                FlashcardImage(flashcard, fileViewModel)
              } else {
                Text(
                    flashcard.value.back,
                    style = Typography.bodyLarge,
                    fontWeight = FontWeight(450),
                    modifier = Modifier.testTag("flashcardBack").graphicsLayer { rotationY = 180f })
              }
            }
      }
}

/**
 * Composable function that displays a multiple choice question (MCQ) flashcard item. The flashcard
 * item includes the front of the flashcard, and multiple choices for the back, randomly shuffled.
 * The user can select one of the choices, and the item will display whether the choice was correct
 * or incorrect.
 *
 * @param flashcard The flashcard to be displayed.
 * @param fileViewModel The ViewModel for file-related data.
 * @param onCorrect The callback to be invoked when the correct choice is selected.
 * @param onIncorrect The callback to be invoked when an incorrect choice is selected.
 * @param choice The state for the selected choice.
 */
@Composable
fun McqPlayItem(
    flashcard: State<Flashcard>,
    fileViewModel: FileViewModel,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    choice: MutableState<Int?> = remember { mutableStateOf(null) }
) {
  val backs =
      listOf(flashcard.value.back) +
          flashcard.value.fakeBacks.filter { it != flashcard.value.back && it.isNotBlank() }
  val shuffledIndexes = remember { backs.indices.shuffled() }

  ElevatedCard(modifier = Modifier.fillMaxWidth(0.9f).padding(5.dp).testTag("flashcard")) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = 30.dp, horizontal = 10.dp)
                .heightIn(min = 200.dp, max = 600.dp)) {
          Text(
              flashcard.value.front,
              style = Typography.bodyLarge,
              fontWeight = FontWeight(550),
              modifier = Modifier.testTag("flashcardFront"))
          FlashcardImage(flashcard, fileViewModel)
          HorizontalDivider(modifier = Modifier.height(5.dp).fillMaxWidth().padding(8.dp))
          shuffledIndexes.forEach { index ->
            val color = remember { mutableStateOf(Color.Gray) }
            color.value =
                if (choice.value != null && index == 0) {
                  Color.Green.copy(green = 0.6f, blue = 0.3f, red = 0.3f)
                } else if (choice.value == index) {
                  Color.Red.copy(red = 0.7f, blue = 0.3f)
                } else {
                  MaterialTheme.colorScheme.onSurface
                }
            ElevatedCard(
                modifier =
                    Modifier.fillMaxWidth(0.7f)
                        .padding(horizontal = 10.dp)
                        .testTag("flashcardChoice"),
                onClick = {
                  if (choice.value == null) {
                    choice.value = index
                    if (index == 0) {
                      onCorrect()
                    } else {
                      onIncorrect()
                    }
                  }
                },
                elevation =
                    if (choice.value == index)
                        CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    else CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
                  Row(
                      modifier = Modifier.fillMaxWidth().padding(5.dp),
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                      verticalAlignment = Alignment.CenterVertically) {
                        AnimatedContent(choice.value?.let { index == 0 }, label = "") {
                          when (it) {
                            null ->
                                Icon(
                                    imageVector = Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = color.value,
                                    modifier =
                                        Modifier.padding(5.dp).testTag("flashcardChoiceIcon"))
                            true ->
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = color.value,
                                    modifier = Modifier.padding(5.dp).testTag("flashcardCheckIcon"))
                            false ->
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = color.value,
                                    modifier = Modifier.padding(5.dp).testTag("flashcardWrongIcon"))
                          }
                        }
                        Text(
                            backs[index],
                            style = Typography.bodyMedium,
                            color = color.value,
                            modifier = Modifier.testTag("flashcardChoice--$index"))
                      }
                }
          }
        }
  }
}

/**
 * Composable function that displays an image for a flashcard. The image is downloaded from Firebase
 * Storage if it hasn't been downloaded yet.
 *
 * @param flashcard The flashcard to display the image for.
 * @param fileViewModel The ViewModel for file-related data.
 * @param imageUri The URI of the image.
 * @param padding The padding around the image.
 */
@Composable
fun FlashcardImage(
    flashcard: State<Flashcard>,
    fileViewModel: FileViewModel,
    imageUri: MutableState<String?> = remember { mutableStateOf(null) },
    padding: Dp = 10.dp,
) {
  if (compareUID(imageUri.value, flashcard.value.id, FileType.FLASHCARD_IMAGE.fileExtension)) {
    Image(
        painter = rememberAsyncImagePainter(imageUri.value!!),
        contentDescription = "Flashcard image",
        modifier =
            Modifier.height(100.dp)
                .clipToBounds()
                .testTag("flashcardImage--${flashcard.value.id}")
                .padding(padding))
  } else if (flashcard.value.hasImage) {
    fileViewModel.downloadFile(
        flashcard.value.id,
        FileType.FLASHCARD_IMAGE,
        context = LocalContext.current,
        onSuccess = { file -> imageUri.value = file.absolutePath })
    LoadingIndicator(
        stringResource(R.string.image_is_being_downloaded),
        modifier =
            Modifier.fillMaxWidth()
                .testTag("flashcardImageLoading--${flashcard.value.id}")
                .padding(padding),
        loadingIndicatorSize = 24.dp,
        spacerHeight = 5.dp,
        style = MaterialTheme.typography.bodySmall)
  }
}
