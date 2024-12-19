package com.github.onlynotesswent.ui.common

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route.DECK_OVERVIEW
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays a deck item in the search screen.
 *
 * @param deck The deck to display.
 * @param deckViewModel The ViewModel used to update the deck.
 * @param author The author of the deck.
 * @param onClick The action to perform when the deck is clicked.
 * @param folderViewModel The ViewModel used to move the deck.
 * @param currentUser The current user.
 * @param navigationActions The navigation instance used to transition between different screens.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckItem(
    deck: Deck,
    deckViewModel: DeckViewModel? = null,
    author: String? = null,
    onClick: () -> Unit,
    folderViewModel: FolderViewModel,
    currentUser: User,
    navigationActions: NavigationActions
) {
  var showBottomSheet by remember { mutableStateOf(false) }

  if (deckViewModel != null && showBottomSheet) {
    DeckOptionsBottomSheet(
        deck = deck,
        deckViewModel = deckViewModel,
        folderViewModel = folderViewModel,
        onDismiss = { showBottomSheet = false },
        navigationActions = navigationActions)
  }

  Card(
      Modifier.testTag("deckCard")
          .height(140.dp)
          .padding(4.dp)
          .semantics(mergeDescendants = true, properties = {})
          .fillMaxWidth()
          // Enable drag and drop for the note card as a source
          .dragAndDropSource {
            detectTapGestures(
                onTap = { onClick() },
                onLongPress = {
                  if (deckViewModel != null) {
                    deckViewModel.draggedDeck(deck)
                    // Start a drag-and-drop operation to transfer the data which is being dragged
                    startTransfer(
                        // Transfer the note Id as a ClipData object
                        DragAndDropTransferData(ClipData.newPlainText("Deck", deck.id)))
                  }
                },
            )
          },
      colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.testTag("deckColumn").fillMaxWidth().padding(8.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(deck.lastModified.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)

                if (deck.userId == currentUser.uid &&
                    navigationActions.currentRoute() != Screen.SEARCH) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier =
                            Modifier.testTag("showBottomSheetButton").size(24.dp).clickable {
                              showBottomSheet = true
                            },
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                  }
                }
              }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
              text = deck.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(0.dp))
          if (author != null) {
            Text(
                text = author,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(0.dp))
          }
        }

        Row(
            modifier = Modifier.fillMaxHeight().padding(bottom = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
          Text(
              text = "${deck.flashcardIds.size} " + stringResource(R.string.cards_min),
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(start = 10.dp))
        }
      }
}

