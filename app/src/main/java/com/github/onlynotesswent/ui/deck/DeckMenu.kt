package com.github.onlynotesswent.ui.deck

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
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
          Column(modifier = Modifier.fillMaxWidth().padding(innerPadding).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Card count and author name
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                  text = selectedDeck.value!!.flashcardIds.size.let { count ->
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
                      ThumbnailPic(user, fileViewModel, size=25)
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
            Text(selectedDeck.value!!.description, style = Typography.bodyMedium, modifier = Modifier.padding(10.dp))
            // Deck play mode buttons
            Button(
                onClick = {
                  deckViewModel.selectDeck(selectedDeck.value!!)
                  // TODO: implement play screen
                  // navigationActions.navigateTo(
                  //    Screen.DECK_PLAY.replace("{deckId}", selectedDeck.value!!.id))
                }) {
                  Text("Play")
                }

            // Deck cards
            LazyColumn(
                modifier = Modifier.fillMaxWidth(0.9f).padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
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
        fileViewModel,
        { editDialogExpanded.value = false },
        mode = "Edit")
  }

  if (dropdownMenuExpanded.value) {
      Row(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
          Spacer(modifier = Modifier.weight(1f))
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
  }

  Card(modifier = Modifier.testTag("flashcardItem").fillMaxWidth()) {
    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
          // Show front and options icon
          Row {
            Text(flashcard.front, style = Typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier =
                    Modifier.testTag("flashcardOptions").clickable(enabled = belongsToUser) {
                      dropdownMenuExpanded.value = true
                    },
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer)
          }
          if (flashcard.hasImage) {
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
                  text = { Text("Add card") }, icon = {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add card")
                  }, onClick = onAddCardClick),
              CustomDropDownMenuItem(
                  text = { Text("Import deck") }, icon = {
                Icon(imageVector = Icons.Default.Download, contentDescription = "Import deck")
                  }, onClick = onImportDeckClick)),
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
    fileViewModel: FileViewModel,
    onDismissRequest: () -> Unit,
    mode: String = "Create"
) {
  val flashcard = flashcardViewModel.selectedFlashcard.collectAsState()
  val front = remember { mutableStateOf(flashcard.value?.front ?: "") }
  val back = remember { mutableStateOf(flashcard.value?.back ?: "") }
  Dialog(onDismissRequest = onDismissRequest) {
      Card {
          if (flashcard.value == null) {
              LoadingIndicator("Loading flashcard...")
          }
          else {
              Column(
                  modifier = Modifier.padding(10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                  Text("$mode Flashcard", style = Typography.headlineSmall)
                  // Front
                  OutlinedTextField(
                      value = front.value,
                      onValueChange = { front.value = it },
                      label = { Text("Front") })

                  // Image
                  val imageUri: MutableState<String?> = remember { mutableStateOf(null) }
                  val hasImageBeenChanged = remember { mutableStateOf(false) }

                  Row {
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
                          Icon(
                              imageVector = Icons.Default.ImageSearch,
                              contentDescription = "Add image"
                          )
                      }
                      if (imageUri.value != null) {
                          IconButton(
                              onClick = {
                                  imageUri.value = null
                                  hasImageBeenChanged.value = true
                              }) {
                              Icon(
                                  imageVector = Icons.Default.HideImage,
                                  contentDescription = "Remove image"
                              )
                          }
                      }

                  }


                  if (flashcard.value!!.hasImage && imageUri.value == null && !hasImageBeenChanged.value) {
                      fileViewModel.downloadFile(
                          flashcard.value!!.id,
                          FileType.FLASHCARD_IMAGE,
                          context = LocalContext.current,
                          onSuccess = { file -> imageUri.value = file.absolutePath }
                      )
                      Text("Image is being downloaded...", style = Typography.bodyMedium)
                  }

                  if (!flashcard.value!!.hasImage && imageUri.value == null && !hasImageBeenChanged.value) {
                      Text("Add an image", style = Typography.bodyMedium)
                  }

                  imageUri.value?.let {
                      Image(
                          painter = rememberAsyncImagePainter(it),
                          contentDescription = "Flashcard image",
                          modifier = Modifier.height(100.dp)
                      )
                  }

                  HorizontalDivider(thickness = 5.dp)
                  // Back
                  OutlinedTextField(
                      value = back.value,
                      onValueChange = { back.value = it },
                      label = { Text("Back") })
                  // Save button
                  OutlinedButton(
                      onClick = {
                          val newHasImage = if (hasImageBeenChanged.value) imageUri.value!=null else flashcard.value!!.hasImage

                          if (hasImageBeenChanged.value && imageUri.value != null) {
                              fileViewModel.uploadFile(
                                  flashcard.value!!.id,
                                  imageUri.value!!.toUri(),
                                  FileType.FLASHCARD_IMAGE,
                                  onSuccess = { hasImageBeenChanged.value = false },
                                  onFailure = {  }
                              )
                          }
                          else if (hasImageBeenChanged.value && imageUri.value == null) {
                              fileViewModel.deleteFile(
                                  flashcard.value!!.id,
                                  FileType.FLASHCARD_IMAGE,
                                  onSuccess = { hasImageBeenChanged.value = false },
                                  onFileNotFound = {  },
                                  onFailure = {  }
                              )
                          }

                          flashcard.value!!.copy(front = front.value, back = back.value, hasImage = newHasImage).let {
                              flashcardViewModel.updateFlashcard(it)
                              if (mode == "Create") {
                                  deckViewModel.selectedDeck.value?.let { deck ->
                                      val newDeck = deck.copy(flashcardIds = deck.flashcardIds + it.id)
                                      deckViewModel.updateDeck(newDeck)
                                  }
                              }
                              flashcardViewModel.fetchFlashcardsFromDeck(deckViewModel.selectedDeck.value!!)
                          }
                          onDismissRequest()
                      }) {
                      Text("Save")
                  }
              }
          }
      }
  }
}
