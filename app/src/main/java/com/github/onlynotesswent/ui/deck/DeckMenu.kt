package com.github.onlynotesswent.ui.deck

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
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

@Composable
fun DeckScreen(
    userViewModel: UserViewModel,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel,
    navigationActions: NavigationActions
) {
  val selectedDeck = deckViewModel.selectedDeck.collectAsState().value
  val belongsToUser = selectedDeck?.userId == userViewModel.currentUser.collectAsState().value?.uid
  val fabDropdownMenuShown = remember { mutableStateOf(false) }
  val author: MutableState<User?> = remember { mutableStateOf(null) }

  selectedDeck?.userId?.let { userId ->
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
        if (selectedDeck == null) {
            LoadingIndicator("Loading deck...")
        }
        else {
            Column(modifier = Modifier.padding(innerPadding)) {
                // Card count and author name
                Row {
                    Text(
                        text = selectedDeck.flashcardIds.size.toString() + " cards",
                        style = Typography.bodyMedium,
                        fontStyle = FontStyle.Italic
                    )
                    VerticalDivider()
                    author.value?.let { user ->
                        Row(
                            modifier =
                            Modifier.clickable {
                                userViewModel.setProfileUser(user)
                                navigationActions.navigateTo(Screen.PUBLIC_PROFILE)
                            }) {
                            ThumbnailPic(user, fileViewModel)
                            Text(
                                text = user.userHandle(),
                                style = Typography.bodyMedium,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
                // Deck Title
                Text(selectedDeck.name, style = Typography.displayMedium)
                // Deck description
                Text(selectedDeck.description, style = Typography.bodyMedium)
                // Deck play mode buttons

                // Deck cards
                LazyColumn {
                    items(selectedDeck.flashcardIds.size) { flashcardId ->
                        FlashcardViewItem(
                            flashcardId = selectedDeck.flashcardIds[flashcardId],
                            deckViewModel = deckViewModel,
                            flashcardViewModel = flashcardViewModel,
                            fileViewModel = fileViewModel,
                            navigationActions = navigationActions
                        )
                    }
                }
            }
        }
      }
}

@Composable
fun FlashcardViewItem(
    flashcardId: String,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel,
    navigationActions: NavigationActions
) { /* TODO */}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckMenuTopAppBar(onBackButtonClick: () -> Unit) {
    ScreenTopBar(
        title = "Deck",
        titleTestTag = "deckTopBarTitle",
        onBackClick = onBackButtonClick,
        icon = { Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back") },
        iconTestTag = "backButton"
    )
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
