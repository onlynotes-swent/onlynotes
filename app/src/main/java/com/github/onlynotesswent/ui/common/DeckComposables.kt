package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.flashcard.deck.Deck
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays a deck item in the search screen.
 *
 * @param deck The deck to display.
 * @param author The author of the deck.
 * @param onClick The action to perform when the deck is clicked.
 */
@Composable
fun DeckSearchItem(deck: Deck, author: String, onClick: () -> Unit) {
  Card(
      Modifier.testTag("deckCard")
          .padding(4.dp)
          .semantics(mergeDescendants = true, properties = {})
          .fillMaxWidth()
          .clickable(onClick = onClick)) {
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
                text = "${deck.flashcardIds.size} cards",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 10.dp))
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
              text = deck.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(0.dp))
          Text(
              text = author,
              style = MaterialTheme.typography.bodySmall,
              fontStyle = FontStyle.Italic,
              modifier = Modifier.padding(0.dp))
        }
      }
}