/**
 * Displays a bottom sheet with options to move or delete a deck. The bottom sheet is displayed when
 * the user clicks on the more options icon in the deck card.
 *
 * @param deck The deck to move or delete.
 * @param deckViewModel The deckViewModel used here to update the deck.
 * @param folderViewModel the folderViewModel used here to move the note.
 * @param onDismiss The callback to be invoked when the bottom sheet is dismissed.
 * @param navigationActions The navigation instance used to transition between different screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckOptionsBottomSheet(
    deck: Deck,
    deckViewModel: DeckViewModel,
    folderViewModel: FolderViewModel,
    onDismiss: () -> Unit,
    navigationActions: NavigationActions
) {
  var showFileSystemPopup by remember { mutableStateOf(false) }
  var showDeletePopup by remember { mutableStateOf(false) }

  if (showFileSystemPopup) {
    FileSystemPopup(
        onDismiss = { showFileSystemPopup = false },
        folderViewModel = folderViewModel,
        onMoveHere = { selectedFolder ->
          deckViewModel.updateDeck(
              deck.copy(folderId = selectedFolder?.id, lastModified = Timestamp.now()))
          showFileSystemPopup = false
          folderViewModel.clearSelectedFolder()
          if (selectedFolder != null) {
            navigationActions.navigateTo(
                Screen.FOLDER_CONTENTS.replace(
                    oldValue = "{folderId}", newValue = selectedFolder.id))
          } else {
            navigationActions.navigateTo(DECK_OVERVIEW)
          }

          onDismiss() // Dismiss the bottom sheet after moving the note
        })
  }

  if (showDeletePopup) {
    ConfirmationPopup(
        title = stringResource(R.string.delete_deck),
        text = stringResource(R.string.delete_deck_text),
        onConfirm = {
          deckViewModel.deleteDeck(deck)
          if (folderViewModel.selectedFolder.value != null) {
            deckViewModel.getDecksByFolder(folderViewModel.selectedFolder.value!!.id)
          } else {
            deckViewModel.getRootDecksFromUserId(deck.userId)
          }
          showDeletePopup = false // Close the dialog after deleting
        },
        onDismiss = {
          showDeletePopup = false // Close the dialog without deleting
        })
  }

  ModalBottomSheet(
      modifier = Modifier.testTag("deckModalBottomSheet"),
      onDismissRequest = onDismiss,
      content = {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .clickable { showFileSystemPopup = true }
                      .padding(vertical = 8.dp)
                      .testTag("moveDeckBottomSheet"),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = stringResource(R.string.move_deck))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.move_deck),
                    style = MaterialTheme.typography.titleMedium)
              }

          HorizontalDivider(Modifier.padding(vertical = 10.dp), 1.dp)

          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .clickable { showDeletePopup = true }
                      .padding(vertical = 8.dp)
                      .testTag("deleteDeckBottomSheet"),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete_deck),
                    tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.delete_deck),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error)
              }
        }
      })
}

/**
 * Composable function that displays a dialog for editing the selected deck, or creating a new one.
 * The dialog contains fields for the deck title, description, and visibility.
 *
 * @param deckViewModel The ViewModel for deck-related data.
 * @param userViewModel The ViewModel for user-related data.
 * @param onDismissRequest The callback to be invoked when the dialog is dismissed.
 * @param mode The mode of the dialog, default is "Edit".
 * @param folderId The ID of the folder the deck belongs to.
 */
@Composable
fun EditDeckDialog(
    deckViewModel: DeckViewModel,
    userViewModel: UserViewModel,
    onDismissRequest: () -> Unit,
    onSave: (() -> Unit)? = null,
    mode: String = stringResource(R.string.update),
    folderId: String? = null
) {
  val deck: State<Deck?> = deckViewModel.selectedDeck.collectAsState()
  val deckTitle = remember { mutableStateOf(deck.value?.name ?: "") }
  val deckDescription = remember { mutableStateOf(deck.value?.description ?: "") }
  val deckVisibility = remember { mutableStateOf(deck.value?.visibility ?: Visibility.DEFAULT) }

  AlertDialog(
      onDismissRequest = onDismissRequest,
      title = { Text("$mode Deck") },
      text = {
        Column(
            modifier = Modifier.padding(16.dp).testTag("DeckDialog"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              OutlinedTextField(
                  value = deckTitle.value,
                  onValueChange = { deckTitle.value = Deck.formatTitle(it) },
                  label = { Text(stringResource(R.string.name)) },
                  maxLines = 1,
                  modifier = Modifier.testTag("deckTitleTextField"),
              )
              SelectVisibility(deckVisibility.value, true) { deckVisibility.value = it }
              OutlinedTextField(
                  value = deckDescription.value,
                  onValueChange = { deckDescription.value = Deck.formatDescription(it) },
                  label = { Text(stringResource(R.string.description)) },
                  minLines = 2,
                  maxLines = 5,
                  modifier = Modifier.testTag("deckDescriptionTextField"))
            }
      },
      confirmButton = {
        Button(
            enabled = deckTitle.value.isNotBlank(),
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
                          folderId = folderId,
                          visibility = deckVisibility.value,
                          description = deckDescription.value,
                          lastModified = Timestamp.now())
              deckViewModel.updateDeck(newDeck, { deckViewModel.selectDeck(newDeck) })
              onSave?.invoke()
              onDismissRequest()
            }) {
              Text(mode)
            }
      },
      dismissButton = {
        OutlinedButton(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            onClick = onDismissRequest,
            modifier = Modifier.testTag("cancelButton")) {
              Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.error)
            }
      })
}
