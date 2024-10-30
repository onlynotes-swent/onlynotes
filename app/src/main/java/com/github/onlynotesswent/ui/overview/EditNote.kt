package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.note.Class
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.note.Type
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.util.Calendar

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(navigationActions: NavigationActions, noteViewModel: NoteViewModel) {
  val note by noteViewModel.note.collectAsState()
  val currentYear = Calendar.getInstance().get(Calendar.YEAR)
  var updatedNoteText by remember { mutableStateOf(note?.content ?: "") }
  var updatedNoteTitle by remember { mutableStateOf(note?.title ?: "") }
  var updatedClassName by remember { mutableStateOf(note?.noteClass?.className ?: "") }
  var updatedClassCode by remember { mutableStateOf(note?.noteClass?.classCode ?: "") }
  var updatedClassYear by remember { mutableIntStateOf(note?.noteClass?.classYear ?: currentYear) }
  var visibility by remember { mutableStateOf(visibilityToString(note?.public ?: false)) }
  var expandedVisibility by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag("editNoteScreen"),
      topBar = {
        TopAppBar(
            title = {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.weight(1.5f))
                    Text("Edit note", Modifier.testTag("editNoteTitle"))
                    Spacer(modifier = Modifier.weight(2f))
                  }
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
      content = { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
                    .testTag("editNoteColumn"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              item {
                OutlinedTextField(
                    value = updatedNoteTitle,
                    onValueChange = { updatedNoteTitle = it },
                    label = { Text("Note Title") },
                    placeholder = { Text("Enter the new title here") },
                    modifier = Modifier.fillMaxWidth().testTag("EditTitle textField"))
              }

              item {
                OptionDropDownMenu(
                    value = visibility,
                    expanded = expandedVisibility,
                    buttonTag = "visibilityEditButton",
                    menuTag = "visibilityEditMenu",
                    onExpandedChange = { expandedVisibility = it },
                    items = listOf("Public", "Private"),
                    onItemClick = { visibility = it })
              }

              item {
                OutlinedTextField(
                    value = updatedClassName,
                    onValueChange = { updatedClassName = it },
                    label = { Text("Class Name") },
                    placeholder = { Text("Set the Class Name for the Note") },
                    modifier = Modifier.fillMaxWidth().testTag("EditClassName textField"))
              }

              item {
                OutlinedTextField(
                    value = updatedClassCode,
                    onValueChange = { updatedClassCode = it },
                    label = { Text("Class Code") },
                    placeholder = { Text("Set the Class Code for the Note") },
                    modifier = Modifier.fillMaxWidth().testTag("EditClassCode textField"))
              }

              item {
                OutlinedTextField(
                    value = updatedClassYear.toString(),
                    onValueChange = { updatedClassYear = it.toIntOrNull() ?: currentYear },
                    label = { Text("Class Year") },
                    placeholder = { Text("Set the Class Year for the Note") },
                    modifier = Modifier.fillMaxWidth().testTag("EditClassYear textField"))
              }

              item {
                OutlinedTextField(
                    value =
                        if (note?.type == Type.NORMAL_TEXT) "Typed note"
                        else note?.type?.name ?: "Typed note",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Note Type") },
                    modifier = Modifier.fillMaxWidth().testTag("EditType textField"))
              }
              if (note?.type == Type.NORMAL_TEXT) {
                item {
                  OutlinedTextField(
                      value = updatedNoteText,
                      onValueChange = { updatedNoteText = it },
                      label = { Text("Note Content") },
                      placeholder = { Text("Enter your note here...") },
                      modifier =
                          Modifier.fillMaxWidth().height(400.dp).testTag("EditNote textField"))
                }
              }

              item {
                Button(
                    onClick = {
                      noteViewModel.updateNote(
                          Note(
                              id = note?.id ?: "1",
                              type = note?.type ?: Type.NORMAL_TEXT,
                              title = updatedNoteTitle,
                              content = updatedNoteText,
                              date = Timestamp.now(), // Use current timestamp
                              public = (visibility == "Public"),
                              userId = note?.userId ?: "1",
                              noteClass =
                                  Class(
                                      updatedClassCode, updatedClassName, updatedClassYear, "path"),
                              image =
                                  note?.image
                                      ?: Bitmap.createBitmap(
                                          1, 1, Bitmap.Config.ARGB_8888) // Placeholder Bitmap
                              ),
                          "1")
                      navigationActions.navigateTo(Screen.OVERVIEW)
                    },
                    modifier = Modifier.testTag("Save button")) {
                      Text("Update note")
                    }
              }

              item {
                Button(
                    onClick = {
                      noteViewModel.deleteNoteById(note?.id ?: "1", note?.userId ?: "1")
                      navigationActions.navigateTo(Screen.OVERVIEW)
                    },
                    modifier = Modifier.testTag("Delete button")) {
                      Text("Delete note")
                    }
              }
            }
      })
}

/**
 * Converts a boolean visibility value to its string representation.
 *
 * @param visibility The visibility value to convert.
 * @return The string representation of the visibility value.
 */
fun visibilityToString(visibility: Boolean): String {
  return if (visibility) "Public" else "Private"
}
