package com.github.onlynotesswent.ui.deck

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.ImageFlashcard
import com.github.onlynotesswent.model.flashcard.TextFlashcard
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.CustomDropDownMenu
import com.github.onlynotesswent.ui.common.CustomDropDownMenuItem
import com.github.onlynotesswent.ui.common.LoadingIndicator
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.theme.Typography
import com.github.onlynotesswent.ui.user.ThumbnailPic
import com.github.onlynotesswent.ui.user.switchProfileTo
import com.github.onlynotesswent.utils.ProfilePictureTaker

@Composable
fun DeckScreen(
    userViewModel: UserViewModel,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel,
    profilePictureTaker: ProfilePictureTaker,
    navigationActions: NavigationActions
) {
  val selectedDeck = deckViewModel.selectedDeck.collectAsState()
  val deckFlashcards = flashcardViewModel.deckFlashcards.collectAsState()
  val belongsToUser =
      selectedDeck.value?.userId == userViewModel.currentUser.collectAsState().value?.uid
  val fabDropdownMenuShown = remember { mutableStateOf(false) }
  val author: MutableState<User?> = remember { mutableStateOf(null) }

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
              onAddCardClick = { /* TODO */},
              onImportDeckClick = { /* TODO */})
        }
      }) { innerPadding ->
        if (selectedDeck.value == null) {
          LoadingIndicator("Loading deck...")
        } else {
          Column(modifier = Modifier.padding(innerPadding)) {
            // Card count and author name
            Row {
              Text(
                  text = selectedDeck.value!!.flashcardIds.size.toString() + " cards",
                  style = Typography.bodyMedium,
                  fontStyle = FontStyle.Italic)
              VerticalDivider()
              author.value?.let { user ->
                Row(
                    modifier =
                        Modifier.clickable {
                          switchProfileTo(user, userViewModel, navigationActions)
                        }) {
                      ThumbnailPic(user, fileViewModel)
                      Text(
                          text = user.userHandle(),
                          style = Typography.bodyMedium,
                          fontStyle = FontStyle.Italic)
                    }
              }
            }
            // Deck Title
            Text(selectedDeck.value!!.name, style = Typography.displayMedium)
            // Deck description
            Text(selectedDeck.value!!.description, style = Typography.bodyMedium)
            // Deck play mode buttons
            Button(
                onClick = {
                  deckViewModel.selectDeck(selectedDeck.value!!)
                  navigationActions.navigateTo(
                      Screen.DECK_PLAY.replace("{deckId}", selectedDeck.value!!.id))
                }) {
                  Text("Play")
                }

            // Deck cards
            LazyColumn {
              items(deckFlashcards.value.size) { index ->
                FlashcardViewItem(
                    flashcard = deckFlashcards.value[index],
                    deckViewModel = deckViewModel,
                    flashcardViewModel = flashcardViewModel,
                    fileViewModel = fileViewModel,
                    profilePictureTaker = profilePictureTaker,
                    belongsToUser = belongsToUser)
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
    profilePictureTaker: ProfilePictureTaker,
    belongsToUser: Boolean = false
) {
  val dropdownMenuExpanded = remember { mutableStateOf(false) }
  val editDialogExpanded = remember { mutableStateOf(false) }

  if (editDialogExpanded.value) {
    FlashcardDialog(
        deckViewModel,
        flashcardViewModel,
        profilePictureTaker,
        { editDialogExpanded.value = false },
        mode = "Edit")
  }

  if (dropdownMenuExpanded.value) {
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

  Card(modifier = Modifier.testTag("flashcardItem")) {
    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
          // Show front and options icon
          Row {
            Text(flashcard.front, style = Typography.bodyMedium)
            Icon(
                modifier =
                    Modifier.testTag("flashcardOptions").clickable(enabled = belongsToUser) {
                      dropdownMenuExpanded.value = true
                    },
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer)
          }
          if (flashcard is ImageFlashcard) {
            // Show image
            val imageUri: MutableState<String?> = remember { mutableStateOf(null) }
            if (imageUri.value == null) {
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
          HorizontalDivider(modifier = Modifier.height(5.dp))
          // Show back
          Text(flashcard.back, style = Typography.bodyMedium)
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
                  text = { Text("Add card") }, icon = {}, onClick = onAddCardClick),
              CustomDropDownMenuItem(
                  text = { Text("Import deck") }, icon = {}, onClick = onImportDeckClick)),
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
    profilePictureTaker: ProfilePictureTaker,
    onDismissRequest: () -> Unit,
    mode: String = "Create"
) {
  val flashcard = flashcardViewModel.selectedFlashcard.collectAsState()
  val front = remember { mutableStateOf(flashcard.value?.front ?: "") }
  val back = remember { mutableStateOf(flashcard.value?.back ?: "") }
  Dialog(onDismissRequest = onDismissRequest) {
    Column {
      Text("$mode Flashcard")
      // Front
      TextField(
          value = front.value, onValueChange = { front.value = it }, label = { Text("Front") })
      if (flashcard.value is ImageFlashcard) {
        // Image
        val imageUri: MutableState<String?> = remember { mutableStateOf(null) }
        val hasImageBeenChanged = remember { mutableStateOf(false) }
        IconButton(
            onClick = {
              profilePictureTaker.setOnImageSelected { uri ->
                if (uri != null) {
                  imageUri.value = uri.toString()
                  hasImageBeenChanged.value = true
                }
              }
              profilePictureTaker.pickImage()
            }) {
              Icon(imageVector = Icons.Default.ImageSearch, contentDescription = "Add image")
            }

        imageUri.value?.let {
          Image(
              painter = rememberAsyncImagePainter(it),
              contentDescription = "Flashcard image",
              modifier = Modifier.height(100.dp))
        }
      }
      HorizontalDivider()
      // Back
      TextField(value = back.value, onValueChange = { back.value = it }, label = { Text("Back") })
      // Save button
      Button(
          onClick = {
            var newFlashcard: Flashcard? = null
            if (flashcard.value is TextFlashcard) {
              val textFlashcard = flashcard.value as TextFlashcard
              newFlashcard = textFlashcard.copy(front = front.value, back = back.value)
            }
            if (flashcard.value is ImageFlashcard) {
              newFlashcard =
                  ImageFlashcard(
                      id = flashcard.value!!.id,
                      front = front.value,
                      back = back.value,
                      imageUrl = (flashcard.value as ImageFlashcard).imageUrl,
                      lastReviewed = flashcard.value!!.lastReviewed,
                      userId = flashcard.value!!.userId,
                      folderId = flashcard.value!!.folderId,
                      noteId = flashcard.value!!.noteId)
            }

            newFlashcard?.let {
              flashcardViewModel.updateFlashcard(it)
              deckViewModel.selectedDeck.value?.let { deck ->
                val newDeck = deck.copy(flashcardIds = deck.flashcardIds + it.id)
                deckViewModel.updateDeck(newDeck)
              }
            }
            onDismissRequest()
          }) {
            Text("Save")
          }
    }
  }
}
