package com.github.onlynotesswent.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen

@Composable
fun AddNote(navigationActions: NavigationActions, noteViewModel: NoteViewModel) {
  Column {
    Text("Add Note Screen")
    Button(onClick = { navigationActions.navigateTo(Screen.AUTH) }) { Text("Go to Auth") }
  }
}
