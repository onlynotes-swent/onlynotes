package com.github.onlynotesswent.ui.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
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
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays the overview screen which contains a list of publicNotes retrieved from the ViewModel.
 * If there are no publicNotes, it shows a text to the user indicating no publicNotes are available.
 * It also provides a floating action button to add a new note.
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to display.
 */
@Composable
fun OverviewScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel
) {
  val userNotes = noteViewModel.userNotes.collectAsState()
  userViewModel.currentUser.collectAsState().value?.let { noteViewModel.getNotesFrom(it.uid) }

  Scaffold(
      modifier = Modifier.testTag("overviewScreen"),
      floatingActionButton = {
        FloatingActionButton(
            onClick = { navigationActions.navigateTo(Screen.ADD_NOTE) },
            modifier = Modifier.testTag("createNote")) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "AddNote")
            }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { pd ->
        Box(modifier = Modifier.fillMaxSize().padding(pd)) {
          if (userNotes.value.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 40.dp),
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(pd)
                        .testTag("noteList")) {
                  items(userNotes.value.size) { index ->
                    NoteItem(note = userNotes.value[index]) {
                      noteViewModel.selectedNote(userNotes.value[index])
                      navigationActions.navigateTo(Screen.EDIT_NOTE)
                    }
                  }
                }
          } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(pd),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(modifier = Modifier.testTag("emptyNotePrompt"), text = "You have no Notes yet.")
              Spacer(modifier = Modifier.height(50.dp))
              RefreshButton {
                userViewModel.currentUser.value?.let { noteViewModel.getNotesFrom(it.uid) }
              }
              Spacer(modifier = Modifier.height(20.dp))
            }
          }
        }
      }
}

/**
 * A composable function that displays a refresh button.
 *
 * @param onClick A lambda function to be invoked when the button is clicked.
 */
@Composable
fun RefreshButton(onClick: () -> Unit) {
  ElevatedButton(onClick = onClick, modifier = Modifier.testTag("refreshButton")) {
    Text("Refresh")
    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
  }
}

/**
 * Displays a single note item in a card format. The card contains the note's date, name, and user
 * ID. When clicked, it triggers the provided [onClick] action, which can be used for navigation or
 * other interactions.
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
      colors = CardDefaults.cardColors(containerColor = Color(0xFFB3E5FC))) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(note.date.toDate()),
                    style = MaterialTheme.typography.bodySmall)

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                      contentDescription = null)
                }
              }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
              text = note.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold)
          Text(text = note.id, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
      }
}
