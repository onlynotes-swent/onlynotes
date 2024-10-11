package com.github.onlynotesswent.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen

@Composable
fun AddNote(navigationActions: NavigationActions, noteViewModel: NoteViewModel) {
  Column {
    Text("Add Note Screen", modifier = Modifier.testTag("AddNote text"))
    Button(
        onClick = { navigationActions.navigateTo(Screen.AUTH) },
        modifier = Modifier.testTag("GoBack button")) {
          Text("Go to Auth")
        }
  }
}
