package com.github.onlynotesswent.ui.common

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.theme.Typography
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays a deck item in the search screen.
 *
 * @param deck The deck to display.
 * @param author The author of the deck.
 * @param onClick The action to perform when the deck is clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckItem(
    deck: Deck,
    deckViewModel: DeckViewModel? = null,
    author: String? = null,
    onClick: () -> Unit
) {
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
          }) {
        Column(
            Modifier.testTag("deckColumn").padding(10.dp).fillMaxWidth(),
        ) {
          Row {
            Text(
                text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(deck.lastModified.toDate()),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 10.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${deck.flashcardIds.size} " + stringResource(R.string.cards_min),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 10.dp))
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
      }
}

/**
 * Composable function that displays a dialog for editing the selected deck, or creating a new one.
 * The dialog contains fields for the deck title, description, and visibility.
 *
 * @param deckViewModel The ViewModel for deck-related data.
 * @param userViewModel The ViewModel for user-related data.
 * @param onDismissRequest The callback to be invoked when the dialog is dismissed.
 * @param mode The mode of the dialog, default is "Edit".
 */
@Composable
fun EditDeckDialog(
    deckViewModel: DeckViewModel,
    userViewModel: UserViewModel,
    onDismissRequest: () -> Unit,
    onSave: (() -> Unit)? = null,
    mode: String = stringResource(R.string.edit_maj),
) {
  val deck: State<Deck?> = deckViewModel.selectedDeck.collectAsState()
  val deckTitle = remember { mutableStateOf(deck.value?.name ?: "") }
  val deckDescription = remember { mutableStateOf(deck.value?.description ?: "") }
  val deckVisibility = remember { mutableStateOf(deck.value?.visibility ?: Visibility.DEFAULT) }
  val currentUser = userViewModel.currentUser.collectAsState().value

  Dialog(onDismissRequest = onDismissRequest) {
    Card(modifier = Modifier.testTag("editDeckDialog").padding(5.dp)) {
      Column(
          modifier = Modifier.padding(10.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("$mode " + stringResource(R.string.deck_maj), style = Typography.headlineSmall)
            OutlinedTextField(
                value = deckTitle.value,
                onValueChange = { deckTitle.value = Deck.formatTitle(it) },
                maxLines = 1,
                modifier = Modifier.testTag("deckTitleTextField"),
            )
            SelectVisibility(deckVisibility.value, currentUser!!.uid == (deck.value?.userId ?: "")) {
              deckVisibility.value = it
            }
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
                  onSave?.invoke()
                  onDismissRequest()
                }) {
                  Text(stringResource(R.string.save))
                }
          }
    }
  }
}
