package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.note.Type
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.google.firebase.Timestamp

/**
 * Displays the edit note screen, where users can update the title and content of an existing note.
 * The screen includes two text fields for editing the note's title and content, and a button to
 * save the changes. The save button updates the note in the ViewModel and navigates back to the
 * overview screen.
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 */
@Composable
fun EditNoteScreen(navigationActions: NavigationActions, noteViewModel: NoteViewModel) {
  val note by noteViewModel.note.collectAsState()
  var updatedNoteText by remember { mutableStateOf(note?.content ?: "") } // Keep track of changes
  var updatedNoteTitle by remember { mutableStateOf(note?.title ?: "") } // Keep track of changes

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Edit Note Screen", modifier = Modifier.testTag("EditNote text"))

        OutlinedTextField(
            value = updatedNoteTitle,
            onValueChange = { newTitle: String -> updatedNoteTitle = newTitle },
            label = { Text("Note Title") },
            placeholder = { Text("Enter the new title here") },
            modifier = Modifier.fillMaxWidth().testTag("EditTitle textField"))

        OutlinedTextField(
            value = updatedNoteText,
            onValueChange = { newText: String -> updatedNoteText = newText },
            label = { Text("Note Content") },
            placeholder = { Text("Enter your note here...") },
            modifier = Modifier.fillMaxWidth().height(400.dp).testTag("EditNote textField"))

        Button(
            onClick = {
              noteViewModel.updateNote(
                  Note(
                      id = note?.id ?: "1",
                      type = Type.NORMAL_TEXT,
                      title = updatedNoteTitle,
                      content = updatedNoteText,
                      date = Timestamp.now(), // Use current timestamp
                      public = note?.public ?: true,
                      userId = note?.userId ?: "1",
                      image =
                          note?.image
                              ?: Bitmap.createBitmap(
                                  1, 1, Bitmap.Config.ARGB_8888) // Placeholder Bitmap
                      ),
                  "1")
              navigationActions.goBack()
            },
            modifier = Modifier.testTag("Save button")) {
              Text("Save")
            }

        Button(
            onClick = {
              noteViewModel.deleteNoteById(note?.id ?: "1", note?.userId ?: "1")
              navigationActions.goBack()
            },
            modifier = Modifier.testTag("Delete button")) {
              Text("Delete")
            }
      }
}
