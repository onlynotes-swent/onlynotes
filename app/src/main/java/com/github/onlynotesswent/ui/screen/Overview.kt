package com.github.onlynotesswent.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays the overview screen which contains a list of notes retrieved from the ViewModel.
 * If there are no notes, it shows a text to the user indicating no notes are available.
 * It also provides a floating action button to add a new note.
 *
 * @param navigationActions The  navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the list of notes to display.
 */
@Composable
fun OverviewScreen(navigationActions: NavigationActions, noteViewModel: NoteViewModel) {
  val notes = noteViewModel.notes.collectAsState()

  Scaffold(
      modifier = Modifier.testTag("overviewScreen"),
      floatingActionButton = {
        FloatingActionButton(
            onClick = { navigationActions.navigateTo(Screen.ADD_NOTE) },
            modifier = Modifier.testTag("createNote")) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "AddNote")
            }
      }
  ) { pd ->
      Box() {
          if (notes.value.isNotEmpty()) {
              LazyColumn(
                  contentPadding = PaddingValues(vertical = 40.dp),
                  modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 16.dp)
                      .padding(pd)
                      .testTag("noteList")
              ) {
                  items(notes.value.size) { index ->
                      NoteItem(
                          note = notes.value[index]
                      ) { navigationActions.navigateTo(Screen.EDIT_NOTE) }
                  }
              }
          } else {
              Text(
                  modifier = Modifier.padding(pd).testTag("emptyNotePrompt"),
                  text = "You have no Notes yet."
              )
          }
          FloatingActionButton(
              modifier =
              Modifier.align(Alignment.BottomCenter).padding(20.dp).testTag("RefreshButton"),
              onClick = { noteViewModel.getNotes("1") }) {
              Text("Refresh")
          }
      }
  }
}

/**
 * Displays a single note item in a card format. The card contains the note's date, name,
 * and user ID. When clicked, it triggers the provided [onClick] action, which can be used
 * for navigation or other interactions.
 *
 * @param note The note data that will be displayed in this card.
 * @param onClick The lambda function to be invoked when the note card is clicked.
 */

@Composable
fun NoteItem(note: Note, onClick: () -> Unit) {
  Card(
      modifier =
          Modifier.testTag("noteCard")
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable(onClick = onClick),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFB3E5FC))
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(note.date.toDate()),
            style = MaterialTheme.typography.bodySmall)

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
          text = note.name,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold)
      Text(text = note.userId, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
  }
}
